package com.iq.quiz.controller

import com.iq.quiz.Dto.QuizWithQuestionsDto
import com.iq.quiz.Dto.ScheduleDto
import com.iq.quiz.service.ScheduleService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/schedule")
class ScheduleController (
    private val scheduleService:ScheduleService
){

    @GetMapping("/{id}")
    fun getScheduleById(@PathVariable id: String): ScheduleDto {
        return scheduleService.getScheduleById(id)
    }

    @GetMapping("/All_Schedule")
    fun getAllSchedules(): List<ScheduleDto> {
        return scheduleService.getAllSchedules()
    }
}
