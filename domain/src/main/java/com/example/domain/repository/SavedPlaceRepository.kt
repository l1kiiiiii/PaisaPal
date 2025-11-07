package com.example.domain.repository


import com.example.domain.model.SavedPlace
import kotlinx.coroutines.flow.Flow

interface SavedPlaceRepository {
    fun getAllPlaces(): Flow<List<SavedPlace>>
    suspend fun insert(place: SavedPlace)
    suspend fun delete(place: SavedPlace)
}
