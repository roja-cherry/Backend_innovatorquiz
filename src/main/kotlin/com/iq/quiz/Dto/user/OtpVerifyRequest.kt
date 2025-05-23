package com.iq.quiz.Dto.user

data class OtpVerifyRequest(
    val email: String,
    val code: Int
)
