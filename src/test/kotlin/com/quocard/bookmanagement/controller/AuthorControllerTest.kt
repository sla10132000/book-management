package com.quocard.bookmanagement.controller

import com.quocard.bookmanagement.exception.GlobalExceptionHandler
import com.quocard.bookmanagement.model.Author
import com.quocard.bookmanagement.model.Book
import com.quocard.bookmanagement.model.PublicationStatus
import com.quocard.bookmanagement.service.AuthorService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import java.time.LocalDate

class AuthorControllerTest {

    private val authorService = mockk<AuthorService>()
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        val validator = LocalValidatorFactoryBean()
        validator.afterPropertiesSet()

        mockMvc = MockMvcBuilders
            .standaloneSetup(AuthorController(authorService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .build()
    }

    private val author = Author(id = 1L, name = "夏目漱石", birthDate = LocalDate.of(1867, 2, 9))

    @Test
    fun `POST authors - 正常系 - 201 を返す`() {
        every { authorService.createAuthor("夏目漱石", LocalDate.of(1867, 2, 9)) } returns author

        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"夏目漱石","birthDate":"1867-02-09"}"""),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("夏目漱石"))
            .andExpect(jsonPath("$.birthDate").value("1867-02-09"))
    }

    @Test
    fun `POST authors - name が空の場合 - 400 を返す`() {
        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"","birthDate":"1867-02-09"}"""),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST authors - birthDate が未来日の場合 - 400 を返す`() {
        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"テスト著者","birthDate":"2099-01-01"}"""),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `PUT authors authorId - 正常系 - 200 を返す`() {
        val updated = author.copy(name = "更新著者")
        every { authorService.updateAuthor(1L, "更新著者", LocalDate.of(1867, 2, 9)) } returns updated

        mockMvc.perform(
            put("/authors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"更新著者","birthDate":"1867-02-09"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("更新著者"))
    }

    @Test
    fun `PUT authors authorId - 存在しない著者の場合 - 404 を返す`() {
        every { authorService.updateAuthor(999L, any(), any()) } throws NoSuchElementException("著者が見つかりません: id=999")

        mockMvc.perform(
            put("/authors/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"名前","birthDate":"1900-01-01"}"""),
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `GET authors authorId books - 正常系 - 書籍一覧を返す`() {
        val book = Book(
            id = 1L,
            title = "吾輩は猫である",
            price = 1200,
            publicationStatus = PublicationStatus.PUBLISHED,
            authors = listOf(author),
        )
        every { authorService.getBooksByAuthor(1L) } returns listOf(book)

        mockMvc.perform(get("/authors/1/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].title").value("吾輩は猫である"))
            .andExpect(jsonPath("$[0].price").value(1200))
            .andExpect(jsonPath("$[0].publicationStatus").value("PUBLISHED"))
    }

    @Test
    fun `GET authors authorId books - 著者が存在しない場合 - 404 を返す`() {
        every { authorService.getBooksByAuthor(999L) } throws NoSuchElementException("著者が見つかりません: id=999")

        mockMvc.perform(get("/authors/999/books"))
            .andExpect(status().isNotFound)
    }
}
