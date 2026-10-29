package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonMintDark
import com.example.ui.theme.NeonMintGlow
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusCoral
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun VoiceAssistantScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isListening by viewModel.isVoiceListening.collectAsStateWithLifecycle()
    val recognizedText by viewModel.voiceRecognizedText.collectAsStateWithLifecycle()
    val rmsLevel by viewModel.voiceRmsDb.collectAsStateWithLifecycle()
    val isSpeakingTts by viewModel.isSpeakingTts.collectAsStateWithLifecycle()

    BackHandler {
        viewModel.stopVoiceInput()
        viewModel.stopSpeaking()
        viewModel.navigateBack()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceInput()
        } else {
            Toast.makeText(context, "Microphone permission required for voice assistant", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleMicToggle() {
        if (isListening) {
            viewModel.stopVoiceInput()
            if (recognizedText.isNotBlank()) {
                viewModel.sendMessage(recognizedText)
            }
        } else {
            val hasPerm = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                viewModel.startVoiceInput()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Ripple pulse animations
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_rings")
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring1"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Back, "Voice Assistant", Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.stopVoiceInput()
                        viewModel.stopSpeaking()
                        viewModel.navigateBack()
                    },
                    modifier = Modifier.testTag("voice_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }

                Text(
                    text = "Voice Assistant",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )

                IconButton(
                    onClick = { viewModel.navigateTo(ScreenDestination.SETTINGS_VIEW) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextWhite.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Center: Giant Glowing Concentric Microphone Button
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Pulse Ring
                if (isListening || isSpeakingTts) {
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .scale(ring1Scale + (rmsLevel * 0.3f))
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        NeonMint.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .scale(1f + (rmsLevel * 0.2f))
                            .clip(CircleShape)
                            .border(1.5.dp, NeonMint.copy(alpha = 0.5f), CircleShape)
                    )
                }

                // Main Circle Button
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) Brush.radialGradient(listOf(NeonMintDark, CyberCardBg))
                            else Brush.radialGradient(listOf(CyberCardElevated, CyberCardBg))
                        )
                        .border(
                            3.dp,
                            if (isListening) NeonMint else CyberBorder,
                            CircleShape
                        )
                        .clickable { handleMicToggle() }
                        .testTag("voice_assistant_giant_mic"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSpeakingTts) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = if (isListening) NeonMintGlow else TextWhite,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            // Labels
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when {
                        isSpeakingTts -> "Speaking answer aloud..."
                        isListening -> "Listening to your voice..."
                        recognizedText.isNotBlank() -> "“$recognizedText”"
                        else -> "Tap to speak"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = if (isListening) NeonMint else TextWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isListening) "Speak your query naturally in English, Hindi, or Odia."
                    else "Say something like 'Hey RM' or just talk normally.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }

            // Animated Cyan/Teal Waveform
            VoiceAudioWaveform(
                isListening = isListening || isSpeakingTts,
                rmsLevel = rmsLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 24.dp)
            )

            // Middle Action Buttons Row: Keyboard | Stop (Red) | Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Keyboard button
                IconButton(
                    onClick = {
                        viewModel.stopVoiceInput()
                        viewModel.selectTab(MainTab.CHAT)
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(CyberCardBg)
                        .border(1.dp, CyberBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Switch to keyboard",
                        tint = TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Red Circular Stop button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(StatusCoral)
                        .clickable {
                            viewModel.stopVoiceInput()
                            viewModel.stopSpeaking()
                        }
                        .testTag("voice_assistant_stop_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = PureWhite,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Settings button
                IconButton(
                    onClick = { viewModel.navigateTo(ScreenDestination.SETTINGS_VIEW) },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(CyberCardBg)
                        .border(1.dp, CyberBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Voice settings",
                        tint = TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Bottom Suggestion Box matching Mockup Screen 4
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
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Say or type what you want...",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SuggestionChipItem(text = "\"Open YouTube\"", onClick = { viewModel.sendMessage("Open YouTube") })
                            SuggestionChipItem(text = "\"Show my files\"", onClick = { viewModel.sendMessage("Show my files") })
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NeonMint.copy(alpha = 0.15f))
                            .clickable { handleMicToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = NeonMint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SuggestionChipItem(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberCardElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = TextWhite
        )
    }
}

@Composable
fun VoiceAudioWaveform(
    isListening: Boolean,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bars = 25
        for (i in 0 until bars) {
            val centerDist = Math.abs(i - bars / 2).toFloat() / (bars / 2)
            val bellFactor = (1f - centerDist * 0.7f).coerceIn(0.2f, 1f)

            val heightFactor = if (isListening) {
                ((rmsLevel * 1.5f + 0.15f) * bellFactor).coerceIn(0.1f, 1f)
            } else {
                (0.12f * bellFactor)
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(3.dp)
                    .height((36.dp * heightFactor).coerceAtLeast(4.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(NeonMintGlow, NeonCyan)
                        )
                    )
            )
        }
    }
}
