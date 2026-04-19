package com.quocard.bookmanagement.controller

import com.quocard.bookmanagement.exception.GlobalExceptionHandler
import com.quocard.bookmanagement.model.Author
import com.quocard.bookmanagement.model.Book
import com.quocard.bookmanagement.model.PublicationStatus
import com.quocard.bookmanagement.service.BookService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import java.time.LocalDate

class BookControllerTest {

    private val bookService = mockk<BookService>()
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        val validator = LocalValidatorFactoryBean()
        validator.afterPropertiesSet()

        mockMvc = MockMvcBuilders
            .standaloneSetup(BookController(bookService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .build()
    }

    private val author = Author(id = 1L, name = "夏目漱石", birthDate = LocalDate.of(1867, 2, 9))
    private val book = Book(
        id = 1L,
        title = "吾輩は猫である",
        price = 1200,
        publicationStatus = PublicationStatus.UNPUBLISHED,
        authors = listOf(author),
    )

    @Test
    fun `POST books - 正常系 - 201 を返す`() {
        every {
            bookService.createBook("吾輩は猫である", 1200, PublicationStatus.UNPUBLISHED, listOf(1L))
        } returns book

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"吾輩は猫である","price":1200,"publicationStatus":"UNPUBLISHED","authorIds":[1]}"""),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("吾輩は猫である"))
            .andExpect(jsonPath("$.price").value(1200))
            .andExpect(jsonPath("$.publicationStatus").value("UNPUBLISHED"))
    }

    @Test
    fun `POST books - title が空の場合 - 400 を返す`() {
        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"","price":1200,"publicationStatus":"UNPUBLISHED","authorIds":[1]}"""),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST books - 価格が負の場合 - 400 を返す`() {
        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"タイトル","price":-1,"publicationStatus":"UNPUBLISHED","authorIds":[1]}"""),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST books - authorIds が空の場合 - 400 を返す`() {
        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"タイトル","price":1200,"publicationStatus":"UNPUBLISHED","authorIds":[]}"""),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `PUT books bookId - 正常系 - 200 を返す`() {
        val updatedBook = book.copy(title = "更新タイトル", publicationStatus = PublicationStatus.PUBLISHED)
        every {
            bookService.updateBook(1L, "更新タイトル", 1500, PublicationStatus.PUBLISHED, listOf(1L))
        } returns updatedBook

        mockMvc.perform(
            put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"更新タイトル","price":1500,"publicationStatus":"PUBLISHED","authorIds":[1]}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("更新タイトル"))
            .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"))
    }

    @Test
    fun `PUT books bookId - 存在しない書籍の場合 - 404 を返す`() {
        every { bookService.updateBook(999L, any(), any(), any(), any()) } throws
            NoSuchElementException("書籍が見つかりません: id=999")

        mockMvc.perform(
            put("/books/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"タイトル","price":1200,"publicationStatus":"UNPUBLISHED","authorIds":[1]}"""),
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `PUT books bookId - PUBLISHED から UNPUBLISHED への変更 - 400 を返す`() {
        every { bookService.updateBook(1L, any(), any(), PublicationStatus.UNPUBLISHED, any()) } throws
            IllegalArgumentException("出版済みの書籍を未出版に変更することはできません")

        mockMvc.perform(
            put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"タイトル","price":1200,"publicationStatus":"UNPUBLISHED","authorIds":[1]}"""),
        )
            .andExpect(status().isBadRequest)
    }
}
