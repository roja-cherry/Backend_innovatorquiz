package com.iq.quiz.model

data class Question(
    val text: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String
)
// This data class represents a quiz question with its text, options, and the correct answer.
// It is used to structure the data when processing quiz files uploaded by the admin.
// The class includes properties for the question text, four options (A, B, C, D), and the correct answer.
