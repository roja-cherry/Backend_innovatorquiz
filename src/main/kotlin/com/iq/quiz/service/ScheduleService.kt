package com.iq.quiz.service

import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.Repository.ScheduleRepository
import org.springframework.stereotype.Service

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val quizRepository: QuizRepository
) {
}