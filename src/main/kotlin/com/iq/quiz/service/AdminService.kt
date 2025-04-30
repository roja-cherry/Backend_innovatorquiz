package com.iq.quiz.service

import com.iq.quiz.Entity.Question
import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.Repository.QuestionRepository
import com.iq.quiz.Repository.QuizRepository
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class AdminService(
    private val quizRepository: QuizRepository,
    private val questionRepository: QuestionRepository
) {

    fun processQuizFile(file: MultipartFile, quizName: String, duration: Int) {
        println("Received Quiz Name: $quizName")
        println("Time Duration: $duration minutes")

        // Create and save Quiz (assumes createdBy will be set later or handled elsewhere)
        val quiz = Quiz(
            quizName = quizName,
            duration = duration,
            status = QuizStatus.INACTIVE,
            createdBy = null // Set user later as per your flow
        )

        val savedQuiz = quizRepository.save(quiz)

        // Read Excel
        val inputStream = file.inputStream
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)

        val questions = mutableListOf<Question>()

        for ((index, row) in sheet.withIndex()) {
            if (index == 0) continue // skip header

            val questionText = row.getCell(0)?.stringCellValue ?: ""
            val optionA = row.getCell(1)?.stringCellValue ?: ""
            val optionB = row.getCell(2)?.stringCellValue ?: ""
            val optionC = row.getCell(3)?.stringCellValue ?: ""
            val optionD = row.getCell(4)?.stringCellValue ?: ""
            val correctAnswer = row.getCell(5)?.stringCellValue ?: ""

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
            println(question)
        }

        questionRepository.saveAll(questions)
        workbook.close()

        println("Quiz and ${questions.size} questions saved successfully.")
    }
}
