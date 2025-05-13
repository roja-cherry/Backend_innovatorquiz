package com.iq.quiz.controller

import com.iq.quiz.Dto.ScheduleDto
import com.iq.quiz.Dto.schedule.ScheduleEditCreateRequest
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.Entity.ScheduleStatus
import com.iq.quiz.service.ScheduleService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/schedule")
class ScheduleController(
    private val scheduleService: ScheduleService
) {

    @PostMapping
    fun createNewSchedule(@RequestBody @Valid dto: ScheduleEditCreateRequest): ResponseEntity<Schedule> {
        val response = scheduleService.createNewQuiz(dto)
        return ResponseEntity.status(201).body(response)
    }

    @GetMapping("/{id}")
    fun getScheduleById(@PathVariable id: String): ScheduleDto {
        return scheduleService.getScheduleById(id)
    }

    @GetMapping("/all-schedule")
    fun getAllSchedules(): List<ScheduleDto> {
        return scheduleService.getAllSchedules()
    }
    @GetMapping("/status")
    fun getSchedulesByStatus(@RequestParam(required = false) status: ScheduleStatus?): List<ScheduleDto> {
        return scheduleService.getSchedulesByStatus(status)
    }


}