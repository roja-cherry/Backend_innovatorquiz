package com.iq.quiz.controller

import com.iq.quiz.service.QuizScheduleService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/schedule")
class QuizScheduleController(
    private val scheduleService: QuizScheduleService
) {
}