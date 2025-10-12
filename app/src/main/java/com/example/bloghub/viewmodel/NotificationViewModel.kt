package com.example.bloghub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloghub.data.model.NotificationModel
import com.example.bloghub.data.model.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<NotificationModel> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class NotificationViewModel : ViewModel() {
    private val repository = NotificationRepository()

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    // Load notifications for a user
    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.getUserNotifications(userId).collect { notifications ->
                    val unreadCount = notifications.count { !it.isRead }
                    _uiState.update {
                        it.copy(
                            notifications = notifications,
                            unreadCount = unreadCount,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load notifications",
                        isLoading = false
                    )
                }
            }
        }
    }

    // Mark a notification as read
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
        }
    }

    // Mark all notifications as read
    fun markAllAsRead(userId: String) {
        viewModelScope.launch {
            repository.markAllAsRead(userId)
        }
    }

    // Delete a notification
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            repository.deleteNotification(notificationId)
        }
    }

    // Create a notification (called when someone likes a post)
    fun createLikeNotification(
        recipientUserId: String,
        actorUserId: String,
        actorName: String,
        actorProfileImage: String?,
        postId: String,
        postTitle: String
    ) {
        // Don't create notification if user likes their own post
        if (recipientUserId == actorUserId) return

        viewModelScope.launch {
            val notification = NotificationModel(
                type = com.example.bloghub.data.model.NotificationType.LIKE,
                recipientUserId = recipientUserId,
                actorUserId = actorUserId,
                actorName = actorName,
                actorProfileImage = actorProfileImage,
                postId = postId,
                postTitle = postTitle,
                message = "$actorName liked your post \"$postTitle\""
            )
            repository.createNotification(notification)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
