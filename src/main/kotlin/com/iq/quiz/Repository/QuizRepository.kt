package com.iq.quiz.Repository

import com.iq.quiz.Entity.Quiz
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface QuizRepository : JpaRepository<Quiz, String>{
    fun findByQuizId(quizid:String):Quiz?

}