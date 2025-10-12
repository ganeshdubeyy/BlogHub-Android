// 1. CORRECTED PACKAGE NAME to match the file's location
package com.example.bloghub.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloghub.auth.AuthRepository
import com.example.bloghub.auth.UserModel
import com.example.bloghub.data.UserRepository
import com.example.bloghub.util.CloudinaryManager // Import the CloudinaryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 2. UPDATED a new UI State that includes all necessary fields
data class ProfileUiState(
    val user: UserModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false // Added for handling navigation
)

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSaved = false, error = null) } // Reset state on load
            val currentUser = authRepository.firebaseAuthCurrentUser()
            if (currentUser != null) {
                userRepository.getUserProfile(currentUser.uid).onSuccess { userModel ->
                    _uiState.update {
                        it.copy(user = userModel, isLoading = false)
                    }
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(error = "Failed to load profile: ${exception.message}", isLoading = false)
                    }
                }
            } else {
                _uiState.update {
                    it.copy(error = "No authenticated user found.", isLoading = false)
                }
            }
        }
    }

    // 3. ADDED the function to save name and bio
    fun saveProfile(name: String, bio: String) {
        val userId = authRepository.firebaseAuthCurrentUser()?.uid ?: run {
            _uiState.update { it.copy(error = "User not logged in.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSaved = false) }
            val updates = mapOf("name" to name, "bio" to bio)
            userRepository.updateUserProfile(userId, updates).onSuccess {
                // Update the local state immediately for a responsive UI
                _uiState.update { currentState ->
                    currentState.copy(
                        user = currentState.user?.copy(name = name, bio = bio),
                        isLoading = false,
                        isSaved = true // Set flag to trigger navigation
                    )
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, error = "Failed to save profile: ${exception.message}") }
            }
        }
    }

    // 4. ADDED the function to upload the image
    fun uploadProfileImage(imageUri: Uri) {
        val userId = authRepository.firebaseAuthCurrentUser()?.uid ?: run {
            _uiState.update { it.copy(error = "User not logged in to upload image.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val imageUrl = CloudinaryManager.uploadImage(imageUri)
            if (imageUrl != null) {
                userRepository.updateUserProfile(userId, mapOf("profileImageUrl" to imageUrl)).onSuccess {
                    // Update the local state with the new image URL
                    _uiState.update { currentState ->
                        currentState.copy(
                            user = currentState.user?.copy(profileImageUrl = imageUrl),
                            isLoading = false
                        )
                    }
                }.onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to save new image URL: ${exception.message}") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Upload failed: Could not retrieve image URL.") }
            }
        }
    }

    // 5. ADDED the helper to reset the saved state
    fun onSavedStateConsumed() {
        _uiState.update { it.copy(isSaved = false) }
    }
}
