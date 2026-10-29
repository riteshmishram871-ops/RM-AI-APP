package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.ConversationEntity
import com.example.ui.components.ChatMessageBubble
import com.example.ui.components.OfflineBanner
import com.example.ui.components.SidebarDrawerContent
import com.example.ui.components.StarterPromptGrid
import com.example.ui.components.VoiceInputBottomSheet
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.NeonMint
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusCoral
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.ScreenDestination
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val activeConvId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val selectedAttachment by viewModel.selectedAttachmentUri.collectAsStateWithLifecycle()

    val showVoiceSheet by viewModel.showVoiceSheet.collectAsStateWithLifecycle()
    val isVoiceListening by viewModel.isVoiceListening.collectAsStateWithLifecycle()
    val voiceRecognizedText by viewModel.voiceRecognizedText.collectAsStateWithLifecycle()
    val voiceRmsLevel by viewModel.voiceRmsDb.collectAsStateWithLifecycle()
    val voiceError by viewModel.voiceError.collectAsStateWithLifecycle()

    var showClearDialog by remember { mutableStateOf(false) }

    val activeConversation = remember(conversations, activeConvId) {
        conversations.find { it.id == activeConvId }
    }

    val listState = rememberLazyListState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setAttachmentUri(uri.toString())
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceInput()
        } else {
            Toast.makeText(context, "Microphone permission required for voice input", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(messages.size, messages.lastOrNull()?.content?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CyberCardBg,
                drawerTonalElevation = 4.dp
            ) {
                SidebarDrawerContent(
                    conversations = conversations,
                    activeConversationId = activeConvId,
                    userProfile = userProfile,
                    onSelectConversation = { id -> viewModel.selectConversation(id) },
                    onNewChat = { viewModel.startNewChat() },
                    onRenameConversation = { id, title -> viewModel.renameConversation(id, title) },
                    onDeleteConversation = { id -> viewModel.deleteConversation(id) },
                    onTogglePin = { id, isPinned -> viewModel.togglePin(id, isPinned) },
                    onOpenSettings = { viewModel.navigateTo(ScreenDestination.SETTINGS_VIEW) },
                    onToggleTheme = { /* preserved */ },
                    onSignOut = { /* preserved */ },
                    onCloseDrawer = { scope.launch { drawerState.close() } },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                // Header matching Mockup Screen 3:
                // [Hamburger]   RM AI   [Trash/Clear]
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    color = CyberCardBg,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("open_sidebar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Sidebar",
                                tint = TextWhite
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "RM ",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                            Text(
                                text = "AI",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = NeonMint
                            )
                        }

                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.testTag("clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Chat",
                                tint = TextMuted
                            )
                        }
                    }
                }
            },
            bottomBar = {
                // Bottom Input Area matching Mockup Screen 3:
                // [Paperclip Attach]   [Type a message...]   [Send (Teal circle)]
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(),
                    color = CyberCardBg,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        // Attachment preview if user picked an image
                        if (selectedAttachment != null) {
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberCardElevated)
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = selectedAttachment,
                                    contentDescription = "Attachment preview",
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Image attached", color = TextWhite, fontSize = 12.sp)
                                IconButton(
                                    onClick = { viewModel.setAttachmentUri(null) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = StatusCoral, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(26.dp))
                                .background(CyberCardElevated)
                                .border(1.dp, CyberBorder, RoundedCornerShape(26.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Attachment paperclip button
                            IconButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "Attach image",
                                    tint = TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Text input field
                            androidx.compose.foundation.text.BasicTextField(
                                value = inputText,
                                onValueChange = { viewModel.onInputTextChange(it) },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(NeonMint),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp, vertical = 10.dp)
                                    .testTag("chat_input_text_field"),
                                decorationBox = { innerTextField ->
                                    if (inputText.isEmpty() && selectedAttachment == null) {
                                        Text(
                                            text = "Type a message...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = TextMuted
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            // Mic button
                            IconButton(
                                onClick = {
                                    val hasPerm = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasPerm) {
                                        viewModel.startVoiceInput()
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice input",
                                    tint = NeonMint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Send Button (Teal circular button with arrow)
                            val canSend = (inputText.isNotBlank() || selectedAttachment != null) && !isGenerating
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (canSend) NeonMint else CyberBorder)
                                    .clickable(enabled = canSend || isGenerating) {
                                        if (isGenerating) viewModel.stopGenerating()
                                        else if (canSend) viewModel.sendMessage()
                                    }
                                    .testTag("send_message_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isGenerating) {
                                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.Black, modifier = Modifier.size(18.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = if (canSend) Color.Black else TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    OfflineBanner(isOnline = isOnline)

                    if (messages.isEmpty()) {
                        StarterPromptGrid(
                            onSelectPrompt = { prompt -> viewModel.sendMessage(prompt) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                ChatMessageBubble(
                                    message = msg,
                                    isDarkTheme = isDarkTheme,
                                    onRegenerate = { viewModel.regenerateLastMessage() }
                                )
                            }
                        }
                    }
                }

                // Scroll to bottom FAB
                val showScrollToBottom by remember {
                    derivedStateOf {
                        messages.isNotEmpty() &&
                                listState.firstVisibleItemIndex < (messages.size - 2).coerceAtLeast(0)
                    }
                }

                AnimatedVisibility(
                    visible = showScrollToBottom,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        },
                        containerColor = CyberCardElevated,
                        contentColor = NeonMint,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Scroll to bottom",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Voice Input Bottom Sheet
    if (showVoiceSheet) {
        VoiceInputBottomSheet(
            isListening = isVoiceListening,
            recognizedText = voiceRecognizedText,
            rmsLevel = voiceRmsLevel,
            errorMessage = voiceError,
            onStartListening = { viewModel.startVoiceInput() },
            onStopListening = { viewModel.stopVoiceInput() },
            onConfirmText = { text, autoSend -> viewModel.onVoiceResultConfirmed(text, autoSend) },
            onDismiss = { viewModel.setShowVoiceSheet(false) }
        )
    }

    // Clear Chat confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = CyberCardBg,
            title = { Text("Clear Conversation?", color = TextWhite) },
            text = { Text("Are you sure you want to delete all messages in this conversation?", color = TextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        activeConvId?.let { viewModel.deleteConversation(it) }
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCoral)
                ) {
                    Text("Clear", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel", color = TextMuted) }
            }
        )
    }
}
