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

            CloesBackHandler(vm)

            // ── Base screens ──────────────────────────────────────────────────
            AnimatedContent(
                targetState = vm.currentScreen,
                transitionSpec = { fadeIn() + slideInVertically { it / 6 } togetherWith fadeOut() + slideOutVertically { -it / 6 } },
                label = "screen"
            ) { screen ->
                when (screen) {
                    "splash"  -> SplashScreen(vm)
                    "onboard" -> OnboardScreen(vm)
                    "main"    -> MainScreen(vm)
                    else      -> SplashScreen(vm)
                }
            }

            // ── Full-screen overlays ──────────────────────────────────────────
            if (vm.showVibeShorts)  VibeShorts(vm)
            if (vm.showProfileVibe) ProfileVibeOverlay(vm)
            if (vm.showEmergency)   EmergencyScreen(vm)
            if (vm.showCallScreen)  CallScreen(vm)
            if (vm.showSettings)    SettingsOverlay(vm)
            if (vm.showProfile)     ProfileOverlay(vm)

            // ── Side Panel ────────────────────────────────────────────────────
            AnimatedVisibility(visible = vm.showSidePanel,
                enter = slideInHorizontally { -it }, exit = slideOutHorizontally { -it }) {
                SidePanel(vm)
            }

            // ── Muse Create (animation studio) ───────────────────────────────
            AnimatedVisibility(visible = vm.showMuseCreate,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                when {
                    vm.showMuseCanvas         -> MuseCreateCanvasScreen(vm)
                    vm.showMuseCreateProjects -> MuseCreateProjectsScreen(vm)
                    else                      -> MuseCreateLandingScreen(vm)
                }
            }

            // ── Muse Clothing ─────────────────────────────────────────────────
            AnimatedVisibility(visible = vm.showMuseClothing,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                MuseClothingScreen(vm)
            }

            // ── Muse History ──────────────────────────────────────────────────
            AnimatedVisibility(visible = vm.showMuseHistory,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                MuseHistoryScreen(vm)
            }

            // ── Audio Extractor ───────────────────────────────────────────────
            AnimatedVisibility(visible = vm.showAudioExtractor,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                AudioExtractorScreen(vm)
            }

            // ── Cloes Echo ────────────────────────────────────────────────────
            AnimatedVisibility(visible = vm.showCloesEcho,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                CloesEchoScreen(vm)
            }

            // ── Auth ──────────────────────────────────────────────────────────
            AnimatedVisibility(visible = vm.showSignupPage || vm.showLoginPage,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                SignUpScreen(vm)
            }

            // ── Muse Task ─────────────────────────────────────────────────────
            AnimatedVisibility(visible = vm.showMuseTask,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                MuseTaskScreen(vm)
            }

            // ── Shared Spaces ─────────────────────────────────────────────────
            AnimatedVisibility(visible = vm.showSharedSpace,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                SharedSpaceScreen(vm)
            }

            // ── Unsent Drawer ─────────────────────────────────────────────────
            if (vm.showUnsentDrawer) {
                UnsentDrawerScreen(vm, vm.currentChatId ?: vm.contacts.firstOrNull()?.id ?: 0L)
            }

            // ── Seasonal Friendship Card ──────────────────────────────────────
            if (vm.showSeasonCard && vm.currentSeason != null) {
                SeasonalFriendshipCard(vm = vm, season = vm.currentSeason!!,
                    onDismiss = { vm.showSeasonCard = false; vm.currentSeason = null })
            }

            // ── Voice Fingerprint ─────────────────────────────────────────────
            AnimatedVisibility(visible = vm.showVoiceFingerprint,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                VoiceFingerprintScreen(vm)
            }

            // ── Muse Voice Reply ──────────────────────────────────────────────
            if (vm.showMuseVoiceReply) MuseVoiceReplyOverlay(vm)

            // ── AnimaForge WebView ────────────────────────────────────────
            AnimatedVisibility(visible = vm.showAnimaForge,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                AnimaForgeScreen(vm)
            }

            // ── NEW: MEET PAGE ────────────────────────────────────────────────
            AnimatedVisibility(visible = vm.showMeetPage,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                MeetPage(vm)
            }

            // ── NEW: BLOOM HISTORY ────────────────────────────────────────────
            AnimatedVisibility(visible = vm.showBloomHistory,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                BloomHistoryScreen(vm)
            }

            // ── NEW: BLOOM RITUAL ─────────────────────────────────────────────
            if (vm.showBloomRitual && vm.bloomRitualContact != null) BloomRitualDialog(vm)

            // ── NEW: MOOD-AWARE MESSAGE ───────────────────────────────────────
            if (vm.showMoodMessageDialog) MoodMessageDialog(vm)

            // ── NEW: VIBE VISIBILITY SETTINGS ─────────────────────────────────
            AnimatedVisibility(visible = vm.showVibeVisibilitySettings,
                enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
                VibeVisibilitySettingsScreen(vm)
            }

            // ── Dialogs ───────────────────────────────────────────────────────
            if (vm.showLogoutDialog)         LogoutDialog(vm)
            if (vm.showDeleteAccountDialog)  DeleteAccountDialog(vm)

            // ── Modal sheets ──────────────────────────────────────────────────
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
            if (vm.showCoinGift && vm.coinGiftContact != null) CoinGiftSheet(vm)
        }
    }
}
