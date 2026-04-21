package com.quocard.bookmanagement.repository

import com.quocard.bookmanagement.jooq.tables.references.AUTHORS
import com.quocard.bookmanagement.jooq.tables.references.BOOK_AUTHORS
import com.quocard.bookmanagement.jooq.tables.references.BOOKS
import com.quocard.bookmanagement.model.Author
import com.quocard.bookmanagement.model.Book
import com.quocard.bookmanagement.model.PublicationStatus
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository

/**
 * 書籍データへのアクセスを担うリポジトリ。jOOQを使用してDBと通信する。
 */
@Repository
class BookRepository(private val dsl: DSLContext) {

    /**
     * 書籍と著者の紐づけを新規挿入する。
     *
     * @param title タイトル
     * @param price 価格
     * @param publicationStatus 出版状況
     * @param authorIds 著者IDのリスト
     * @return 挿入された書籍（著者情報含む）
     */
    fun insert(title: String, price: Int, publicationStatus: PublicationStatus, authorIds: List<Long>): Book {
        val bookRecord = dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, title)
            .set(BOOKS.PRICE, price)
            .set(BOOKS.PUBLICATION_STATUS, publicationStatus.name)
            .returning()
            .fetchOne() ?: error("書籍の挿入に失敗しました")

        val bookId = bookRecord.id ?: error("書籍IDが存在しません")
        insertBookAuthors(bookId, authorIds)

        return findWithAuthorsById(bookId) ?: error("挿入した書籍が見つかりません")
    }

    /**
     * 指定したIDの書籍情報を更新する。
     *
     * 著者の紐づけは一度全削除して再登録する。
     *
     * @param id 更新対象の書籍ID
     * @param title 新しいタイトル
     * @param price 新しい価格
     * @param publicationStatus 新しい出版状況
     * @param authorIds 新しい著者IDのリスト
     * @return 更新後の書籍（著者情報含む）、対象が存在しない場合は null
     */
    fun update(id: Long, title: String, price: Int, publicationStatus: PublicationStatus, authorIds: List<Long>): Book? {
        val updated = dsl.update(BOOKS)
            .set(BOOKS.TITLE, title)
            .set(BOOKS.PRICE, price)
            .set(BOOKS.PUBLICATION_STATUS, publicationStatus.name)
            .where(BOOKS.ID.eq(id))
            .execute()

        if (updated == 0) return null

        // 著者の紐づけを洗い替え
        dsl.deleteFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(id))
            .execute()
        insertBookAuthors(id, authorIds)

        return findWithAuthorsById(id)
    }

    /**
     * IDで書籍を著者情報付きで1件取得する。
     *
     * @param id 書籍ID
     * @return 書籍（著者情報含む）、存在しない場合は null
     */
    fun findWithAuthorsById(id: Long): Book? = findWithAuthorsByIds(listOf(id)).firstOrNull()

    /**
     * 複数のIDで書籍を著者情報付きで一括取得する。
     *
     * BOOKS → BOOK_AUTHORS → AUTHORS をJOINして取得する。
     *
     * @param ids 書籍IDのリスト
     * @return 書籍のリスト（著者情報含む）
     */
    fun findWithAuthorsByIds(ids: List<Long>): List<Book> {
        if (ids.isEmpty()) return emptyList()
        val rows = dsl
            .select(
                BOOKS.ID,
                BOOKS.TITLE,
                BOOKS.PRICE,
                BOOKS.PUBLICATION_STATUS,
                AUTHORS.ID,
                AUTHORS.NAME,
                AUTHORS.BIRTH_DATE,
            )
            .from(BOOKS)
            .join(BOOK_AUTHORS).on(BOOK_AUTHORS.BOOK_ID.eq(BOOKS.ID))
            .join(AUTHORS).on(AUTHORS.ID.eq(BOOK_AUTHORS.AUTHOR_ID))
            .where(BOOKS.ID.`in`(ids))
            .fetch()
        return mapRowsToBooks(rows)
    }

    /**
     * 指定したIDの書籍の出版状況のみを取得する。
     *
     * 更新前の状態確認など、書籍全体を取得せずに済む場合に使用する。
     *
     * @param id 書籍ID
     * @return 出版状況、存在しない場合は null
     */
    fun findPublicationStatusById(id: Long): PublicationStatus? =
        dsl.select(BOOKS.PUBLICATION_STATUS)
            .from(BOOKS)
            .where(BOOKS.ID.eq(id))
            .fetchOne(BOOKS.PUBLICATION_STATUS)
            ?.let { PublicationStatus.valueOf(it) }

    /**
     * JOINで取得したフラットなレコード群を [Book] のリストに変換する。
     *
     * 書籍IDでグルーピングし、著者を集約する。
     */
    private fun mapRowsToBooks(rows: List<Record>): List<Book> =
        rows
            .groupBy { it[BOOKS.ID] ?: error("書籍IDがnullです") }
            .map { (_, bookRows) ->
                val first = bookRows.first()
                Book(
                    id = first[BOOKS.ID] ?: error("書籍IDがnullです"),
                    title = first[BOOKS.TITLE] ?: error("書籍タイトルがnullです"),
                    price = first[BOOKS.PRICE] ?: error("書籍価格がnullです"),
                    publicationStatus = PublicationStatus.valueOf(first[BOOKS.PUBLICATION_STATUS] ?: error("出版状況がnullです")),
                    authors = bookRows.map { row ->
                        Author(
                            id = row[AUTHORS.ID] ?: error("著者IDがnullです"),
                            name = row[AUTHORS.NAME] ?: error("著者名がnullです"),
                            birthDate = row[AUTHORS.BIRTH_DATE] ?: error("著者生年月日がnullです"),
                        )
                    },
                )
            }

    /**
     * 書籍と著者の紐づけを [BOOK_AUTHORS] テーブルに挿入する。
     *
     * @param bookId 書籍ID
     * @param authorIds 著者IDのリスト
     */
    private fun insertBookAuthors(bookId: Long, authorIds: List<Long>) {
        authorIds.forEach { authorId ->
            dsl.insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, bookId)
                .set(BOOK_AUTHORS.AUTHOR_ID, authorId)
                .execute()
        }
    }
}
