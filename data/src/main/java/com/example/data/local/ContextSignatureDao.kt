package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ContextSignatureEntity

@Dao
interface ContextSignatureDao {
    @Query("SELECT * FROM context_signatures WHERE triggerValue = :triggerValue AND triggerType = :triggerType LIMIT 1")
    suspend fun getByTrigger(triggerType: String, triggerValue: String): ContextSignatureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(signature: ContextSignatureEntity)

    @Update
    suspend fun update(signature: ContextSignatureEntity)

    @Query("DELETE FROM context_signatures WHERE id = :id")
    suspend fun deleteById(id: String)
}
