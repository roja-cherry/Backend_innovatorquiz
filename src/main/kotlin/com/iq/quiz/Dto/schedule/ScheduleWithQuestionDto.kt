package com.iq.quiz.Dto.schedule

import com.iq.quiz.Dto.QuestionWithoutAnswerDTO
import com.iq.quiz.Dto.ScheduleDto

data class ScheduleWithQuestionsDto(
    val schedule: ScheduleDto,
    val questions: List<QuestionWithoutAnswerDTO>,
    val timer:Long
)