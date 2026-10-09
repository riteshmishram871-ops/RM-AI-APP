package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MessageEntity
import com.example.ui.theme.AssistantBubbleDark
import com.example.ui.theme.AssistantBubbleLight
import com.example.ui.theme.CodeBlockDark
import com.example.ui.theme.CodeBlockLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RichBlack
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.UserBubbleColor
import com.example.util.DateUtils
import kotlinx.coroutines.delay

@Composable
fun ChatMessageBubble(
    message: MessageEntity,
    isDarkTheme: Boolean,
    onRegenerate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val isAssistant = message.role == "assistant"
    val context = LocalContext.current

    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    }

    val bubbleBg = if (isUser) {
        UserBubbleColor
    } else {
        if (isDarkTheme) AssistantBubbleDark else AssistantBubbleLight
    }

    val contentColor = if (isUser) {
        PureWhite
    } else {
        if (isDarkTheme) PureWhite else Slate800
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = alignment
    ) {
        // Main bubble
        Box(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .clip(bubbleShape)
                .background(bubbleBg)
                .then(
                    if (!isUser && isDarkTheme) {
                        Modifier.border(1.dp, Slate700.copy(alpha = 0.5f), bubbleShape)
                    } else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                if (isUser) {
                    Text(
                        text = message.content,
                        color = contentColor,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                        modifier = Modifier.testTag("user_message_text")
                    )
                } else {
                    if (message.content.isBlank() && message.isStreaming) {
                        TypingIndicator(modifier = Modifier.padding(vertical = 4.dp))
                    } else {
                        MarkdownMessageRenderer(
                            content = message.content,
                            isDarkTheme = isDarkTheme,
                            isStreaming = message.isStreaming,
                            textColor = contentColor
                        )
                    }
                }
            }
        }

        // Message footer row (timestamp + action buttons)
        Row(
            modifier = Modifier
                .padding(top = 4.dp, start = if (isUser) 0.dp else 4.dp, end = if (isUser) 4.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = DateUtils.formatMessageTime(message.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = Slate400
            )

            if (isAssistant && message.content.isNotBlank() && !message.isStreaming) {
                // Copy full message icon button
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("RM AI Response", message.content))
                        Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy message",
                        tint = Slate400,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Regenerate button
                IconButton(
                    onClick = onRegenerate,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Regenerate response",
                        tint = Slate400,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Parses markdown into text paragraphs and syntax code blocks.
 */
@Composable
fun MarkdownMessageRenderer(
    content: String,
    isDarkTheme: Boolean,
    isStreaming: Boolean,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val segments = remember(content) { parseMarkdownSegments(content) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        segments.forEach { segment ->
            when (segment) {
                is MarkdownSegment.CodeBlock -> {
                    CodeBlockCard(
                        code = segment.code,
                        language = segment.language,
                        isDarkTheme = isDarkTheme
                    )
                }
                is MarkdownSegment.TextBlock -> {
                    TextParagraph(
                        text = segment.text,
                        textColor = textColor,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }

        if (isStreaming) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 8.dp, height = 16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PrimaryIndigo)
                )
            }
        }
    }
}

@Composable
fun TextParagraph(
    text: String,
    textColor: Color,
    isDarkTheme: Boolean
) {
    val lines = text.split("\n")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### "),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## "),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = textColor,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.removePrefix("# "),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = textColor,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("> ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .background(PrimaryIndigo)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = trimmed.removePrefix("> "),
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = textColor.copy(alpha = 0.85f)
                        )
                    }
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, top = 1.dp, bottom = 1.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryIndigo,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = formatInlineMarkdown(trimmed.removePrefix("- ").removePrefix("* ")),
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                            color = textColor
                        )
                    }
                }
                trimmed.matches("^\\d+\\.\\s.*".toRegex()) -> {
                    val number = trimmed.substringBefore(".")
                    val body = trimmed.substringAfter(". ")
                    Row(
                        modifier = Modifier.padding(start = 4.dp, top = 1.dp, bottom = 1.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "$number.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = PrimaryIndigo,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = formatInlineMarkdown(body),
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                            color = textColor
                        )
                    }
                }
                trimmed.isNotBlank() -> {
                    Text(
                        text = formatInlineMarkdown(trimmed),
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                        color = textColor
                    )
                }
            }
        }
    }
}

