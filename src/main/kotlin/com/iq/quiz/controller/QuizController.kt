package com.iq.quiz.controller

import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Dto.QuizWithQuestionsDto
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.service.QuizService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/quiz")
class QuizController(private val quizService: QuizService) {

    @PostMapping
    fun createQuiz(
        @RequestParam quizName: String,
        @RequestParam timer: Long,
        @RequestParam file: MultipartFile
    ): ResponseEntity<QuizWithQuestionsDto> {
        val response = quizService.createNewQuiz(quizName, timer, file)
        return ResponseEntity.status(201).body(response)
    }

    @DeleteMapping("/{quizId}")
    fun deleteQuiz(@PathVariable quizId: String): ResponseEntity<Void> {
        quizService.deleteQuiz(quizId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}")
    fun editQuiz(
        @PathVariable id: String,
        @RequestParam quizName: String?,
        @RequestParam timer: Long?,
        @RequestParam file: MultipartFile?
    ): QuizWithQuestionsDto {
        return quizService.editQuiz(id, quizName, timer, file)
    }

    @GetMapping("/quizzes")
    fun getAllQuizzesForAdmin(
        @RequestParam(required = false) status: QuizStatus?
    ): List<QuizDTO> {
        return quizService.getAllQuizzesForAdmin(status)
    }
}