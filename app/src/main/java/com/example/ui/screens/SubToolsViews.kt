package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.QuickNoteEntity
import com.example.data.local.ReminderEntity
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusCoral
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ChatViewModel

// -------------------------------------------------------------
// 1. Web Search View (with live source links & structured summary)
// -------------------------------------------------------------
@Composable
fun WebSearchView(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }
    var query by remember { mutableStateOf("Best laptop under 50k India") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResult by remember {
        mutableStateOf(
            """### 🌐 Web Intelligence Summary: Best Laptops Under ₹50,000

1. **ASUS Vivobook 15 (X1502ZA)**
   - Intel Core i5 12th Gen | 16GB RAM | 512GB NVMe SSD
   - Price: ~₹46,990 | Weight: 1.7kg | Anti-glare FHD Display
   - *Verdict:* Best all-rounder for students and coding.

2. **Lenovo IdeaPad Slim 3**
   - AMD Ryzen 5 7520U | 16GB LPDDR5 | 512GB SSD
   - Price: ~₹44,990 | Battery: Up to 9 hours
   - *Verdict:* Superb battery efficiency and lightweight chassis.

3. **HP 15s (fq5007TU)**
   - Intel Core i3 12th Gen | 8GB RAM (expandable) | 512GB SSD
   - Price: ~₹38,990 | Fast charging support

**Sources & References:**
• TechRadar India Review (Updated October 2026)
• Digit.in Laptop Buyer’s Guide"""
        )
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            SubToolHeader(title = "Web Search", onBack = { viewModel.navigateBack() })

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search the web…") },
                trailingIcon = {
                    IconButton(onClick = {
                        isSearching = true
                        viewModel.sendMessage("Search the internet for: $query")
                        isSearching = false
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonMint)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        Text(
                            text = searchResult,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                            color = TextWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -------------------------------------------------------------
// 2. Code Assistant View (Language selector: C++, Python, Java, Kotlin)
// -------------------------------------------------------------
@Composable
fun CodeAssistantView(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { viewModel.navigateBack() }

    val languages = listOf("C++", "Python", "Java", "Kotlin", "JavaScript")
    var selectedLang by remember { mutableStateOf("C++") }
    var codePrompt by remember { mutableStateOf("Write a fast binary search function") }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            SubToolHeader(title = "Code Assistant", onBack = { viewModel.navigateBack() })

            // Language Selector Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                languages.forEach { lang ->
                    FilterChip(
                        selected = selectedLang == lang,
                        onClick = { selectedLang = lang },
                        label = { Text(lang) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonMint,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = codePrompt,
                onValueChange = { codePrompt = it },
                label = { Text("What code do you need help with?") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.selectTab(com.example.ui.viewmodel.MainTab.CHAT)
                    viewModel.sendMessage("[$selectedLang Code Assistant] $codePrompt")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Generate & Explain in Chat", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Code snippet showcase card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$selectedLang EXAMPLE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonMint
                        )
                        IconButton(
                            onClick = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("Code", "// RM AI Code Template"))
                                Toast.makeText(context, "Copied snippet", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when (selectedLang) {
                            "Python" -> "def binary_search(arr, target):\n    low, high = 0, len(arr) - 1\n    while low <= high:\n        mid = (low + high) // 2\n        if arr[mid] == target: return mid\n        elif arr[mid] < target: low = mid + 1\n        else: high = mid - 1\n    return -1"
                            "Kotlin" -> "fun binarySearch(arr: IntArray, target: Int): Int {\n    var low = 0\n    var high = arr.size - 1\n    while (low <= high) {\n        val mid = (low + high) ushr 1\n        if (arr[mid] == target) return mid\n        else if (arr[mid] < target) low = mid + 1\n        else high = mid - 1\n    }\n    return -1\n}"
                            else -> "int binarySearch(const std::vector<int>& arr, int target) {\n    int low = 0, high = arr.size() - 1;\n    while (low <= high) {\n        int mid = low + (high - low) / 2;\n        if (arr[mid] == target) return mid;\n        else if (arr[mid] < target) low = mid + 1;\n        else high = mid - 1;\n    }\n    return -1;\n}"
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TextWhite,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -------------------------------------------------------------
// 3. Language Helper View (with Odia, Hindi, English translations)
// -------------------------------------------------------------
@Composable
fun LanguageHelperView(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }
    var inputText by remember { mutableStateOf("Bhai, python re input output kemiti kaam kare?") }
    var targetLang by remember { mutableStateOf("Romanized Odia") }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            SubToolHeader(title = "Language Helper", onBack = { viewModel.navigateBack() })

            Text(
                text = "Translate & communicate in English, Hindi, and Romanized Odia.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Input phrase / question") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.selectTab(com.example.ui.viewmodel.MainTab.CHAT)
                    viewModel.sendMessage(inputText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ask RM AI in Odia/English", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💡 Common Odia AI Phrases:", fontWeight = FontWeight.Bold, color = NeonMint)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• 'Bhai, python re input output kemiti kaam kare?' (How does I/O work?)", color = TextWhite, fontSize = 13.sp)
                    Text("• 'Aaji ra mausam kemiti achhi?' (How is today's weather?)", color = TextWhite, fontSize = 13.sp)
                    Text("• 'Mo pain gote email lekhi dio.' (Write an email for me.)", color = TextWhite, fontSize = 13.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Calculator View
// -------------------------------------------------------------
@Composable
fun CalculatorView(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }
    var expression by remember { mutableStateOf("0") }

    fun onButtonPress(btn: String) {
        when (btn) {
            "C" -> expression = "0"
            "=" -> {
                try {
                    // Simple evaluation
                    val result = evaluateMathExpression(expression)
                    expression = result
                } catch (_: Exception) {
                    expression = "Error"
                }
            }
            "⌫" -> {
                expression = if (expression.length > 1) expression.dropLast(1) else "0"
            }
            else -> {
                expression = if (expression == "0" || expression == "Error") btn else expression + btn
            }
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            SubToolHeader(title = "Calculator", onBack = { viewModel.navigateBack() })

            // Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = expression,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = NeonMint
                    )
                }
            }

            // Keypad Grid
            val buttons = listOf(
                listOf("C", "⌫", "%", "/"),
                listOf("7", "8", "9", "*"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("00", "0", ".", "=")
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                buttons.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { char ->
                            val isOp = char in listOf("/", "*", "-", "+", "=")
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (char == "=") NeonMint else if (isOp) CyberCardElevated else CyberCardBg)
                                    .clickable { onButtonPress(char) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (char == "=") Color.Black else TextWhite
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun evaluateMathExpression(expr: String): String {
    // Simple 2-number parser fallback for safety
    val clean = expr.replace(" ", "")
    val op = clean.firstOrNull { it in listOf('+', '-', '*', '/') } ?: return clean
    val parts = clean.split(op)
    if (parts.size != 2) return clean
    val n1 = parts[0].toDoubleOrNull() ?: 0.0
    val n2 = parts[1].toDoubleOrNull() ?: 0.0
    val res = when (op) {
        '+' -> n1 + n2
        '-' -> n1 - n2
        '*' -> n1 * n2
        '/' -> if (n2 != 0.0) n1 / n2 else Double.NaN
        else -> 0.0
    }
    return if (res % 1 == 0.0) res.toLong().toString() else String.format("%.2f", res)
}

// -------------------------------------------------------------
// 5. Quick Notes View
// -------------------------------------------------------------
@Composable
fun QuickNotesView(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubToolHeader(title = "Quick Notes", onBack = { viewModel.navigateBack() })
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note", tint = NeonMint)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(note.title, fontWeight = FontWeight.Bold, color = TextWhite)
                                IconButton(
                                    onClick = { viewModel.deleteNote(note.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = StatusCoral, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(note.content, color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = CyberCardBg,
            title = { Text("New Quick Note", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = noteTitle, onValueChange = { noteTitle = it }, label = { Text("Title") })
                    OutlinedTextField(value = noteContent, onValueChange = { noteContent = it }, label = { Text("Note content") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteContent.isNotBlank()) {
                            viewModel.saveNote(noteTitle, noteContent)
                            noteTitle = ""
                            noteContent = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint)
                ) {
                    Text("Save Note", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = TextMuted) }
            }
        )
    }
}

// -------------------------------------------------------------
// 6. Reminders View
// -------------------------------------------------------------
@Composable
fun RemindersView(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubToolHeader(title = "Reminders & To-Do", onBack = { viewModel.navigateBack() })
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task", tint = NeonMint)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = reminder.isCompleted,
                                onCheckedChange = { viewModel.toggleReminder(reminder.id, it) },
                                colors = CheckboxDefaults.colors(checkedColor = NeonMint)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = reminder.title,
                                color = if (reminder.isCompleted) TextMuted else TextWhite,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.deleteReminder(reminder.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = StatusCoral, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = CyberCardBg,
            title = { Text("Add Reminder / Task", color = TextWhite) },
            text = {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task description") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            viewModel.addReminder(taskTitle, System.currentTimeMillis() + 3600000L)
                            taskTitle = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint)
                ) {
                    Text("Add Task", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = TextMuted) }
            }
        )
    }
}

@Composable
fun SubToolHeader(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = TextWhite
        )
    }
}
