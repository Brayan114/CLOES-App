package com.cloes.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cloes.app.ui.CloesApp
import com.cloes.app.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    // Cap font scale so large system fonts don't break layouts
    override fun attachBaseContext(newBase: android.content.Context) {
        val config = Configuration(newBase.resources.configuration)
        if (config.fontScale > 1.2f) config.fontScale = 1.2f
        val ctx = newBase.createConfigurationContext(config)
        super.attachBaseContext(ctx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CloesApp()
                }
            }
        }
    }
}

@Composable
fun CloesBackHandler(vm: AppViewModel) {
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val callback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    vm.showAnimaForge            -> vm.showAnimaForge = false
                    vm.showSidePanel             -> vm.showSidePanel = false
                    vm.showMuseCreate            -> { vm.showMuseCanvas = false; vm.showMuseCreateProjects = false; vm.showMuseCreate = false }
                    vm.showMuseClothing          -> vm.showMuseClothing = false
                    vm.showMuseHistory           -> vm.showMuseHistory = false
                    vm.showAudioExtractor        -> vm.showAudioExtractor = false
                    vm.showCloesEcho             -> vm.showCloesEcho = false
                    vm.showSignupPage            -> vm.showSignupPage = false
                    vm.showLoginPage             -> vm.showLoginPage = false
                    vm.showMuseTask              -> vm.showMuseTask = false
                    vm.showSharedSpace           -> vm.showSharedSpace = false
                    vm.showUnsentDrawer          -> vm.showUnsentDrawer = false
                    vm.showVoiceFingerprint      -> vm.showVoiceFingerprint = false
                    vm.showMeetPage              -> vm.showMeetPage = false
                    vm.showBloomHistory          -> vm.showBloomHistory = false
                    vm.showVibeVisibilitySettings -> vm.showVibeVisibilitySettings = false
                    vm.showLogoutDialog          -> vm.showLogoutDialog = false
                    vm.showDeleteAccountDialog   -> vm.showDeleteAccountDialog = false
                    vm.showSettings              -> vm.showSettings = false
                    vm.showEditContact           -> vm.showEditContact = false
                    vm.showEmergency             -> vm.showEmergency = false
                    vm.showCallScreen            -> vm.showCallScreen = false
                    vm.showVibeShorts            -> vm.showVibeShorts = false
                    vm.showProfileVibe           -> vm.showProfileVibe = false
                    vm.showGroupChatView         -> vm.showGroupChatView = false
                    vm.showBloomRitual           -> vm.showBloomRitual = false
                    vm.showMoodMessageDialog     -> vm.showMoodMessageDialog = false
                    vm.currentChatId != null     -> vm.closeChat()
                    vm.currentTab != "chats"     -> vm.currentTab = "chats"
                    else -> {
                        isEnabled = false
                        dispatcher?.onBackPressed()
                        isEnabled = true
                    }
                }
            }
        }
    }
    DisposableEffect(dispatcher) {
        dispatcher?.addCallback(callback)
        onDispose { callback.remove() }
    }
}
