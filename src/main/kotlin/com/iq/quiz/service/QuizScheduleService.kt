package com.iq.quiz.service

import com.iq.quiz.Dto.PublishQuizRequest
import com.iq.quiz.Dto.ScheduleDto
import com.iq.quiz.Dto.schedule.ScheduleEditCreateRequest
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.Entity.ScheduleStatus
import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.Repository.ScheduleRepository
import com.iq.quiz.exception.ScheduleException
import com.iq.quiz.mapper.scheduleToDto
import jakarta.persistence.criteria.Predicate
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class QuizScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val quizRepository: QuizRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(QuizScheduleService::class.java)

    fun scheduleSpecification(
        status: ScheduleStatus? = ScheduleStatus.ACTIVE
    ): Specification<Schedule> {
        logger.debug("Building schedule specification with status: $status")
        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()
            status?.let {
                predicates.add(cb.equal(root.get<ScheduleStatus>("status"), it))
            }
            cb.and(*predicates.toTypedArray())
        }
    }

    fun getScheduleById(scheduleId: String): Schedule {
        logger.info("Fetching schedule with ID: $scheduleId")
        return scheduleRepository.findById(scheduleId)
            .orElseThrow {
                logger.warn("Schedule not found with ID: $scheduleId")
                RuntimeException("Schedule not found")
            }
    }

    fun publishQuiz(request: PublishQuizRequest): ScheduleDto {
        logger.info("Publishing quiz with ID: ${request.quizId}")
        val quiz = quizRepository.findById(request.quizId)
            .orElseThrow {
                logger.error("Quiz not found with ID: ${request.quizId}")
                RuntimeException("quiz not found")
            }

        quiz.status = QuizStatus.PUBLISHED
        quizRepository.save(quiz)

        val schedule = Schedule(
            quiz = quiz,
            startDateTime = request.quizStartDateTime,
            endDateTime = request.quizEndDateTime,
            createdAt = LocalDateTime.now(),
            status = ScheduleStatus.PUBLISHED,
        )
        val saved = scheduleRepository.save(schedule)
        logger.info("Quiz published and schedule created with ID: ${saved.id}")
        return scheduleToDto(saved)
    }

    fun getAllSchedulesFiltered(status: ScheduleStatus?): List<ScheduleDto> {
        logger.info("Retrieving all schedules with status: $status")
        val spec = scheduleSpecification(status)
        val schedules = scheduleRepository.findAll(spec)
        return schedules.map { scheduleToDto(it) }
    }

    @Transactional
    fun cancelSchedule(id: String): ScheduleDto {
        val existingSchedule = scheduleRepository.findById(id)
            .orElseThrow { ScheduleException("Schedule Not Found with Id ${id}", HttpStatus.NOT_FOUND) }
        if (existingSchedule.status == ScheduleStatus.PUBLISHED || existingSchedule.status == ScheduleStatus.ACTIVE) {
            existingSchedule.status = ScheduleStatus.CANCELLED
            scheduleRepository.save(existingSchedule)
            val existingQuiz = quizRepository.findByQuizId(existingSchedule.quiz.quizId.toString())
            if (existingQuiz != null) {
                existingQuiz.status = QuizStatus.CREATED
                quizRepository.save(existingQuiz)
            }
        } else {
            throw ScheduleException("Only Published or Active Schedules can be cancelled", HttpStatus.FORBIDDEN)
        }
        return scheduleToDto(existingSchedule)

    }

    @Transactional
    fun reschedule(scheduleId: String, reschedule: ScheduleEditCreateRequest): ScheduleDto {
        val existingSchedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { ScheduleException("Schedule not found with Id ${scheduleId}", HttpStatus.NOT_FOUND) }
        if (existingSchedule.status == ScheduleStatus.PUBLISHED || existingSchedule.status == ScheduleStatus.CANCELLED) {
            existingSchedule.startDateTime = reschedule.startDateTime
            existingSchedule.endDateTime = reschedule.endDateTime
            existingSchedule.status = ScheduleStatus.PUBLISHED
            scheduleRepository.save(existingSchedule)
            val existingQuiz = quizRepository.findByQuizId(existingSchedule.quiz.quizId.toString())
            if (existingQuiz != null) {
                existingQuiz.status = QuizStatus.PUBLISHED
                quizRepository.save(existingQuiz)
            }
        } else {
            throw ScheduleException(
                "Only Published and Cancelled Schedules are allowed to Reschedule",
                HttpStatus.FORBIDDEN
            )
        }
        return scheduleToDto(existingSchedule)

    }
}