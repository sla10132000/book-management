package com.quocard.bookmanagement.repository

import com.quocard.bookmanagement.jooq.tables.references.AUTHORS
import com.quocard.bookmanagement.jooq.tables.references.BOOK_AUTHORS
import com.quocard.bookmanagement.model.Author
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class AuthorRepository(private val dsl: DSLContext) {

    fun insert(name: String, birthDate: LocalDate): Author {
        val record = dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .returning()
            .fetchOne() ?: error("著者の挿入に失敗しました")
        return record.toAuthor()
    }

    fun update(id: Long, name: String, birthDate: LocalDate): Author? {
        val record = dsl.update(AUTHORS)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .where(AUTHORS.ID.eq(id))
            .returning()
            .fetchOne()
        return record?.toAuthor()
    }

    fun findById(id: Long): Author? =
        dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne()
            ?.toAuthor()

    fun findAllByIds(ids: List<Long>): List<Author> =
        dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.`in`(ids))
            .fetch()
            .map { it.toAuthor() }

    fun findBookIdsByAuthorId(authorId: Long): List<Long> =
        dsl.select(BOOK_AUTHORS.BOOK_ID)
            .from(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .fetch(BOOK_AUTHORS.BOOK_ID)
            .filterNotNull()

    private fun com.quocard.bookmanagement.jooq.tables.records.AuthorsRecord.toAuthor() = Author(
        id = id ?: error("著者IDが存在しません"),
        name = name ?: error("著者名が存在しません"),
        birthDate = birthDate ?: error("著者生年月日が存在しません"),
    )
}
