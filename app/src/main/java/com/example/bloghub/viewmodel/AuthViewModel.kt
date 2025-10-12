package com.example.bloghub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloghub.auth.AuthRepository
import com.example.bloghub.auth.UserModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // UI state
    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val errorMessage: MutableSharedFlow<String> = MutableSharedFlow(extraBufferCapacity = 1)

    // Raw auth state from Firebase
    private val _firebaseUser: MutableStateFlow<FirebaseUser?> = MutableStateFlow(repository.firebaseAuthCurrentUser())
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    val authState: StateFlow<FirebaseUser?> = repository.firebaseUserFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = repository.firebaseAuthCurrentUser()
    )

    // Mapped user model loaded from Firestore users/{uid}
    private val _currentUserModel: MutableStateFlow<UserModel?> = MutableStateFlow(null)
    val currentUser: StateFlow<UserModel?> = _currentUserModel.asStateFlow()

    init {
        // Observe Firebase auth changes and fetch profile document
        viewModelScope.launch {
            authState.collect { fbUser ->
                _firebaseUser.value = fbUser
                if (fbUser == null) {
                    _currentUserModel.value = null
                } else {
                    fetchAndEmitUser(fbUser.uid)
                }
            }
        }
    }

    private suspend fun fetchAndEmitUser(uid: String) {
        try {
            isLoading.value = true
            val doc = firestore.collection("users").document(uid).get().await()
            val model = if (doc.exists()) {
                UserModel(
                    uid = uid,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    bio = doc.getString("bio") ?: "",
                    profileImageUrl = doc.getString("profileImageUrl") ?: "",
                    socialLinks = (doc.get("socialLinks") as? Map<String, String>) ?: emptyMap(),
                    createdAt = doc.getTimestamp("createdAt")?.toDate() // <-- THIS IS THE FIX
                )
            } else null
            _currentUserModel.value = model
        } catch (t: Throwable) {
            errorMessage.tryEmit(t.message ?: "Failed to load user")
        } finally {
            isLoading.value = false
        }
    }

    fun signUpWithEmail(name: String, email: String, password: String, onResult: (Result<UserModel>) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            val result = repository.signUpWithEmail(name, email, password)
            result.fold(
                onSuccess = { user ->
                    launch { fetchAndEmitUser(user.uid) }
                    onResult(Result.success(user))
                },
                onFailure = { e ->
                    errorMessage.tryEmit(e.message ?: "Sign up failed")
                    onResult(Result.failure(e))
                }
            )
            isLoading.value = false
        }
    }

    fun loginWithEmail(email: String, password: String, onResult: (Result<UserModel>) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            val result = repository.loginWithEmail(email, password)
            result.fold(
                onSuccess = { user ->
                    launch { fetchAndEmitUser(user.uid) }
                    onResult(Result.success(user))
                },
                onFailure = { e ->
                    errorMessage.tryEmit(e.message ?: "Login failed")
                    onResult(Result.failure(e))
                }
            )
            isLoading.value = false
        }
    }

    fun loginWithGoogle(idToken: String, onResult: (Result<UserModel>) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            val result = repository.loginWithGoogle(idToken)
            result.fold(
                onSuccess = { user ->
                    launch { fetchAndEmitUser(user.uid) }
                    onResult(Result.success(user))
                },
                onFailure = { e ->
                    errorMessage.tryEmit(e.message ?: "Google sign-in failed")
                    onResult(Result.failure(e))
                }
            )
            isLoading.value = false
        }
    }

    fun signOut() {
        repository.signOut()
    }
}
