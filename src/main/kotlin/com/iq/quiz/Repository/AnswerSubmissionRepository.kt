package com.iq.quiz.Repository

import com.iq.quiz.Entity.AnswerSubmission
import org.springframework.data.jpa.repository.JpaRepository

interface AnswerSubmissionRepository : JpaRepository<AnswerSubmission, String> {
    fun findAllByAttemptId(attemptId: String): List<AnswerSubmission>
}