/**
 * Renders styled terminal-like code block with language title and a one-click copy button.
 */
@Composable
fun CodeBlockCard(
    code: String,
    language: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    val codeBg = if (isDarkTheme) CodeBlockDark else Slate900
    val headerBg = if (isDarkTheme) Color(0xFF131C2E) else Color(0xFF1E293B)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(codeBg)
            .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
    ) {
        // Header Bar with Language tag and Copy button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" }.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = Slate400
            )

            // One-click copy code button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("RM AI Code", code))
                        isCopied = true
                    }
                    .background(if (isCopied) EmeraldSuccess.copy(alpha = 0.2f) else Slate700.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = if (isCopied) "Copied" else "Copy Code",
                    tint = if (isCopied) EmeraldSuccess else Slate300,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (isCopied) "Copied!" else "Copy Code",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = if (isCopied) EmeraldSuccess else Slate300
                )
            }
        }

        // Code content
        Text(
            text = code,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 19.sp
            ),
            color = Color(0xFFE2E8F0),
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        )
    }
}

fun formatInlineMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex()
        val italicRegex = "(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)".toRegex()
        val codeRegex = "`(.*?)`".toRegex()

        // Simple token matching
        var currentText = text
        var i = 0
        while (i < currentText.length) {
            if (currentText.startsWith("**", i)) {
                val end = currentText.indexOf("**", i + 2)
                if (end != -1) {
                    val boldContent = currentText.substring(i + 2, end)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(boldContent)
                    }
                    i = end + 2
                    continue
                }
            } else if (currentText.startsWith("`", i)) {
                val end = currentText.indexOf("`", i + 1)
                if (end != -1) {
                    val codeContent = currentText.substring(i + 1, end)
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0x334F46E5),
                            color = PrimaryIndigo
                        )
                    ) {
                        append(" $codeContent ")
                    }
                    i = end + 1
                    continue
                }
            } else if (currentText.startsWith("*", i) && !currentText.startsWith("**", i)) {
                val end = currentText.indexOf("*", i + 1)
                if (end != -1) {
                    val italicContent = currentText.substring(i + 1, end)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(italicContent)
                    }
                    i = end + 1
                    continue
                }
            }
            append(currentText[i])
            i++
        }
    }
}

sealed class MarkdownSegment {
    data class TextBlock(val text: String) : MarkdownSegment()
    data class CodeBlock(val code: String, val language: String) : MarkdownSegment()
}

fun parseMarkdownSegments(markdown: String): List<MarkdownSegment> {
    val segments = mutableListOf<MarkdownSegment>()
    val codeFenceRegex = "```([a-zA-Z0-9_-]*)\\s*\\n([\\s\\S]*?)```".toRegex()

    var lastIndex = 0
    val matches = codeFenceRegex.findAll(markdown).toList()

    for (match in matches) {
        val range = match.range
        if (range.first > lastIndex) {
            val textBefore = markdown.substring(lastIndex, range.first).trim()
            if (textBefore.isNotEmpty()) {
                segments.add(MarkdownSegment.TextBlock(textBefore))
            }
        }
        val language = match.groupValues.getOrNull(1)?.trim() ?: ""
        val code = match.groupValues.getOrNull(2)?.trimEnd() ?: ""
        segments.add(MarkdownSegment.CodeBlock(code = code, language = language))
        lastIndex = range.last + 1
    }

    if (lastIndex < markdown.length) {
        val textAfter = markdown.substring(lastIndex).trim()
        if (textAfter.isNotEmpty()) {
            segments.add(MarkdownSegment.TextBlock(textAfter))
        }
    }

    if (segments.isEmpty() && markdown.isNotBlank()) {
        segments.add(MarkdownSegment.TextBlock(markdown))
    }

    return segments
}
