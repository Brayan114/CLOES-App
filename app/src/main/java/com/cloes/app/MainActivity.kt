package com.cloes.app

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

// ── Back Handler composable — handles Android back button app-wide ─────────────
@Composable
fun CloesBackHandler(vm: AppViewModel) {
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val callback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    // Side panel / feature screens — close them
                    vm.showSidePanel        -> vm.showSidePanel = false
                    vm.showMuseDraw         -> vm.showMuseDraw = false
                    vm.showMuseClothing     -> vm.showMuseClothing = false
                    vm.showMuseHistory      -> vm.showMuseHistory = false
                    vm.showAudioExtractor   -> vm.showAudioExtractor = false
                    vm.showCloesEcho        -> vm.showCloesEcho = false
                    vm.showSignupPage       -> vm.showSignupPage = false
                    vm.showLoginPage        -> vm.showLoginPage = false
                    vm.showMuseTask         -> vm.showMuseTask = false
                    // Dialogs
                    vm.showLogoutDialog     -> vm.showLogoutDialog = false
                    vm.showDeleteAccountDialog -> vm.showDeleteAccountDialog = false
                    vm.showSettings         -> vm.showSettings = false
                    vm.showEditContact      -> vm.showEditContact = false
                    vm.showEmergency        -> vm.showEmergency = false
                    vm.showCallScreen       -> vm.showCallScreen = false
                    vm.showVibeShorts       -> vm.showVibeShorts = false
                    vm.showProfileVibe      -> vm.showProfileVibe = false
                    // Chat views — close to main
                    vm.showGroupChatView    -> vm.showGroupChatView = false
                    vm.currentChatId != null -> vm.closeChat()
                    // Bottom tabs — if NOT on messages, go back to messages
                    vm.currentTab != "chats" -> vm.currentTab = "chats"
                    // On Messages tab: let the system handle (exit app)
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
