package com.iq.quiz.Dto

import com.iq.quiz.Entity.ScheduleStatus
import java.time.LocalDateTime

data class ScheduleDto(
    val id: String? = null,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val createdAt: LocalDateTime,
    val status: ScheduleStatus,
    val quizTitle: String,
    val quizId: String,
) {
    fun getStatusText(): String {
        return status.text
    }
}
