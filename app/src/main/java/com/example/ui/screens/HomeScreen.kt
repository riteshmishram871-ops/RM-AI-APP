package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AccentCamera
import com.example.ui.theme.AccentChat
import com.example.ui.theme.AccentFiles
import com.example.ui.theme.AccentGallery
import com.example.ui.theme.AccentMemory
import com.example.ui.theme.AccentPhone
import com.example.ui.theme.AccentVoice
import com.example.ui.theme.AccentWeb
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.NeonMint
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.ScreenDestination

data class HomeActionCardItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val displayName = userProfile?.displayName ?: "Ritesh"

    val gridItems = listOf(
        HomeActionCardItem(
            id = "chat",
            title = "Chat",
            subtitle = "Ask anything",
            icon = Icons.AutoMirrored.Filled.Chat,
            iconBgColor = AccentChat,
            onClick = { viewModel.selectTab(MainTab.CHAT) }
        ),
        HomeActionCardItem(
            id = "voice",
            title = "Voice",
            subtitle = "Talk & listen",
            icon = Icons.Default.Mic,
            iconBgColor = AccentVoice,
            onClick = { viewModel.navigateTo(ScreenDestination.VOICE_ASSISTANT) }
        ),
        HomeActionCardItem(
            id = "phone_control",
            title = "Phone Control",
            subtitle = "Apps, settings...",
            icon = Icons.Default.Smartphone,
            iconBgColor = AccentPhone,
            onClick = { viewModel.navigateTo(ScreenDestination.PHONE_CONTROL) }
        ),
        HomeActionCardItem(
            id = "camera",
            title = "Camera",
            subtitle = "Capture & scan",
            icon = Icons.Default.CameraAlt,
            iconBgColor = AccentCamera,
            onClick = { viewModel.navigateTo(ScreenDestination.CAMERA_VIEW) }
        ),
        HomeActionCardItem(
            id = "gallery",
            title = "Gallery",
            subtitle = "View your photos",
            icon = Icons.Default.PhotoLibrary,
            iconBgColor = AccentGallery,
            onClick = { viewModel.navigateTo(ScreenDestination.GALLERY_VIEW) }
        ),
        HomeActionCardItem(
            id = "files",
            title = "Files",
            subtitle = "Browse & manage",
            icon = Icons.Default.Folder,
            iconBgColor = AccentFiles,
            onClick = {
                try {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Opening Files...", Toast.LENGTH_SHORT).show()
                }
            }
        ),
        HomeActionCardItem(
            id = "web_search",
            title = "Web Search",
            subtitle = "Search the internet",
            icon = Icons.Default.Language,
            iconBgColor = AccentWeb,
            onClick = { viewModel.navigateTo(ScreenDestination.WEB_SEARCH_VIEW) }
        ),
        HomeActionCardItem(
            id = "memory",
            title = "Memory",
            subtitle = "Save & recall",
            icon = Icons.Default.Psychology,
            iconBgColor = AccentMemory,
            onClick = { viewModel.navigateTo(ScreenDestination.MEMORY_VIEW) }
        )
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            // Header: "RM AI" with Settings gear icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "RM ",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = TextWhite
                    )
                    Text(
                        text = "AI",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = NeonMint
                    )
                }

                IconButton(
                    onClick = { viewModel.navigateTo(ScreenDestination.SETTINGS_VIEW) },
                    modifier = Modifier.testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextWhite.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // User Greeting
            Text(
                text = "Good Morning, $displayName! 👋",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = TextMuted,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            // Prompt Box Card: "How can I help you today? >"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.selectTab(MainTab.CHAT) }
                    .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
                    .testTag("home_prompt_bar"),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonMint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = NeonMint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "How can I help you today?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextMuted,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go to chat",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2-Column Grid of 8 Actions (Exact match to Mockup Screen 2)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(gridItems, key = { it.id }) { item ->
                    HomeGridCard(item = item)
                }
            }
        }
    }
}

@Composable
fun HomeGridCard(
    item: HomeActionCardItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = item.onClick)
            .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
            .testTag("home_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.iconBgColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconBgColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = TextWhite
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = TextMuted
                )
            }
        }
    }
}
