package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val systemPrompt: String = ""
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val role: String, // "user", "assistant", "error"
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val attachmentUri: String? = null
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: String = "primary_user",
    val displayName: String = "Ritesh",
    val email: String = "ritesh.rm871@gmail.com",
    val avatarColorHex: String = "#00E5A3",
    val themeMode: String = "DARK", // "DARK", "LIGHT", "SYSTEM"
    val aiPersona: String = "HELPFUL", // "HELPFUL", "CODER", "WRITER", "TUTOR"
    val temperature: Float = 0.7f,
    val customApiKey: String = "",
    val isLoggedIn: Boolean = true,
    val selectedLanguage: String = "English (India)", // English, Odia, Hindi, etc.
    val isMemoryEnabled: Boolean = true,
    val ttsSpeechRate: Float = 1.0f
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey
    val id: String,
    val category: String, // "PREFERENCE", "PROJECT", "FACT"
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isEnabled: Boolean = true
)

@Entity(tableName = "quick_notes")
data class QuickNoteEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val dueTime: Long = System.currentTimeMillis() + 3600000L,
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val isCompleted: Boolean = false
)
