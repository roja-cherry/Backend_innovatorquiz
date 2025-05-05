package com.iq.quiz.service

import com.iq.quiz.Dto.QuestionDTO
import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Dto.QuizWithQuestionsDto
import com.iq.quiz.Entity.Question
import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.Repository.QuestionRepository
import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.exception.FileFormatException
import org.apache.coyote.Response
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
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
            status = QuizStatus.INACTIVE,
            createdBy = null
        )
        val savedQuiz = quizRepository.save(quiz)

        val questions = mutableListOf<Question>()

        try {
            file.inputStream.use { inputStream ->
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)

                for ((index, row) in sheet.withIndex()) {
                    if (index == 0) continue // Skip header row

                    // Validate row cells
                    val questionText = row.getCell(0)?.stringCellValue?.trim()
                        ?: throw FileFormatException("Missing question text in row ${index + 1}")
                    val optionA = row.getCell(1)?.stringCellValue?.trim()
                        ?: throw FileFormatException("Missing option A in row ${index + 1}")
                    val optionB = row.getCell(2)?.stringCellValue?.trim()
                        ?: throw FileFormatException("Missing option B in row ${index + 1}")
                    val optionC = row.getCell(3)?.stringCellValue?.trim()
                        ?: throw FileFormatException("Missing option C in row ${index + 1}")
                    val optionD = row.getCell(4)?.stringCellValue?.trim()
                        ?: throw FileFormatException("Missing option D in row ${index + 1}")

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
                        quiz = savedQuiz,
                        question = questionText,
                        option1 = optionA,
                        option2 = optionB,
                        option3 = optionC,
                        option4 = optionD,
                        correctAnswer = correctAnswer
                    )
                    questions.add(question)
                }

                questionRepository.saveAll(questions)
                workbook.close()
            }
        } catch (e: FileFormatException) {
            throw e
        } catch (e: Exception) {
            throw FileFormatException("Error processing file: ${e.localizedMessage}")
        }

        val quizDto = QuizDTO(
            quizId = savedQuiz.quizId,
            quizName = savedQuiz.quizName,
            duration = savedQuiz.duration,
            status = savedQuiz.status,
            createdByUserId = savedQuiz.createdBy ?: UUID.randomUUID()
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

    fun getQuizDtoService(quizId: String): QuizDTO? {
        val quiz= quizRepository.findByQuizId(quizId)?:throw ResponseStatusException(HttpStatus.NOT_FOUND,"Quiz Not Found")
        return (quiz)
    }
}