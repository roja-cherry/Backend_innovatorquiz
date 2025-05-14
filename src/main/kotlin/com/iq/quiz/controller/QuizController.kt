package com.iq.quiz.controller

import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Dto.QuizWithQuestionsDto
import com.iq.quiz.service.QuizService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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
}