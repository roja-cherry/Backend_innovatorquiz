package com.iq.quiz.service

import com.iq.quiz.Entity.Question
import com.iq.quiz.Entity.Quiz
import com.iq.quiz.exception.FileFormatException
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ExcelService {

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
}