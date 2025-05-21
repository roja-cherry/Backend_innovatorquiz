package com.iq.quiz.Repository

import com.iq.quiz.Dto.QuizAttemptDTO
import com.iq.quiz.Entity.QuizAttempt
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface QuizAttemptRepository : JpaRepository<QuizAttempt, String> {
    fun findByUserUserIdAndScheduleId(userId: String, scheduleId: String): QuizAttempt?

}