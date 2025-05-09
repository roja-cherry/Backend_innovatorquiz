package com.iq.quiz.Dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.FutureOrPresent
import java.time.LocalDateTime

data class PublishQuizRequest(
    val quizId: String,

    @field:FutureOrPresent
    val quizStartDateTime: LocalDateTime,

    @field:Future
    val quizEndDateTime: LocalDateTime
)
