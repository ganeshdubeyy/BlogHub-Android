package com.example.bloghub.data.model

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationsCollection = firestore.collection("notifications")

    // Create a new notification
    suspend fun createNotification(notification: NotificationModel): Result<Unit> {
        return try {
            val docRef = notificationsCollection.document()
            val notificationWithId = notification.copy(id = docRef.id)
            docRef.set(notificationWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get notifications for a specific user (real-time)
    fun getUserNotifications(userId: String): Flow<List<NotificationModel>> = callbackFlow {
        val listener = notificationsCollection
            .whereEqualTo("recipientUserId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        NotificationModel(
                            id = doc.id,
                            type = NotificationType.valueOf(doc.getString("type") ?: "LIKE"),
                            recipientUserId = doc.getString("recipientUserId") ?: "",
                            actorUserId = doc.getString("actorUserId") ?: "",
                            actorName = doc.getString("actorName") ?: "",
                            actorProfileImage = doc.getString("actorProfileImage"),
                            postId = doc.getString("postId") ?: "",
                            postTitle = doc.getString("postTitle") ?: "",
                            message = doc.getString("message") ?: "",
                            isRead = doc.getBoolean("isRead") ?: false,
                            createdAt = doc.getTimestamp("createdAt") ?: com.google.firebase.Timestamp.now()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(notifications)
            }

        awaitClose { listener.remove() }
    }

    // Mark notification as read
    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mark all notifications as read for a user
    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("recipientUserId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete a notification
    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get unread count
    suspend fun getUnreadCount(userId: String): Result<Int> {
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("recipientUserId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
