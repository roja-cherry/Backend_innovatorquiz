package com.iq.quiz.controller

import com.iq.quiz.Entity.QuizAttempt
import com.iq.quiz.service.ParticipantService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/participant")
class ParticipantScheduleController(
    private val participantService: ParticipantService
) {
    @PostMapping("/submit")
    fun submit(
        @RequestParam userId: String,
        @RequestParam scheduleId: String,
        @RequestBody answers: Map<String,String>
    ): QuizAttempt {
        return participantService.submitAndScore(userId, scheduleId, answers)
    }
}