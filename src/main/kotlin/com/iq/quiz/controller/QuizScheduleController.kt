package com.iq.quiz.controller

import com.iq.quiz.Dto.PublishQuizRequest
import com.iq.quiz.Dto.ScheduleDto
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.service.QuizScheduleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/schedule")
class QuizScheduleController(
    private val scheduleService: QuizScheduleService
) {

    @GetMapping("/{scheduleId}")
    fun getSchedule(@PathVariable scheduleId: String): ResponseEntity<Schedule> {
        val schedule = scheduleService.getScheduleById(scheduleId)
        return ResponseEntity.ok(schedule)
    }

    @PostMapping("/publish")
    fun publishQuiz(@RequestBody request:PublishQuizRequest):ResponseEntity<ScheduleDto>{
     val result=scheduleService.publishQuiz(request)
        return ResponseEntity.ok(result)
    }


    @PatchMapping("/{scheduleId}/cancel")
    fun cancelSchedule(@PathVariable scheduleId: String):ResponseEntity<ScheduleDto>{
        val response = scheduleService.cancelSchedule(scheduleId)
        return ResponseEntity.ok(response)
    }
}