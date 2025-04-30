package com.iq.quiz.service

import com.iq.quiz.Entity.Question
import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.Repository.QuestionRepository
import com.iq.quiz.Repository.QuizRepository
import org.apache.poi.ss.usermodel.CellType
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

        // Validate file type
        val fileName = file.originalFilename ?: throw IllegalArgumentException("File name is missing.")
        if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".csv")) {
            throw IllegalArgumentException("Invalid file type. Please upload a file with .xlsx or .csv extension.")
        }

        // Create and save Quiz
        val quiz = Quiz(
            quizName = quizName,
            duration = duration,
            status = QuizStatus.INACTIVE,
            createdBy = null
        )

        val savedQuiz = quizRepository.save(quiz)

        // Read Excel
        file.inputStream.use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            if (sheet.physicalNumberOfRows <= 1) {
                throw IllegalArgumentException("The uploaded file is empty or does not contain valid data.")
            }

            val questions = mutableListOf<Question>()

            for ((index, row) in sheet.withIndex()) {
                if (index == 0) continue // skip header

                // Validate row structure
                if (row.physicalNumberOfCells < 6) {
                    throw IllegalArgumentException("Invalid format in row ${index + 1}. Each row must have a question, 4 options, and the correct answer.")
                }

                val questionText = row.getCell(0)?.stringCellValue?.trim() ?: ""
                val optionA = row.getCell(1)?.stringCellValue?.trim() ?: ""
                val optionB = row.getCell(2)?.stringCellValue?.trim() ?: ""
                val optionC = row.getCell(3)?.stringCellValue?.trim() ?: ""
                val optionD = row.getCell(4)?.stringCellValue?.trim() ?: ""

                // Handle correct answer cell type
                val correctAnswerCell = row.getCell(5)
                val correctAnswer = when (correctAnswerCell.cellType) {
                    CellType.STRING -> correctAnswerCell.stringCellValue.trim()
                    CellType.NUMERIC -> correctAnswerCell.numericCellValue.toInt().toString()
                    else -> throw IllegalArgumentException("Invalid data type in row ${index + 1} for the correct answer.")
                }

                // Validate content
                if (questionText.isEmpty() || optionA.isEmpty() || optionB.isEmpty() || optionC.isEmpty() || optionD.isEmpty()) {
                    throw IllegalArgumentException("Invalid data in row ${index + 1}. Question and all 4 options are required.")
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
                println(question)
            }

            questionRepository.saveAll(questions)
            workbook.close()
            println("Quiz and ${questions.size} questions saved successfully.")
        }
    }
}