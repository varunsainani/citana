package com.citana.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String? = null,
    val slug: String,
    val name: String,
    val icon: String = "event",
)

@Serializable
data class ServiceDto(
    val id: String,
    val name: String,
    val durationMin: Int,
    val priceCents: Long,
    val currency: String = "USD",
)

@Serializable
data class ProviderDto(
    val id: String,
    val name: String,
    val categorySlug: String,
    val bio: String = "",
    val city: String = "",
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val imageUrl: String = "",
    val services: List<ServiceDto> = emptyList(),
)

@Serializable
data class AvailabilityDto(
    val date: String,
    val serviceId: String,
    val durationMin: Int = 0,
    val slots: List<String> = emptyList(),
)

@Serializable
data class BookingDto(
    val id: String,
    val providerId: String,
    val providerName: String,
    val serviceId: String,
    val serviceName: String,
    val startAt: String,
    val endAt: String,
    val priceCents: Long,
    val currency: String = "USD",
    val status: String,
)

@Serializable
data class CreateBookingRequest(
    val providerId: String,
    val serviceId: String,
    val startAt: String,
)

@Serializable
data class UpdateBookingRequest(
    val status: String,
)
