package com.iq.quiz.Repository

import com.iq.quiz.Entity.Question
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import java.util.UUID

interface QuestionRepository : JpaRepository<Question, String> {
    fun findByQuizQuizId(id: String): List<Question>

    @Modifying
    fun deleteAllByQuizQuizId(id: String)
}