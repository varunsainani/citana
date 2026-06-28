package com.citana.app.ui.browse

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citana.app.data.repo.CatalogRepository
import com.citana.app.domain.model.Provider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BrowseUiState(
    val providers: List<Provider> = emptyList(),
    val loading: Boolean = true,
    val error: Boolean = false,
)

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val slug: String = savedStateHandle.get<String>("slug") ?: ""

    private val _state = MutableStateFlow(BrowseUiState())
    val state: StateFlow<BrowseUiState> = _state.asStateFlow()

    init {
        catalog.observeProviders(slug.ifBlank { null })
            .onEach { list -> _state.update { it.copy(providers = list) } }
            .launchIn(viewModelScope)
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(loading = it.providers.isEmpty(), error = false) }
        viewModelScope.launch {
            val ok = runCatching { catalog.refreshProviders(slug.ifBlank { null }) }.isSuccess
            _state.update { it.copy(loading = false, error = !ok && it.providers.isEmpty()) }
        }
    }
}
