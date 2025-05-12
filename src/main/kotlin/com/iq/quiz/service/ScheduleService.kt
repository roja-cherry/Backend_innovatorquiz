package com.iq.quiz.service
import com.iq.quiz.Dto.ScheduleDto
import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.Repository.ScheduleRepository
import org.springframework.stereotype.Service

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val quizRepository: QuizRepository
) {

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




