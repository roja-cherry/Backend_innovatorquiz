package com.iq.quiz.service

import com.iq.quiz.Dto.QuestionDTO
import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Dto.QuizWithQuestionsDto
import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.Repository.QuestionRepository
import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.Repository.ScheduleRepository
import com.iq.quiz.exception.FileFormatException
import com.iq.quiz.exception.QuizException
import com.iq.quiz.exception.QuizNotFoundException
import com.iq.quiz.mapper.questionToDto
import com.iq.quiz.mapper.quizToDto
import com.iq.quiz.mapper.quizToQuizDto
import jakarta.persistence.criteria.Predicate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

fun quizSpecification(
    search: String?,
    startDate: LocalDateTime?,
    endDate: LocalDateTime?,
    status: QuizStatus?
): Specification<Quiz> {
    return Specification { root, _, cb ->
        val predicates = mutableListOf<Predicate>()
        //Filter by quizName
        search?.let {
            predicates.add(cb.like(cb.lower(root.get("quizName")), "%${it.lowercase()}%"))
        }
        startDate?.let {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), it))
        }
        endDate?.let {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), it))
        }
        // Filter by status
        status?.let {
            predicates.add(cb.equal(root.get<QuizStatus>("status"), it))
        }
        cb.and(*predicates.toTypedArray())
    }
}



