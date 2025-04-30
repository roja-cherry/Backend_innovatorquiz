package com.iq.quiz.Dto

data class QuestionDTO(
//    val quizId: String?,
    val questionId: String?,
    val question: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val correctAnswer: String
)
