package com.quocard.bookmanagement.service

import com.quocard.bookmanagement.model.Book
import com.quocard.bookmanagement.model.PublicationStatus
import com.quocard.bookmanagement.repository.AuthorRepository
import com.quocard.bookmanagement.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
) {

    @Transactional
    fun createBook(title: String, price: Int, publicationStatus: PublicationStatus, authorIds: List<Long>): Book {
        require(price >= 0) { "価格は0以上である必要があります" }
        require(authorIds.isNotEmpty()) { "著者は1人以上指定する必要があります" }
        validateAuthorsExist(authorIds)
        return bookRepository.insert(title, price, publicationStatus, authorIds)
    }

    @Transactional
    fun updateBook(id: Long, title: String, price: Int, publicationStatus: PublicationStatus, authorIds: List<Long>): Book {
        require(price >= 0) { "価格は0以上である必要があります" }
        require(authorIds.isNotEmpty()) { "著者は1人以上指定する必要があります" }

        val currentStatus = bookRepository.findPublicationStatusById(id)
            ?: throw NoSuchElementException("書籍が見つかりません: id=$id")

        require(currentStatus != PublicationStatus.PUBLISHED || publicationStatus == PublicationStatus.PUBLISHED) {
            "出版済みの書籍を未出版に変更することはできません"
        }

        validateAuthorsExist(authorIds)
        return bookRepository.update(id, title, price, publicationStatus, authorIds)
            ?: throw NoSuchElementException("書籍が見つかりません: id=$id")
    }

    private fun validateAuthorsExist(authorIds: List<Long>) {
        val foundAuthors = authorRepository.findAllByIds(authorIds)
        val foundIds = foundAuthors.map { it.id }.toSet()
        val missingIds = authorIds.filter { it !in foundIds }
        require(missingIds.isEmpty()) { "存在しない著者IDが含まれています: $missingIds" }
    }
}
