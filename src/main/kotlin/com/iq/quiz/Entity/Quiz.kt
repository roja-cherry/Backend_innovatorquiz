package com.iq.quiz.Entity

import jakarta.persistence.*
import java.security.Timestamp
import java.time.LocalDateTime
import java.util.*

@Entity
data class Quiz(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val quizId : String ?= null,

    @Column(unique = true)
    val quizName : String,

    val timer : Long,
    var createdAt: LocalDateTime ?= null,

    val status: QuizStatus
)

