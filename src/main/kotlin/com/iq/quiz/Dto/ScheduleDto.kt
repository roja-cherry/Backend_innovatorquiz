package com.iq.quiz.Dto

import com.iq.quiz.Entity.ScheduleStatus
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.validation.constraints.Future
import java.time.LocalDateTime

data class ScheduleDto(
    val id: String? = null,
    val startDateTime: LocalDateTime? = null,
    val endDateTime: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val status: ScheduleStatus,


)
