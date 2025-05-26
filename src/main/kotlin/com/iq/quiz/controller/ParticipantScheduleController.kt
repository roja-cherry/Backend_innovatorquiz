package com.iq.quiz.controller

import com.iq.quiz.Dto.QuizLoginDto
import com.iq.quiz.Dto.QuestionWithoutAnswerDTO
import com.iq.quiz.Dto.QuizAttemptDTO
import com.iq.quiz.Dto.schedule.HomePageSchedule
import com.iq.quiz.Dto.schedule.ScheduleWithQuestionsDto
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
    private val scheduleService: QuizScheduleService
) {
    @PostMapping("/submit")
    fun submit(
        @RequestParam userId: String,
        @RequestParam scheduleId: String,
        @RequestBody answers: Map<String, String>
    ): QuizAttempt {
        return participantService.submitAndScore(userId, scheduleId, answers)
    }

    @GetMapping("/schedule/{scheduleId}")
    fun getSchedule(@PathVariable scheduleId: String): ResponseEntity<Schedule> {
        val schedule = scheduleService.getScheduleById(scheduleId)
        return ResponseEntity.ok(schedule)
    }

    @GetMapping("/schedule/{scheduleId}/quiz")
    fun getScheduleWithQuizzes(@PathVariable scheduleId: String): ResponseEntity<ScheduleWithQuestionsDto> {
        val scheduleWithQuestions = participantService.getScheduleWithQuestion(scheduleId)
        return ResponseEntity.ok(scheduleWithQuestions)
    }

    @GetMapping("/attempt/{id}")
    fun getQuizAttempt(@PathVariable id: String): QuizAttemptDTO {
        return participantService.getAttemptById(id)
    }

    @GetMapping("/attempts/{userId}/{scheduleId}")
    fun getQuizAttemptByUserAndSchedule(
        @PathVariable userId: String,
        @PathVariable scheduleId: String,
    ): QuizAttemptDTO {
        return participantService.getAttemptByScheduleAndUser(userId,scheduleId)
    }

    @GetMapping("/{userId}/homepage")
    fun getHomePageSchedules(@PathVariable userId: String): ResponseEntity<List<HomePageSchedule>> {
        val schedules = participantService.getUserHomePageSchedules(userId)
        return ResponseEntity.ok(schedules)
    }

    @PostMapping("/create-attempt")
    fun createAttempt(
        @RequestParam userId: String,
        @RequestParam scheduleId: String
    ): QuizAttempt {
        return participantService.createAttempt(userId, scheduleId)
    }





}