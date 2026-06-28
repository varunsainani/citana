package com.citana.app.ui.profile

import androidx.lifecycle.ViewModel
import com.citana.app.core.ThemeController
import com.citana.app.core.ThemeMode
import com.citana.app.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val theme: ThemeController,
) : ViewModel() {
    val email: String = auth.currentEmail.orEmpty()
    val name: String = auth.currentName
        ?.takeIf { it.isNotBlank() }
        ?: email.substringBefore("@").ifBlank { email }
    val themeMode: StateFlow<ThemeMode> = theme.mode

    fun setTheme(mode: ThemeMode) = theme.set(mode)

    fun signOut() = auth.signOut()
}
