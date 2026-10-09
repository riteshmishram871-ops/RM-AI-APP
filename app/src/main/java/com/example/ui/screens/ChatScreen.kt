package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.ConversationEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatMessageBubble
import com.example.ui.components.OfflineBanner
import com.example.ui.components.SidebarDrawerContent
import com.example.ui.components.StarterPromptGrid
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoLight
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val activeConvId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    var showProfileSheet by remember { mutableStateOf(false) }

    val activeConversation = remember(conversations, activeConvId) {
        conversations.find { it.id == activeConvId }
    }

    val listState = rememberLazyListState()

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, messages.lastOrNull()?.content?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Handle back button when drawer is open
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
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
                    onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    onToggleTheme = { viewModel.toggleTheme() },
                    onSignOut = { viewModel.signOut() },
                    onCloseDrawer = { scope.launch { drawerState.close() } },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                ChatTopHeader(
                    title = activeConversation?.title ?: "New Chat",
                    userProfile = userProfile,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onOpenProfile = { showProfileSheet = true }
                )
            },
            bottomBar = {
                ChatInputBar(
                    inputText = inputText,
                    onInputChange = { viewModel.onInputTextChange(it) },
                    onSendMessage = { viewModel.sendMessage() },
                    isGenerating = isGenerating,
                    onStopGenerating = { viewModel.stopGenerating() },
                    isOnline = isOnline,
                    modifier = Modifier.imePadding()
                )
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
                    // Offline alert banner
                    OfflineBanner(isOnline = isOnline)

                    if (messages.isEmpty()) {
                        StarterPromptGrid(
                            onSelectPrompt = { prompt ->
                                viewModel.sendMessage(prompt)
                            },
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
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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

    // Profile & Quick Settings Modal Bottom Sheet
    if (showProfileSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showProfileSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ProfileQuickSheetContent(
                userProfile = userProfile,
                onOpenFullSettings = {
                    showProfileSheet = false
                    viewModel.navigateTo(AppScreen.SETTINGS)
                },
                onToggleTheme = { viewModel.toggleTheme() },
                onSignOut = {
                    showProfileSheet = false
                    viewModel.signOut()
                },
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
fun ChatTopHeader(
    title: String,
    userProfile: UserProfileEntity?,
    onOpenDrawer: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = userProfile ?: UserProfileEntity()
    val avatarColor = remember(profile.avatarColorHex) {
        try {
            Color(android.graphics.Color.parseColor(profile.avatarColorHex))
        } catch (_: Exception) {
            PrimaryIndigo
        }
    }
    val initials = remember(profile.displayName) {
        profile.displayName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifBlank { "RM" }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Hamburger icon button
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("open_sidebar_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open sidebar",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title & Persona Badge
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "RM AI • ${getPersonaLabel(profile.aiPersona)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryIndigoLight
                )
            }

            // User Avatar Circle
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(avatarColor)
                    .clickable(onClick = onOpenProfile)
                    .testTag("user_avatar_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = PureWhite
                )
            }
        }
    }
}

@Composable
fun ProfileQuickSheetContent(
    userProfile: UserProfileEntity?,
    onOpenFullSettings: () -> Unit,
    onToggleTheme: () -> Unit,
    onSignOut: () -> Unit,
    isDarkTheme: Boolean
) {
    val profile = userProfile ?: UserProfileEntity()
    val avatarColor = remember(profile.avatarColorHex) {
        try {
            Color(android.graphics.Color.parseColor(profile.avatarColorHex))
        } catch (_: Exception) {
            PrimaryIndigo
        }
    }
    val initials = remember(profile.displayName) {
        profile.displayName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifBlank { "RM" }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = PureWhite
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = profile.displayName,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = profile.email,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400
        )

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))

        // Open Full Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onOpenFullSettings)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Settings & AI Configuration",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Toggle Theme
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onToggleTheme)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Dark Mode",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isDarkTheme) "Enabled (Dark)" else "Disabled (Light)",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate400
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sign Out
        TextButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Sign Out",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun getPersonaLabel(persona: String): String {
    return when (persona) {
        "CODER" -> "Code Architect"
        "WRITER" -> "Creative Writer"
        "TUTOR" -> "Academic Tutor"
        else -> "Helpful Assistant"
    }
}
