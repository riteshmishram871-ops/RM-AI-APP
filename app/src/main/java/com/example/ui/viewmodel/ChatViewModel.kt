package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ConversationEntity
import com.example.data.local.MemoryEntity
import com.example.data.local.MessageEntity
import com.example.data.local.QuickNoteEntity
import com.example.data.local.ReminderEntity
import com.example.data.local.UserProfileEntity
import com.example.data.remote.FirestoreService
import com.example.data.repository.ChatRepository
import com.example.util.GoogleAuthManager
import com.example.util.NetworkMonitor
import com.example.util.SpeechRecognizerManager
import com.example.util.TextToSpeechManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Stack

enum class MainTab {
    HOME,
    CHAT,
    CREATE,
    TOOLS,
    MORE
}

enum class ScreenDestination {
    MAIN_TABS,
    VOICE_ASSISTANT,
    PHONE_CONTROL,
    CAMERA_VIEW,
    GALLERY_VIEW,
    MEMORY_VIEW,
    SETTINGS_VIEW,
    WEB_SEARCH_VIEW,
    CODE_ASSISTANT_VIEW,
    CALCULATOR_VIEW,
    QUICK_NOTES_VIEW,
    REMINDERS_VIEW,
    LANGUAGE_HELPER_VIEW
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application)
    private val networkMonitor = NetworkMonitor(application)
    private val firestoreService = FirestoreService(application)
    private val auth = FirebaseAuth.getInstance()

    val speechRecognizerManager = SpeechRecognizerManager(application)
    val ttsManager = TextToSpeechManager(application)

    // Firebase Auth State
    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    private val _isGuestSession = MutableStateFlow(false)
    val isGuestSession: StateFlow<Boolean> = _isGuestSession.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { fbAuth ->
        _firebaseUser.value = fbAuth.currentUser
        val user = fbAuth.currentUser
        if (user != null) {
            viewModelScope.launch {
                val displayName = user.displayName?.takeIf { it.isNotBlank() } ?: "User"
                val email = user.email ?: ""
                val photoUrl = user.photoUrl?.toString() ?: ""
                val current = userProfile.value ?: UserProfileEntity()
                repository.updateProfile(
                    current.copy(
                        displayName = displayName,
                        email = email,
                        isLoggedIn = true
                    )
                )
                firestoreService.syncUserProfile(displayName, email, photoUrl)
            }
        }
    }

    init {
        auth.addAuthStateListener(authListener)
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                firestoreService.syncUserProfile(
                    user.displayName?.takeIf { it.isNotBlank() } ?: "User",
                    user.email ?: "",
                    user.photoUrl?.toString() ?: ""
                )
            }
        }
    }

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val memories: StateFlow<List<MemoryEntity>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<QuickNoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation State
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _currentDestination = MutableStateFlow(ScreenDestination.MAIN_TABS)
    val currentDestination: StateFlow<ScreenDestination> = _currentDestination.asStateFlow()

    private val backStack = Stack<ScreenDestination>()

    // Chat State
    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedAttachmentUri = MutableStateFlow<String?>(null)
    val selectedAttachmentUri: StateFlow<String?> = _selectedAttachmentUri.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Voice to text states
    val isVoiceListening: StateFlow<Boolean> = speechRecognizerManager.isListening
    val voiceRecognizedText: StateFlow<String> = speechRecognizerManager.partialText
    val voiceRmsDb: StateFlow<Float> = speechRecognizerManager.rmsDb
    val voiceError: StateFlow<String?> = speechRecognizerManager.errorMessage

    private val _showVoiceSheet = MutableStateFlow(false)
    val showVoiceSheet: StateFlow<Boolean> = _showVoiceSheet.asStateFlow()

    val isSpeakingTts: StateFlow<Boolean> = ttsManager.isSpeaking

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

            // Pick latest conversation or create new
            val existing = repository.allConversations.firstOrNull()?.firstOrNull()
            if (existing != null) {
                _activeConversationId.value = existing.id
            } else {
                startNewChat()
            }

            // Seed initial memories and notes if empty
            seedInitialData()
        }
    }

    private suspend fun seedInitialData() {
        val existingMemories = repository.allMemories.firstOrNull()
        if (existingMemories.isNullOrEmpty()) {
            repository.addMemory("Preferred Language", "English & Romanized Odia", "PREFERENCE")
            repository.addMemory("Target Tech Stack", "Android Jetpack Compose & Python", "PROJECT")
            repository.addMemory("Name", "Ritesh", "FACT")
        }

        val existingNotes = repository.allNotes.firstOrNull()
        if (existingNotes.isNullOrEmpty()) {
            repository.saveNote("RM AI Project Vision", "Build the ultimate personal AI assistant with fast voice, multimodal tools, and cyber design.")
            repository.saveNote("Odia Phrases", "Bhai kemiti achha? (Brother how are you?), Dhanyabaad (Thank you).")
        }

        val existingReminders = repository.allReminders.firstOrNull()
        if (existingReminders.isNullOrEmpty()) {
            repository.addReminder("Review AI Image generator outputs", System.currentTimeMillis() + 7200000L, "HIGH")
            repository.addReminder("Daily Python coding practice", System.currentTimeMillis() + 18000000L, "MEDIUM")
        }
    }

    // Navigation actions
    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
        _currentDestination.value = ScreenDestination.MAIN_TABS
    }

    fun navigateTo(destination: ScreenDestination) {
        backStack.push(_currentDestination.value)
        _currentDestination.value = destination
    }

    fun navigateBack(): Boolean {
        if (_currentDestination.value != ScreenDestination.MAIN_TABS) {
            _currentDestination.value = if (backStack.isNotEmpty()) backStack.pop() else ScreenDestination.MAIN_TABS
            return true
        }
        return false
    }

    // Chat actions
    fun onInputTextChange(newText: String) {
        _inputText.value = newText
    }

    fun setAttachmentUri(uri: String?) {
        _selectedAttachmentUri.value = uri
    }

    fun selectConversation(id: String) {
        _activeConversationId.value = id
        _currentTab.value = MainTab.CHAT
        _currentDestination.value = ScreenDestination.MAIN_TABS
    }

    fun startNewChat() {
        viewModelScope.launch {
            val newConv = repository.createNewConversation("New Chat")
            _activeConversationId.value = newConv.id
        }
    }

    fun sendMessage(customPrompt: String? = null) {
        val promptToSend = customPrompt ?: _inputText.value
        val attachment = _selectedAttachmentUri.value
        if ((promptToSend.isBlank() && attachment == null) || _isGenerating.value) return

        val convId = _activeConversationId.value ?: return

        _inputText.value = ""
        _selectedAttachmentUri.value = null
        _isGenerating.value = true

        generatingJob = viewModelScope.launch {
            try {
                val result = repository.sendMessage(
                    conversationId = convId,
                    userPrompt = promptToSend.ifBlank { "What is in this image?" },
                    attachmentUri = attachment
                )
                // If TTS is desired in voice mode, speak response
                if (_currentDestination.value == ScreenDestination.VOICE_ASSISTANT) {
                    result.getOrNull()?.let { responseText ->
                        ttsManager.speak(responseText)
                    }
                }
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun stopGenerating() {
        generatingJob?.cancel()
        _isGenerating.value = false
        ttsManager.stop()
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

    fun clearAllConversations() {
        viewModelScope.launch {
            repository.deleteAllConversations()
            startNewChat()
        }
    }

    // Voice Input Controls
    fun setShowVoiceSheet(show: Boolean) {
        _showVoiceSheet.value = show
        if (!show && isVoiceListening.value) {
            speechRecognizerManager.stopListening()
        }
    }

    fun startVoiceInput() {
        _showVoiceSheet.value = true
        speechRecognizerManager.startListening()
    }

    fun stopVoiceInput() {
        speechRecognizerManager.stopListening()
    }

    fun onVoiceResultConfirmed(text: String, sendImmediately: Boolean) {
        speechRecognizerManager.stopListening()
        _showVoiceSheet.value = false
        if (text.isNotBlank()) {
            if (sendImmediately) {
                sendMessage(text)
            } else {
                val existing = _inputText.value.trim()
                _inputText.value = if (existing.isBlank()) text else "$existing $text"
            }
        }
    }

    // Text to Speech Controls
    fun speakText(text: String) {
        ttsManager.speak(text)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    // Memories management
    fun addMemory(title: String, content: String, category: String = "PREFERENCE") {
        viewModelScope.launch {
            repository.addMemory(title, content, category)
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun toggleMemory(id: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleMemoryEnabled(id, enabled)
        }
    }

    // Quick notes
    fun saveNote(title: String, content: String, id: String? = null) {
        viewModelScope.launch {
            repository.saveNote(title, content, id)
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    // Reminders
    fun addReminder(title: String, dueTime: Long, priority: String = "MEDIUM") {
        viewModelScope.launch {
            repository.addReminder(title, dueTime, priority)
        }
    }

    fun toggleReminder(id: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleReminder(id, isCompleted)
        }
    }

    fun deleteReminder(id: String) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    // Profile Settings
    fun updateProfile(
        displayName: String,
        email: String,
        avatarColorHex: String,
        themeMode: String,
        aiPersona: String,
        temperature: Float,
        customApiKey: String,
        selectedLanguage: String = "English (India)",
        isMemoryEnabled: Boolean = true
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
                    customApiKey = customApiKey.trim(),
                    selectedLanguage = selectedLanguage,
                    isMemoryEnabled = isMemoryEnabled
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

    fun initiateGoogleSignIn(activity: Activity) {
        viewModelScope.launch {
            _isSigningIn.value = true
            _authError.value = null
            val result = GoogleAuthManager.signInWithGoogle(activity)
            _isSigningIn.value = false
            result.onSuccess { user ->
                _authError.value = null
                val displayName = user.displayName?.takeIf { it.isNotBlank() } ?: "User"
                val email = user.email ?: ""
                val photoUrl = user.photoUrl?.toString() ?: ""
                val current = userProfile.value ?: UserProfileEntity()
                repository.updateProfile(
                    current.copy(
                        displayName = displayName,
                        email = email,
                        isLoggedIn = true
                    )
                )
                firestoreService.syncUserProfile(displayName, email, photoUrl)
            }.onFailure { error ->
                if (error is androidx.credentials.exceptions.GetCredentialCancellationException) {
                    _authError.value = "Sign-in was cancelled. Please tap to try again."
                } else {
                    _authError.value = "Sign-in error: ${error.localizedMessage ?: "Authentication failed"}"
                }
            }
        }
    }

    fun continueAsGuest() {
        _isGuestSession.value = true
        signIn("Guest User", "guest@rmai.local")
    }

    fun clearAuthError() {
        _authError.value = null
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
        }
    }

    fun signOut() {
        viewModelScope.launch {
            GoogleAuthManager.signOut()
            _isGuestSession.value = false
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateProfile(current.copy(isLoggedIn = false))
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
        speechRecognizerManager.destroy()
        ttsManager.shutdown()
    }
}
