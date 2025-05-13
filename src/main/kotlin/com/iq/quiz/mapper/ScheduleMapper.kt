package com.iq.quiz.mapper

import com.iq.quiz.Dto.ScheduleDto
import com.iq.quiz.Entity.Schedule


fun scheduleToDto(schedule: Schedule): ScheduleDto {
    return ScheduleDto(
        id = schedule.id,
        startDateTime = schedule.startDateTime,
        endDateTime = schedule.endDateTime,
        createdAt = schedule.createdAt,
        updatedAt = schedule.updatedAt,
        status = schedule.status
    )
}