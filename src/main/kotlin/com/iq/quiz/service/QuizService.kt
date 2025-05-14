package com.iq.quiz.service

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
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class QuizService(
    private val quizRepository: QuizRepository,
    private val scheduleRepository: ScheduleRepository,
    private val questionRepository: QuestionRepository,
    private val excelService: ExcelService
) {

    @Transactional
    fun createNewQuiz(quizName: String, timer: Long, file: MultipartFile): QuizWithQuestionsDto {
        if (file.isEmpty) {
            throw FileFormatException("Uploaded file is empty.")
        }

        if (quizRepository.existsByQuizName(quizName)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A quiz with the name '$quizName' already exists.")
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

        return QuizWithQuestionsDto(quiz = quizDto, questions = questionDTOs)
    }

    @Transactional
    fun editQuiz(id: String, quizName: String, timer: Long, file: MultipartFile?): QuizWithQuestionsDto {
        val quiz = quizRepository.findById(id)
            .orElseThrow { QuizNotFoundException("Quiz with id '$id' not found") }

        if(quiz.status != QuizStatus.CREATED && quiz.status != QuizStatus.PUBLISHED)
            throw QuizException("Can't edit quiz, status is ${quiz.status.text}", HttpStatus.BAD_REQUEST)

        val updatedQuiz = quiz.copy(
            quizName = quizName,
            timer    = timer
        )

        file?.let {
            questionRepository.deleteAllByQuizQuizId(id)
            val newQuestions = excelService.extractQuestionsFromExcel(it, updatedQuiz)
            questionRepository.saveAll(newQuestions)
        }
        quizRepository.save(updatedQuiz)

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

        // Delete associated questions first
        questionRepository.deleteAllByQuizQuizId(quizId)

        // Delete the quiz
        quizRepository.deleteById(quizId)
    }
}