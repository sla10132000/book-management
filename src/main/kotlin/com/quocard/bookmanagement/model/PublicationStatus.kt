package com.quocard.bookmanagement.model

/**
 * 書籍の出版状況を表す列挙型。
 *
 * 出版済みから未出版への変更は業務ルールにより禁止されている。
 */
enum class PublicationStatus {
    /** 未出版 */
    UNPUBLISHED,
    /** 出版済み */
    PUBLISHED,
}
