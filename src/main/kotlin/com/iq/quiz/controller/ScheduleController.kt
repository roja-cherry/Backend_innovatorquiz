package com.iq.quiz.controller

import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Dto.UpdateIsActive
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.Entity.ScheduleStatus
import com.iq.quiz.service.AdminService
import com.iq.quiz.service.ScheduleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/schedule")
class ScheduleController(private val scheduleService: ScheduleService ) {

    @PatchMapping("/{id}/cancel")
    fun updateToCancel(@PathVariable id: String): ResponseEntity<Schedule> {
        val updatedSchedule = scheduleService.updateToCancel(id)
        return ResponseEntity.ok(updatedSchedule)
    }


}