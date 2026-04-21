package com.quocard.bookmanagement.service

import com.quocard.bookmanagement.model.Author
import com.quocard.bookmanagement.model.Book
import com.quocard.bookmanagement.repository.AuthorRepository
import com.quocard.bookmanagement.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * 著者に関するビジネスロジックを担うサービス。
 */
@Service
class AuthorService(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository,
) {

    /**
     * 著者を新規登録する。
     *
     * @param name 著者名
     * @param birthDate 生年月日（現在日以前であること）
     * @return 登録された著者
     * @throws IllegalArgumentException 生年月日が現在日より後の場合
     */
    @Transactional
    fun createAuthor(name: String, birthDate: LocalDate): Author {
        require(birthDate <= LocalDate.now()) { "生年月日は現在日以前である必要があります" }
        return authorRepository.insert(name, birthDate)
    }

    /**
     * 著者情報を更新する。
     *
     * @param id 更新対象の著者ID
     * @param name 新しい著者名
     * @param birthDate 新しい生年月日（現在日以前であること）
     * @return 更新後の著者
     * @throws IllegalArgumentException 生年月日が現在日より後の場合
     * @throws NoSuchElementException 指定したIDの著者が存在しない場合
     */
    @Transactional
    fun updateAuthor(id: Long, name: String, birthDate: LocalDate): Author {
        require(birthDate <= LocalDate.now()) { "生年月日は現在日以前である必要があります" }
        return authorRepository.update(id, name, birthDate)
            ?: throw NoSuchElementException("著者が見つかりません: id=$id")
    }

    /**
     * 指定した著者が執筆した書籍一覧を取得する。
     *
     * @param authorId 著者ID
     * @return 書籍のリスト
     * @throws NoSuchElementException 指定したIDの著者が存在しない場合
     */
    @Transactional(readOnly = true)
    fun getBooksByAuthor(authorId: Long): List<Book> {
        authorRepository.findById(authorId)
            ?: throw NoSuchElementException("著者が見つかりません: id=$authorId")
        val bookIds = authorRepository.findBookIdsByAuthorId(authorId)
        return bookRepository.findWithAuthorsByIds(bookIds)
    }
}
