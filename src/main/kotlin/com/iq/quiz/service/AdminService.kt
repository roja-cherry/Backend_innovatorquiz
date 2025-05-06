package com.iq.quiz.service

import com.iq.quiz.Dto.QuestionDTO
import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Dto.QuizWithQuestionsDto
import com.iq.quiz.Dto.UserDTO
import com.iq.quiz.Entity.Question
import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.Entity.User
import com.iq.quiz.Repository.QuestionRepository
import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.exception.FileFormatException
import com.iq.quiz.exception.QuizNotFoundException
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.*

@Service
class AdminService(
    private val quizRepository: QuizRepository,
    private val questionRepository: QuestionRepository
) {

    fun processQuizFile(file: MultipartFile, quizName: String, duration: Int): QuizWithQuestionsDto {
        if (file.isEmpty) {
            throw FileFormatException("Uploaded file is empty.")
        }

        val quiz = Quiz(
            quizName = quizName,
            duration = duration,
            status = QuizStatus.CREATED,
            createdBy = null,
            createdAt = LocalDateTime.now()
        )
        val savedQuiz = quizRepository.save(quiz)

        val questions = extractQuestionsFromExcel(file, savedQuiz)
        questionRepository.saveAll(questions)

        val quizDto = QuizDTO(
            quizId = savedQuiz.quizId,
            quizName = savedQuiz.quizName,
            duration = savedQuiz.duration,
            status = savedQuiz.status,
            createdBy = null, // No user mapping for now, setting to empty string
            createdAt = savedQuiz.createdAt
        )

        val questionDTOs = questions.map {
            QuestionDTO(
                questionId = it.questionId,
                question = it.question,
                option1 = it.option1,
                option2 = it.option2,
                option3 = it.option3,
                option4 = it.option4,
                correctAnswer = it.correctAnswer
            )
        }

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
        duration: Int?,
        id: String
    ): QuizWithQuestionsDto {
        val quiz = quizRepository.findById(id)
            .orElseThrow({ QuizNotFoundException("Quiz not found") })

        val updatedQuiz = quiz.copy(
            quizName = quizName ?: quiz.quizName,
            duration = duration ?: quiz.duration
        )

        if(file != null) {
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
            duration = quiz.duration,
            status = quiz.status,
            createdBy = quiz.createdBy?.let {
                UserDTO(
                    username = it.userName,
                    email = it.email,
                    password = it.password,
                    role = it.role
                )
            },
            createdAt = quiz.createdAt
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
                    questions.add(question)
                }
                workbook.close()
                return questions
            }
        } catch (e: FileFormatException) {
            throw e
        } catch (e: Exception) {
            throw FileFormatException("Error processing file: ${e.localizedMessage}")
        }
    }

    fun getAllQuizzesForAdmin(
        search: String?,
        minDuration: Int?,
        status: QuizStatus?,
        createdAfter: LocalDateTime?
    ): List<QuizDTO> {
        // Fetch all quizzes
        val quizzes = quizRepository.findAll()

        // Apply filters
        return quizzes.filter { quiz ->
            (search == null || quiz.quizName.contains(search, ignoreCase = true)) &&
                    (minDuration == null || quiz.duration >= minDuration) &&
                    (status == null || quiz.status == status) &&
                    (createdAfter == null || quiz.createdAt?.isAfter(createdAfter) == true)
        }.map { quiz ->
            QuizDTO(
                quizId = quiz.quizId,
                quizName = quiz.quizName,
                duration = quiz.duration,
                status = quiz.status,
                createdBy = quiz.createdBy?.let { user ->
                    UserDTO(
                        username = user.userName,
                        email = user.email,
                        password = user.password,
                        role = user.role
                    )
                },
                createdAt = quiz.createdAt
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


}