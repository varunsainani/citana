package com.citana.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val slug: String,
    val name: String,
    val icon: String,
    val sort: Int,
)

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val categorySlug: String,
    val bio: String,
    val city: String,
    val rating: Double,
    val ratingCount: Int,
    val imageUrl: String,
    val servicesJson: String,
)
