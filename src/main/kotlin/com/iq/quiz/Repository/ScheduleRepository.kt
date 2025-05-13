package com.iq.quiz.Repository

import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.Schedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ScheduleRepository: JpaRepository<Schedule, String> {
    fun findAllByQuiz(quiz: Quiz): List<Schedule>

    fun findByQuizQuizId(quizId: String): List<Schedule>


}