package com.quocard.bookmanagement.service

import com.quocard.bookmanagement.model.Author
import com.quocard.bookmanagement.model.Book
import com.quocard.bookmanagement.model.PublicationStatus
import com.quocard.bookmanagement.repository.AuthorRepository
import com.quocard.bookmanagement.repository.BookRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class AuthorServiceTest {

    private val authorRepository = mockk<AuthorRepository>()
    private val bookRepository = mockk<BookRepository>()
    private val authorService = AuthorService(authorRepository, bookRepository)

    @Test
    fun `createAuthor - 正常系`() {
        val name = "夏目漱石"
        val birthDate = LocalDate.of(1867, 2, 9)
        val expected = Author(id = 1L, name = name, birthDate = birthDate)
        every { authorRepository.insert(name, birthDate) } returns expected

        val result = authorService.createAuthor(name, birthDate)

        assertEquals(expected, result)
        verify { authorRepository.insert(name, birthDate) }
    }

    @Test
    fun `createAuthor - 生年月日が現在日の場合は登録できる`() {
        val name = "テスト著者"
        val birthDate = LocalDate.now()
        val expected = Author(id = 1L, name = name, birthDate = birthDate)
        every { authorRepository.insert(name, birthDate) } returns expected

        val result = authorService.createAuthor(name, birthDate)

        assertEquals(expected, result)
    }

    @Test
    fun `createAuthor - 生年月日が未来日の場合は例外がスローされる`() {
        val exception = assertThrows<IllegalArgumentException> {
            authorService.createAuthor("テスト著者", LocalDate.now().plusDays(1))
        }
        assertEquals("生年月日は現在日以前である必要があります", exception.message)
    }

    @Test
    fun `updateAuthor - 正常系`() {
        val id = 1L
        val name = "更新著者"
        val birthDate = LocalDate.of(1900, 1, 1)
        val expected = Author(id = id, name = name, birthDate = birthDate)
        every { authorRepository.update(id, name, birthDate) } returns expected

        val result = authorService.updateAuthor(id, name, birthDate)

        assertEquals(expected, result)
    }

    @Test
    fun `updateAuthor - 存在しない著者IDの場合は例外がスローされる`() {
        val id = 999L
        every { authorRepository.update(id, any(), any()) } returns null

        val exception = assertThrows<NoSuchElementException> {
            authorService.updateAuthor(id, "名前", LocalDate.of(1900, 1, 1))
        }
        assertEquals("著者が見つかりません: id=$id", exception.message)
    }

    @Test
    fun `updateAuthor - 生年月日が未来日の場合は例外がスローされる`() {
        assertThrows<IllegalArgumentException> {
            authorService.updateAuthor(1L, "名前", LocalDate.now().plusDays(1))
        }
    }

    @Test
    fun `getBooksByAuthor - 正常系`() {
        val authorId = 1L
        val author = Author(id = authorId, name = "夏目漱石", birthDate = LocalDate.of(1867, 2, 9))
        val bookIds = listOf(10L, 20L)
        val books = listOf(
            Book(id = 10L, title = "吾輩は猫である", price = 1000, publicationStatus = PublicationStatus.PUBLISHED, authors = listOf(author)),
            Book(id = 20L, title = "坊っちゃん", price = 800, publicationStatus = PublicationStatus.PUBLISHED, authors = listOf(author)),
        )
        every { authorRepository.findById(authorId) } returns author
        every { authorRepository.findBookIdsByAuthorId(authorId) } returns bookIds
        every { bookRepository.findWithAuthorsByIds(bookIds) } returns books

        val result = authorService.getBooksByAuthor(authorId)

        assertEquals(books, result)
        verify { authorRepository.findBookIdsByAuthorId(authorId) }
        verify { bookRepository.findWithAuthorsByIds(bookIds) }
    }

    @Test
    fun `getBooksByAuthor - 著者が存在しない場合は例外がスローされる`() {
        val authorId = 999L
        every { authorRepository.findById(authorId) } returns null

        val exception = assertThrows<NoSuchElementException> {
            authorService.getBooksByAuthor(authorId)
        }
        assertEquals("著者が見つかりません: id=$authorId", exception.message)
    }
}
