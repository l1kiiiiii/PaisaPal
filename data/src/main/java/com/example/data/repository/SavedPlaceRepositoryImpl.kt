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
import android.location.Location
import kotlinx.coroutines.flow.first


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
    override suspend fun findNearbyPlace(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double
    ): SavedPlace? {
        val allPlaces = getAllPlaces().first()

        return allPlaces.firstOrNull { place ->
            val distance = calculateDistance(
                latitude, longitude,
                place.latitude, place.longitude
            )
            distance <= radiusMeters
        }
    }
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0].toDouble()
    }
}
