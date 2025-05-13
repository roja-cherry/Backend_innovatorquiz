package com.iq.quiz.service

import com.iq.quiz.Dto.*
import com.iq.quiz.Entity.Question
import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.Entity.ScheduleStatus
import com.iq.quiz.Repository.QuestionRepository
import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.Repository.ScheduleRepository
import com.iq.quiz.exception.FileFormatException
import com.iq.quiz.exception.QuizException
import com.iq.quiz.exception.QuizNotFoundException
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class AdminService(
    private val quizRepository: QuizRepository,
    private val questionRepository: QuestionRepository,
    private val scheduleRepository: ScheduleRepository
) {

    @Transactional
    fun processQuizFile(file: MultipartFile, quizName: String, timer: Int): QuizWithQuestionsDto {
        if (file.isEmpty) {
            throw FileFormatException("Uploaded file is empty.")
        }

        if (quizRepository.existsByQuizName(quizName)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A quiz with the name '$quizName' already exists.")
        }

        val quiz = Quiz(
            quizName = quizName,
            timer = timer,
            createdAt = LocalDateTime.now(),
        )

        val savedQuiz = quizRepository.save(quiz)
        val questions = extractQuestionsFromExcel(file, savedQuiz)
        questionRepository.saveAll(questions)

        val quizDto = quizToQuizDto(savedQuiz)
        val questionDTOs = questions.map { questionToQuestionsDto(it) }

        return QuizWithQuestionsDto(quiz = quizDto, questions = questionDTOs)
    }


    fun getQuizDto(quizId: String): QuizDTO? {
        val quiz = quizRepository.findByQuizId(quizId) ?: throw QuizNotFoundException("Quiz Not Found")
        return (quizToQuizDto(quiz))
    }

    @Transactional
    fun editQuiz(
        file: MultipartFile?,
        quizName: String?,
        timer: Int?,
        id: String
    ): QuizWithQuestionsDto {
        // 1. Load or 404
        val quiz = quizRepository.findById(id)
            .orElseThrow { QuizNotFoundException("Quiz with id '$id' not found") }

        // 2. Prevent edits if already scheduled or live
        val hasSchedule = scheduleRepository.existsByQuizQuizIdAndStatusIn(
            quizId   = id,
            statuses = listOf(ScheduleStatus.SCHEDULED, ScheduleStatus.LIVE)
        )
        if (hasSchedule) {
            throw QuizException(
                status  = HttpStatus.FORBIDDEN,
                message = "Cannot edit quiz '${quiz.quizName}' once it’s scheduled or live."
            )
        }
        // 4. Apply updates
        val updatedQuiz = quiz.copy(
            quizName = quizName ?: quiz.quizName,
            timer    = timer    ?: quiz.timer
        )
        // 5. Replace questions if file provided
        file?.let {
            questionRepository.deleteAllByQuizQuizId(id)
            questionRepository.saveAll(extractQuestionsFromExcel(it, updatedQuiz))
        }

        quizRepository.save(updatedQuiz)

        return QuizWithQuestionsDto(
            quiz      = quizToQuizDto(updatedQuiz),
            questions = questionRepository.findByQuizQuizId(id).map(::questionToQuestionsDto)
        )
    }

    fun quizToQuizDto(quiz: Quiz): QuizDTO {
        return QuizDTO(
            quizId = quiz.quizId,
            quizName = quiz.quizName,
            timer = quiz.timer,
            createdAt = quiz.createdAt,
        )
    }

    fun questionToQuestionsDto(question: Question): QuestionDTO {
        return QuestionDTO(
            questionId = question.questionId,
            question = question.question,
            option1 = question.option1,
            option2 = question.option2,
            option3 = question.option3,
            option4 = question.option4,
            correctAnswer = question.correctAnswer
        )
    }

    fun extractQuestionsFromExcel(file: MultipartFile, quiz: Quiz): List<Question> {
        val questions = mutableListOf<Question>()

        try {
            file.inputStream.use { inputStream ->
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)

                for ((index, row) in sheet.withIndex()) {
                    if (index == 0) continue

                    val questionText = row.getCell(0)?.stringCellValue?.trim()
                        ?: throw FileFormatException("Missing question text in row ${index + 1}")
                    val optionA = row.getCell(1)?.stringCellValue?.trim()
                        ?: throw FileFormatException("Missing option 1 in row ${index + 1}")
                    val optionB = row.getCell(2)?.stringCellValue?.trim()
                        ?: throw FileFormatException("Missing option 2 in row ${index + 1}")
                    val optionC = row.getCell(3)?.stringCellValue?.trim()
                        ?: throw FileFormatException("Missing option 3 in row ${index + 1}")
                    val optionD = row.getCell(4)?.stringCellValue?.trim()
                        ?: throw FileFormatException("Missing option 4 in row ${index + 1}")

                    val correctAnswerCell = row.getCell(5)
                        ?: throw FileFormatException("Missing correct answer in row ${index + 1}")
                    val correctAnswer = when (correctAnswerCell.cellType) {
                        org.apache.poi.ss.usermodel.CellType.STRING ->
                            correctAnswerCell.stringCellValue.trim()

                        org.apache.poi.ss.usermodel.CellType.NUMERIC ->
                            correctAnswerCell.numericCellValue.toInt().toString()

                        else -> throw FileFormatException("Invalid correct answer format in row ${index + 1}")
                    }

                    val question = Question(
                        quiz = quiz,
                        question = questionText,
                        option1 = optionA,
                        option2 = optionB,
                        option3 = optionC,
                        option4 = optionD,
                        correctAnswer = correctAnswer
                    )
                    println(question)
                    questions.add(question)
                }
                workbook.close()
                return questions
            }
        } catch (e: FileFormatException) {
            throw e
        } catch (e: Exception) {
            println(e.message)
            throw FileFormatException("Error processing file: File content not supported")
        }
    }

    fun getAllQuizzesForAdmin(
        isActive: Boolean,
        status: QuizStatus?,
        createdWithin: String? // "1m", "3m", "6m", "before6m"
    ): List<QuizDTO> {
        val now = LocalDateTime.now()

        val cutoffDate: LocalDateTime? = when (createdWithin) {
            "1m" -> now.minusMonths(1)
            "3m" -> now.minusMonths(3)
            "6m" -> now.minusMonths(6)
            "before6m" -> now.minusMonths(6)
            else -> null
        }

        return quizRepository.findAll().filter { quiz ->
            (createdWithin == null || when (createdWithin) {
                "before6m" -> quiz.createdAt?.isBefore(cutoffDate) == true
                else -> quiz.createdAt?.isAfter(cutoffDate) == true
            })
        }.map { quiz ->
            val scheduled = scheduleRepository.existsByQuizQuizIdAndStatusIn(
                quizId = quiz.quizId!!,
                statuses = listOf(ScheduleStatus.SCHEDULED, ScheduleStatus.LIVE)
            )
            QuizDTO(
                quizId     = quiz.quizId,
                quizName   = quiz.quizName,
                timer      = quiz.timer,
                createdAt  = quiz.createdAt,
                isScheduled = scheduled
            )
        }
    }


    //fun for search

    fun searchQuizzes(keyword: String): List<QuizDTO> {
        val results = quizRepository.searchByKeyword(keyword)
        return results.map { quizToQuizDto(it) }
    }

    //Delete

    @Transactional
    fun deleteQuizById(quizId: String) {
        val quiz = quizRepository.findByQuizId(quizId)
            ?: throw QuizNotFoundException("Quiz with id '$quizId' not found")

        // Block if scheduled/live
        val hasSchedule = scheduleRepository.existsByQuizQuizIdAndStatusIn(
            quizId   = quizId,
            statuses = listOf(ScheduleStatus.SCHEDULED, ScheduleStatus.LIVE)
        )
        if (hasSchedule) {
            throw QuizException(
                status  = HttpStatus.FORBIDDEN,
                message = "Cannot delete quiz '${quiz.quizName}' once it’s scheduled or live."
            )
        }

        questionRepository.deleteAllByQuizQuizId(quizId)
        quizRepository.delete(quiz)
    }

    fun updateIsActive(id: String, active: Boolean): QuizDTO {
        val quiz = quizRepository.findByQuizId(id) ?: throw QuizNotFoundException("Quiz not found with ID: $id")
        quizRepository.save(quiz)
        val updatedQuiz = quizToQuizDto(quiz)
        return updatedQuiz
    }

    fun getQuizWithQuestions(quizId: String): QuizWithQuestionsDto {
        val quiz = quizRepository.findByQuizId(quizId)
            ?: throw RuntimeException("Quiz not found with id: $quizId")

        val questions = questionRepository.findByQuizQuizId(quizId)

        val quizDto = QuizDTO(
            quizId = quiz.quizId,
            quizName = quiz.quizName,
            timer = quiz.timer,
            createdAt = quiz.createdAt,
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
}