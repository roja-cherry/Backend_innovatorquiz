package com.iq.quiz.Entity

import jakarta.persistence.*
import java.security.Timestamp
import java.time.LocalDateTime
import java.util.*

@Entity
data class Quiz(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val quizId : String?=null,

    @Column(unique = true)
    val quizName : String,

    val timer : Int,

    @Enumerated(EnumType.STRING)
    var status: QuizStatus,

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = true)
    val createdBy: User? = null,

    var createdAt: LocalDateTime?=null,
    var isActive: Boolean,
    var quizStartDateTime: LocalDateTime?=null,
    var quizEndDateTime: LocalDateTime?=null
)

