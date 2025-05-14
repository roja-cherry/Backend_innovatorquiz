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
import com.iq.quiz.mapper.scheduleToDto
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.LocalDateTime

private fun scheduleSpecification(
    startDateTime: LocalDateTime? = null,
    endDateTime: LocalDateTime? = null,
    statuses: List<ScheduleStatus>? = null
): Specification<Schedule> {
    return Specification { root, _, cb ->
        val predicates = mutableListOf<Predicate>()

        // Default to SCHEDULED if statuses not provided
        val effectiveStatuses = statuses ?: listOf(ScheduleStatus.SCHEDULED)

        // Filter by status list
        predicates.add(root.get<ScheduleStatus>("status").`in`(effectiveStatuses))

        // Optional: Filter by startDateTime >= ...
        startDateTime?.let {
            predicates.add(cb.greaterThanOrEqualTo(root.get("startDateTime"), it))
        }

        // Optional: Filter by endDateTime <= ...
        endDateTime?.let {
            predicates.add(cb.lessThanOrEqualTo(root.get("endDateTime"), it))
        }

        cb.and(*predicates.toTypedArray())
    }
}


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
//        schedule.startDateTime=null
//        schedule.endDateTime=null
        return scheduleRepository.save(schedule) // Save changes
    }


    fun createNewSchedule(dto: ScheduleEditCreateRequest): Schedule {
        val quiz = quizRepository.findByQuizId(dto.quizId)
            ?: throw QuizNotFoundException("Quiz not found with id ${dto.quizId}")

        val isScheduleExistsBetweenTime =
            scheduleRepository.existsByTimeRangeOverlap(dto.startDateTime, dto.endDateTime)
        if (isScheduleExistsBetweenTime) {
            throw ScheduleException("A quiz is already scheduled between this time range", HttpStatus.BAD_REQUEST)
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

    fun getSchedulesByStatuses(statuses: List<ScheduleStatus>?): List<ScheduleDto> {
        val schedules = if (!statuses.isNullOrEmpty()) {
            scheduleRepository.findByStatusIn(statuses)

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


    fun reschedule(id: String, request: ScheduleEditCreateRequest): Schedule {

        val existingSchedule = scheduleRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Schedule not found with ID: ${id}") }

        val isAnyOtherScheduled = scheduleRepository.existsByTimeRangeOverlapExcludesGivenSchedule(
            request.startDateTime,
            request.endDateTime,
            id
        )

        if (isAnyOtherScheduled) {
            throw ScheduleException("A quiz is already scheduled between this time range", HttpStatus.BAD_REQUEST)
        }
        existingSchedule.startDateTime = request.startDateTime
        existingSchedule.endDateTime = request.endDateTime
        existingSchedule.updatedAt = LocalDateTime.now()
        existingSchedule.status = ScheduleStatus.SCHEDULED

        return scheduleRepository.save(existingSchedule)
    }


    fun getSchedulesByQuizId(quizId: String): List<ScheduleDto> {
        val schedules = scheduleRepository.findByQuizQuizId(quizId)
        return schedules.map { schedule -> scheduleToDto(schedule) }
    }

    fun getAllSchedulesFiltered(
        sortBy: String?,
        search: String?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        status: List<ScheduleStatus>?
    )
            : List<ScheduleDto> {
        val sort = Sort.by(Sort.Direction.ASC, sortBy)
        val spec = scheduleSpecification(startDate, endDate, status)
        val schedules = scheduleRepository.findAll(spec, sort)
        return schedules.map { schedule -> scheduleToDto(schedule) }
    }

    fun deleteScheduleById(id: String): String {
        scheduleRepository.deleteById(id)
        return "Deleted Successfully"

    }
}