package com.iq.quiz.controller

import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Dto.QuizWithQuestionsDto
import com.iq.quiz.service.QuizService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
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
}