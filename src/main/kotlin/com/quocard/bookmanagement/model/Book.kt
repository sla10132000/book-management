package com.quocard.bookmanagement.model

/**
 * 書籍を表すドメインモデル。
 *
 * @property id 書籍の一意識別子
 * @property title 書籍タイトル
 * @property price 価格（0以上）
 * @property publicationStatus 出版状況
 * @property authors 著者リスト（1人以上）
 */
data class Book(
    val id: Long,
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus,
    val authors: List<Author>,
)
