package com.iq.quiz.Dto

import com.iq.quiz.Entity.QuizStatus

data class QuizDTO(
    val quizId: String?,
    val quizName: String,
    val duration: Int,
    val status: QuizStatus = QuizStatus.INACTIVE,
    val createdByUserId: Any
)
