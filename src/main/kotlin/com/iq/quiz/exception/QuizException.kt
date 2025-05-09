package com.iq.quiz.exception

import org.springframework.http.HttpStatus

class QuizException(message: String, val status: HttpStatus): RuntimeException(message) {

}