package com.citana.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citana.app.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isSignUp: Boolean = false,
    val loading: Boolean = false,
    val error: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun toggleMode() = _state.update { it.copy(isSignUp = !it.isSignUp, error = false) }

    fun submit(name: String, email: String, password: String) {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            val signUp = _state.value.isSignUp
            val result = if (signUp) auth.signUp(name, email, password) else auth.signIn(email, password)
            _state.update { it.copy(loading = false, error = result.isFailure) }
        }
    }

    fun demo() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            val result = auth.signInDemo()
            _state.update { it.copy(loading = false, error = result.isFailure) }
        }
    }
}
