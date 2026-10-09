package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.UserProfileEntity
import com.example.data.remote.GeminiApiClient
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoLight
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ChatViewModel

val avatarColorPalette = listOf(
    "#4F46E5", // Deep Indigo
    "#7C3AED", // Electric Violet
    "#10B981", // Emerald
    "#F43F5E", // Rose
    "#F59E0B", // Amber
    "#06B6D4"  // Cyan
)

@Composable
fun SettingsScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = userProfile ?: UserProfileEntity()

    var displayName by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var email by remember(profile.email) { mutableStateOf(profile.email) }
    var selectedColor by remember(profile.avatarColorHex) { mutableStateOf(profile.avatarColorHex) }
    var selectedTheme by remember(profile.themeMode) { mutableStateOf(profile.themeMode) }
    var selectedPersona by remember(profile.aiPersona) { mutableStateOf(profile.aiPersona) }
    var temperature by remember(profile.temperature) { mutableFloatStateOf(profile.temperature) }
    var customApiKey by remember(profile.customApiKey) { mutableStateOf(profile.customApiKey) }

    var showClearHistoryDialog by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    val avatarColor = remember(selectedColor) {
        try {
            Color(android.graphics.Color.parseColor(selectedColor))
        } catch (_: Exception) {
            PrimaryIndigo
        }
    }

    val initials = remember(displayName) {
        displayName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifBlank { "RM" }
    }

    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Settings & Profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Profile Card
                SettingsSectionHeader(title = "PROFILE", icon = Icons.Default.Palette)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Avatar preview + color picker
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(avatarColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhite
                                )
                            }

                            Column {
                                Text(
                                    text = "Avatar Color",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Slate400
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    avatarColorPalette.forEach { hex ->
                                        val c = try {
                                            Color(android.graphics.Color.parseColor(hex))
                                        } catch (_: Exception) {
                                            PrimaryIndigo
                                        }
                                        val isSelected = selectedColor.equals(hex, ignoreCase = true)
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(c)
                                                .border(
                                                    if (isSelected) 2.dp else 0.dp,
                                                    if (isSelected) PureWhite else Color.Transparent,
                                                    CircleShape
                                                )
                                                .clickable { selectedColor = hex },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = PureWhite,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("display_name_input")
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                viewModel.updateProfile(
                                    displayName = displayName,
                                    email = email,
                                    avatarColorHex = selectedColor,
                                    themeMode = selectedTheme,
                                    aiPersona = selectedPersona,
                                    temperature = temperature,
                                    customApiKey = customApiKey
                                )
                                Toast.makeText(context, "Profile changes saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("save_profile_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Profile Changes", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Appearance Section
                SettingsSectionHeader(title = "APPEARANCE", icon = Icons.Default.Palette)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Theme Preference",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("DARK" to "Dark", "LIGHT" to "Light", "SYSTEM" to "System").forEach { (mode, label) ->
                                val selected = selectedTheme == mode
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedTheme = mode
                                        viewModel.updateProfile(
                                            displayName = displayName,
                                            email = email,
                                            avatarColorHex = selectedColor,
                                            themeMode = mode,
                                            aiPersona = selectedPersona,
                                            temperature = temperature,
                                            customApiKey = customApiKey
                                        )
                                    },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryIndigo,
                                        selectedLabelColor = PureWhite
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // AI Persona & Intelligence Section
                SettingsSectionHeader(title = "AI PERSONA & SETTINGS", icon = Icons.Default.Psychology)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Assistant Persona",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val personas = listOf(
                            "HELPFUL" to ("🤖 Helpful Assistant" to "Balanced, clear, friendly"),
                            "CODER" to ("💻 Code Architect" to "Code-first, type safe, concise"),
                            "WRITER" to ("✍️ Creative Writer" to "Engaging, editorial, eloquent"),
                            "TUTOR" to ("🎓 Academic Tutor" to "Intuitive, step-by-step breakdown")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            personas.forEach { (id, pair) ->
                                val (title, desc) = pair
                                val isSelected = selectedPersona == id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) PrimaryIndigo.copy(alpha = 0.12f) else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedPersona = id
                                            viewModel.updateProfile(
                                                displayName = displayName,
                                                email = email,
                                                avatarColorHex = selectedColor,
                                                themeMode = selectedTheme,
                                                aiPersona = id,
                                                temperature = temperature,
                                                customApiKey = customApiKey
                                            )
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate400
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = PrimaryIndigo,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Temperature Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Creativity (Temperature)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = String.format("%.1f", temperature),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryIndigoLight
                                )
                            }
                            Slider(
                                value = temperature,
                                onValueChange = {
                                    temperature = it
                                    viewModel.updateProfile(
                                        displayName = displayName,
                                        email = email,
                                        avatarColorHex = selectedColor,
                                        themeMode = selectedTheme,
                                        aiPersona = selectedPersona,
                                        temperature = it,
                                        customApiKey = customApiKey
                                    )
                                },
                                valueRange = 0.1f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = PrimaryIndigo,
                                    activeTrackColor = PrimaryIndigo
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Precise & Factual", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                Text("Creative & Fluent", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            }
                        }
                    }
                }

                // API Key & Model Configuration
                SettingsSectionHeader(title = "API & MODEL ENGINE", icon = Icons.Default.Key)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val keyConfigured = GeminiApiClient.isApiKeyConfigured(customApiKey)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (keyConfigured) EmeraldSuccess else PrimaryIndigoLight)
                            )
                            Text(
                                text = if (keyConfigured) "AI Status: Online (Gemini 3.5 Flash)" else "AI Status: Built-in Intelligent Engine",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "By default, RM AI is connected with server-side AI processing. You can also specify an optional custom Gemini API key below to override.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )

                        OutlinedTextField(
                            value = customApiKey,
                            onValueChange = { customApiKey = it },
                            label = { Text("Custom API Key (Optional)") },
                            singleLine = true,
                            placeholder = { Text("AIzaSy...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Data Management Section
                SettingsSectionHeader(title = "DATA & SESSIONS", icon = Icons.Default.DeleteForever)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showClearHistoryDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Clear All Chat History",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.signOut() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Sign Out", color = CoralError, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // About section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "RM AI v1.0",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Responsive AI conversation assistant. All conversations are stored privately on your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All Conversations?") },
            text = { Text("This will permanently delete all past chat threads. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllConversations()
                        showClearHistoryDialog = false
                        Toast.makeText(context, "All conversations cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralError)
                ) {
                    Text("Delete Everything", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryIndigoLight,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = PrimaryIndigoLight
        )
    }
}
