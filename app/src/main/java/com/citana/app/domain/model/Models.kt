package com.citana.app.domain.model

data class Category(
    val slug: String,
    val name: String,
    val icon: String,
)

data class Service(
    val id: String,
    val name: String,
    val durationMin: Int,
    val priceCents: Long,
    val currency: String = "USD",
)

data class Provider(
    val id: String,
    val name: String,
    val categorySlug: String,
    val bio: String,
    val city: String,
    val rating: Double,
    val ratingCount: Int,
    val imageUrl: String,
    val services: List<Service>,
)

data class Booking(
    val id: String,
    val providerId: String,
    val providerName: String,
    val serviceId: String,
    val serviceName: String,
    val startAt: String,
    val endAt: String,
    val priceCents: Long,
    val currency: String,
    val status: String,
)
