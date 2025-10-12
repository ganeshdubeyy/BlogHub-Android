package com.example.bloghub.data

import com.example.bloghub.auth.UserModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

// Predefined blog categories
enum class BlogCategory(val displayName: String) {
    TECHNOLOGY("Technology"),
    LIFESTYLE("Lifestyle"),
    ROMANCE("Romance"),
    TRAVEL("Travel"),
    FOOD("Food"),
    HEALTH("Health"),
    BUSINESS("Business"),
    EDUCATION("Education"),
    ENTERTAINMENT("Entertainment"),
    SPORTS("Sports"),
    GENERAL("General");

    companion object {
        fun fromString(value: String): BlogCategory {
            return values().find { it.name == value } ?: GENERAL
        }
    }
}

// DATA MODEL FOR A BLOG POST
data class BlogModel(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val author: UserModel? = null, // Nested author information
    val createdAt: Timestamp = Timestamp.now(),
    val imageUrl: String? = null, // Optional image for the blog post
    val likedBy: List<String> = emptyList(), // List of user IDs who liked this post
    val category: String = BlogCategory.GENERAL.name // Category of the blog post
) {
    // Helper property to get like count
    val likeCount: Int
        get() = likedBy.size
    
    // Helper function to check if a user has liked this post
    fun isLikedBy(userId: String): Boolean = likedBy.contains(userId)
    
    // Helper property to get category enum
    val categoryEnum: BlogCategory
        get() = BlogCategory.fromString(category)
}

// REPOSITORY TO MANAGE BLOGS IN FIRESTORE
class BlogRepository {
    private val db = FirebaseFirestore.getInstance()
    private val blogsCollection = db.collection("blogs")

    // --- CREATE ---
    // Creates a new blog post in Firestore
    suspend fun createPost(post: BlogModel): Result<Unit> {
        return try {
            // Firestore will auto-generate an ID
            blogsCollection.add(post).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- READ (ALL) ---
    // Fetches all blog posts from all users, ordered by newest first
    suspend fun getAllPosts(): Result<List<BlogModel>> {
        return try {
            val snapshot = blogsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            // Convert each document to a BlogModel object
            val posts = snapshot.documents.mapNotNull { document ->
                document.toObject<BlogModel>()?.copy(id = document.id)
            }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- NEW: READ (BY USER) ---
    // Fetches all blog posts created by a specific user, ordered by newest first
    suspend fun getPostsByUserId(userId: String): Result<List<BlogModel>> {
        return try {
            val snapshot = blogsCollection
                // Filter posts where the 'author.userId' field matches the provided userId
                .whereEqualTo("author.uid", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val posts = snapshot.documents.mapNotNull { document ->
                document.toObject<BlogModel>()?.copy(id = document.id)
            }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- UPDATE ---
    // Updates a post's title, content, category, and optionally its image URL
    suspend fun updatePost(postId: String, title: String, content: String, category: String, newImageUrl: String?): Result<Unit> {
        return try {
            // Use a mutable map to build the update object
            val updates = mutableMapOf<String, Any>(
                "title" to title,
                "content" to content,
                "category" to category
            )

            // Only add the imageUrl to the map if a new one was provided.
            if (newImageUrl != null) {
                updates["imageUrl"] = newImageUrl
            }

            blogsCollection.document(postId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- DELETE ---
    // Deletes a post from Firestore
    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            blogsCollection.document(postId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- LIKE ---
    // Adds a user to the likedBy list
    suspend fun likePost(postId: String, userId: String): Result<Unit> {
        return try {
            val docRef = blogsCollection.document(postId)
            val snapshot = docRef.get().await()
            
            if (snapshot.exists()) {
                val currentLikes = snapshot.get("likedBy") as? List<*> ?: emptyList<String>()
                val likedByList = currentLikes.filterIsInstance<String>().toMutableList()
                
                // Add user if not already in the list
                if (!likedByList.contains(userId)) {
                    likedByList.add(userId)
                    docRef.update("likedBy", likedByList).await()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- UNLIKE ---
    // Removes a user from the likedBy list
    suspend fun unlikePost(postId: String, userId: String): Result<Unit> {
        return try {
            val docRef = blogsCollection.document(postId)
            val snapshot = docRef.get().await()
            
            if (snapshot.exists()) {
                val currentLikes = snapshot.get("likedBy") as? List<*> ?: emptyList<String>()
                val likedByList = currentLikes.filterIsInstance<String>().toMutableList()
                
                // Remove user if in the list
                if (likedByList.contains(userId)) {
                    likedByList.remove(userId)
                    docRef.update("likedBy", likedByList).await()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- TOGGLE LIKE ---
    // Convenience function to toggle like status
    suspend fun toggleLike(postId: String, userId: String, currentlyLiked: Boolean): Result<Unit> {
        return if (currentlyLiked) {
            unlikePost(postId, userId)
        } else {
            likePost(postId, userId)
        }
    }
}
