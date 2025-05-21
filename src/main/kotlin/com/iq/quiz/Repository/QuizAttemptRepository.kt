package com.iq.quiz.Repository

import com.iq.quiz.Entity.QuizAttempt
import org.springframework.data.jpa.repository.JpaRepository

interface QuizAttemptRepository : JpaRepository<QuizAttempt, String> {
    fun findByUserUserIdAndScheduleId(userId: String, scheduleId: String): QuizAttempt?
}