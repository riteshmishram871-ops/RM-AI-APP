package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.RMAITheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by chatViewModel.userProfile.collectAsStateWithLifecycle()
            val currentScreen by chatViewModel.currentScreen.collectAsStateWithLifecycle()

            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (userProfile?.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemDark
            }

            RMAITheme(darkTheme = isDarkTheme) {
                Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                    when (screen) {
                        AppScreen.AUTH -> {
                            AuthScreen(
                                viewModel = chatViewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        AppScreen.CHAT -> {
                            ChatScreen(
                                viewModel = chatViewModel,
                                isDarkTheme = isDarkTheme,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                viewModel = chatViewModel,
                                onBack = { chatViewModel.navigateTo(AppScreen.CHAT) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
