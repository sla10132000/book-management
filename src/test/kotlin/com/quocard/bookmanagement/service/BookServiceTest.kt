package com.quocard.bookmanagement.service

import com.quocard.bookmanagement.model.Author
import com.quocard.bookmanagement.model.Book
import com.quocard.bookmanagement.model.PublicationStatus
import com.quocard.bookmanagement.repository.AuthorRepository
import com.quocard.bookmanagement.repository.BookRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class BookServiceTest {

    private val bookRepository = mockk<BookRepository>()
    private val authorRepository = mockk<AuthorRepository>()
    private val bookService = BookService(bookRepository, authorRepository)

    private val author = Author(id = 1L, name = "夏目漱石", birthDate = LocalDate.of(1867, 2, 9))
    private val book = Book(
        id = 1L,
        title = "吾輩は猫である",
        price = 1200,
        publicationStatus = PublicationStatus.UNPUBLISHED,
        authors = listOf(author),
    )

    @Test
    fun `createBook - 正常系`() {
        every { authorRepository.findAllByIds(listOf(1L)) } returns listOf(author)
        every { bookRepository.insert("吾輩は猫である", 1200, PublicationStatus.UNPUBLISHED, listOf(1L)) } returns book

        val result = bookService.createBook("吾輩は猫である", 1200, PublicationStatus.UNPUBLISHED, listOf(1L))

        assertEquals(book, result)
    }

    @Test
    fun `createBook - 価格が0の場合は登録できる`() {
        val freeBook = book.copy(price = 0)
        every { authorRepository.findAllByIds(listOf(1L)) } returns listOf(author)
        every { bookRepository.insert("吾輩は猫である", 0, PublicationStatus.UNPUBLISHED, listOf(1L)) } returns freeBook

        val result = bookService.createBook("吾輩は猫である", 0, PublicationStatus.UNPUBLISHED, listOf(1L))

        assertEquals(freeBook, result)
    }

    @Test
    fun `createBook - 価格が負の値の場合は例外がスローされる`() {
        val exception = assertThrows<IllegalArgumentException> {
            bookService.createBook("タイトル", -1, PublicationStatus.UNPUBLISHED, listOf(1L))
        }
        assertEquals("価格は0以上である必要があります", exception.message)
    }

    @Test
    fun `createBook - 著者IDが空の場合は例外がスローされる`() {
        val exception = assertThrows<IllegalArgumentException> {
            bookService.createBook("タイトル", 1000, PublicationStatus.UNPUBLISHED, emptyList())
        }
        assertEquals("著者は1人以上指定する必要があります", exception.message)
    }

    @Test
    fun `createBook - 存在しない著者IDが含まれる場合は例外がスローされる`() {
        every { authorRepository.findAllByIds(listOf(1L, 999L)) } returns listOf(author)

        val exception = assertThrows<IllegalArgumentException> {
            bookService.createBook("タイトル", 1000, PublicationStatus.UNPUBLISHED, listOf(1L, 999L))
        }
        assert(exception.message!!.contains("999"))
    }

    @Test
    fun `updateBook - UNPUBLISHED から PUBLISHED への変更は成功する`() {
        val updatedBook = book.copy(publicationStatus = PublicationStatus.PUBLISHED)
        every { bookRepository.findPublicationStatusById(1L) } returns PublicationStatus.UNPUBLISHED
        every { authorRepository.findAllByIds(listOf(1L)) } returns listOf(author)
        every { bookRepository.update(1L, "吾輩は猫である", 1200, PublicationStatus.PUBLISHED, listOf(1L)) } returns updatedBook

        val result = bookService.updateBook(1L, "吾輩は猫である", 1200, PublicationStatus.PUBLISHED, listOf(1L))

        assertEquals(PublicationStatus.PUBLISHED, result.publicationStatus)
    }

    @Test
    fun `updateBook - PUBLISHED から PUBLISHED への変更は成功する`() {
        val publishedBook = book.copy(publicationStatus = PublicationStatus.PUBLISHED)
        every { bookRepository.findPublicationStatusById(1L) } returns PublicationStatus.PUBLISHED
        every { authorRepository.findAllByIds(listOf(1L)) } returns listOf(author)
        every { bookRepository.update(1L, "吾輩は猫である", 1200, PublicationStatus.PUBLISHED, listOf(1L)) } returns publishedBook

        val result = bookService.updateBook(1L, "吾輩は猫である", 1200, PublicationStatus.PUBLISHED, listOf(1L))

        assertEquals(PublicationStatus.PUBLISHED, result.publicationStatus)
    }

    @Test
    fun `updateBook - PUBLISHED から UNPUBLISHED への変更は例外がスローされる`() {
        every { bookRepository.findPublicationStatusById(1L) } returns PublicationStatus.PUBLISHED

        val exception = assertThrows<IllegalArgumentException> {
            bookService.updateBook(1L, "タイトル", 1200, PublicationStatus.UNPUBLISHED, listOf(1L))
        }
        assertEquals("出版済みの書籍を未出版に変更することはできません", exception.message)
    }

    @Test
    fun `updateBook - 存在しない書籍IDの場合は例外がスローされる`() {
        every { bookRepository.findPublicationStatusById(999L) } returns null

        val exception = assertThrows<NoSuchElementException> {
            bookService.updateBook(999L, "タイトル", 1200, PublicationStatus.UNPUBLISHED, listOf(1L))
        }
        assertEquals("書籍が見つかりません: id=999", exception.message)
    }
}
