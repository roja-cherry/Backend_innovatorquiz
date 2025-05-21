package com.iq.quiz.Dto
import java.time.LocalDateTime


data class QuizAttemptDTO (
    val id: String,
    val userId: String,
    val userName: String,
    val scheduleId: String,
    val startedAt: LocalDateTime,
    val finishedAt: LocalDateTime?,
    val score: Int?,
    val maxScore: Int?
    )


