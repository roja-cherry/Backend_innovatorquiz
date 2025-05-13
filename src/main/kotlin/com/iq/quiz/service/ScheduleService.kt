package com.iq.quiz.service

import com.iq.quiz.Dto.ScheduleDto
import com.iq.quiz.Dto.schedule.ScheduleEditCreateRequest
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.Entity.ScheduleStatus
import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.Repository.ScheduleRepository
import com.iq.quiz.exception.QuizNotFoundException
import com.iq.quiz.exception.ScheduleException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime

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


    fun createNewSchedule(dto: ScheduleEditCreateRequest): Schedule {
        val quiz = quizRepository.findByQuizId(dto.quizId)
            ?: throw QuizNotFoundException("Quiz not found with id ${dto.quizId}")

        val isScheduleExistsBetweenTime = scheduleRepository.existsByTimeRangeOverlap(dto.startDateTime, dto.endDateTime)
        if(isScheduleExistsBetweenTime) {
            throw ScheduleException("A quiz is already scheduled between this this", HttpStatus.BAD_REQUEST)
        }

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
            status = schedule.status,
            quizId = schedule.quiz.quizId!!,
            quizTitle = schedule.quiz.quizName
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
                status = schedule.status,
                quizId = schedule.quiz.quizId!!,
                quizTitle = schedule.quiz.quizName
            )
        }
    }

    fun getSchedulesByStatus(status: ScheduleStatus?): List<ScheduleDto> {
        val schedules = if (status != null) {
            scheduleRepository.findByStatus(status)
        } else {
            scheduleRepository.findAll()
        }

        return schedules.map { schedule ->
            ScheduleDto(
                id = schedule.id,
                startDateTime = schedule.startDateTime,
                endDateTime = schedule.endDateTime,
                createdAt = schedule.createdAt,
                updatedAt = schedule.updatedAt,
                status = schedule.status,
                quizTitle = schedule.quiz.quizName,
                quizId = schedule.quiz.quizId!!
            )
        }
    }


    fun reschedule(id: String,request: ScheduleEditCreateRequest): Schedule {

        val existingSchedule = scheduleRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Schedule not found with ID: ${id}") }
        if (existingSchedule.status != ScheduleStatus.CANCELLED)
        {
            throw IllegalStateException("Only Cancelled Schedules can be Reschedulled")
        }
        existingSchedule.startDateTime=request.startDateTime
        existingSchedule.endDateTime=request.endDateTime
        existingSchedule.updatedAt= LocalDateTime.now()
        existingSchedule.status=ScheduleStatus.SCHEDULED

        return scheduleRepository.save(existingSchedule)
    }


}