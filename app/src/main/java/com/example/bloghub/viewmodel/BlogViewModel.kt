package com.example.bloghub.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.bloghub.auth.AuthRepository
import com.example.bloghub.data.BlogModel
import com.example.bloghub.data.BlogRepository
import com.example.bloghub.data.model.NotificationRepository
import com.example.bloghub.data.model.NotificationModel
import com.example.bloghub.data.model.NotificationType
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// --- SOLUTION: SEPARATE STATES FOR EACH SCREEN ---
data class BlogUiState(
    // State for HomeScreen
    val allPosts: List<BlogModel> = emptyList(),
    val allPostsLoading: Boolean = false,

    // State for MyBlogsScreen
    val myPosts: List<BlogModel> = emptyList(),
    val myPostsLoading: Boolean = false,

    // Shared State
    val error: String? = null,
    val postSaved: Boolean = false
)

class BlogViewModel(
    private val blogRepository: BlogRepository = BlogRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlogUiState())
    val uiState = _uiState.asStateFlow()

    // --- READ (ALL POSTS) ---
    fun loadAllPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(allPostsLoading = true, error = null) }
            blogRepository.getAllPosts().onSuccess { posts ->
                _uiState.update { it.copy(allPosts = posts, allPostsLoading = false) }
            }.onFailure { exception ->
                _uiState.update { it.copy(allPostsLoading = false, error = "Failed to load posts: ${exception.message}") }
            }
        }
    }

    // --- READ (LOGGED-IN USER'S POSTS) ---
    fun loadMyPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(myPostsLoading = true, error = null) }
            val userId = authRepository.firebaseAuthCurrentUser()?.uid
            if (userId == null) {
                _uiState.update { it.copy(myPostsLoading = false, error = "User not logged in.") }
                return@launch
            }

            blogRepository.getPostsByUserId(userId).onSuccess { userPosts ->
                _uiState.update { it.copy(myPosts = userPosts, myPostsLoading = false) }
            }.onFailure { exception ->
                // This is the error you were seeing. It's now handled correctly.
                _uiState.update { it.copy(myPostsLoading = false, error = "Failed to load your posts: ${exception.message}") }
            }
        }
    }

    // --- CREATE ---
    fun createPost(title: String, content: String, category: String, imageUri: Uri?) {
        viewModelScope.launch {
            // Set a general loading state for saving
            _uiState.update { it.copy(postSaved = false, error = null, allPostsLoading = true, myPostsLoading = true) }

            val userId = authRepository.firebaseAuthCurrentUser()?.uid
            if (userId == null) {
                _uiState.update { it.copy(error = "User not logged in.", allPostsLoading = false, myPostsLoading = false) }
                return@launch
            }

            val result = imageUri?.let { uploadImageToCloudinary(it) }

            if (result?.isFailure == true) {
                _uiState.update { it.copy(error = "Image upload failed: ${result.exceptionOrNull()?.message}", allPostsLoading = false, myPostsLoading = false) }
                return@launch
            }

            createPostInFirestore(userId, title, content, category, result?.getOrNull())
        }
    }

    // --- UPDATE ---
    fun updatePost(postId: String, title: String, content: String, category: String, newImageUri: Uri?) {
        viewModelScope.launch {
            _uiState.update { it.copy(postSaved = false, error = null, allPostsLoading = true, myPostsLoading = true) }

            val result = newImageUri?.let { uploadImageToCloudinary(it) }

            if (result?.isFailure == true) {
                _uiState.update { it.copy(error = "Image upload failed: ${result.exceptionOrNull()?.message}", allPostsLoading = false, myPostsLoading = false) }
                return@launch
            }

            updatePostInFirestore(postId, title, content, category, result?.getOrNull())
        }
    }

    // --- DELETE ---
    fun deletePost(postId: String) {
        viewModelScope.launch {
            // Optimistically update the UI immediately
            _uiState.update { currentState ->
                currentState.copy(
                    allPosts = currentState.allPosts.filterNot { it.id == postId },
                    myPosts = currentState.myPosts.filterNot { it.id == postId }
                )
            }
            // Then perform the delete in the background
            blogRepository.deletePost(postId).onFailure { exception ->
                _uiState.update { it.copy(error = "Failed to delete post: ${exception.message}") }
                // Optional: If delete fails, refresh the data to revert the optimistic update
                loadAllPosts()
                loadMyPosts()
            }
        }
    }

    // --- HELPERS ---
    private suspend fun uploadImageToCloudinary(imageUri: Uri): Result<String> = suspendCoroutine { continuation ->
        MediaManager.get().upload(imageUri).callback(object : UploadCallback {
            override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                val imageUrl = resultData?.get("secure_url") as? String
                continuation.resume(imageUrl?.let { Result.success(it) } ?: Result.failure(Exception("Cloudinary URL was null.")))
            }
            override fun onError(requestId: String?, error: ErrorInfo?) {
                continuation.resume(Result.failure(Exception(error?.description ?: "Unknown Cloudinary error.")))
            }
            override fun onStart(requestId: String?) {}
            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
            override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
        }).dispatch()
    }

    private fun createPostInFirestore(userId: String, title: String, content: String, category: String, imageUrl: String?) {
        viewModelScope.launch {
            val userProfileResult = authRepository.getUserProfile(userId)
            if (userProfileResult.isFailure) {
                _uiState.update { it.copy(error = "Could not fetch author profile.", allPostsLoading = false, myPostsLoading = false) }
                return@launch
            }
            val newPost = BlogModel(
                title = title,
                content = content,
                category = category,
                author = userProfileResult.getOrNull(),
                createdAt = Timestamp.now(),
                imageUrl = imageUrl
            )

            blogRepository.createPost(newPost).onSuccess {
                _uiState.update { it.copy(postSaved = true) } // Signal that save is complete
            }.onFailure { exception ->
                _uiState.update { it.copy(error = "Error creating post: ${exception.message}", allPostsLoading = false, myPostsLoading = false) }
            }
        }
    }

    private fun updatePostInFirestore(postId: String, title: String, content: String, category: String, newImageUrl: String?) {
        viewModelScope.launch {
            blogRepository.updatePost(postId, title, content, category, newImageUrl).onSuccess {
                _uiState.update { it.copy(postSaved = true) } // Signal that save is complete
            }.onFailure { exception ->
                _uiState.update { it.copy(error = "Error updating post: ${exception.message}", allPostsLoading = false, myPostsLoading = false) }
            }
        }
    }
    // Helper to reset the saved flag for navigation
    fun onPostSavedConsumed() {
        _uiState.update { it.copy(postSaved = false) }
    }

    // Helper to clear error messages when navigating between screens
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // --- LIKE/UNLIKE ---
    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val userId = authRepository.firebaseAuthCurrentUser()?.uid
            if (userId == null) {
                _uiState.update { it.copy(error = "You must be logged in to like posts.") }
                return@launch
            }

            // Find the post in either list
            val post = (uiState.value.allPosts + uiState.value.myPosts).find { it.id == postId }
            if (post == null) return@launch

            val currentlyLiked = post.isLikedBy(userId)

            // Optimistically update the UI immediately
            val updatedLikedBy = if (currentlyLiked) {
                post.likedBy.filter { it != userId }
            } else {
                post.likedBy + userId
            }

            val updatedPost = post.copy(likedBy = updatedLikedBy)

            // Update both lists
            _uiState.update { currentState ->
                currentState.copy(
                    allPosts = currentState.allPosts.map { if (it.id == postId) updatedPost else it },
                    myPosts = currentState.myPosts.map { if (it.id == postId) updatedPost else it }
                )
            }

            // Then update Firestore in the background
            blogRepository.toggleLike(postId, userId, currentlyLiked).onSuccess {
                // If user just liked the post (not unliked), create a notification
                if (!currentlyLiked && post.author?.uid != null && post.author.uid != userId) {
                    createLikeNotification(
                        recipientUserId = post.author.uid,
                        actorUserId = userId,
                        postId = postId,
                        postTitle = post.title
                    )
                }
            }.onFailure { exception ->
                // If it fails, revert the optimistic update
                _uiState.update { currentState ->
                    currentState.copy(
                        allPosts = currentState.allPosts.map { if (it.id == postId) post else it },
                        myPosts = currentState.myPosts.map { if (it.id == postId) post else it },
                        error = "Failed to update like: ${exception.message}"
                    )
                }
            }
        }
    }

    // Create a like notification
    private fun createLikeNotification(
        recipientUserId: String,
        actorUserId: String,
        postId: String,
        postTitle: String
    ) {
        viewModelScope.launch {
            try {
                // Get the actor's user profile
                val actorProfile = authRepository.getUserProfile(actorUserId).getOrNull()
                
                val notification = NotificationModel(
                    type = NotificationType.LIKE,
                    recipientUserId = recipientUserId,
                    actorUserId = actorUserId,
                    actorName = actorProfile?.name ?: "Someone",
                    actorProfileImage = actorProfile?.profileImageUrl,
                    postId = postId,
                    postTitle = postTitle,
                    message = "${actorProfile?.name ?: "Someone"} liked your post \"$postTitle\""
                )
                
                notificationRepository.createNotification(notification)
            } catch (e: Exception) {
                // Silently fail - notification creation shouldn't block the like action
            }
        }
    }
}
