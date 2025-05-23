package com.iq.quiz.service

import com.iq.quiz.Dto.QuizLoginDto
import com.iq.quiz.Dto.user.LoginRequestDto
import com.iq.quiz.Dto.user.OtpRequest
import com.iq.quiz.Dto.user.OtpVerifyRequest
import com.iq.quiz.Dto.user.UserDto
import com.iq.quiz.Entity.Otp
import com.iq.quiz.Entity.User
import com.iq.quiz.Entity.UserRole
import com.iq.quiz.Repository.OtpRepository
import com.iq.quiz.Repository.ScheduleRepository
import com.iq.quiz.Repository.UserRepository
import com.iq.quiz.auth.JwtService
import com.iq.quiz.exception.AuthException
import com.iq.quiz.exception.ScheduleException
import com.iq.quiz.mapper.userToDto
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute.Use
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val scheduleRepository: ScheduleRepository,
    private val jwtService: JwtService,
    private val otpRepository: OtpRepository,
    private val emailService: EmailService
) {

    private val logger: Logger = LoggerFactory.getLogger(AuthService::class.java)

    fun loginForSchedule(scheduleId: String?, quizLoginDto: QuizLoginDto): UserDto {
        if(scheduleId != null) {
            scheduleRepository.findById(scheduleId)
                .orElseThrow {
                    logger.debug("Invalid schedule id found for login $scheduleId")
                    ScheduleException("Sorry, quiz schedule not found, please check URL", HttpStatus.BAD_REQUEST)
                }

        }
        val user = userRepository.findByEmail(quizLoginDto.email)

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

    fun login(loginRequestDto: LoginRequestDto): String {
        val user = userRepository.findByEmail(loginRequestDto.email)
            .orElseThrow { AuthException("User not registered with this email", HttpStatus.NOT_FOUND) }

        if(user.password != loginRequestDto.password) {
            throw AuthException("Invalid email or password", HttpStatus.BAD_REQUEST)
        }
        return jwtService.createToken(user)
    }

    fun getUserProfile(token: String): UserDto {
        val claims = jwtService.extractClaims(token)
        val user = userRepository.findByEmail(claims.subject)
            .orElseThrow { AuthException("Failed to authenticate user", HttpStatus.NOT_FOUND) }
        return userToDto(user)
    }

    @Transactional
    fun generateOtp(otpRequest: OtpRequest): String {
        try {
            val user = userRepository.findByEmail(otpRequest.email)
                .orElseGet {
                    userRepository.save(
                        User(
                            email = otpRequest.email,
                            username = otpRequest.email,
                            role = UserRole.PARTICIPANT
                        )
                    )
                }

            val code = (1000..9999).random()
            otpRepository.save(Otp(user = user, code = code))

            emailService.sendEmail(to = otpRequest.email, subject = "IQ Quiz App Login OTP", text = "Your otp is: $code")
            return "Otp sent to email"
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw AuthException("Failed to sent email", HttpStatus.INTERNAL_SERVER_ERROR)
        }

    }


    fun verifyOtp(verifyDto: OtpVerifyRequest): String {
        val user = userRepository.findByEmail(verifyDto.email)
            .orElseThrow { AuthException("User not requested for otp", HttpStatus.BAD_REQUEST) }
        val latestOtp = otpRepository.findLatestByUser(user)
            .orElseThrow{AuthException("No OTP request found", HttpStatus.BAD_REQUEST)}
        if(latestOtp.code != verifyDto.code)
            throw AuthException("Invalid otp", HttpStatus.BAD_REQUEST)

        return jwtService.createToken(user)
    }
}