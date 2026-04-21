package com.quocard.bookmanagement.model

import java.time.LocalDate

/**
 * 著者を表すドメインモデル。
 *
 * @property id 著者の一意識別子
 * @property name 著者名
 * @property birthDate 著者の生年月日
 */
data class Author(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
)
