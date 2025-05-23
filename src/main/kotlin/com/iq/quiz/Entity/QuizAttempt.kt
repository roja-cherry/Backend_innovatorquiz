// src/main/kotlin/com/iq/quiz/entity/QuizAttempt.kt
package com.iq.quiz.Entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.Entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "quiz_attempts")
data class QuizAttempt(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    val schedule: Schedule,

    val startedAt: LocalDateTime = LocalDateTime.now(),
    var finishedAt: LocalDateTime? = null,

    /** number of correct answers */
    var score: Int? = null,

    /** total questions answered */
    var maxScore: Int? = null,

    @OneToMany(mappedBy = "attempt", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val answerSubmissions: MutableList<AnswerSubmission> ?= mutableListOf()
)
