package com.iq.quiz.mapper

import com.iq.quiz.Dto.ScheduleDto
import com.iq.quiz.Entity.Schedule


fun scheduleToDto(schedule: Schedule): ScheduleDto {
    return ScheduleDto(
        id = schedule.id,
        startDateTime = schedule.startDateTime!!,
        endDateTime = schedule.endDateTime!!,
        createdAt = schedule.createdAt,
        status = schedule.status,
        quizId= schedule.quiz.quizId!!,
        quizTitle = schedule.quiz.quizName
    )
}