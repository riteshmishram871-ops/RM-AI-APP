package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
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

@Composable
fun CameraScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { viewModel.navigateBack() }

    var flashEnabled by remember { mutableStateOf(false) }
    var activeMode by remember { mutableStateOf("Photo") } // "Photo", "Video", "More"
    var isFrontCamera by remember { mutableStateOf(false) }
    var capturedPhotoUri by remember { mutableStateOf<String?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            capturedPhotoUri = uri.toString()
            showActionDialog = true
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Camera ready", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Camera permission needed to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Back, "Camera", Flash, Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.testTag("camera_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }

                Text(
                    text = "Camera",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )

                Row {
                    IconButton(onClick = { flashEnabled = !flashEnabled }) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (flashEnabled) NeonMint else TextWhite
                        )
                    }

                    IconButton(onClick = { viewModel.navigateTo(ScreenDestination.SETTINGS_VIEW) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextWhite.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Viewfinder Area matching Mockup Screen 6
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CyberCardElevated)
                    .border(1.dp, CyberBorder, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                // If user selected or captured an image, show it; otherwise show scenic alpine placeholder
                if (capturedPhotoUri != null) {
                    AsyncImage(
                        model = capturedPhotoUri,
                        contentDescription = "Captured Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.rm_ai_logo),
                        contentDescription = "Camera viewfinder",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(40.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Grid overlay lines
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                )

                // Prompt overlay on viewfinder
                Text(
                    text = if (isFrontCamera) "Front Camera (Active)" else "Rear AI Vision Lens (Active)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextWhite.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Camera Mode Selector: Photo | Video | More (matching mockup)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Photo", "Video", "More").forEach { mode ->
                    val isSelected = activeMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) NeonMint.copy(alpha = 0.16f) else Color.Transparent)
                            .clickable { activeMode = mode }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = mode,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) NeonMint else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shutter Controls Row matching Mockup Screen 6:
            // [Gallery Thumbnail]  ---  [Big White Circular Shutter]  ---  [Flip Camera]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Thumbnail Button
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCardElevated)
                        .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                        .testTag("camera_gallery_shortcut"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Open Gallery",
                        tint = TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Big White Circular Shutter Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(4.dp, PureWhite, CircleShape)
                        .padding(5.dp)
                        .clip(CircleShape)
                        .background(PureWhite)
                        .clickable {
                            // Check permission
                            val hasCam = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (!hasCam) {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                            // Set a mock captured photo and open AI dialog
                            capturedPhotoUri = "android.resource://${context.packageName}/${R.drawable.rm_ai_logo}"
                            showActionDialog = true
                        }
                        .testTag("camera_shutter_button")
                )

                // Front/Rear Camera Switch Button
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(CyberCardElevated)
                        .border(1.dp, CyberBorder, CircleShape)
                        .clickable { isFrontCamera = !isFrontCamera }
                        .testTag("camera_flip_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    // Action Dialog after Capture/Pick: "Ask AI about this image", "Describe", "Read text"
    if (showActionDialog) {
        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            containerColor = CyberCardBg,
            title = {
                Text(
                    text = "Analyze Image with RM AI",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose an AI action for your captured photo:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )

                    Button(
                        onClick = {
                            showActionDialog = false
                            viewModel.setAttachmentUri(capturedPhotoUri)
                            viewModel.selectTab(MainTab.CHAT)
                            viewModel.sendMessage("Describe everything visible in this photo.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔍 Describe Scene & Objects", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            showActionDialog = false
                            viewModel.setAttachmentUri(capturedPhotoUri)
                            viewModel.selectTab(MainTab.CHAT)
                            viewModel.sendMessage("Extract and transcribe any visible text in this image.")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📝 Read & Extract Text", color = TextWhite)
                    }

                    OutlinedButton(
                        onClick = {
                            showActionDialog = false
                            viewModel.setAttachmentUri(capturedPhotoUri)
                            viewModel.selectTab(MainTab.CHAT)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("💬 Ask Custom Question in Chat", color = TextWhite)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showActionDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}
