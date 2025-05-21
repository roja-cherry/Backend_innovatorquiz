package com.iq.quiz.service

import com.iq.quiz.Dto.QuizLoginDto
import com.iq.quiz.Dto.user.UserDto
import com.iq.quiz.Entity.User
import com.iq.quiz.Entity.UserRole
import com.iq.quiz.Repository.ScheduleRepository
import com.iq.quiz.Repository.UserRepository
import com.iq.quiz.exception.ScheduleException
import com.iq.quiz.mapper.userToDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val scheduleRepository: ScheduleRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(AuthService::class.java)

    fun loginForSchedule(scheduleId: String, quizLoginDto: QuizLoginDto): UserDto {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow {
                logger.debug("Invalid schedule id found for login $scheduleId")
                ScheduleException("Sorry, quiz schedule not found, please check URL", HttpStatus.BAD_REQUEST)
            }

        val user = userRepository.findByEmailAndUsername(quizLoginDto.email, quizLoginDto.username)

        if(user.isPresent)
            return userToDto(user.get())

        val newUser = User(
            username = quizLoginDto.username,
            email = quizLoginDto.email,
            role = UserRole.PARTICIPANT
        )

        val savedNewUser = userRepository.save(newUser)
        return userToDto(savedNewUser)
    }
}