package com.quocard.bookmanagement.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .joinToString(", ") { it.defaultMessage ?: "バリデーションエラー" }
        return ResponseEntity.badRequest().body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), message))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest().body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.message ?: "不正なリクエストです"))

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.message ?: "リソースが見つかりません"))

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.internalServerError().body(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "予期せぬエラーが発生しました"))
}

data class ErrorResponse(
    val status: Int,
    val message: String,
)
