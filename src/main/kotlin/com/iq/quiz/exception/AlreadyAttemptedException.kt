package com.iq.quiz.exception

import com.iq.quiz.Entity.QuizStatus
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class AlreadyAttemptedException(
    reason: String
) : ResponseStatusException(HttpStatus.BAD_REQUEST, reason)