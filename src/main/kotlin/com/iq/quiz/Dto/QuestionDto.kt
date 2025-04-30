package com.iq.quiz.Dto

import java.util.*

data class QuestionDTO(
    val quizId: UUID,
    val question: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val correctAnswer: String
)
