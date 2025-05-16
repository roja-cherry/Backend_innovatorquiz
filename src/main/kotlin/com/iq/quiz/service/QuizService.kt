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

        // 📝 Filter by quizName
        search?.let {
            predicates.add(cb.like(cb.lower(root.get("quizName")), "%${it.lowercase()}%"))
        }

        startDate?.let {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), it))
        }
        endDate?.let {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), it))
        }

        // 🚦 Filter by status
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
        if (file.isEmpty) {
            throw FileFormatException("Uploaded file is empty.")
        }

        if(timer < 5 || timer > 60) {
            throw QuizException("Timer should between 5 to 60 minutes", HttpStatus.BAD_REQUEST)
        }

        val quiz = Quiz(
            quizName = quizName,
            timer = timer,
            status = QuizStatus.CREATED,
            createdAt = LocalDateTime.now()
        )
        val savedQuiz = quizRepository.save(quiz)

        val questions = excelService.extractQuestionsFromExcel(file, savedQuiz)
        questionRepository.saveAll(questions)

        val quizDto = quizToDto(savedQuiz)
        val questionDTOs = questions.map { questionToDto(it) }

        logger.info("New quiz created:  $savedQuiz")
        logger.info("Questions created: $questions")

        return QuizWithQuestionsDto(quiz = quizDto, questions = questionDTOs)
    }

    fun getQuizWithQuestions(quizId: String): QuizWithQuestionsDto {
        val quiz = quizRepository.findByQuizId(quizId)
            ?: throw QuizNotFoundException("Quiz Not Found")

        val questions = questionRepository.findByQuizQuizId(quizId) // ✅ Corrected repository call

        val quizDto = QuizDTO(
            quizId = quiz.quizId,
            quizName = quiz.quizName,
            timer = quiz.timer,
            createdAt = quiz.createdAt,
            status = quiz.status
        )

        val questionDtos = questions.map { question ->
            QuestionDTO(
                questionId = question.questionId,
                question = question.question,
                option1 = question.option1,
                option2 = question.option2,
                option3 = question.option3,
                option4 = question.option4,
                correctAnswer = question.correctAnswer
            )
        }

        return QuizWithQuestionsDto(
            quiz = quizDto,
            questions = questionDtos
        )
    }


    fun getAllQuizzesFiltered(
        sortBy: String?,
        search: String?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        status: QuizStatus?
    ): List<QuizDTO> {
        val sort = Sort.by(Sort.Direction.ASC, sortBy ?: "createdAt")
        val spec = quizSpecification(search, startDate, endDate, status)
        val quizzes = quizRepository.findAll(spec, sort)
        return quizzes.map { quiz -> quizToDto(quiz) }
    }


    @Transactional
    fun editQuiz(id: String, quizName: String?, timer: Long?, file: MultipartFile?): QuizWithQuestionsDto {
        val quiz = quizRepository.findById(id)
            .orElseThrow { QuizNotFoundException("Quiz with id '$id' not found") }

        if(quiz.status != QuizStatus.CREATED && quiz.status != QuizStatus.PUBLISHED)
            throw QuizException("Can't edit quiz, status is ${quiz.status.text}", HttpStatus.BAD_REQUEST)

        val updatedQuiz = quiz.copy(
            quizName = quizName ?: quiz.quizName,
            timer    = timer ?: quiz?.timer!!
        )

        file?.let {
            questionRepository.deleteAllByQuizQuizId(id)
            val newQuestions = excelService.extractQuestionsFromExcel(it, updatedQuiz)
            questionRepository.saveAll(newQuestions)
        }
        quizRepository.save(updatedQuiz)

        logger.info("Edit quiz with id $id: fields => file? $file, quizName? $quizName, timer? $timer")

        val questions = questionRepository.findByQuizQuizId(id).map { questionToDto(it) }
        return QuizWithQuestionsDto(
            quiz = quizToDto(updatedQuiz),
            questions = questions
        )
    }

    @Transactional
    fun deleteQuiz(quizId: String) {
        val quiz = quizRepository.findById(quizId)
            .orElseThrow { QuizNotFoundException("Quiz with id '$quizId' not found") }

        if (quiz.status != QuizStatus.CREATED && quiz.status != QuizStatus.COMPLETED) {
            throw QuizException(
                "Only quizzes with status 'Created' or 'Completed' can be deleted. Current status: ${quiz.status.text}",
                HttpStatus.BAD_REQUEST
            )
        }


        scheduleRepository.deleteAllByQuizQuizId(quizId)
        questionRepository.deleteAllByQuizQuizId(quizId)
        quizRepository.deleteById(quizId)
    }


    fun getAllQuizzesForAdmin(status: QuizStatus?): List<QuizDTO> {
        val quizzes = if (status != null) {
            quizRepository.findByStatus(status)
        } else {
            quizRepository.findAll()
        }

        return quizzes.map { quiz ->
            QuizDTO(
                quizId = quiz.quizId,
                quizName = quiz.quizName,
                timer = quiz.timer,
                createdAt = quiz.createdAt,
                status = quiz.status
            )
        }
    }
    //fun for search

    fun searchQuizzes(keyword: String): List<QuizDTO> {
        val results = quizRepository.searchByKeyword(keyword)
        return results.map { quizToQuizDto(it) }
    }



}