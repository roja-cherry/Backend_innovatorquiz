package com.iq.quiz.Entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.util.*

@Entity
data class Question(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val questionId : String?=null,

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonIgnore
    val quiz : Quiz,

    val question : String,

    val option1 : String,
    val option2 : String,
    val option3 : String,
    val option4 : String,

    val correctAnswer : String,

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val answerSubmissions: MutableList<AnswerSubmission> ?= mutableListOf(),
)