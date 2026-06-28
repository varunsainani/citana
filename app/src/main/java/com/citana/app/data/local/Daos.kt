package com.citana.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sort")
    fun observe(): Flow<List<CategoryEntity>>

    @Upsert
    suspend fun upsertAll(items: List<CategoryEntity>)
}

@Dao
interface ProviderDao {
    @Query("SELECT * FROM providers ORDER BY rating DESC")
    fun observeAll(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE categorySlug = :category ORDER BY rating DESC")
    fun observeByCategory(category: String): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE id = :id")
    suspend fun getById(id: String): ProviderEntity?

    @Upsert
    suspend fun upsertAll(items: List<ProviderEntity>)
}
