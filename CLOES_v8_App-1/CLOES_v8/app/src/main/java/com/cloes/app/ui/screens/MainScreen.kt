package com.cloes.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cloes.app.R
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun MainScreen(vm: AppViewModel) {
    val c = vm.themeColors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        when {
                            // RIGHT swipe: if side panel closed → cycle prev tab; if panel open → close it
                            totalDrag > 70f && !vm.showSidePanel -> vm.swipeNavPrev()
                            totalDrag > 70f && vm.showSidePanel  -> vm.showSidePanel = false
                            // LEFT swipe: if panel closed → cycle next tab; don't close panel on left
                            totalDrag < -70f && !vm.showSidePanel -> vm.swipeNavNext()
                            totalDrag < -70f && vm.showSidePanel  -> vm.showSidePanel = false
                        }
                    },
                    onHorizontalDrag = { _, amount -> totalDrag += amount }
                )
            }
    ) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())

        if (vm.urgencyTintOn) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEF4444).copy(0.07f)))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.surface2)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Hamburger — opens side panel
                NeuIconButton(
                    icon = { Icon(Icons.Default.Menu, "Menu", tint = c.textMid, modifier = Modifier.size(20.dp)) },
                    onClick = { vm.showSidePanel = true }
                )
                // CLOES Logo — uses crystal C image
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cloes_logo),
                        contentDescription = "CLOES",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    "CLOES",
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(listOf(Pink, Purple, Cyan)),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                // Tab indicator pills — show current position in swipe cycle
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    vm.navTabOrder.forEach { tab ->
                        Box(
                            modifier = Modifier
                                .size(if (vm.currentTab == tab) 8.dp else 5.dp)
                                .clip(CircleShape)
                                .background(if (vm.currentTab == tab) Purple else c.border)
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ghost mode indicator
                    if (vm.ghostModeEnabled) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Purple.copy(0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👻", fontSize = 14.sp)
                        }
                    }
                    // Emergency
                    NeuIconButton(
                        icon = { Icon(Icons.Default.Warning, "Emergency", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) },
                        onClick = { vm.showEmergency = true }
                    )
                    // Profile
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { vm.showSettings = true }
                    ) {
                        FragmentArt(
                            palette = vm.profile.palette.ifEmpty { listOf(Pink, Purple, Cyan) },
                            seed = vm.profile.lightSeed,
                            animating = false
                        )
                    }
                }
            }

            // ── Page Content ─────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = vm.currentTab,
                    transitionSpec = {
                        slideInHorizontally { it / 3 } + fadeIn() togetherWith
                        slideOutHorizontally { -it / 3 } + fadeOut()
                    },
                    label = "tab"
                ) { tab ->
                    when (tab) {
                        "chats"  -> ChatsPage(vm)
                        "bloom"  -> BloomPage(vm)
                        "groups" -> GroupsPage(vm)
                        "muse"   -> MuseAIPage(vm)
                        "vibe"   -> VibePage(vm)
                        "coins"  -> CoinsPage(vm)
                        "meet"   -> MeetPageTab(vm)
                        else     -> ChatsPage(vm)
                    }
                }
            }

            // ── Bottom Nav ─────────────────────────────────────────────
            if (vm.navBarVisible) {
                BottomNavBar(vm = vm, c = c, onHide = {
                    if (vm.disappearingNavEnabled) vm.navBarVisible = false
                })
            }
        }

        // Arrow to restore nav when hidden
        if (!vm.navBarVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Purple.copy(0.85f))
                        .clickable { vm.navBarVisible = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Show Nav", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        // ── Chat views ────────────────────────────────────────────────────
        AnimatedVisibility(visible = vm.currentChatId != null,
            enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
            ChatView(vm)
        }
        AnimatedVisibility(visible = vm.showGroupChatView,
            enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
            GroupChatView(vm)
        }

        // ── Settings ──────────────────────────────────────────────────────
        AnimatedVisibility(visible = vm.showSettings,
            enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
            SettingsOverlay(vm)
        }
        AnimatedVisibility(visible = vm.showEditContact,
            enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
            EditContactOverlay(vm)
        }

        // ── Bloom Ritual nudge ────────────────────────────────────────────
        if (vm.showBloomRitual && vm.bloomRitualContact != null) {
            BloomRitualNudge(vm = vm, contact = vm.bloomRitualContact!!)
        }

        // ── Toast ─────────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)) {
            CloesToast(
                visible = vm.toastVisible,
                name = vm.toastName,
                msg = vm.toastMsg,
                urgency = vm.toastUrgency,
                paletteIndex = vm.contacts.find { it.name.contains(vm.toastName.split("·")[0]) }
                    ?.paletteIndex ?: 0,
                onDismiss = { vm.toastVisible = false },
                onClick = {
                    val contact = vm.contacts.find { it.name.contains(vm.toastName.split("·")[0]) }
                    if (contact != null) vm.openChat(contact.id)
                    vm.toastVisible = false
                }
            )
        }

        if (vm.toastVisible) {
            LaunchedEffect(vm.toastVisible, vm.toastMsg) {
                delay(3600)
                vm.toastVisible = false
            }
        }

        // ── Demo toasts ───────────────────────────────────────────────────
        LaunchedEffect(Unit) {
            delay(2000)
            vm.showIncomingToast("Nova·△7", "need to talk to you NOW", "high")
            delay(6000)
            vm.showIncomingToast("Mum", "Call me when you get this", "low")
        }

        // ── Muse Bloom Ritual: show 3 min after sign-in, then every 6 min ─
        LaunchedEffect(Unit) {
            delay(3 * 60 * 1000L) // 3 minutes after sign-in
            vm.checkBloomRituals()
            while (true) {
                delay(6 * 60 * 1000L) // repeat every 6 minutes
                vm.checkBloomRituals()
            }
        }
    }
}

