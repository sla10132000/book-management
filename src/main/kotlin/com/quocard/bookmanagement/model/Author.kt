package com.quocard.bookmanagement.model

import java.time.LocalDate

data class Author(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
)
