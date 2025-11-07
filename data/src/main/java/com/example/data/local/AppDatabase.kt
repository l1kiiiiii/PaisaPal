package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.entity.SavedPlaceEntity
import com.example.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, SavedPlaceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savedPlaceDao(): SavedPlaceDao
}
