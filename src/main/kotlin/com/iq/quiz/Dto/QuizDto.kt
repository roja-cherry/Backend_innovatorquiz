package com.iq.quiz.Dto

import com.iq.quiz.Entity.QuizStatus
import java.time.LocalDateTime



data class QuizDTO(
    val quizId: String?,
    val quizName: String,
    val timer: Int,
    val createdAt: LocalDateTime? = null,
)

