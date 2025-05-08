package com.iq.quiz.Entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Quiz(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val quizId : String?=null,

    val quizName : String,
    val timer : Int,

    @Enumerated(EnumType.STRING)
    var status: QuizStatus,

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = true)
    val createdBy: User? = null,


    var createdAt: LocalDateTime?=null,

    var isActive:Boolean

)

