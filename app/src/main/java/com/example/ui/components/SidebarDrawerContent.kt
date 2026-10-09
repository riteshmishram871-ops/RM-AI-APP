package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ConversationEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.CoralError
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoDark
import com.example.ui.theme.PrimaryIndigoLight
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.util.DateUtils

@Composable
fun SidebarDrawerContent(
    conversations: List<ConversationEntity>,
    activeConversationId: String?,
    userProfile: UserProfileEntity?,
    onSelectConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    onRenameConversation: (id: String, newTitle: String) -> Unit,
    onDeleteConversation: (id: String) -> Unit,
    onTogglePin: (id: String, isPinned: Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTheme: () -> Unit,
    onSignOut: () -> Unit,
    onCloseDrawer: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var renameDialogTarget by remember { mutableStateOf<ConversationEntity?>(null) }
    var renameInputText by remember { mutableStateOf("") }
    var deleteDialogTarget by remember { mutableStateOf<ConversationEntity?>(null) }

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    val groupedConversations = remember(filteredConversations) {
        filteredConversations.groupBy {
            if (it.isPinned) "Pinned" else DateUtils.getDateGroup(it.updatedAt)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(310.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
        ) {
            // Drawer Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.rm_ai_logo),
                        contentDescription = "RM AI Logo",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                    Column {
                        Text(
                            text = "RM AI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Conversational Assistant",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                    }
                }

                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close sidebar",
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // New Chat Button
            Button(
                onClick = {
                    onNewChat()
                    onCloseDrawer()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .height(46.dp)
                    .testTag("new_chat_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Chat",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = PureWhite
                )
            }

            // Search bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search chats…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(PrimaryIndigo),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // Conversations List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                if (conversations.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No conversations yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate400
                            )
                        }
                    }
                } else {
                    groupedConversations.forEach { (groupLabel, itemsInGroup) ->
                        item {
                            Text(
                                text = groupLabel.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp
                                ),
                                color = PrimaryIndigoLight,
                                modifier = Modifier.padding(start = 10.dp, top = 12.dp, bottom = 4.dp)
                            )
                        }

                        items(itemsInGroup, key = { it.id }) { conv ->
                            ConversationRowItem(
                                conversation = conv,
                                isActive = conv.id == activeConversationId,
                                onClick = {
                                    onSelectConversation(conv.id)
                                    onCloseDrawer()
                                },
                                onRename = {
                                    renameDialogTarget = conv
                                    renameInputText = conv.title
                                },
                                onDelete = {
                                    deleteDialogTarget = conv
                                },
                                onTogglePin = {
                                    onTogglePin(conv.id, !conv.isPinned)
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // Bottom Profile Section
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onOpenSettings()
                            onCloseDrawer()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(avatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = profile.email,
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            onOpenSettings()
                            onCloseDrawer()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Rename Dialog
    renameDialogTarget?.let { conv ->
        AlertDialog(
            onDismissRequest = { renameDialogTarget = null },
            title = { Text("Rename Chat") },
            text = {
                OutlinedTextField(
                    value = renameInputText,
                    onValueChange = { renameInputText = it },
                    label = { Text("Chat Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInputText.isNotBlank()) {
                            onRenameConversation(conv.id, renameInputText.trim())
                        }
                        renameDialogTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameDialogTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Dialog
    deleteDialogTarget?.let { conv ->
        AlertDialog(
            onDismissRequest = { deleteDialogTarget = null },
            title = { Text("Delete Conversation") },
            text = { Text("Are you sure you want to delete \"${conv.title}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteConversation(conv.id)
                        deleteDialogTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralError)
                ) {
                    Text("Delete", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ConversationRowItem(
    conversation: ConversationEntity,
    isActive: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val bg = if (isActive) {
        PrimaryIndigo.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .then(
                if (isActive) {
                    Modifier.border(1.dp, PrimaryIndigo.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (conversation.isPinned) Icons.Default.PushPin else Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            tint = if (isActive) PrimaryIndigo else Slate400,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = conversation.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Slate400,
                    modifier = Modifier.size(14.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (conversation.isPinned) "Unpin" else "Pin to top") },
                    onClick = {
                        menuExpanded = false
                        onTogglePin()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = {
                        menuExpanded = false
                        onRename()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = CoralError) },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = CoralError, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}
