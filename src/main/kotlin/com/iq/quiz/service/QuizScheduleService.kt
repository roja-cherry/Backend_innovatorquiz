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

    fun scheduleSpecification(
        status: ScheduleStatus? = ScheduleStatus.ACTIVE
    ): Specification<Schedule> {
        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            status?.let {
                predicates.add(cb.equal(root.get<ScheduleStatus>("status"), it))
            }

            cb.and(*predicates.toTypedArray())
        }
    }

    fun getScheduleById(scheduleId: String): Schedule {
        return scheduleRepository.findById(scheduleId)
            .orElseThrow { RuntimeException("Schedule not found") }
    }

    fun publishQuiz(request: PublishQuizRequest): ScheduleDto {
        val quiz = quizRepository.findById(request.quizId)
            .orElseThrow { RuntimeException("quiz not found") }
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
        scheduleRepository.save(saved)
        return scheduleToDto(saved)
    }

    fun getAllSchedulesFiltered(status: ScheduleStatus?): List<ScheduleDto> {
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