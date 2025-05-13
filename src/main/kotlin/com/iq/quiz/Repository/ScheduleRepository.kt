package com.iq.quiz.Repository

import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.Entity.ScheduleStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime



@Repository
interface ScheduleRepository: JpaRepository<Schedule, String> {
    fun findAllByQuiz(quiz: Quiz): List<Schedule>
    fun findByStatusIn(statuses: List<ScheduleStatus>): List<Schedule>



    @Query(
        ("SELECT COUNT(s) > 0 FROM Schedule s " +
                "WHERE s.startDateTime < :end " +
                "AND s.endDateTime > :start")
    )
    fun existsByTimeRangeOverlap(
        @Param("start") start: LocalDateTime?,
        @Param("end") end: LocalDateTime?
    ): Boolean

    fun existsByQuizQuizIdAndStatusIn(quizId: String, statuses: Collection<ScheduleStatus>): Boolean

    fun findByQuizQuizId(quizId: String): List<Schedule>

    @Query("""
    SELECT COUNT(s) > 0 FROM Schedule s
    WHERE s.startDateTime < :end
      AND s.endDateTime > :start
      AND s.id <> :excludeId
""")
    fun existsByTimeRangeOverlapExcludesGivenSchedule(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime,
        @Param("excludeId") excludeId: String
    ): Boolean

    @Modifying
    @Query("""
    UPDATE Schedule s
    SET s.status = CASE
        WHEN s.startDateTime <= :now AND s.endDateTime >= :now THEN 'LIVE'
        WHEN s.endDateTime < :now THEN 'COMPLETED'
        ELSE s.status
    END
    WHERE s.status NOT IN ('COMPLETED', 'CANCELLED')
      AND (
          (s.startDateTime <= :now AND s.endDateTime >= :now)
          OR (s.endDateTime < :now)
      )
    """)
    fun updateStatuses(@Param("now") now: LocalDateTime): Int;
}