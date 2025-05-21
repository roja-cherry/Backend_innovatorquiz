package com.iq.quiz.auth

import com.iq.quiz.Dto.user.UserDto
import com.iq.quiz.service.AuthService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val authService: AuthService
) {

    @GetMapping("/profile")
    fun getProfile(@RequestHeader("Authorization") authHeader: String): UserDto {
        return authService.getUserProfile(token = authHeader)
    }
}