package com.iq.quiz.Entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import jakarta.validation.constraints.Future
import java.time.LocalDateTime

@Entity
data class Schedule(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id : String ?= null,

    @Future
    var startDateTime: LocalDateTime?,

    @Future
    var endDateTime: LocalDateTime?,

    val createdAt: LocalDateTime,


    @Enumerated(EnumType.STRING)
    var status: ScheduleStatus,

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    val quiz : Quiz,

    @OneToMany(mappedBy = "schedule", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val quizAttempts: MutableList<QuizAttempt> ?= mutableListOf()
) {
    fun getStatusText(): String {
        return status.text
    }
}