package com.quocard.bookmanagement.repository

import com.quocard.bookmanagement.jooq.tables.references.AUTHORS
import com.quocard.bookmanagement.jooq.tables.references.BOOK_AUTHORS
import com.quocard.bookmanagement.model.Author
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * 著者データへのアクセスを担うリポジトリ。jOOQを使用してDBと通信する。
 */
@Repository
class AuthorRepository(private val dsl: DSLContext) {

    /**
     * 著者を新規挿入する。
     *
     * @param name 著者名
     * @param birthDate 生年月日
     * @return 挿入された著者
     */
    fun insert(name: String, birthDate: LocalDate): Author {
        val record = dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .returning()
            .fetchOne() ?: error("著者の挿入に失敗しました")
        return record.toAuthor()
    }

    /**
     * 指定したIDの著者情報を更新する。
     *
     * @param id 更新対象の著者ID
     * @param name 新しい著者名
     * @param birthDate 新しい生年月日
     * @return 更新後の著者、対象が存在しない場合は null
     */
    fun update(id: Long, name: String, birthDate: LocalDate): Author? {
        val record = dsl.update(AUTHORS)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .where(AUTHORS.ID.eq(id))
            .returning()
            .fetchOne()
        return record?.toAuthor()
    }

    /**
     * IDで著者を1件取得する。
     *
     * @param id 著者ID
     * @return 著者、存在しない場合は null
     */
    fun findById(id: Long): Author? =
        dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne()
            ?.toAuthor()

    /**
     * 複数のIDで著者を一括取得する。
     *
     * @param ids 著者IDのリスト
     * @return 見つかった著者のリスト
     */
    fun findAllByIds(ids: List<Long>): List<Author> =
        dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.`in`(ids))
            .fetch()
            .map { it.toAuthor() }

    /**
     * 指定した著者が紐づく書籍IDを取得する。
     *
     * @param authorId 著者ID
     * @return 書籍IDのリスト
     */
    fun findBookIdsByAuthorId(authorId: Long): List<Long> =
        dsl.select(BOOK_AUTHORS.BOOK_ID)
            .from(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .fetch(BOOK_AUTHORS.BOOK_ID)
            .filterNotNull()

    /** jOOQの [AuthorsRecord] を [Author] ドメインモデルに変換する */
    private fun com.quocard.bookmanagement.jooq.tables.records.AuthorsRecord.toAuthor() = Author(
        id = id ?: error("著者IDが存在しません"),
        name = name ?: error("著者名が存在しません"),
        birthDate = birthDate ?: error("著者生年月日が存在しません"),
    )
}
