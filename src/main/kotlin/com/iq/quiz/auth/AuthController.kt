package com.iq.quiz.auth

import com.iq.quiz.Dto.QuizLoginDto
import com.iq.quiz.Dto.user.LoginRequestDto
import com.iq.quiz.Dto.user.UserDto
import com.iq.quiz.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/participant-login")
    fun loginForSchedule(@RequestBody quizLoginDto: QuizLoginDto): ResponseEntity<UserDto> {
        val response = authService.loginForSchedule(quizLoginDto)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequestDto: LoginRequestDto): ResponseEntity<String> {
        val token = authService.login(loginRequestDto)
        return ResponseEntity.ok(token)
    }
}