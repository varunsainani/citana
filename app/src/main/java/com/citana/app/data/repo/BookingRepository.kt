package com.citana.app.data.repo

import com.citana.app.data.remote.CitanaApi
import com.citana.app.data.remote.CreateBookingRequest
import com.citana.app.data.remote.UpdateBookingRequest
import com.citana.app.data.toBooking
import com.citana.app.data.toDomain
import com.citana.app.domain.model.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    /** Realtime stream of the signed-in user's bookings (also works offline via Firestore cache). */
    fun observeMyBookings(): Flow<List<Booking>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("bookings")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.documents
                    ?.mapNotNull { it.toBooking() }
                    ?.sortedByDescending { it.startAt }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }
}
