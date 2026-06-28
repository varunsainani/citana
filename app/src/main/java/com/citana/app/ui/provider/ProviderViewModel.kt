package com.citana.app.ui.provider

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citana.app.data.repo.CatalogRepository
import com.citana.app.domain.model.Provider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProviderUiState(
    val provider: Provider? = null,
    val loading: Boolean = true,
    val error: Boolean = false,
)

@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val id: String = savedStateHandle.get<String>("id") ?: ""

    private val _state = MutableStateFlow(ProviderUiState())
    val state: StateFlow<ProviderUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            runCatching { catalog.provider(id) }
                .onSuccess { p -> _state.update { it.copy(provider = p, loading = false, error = false) } }
                .onFailure { _state.update { it.copy(loading = false, error = true) } }
        }
    }
}
