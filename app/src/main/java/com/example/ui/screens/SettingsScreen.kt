package com.example.ui.screens

import android.app.Activity
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.NeonMint
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusCoral
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ChatViewModel

data class SettingsRowItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun SettingsScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit = { viewModel.navigateBack() },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = userProfile ?: UserProfileEntity()
    val firebaseUser by viewModel.firebaseUser.collectAsStateWithLifecycle()
    val activity = context as? Activity

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val settingsItems = listOf(
        SettingsRowItem(
            title = "Language",
            subtitle = profile.selectedLanguage,
            icon = Icons.Default.Language,
            onClick = { showLanguageDialog = true }
        ),
        SettingsRowItem(
            title = "Voice & Speech",
            subtitle = "Voice settings & TTS playback",
            icon = Icons.Default.Mic,
            onClick = { showVoiceDialog = true }
        ),
        SettingsRowItem(
            title = "Theme",
            subtitle = if (profile.themeMode == "DARK") "Dark (Default)" else "Light",
            icon = Icons.Default.DarkMode,
            onClick = {
                viewModel.toggleTheme()
                Toast.makeText(context, "Theme toggled", Toast.LENGTH_SHORT).show()
            }
        ),
        SettingsRowItem(
            title = "Firebase Cloud Sync",
            subtitle = if (firebaseUser != null) "Connected & Synced" else "Offline Mode",
            icon = Icons.Default.CloudDone,
            onClick = {
                if (firebaseUser == null && activity != null) {
                    viewModel.initiateGoogleSignIn(activity)
                } else {
                    Toast.makeText(context, "Firebase Firestore sync active", Toast.LENGTH_SHORT).show()
                }
            }
        ),
        SettingsRowItem(
            title = "Privacy & Security",
            subtitle = "Zero-trust encrypted database rules",
            icon = Icons.Default.Security,
            onClick = { showPrivacyDialog = true }
        ),
        SettingsRowItem(
            title = "About RM AI",
            subtitle = "v1.0.0 • Gemini 3.5 Flash",
            icon = Icons.Default.Info,
            onClick = { showAboutDialog = true }
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
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                }

                // Profile Card matching Mockup Screen 10:
                // [RM Circle Icon]   RM AI   v1.0.0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CyberBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberCardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(CyberCardElevated)
                                .border(2.dp, NeonMint, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (firebaseUser?.displayName ?: profile.displayName).take(2).uppercase().ifBlank { "RM" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = NeonMint
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = firebaseUser?.displayName ?: profile.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                            Text(
                                text = firebaseUser?.email ?: if (profile.isLoggedIn) profile.email else "Guest Session",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (firebaseUser != null) NeonMint else TextMuted)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (firebaseUser != null) "Cloud Synced (Firebase)" else "Offline Cache (Room)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = if (firebaseUser != null) NeonMint else TextMuted
                                )
                            }
                        }

                        if (firebaseUser != null || profile.isLoggedIn) {
                            IconButton(
                                onClick = {
                                    viewModel.signOut()
                                    Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign out",
                                    tint = StatusCoral,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Settings List Items
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    settingsItems.forEach { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(onClick = item.onClick)
                                .border(1.dp, CyberBorder, RoundedCornerShape(16.dp)),
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
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CyberCardElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = NeonMint,
                                        modifier = Modifier.size(20.dp)
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
                }
            }

            // Footer Quote matching Mockup Screen 10:
            // "Better questions create a better tomorrow. - RM AI"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Better questions\ncreate a better tomorrow.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp
                    ),
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "– RM AI",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = NeonMint,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Language Dialog (English, Romanized Odia, Hindi, Spanish)
    if (showLanguageDialog) {
        val langs = listOf("English (India)", "Romanized Odia", "Hindi", "English (US)", "Spanish")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = CyberCardBg,
            title = { Text("Select AI Assistant Language", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    langs.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (profile.selectedLanguage == lang) NeonMint.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    viewModel.updateProfile(
                                        displayName = profile.displayName,
                                        email = profile.email,
                                        avatarColorHex = profile.avatarColorHex,
                                        themeMode = profile.themeMode,
                                        aiPersona = profile.aiPersona,
                                        temperature = profile.temperature,
                                        customApiKey = profile.customApiKey,
                                        selectedLanguage = lang,
                                        isMemoryEnabled = profile.isMemoryEnabled
                                    )
                                    showLanguageDialog = false
                                }
                                .padding(12.dp)
                        ) {
                            Text(lang, color = if (profile.selectedLanguage == lang) NeonMint else TextWhite)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Close", color = TextMuted) }
            }
        )
    }

    // Voice & Speech TTS settings dialog
    if (showVoiceDialog) {
        var testRate by remember { mutableFloatStateOf(profile.ttsSpeechRate) }
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            containerColor = CyberCardBg,
            title = { Text("Voice & Speech Settings", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Speech Rate (Speed): ${String.format("%.1fx", testRate)}", color = TextMuted)
                    Slider(
                        value = testRate,
                        onValueChange = { testRate = it },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = NeonMint, activeTrackColor = NeonMint)
                    )

                    Button(
                        onClick = { viewModel.speakText("Hello Ritesh! This is RM AI speaking to you.") },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCardElevated),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = NeonMint)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Voice Audio", color = TextWhite)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoiceDialog = false }) { Text("Done", color = NeonMint) }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = CyberCardBg,
            title = { Text("Privacy & Security", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "• All your conversations, notes, and memories are stored on-device in a protected Room SQLite database.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Text(
                        "• API calls are securely authenticated without leaking personal credentials.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = {
                            viewModel.clearAllConversations()
                            Toast.makeText(context, "All chat history wiped", Toast.LENGTH_SHORT).show()
                            showPrivacyDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusCoral),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete All Chat History", color = PureWhite)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Close", color = NeonMint) }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = CyberCardBg,
            title = { Text("About RM AI", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "RM AI — Your Personal AI Assistant\nVersion 1.0.0\n\nEquipped with multilingual speech recognition, text-to-speech, camera analysis, and smart tools.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("OK", color = NeonMint) }
            }
        )
    }
}
