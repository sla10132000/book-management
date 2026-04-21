package com.quocard.bookmanagement.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * アプリケーション全体の例外をハンドリングし、統一されたエラーレスポンスを返すアドバイスクラス。
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * リクエストボディのバリデーションエラーを処理する（400 Bad Request）。
     *
     * @param ex バリデーション例外
     * @return フィールドエラーメッセージを結合したエラーレスポンス
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .joinToString(", ") { it.defaultMessage ?: "バリデーションエラー" }
        return ResponseEntity.badRequest().body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), message))
    }

    /**
     * 業務ルール違反などの不正引数例外を処理する（400 Bad Request）。
     *
     * @param ex 不正引数例外
     * @return エラーレスポンス
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest().body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.message ?: "不正なリクエストです"))

    /**
     * リソースが見つからない場合の例外を処理する（404 Not Found）。
     *
     * @param ex 要素なし例外
     * @return エラーレスポンス
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.message ?: "リソースが見つかりません"))

    /**
     * 上記以外の予期しない例外を処理する（500 Internal Server Error）。
     *
     * @param ex 例外
     * @return エラーレスポンス
     */
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.internalServerError().body(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "予期せぬエラーが発生しました"))
}

/**
 * エラーレスポンスのデータクラス。
 *
 * @property status HTTPステータスコード
 * @property message エラーメッセージ
 */
data class ErrorResponse(
    val status: Int,
    val message: String,
)
