package com.iq.quiz.service

import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.Repository.ScheduleRepository
import org.springframework.stereotype.Service

@Service
class ParticipantService(
    private val quizRepository: QuizRepository,
    private val scheduleRepository: ScheduleRepository
) {
}