package com.iq.quiz.exception

import com.iq.quiz.service.QuizScheduleService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
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
    return "An error occurred!"
}

data class ValidationErrorResponse(
    val status: Int,
    val message: String = "Field errors",
    val errors: List<FieldValidationError>
)

data class FieldValidationError(
    val field: String,
    val error: String
)

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(FileFormatException::class)
    fun handleFileFormatException(ex : FileFormatException) : ResponseEntity<ErrorResponse>{
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            message = ex.message
        )
        logger.error(ex.stackTrace.toString())
        return ResponseEntity(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    }

    @ExceptionHandler(QuizNotFoundException::class)
    fun handleQuizNoFound(ex :QuizNotFoundException) : ResponseEntity<ErrorResponse>{
            val error = ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.NOT_FOUND.value(),
                message = ex.message,
            )
            logger.error(ex.stackTrace.toString())
            return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(QuizException::class)
    fun handleQuizException(ex: QuizException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = ex.status.value(),
            message = ex.message
        )
        logger.error(ex.stackTrace.toString())
        return ResponseEntity.status(ex.status).body(error)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityException(ex: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            message = getDuplicateFieldMessage(ex.localizedMessage),
            status = HttpStatus.BAD_REQUEST.value()
        )
        logger.error(ex.stackTrace.toString())
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(ScheduleException::class)
    fun handleScheduleException(ex: ScheduleException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            message = ex.message,
            status = HttpStatus.BAD_REQUEST.value()
        )
        logger.error(ex.stackTrace.toString())
        return ResponseEntity.status(ex.status).body(response)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            message = ex.message,
            status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        )
        logger.error(ex.stackTrace.toString())
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    @ExceptionHandler(AlreadyAttemptedException::class)
    fun handleAlreadyAttempted(ex: AlreadyAttemptedException): ResponseEntity<ErrorResponse> {
        logger.error("AlreadyAttemptedException", ex)
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status    = ex.statusCode.value(),              // ← use statusCode here
            message   = ex.reason ?: "You have already submitted this quiz."
        )
        return ResponseEntity.status(ex.statusCode).body(error)
    }

    @ExceptionHandler(AuthException::class)
    fun handleAuthException(ex: AuthException): ResponseEntity<ErrorResponse> {
        logger.trace("AlreadyAttemptedException", ex)
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status    = ex.status.value(),              // ← use statusCode here
            message   = ex.message ?: "Error in authenticating user"
        )
        return ResponseEntity.status(ex.status).body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        logger.trace("AlreadyAttemptedException", ex)
        val fieldErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            FieldValidationError(
                field = fieldError.field,
                error = fieldError.defaultMessage ?: "Invalid value"
            )
        }

        val response = ValidationErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            errors = fieldErrors
        )

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }


}
