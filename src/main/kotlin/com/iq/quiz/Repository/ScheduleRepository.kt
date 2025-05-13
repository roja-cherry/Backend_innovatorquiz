package com.iq.quiz.Repository

import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.Entity.ScheduleStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ScheduleRepository: JpaRepository<Schedule, String> {
    fun findAllByQuiz(quiz: Quiz): List<Schedule>
    fun findByStatus(status: ScheduleStatus): List<Schedule>


    // returns true if there’s any schedule in SCHEDULED (or LIVE) state for this quiz
    fun existsByQuizQuizIdAndStatusIn(quizId: String, statuses: Collection<ScheduleStatus>): Boolean

}