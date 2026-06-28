package com.citana.app.data.repo

import com.citana.app.data.local.CategoryDao
import com.citana.app.data.local.ProviderDao
import com.citana.app.data.remote.CitanaApi
import com.citana.app.data.toDomain
import com.citana.app.data.toEntity
import com.citana.app.domain.model.Category
import com.citana.app.domain.model.Provider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor(
    private val api: CitanaApi,
    private val categoryDao: CategoryDao,
    private val providerDao: ProviderDao,
    private val json: Json,
) {
    fun observeCategories(): Flow<List<Category>> =
        categoryDao.observe().map { list -> list.map { it.toDomain() } }

    suspend fun refreshCategories() {
        val remote = api.categories()
        categoryDao.upsertAll(remote.mapIndexed { i, c -> c.toEntity(i) })
    }

    fun observeProviders(categorySlug: String?): Flow<List<Provider>> {
        val source = if (categorySlug.isNullOrBlank()) {
            providerDao.observeAll()
        } else {
            providerDao.observeByCategory(categorySlug)
        }
        return source.map { list -> list.map { it.toDomain(json) } }
    }

    suspend fun refreshProviders(categorySlug: String?) {
        val remote = api.providers(categorySlug?.ifBlank { null })
        providerDao.upsertAll(remote.map { it.toEntity(json) })
    }

    suspend fun provider(id: String): Provider =
        runCatching { api.provider(id).toDomain() }.getOrElse {
            providerDao.getById(id)?.toDomain(json) ?: throw it
        }
}
