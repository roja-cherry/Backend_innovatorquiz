package com.iq.quiz.controller

import com.iq.quiz.Dto.ScheduleDto
import com.iq.quiz.Dto.schedule.ScheduleEditCreateRequest
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.Entity.ScheduleStatus
import com.iq.quiz.service.ScheduleService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity

import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/schedule")
class ScheduleController(
    private val scheduleService: ScheduleService
) {

    @PostMapping
    fun createNewSchedule(@RequestBody @Valid dto: ScheduleEditCreateRequest): ResponseEntity<Schedule> {
        val response = scheduleService.createNewSchedule(dto)
        return ResponseEntity.status(201).body(response)
    }
    @PatchMapping("/{id}/cancel")
    fun updateToCancel(@PathVariable id: String): ResponseEntity<Schedule> {
        val updatedSchedule = scheduleService.updateToCancel(id)
        return ResponseEntity.ok(updatedSchedule)
    }



    @PutMapping("/{id}")
    fun editSchedule(@RequestBody @Valid dto: ScheduleEditCreateRequest): ResponseEntity<Schedule>? {
        return null
    }

    @GetMapping("/{id}")
    fun getScheduleById(@PathVariable id: String): ScheduleDto {
        return scheduleService.getScheduleById(id)
    }

    @GetMapping("/all-schedule")
    fun getAllSchedules(
        @RequestParam(required = false) status: List<ScheduleStatus>?
    ): List<ScheduleDto> {
        return scheduleService.getSchedulesByStatuses(status)
    }


    @GetMapping("/quiz/{quizId}")
    fun getSchedulesByQuizId(@PathVariable quizId: String): ResponseEntity<List<ScheduleDto>> {
        val schedules = scheduleService.getSchedulesByQuizId(quizId)
        return ResponseEntity.ok(schedules)
    }



    @GetMapping("/status")
    fun getSchedulesByStatus(@RequestParam(required = false) status: List<ScheduleStatus>?): List<ScheduleDto> {
        return scheduleService.getSchedulesByStatuses(status)
    }



    @PatchMapping("/{id}/reschedule")
    fun reschedule(
        @PathVariable id:String,
        @RequestBody @Valid request:ScheduleEditCreateRequest
    ):ResponseEntity<Schedule>{
        val response=scheduleService.reschedule(id,request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteSchedule(@PathVariable id:String):String{
        val response = scheduleService.deleteScheduleById(id)
        return response
    }
}