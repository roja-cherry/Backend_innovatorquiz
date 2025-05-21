package com.iq.quiz.mapper

import com.iq.quiz.Dto.QuestionDTO
import com.iq.quiz.Dto.QuestionWithoutAnswerDTO
import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Dto.ScheduleDto
import com.iq.quiz.Dto.user.UserDto
import com.iq.quiz.Entity.Question
import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.Entity.User


fun scheduleToDto(schedule: Schedule): ScheduleDto {
    return ScheduleDto(
        id = schedule.id,
        startDateTime = schedule.startDateTime!!,
        endDateTime = schedule.endDateTime!!,
        createdAt = schedule.createdAt,
        status = schedule.status,
        quizId= schedule.quiz.quizId!!,
        quizTitle = schedule.quiz.quizName
    )
}

fun quizToDto(quiz: Quiz): QuizDTO {
    return QuizDTO(
        quizId = quiz.quizId,
        quizName = quiz.quizName,
        timer = quiz.timer,
        createdAt = quiz.createdAt,
        status = quiz.status
    )
}

fun questionToDto(question: Question): QuestionDTO {
    return QuestionDTO(
        questionId = question.questionId,
        question = question.question,
        option1 = question.option1,
        option2 = question.option2,
        option3 = question.option3,
        option4 = question.option4,
        correctAnswer = question.correctAnswer
    )
}
fun quizToQuizDto(quiz: Quiz): QuizDTO {
    return QuizDTO(
        quizId = quiz.quizId,
        quizName = quiz.quizName,
        timer = quiz.timer,
        createdAt = quiz.createdAt,
        status = quiz.status
    )
}

fun questionToQuestionWithoutAnswerDto(question: Question): QuestionWithoutAnswerDTO = QuestionWithoutAnswerDTO(
    questionId = question.questionId!!,
    question = question.question,
    option1 = question.option1,
    option2 = question.option2,
    option3 = question.option3,
    option4 = question.option4
)

fun userToDto(user: User): UserDto {
    return UserDto(
        userId = user.userId!!,
        username = user.username,
        email = user.email,
        role = user.role
    )
}


