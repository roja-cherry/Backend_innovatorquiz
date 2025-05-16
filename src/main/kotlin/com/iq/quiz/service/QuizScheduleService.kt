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
        logger.info("Attempting to cancel schedule with ID: {}",id)
        val existingSchedule = scheduleRepository.findById(id)
            .orElseThrow {
                logger.error("Schedule not found with Id : {}",id)
                ScheduleException("Schedule Not Found with Id ${id}", HttpStatus.NOT_FOUND)
            }
        logger.info("Found Schedule with Id : {} and Status: {}",id,existingSchedule.status)
        if (existingSchedule.status == ScheduleStatus.PUBLISHED || existingSchedule.status == ScheduleStatus.ACTIVE) {
            logger.info("Schedule with Id :{} is cancellable.Updating status to CANCELLED",id)
            existingSchedule.status = ScheduleStatus.CANCELLED
            scheduleRepository.save(existingSchedule)
            logger.info("Schedule with Id:{} sucessfully cancelled",id)
            val existingQuiz = quizRepository.findByQuizId(existingSchedule.quiz.quizId.toString())
            if (existingQuiz != null) {
                logger.info("Resetting quiz status for quiz Id:{}",existingQuiz.quizId)
                existingQuiz.status = QuizStatus.CREATED
                quizRepository.save(existingQuiz)
                logger.info("Quiz status reset to CREATED for quiz Id:{}",existingQuiz.quizId)
            }
            else
            {
                logger.warn("Quiz not found for quiz Id:{}",existingQuiz?.quizId)
                throw ScheduleException("Quiz not found for qiuz Id:${existingQuiz?.quizId}",HttpStatus.NOT_FOUND)
            }
        } else {
            logger.warn("Cannot cancel schedule with ID:{} as it is in status:{}",id,existingSchedule.status)
            throw ScheduleException("Only Published or Active Schedules can be cancelled", HttpStatus.FORBIDDEN)
        }
        return scheduleToDto(existingSchedule)

    }

    @Transactional
    fun reschedule(scheduleId: String, reschedule: ScheduleEditCreateRequest): ScheduleDto {
        logger.info("Attempting to reschedule schedule with ID: {}",scheduleId)
        val existingSchedule = scheduleRepository.findById(scheduleId)
            .orElseThrow {
                logger.error("Schedule not found with ID: {}", scheduleId)
                ScheduleException("Schedule not found with Id ${scheduleId}", HttpStatus.NOT_FOUND)
            }
        logger.info("Found schedule with ID: {} and current status: {}", scheduleId, existingSchedule.status)
        if (existingSchedule.status == ScheduleStatus.PUBLISHED || existingSchedule.status == ScheduleStatus.CANCELLED) {
            logger.info("Rescheduling schedule ID: {} to new times: start={}, end={}", scheduleId, reschedule.startDateTime, reschedule.endDateTime)
            existingSchedule.startDateTime = reschedule.startDateTime
            existingSchedule.endDateTime = reschedule.endDateTime
            existingSchedule.status = ScheduleStatus.PUBLISHED
            scheduleRepository.save(existingSchedule)
            logger.info("Schedule ID: {} successfully updated and saved with new status: {}", scheduleId, existingSchedule.status)
            val existingQuiz = quizRepository.findByQuizId(existingSchedule.quiz.quizId.toString())
            if (existingQuiz != null) {
                logger.info("Updating quiz status to PUBLISHED for quiz ID: {}", existingQuiz.quizId)
                existingQuiz.status = QuizStatus.PUBLISHED
                quizRepository.save(existingQuiz)
                logger.info("Quiz ID: {} status updated to PUBLISHED", existingQuiz.quizId)
            }
        } else {
            logger.warn("Reschedule not allowed for schedule ID: {} with status: {}", scheduleId, existingSchedule.status)
            throw ScheduleException(
                "Only Published and Cancelled Schedules are allowed to Reschedule",
                HttpStatus.FORBIDDEN
            )
        }
        return scheduleToDto(existingSchedule)

    }
}