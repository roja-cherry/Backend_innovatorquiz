package com.iq.quiz.Dto.schedule

import jakarta.validation.constraints.Future
import java.time.LocalDateTime

data class ScheduleEditCreateRequest(
    val id: String? = null,

    @field:Future
    val startDateTime: LocalDateTime,

    @field:Future
    val endDateTime: LocalDateTime,

    val quizId: String
)
