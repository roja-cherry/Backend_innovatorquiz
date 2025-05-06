package com.iq.quiz.Dto

import com.iq.quiz.Entity.QuizStatus
import java.time.LocalDateTime



data class QuizDTO(
    val quizId: String?,
    val quizName: String,
    val duration: Int,
    val status: QuizStatus = QuizStatus.CREATED,
    val createdBy: UserDTO?= null,
    val createdAt: LocalDateTime? = null,
    val isActive:Boolean
)

