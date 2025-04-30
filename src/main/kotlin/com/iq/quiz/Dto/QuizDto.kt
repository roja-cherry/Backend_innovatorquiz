package com.iq.quiz.Dto

import com.iq.quiz.Entity.QuizStatus
import java.util.*

data class QuizDTO(
    val quizName: String,
    val duration: Int,
    val status: QuizStatus = QuizStatus.INACTIVE,
    val createdByUserId: UUID
)
