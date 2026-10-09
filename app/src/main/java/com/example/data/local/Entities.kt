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
    val isStreaming: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: String = "primary_user",
    val displayName: String = "Ritesh Mishra",
    val email: String = "ritesh.rm@example.com",
    val avatarColorHex: String = "#4F46E5",
    val themeMode: String = "DARK", // "DARK", "LIGHT", "SYSTEM"
    val aiPersona: String = "HELPFUL", // "HELPFUL", "CODER", "WRITER", "TUTOR"
    val temperature: Float = 0.7f,
    val customApiKey: String = "",
    val isLoggedIn: Boolean = true
)
