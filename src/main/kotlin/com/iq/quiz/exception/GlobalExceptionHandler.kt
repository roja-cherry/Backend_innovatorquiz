package com.iq.quiz.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {


    @ExceptionHandler(FileFormatException::class)
    fun handleFileFormatException(ex : FileFormatException) : ResponseEntity<ErrorResponse>{
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            message = ex.message
        )
        return ResponseEntity(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    }

    @ExceptionHandler(QuizNotFoundException::class)
    fun handleQuizNoFound(ex :QuizNotFoundException) : ResponseEntity<ErrorResponse>{
            val error = ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.NOT_FOUND.value(),
                message = ex.message,
            )
            return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

}