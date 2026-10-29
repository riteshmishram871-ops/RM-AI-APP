package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.ScreenDestination

data class ToolFeatureItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color,
    val onClick: () -> Unit
)

@Composable
fun ToolsScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val toolsList = listOf(
        ToolFeatureItem(
            id = "image_analysis",
            title = "Image Analysis",
            subtitle = "Understand images",
            icon = Icons.Default.ImageSearch,
            iconColor = AccentGallery,
            onClick = { viewModel.navigateTo(ScreenDestination.CAMERA_VIEW) }
        ),
        ToolFeatureItem(
            id = "web_search",
            title = "Web Search",
            subtitle = "Search the internet",
            icon = Icons.Default.Language,
            iconColor = AccentWeb,
            onClick = { viewModel.navigateTo(ScreenDestination.WEB_SEARCH_VIEW) }
        ),
        ToolFeatureItem(
            id = "language",
            title = "Language",
            subtitle = "Translate & help (Odia/Hindi/Eng)",
            icon = Icons.Default.Translate,
            iconColor = AccentChat,
            onClick = { viewModel.navigateTo(ScreenDestination.LANGUAGE_HELPER_VIEW) }
        ),
        ToolFeatureItem(
            id = "code_assistant",
            title = "Code Assistant",
            subtitle = "Help with code",
            icon = Icons.Default.Code,
            iconColor = AccentVoice,
            onClick = { viewModel.navigateTo(ScreenDestination.CODE_ASSISTANT_VIEW) }
        ),
        ToolFeatureItem(
            id = "calculator",
            title = "Calculator",
            subtitle = "Do calculations",
            icon = Icons.Default.Calculate,
            iconColor = AccentPhone,
            onClick = { viewModel.navigateTo(ScreenDestination.CALCULATOR_VIEW) }
        ),
        ToolFeatureItem(
            id = "quick_notes",
            title = "Quick Notes",
            subtitle = "Save your thoughts",
            icon = Icons.Default.EditNote,
            iconColor = AccentFiles,
            onClick = { viewModel.navigateTo(ScreenDestination.QUICK_NOTES_VIEW) }
        ),
        ToolFeatureItem(
            id = "reminders",
            title = "Reminders & To-Do",
            subtitle = "Schedule alerts & tasks",
            icon = Icons.Default.Alarm,
            iconColor = AccentMemory,
            onClick = { viewModel.navigateTo(ScreenDestination.REMINDERS_VIEW) }
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
            // Header matching Mockup Screen 8
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.testTag("tools_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tools",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Text(
                        text = "Useful AI-powered features",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tools Card List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(toolsList, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = item.onClick)
                            .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
                            .testTag("tool_card_${item.id}"),
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
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(item.iconColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = item.iconColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextWhite
                                )
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }
}
