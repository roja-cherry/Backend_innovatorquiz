package com.iq.quiz.Repository

import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.Entity.ScheduleStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime



@Repository
interface ScheduleRepository: JpaRepository<Schedule, String> {
    fun findAllByQuiz(quiz: Quiz): List<Schedule>
    fun findByStatus(status: ScheduleStatus): List<Schedule>


    @Query(
        ("SELECT COUNT(s) > 0 FROM Schedule s " +
                "WHERE s.startDateTime < :end " +
                "AND s.endDateTime > :start")
    )
    fun existsByTimeRangeOverlap(
        @Param("start") start: LocalDateTime?,
        @Param("end") end: LocalDateTime?
    ): Boolean


    // returns true if there’s any schedule in SCHEDULED (or LIVE) state for this quiz
    fun existsByQuizQuizIdAndStatusIn(quizId: String, statuses: Collection<ScheduleStatus>): Boolean

}