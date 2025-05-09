package com.iq.quiz.service

import com.iq.quiz.Dto.QuestionDTO
import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Dto.QuizWithQuestionsDto
import com.iq.quiz.Dto.UserDTO
import com.iq.quiz.Entity.Question
import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.Repository.QuestionRepository
import com.iq.quiz.Repository.QuizRepository
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
    private val questionRepository: QuestionRepository
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
            status = QuizStatus.CREATED,
            createdBy = null,
            createdAt = LocalDateTime.now(),
            isActive = false
        )

        val savedQuiz = quizRepository.save(quiz)
        val questions = extractQuestionsFromExcel(file, savedQuiz)
        questionRepository.saveAll(questions)

        val quizDto = quizToQuizDto(savedQuiz)
        val questionDTOs = questions.map { questionToQuestionsDto(it) }

        return QuizWithQuestionsDto(quiz = quizDto, questions = questionDTOs)
    }


    fun getQuizDto(quizId: String): QuizDTO? {
        val quiz= quizRepository.findByQuizId(quizId) ?: throw QuizNotFoundException("Quiz Not Found")
        return (quizToQuizDto(quiz))
    }

    @Transactional
    fun editQuiz(
        file: MultipartFile?,
        quizName: String?,
        timer: Int?,
        id: String
    ): QuizWithQuestionsDto {
        val quiz = quizRepository.findById(id)
            .orElseThrow { QuizNotFoundException("Quiz not found") }

        // Check for duplicate name if quizName is being changed
        if (quizName != null && quizName != quiz.quizName) {
            val duplicateExists = quizRepository.existsByQuizName(quizName)
            if (duplicateExists) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "A quiz with the name '$quizName' already exists.")
            }
        }

        val updatedQuiz = quiz.copy(
            quizName = quizName ?: quiz.quizName,
            timer = timer ?: quiz.timer
        )

        if (file != null) {
            questionRepository.deleteAllByQuizQuizId(id)
            val questions = extractQuestionsFromExcel(file, updatedQuiz)
            questionRepository.saveAll(questions)
        }

        quizRepository.save(updatedQuiz)
        val questions = questionRepository.findByQuizQuizId(id)
        val questionsDto = questions.map { questionToQuestionsDto(it) }

        return QuizWithQuestionsDto(
            quiz = quizToQuizDto(updatedQuiz),
            questions = questionsDto
        )
    }

    fun quizToQuizDto(quiz: Quiz): QuizDTO {
        return QuizDTO(
            quizId = quiz.quizId,
            quizName = quiz.quizName,
            timer = quiz.timer,
            status = quiz.status,
            createdBy = quiz.createdBy?.let {
                UserDTO(
                    username = it.userName,
                    email = it.email,
                    password = it.password,
                    role = it.role
                )
            },
            createdAt = quiz.createdAt,
            isActive = quiz.isActive,
            quizStartDateTime = quiz.quizStartDateTime,
            quizEndDateTime = quiz.quizEndDateTime
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

    fun extractQuestionsFromExcel(file: MultipartFile, quiz: Quiz) : List<Question> {
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
            quiz.isActive == isActive &&
                    (status == null || quiz.status == status) &&
                    (createdWithin == null || when (createdWithin) {
                        "before6m" -> quiz.createdAt?.isBefore(cutoffDate) == true
                        else -> quiz.createdAt?.isAfter(cutoffDate) == true
                    })
        }.map { quiz ->
            QuizDTO(
                quizId = quiz.quizId,
                quizName = quiz.quizName,
                timer = quiz.timer,
                status = quiz.status,
                isActive = quiz.isActive,
                createdBy = quiz.createdBy?.let { user ->
                    UserDTO(
                        username = user.userName,
                        email = user.email,
                        password = user.password,
                        role = user.role
                    )
                },
                createdAt = quiz.createdAt,
                quizStartDateTime = quiz.quizStartDateTime,
                quizEndDateTime = quiz.quizEndDateTime
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
            ?: throw QuizNotFoundException("Quiz not found with ID: $quizId")

        questionRepository.deleteAllByQuizQuizId(quizId)
        quizRepository.delete(quiz)
    }

    @Transactional
    fun publishQuiz(publishDto: PublishQuizRequest): QuizDTO {
        val quiz = quizRepository.findById(publishDto.quizId)
            .orElseThrow { QuizNotFoundException("Quiz not found with id ${publishDto.quizId}") }

        if(quiz.status == QuizStatus.COMPLETED)
            throw QuizException("Can't publish quiz, quiz completed", HttpStatus.BAD_REQUEST)


        val publishedQuiz = quiz.copy(
            status = QuizStatus.PUBLISHED,
            quizStartDateTime = publishDto.quizStartDateTime,
            quizEndDateTime = publishDto.quizEndDateTime
        )
    fun getQuizWithQuestions(quizId: String): QuizWithQuestionsDto {
        val quiz = quizRepository.findByQuizId(quizId)
            ?: throw RuntimeException("Quiz not found with id: $quizId")

        val questions = questionRepository.findByQuizQuizId(quizId)

        val quizDto = QuizDTO(
            quizId = quiz.quizId,
            quizName = quiz.quizName,
            timer = quiz.timer,
            status = quiz.status,
            createdBy = null, // Optional: map createdBy -> UserDTO if needed
            createdAt = quiz.createdAt,
            isActive = quiz.isActive
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

        val savedQuiz = quizRepository.save(publishedQuiz)
        return quizToQuizDto(savedQuiz)
    }

    fun updateIsActive(id: String, active: Boolean):QuizDTO {
        val quiz=quizRepository.findByQuizId(id)?:throw QuizNotFoundException("Quiz not found with ID: $id")
        quiz.isActive=active;
        quizRepository.save(quiz)
        val updatedQuiz=quizToQuizDto(quiz)
        return updatedQuiz
    }
}