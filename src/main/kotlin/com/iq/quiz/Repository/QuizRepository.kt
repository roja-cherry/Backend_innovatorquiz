package com.iq.quiz.Repository

import com.iq.quiz.Entity.Quiz
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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


}