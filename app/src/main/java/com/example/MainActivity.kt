package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.RMBottomNavBar
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CalculatorView
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CodeAssistantView
import com.example.ui.screens.CreateScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LanguageHelperView
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.PhoneControlScreen
import com.example.ui.screens.QuickNotesView
import com.example.ui.screens.RemindersView
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.VoiceAssistantScreen
import com.example.ui.screens.WebSearchView
import com.example.ui.theme.RMAITheme
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.ScreenDestination
import com.example.util.FirebaseAppCheckUtil

class MainActivity : ComponentActivity() {
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAppCheckUtil.configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            val userProfile by chatViewModel.userProfile.collectAsStateWithLifecycle()
            val currentUser by chatViewModel.firebaseUser.collectAsStateWithLifecycle()
            val isGuestSession by chatViewModel.isGuestSession.collectAsStateWithLifecycle()

            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (userProfile?.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> true // Default to sleek Cyber Dark as shown in mockup
            }

            var splashDone by remember { mutableStateOf(false) }

            RMAITheme(darkTheme = isDarkTheme) {
                if (!splashDone) {
                    SplashScreen(
                        onSplashComplete = { splashDone = true }
                    )
                } else if (currentUser == null && !isGuestSession) {
                    AuthScreen(viewModel = chatViewModel)
                } else {
                    MainAppContent(
                        viewModel = chatViewModel,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: ChatViewModel,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()

    BackHandler(enabled = currentDestination != ScreenDestination.MAIN_TABS) {
        viewModel.navigateBack()
    }

    if (currentDestination == ScreenDestination.MAIN_TABS) {
        Scaffold(
            bottomBar = {
                RMBottomNavBar(
                    selectedTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            },
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                    when (tab) {
                        MainTab.HOME -> HomeScreen(viewModel = viewModel)
                        MainTab.CHAT -> ChatScreen(viewModel = viewModel, isDarkTheme = isDarkTheme)
                        MainTab.CREATE -> CreateScreen(viewModel = viewModel)
                        MainTab.TOOLS -> ToolsScreen(viewModel = viewModel)
                        MainTab.MORE -> SettingsScreen(viewModel = viewModel, onBack = { viewModel.selectTab(MainTab.HOME) })
                    }
                }
            }
        }
    } else {
        Crossfade(targetState = currentDestination, label = "dest_crossfade") { dest ->
            when (dest) {
                ScreenDestination.VOICE_ASSISTANT -> VoiceAssistantScreen(viewModel = viewModel)
                ScreenDestination.PHONE_CONTROL -> PhoneControlScreen(viewModel = viewModel)
                ScreenDestination.CAMERA_VIEW -> CameraScreen(viewModel = viewModel)
                ScreenDestination.GALLERY_VIEW -> GalleryScreen(viewModel = viewModel)
                ScreenDestination.MEMORY_VIEW -> MemoryScreen(viewModel = viewModel)
                ScreenDestination.SETTINGS_VIEW -> SettingsScreen(viewModel = viewModel, onBack = { viewModel.navigateBack() })
                ScreenDestination.WEB_SEARCH_VIEW -> WebSearchView(viewModel = viewModel)
                ScreenDestination.CODE_ASSISTANT_VIEW -> CodeAssistantView(viewModel = viewModel)
                ScreenDestination.LANGUAGE_HELPER_VIEW -> LanguageHelperView(viewModel = viewModel)
                ScreenDestination.CALCULATOR_VIEW -> CalculatorView(viewModel = viewModel)
                ScreenDestination.QUICK_NOTES_VIEW -> QuickNotesView(viewModel = viewModel)
                ScreenDestination.REMINDERS_VIEW -> RemindersView(viewModel = viewModel)
                ScreenDestination.MAIN_TABS -> { /* Handled in outer condition */ }
            }
        }
    }
}
