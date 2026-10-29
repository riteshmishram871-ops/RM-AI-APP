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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

data class StarterPrompt(
    val iconEmoji: String,
    val title: String,
    val subtitle: String,
    val promptText: String
)

val defaultStarterPrompts = listOf(
    StarterPrompt(
        iconEmoji = "🗣️",
        title = "Odia / Multilingual Chat",
        subtitle = "Bhai, python re input output kemiti kaam kare?",
        promptText = "Bhai, python re input output kemiti kaam kare?"
    ),
    StarterPrompt(
        iconEmoji = "💡",
        title = "Explain Quantum Computing",
        subtitle = "Intuitive breakdown with real-world analogies",
        promptText = "Explain quantum computing in simple terms with everyday analogies."
    ),
    StarterPrompt(
        iconEmoji = "💻",
        title = "Debug Python Async Code",
        subtitle = "Concurrency, gather tasks, and error handling",
        promptText = "Show me an example of an asynchronous Python task with error handling and typing."
    ),
    StarterPrompt(
        iconEmoji = "📝",
        title = "Draft a High-Impact Pitch",
        subtitle = "Concise, professional cold email outreach",
        promptText = "Draft a concise, professional cold outreach email proposing a strategic AI partnership."
    )
)

@Composable
fun StarterPromptGrid(
    onSelectPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // RM AI Circular Monogram
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(16.dp, CircleShape, spotColor = NeonMint)
                .clip(CircleShape)
                .background(CyberCardBg)
                .border(2.dp, NeonMint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.rm_ai_logo),
                contentDescription = "RM AI Logo",
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your personal AI conversation partner",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SUGGESTIONS",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = NeonMint,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            defaultStarterPrompts.forEach { item ->
                PromptCard(
                    prompt = item,
                    onClick = { onSelectPrompt(item.promptText) }
                )
            }
        }
    }
}

@Composable
fun PromptCard(
    prompt: StarterPrompt,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .testTag("starter_prompt_${prompt.title.lowercase().replace(" ", "_")}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberCardBg)
            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(NeonMint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = prompt.iconEmoji,
                fontSize = 18.sp
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prompt.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextWhite
            )
            Text(
                text = prompt.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}
