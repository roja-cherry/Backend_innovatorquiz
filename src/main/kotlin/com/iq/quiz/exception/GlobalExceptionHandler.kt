package com.iq.quiz.exception

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime


fun getDuplicateFieldMessage(msg: String): String {
    val regex = Regex("""Key \((.*?)\)=\((.*?)\) already exists""")
    val matchResult = regex.find(msg)

    if (matchResult != null) {
        val column = matchResult.groupValues[1]  // e.g., "title"
        val value = matchResult.groupValues[2]   // e.g., "GK"
        return  "Duplicate value $value for field $column"
    }
    return "An error occurred while saving fields"
}

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

    @ExceptionHandler(QuizException::class)
    fun handleQuizException(ex: QuizException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = ex.status.value(),
            message = ex.message
        )
        return ResponseEntity.status(ex.status).body(error)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityException(ex: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            message = getDuplicateFieldMessage(ex.localizedMessage),
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(ScheduleException::class)
    fun handleScheduleException(ex: ScheduleException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            message = ex.message,
            status = HttpStatus.BAD_REQUEST.value()
        )
        ex.printStackTrace()
        return ResponseEntity.status(ex.status).body(response)
    }

}