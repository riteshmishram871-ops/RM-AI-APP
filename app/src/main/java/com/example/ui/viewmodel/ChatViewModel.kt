package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserProfileEntity
import com.example.data.repository.ChatRepository
import com.example.util.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    CHAT,
    SETTINGS,
    AUTH
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application)
    private val networkMonitor = NetworkMonitor(application)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.CHAT)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _showProfileBottomSheet = MutableStateFlow(false)
    val showProfileBottomSheet: StateFlow<Boolean> = _showProfileBottomSheet.asStateFlow()

    private var generatingJob: Job? = null

    // Messages for active conversation
    val messages: StateFlow<List<MessageEntity>> = _activeConversationId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getMessagesForConversation(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val profile = repository.getOrCreateProfile()
            if (!profile.isLoggedIn) {
                _currentScreen.value = AppScreen.AUTH
            }

            // Pick latest conversation or create new
            val existing = repository.allConversations.firstOrNull()?.firstOrNull()
            if (existing != null) {
                _activeConversationId.value = existing.id
            } else {
                startNewChat()
            }
        }
    }

    fun onInputTextChange(newText: String) {
        _inputText.value = newText
    }

    fun selectConversation(id: String) {
        _activeConversationId.value = id
    }

    fun startNewChat() {
        viewModelScope.launch {
            val newConv = repository.createNewConversation("New Chat")
            _activeConversationId.value = newConv.id
        }
    }

    fun sendMessage(customPrompt: String? = null) {
        val promptToSend = customPrompt ?: _inputText.value
        if (promptToSend.isBlank() || _isGenerating.value) return

        val convId = _activeConversationId.value ?: return

        _inputText.value = ""
        _isGenerating.value = true

        generatingJob = viewModelScope.launch {
            try {
                repository.sendMessage(
                    conversationId = convId,
                    userPrompt = promptToSend
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun stopGenerating() {
        generatingJob?.cancel()
        _isGenerating.value = false
    }

    fun regenerateLastMessage() {
        val currentMsgs = messages.value
        val lastUserMessage = currentMsgs.lastOrNull { it.role == "user" }?.content ?: return
        sendMessage(lastUserMessage)
    }

    fun renameConversation(id: String, newTitle: String) {
        viewModelScope.launch {
            repository.updateConversationTitle(id, newTitle)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_activeConversationId.value == id) {
                val remaining = conversations.value.filter { it.id != id }
                if (remaining.isNotEmpty()) {
                    _activeConversationId.value = remaining.first().id
                } else {
                    startNewChat()
                }
            }
        }
    }

    fun togglePin(id: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinConversation(id, isPinned)
        }
    }

    fun updateProfile(
        displayName: String,
        email: String,
        avatarColorHex: String,
        themeMode: String,
        aiPersona: String,
        temperature: Float,
        customApiKey: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateProfile(
                current.copy(
                    displayName = displayName.trim(),
                    email = email.trim(),
                    avatarColorHex = avatarColorHex,
                    themeMode = themeMode,
                    aiPersona = aiPersona,
                    temperature = temperature,
                    customApiKey = customApiKey.trim()
                )
            )
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            val newTheme = if (current.themeMode == "DARK") "LIGHT" else "DARK"
            repository.updateProfile(current.copy(themeMode = newTheme))
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            repository.deleteAllConversations()
            startNewChat()
        }
    }

    fun setProfileBottomSheetVisible(visible: Boolean) {
        _showProfileBottomSheet.value = visible
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun signIn(displayName: String, email: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateProfile(
                current.copy(
                    displayName = displayName,
                    email = email,
                    isLoggedIn = true
                )
            )
            _currentScreen.value = AppScreen.CHAT
        }
    }

    fun signOut() {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateProfile(current.copy(isLoggedIn = false))
            _currentScreen.value = AppScreen.AUTH
        }
    }
}
