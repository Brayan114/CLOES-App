package com.cloes.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cloes.app.CloesBackHandler
import com.cloes.app.ui.screens.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel

@Composable
fun CloesApp() {
    val vm: AppViewModel = viewModel()
    CompositionLocalProvider(
        LocalCloesColors provides vm.themeColors,
        LocalCloesFont   provides vm.appFontFamily
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // ── Back button handling ──────────────────────────────────────────
            CloesBackHandler(vm)

            // ── Base screens ──────────────────────────────────────────────────
            AnimatedContent(
                targetState = vm.currentScreen,
                transitionSpec = {
                    fadeIn() + slideInVertically { it / 6 } togetherWith
                    fadeOut() + slideOutVertically { -it / 6 }
                },
                label = "screen"
            ) { screen ->
                when (screen) {
                    "splash"  -> SplashScreen(vm)
                    "onboard" -> OnboardScreen(vm)
                    "main"    -> MainScreen(vm)
                    else      -> SplashScreen(vm)
                }
            }

            // ── Vibe / Emergency / Call (full screen) ─────────────────────────
            if (vm.showVibeShorts)  VibeShorts(vm)
            if (vm.showProfileVibe) ProfileVibeOverlay(vm)
            if (vm.showEmergency)   EmergencyScreen(vm)
            if (vm.showCallScreen)  CallScreen(vm)

            // ── Settings / Profile overlays ───────────────────────────────────
            if (vm.showSettings)    SettingsOverlay(vm)
            // EditProfileOverlay is embedded inside SettingsOverlay (via activeSection)
            if (vm.showProfile)     ProfileOverlay(vm)

            // ── Side Panel + Feature Screens ──────────────────────────────────
            AnimatedVisibility(visible = vm.showSidePanel,
                enter = slideInHorizontally { -it }, exit = slideOutHorizontally { -it }) {
                SidePanel(vm)
            }
            AnimatedVisibility(visible = vm.showMuseDraw,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                MuseDrawScreen(vm)
            }
            AnimatedVisibility(visible = vm.showMuseClothing,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                MuseClothingScreen(vm)
            }
            AnimatedVisibility(visible = vm.showMuseHistory,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                MuseHistoryScreen(vm)
            }
            AnimatedVisibility(visible = vm.showAudioExtractor,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                AudioExtractorScreen(vm)
            }
            AnimatedVisibility(visible = vm.showCloesEcho,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                CloesEchoScreen(vm)
            }
            AnimatedVisibility(visible = vm.showSignupPage || vm.showLoginPage,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                SignUpScreen(vm)
            }
            AnimatedVisibility(visible = vm.showMuseTask,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                MuseTaskScreen(vm)
            }

            // ── Dialogs ───────────────────────────────────────────────────────
            if (vm.showLogoutDialog)         LogoutDialog(vm)
            if (vm.showDeleteAccountDialog)  DeleteAccountDialog(vm)

            // ── Modal bottom sheets ───────────────────────────────────────────
            if (vm.showChatMenu)          ChatMenuSheet(vm)
            if (vm.showThemeModal)        ThemeSheet(vm)
            if (vm.showDisappearModal)    DisappearSheet(vm)
            if (vm.showPollModal)         PollSheet(vm)
            if (vm.showFileModal)         FileShareSheet(vm)
            if (vm.showAddContact)        AddContactSheet(vm)
            if (vm.showAddGroup)          AddGroupSheet(vm)
            if (vm.showAddGroupChat)      AddGroupChatSheet(vm)
            if (vm.showShareModal)        ShareSheet(vm)
            if (vm.showUploadVideo)       UploadVideoSheet(vm)
            if (vm.showSetCloesedKey)     CloesedKeySetupDialog(vm)
            if (vm.showFontPicker)        FontPickerSheet(vm)
            if (vm.showCircleMessage)     CircleMessageSheet(vm)
            if (vm.showCreateGroupChat)   CreateGroupChatSheet(vm)
            if (vm.showDeleteContact)     DeleteContactDialog(vm)
            if (vm.showLeaveGroup)        LeaveGroupDialog(vm)
            if (vm.showDeleteBubble)      DeleteBubbleDialog(vm)
            if (vm.showEditBubble)        EditBubbleDialog(vm)
            if (vm.showCleanChat)         CleanChatDialog(vm)
            if (vm.showGroupChatMenu)     GroupChatMenuSheet(vm)
            if (vm.showEditGroup)         EditGroupSheet(vm)
        }
    }
}
