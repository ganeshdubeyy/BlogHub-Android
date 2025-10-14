package com.example.bloghub.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.bloghub.MainActivity
import com.example.bloghub.R

object NotificationHelper {
    
    private const val TAG = "NotificationHelper"
    
    // Notification Channel IDs
    private const val CHANNEL_ID_BLOG = "blog_notifications"
    private const val CHANNEL_ID_LIKES = "like_notifications"
    
    // Notification IDs
    private const val NOTIFICATION_ID_BLOG = 1001
    private const val NOTIFICATION_ID_LIKE = 1002
    
    /**
     * Creates notification channels (required for Android 8.0+)
     * Call this once when the app starts
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel for blog post notifications
            val blogChannel = NotificationChannel(
                CHANNEL_ID_BLOG,
                "Blog Posts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for blog post actions"
            }
            
            // Channel for like notifications
            val likeChannel = NotificationChannel(
                CHANNEL_ID_LIKES,
                "Likes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when someone likes your post"
            }
            
            // Register channels with the system
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(blogChannel)
            notificationManager.createNotificationChannel(likeChannel)
        }
    }
    
    /**
     * Shows a notification when a blog post is successfully published
     */
    fun showBlogPublishedNotification(context: Context, postTitle: String) {
        Log.d(TAG, "Attempting to show blog published notification for: $postTitle")
        // Create an intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_BLOG)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using Android system icon
            .setContentTitle("Blog Published! 🎉")
            .setContentText("Your post \"$postTitle\" is now live")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your post \"$postTitle\" has been successfully published and is now visible to all users."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss when tapped
            .build()
        
        // Show the notification
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BLOG, notification)
            Log.d(TAG, "Blog published notification shown successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission denied", e)
        }
    }
    
    /**
     * Shows a notification when someone likes your blog post
     */
    fun showLikeNotification(context: Context, likerName: String, postTitle: String) {
        Log.d(TAG, "Attempting to show like notification from $likerName for: $postTitle")
        // Create an intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_LIKES)
            .setSmallIcon(android.R.drawable.star_on) // Using Android system icon for likes
            .setContentTitle("New Like! ❤️")
            .setContentText("$likerName liked your post \"$postTitle\"")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$likerName liked your post \"$postTitle\". Keep creating great content!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Show the notification
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_LIKE, notification)
            Log.d(TAG, "Like notification shown successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission denied", e)
        }
    }
    
    /**
     * Checks if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true // Permission not required for older versions
        }
    }
}
