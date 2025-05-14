package com.iq.quiz.Dto

import com.iq.quiz.Entity.ScheduleStatus
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.validation.constraints.Future
import java.time.LocalDateTime

data class ScheduleDto(
    val id: String? = null,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val createdAt: LocalDateTime,
    val status: ScheduleStatus,
    val quizTitle: String,
    val quizId: String
) {
    fun getStatusText(): String {
        return status.text
    }
}
