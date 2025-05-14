package com.iq.quiz.controller

import com.iq.quiz.service.QuizService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/quiz")
class QuizController(private val quizService: QuizService) {
}