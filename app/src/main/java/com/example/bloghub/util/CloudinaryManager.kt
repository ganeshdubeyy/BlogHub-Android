// This package name now correctly matches your folder path: .../bloghub/util/
package com.example.bloghub.util

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.bloghub.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object CloudinaryManager {

    private var isInitialized = false

    /**
     * Initializes the Cloudinary MediaManager with the credentials from BuildConfig.
     * This should be called once, ideally in the Application class.
     */
    fun init(context: Context) {
        // Prevent re-initialization
        if (isInitialized) return

        try {
            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
            )
            MediaManager.init(context, config)
            isInitialized = true
        } catch (e: Exception) {
            // In a real app, you might want to log this error to a crash reporting tool
            e.printStackTrace()
        }
    }

    /**
     * Uploads an image from a given URI to Cloudinary.
     * This is a suspend function, so it must be called from a coroutine.
     * @return The secure URL of the uploaded image, or null if the upload fails.
     */
    suspend fun uploadImage(imageUri: Uri): String? {
        return suspendCancellableCoroutine { continuation ->
            // Use .unsigned() for client-side uploads without a server-side signature.
            // This is crucial for security and functionality.
            val requestId = MediaManager.get().upload(imageUri)
                // ▼▼▼ IMPORTANT: Replace this string with your actual preset name from the Cloudinary dashboard! ▼▼▼
                .unsigned("appdefault")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        // Optional: Log that the upload has started
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        // Optional: Calculate and show upload progress if needed
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"] as? String
                        // Resume the coroutine with the successful URL
                        if (continuation.isActive) {
                            continuation.resume(secureUrl)
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        // Resume the coroutine with null to indicate failure
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        // Called when the upload is automatically retried
                    }
                }).dispatch()

            // If the coroutine is cancelled, cancel the ongoing upload request
            continuation.invokeOnCancellation {
                MediaManager.get().cancelRequest(requestId)
            }
        }
    }
}
