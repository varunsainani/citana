package com.citana.app.data.auth

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
) {
    private val _signedIn = MutableStateFlow(auth.currentUser != null)
    val signedIn: StateFlow<Boolean> = _signedIn.asStateFlow()

    init {
        auth.addAuthStateListener { _signedIn.value = it.currentUser != null }
    }

    val currentEmail: String? get() = auth.currentUser?.email
    val currentName: String? get() = auth.currentUser?.displayName

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) { Tasks.await(auth.signInWithEmailAndPassword(email.trim(), password)) }
        Unit
    }

    suspend fun signUp(name: String, email: String, password: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val res = Tasks.await(auth.createUserWithEmailAndPassword(email.trim(), password))
            val update = UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
            res.user?.let { Tasks.await(it.updateProfile(update)) }
        }
        Unit
    }

    suspend fun signInDemo(): Result<Unit> = signIn("demo@citana.app", "demo1234")

    fun signOut() = auth.signOut()
}
