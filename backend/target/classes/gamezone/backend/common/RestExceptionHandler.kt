package gamezone.backend.common

import gamezone.backend.auth.InvalidCredentialsException
import gamezone.backend.user.UserAlreadyExistsException
import gamezone.backend.user.UserNotFoundException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExists(ex: UserAlreadyExistsException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.CONFLICT
        return ResponseEntity
            .status(status)
            .body(ErrorResponse(status = status.value(), message = ex.message ?: status.reasonPhrase))
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.UNAUTHORIZED
        return ResponseEntity
            .status(status)
            .body(ErrorResponse(status = status.value(), message = ex.message ?: status.reasonPhrase))
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.NOT_FOUND
        return ResponseEntity
            .status(status)
            .body(ErrorResponse(status = status.value(), message = ex.message ?: status.reasonPhrase))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult
            .allErrors
            .filterIsInstance<FieldError>()
            .associate { it.field to (it.defaultMessage ?: "Valor inválido") }

        val status = HttpStatus.BAD_REQUEST
        return ResponseEntity
            .status(status)
            .body(ErrorResponse(status = status.value(), message = "Validación fallida", errors = errors))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolations(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errors = ex.constraintViolations.associate { violation ->
            val path = violation.propertyPath.toString()
            path to (violation.message ?: "Valor inválido")
        }

        val status = HttpStatus.BAD_REQUEST
        return ResponseEntity
            .status(status)
            .body(ErrorResponse(status = status.value(), message = "Validación fallida", errors = errors))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        return ResponseEntity
            .status(status)
            .body(ErrorResponse(status = status.value(), message = ex.message ?: status.reasonPhrase))
    }
}
