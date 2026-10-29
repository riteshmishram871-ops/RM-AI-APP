package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreService(private val context: Context) {
    private val TAG = "FirestoreService"

    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        try {
            FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
        } catch (e: Exception) {
            Log.w(TAG, "Fallback to getInstance(dbId): ${e.message}")
            FirebaseFirestore.getInstance(dbId)
        }
    }

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    val currentUserId: String? get() = auth.currentUser?.uid

    suspend fun syncUserProfile(displayName: String, email: String, photoUrl: String = "") = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext
        val userDoc = db.collection("users").document(uid)
        val data = hashMapOf<String, Any>(
            "userId" to uid,
            "displayName" to displayName,
            "email" to email,
            "photoUrl" to photoUrl,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            userDoc.set(data, SetOptions.merge()).await()
            Log.d(TAG, "Synced user profile to Firestore: $uid")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync user profile", e)
        }
    }

    suspend fun syncConversation(id: String, title: String, isPinned: Boolean, messageCount: Int) = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext
        val convDoc = db.collection("users").document(uid).collection("conversations").document(id)
        val data = hashMapOf<String, Any>(
            "id" to id,
            "userId" to uid,
            "title" to title,
            "isPinned" to isPinned,
            "messageCount" to messageCount,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            convDoc.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync conversation", e)
        }
    }

    suspend fun syncMessage(id: String, conversationId: String, sender: String, text: String, imageUrl: String? = null) = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext
        val msgDoc = db.collection("users").document(uid)
            .collection("conversations").document(conversationId)
            .collection("messages").document(id)
        val data = hashMapOf<String, Any>(
            "id" to id,
            "conversationId" to conversationId,
            "userId" to uid,
            "sender" to sender,
            "text" to text,
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to "sent"
        )
        if (!imageUrl.isNullOrBlank()) {
            data["imageUrl"] = imageUrl
        }
        try {
            msgDoc.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync message", e)
        }
    }

    suspend fun syncReminder(id: String, title: String, dueTimestamp: Long, isCompleted: Boolean, priority: String) = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext
        val reminderDoc = db.collection("users").document(uid).collection("reminders").document(id)
        val data = hashMapOf<String, Any>(
            "id" to id,
            "userId" to uid,
            "title" to title,
            "dueTimestamp" to dueTimestamp,
            "isCompleted" to isCompleted,
            "priority" to priority,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            reminderDoc.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync reminder", e)
        }
    }

    suspend fun syncNote(id: String, title: String, content: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext
        val noteDoc = db.collection("users").document(uid).collection("notes").document(id)
        val data = hashMapOf<String, Any>(
            "id" to id,
            "userId" to uid,
            "title" to title,
            "content" to content,
            "isPinned" to isPinned,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            noteDoc.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync note", e)
        }
    }

    suspend fun syncMemory(id: String, key: String, value: String, category: String) = withContext(Dispatchers.IO) {
        val uid = currentUserId ?: return@withContext
        val memoryDoc = db.collection("users").document(uid).collection("memories").document(id)
        val data = hashMapOf<String, Any>(
            "id" to id,
            "userId" to uid,
            "key" to key,
            "value" to value,
            "category" to category,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            memoryDoc.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync memory", e)
        }
    }
}
