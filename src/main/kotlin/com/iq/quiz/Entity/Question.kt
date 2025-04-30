package com.iq.quiz.Entity

import jakarta.persistence.*
import java.util.*

@Entity
data class Question(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val questionId : String?=null,

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    val quiz : Quiz,

    val question : String,

    val option1 : String,
    val option2 : String,
    val option3 : String,
    val option4 : String,

    val correctAnswer : String
)