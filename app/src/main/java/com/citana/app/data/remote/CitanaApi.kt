package com.citana.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CitanaApi {
    @GET("categories")
    suspend fun categories(): List<CategoryDto>

    @GET("providers")
    suspend fun providers(@Query("category") category: String?): List<ProviderDto>

    @GET("providers/{id}")
    suspend fun provider(@Path("id") id: String): ProviderDto

    @GET("providers/{id}/availability")
    suspend fun availability(
        @Path("id") id: String,
        @Query("date") date: String,
        @Query("serviceId") serviceId: String,
    ): AvailabilityDto

    @POST("bookings")
    suspend fun createBooking(@Body body: CreateBookingRequest): BookingDto

    @GET("me/bookings")
    suspend fun myBookings(): List<BookingDto>

    @PATCH("bookings/{id}")
    suspend fun updateBooking(@Path("id") id: String, @Body body: UpdateBookingRequest): BookingDto
}
