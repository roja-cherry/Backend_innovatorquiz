package com.iq.quiz.auth

import com.iq.quiz.Dto.user.OtpRequest
import com.iq.quiz.Dto.user.OtpVerifyRequest
import com.iq.quiz.service.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth/otp")
class OtpAuthController(
    private val authService: AuthService
) {

    @PostMapping
    fun generateOtp(@RequestBody otpRequest: OtpRequest): String {
        return authService.generateOtp(otpRequest)
    }

    @PostMapping("/verify")
    fun verifyOpt(@RequestBody otpVerifyRequest: OtpVerifyRequest): String {
        return authService.verifyOtp(otpVerifyRequest)
    }
}