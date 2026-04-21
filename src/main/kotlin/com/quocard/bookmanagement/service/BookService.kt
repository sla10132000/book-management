package com.quocard.bookmanagement.service

import com.quocard.bookmanagement.model.Book
import com.quocard.bookmanagement.model.PublicationStatus
import com.quocard.bookmanagement.repository.AuthorRepository
import com.quocard.bookmanagement.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 書籍に関するビジネスロジックを担うサービス。
 */
@Service
class BookService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
) {

    /**
     * 書籍を新規登録する。
     *
     * @param title 書籍タイトル
     * @param price 価格（0以上）
     * @param publicationStatus 出版状況
     * @param authorIds 著者IDのリスト（1件以上）
     * @return 登録された書籍
     * @throws IllegalArgumentException 価格が負・著者未指定・存在しない著者IDが含まれる場合
     */
    @Transactional
    fun createBook(title: String, price: Int, publicationStatus: PublicationStatus, authorIds: List<Long>): Book {
        require(price >= 0) { "価格は0以上である必要があります" }
        require(authorIds.isNotEmpty()) { "著者は1人以上指定する必要があります" }
        validateAuthorsExist(authorIds)
        return bookRepository.insert(title, price, publicationStatus, authorIds)
    }

    /**
     * 書籍情報を更新する。
     *
     * 出版済み（[PublicationStatus.PUBLISHED]）の書籍を未出版に変更することはできない。
     *
     * @param id 更新対象の書籍ID
     * @param title 新しいタイトル
     * @param price 新しい価格（0以上）
     * @param publicationStatus 新しい出版状況
     * @param authorIds 新しい著者IDのリスト（1件以上）
     * @return 更新後の書籍
     * @throws IllegalArgumentException 価格が負・著者未指定・出版済みから未出版への変更・存在しない著者IDが含まれる場合
     * @throws NoSuchElementException 指定したIDの書籍が存在しない場合
     */
    @Transactional
    fun updateBook(id: Long, title: String, price: Int, publicationStatus: PublicationStatus, authorIds: List<Long>): Book {
        require(price >= 0) { "価格は0以上である必要があります" }
        require(authorIds.isNotEmpty()) { "著者は1人以上指定する必要があります" }

        val currentStatus = bookRepository.findPublicationStatusById(id)
            ?: throw NoSuchElementException("書籍が見つかりません: id=$id")

        // 出版済みから未出版への変更は業務ルール上禁止
        require(currentStatus != PublicationStatus.PUBLISHED || publicationStatus == PublicationStatus.PUBLISHED) {
            "出版済みの書籍を未出版に変更することはできません"
        }

        validateAuthorsExist(authorIds)
        return bookRepository.update(id, title, price, publicationStatus, authorIds)
            ?: throw NoSuchElementException("書籍が見つかりません: id=$id")
    }

    /**
     * 指定した著者IDが全てDBに存在することを検証する。
     *
     * @param authorIds 検証する著者IDリスト
     * @throws IllegalArgumentException 存在しない著者IDが含まれている場合
     */
    private fun validateAuthorsExist(authorIds: List<Long>) {
        val foundAuthors = authorRepository.findAllByIds(authorIds)
        val foundIds = foundAuthors.map { it.id }.toSet()
        val missingIds = authorIds.filter { it !in foundIds }
        require(missingIds.isEmpty()) { "存在しない著者IDが含まれています: $missingIds" }
    }
}
