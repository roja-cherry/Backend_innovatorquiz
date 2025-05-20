package com.iq.quiz.controller

import com.iq.quiz.Dto.QuizLoginDto
import com.iq.quiz.Entity.QuizAttempt
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.service.ParticipantService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.iq.quiz.service.QuizScheduleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/participant")
class ParticipantScheduleController(
    private val participantService: ParticipantService,
    private  val scheduleService: QuizScheduleService
) {
    @PostMapping("/submit")
    fun submit(
        @RequestParam userId: String,
        @RequestParam scheduleId: String,
        @RequestBody answers: Map<String,String>
    ): QuizAttempt {
        return participantService.submitAndScore(userId, scheduleId, answers)
    }

    @GetMapping("/schedule/{scheduleId}")
    fun getSchedule(@PathVariable scheduleId: String): ResponseEntity<Schedule> {
        val schedule = scheduleService.getScheduleById(scheduleId)
        return ResponseEntity.ok(schedule)
    }
}