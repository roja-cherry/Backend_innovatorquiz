package com.iq.quiz.exception

import org.springframework.http.HttpStatus

class ScheduleException(message: String, val status: HttpStatus): RuntimeException(message) {
}