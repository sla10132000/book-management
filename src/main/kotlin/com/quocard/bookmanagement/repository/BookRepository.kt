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

@Repository
class BookRepository(private val dsl: DSLContext) {

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

    fun update(id: Long, title: String, price: Int, publicationStatus: PublicationStatus, authorIds: List<Long>): Book? {
        val updated = dsl.update(BOOKS)
            .set(BOOKS.TITLE, title)
            .set(BOOKS.PRICE, price)
            .set(BOOKS.PUBLICATION_STATUS, publicationStatus.name)
            .where(BOOKS.ID.eq(id))
            .execute()

        if (updated == 0) return null

        dsl.deleteFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(id))
            .execute()
        insertBookAuthors(id, authorIds)

        return findWithAuthorsById(id)
    }

    fun findWithAuthorsById(id: Long): Book? = findWithAuthorsByIds(listOf(id)).firstOrNull()

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

    fun findPublicationStatusById(id: Long): PublicationStatus? =
        dsl.select(BOOKS.PUBLICATION_STATUS)
            .from(BOOKS)
            .where(BOOKS.ID.eq(id))
            .fetchOne(BOOKS.PUBLICATION_STATUS)
            ?.let { PublicationStatus.valueOf(it) }

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

    private fun insertBookAuthors(bookId: Long, authorIds: List<Long>) {
        authorIds.forEach { authorId ->
            dsl.insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, bookId)
                .set(BOOK_AUTHORS.AUTHOR_ID, authorId)
                .execute()
        }
    }
}
