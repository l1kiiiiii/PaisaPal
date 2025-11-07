package com.example.data.repository

import com.example.data.local.SavedPlaceDao
import com.example.domain.model.SavedPlace
import com.example.domain.repository.SavedPlaceRepository
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.map

class SavedPlaceRepositoryImpl @Inject constructor(
    private val dao: SavedPlaceDao
) : SavedPlaceRepository {

    override fun getAllPlaces(): Flow<List<SavedPlace>> {
        return dao.getAllPlaces().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insert(place: SavedPlace) {
        dao.insert(place.toEntity())
    }

    override suspend fun delete(place: SavedPlace) {
        dao.delete(place.toEntity())
    }
}
