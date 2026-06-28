package com.citana.app.ui.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citana.app.data.repo.BookingRepository
import com.citana.app.data.repo.CatalogRepository
import com.citana.app.domain.model.Provider
import com.citana.app.domain.model.Service
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BookUiState(
    val provider: Provider? = null,
    val service: Service? = null,
    val dates: List<LocalDate> = emptyList(),
    val selectedDate: LocalDate? = null,
    val slots: List<String> = emptyList(),
    val selectedSlot: String? = null,
    val loading: Boolean = true,
    val slotsLoading: Boolean = false,
    val booking: Boolean = false,
    val booked: Boolean = false,
    val error: Boolean = false,
    val bookFailed: Boolean = false,
)

@HiltViewModel
class BookViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    private val bookings: BookingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val providerId: String = savedStateHandle.get<String>("providerId") ?: ""
    private val serviceId: String = savedStateHandle.get<String>("serviceId") ?: ""

    private val _state = MutableStateFlow(
        BookUiState(dates = (0L until 14L).map { LocalDate.now().plusDays(it) }),
    )
    val state: StateFlow<BookUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            runCatching { catalog.provider(providerId) }
                .onSuccess { p ->
                    val service = p.services.firstOrNull { it.id == serviceId }
                    _state.update { it.copy(provider = p, service = service, loading = false, error = false) }
                    _state.value.dates.firstOrNull()?.let { selectDate(it) }
                }
                .onFailure { _state.update { it.copy(loading = false, error = true) } }
        }
    }

    fun selectDate(date: LocalDate) {
        _state.update {
            it.copy(selectedDate = date, selectedSlot = null, slots = emptyList(), slotsLoading = true)
        }
        viewModelScope.launch {
            val slots = runCatching {
                bookings.availability(providerId, date.toString(), serviceId)
            }.getOrDefault(emptyList())
            _state.update { it.copy(slots = slots, slotsLoading = false) }
        }
    }

    fun selectSlot(slot: String) {
        _state.update { it.copy(selectedSlot = slot, bookFailed = false) }
    }

    fun confirm() {
        val date = _state.value.selectedDate ?: return
        val slot = _state.value.selectedSlot ?: return
        _state.update { it.copy(booking = true, bookFailed = false) }
        viewModelScope.launch {
            val startAt = "${date}T$slot:00.000Z"
            runCatching { bookings.createBooking(providerId, serviceId, startAt) }
                .onSuccess { _state.update { it.copy(booking = false, booked = true) } }
                .onFailure { _state.update { it.copy(booking = false, bookFailed = true) } }
        }
    }
}
