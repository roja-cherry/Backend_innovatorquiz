package com.iq.quiz.Dto

import com.iq.quiz.Entity.QuizStatus
import java.time.LocalDateTime

data class QuizDTO(
    val quizId: String?,
    val quizName: String,
    val timer: Long,
    val createdAt: LocalDateTime? = null,
    val status: QuizStatus
) {
    fun getStatusText(): String {
        return status.text
    }
}

