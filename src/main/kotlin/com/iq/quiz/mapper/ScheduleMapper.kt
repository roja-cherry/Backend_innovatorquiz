package com.iq.quiz.mapper

import com.iq.quiz.Dto.*
import com.iq.quiz.Dto.user.UserDto
import com.iq.quiz.Entity.*


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

fun quizAttemptToDto(attempt: QuizAttempt): QuizAttemptDTO {
    val quizName = attempt.schedule.quiz.quizName // assuming relations exist
    return QuizAttemptDTO(
        id = attempt.id,
        userId = attempt.user.userId,
        userName = attempt.user.username,
        scheduleId = attempt.schedule.id,
        startedAt = attempt.startedAt,
        finishedAt = attempt.finishedAt,
        score = attempt.score,
        maxScore = attempt.maxScore,
        quizName = quizName
    )
}



