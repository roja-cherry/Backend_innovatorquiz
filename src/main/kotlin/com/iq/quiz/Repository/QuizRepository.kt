package com.iq.quiz.Repository

import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.QuizStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface QuizRepository : JpaRepository<Quiz, String>, JpaSpecificationExecutor<Quiz>
{
    fun findByQuizId(quizid:String):Quiz?

    @Query("SELECT q FROM Quiz q WHERE LOWER(q.quizName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    fun searchByKeyword(@Param("keyword") keyword: String): List<Quiz>

    fun deleteByQuizId(quizId: String)

    fun existsByQuizName(quizName: String): Boolean

    fun findByStatus(status: QuizStatus): MutableList<Quiz>

    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE Quiz q
    SET q.status = CASE
        WHEN EXISTS (
            SELECT 1 FROM Schedule s
            WHERE s.quiz = q
              AND s.status NOT IN ('COMPLETED', 'CANCELLED')
              AND s.startDateTime <= :now AND s.endDateTime >= :now
        ) THEN 'ACTIVE'
        WHEN EXISTS (
            SELECT 1 FROM Schedule s
            WHERE s.quiz = q
              AND s.status NOT IN ('COMPLETED', 'CANCELLED')
              AND s.endDateTime < :now
        ) THEN 'COMPLETED'
        ELSE q.status
    END
    WHERE q.status NOT IN ('COMPLETED', 'CANCELLED')
      AND (
          EXISTS (
              SELECT 1 FROM Schedule s
              WHERE s.quiz = q
                AND s.status NOT IN ('COMPLETED', 'CANCELLED')
                AND (
                    (s.startDateTime <= :now AND s.endDateTime >= :now)
                    OR (s.endDateTime < :now)
                )
          )
      )
""")
    fun updateQuizStatuses(@Param("now") now: LocalDateTime): Int

}