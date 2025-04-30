package com.iq.quiz.service

import com.iq.quiz.model.Question
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class AdminService {

    fun processQuizFile(file: MultipartFile, quizName: String, duration: Int) {
        println("Received Quiz Name: $quizName")
        println("Time Duration: $duration minutes")

        val inputStream = file.inputStream
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)

        println("Questions from Excel:")
        for ((index, row) in sheet.withIndex()) {
            if (index == 0) continue // skip header

            val questionText = row.getCell(0)?.stringCellValue ?: ""
            val optionA = row.getCell(1)?.stringCellValue ?: ""
            val optionB = row.getCell(2)?.stringCellValue ?: ""
            val optionC = row.getCell(3)?.stringCellValue ?: ""
            val optionD = row.getCell(4)?.stringCellValue ?: ""
            val correctAnswer = row.getCell(5)?.stringCellValue ?: ""

            val question = Question(
                text = questionText,
                optionA = optionA,
                optionB = optionB,
                optionC = optionC,
                optionD = optionD,
                correctAnswer = correctAnswer
            )

            println(question)
        }

        workbook.close()
    }
}
