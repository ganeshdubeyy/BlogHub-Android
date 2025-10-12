// app/src/main/java/com/example/bloghub/auth/UserModel.kt
package com.example.bloghub.auth

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserModel(
	val uid: String = "",
	val name: String = "",
	val email: String = "",
	val bio: String = "",
	val profileImageUrl: String = "",
	val socialLinks: Map<String, String> = emptyMap(),
	@ServerTimestamp val createdAt: Date? = null // Use Date with annotation for Firestore
)
