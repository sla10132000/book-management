package com.quocard.bookmanagement.controller

import com.quocard.bookmanagement.model.PublicationStatus
import com.quocard.bookmanagement.service.BookService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 書籍に関するREST APIエンドポイントを提供するコントローラー。
 */
@RestController
@RequestMapping("/books")
class BookController(private val bookService: BookService) {

    /**
     * 書籍を新規登録する。
     *
     * @param request 書籍作成リクエスト（タイトル・価格・出版状況・著者IDリスト）
     * @return 作成された書籍のレスポンス
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBook(@RequestBody @Valid request: CreateBookRequest): BookResponse =
        bookService.createBook(request.title, request.price, request.publicationStatus, request.authorIds).toResponse()

    /**
     * 書籍情報を更新する。
     *
     * 出版済みの書籍を未出版に変更することはできない。
     *
     * @param bookId 更新対象の書籍ID
     * @param request 書籍更新リクエスト（タイトル・価格・出版状況・著者IDリスト）
     * @return 更新後の書籍のレスポンス
     */
    @PutMapping("/{bookId}")
    fun updateBook(
        @PathVariable bookId: Long,
        @RequestBody @Valid request: UpdateBookRequest,
    ): BookResponse =
        bookService.updateBook(bookId, request.title, request.price, request.publicationStatus, request.authorIds)
            .toResponse()
}

/** 書籍作成リクエスト */
data class CreateBookRequest(
    @field:NotBlank(message = "タイトルは必須です")
    val title: String,
    @field:Min(value = 0, message = "価格は0以上である必要があります")
    val price: Int,
    val publicationStatus: PublicationStatus,
    @field:NotEmpty(message = "著者は1人以上指定する必要があります")
    val authorIds: List<Long>,
)

/** 書籍更新リクエスト */
data class UpdateBookRequest(
    @field:NotBlank(message = "タイトルは必須です")
    val title: String,
    @field:Min(value = 0, message = "価格は0以上である必要があります")
    val price: Int,
    val publicationStatus: PublicationStatus,
    @field:NotEmpty(message = "著者は1人以上指定する必要があります")
    val authorIds: List<Long>,
)
