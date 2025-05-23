package com.iq.quiz.Entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.security.Timestamp
import java.time.LocalDateTime
import java.util.*

@Entity
data class Quiz(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val quizId: String ?= null,

    @Column(unique = true)
    val quizName : String,

    val timer : Long,
    val createdAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    var status: QuizStatus,

    @OneToMany(mappedBy = "quiz", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val questions: MutableList<Question> ?= mutableListOf(),

    @OneToMany(mappedBy = "quiz", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val schedules: MutableList<Schedule> ?= mutableListOf()


){
    fun getStatusText(): String {
        return status.text
    }

}

