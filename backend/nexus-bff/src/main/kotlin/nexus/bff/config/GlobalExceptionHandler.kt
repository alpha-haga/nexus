package nexus.bff.config

import nexus.core.exception.AuthorizationException
import nexus.core.exception.NotImplementedException
import nexus.core.exception.ResourceNotFoundException
import nexus.core.exception.ValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import jakarta.validation.ConstraintViolationException
import java.time.LocalDateTime

/**
 * グローバル例外ハンドラー（BFF用）
 * 
 * P1-B0 で定義された 200/400/403/404 に対応
 * - 400: Validation 系（ValidationException + Spring validation 系）
 * - 403: AuthorizationException
 * - 404: ResourceNotFoundException
 * - 500: その他の例外
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotImplementedException::class)
    fun handleNotImplemented(ex: NotImplementedException): ResponseEntity<ErrorResponse> {
        val details = mapOf(
            "feature" to ex.feature
        )

        return ResponseEntity
            .status(HttpStatus.NOT_IMPLEMENTED)
            .body(
                ErrorResponse(
                    status = HttpStatus.NOT_IMPLEMENTED.value(),
                    error = "Not Implemented",
                    message = ex.message ?: "Not implemented",
                    code = "NOT_IMPLEMENTED",
                    details = details
                )
            )
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ErrorResponse> {
        val details = mapOf(
            "field" to ex.field,
            "reason" to ex.reason
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Bad Request",
                    message = ex.message ?: "Validation failed",
                    code = "VALIDATION_ERROR",
                    details = details
                )
            )
    }

    @ExceptionHandler(
        MethodArgumentNotValidException::class,
        ConstraintViolationException::class,
        BindException::class
    )
    fun handleSpringValidation(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Bad Request",
                    message = "Validation failed",
                    code = "VALIDATION_ERROR"
                )
            )
    }

    @ExceptionHandler(AuthorizationException::class)
    fun handleAuthorization(ex: AuthorizationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(
                ErrorResponse(
                    status = HttpStatus.FORBIDDEN.value(),
                    error = "Forbidden",
                    message = ex.message ?: "Access denied",
                    code = "AUTHORIZATION_ERROR"
                )
            )
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    status = HttpStatus.NOT_FOUND.value(),
                    error = "Not Found",
                    message = ex.message ?: "Resource not found",
                    code = "RESOURCE_NOT_FOUND"
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = "Internal Server Error",
                    message = "An unexpected error occurred",
                    code = "INTERNAL_ERROR"
                )
            )
    }
}

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val code: String,
    val details: Map<String, Any>? = null
)
