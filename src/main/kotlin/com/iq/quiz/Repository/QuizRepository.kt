package com.iq.quiz.Repository

import com.iq.quiz.Entity.Quiz
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface QuizRepository : JpaRepository<Quiz, String>{
    fun findByQuizId(quizid:String):Quiz?

    @Query("SELECT q FROM Quiz q WHERE LOWER(q.quizName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    fun searchByKeyword(@Param("keyword") keyword: String): List<Quiz>

    fun deleteByQuizId(quizId: String)

    fun existsByQuizName(quizName: String): Boolean


}