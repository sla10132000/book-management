package com.quocard.bookmanagement.controller

import com.quocard.bookmanagement.model.Author
import com.quocard.bookmanagement.model.Book
import com.quocard.bookmanagement.service.AuthorService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 * 著者に関するREST APIエンドポイントを提供するコントローラー。
 */
@RestController
@RequestMapping("/authors")
class AuthorController(private val authorService: AuthorService) {

    /**
     * 著者を新規登録する。
     *
     * @param request 著者作成リクエスト（名前・生年月日）
     * @return 作成された著者のレスポンス
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAuthor(@RequestBody @Valid request: CreateAuthorRequest): AuthorResponse =
        authorService.createAuthor(request.name, request.birthDate).toResponse()

    /**
     * 著者情報を更新する。
     *
     * @param authorId 更新対象の著者ID
     * @param request 著者更新リクエスト（名前・生年月日）
     * @return 更新後の著者のレスポンス
     */
    @PutMapping("/{authorId}")
    fun updateAuthor(
        @PathVariable authorId: Long,
        @RequestBody @Valid request: UpdateAuthorRequest,
    ): AuthorResponse = authorService.updateAuthor(authorId, request.name, request.birthDate).toResponse()

    /**
     * 指定した著者が執筆した書籍一覧を取得する。
     *
     * @param authorId 著者ID
     * @return 書籍レスポンスのリスト
     */
    @GetMapping("/{authorId}/books")
    fun getBooksByAuthor(@PathVariable authorId: Long): List<BookResponse> =
        authorService.getBooksByAuthor(authorId).map { it.toResponse() }
}

/** 著者作成リクエスト */
data class CreateAuthorRequest(
    @field:NotBlank(message = "名前は必須です")
    val name: String,
    @field:PastOrPresent(message = "生年月日は現在日以前である必要があります")
    val birthDate: LocalDate,
)

/** 著者更新リクエスト */
data class UpdateAuthorRequest(
    @field:NotBlank(message = "名前は必須です")
    val name: String,
    @field:PastOrPresent(message = "生年月日は現在日以前である必要があります")
    val birthDate: LocalDate,
)

/** 著者レスポンス */
data class AuthorResponse(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
)

/** 書籍レスポンス */
data class BookResponse(
    val id: Long,
    val title: String,
    val price: Int,
    val publicationStatus: String,
    val authors: List<AuthorResponse>,
)

/** [Author] ドメインモデルをレスポンスに変換する */
fun Author.toResponse() = AuthorResponse(id = id, name = name, birthDate = birthDate)

/** [Book] ドメインモデルをレスポンスに変換する */
fun Book.toResponse() = BookResponse(
    id = id,
    title = title,
    price = price,
    publicationStatus = publicationStatus.name,
    authors = authors.map { it.toResponse() },
)
