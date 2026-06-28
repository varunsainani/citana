package com.citana.app.data.repo

import com.citana.app.data.remote.CitanaApi
import com.citana.app.data.remote.CreateBookingRequest
import com.citana.app.data.remote.UpdateBookingRequest
import com.citana.app.data.toBooking
import com.citana.app.data.toDomain
import com.citana.app.domain.model.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val api: CitanaApi,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    suspend fun availability(providerId: String, date: String, serviceId: String): List<String> =
        api.availability(providerId, date, serviceId).slots

    suspend fun createBooking(providerId: String, serviceId: String, startAt: String): Booking =
        api.createBooking(CreateBookingRequest(providerId, serviceId, startAt)).toDomain()

    suspend fun cancel(id: String): Booking =
        api.updateBooking(id, UpdateBookingRequest("cancelled")).toDomain()

    /**
     * The signed-in user's bookings. The REST API is the source of truth (polled
     * lightly so create/cancel always reflect), plus a best-effort Firestore
     * snapshot listener that pushes instant updates when security rules allow it.
     */
    fun observeMyBookings(): Flow<List<Booking>> = callbackFlow {
        val uid = auth.currentUser?.uid

        val registration: ListenerRegistration? = uid?.let { id ->
            firestore.collection("bookings")
                .whereEqualTo("userId", id)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        trySend(
                            snapshot.documents
                                .mapNotNull { it.toBooking() }
                                .sortedByDescending { it.startAt },
                        )
                    }
                }
        }

        val poll = launch {
            while (isActive) {
                runCatching { api.myBookings().map { it.toDomain() } }.onSuccess { trySend(it) }
                delay(5000)
            }
        }

        awaitClose {
            registration?.remove()
            poll.cancel()
        }
    }
}
