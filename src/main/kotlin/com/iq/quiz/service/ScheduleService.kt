package com.iq.quiz.service

import com.iq.quiz.Dto.ScheduleDto
import com.iq.quiz.Dto.schedule.ScheduleEditCreateRequest
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.Entity.ScheduleStatus
import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.Repository.ScheduleRepository
import com.iq.quiz.exception.QuizNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val quizRepository: QuizRepository
) {

    fun createNewQuiz(dto: ScheduleEditCreateRequest): Schedule {
        val quiz = quizRepository.findByQuizId(dto.quizId)
            ?: throw QuizNotFoundException("Quiz not found with id ${dto.quizId}")

        val schedule = Schedule(
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            status = ScheduleStatus.SCHEDULED,
            quiz = quiz,
            startDateTime = dto.startDateTime,
            endDateTime = dto.endDateTime
        )

        return scheduleRepository.save(schedule)
    }

    fun getScheduleById(id: String): ScheduleDto {
        val schedule = scheduleRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Schedule not found with ID: $id") }

        return ScheduleDto(
            id = schedule.id,
            startDateTime = schedule.startDateTime,
            endDateTime = schedule.endDateTime,
            createdAt = schedule.createdAt,
            updatedAt = schedule.updatedAt,
            status = schedule.status
        )
    }

    fun getAllSchedules(): List<ScheduleDto> {
        return scheduleRepository.findAll().map { schedule ->
            ScheduleDto(
                id = schedule.id,
                startDateTime = schedule.startDateTime,
                endDateTime = schedule.endDateTime,
                createdAt = schedule.createdAt,
                updatedAt = schedule.updatedAt,
                status = schedule.status
            )
        }
    }
}