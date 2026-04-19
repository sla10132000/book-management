package com.quocard.bookmanagement.model

data class Book(
    val id: Long,
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus,
    val authors: List<Author>,
)
