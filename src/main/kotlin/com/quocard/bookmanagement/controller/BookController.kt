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

@RestController
@RequestMapping("/books")
class BookController(private val bookService: BookService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBook(@RequestBody @Valid request: CreateBookRequest): BookResponse =
        bookService.createBook(request.title, request.price, request.publicationStatus, request.authorIds).toResponse()

    @PutMapping("/{bookId}")
    fun updateBook(
        @PathVariable bookId: Long,
        @RequestBody @Valid request: UpdateBookRequest,
    ): BookResponse =
        bookService.updateBook(bookId, request.title, request.price, request.publicationStatus, request.authorIds)
            .toResponse()
}

data class CreateBookRequest(
    @field:NotBlank(message = "タイトルは必須です")
    val title: String,
    @field:Min(value = 0, message = "価格は0以上である必要があります")
    val price: Int,
    val publicationStatus: PublicationStatus,
    @field:NotEmpty(message = "著者は1人以上指定する必要があります")
    val authorIds: List<Long>,
)

data class UpdateBookRequest(
    @field:NotBlank(message = "タイトルは必須です")
    val title: String,
    @field:Min(value = 0, message = "価格は0以上である必要があります")
    val price: Int,
    val publicationStatus: PublicationStatus,
    @field:NotEmpty(message = "著者は1人以上指定する必要があります")
    val authorIds: List<Long>,
)
