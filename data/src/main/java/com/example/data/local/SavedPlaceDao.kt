package com.example.data.local

import androidx.room.*
import com.example.data.local.entity.SavedPlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlaceDao {

    @Query("SELECT * FROM saved_places")
    fun getAllPlaces(): Flow<List<SavedPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(place: SavedPlaceEntity)

    @Delete
    suspend fun delete(place: SavedPlaceEntity)
}
