package com.example.bloghub.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepository(
	private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
	private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

	suspend fun signUpWithEmail(name: String, email: String, password: String): Result<UserModel> {
		return try {
			val user = auth.createUserWithEmailAndPassword(email, password).await().user
			if (user == null) return Result.failure(IllegalStateException("User is null after sign up"))
			val uid = user.uid
			val profile = mapOf(
				"name" to name,
				"email" to email,
				"bio" to "",
				"profileImageUrl" to "",
				"socialLinks" to emptyMap<String, String>(),
				"createdAt" to FieldValue.serverTimestamp()
			)
			firestore.collection("users").document(uid).set(profile).await()
			Result.success(UserModel(uid = uid, name = name, email = email))
		} catch (t: Throwable) {
			Result.failure(t)
		}
	}

	suspend fun loginWithEmail(email: String, password: String): Result<UserModel> {
		return try {
			val user = auth.signInWithEmailAndPassword(email, password).await().user
			if (user == null) return Result.failure(IllegalStateException("User is null after login"))
			// Try to read name from Firestore; fallback to email local part
			val doc = firestore.collection("users").document(user.uid).get().await()
			val name = doc.getString("name") ?: email.substringBefore('@')
			Result.success(
				UserModel(
					uid = user.uid,
					name = name,
					email = user.email ?: email,
					bio = doc.getString("bio") ?: "",
					profileImageUrl = doc.getString("profileImageUrl") ?: "",
					socialLinks = (doc.get("socialLinks") as? Map<String, String>) ?: emptyMap(),
					createdAt = doc.getTimestamp("createdAt")?.toDate()
				)
			)
		} catch (t: Throwable) {
			Result.failure(t)
		}
	}

	// --- THIS IS THE CORRECTED FUNCTION ---
	suspend fun getUserProfile(userId: String): Result<UserModel?> {
		return try {
			val document = firestore.collection("users").document(userId).get().await()
			if (document.exists()) {
				val user = document.toObject(UserModel::class.java)

				// THE FIX: The object from toObject() does not include the document's ID.
				// We MUST manually copy it into the 'uid' field before returning.
				Result.success(user?.copy(uid = document.id))
			} else {
				Result.failure(Exception("User profile not found in Firestore for ID: $userId"))
			}
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	fun firebaseAuthCurrentUser(): FirebaseUser? = auth.currentUser

	fun signOut() {
		auth.signOut()
	}

	fun firebaseUserFlow(): Flow<FirebaseUser?> = callbackFlow {
		val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
			trySend(firebaseAuth.currentUser)
		}
		auth.addAuthStateListener(listener)
		awaitClose { auth.removeAuthStateListener(listener) }
	}

	suspend fun loginWithGoogle(idToken: String): Result<UserModel> {
		return try {
			val credential = GoogleAuthProvider.getCredential(idToken, null)
			val user = auth.signInWithCredential(credential).await().user
			if (user == null) return Result.failure(IllegalStateException("User is null after Google login"))
			// Ensure user profile exists
			val uid = user.uid
			val docRef = firestore.collection("users").document(uid)
			val snapshot = docRef.get().await()
			if (!snapshot.exists()) {
				val profile = mapOf(
					"name" to (user.displayName ?: ""),
					"email" to (user.email ?: ""),
					"bio" to "",
					"profileImageUrl" to (user.photoUrl?.toString() ?: ""),
					"socialLinks" to emptyMap<String, String>(),
					"createdAt" to FieldValue.serverTimestamp()
				)
				docRef.set(profile).await()
			}
			Result.success(
				UserModel(
					uid = uid,
					name = user.displayName ?: "",
					email = user.email ?: ""
				)
			)
		} catch (t: Throwable) {
			Result.failure(t)
		}
	}
}

// await() helper for Tasks
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
	addOnCompleteListener { task ->
		if (task.isSuccessful) {
			cont.resume(task.result)
		} else {
			cont.resumeWithException(task.exception ?: RuntimeException("Task failed"))
		}
	}
	// No direct cancellation for Task
}
