// app/src/main/java/com/example/bloghub/data/UserRepository.kt
package com.example.bloghub.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.example.bloghub.auth.UserModel
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    /**
     * Fetches a user's profile from Firestore.
     */
    suspend fun getUserProfile(uid: String): Result<UserModel> {
        return try {
            val document = usersCollection.document(uid).get().await()
            if (document != null && document.exists()) {
                val user = document.toObject(UserModel::class.java)!!
                Result.success(user.copy(uid = uid)) // Ensure UID is set
            } else {
                Result.failure(Exception("User profile not found."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates a user's profile in Firestore.
     * We pass a map to allow updating only specific fields.
     */
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
