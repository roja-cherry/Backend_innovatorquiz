package com.iq.quiz.scheduler

import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.Repository.ScheduleRepository
import com.iq.quiz.service.QuizScheduleService
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class StatusUpdateScheduler (
    private val scheduleRepository: ScheduleRepository,
    private val quizRepository: QuizRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(StatusUpdateScheduler::class.java)

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Scheduled(fixedRate = 60000) // Every 1 minute
    @Transactional
    fun runStatusUpdate() {
        val now = LocalDateTime.now()
        val updatedScheduleCount = scheduleRepository.updateScheduleStatuses(now)
        entityManager.flush()
        val updatedQuizCount = quizRepository.updateQuizStatuses(now)
        logger.info("Updated $updatedScheduleCount schedules status & $updatedQuizCount quiz status")
    }
}