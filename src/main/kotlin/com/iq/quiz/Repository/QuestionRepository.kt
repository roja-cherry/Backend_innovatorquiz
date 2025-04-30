package com.iq.quiz.Repository

import com.iq.quiz.Entity.Question
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface QuestionRepository : JpaRepository<Question, UUID> {

}