package com.citana.app.data

import com.citana.app.data.local.CategoryEntity
import com.citana.app.data.local.ProviderEntity
import com.citana.app.data.remote.BookingDto
import com.citana.app.data.remote.CategoryDto
import com.citana.app.data.remote.ProviderDto
import com.citana.app.data.remote.ServiceDto
import com.citana.app.domain.model.Booking
import com.citana.app.domain.model.Category
import com.citana.app.domain.model.Provider
import com.citana.app.domain.model.Service
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun ServiceDto.toDomain() = Service(id, name, durationMin, priceCents, currency)

fun CategoryDto.toDomain() = Category(slug, name, icon)

fun ProviderDto.toDomain() = Provider(
    id = id,
    name = name,
    categorySlug = categorySlug,
    bio = bio,
    city = city,
    rating = rating,
    ratingCount = ratingCount,
    imageUrl = imageUrl,
    services = services.map { it.toDomain() },
)

fun BookingDto.toDomain() = Booking(
    id = id,
    providerId = providerId,
    providerName = providerName,
    serviceId = serviceId,
    serviceName = serviceName,
    startAt = startAt,
    endAt = endAt,
    priceCents = priceCents,
    currency = currency,
    status = status,
)

fun CategoryDto.toEntity(sort: Int) = CategoryEntity(slug, name, icon, sort)

fun CategoryEntity.toDomain() = Category(slug, name, icon)

fun ProviderDto.toEntity(json: Json) =
    ProviderEntity(id, name, categorySlug, bio, city, rating, ratingCount, imageUrl, json.encodeToString<List<ServiceDto>>(services))

fun ProviderEntity.toDomain(json: Json): Provider {
    val services = runCatching { json.decodeFromString<List<ServiceDto>>(servicesJson) }.getOrDefault(emptyList())
    return Provider(id, name, categorySlug, bio, city, rating, ratingCount, imageUrl, services.map { it.toDomain() })
}

fun DocumentSnapshot.toBooking(): Booking? {
    val d = data ?: return null
    return Booking(
        id = id,
        providerId = d["providerId"] as? String ?: "",
        providerName = d["providerName"] as? String ?: "",
        serviceId = d["serviceId"] as? String ?: "",
        serviceName = d["serviceName"] as? String ?: "",
        startAt = d["startAt"] as? String ?: "",
        endAt = d["endAt"] as? String ?: "",
        priceCents = (d["priceCents"] as? Number)?.toLong() ?: 0L,
        currency = d["currency"] as? String ?: "USD",
        status = d["status"] as? String ?: "confirmed",
    )
}
