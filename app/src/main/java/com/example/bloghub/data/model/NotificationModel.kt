package com.example.bloghub.data.model

import com.google.firebase.Timestamp

enum class NotificationType {
    LIKE,
    COMMENT,
    FOLLOW,
    MENTION
}

data class NotificationModel(
    val id: String = "",
    val type: NotificationType = NotificationType.LIKE,
    val recipientUserId: String = "", // User who receives the notification
    val actorUserId: String = "", // User who performed the action
    val actorName: String = "", // Name of the user who performed the action
    val actorProfileImage: String? = null, // Profile image of the actor
    val postId: String = "", // Related blog post ID
    val postTitle: String = "", // Title of the blog post
    val message: String = "", // Notification message
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)
