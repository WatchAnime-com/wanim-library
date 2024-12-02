package com.wanim_ms.wanimlibrary.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception, request: HttpServletRequest): ResponseEntity<ExceptionModel> {
        val status = when (ex) {
            is Exceptions.BadRequestEx -> HttpStatus.BAD_REQUEST
            is IllegalArgumentException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR // Varsayılan 500
        }

        val message = ex.message ?: "An unexpected error occurred"
        val exceptionModel = ExceptionModel(
            timestamp = LocalDateTime.now(),
            status = status.value(),
            message = message,
            path = request.requestURI
        )

        return ResponseEntity(exceptionModel, status)
    }
}
