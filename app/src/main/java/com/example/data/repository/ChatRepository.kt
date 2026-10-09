package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserProfileEntity
import com.example.data.remote.GeminiApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    private val profileDao = database.userProfileDao()

    val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()
    val userProfile: Flow<UserProfileEntity?> = profileDao.getProfileFlow()

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

    /**
     * Sends a message to the AI, handles streaming response animation and updates Room.
     */
    suspend fun sendMessage(
        conversationId: String,
        userPrompt: String,
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

        // 1. Insert user message
        val userMsgId = UUID.randomUUID().toString()
        val userMsg = MessageEntity(
            id = userMsgId,
            conversationId = conversationId,
            role = "user",
            content = userPrompt.trim(),
            createdAt = System.currentTimeMillis()
        )
        messageDao.insert(userMsg)

        // 2. Fetch profile configuration
        val profile = getOrCreateProfile()
        val systemPrompt = getSystemPromptForPersona(profile.aiPersona)

        // 3. Prepare previous conversation turns
        val pastMessages = messageDao.getMessagesList(conversationId)
        val historyPairs = pastMessages.map { it.role to it.content }

        // 4. Insert temporary assistant message with isStreaming = true
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

        // 5. Call AI Client
        val aiResult = GeminiApiClient.generateChatResponse(
            messages = historyPairs,
            systemPrompt = systemPrompt,
            temperature = profile.temperature,
            customApiKey = profile.customApiKey.takeIf { it.isNotBlank() }
        )

        return@withContext if (aiResult.isSuccess) {
            val fullText = aiResult.getOrNull() ?: ""

            // Stream simulation to deliver the signature smooth conversational typing feel
            val chunks = splitIntoStreamChunks(fullText)
            val currentBuilder = StringBuilder()

            for (chunk in chunks) {
                currentBuilder.append(chunk)
                val partial = currentBuilder.toString()
                onChunk(partial)
                messageDao.updateContent(assistantMsgId, partial, isStreaming = true)
                delay(18) // smooth reading pacing
            }

            // Finalize message
            messageDao.updateContent(assistantMsgId, fullText, isStreaming = false)
            conversationDao.updateTitle(conversationId, conv.title, System.currentTimeMillis())
            Result.success(fullText)
        } else {
            val errorMsg = "⚠️ I encountered an error: ${aiResult.exceptionOrNull()?.localizedMessage ?: "Unknown issue"}. Tap retry below to attempt again."
            messageDao.updateContent(assistantMsgId, errorMsg, isStreaming = false)
            Result.failure(aiResult.exceptionOrNull() ?: Exception("Unknown error"))
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
            // chunk size between 2 and 6 characters
            val chunkSize = (3..7).random().coerceAtMost(text.length - index)
            result.add(text.substring(index, index + chunkSize))
            index += chunkSize
        }
        return result
    }

    private fun getSystemPromptForPersona(persona: String): String {
        return when (persona) {
            "CODER" -> """You are RM AI, a senior software engineer and systems architect.
Provide clean, idiomatic, and bug-free code blocks with syntax highlighting.
Always explain design choices clearly, prioritize security and edge cases, and keep comments helpful and concise."""

            "WRITER" -> """You are RM AI, an expert copywriter, creative writer, and editorial strategist.
Craft engaging, elegant, and punchy text with rich tone variations, persuasive hooks, and clean typography."""

            "TUTOR" -> """You are RM AI, a patient, world-class educator and tutor.
Break down complex topics using intuitive analogies, step-by-step reasoning, and friendly follow-up questions to test comprehension."""

            else -> """You are RM AI, an intelligent, helpful, and friendly AI assistant.
Answer clearly and accurately. Format responses with clean Markdown, bullet points, and code blocks with language tags when appropriate.
If you are unsure of any facts, state so instead of fabricating information. Keep your tone professional, responsive, and approachable."""
        }
    }
}
