package com.iq.quiz.Dto

data class QuestionWithoutAnswerDTO(
    val questionId: String,
    val question: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String
)