// ── Bloom Ritual Nudge ──────────────────────────────────────────────────────
@Composable
private fun BloomRitualNudge(vm: AppViewModel, contact: com.cloes.app.data.Contact) {
    val c = vm.themeColors
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.55f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🌸", fontSize = 28.sp)
                    Column {
                        Text("Bloom Ritual", color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Muse noticed something ✦", color = c.textSub, fontSize = 12.sp)
                    }
                }
                Text(
                    "Your connection with ${contact.name} has been fading. Their Bloom is at ${contact.bloomScore}. Want to send a quick voice note?",
                    color = c.textMid, fontSize = 14.sp, lineHeight = 20.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GradientButton(
                        text = "Send a note ✦",
                        onClick = {
                            vm.showBloomRitual = false
                            vm.openChat(contact.id)
                            vm.showToast(contact.name, "Tap the mic to send a voice note 🎙", "low")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = { vm.showBloomRitual = false }) {
                        Text("Later", color = c.textSub, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ─── Bottom Navigation Bar ─────────────────────────────────────────────────────
private data class NavTab(val id: String, val label: String, val icon: ImageVector, val iconSelected: ImageVector)

@Composable
private fun BottomNavBar(vm: AppViewModel, c: CloesColors, onHide: () -> Unit = {}) {
    LaunchedEffect(vm.currentTab, vm.navBarVisible) {
        if (vm.disappearingNavEnabled && vm.navBarVisible) { delay(3000); onHide() }
    }
    val tabs = listOf(
        NavTab("chats",  "Messages", Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble),
        NavTab("bloom",  "Bloom",    Icons.Outlined.Favorite,          Icons.Filled.Favorite),
        NavTab("groups", "Circles",  Icons.Outlined.Groups,            Icons.Filled.Groups),
        NavTab("muse",   "Muse",     Icons.Outlined.AutoAwesome,       Icons.Filled.AutoAwesome),
        NavTab("vibe",   "Vibe",     Icons.Outlined.PlayCircleOutline,  Icons.Filled.PlayCircle),
        NavTab("coins",  "Coins",    Icons.Outlined.MonetizationOn,    Icons.Filled.MonetizationOn)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface2)
            .border(0.5.dp, c.border, RoundedCornerShape(0.dp))
            .navigationBarsPadding()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val isActive = vm.currentTab == tab.id
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isActive) Brush.linearGradient(listOf(Pink.copy(0.09f), Purple.copy(0.09f)))
                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable { vm.currentTab = tab.id },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 6.dp)
                ) {
                    Box {
                        Icon(
                            if (isActive) tab.iconSelected else tab.icon,
                            contentDescription = tab.label,
                            tint = if (isActive) Purple else c.textSub,
                            modifier = Modifier.size(if (isActive) 22.dp else 20.dp)
                        )
                        if (tab.id == "chats" && vm.totalUnread() > 0) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-3).dp)
                                    .clip(CircleShape)
                                    .background(Pink)
                                    .border(1.5.dp, c.bg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${vm.totalUnread()}", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        tab.label,
                        fontSize = 7.sp, letterSpacing = 0.3.sp,
                        color = if (isActive) Purple else c.textSub,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
