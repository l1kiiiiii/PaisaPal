package com.example.domain.model

data class SavedPlace(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val radius: Int
)
