package com.iq.quiz.service

import com.iq.quiz.Entity.Schedule
import com.iq.quiz.Entity.ScheduleStatus
import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.Repository.ScheduleRepository
import org.springframework.stereotype.Service

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val quizRepository: QuizRepository
) {
    fun updateToCancel(id: String): Schedule {
        val schedule = scheduleRepository.findById(id).orElseThrow {
            RuntimeException("Schedule not found")
        }
        schedule.status = ScheduleStatus.CANCELLED // Updating status to CANCELLED
        return scheduleRepository.save(schedule) // Save changes
    }

}