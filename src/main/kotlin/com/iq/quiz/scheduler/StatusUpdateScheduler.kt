package com.iq.quiz.scheduler

import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.Repository.ScheduleRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class StatusUpdateScheduler (
    private val scheduleRepository: ScheduleRepository,
    private val quizRepository: QuizRepository
) {

    @Scheduled(fixedRate = 60000) // Every 1 minute
    @Transactional
    fun runStatusUpdate() {
        println("Running in scheduler")
        val now = LocalDateTime.now()
        val updatedScheduleCount=scheduleRepository.updateScheduleStatuses(now)
//        val updatedQuizCount =quizRepository.updateQuizStatuses(now)
        println("Updated $updatedScheduleCount schedules status & quiz status")
    }
}