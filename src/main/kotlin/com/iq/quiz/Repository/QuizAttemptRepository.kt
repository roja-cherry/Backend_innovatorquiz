package com.iq.quiz.Repository

import com.iq.quiz.Dto.UserScoreSummary
import com.iq.quiz.Entity.QuizAttempt
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface QuizAttemptRepository : JpaRepository<QuizAttempt, String> {
    fun findByUserUserIdAndScheduleId(userId: String, scheduleId: String): QuizAttempt?

    @Query(
        value = """
        SELECT qa.user_id AS userId, u.username AS userName, SUM(qa.score) AS totalScore
        FROM quiz_attempts qa
        JOIN users u ON qa.user_id = u.user_id
        WHERE qa.schedule_id = :scheduleId
        GROUP BY qa.user_id, u.username
        ORDER BY totalScore DESC
        LIMIT 10
    """,
        nativeQuery = true
    )
    fun findTop10BySchedule(@Param("scheduleId") scheduleId: String): List<UserScoreSummary>


    @Query(
        value = """
        SELECT qa.user_id AS userId, u.username AS userName, SUM(qa.score) AS totalScore
        FROM quiz_attempts qa
        JOIN users u ON qa.user_id = u.user_id
        GROUP BY qa.user_id, u.username
        ORDER BY totalScore DESC
        LIMIT 10
    """,
        nativeQuery = true
    )
    fun findTop10Global(): List<UserScoreSummary>



}