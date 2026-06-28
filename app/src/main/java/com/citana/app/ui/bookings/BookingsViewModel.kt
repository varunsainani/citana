package com.citana.app.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citana.app.data.repo.BookingRepository
import com.citana.app.domain.model.Booking
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingsUiState(
    val bookings: List<Booking> = emptyList(),
    val loading: Boolean = true,
)

@HiltViewModel
class BookingsViewModel @Inject constructor(
    private val repo: BookingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(BookingsUiState())
    val state: StateFlow<BookingsUiState> = _state.asStateFlow()

    init {
        repo.observeMyBookings()
            .onEach { list -> _state.update { it.copy(bookings = list, loading = false) } }
            .launchIn(viewModelScope)
    }

    fun cancel(id: String) {
        viewModelScope.launch { runCatching { repo.cancel(id) } }
    }
}
