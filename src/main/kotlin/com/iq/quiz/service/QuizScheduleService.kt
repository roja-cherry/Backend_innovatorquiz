package com.iq.quiz.service

import com.iq.quiz.Dto.PublishQuizRequest
import com.iq.quiz.Dto.ScheduleDto
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.Entity.ScheduleStatus
import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.Repository.ScheduleRepository
import com.iq.quiz.mapper.scheduleToDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class QuizScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val quizRepository: QuizRepository
) {

    fun getScheduleById(scheduleId: String): Schedule {
        return scheduleRepository.findById(scheduleId)
            .orElseThrow { RuntimeException("Schedule not found") }
    }

    fun publishQuiz(request: PublishQuizRequest):ScheduleDto{
        val quiz=quizRepository.findById(request.quizId)
            .orElseThrow(){

                RuntimeException("quiz not found")}
                quiz.status=QuizStatus.PUBLISHED
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
}