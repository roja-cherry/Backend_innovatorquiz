package com.iq.quiz.service

import com.iq.quiz.Dto.QuestionDTO
import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Dto.QuizWithQuestionsDto
import com.iq.quiz.Entity.Question
import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.Repository.QuestionRepository
import com.iq.quiz.Repository.QuizRepository
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class AdminService(
    private val quizRepository: QuizRepository,
    private val questionRepository: QuestionRepository
) {

    fun processQuizFile(file: MultipartFile, quizName: String, duration: Int): QuizWithQuestionsDto {
        val quiz = Quiz(
            quizName = quizName,
            duration = duration,
            status = QuizStatus.INACTIVE,
            createdBy = null
        )

        val savedQuiz = quizRepository.save(quiz)

        val questions = mutableListOf<Question>()

        file.inputStream.use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            for ((index, row) in sheet.withIndex()) {
                if (index == 0) continue

                val questionText = row.getCell(0)?.stringCellValue?.trim() ?: ""
                val optionA = row.getCell(1)?.stringCellValue?.trim() ?: ""
                val optionB = row.getCell(2)?.stringCellValue?.trim() ?: ""
                val optionC = row.getCell(3)?.stringCellValue?.trim() ?: ""
                val optionD = row.getCell(4)?.stringCellValue?.trim() ?: ""
                val correctAnswerCell = row.getCell(5)
                val correctAnswer = when (correctAnswerCell.cellType) {
                    org.apache.poi.ss.usermodel.CellType.STRING -> correctAnswerCell.stringCellValue.trim()
                    org.apache.poi.ss.usermodel.CellType.NUMERIC -> correctAnswerCell.numericCellValue.toInt()
                        .toString()

                    else -> throw IllegalArgumentException("Invalid data in correct answer")
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

        val quizDto = QuizDTO(
            quizName = savedQuiz.quizName,
            duration = savedQuiz.duration,
            status = savedQuiz.status,
            createdByUserId = savedQuiz.createdBy ?: UUID.randomUUID() // or handle null properly
        )

        val questionDtos = questions.map {
            QuestionDTO(
                quizId = savedQuiz.quizId,
                question = it.question,
                option1 = it.option1,
                option2 = it.option2,
                option3 = it.option3,
                option4 = it.option4,
                correctAnswer = it.correctAnswer
            )
        }

        return QuizWithQuestionsDto(quiz = quizDto, questions = questionDtos)
    }
}