@Service
class QuizService(
    private val quizRepository: QuizRepository,
    private val scheduleRepository: ScheduleRepository,
    private val questionRepository: QuestionRepository,
    private val excelService: ExcelService
) {
    private val logger: Logger = LoggerFactory.getLogger(QuizService::class.java)

    @Transactional
    fun createNewQuiz(quizName: String, timer: Long, file: MultipartFile): QuizWithQuestionsDto {
        logger.info("createNewQuiz() called: name='{}', timer={}", quizName, timer)
        if (file.isEmpty) {
            logger.warn("createNewQuiz: uploaded file is empty for quiz='{}'", quizName)
            throw FileFormatException("Uploaded file is empty.")
        }

        if (timer < 5 || timer > 60) {
            logger.warn("createNewQuiz: invalid timer {} for quiz='{}'", timer, quizName)
            throw QuizException("Timer should between 5 to 60 minutes", HttpStatus.BAD_REQUEST)
        }

        val quiz = Quiz(
            quizName = quizName,
            timer = timer,
            status = QuizStatus.CREATED,
            createdAt = LocalDateTime.now()
        )
        val savedQuiz = quizRepository.save(quiz)
        logger.info("Quiz saved: id='{}', name='{}'", savedQuiz.quizId, savedQuiz.quizName)

        val questions = excelService.extractQuestionsFromExcel(file, savedQuiz)
        questionRepository.saveAll(questions)
        logger.debug("Saved {} questions for quizId='{}'", questions.size, savedQuiz.quizId)

        val result = QuizWithQuestionsDto(
            quiz = quizToDto(savedQuiz),
            questions = questions.map { questionToDto(it) }
        )
        logger.info("createNewQuiz() completed for quizId='{}'", savedQuiz.quizId)
        return result
    }

    fun getQuizWithQuestions(quizId: String): QuizWithQuestionsDto {
        logger.info("getQuizWithQuestions() called for quizId='{}'", quizId)
        val quiz = quizRepository.findByQuizId(quizId)
            ?: run {
                logger.error("getQuizWithQuestions: quiz not found for id='{}'", quizId)
                throw QuizNotFoundException("Quiz Not Found")
            }
        logger.debug("Fetched quiz: {} with status={}", quiz.quizName, quiz.status)

        val questions = questionRepository.findByQuizQuizId(quizId)
        logger.debug("Found {} questions for quizId='{}'", questions.size, quizId)

        val result = QuizWithQuestionsDto(
            quiz = QuizDTO(
                quizId = quiz.quizId,
                quizName = quiz.quizName,
                timer = quiz.timer,
                createdAt = quiz.createdAt,
                status = quiz.status
            ),
            questions = questions.map { q -> questionToDto(q) }
        )
        logger.info("getQuizWithQuestions() returning DTO for quizId='{}'", quizId)
        return result
    }

    fun getAllQuizzesFiltered(
        sortBy: String?,
        search: String?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        status: QuizStatus?
    ): List<QuizDTO> {
        logger.debug(
            "getAllQuizzesFiltered() called with sortBy='{}', search='{}', startDate='{}', endDate='{}', status='{}'",
            sortBy, search, startDate, endDate, status
        )

        val sort = Sort.by(Sort.Direction.ASC, sortBy ?: "createdAt")
        val spec = quizSpecification(search, startDate, endDate, status)
        val quizzes = quizRepository.findAll(spec, sort)
        logger.info("getAllQuizzesFiltered: found {} quizzes", quizzes.size)

        return quizzes.map { quizToDto(it) }
    }

    @Transactional
    fun editQuiz(id: String, quizName: String?, timer: Long?, file: MultipartFile?): QuizWithQuestionsDto {
        logger.info("editQuiz() called for quizId='{}'", id)
        val quiz = quizRepository.findById(id)
            .orElseThrow {
                logger.error("editQuiz: quiz not found id='{}'", id)
                QuizNotFoundException("Quiz with id '$id' not found")
            }

        if (quiz.status != QuizStatus.CREATED && quiz.status != QuizStatus.PUBLISHED) {
            logger.warn("editQuiz: cannot edit quizId='{}' with status='{}'", id, quiz.status)
            throw QuizException("Can't edit quiz, status is ${quiz.status.text}", HttpStatus.BAD_REQUEST)
        }

        val updatedQuiz = quiz.copy(
            quizName = quizName ?: quiz.quizName,
            timer    = timer   ?: quiz.timer
        )
        quizRepository.save(updatedQuiz)
        logger.info("Quiz updated: id='{}', name='{}'", updatedQuiz.quizId, updatedQuiz.quizName)

        file?.let {
            questionRepository.deleteAllByQuizQuizId(id)
            val newQuestions = excelService.extractQuestionsFromExcel(it, updatedQuiz)
            questionRepository.saveAll(newQuestions)
            logger.debug("Replaced {} questions for quizId='{}'", newQuestions.size, id)
        }

        val result = QuizWithQuestionsDto(
            quiz = quizToDto(updatedQuiz),
            questions = questionRepository.findByQuizQuizId(id).map { questionToDto(it) }
        )
        logger.info("editQuiz() completed for quizId='{}'", id)
        return result
    }

    @Transactional
    fun deleteQuiz(quizId: String) {
        logger.info("deleteQuiz() called for quizId='{}'", quizId)
        val quiz = quizRepository.findById(quizId)
            .orElseThrow {
                logger.error("deleteQuiz: quiz not found id='{}'", quizId)
                QuizNotFoundException("Quiz with id '$quizId' not found")
            }
        logger.debug("Found quiz to delete: {} (status={})", quiz.quizName, quiz.status)

        if (quiz.status != QuizStatus.CREATED && quiz.status != QuizStatus.COMPLETED) {
            logger.warn("deleteQuiz: cannot delete quizId='{}' with status='{}'", quizId, quiz.status)
            throw QuizException(
                "Only quizzes with status 'Created' or 'Completed' can be deleted. Current status: ${quiz.status.text}",
                HttpStatus.BAD_REQUEST
            )
        }

        val schedulesDeleted = scheduleRepository.deleteAllByQuizQuizId(quizId)
        logger.info("Deleted {} schedules for quizId='{}'", schedulesDeleted, quizId)

        val questionsDeleted = questionRepository.deleteAllByQuizQuizId(quizId)
        logger.info("Deleted {} questions for quizId='{}'", questionsDeleted, quizId)

        quizRepository.deleteById(quizId)
        logger.info("Quiz deletion completed for quizId='{}'", quizId)
    }

    fun getAllQuizzesForAdmin(status: QuizStatus?): List<QuizDTO> {
        logger.info("getAllQuizzesForAdmin() called with status='{}'", status)
        val quizzes = status
            ?.let { quizRepository.findByStatus(it) }
            ?: quizRepository.findAll()
        logger.info("getAllQuizzesForAdmin: found {} quizzes", quizzes.size)

        return quizzes.map {
            QuizDTO(
                quizId = it.quizId,
                quizName = it.quizName,
                timer = it.timer,
                createdAt = it.createdAt,
                status = it.status
            )
        }
    }

    fun searchQuizzes(keyword: String): List<QuizDTO> {
        logger.info("searchQuizzes() called with keyword='{}'", keyword)
        val results = quizRepository.searchByKeyword(keyword)
        logger.info("searchQuizzes: found {} results", results.size)
        return results.map { quizToQuizDto(it) }
    }
}