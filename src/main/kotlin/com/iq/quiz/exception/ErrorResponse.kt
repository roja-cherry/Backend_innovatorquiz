package com.iq.quiz.exception

import java.time.LocalDateTime

data class ErrorResponse (
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status : Int,
    val message : String? ,

)