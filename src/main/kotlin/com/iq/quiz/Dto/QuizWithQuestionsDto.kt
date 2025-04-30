package com.iq.quiz.Dto
data class QuizWithQuestionsDto(
    val quiz: QuizDTO,
    val questions: List<QuestionDTO>
)

