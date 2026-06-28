package com.citana.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citana.app.data.auth.AuthRepository
import com.citana.app.data.repo.CatalogRepository
import com.citana.app.domain.model.Category
import com.citana.app.domain.model.Provider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val categories: List<Category> = emptyList(),
    val featured: List<Provider> = emptyList(),
    val loading: Boolean = true,
    val error: Boolean = false,
    val firstName: String = "",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    auth: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(
        HomeUiState(firstName = auth.currentName?.split(" ")?.firstOrNull().orEmpty()),
    )
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        combine(catalog.observeCategories(), catalog.observeProviders(null)) { c, p -> c to p }
            .onEach { (c, p) -> _state.update { it.copy(categories = c, featured = p.take(6)) } }
            .launchIn(viewModelScope)
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(loading = it.categories.isEmpty(), error = false) }
        viewModelScope.launch {
            val ok = runCatching {
                catalog.refreshCategories()
                catalog.refreshProviders(null)
            }.isSuccess
            _state.update { it.copy(loading = false, error = !ok && it.categories.isEmpty()) }
        }
    }
}
