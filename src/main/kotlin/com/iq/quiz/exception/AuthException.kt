package com.iq.quiz.exception

import org.springframework.http.HttpStatus

class AuthException(message: String, val status: HttpStatus): RuntimeException(message) {
}