package com.quocard.bookmanagement.service

import com.quocard.bookmanagement.model.Author
import com.quocard.bookmanagement.model.Book
import com.quocard.bookmanagement.repository.AuthorRepository
import com.quocard.bookmanagement.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class AuthorService(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository,
) {

    @Transactional
    fun createAuthor(name: String, birthDate: LocalDate): Author {
        require(birthDate <= LocalDate.now()) { "生年月日は現在日以前である必要があります" }
        return authorRepository.insert(name, birthDate)
    }

    @Transactional
    fun updateAuthor(id: Long, name: String, birthDate: LocalDate): Author {
        require(birthDate <= LocalDate.now()) { "生年月日は現在日以前である必要があります" }
        return authorRepository.update(id, name, birthDate)
            ?: throw NoSuchElementException("著者が見つかりません: id=$id")
    }

    @Transactional(readOnly = true)
    fun getBooksByAuthor(authorId: Long): List<Book> {
        authorRepository.findById(authorId)
            ?: throw NoSuchElementException("著者が見つかりません: id=$authorId")
        val bookIds = authorRepository.findBookIdsByAuthorId(authorId)
        return bookRepository.findWithAuthorsByIds(bookIds)
    }
}
