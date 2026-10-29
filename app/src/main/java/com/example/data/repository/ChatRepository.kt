package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MemoryEntity
import com.example.data.local.MessageEntity
import com.example.data.local.QuickNoteEntity
import com.example.data.local.ReminderEntity
import com.example.data.local.UserProfileEntity
import com.example.data.remote.GeminiApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    private val profileDao = database.userProfileDao()
    private val memoryDao = database.memoryDao()
    private val noteDao = database.quickNoteDao()
    private val reminderDao = database.reminderDao()

    val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()
    val userProfile: Flow<UserProfileEntity?> = profileDao.getProfileFlow()
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()
    val allNotes: Flow<List<QuickNoteEntity>> = noteDao.getAllNotes()
    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    suspend fun getOrCreateProfile(): UserProfileEntity = withContext(Dispatchers.IO) {
        val existing = profileDao.getProfile()
        if (existing != null) return@withContext existing
        val defaultProfile = UserProfileEntity()
        profileDao.insertOrUpdate(defaultProfile)
        defaultProfile
    }

    suspend fun updateProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        profileDao.insertOrUpdate(profile)
    }

    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    suspend fun createNewConversation(title: String = "New Chat"): ConversationEntity = withContext(Dispatchers.IO) {
        val newConv = ConversationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        conversationDao.insertOrUpdate(newConv)
        newConv
    }

    suspend fun updateConversationTitle(id: String, newTitle: String) = withContext(Dispatchers.IO) {
        conversationDao.updateTitle(id, newTitle)
    }

    suspend fun togglePinConversation(id: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        conversationDao.setPinned(id, isPinned)
    }

    suspend fun deleteConversation(id: String) = withContext(Dispatchers.IO) {
        conversationDao.deleteById(id)
    }

    suspend fun deleteAllConversations() = withContext(Dispatchers.IO) {
        conversationDao.deleteAll()
    }

    suspend fun deleteMessage(messageId: String) = withContext(Dispatchers.IO) {
        messageDao.deleteById(messageId)
    }

    // Memories
    suspend fun addMemory(title: String, content: String, category: String = "PREFERENCE") = withContext(Dispatchers.IO) {
        memoryDao.insert(
            MemoryEntity(
                id = UUID.randomUUID().toString(),
                category = category,
                title = title,
                content = content
            )
        )
    }

    suspend fun deleteMemory(id: String) = withContext(Dispatchers.IO) {
        memoryDao.deleteById(id)
    }

    suspend fun toggleMemoryEnabled(id: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        memoryDao.setEnabled(id, enabled)
    }

    suspend fun clearAllMemories() = withContext(Dispatchers.IO) {
        memoryDao.deleteAll()
    }

    // Quick Notes
    suspend fun saveNote(title: String, content: String, id: String? = null) = withContext(Dispatchers.IO) {
        noteDao.insertOrUpdate(
            QuickNoteEntity(
                id = id ?: UUID.randomUUID().toString(),
                title = title.ifBlank { "Untitled Note" },
                content = content,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteNote(id: String) = withContext(Dispatchers.IO) {
        noteDao.deleteById(id)
    }

    // Reminders
    suspend fun addReminder(title: String, dueTime: Long, priority: String = "MEDIUM") = withContext(Dispatchers.IO) {
        reminderDao.insertOrUpdate(
            ReminderEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                dueTime = dueTime,
                priority = priority,
                isCompleted = false
            )
        )
    }

    suspend fun toggleReminder(id: String, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        reminderDao.setCompleted(id, isCompleted)
    }

    suspend fun deleteReminder(id: String) = withContext(Dispatchers.IO) {
        reminderDao.deleteById(id)
    }

    /**
     * Sends message with optional image attachment
     */
    suspend fun sendMessage(
        conversationId: String,
        userPrompt: String,
        attachmentUri: String? = null,
        onChunk: (String) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        var conv = conversationDao.getConversationById(conversationId)
        if (conv == null) {
            conv = ConversationEntity(
                id = conversationId,
                title = generateTitleFromPrompt(userPrompt),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            conversationDao.insertOrUpdate(conv)
        } else if (conv.title == "New Chat" || conv.title.isBlank()) {
            conversationDao.updateTitle(conv.id, generateTitleFromPrompt(userPrompt))
        }

        // Insert user message
        val userMsgId = UUID.randomUUID().toString()
        val userMsg = MessageEntity(
            id = userMsgId,
            conversationId = conversationId,
            role = "user",
            content = userPrompt.trim(),
            createdAt = System.currentTimeMillis(),
            attachmentUri = attachmentUri
        )
        messageDao.insert(userMsg)

        // Decode attachment if any
        val bitmap = attachmentUri?.let { decodeUriToBitmap(it) }

        // Fetch profile
        val profile = getOrCreateProfile()
        val systemPrompt = getSystemPromptForPersona(profile.aiPersona, profile.selectedLanguage)

        // History
        val pastMessages = messageDao.getMessagesList(conversationId)
        val historyPairs = pastMessages.map { it.role to it.content }

        // Placeholder assistant message
        val assistantMsgId = UUID.randomUUID().toString()
        val initialAssistantMsg = MessageEntity(
            id = assistantMsgId,
            conversationId = conversationId,
            role = "assistant",
            content = "",
            createdAt = System.currentTimeMillis() + 1,
            isStreaming = true
        )
        messageDao.insert(initialAssistantMsg)

        val aiResult = GeminiApiClient.generateChatResponse(
            messages = historyPairs,
            systemPrompt = systemPrompt,
            temperature = profile.temperature,
            customApiKey = profile.customApiKey.takeIf { it.isNotBlank() },
            attachedBitmap = bitmap
        )

        return@withContext if (aiResult.isSuccess) {
            val fullText = aiResult.getOrNull() ?: ""
            val chunks = splitIntoStreamChunks(fullText)
            val currentBuilder = StringBuilder()

            for (chunk in chunks) {
                currentBuilder.append(chunk)
                val partial = currentBuilder.toString()
                onChunk(partial)
                messageDao.updateContent(assistantMsgId, partial, isStreaming = true)
                delay(14)
            }

            messageDao.updateContent(assistantMsgId, fullText, isStreaming = false)
            conversationDao.updateTitle(conversationId, conv.title, System.currentTimeMillis())
            Result.success(fullText)
        } else {
            val errorMsg = "⚠️ I encountered an error. Please tap retry."
            messageDao.updateContent(assistantMsgId, errorMsg, isStreaming = false)
            Result.failure(aiResult.exceptionOrNull() ?: Exception("Unknown error"))
        }
    }

    private fun decodeUriToBitmap(uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun generateTitleFromPrompt(prompt: String): String {
        val words = prompt.trim().split("\\s+".toRegex())
        val titleWords = words.take(5).joinToString(" ")
        return if (titleWords.length > 32) {
            titleWords.take(30).trimEnd() + "…"
        } else if (words.size > 5) {
            "$titleWords…"
        } else {
            titleWords.ifBlank { "New Chat" }
        }
    }

    private fun splitIntoStreamChunks(text: String): List<String> {
        val result = mutableListOf<String>()
        var index = 0
        while (index < text.length) {
            val chunkSize = (4..8).random().coerceAtMost(text.length - index)
            result.add(text.substring(index, index + chunkSize))
            index += chunkSize
        }
        return result
    }

    private fun getSystemPromptForPersona(persona: String, language: String): String {
        val langInstruction = "Preferred Language: $language. You understand English, Hindi, and Romanized Odia fluently."
        return when (persona) {
            "CODER" -> """You are RM AI, an expert Senior Software Engineer and Architect.
$langInstruction
Provide clean, idiomatic code with syntax highlighting in C++, Python, Java, Kotlin, or JavaScript.
Keep explanations concise, secure, and focused on best practices."""

            "WRITER" -> """You are RM AI, a creative writer and storytelling strategist.
$langInstruction
Write engaging, eloquent, and crisp copy with expressive flair."""

            "TUTOR" -> """You are RM AI, an intuitive and patient academic tutor.
$langInstruction
Break down complex topics step-by-step with clear analogies and examples."""

            else -> """You are RM AI, an intelligent, friendly, and responsive personal AI assistant.
$langInstruction
Answer clearly and accurately. Format responses with clean Markdown, bullet points, and code blocks with language tags when appropriate.
Answer in Romanized Odia when the user addresses you in Odia (e.g. 'Bhai, kemiti karibi?'). Be helpful, respectful, and sharp."""
        }
    }
}
