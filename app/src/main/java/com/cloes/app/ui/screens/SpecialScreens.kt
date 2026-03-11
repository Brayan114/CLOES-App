package com.cloes.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cloes.app.data.*
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════
//   VIBE SHORTS (TikTok/Reels style player)
// ══════════════════════════════════════════════════════════
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VibeShorts(vm: AppViewModel) {
    val videos = vm.vibeVideos
    if (videos.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = vm.vibeShortStartIdx.coerceIn(0, videos.size - 1),
        pageCount = { videos.size }
    )
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val video = videos[page]
            VibeShortItem(vm = vm, video = video, idx = page, isActive = page == pagerState.currentPage)
        }

        // Close button
        Box(
            modifier = Modifier
                .padding(top = 52.dp, start = 16.dp)
                .align(Alignment.TopStart)
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(0.4f))
                .clickable { vm.showVibeShorts = false },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun VibeShortItem(vm: AppViewModel, video: VibeVideo, idx: Int, isActive: Boolean) {
    val c = vm.themeColors
    var isPlaying by remember { mutableStateOf(true) }
    var liked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Progress animation
    val progress = remember { Animatable(0f) }
    LaunchedEffect(isActive, isPlaying) {
        if (isActive && isPlaying) {
            progress.animateTo(1f, animationSpec = tween(15000, easing = LinearEasing))
        } else if (!isActive) {
            progress.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { isPlaying = !isPlaying }
    ) {
        // Background: Fragment art
        FragmentArt(
            palette = video.paletteColors.ifEmpty { PALETTES[idx % PALETTES.size] },
            seed = idx * 0.37f + 0.05f,
            animating = isActive && isPlaying,
            modifier = Modifier.fillMaxSize()
        )

        // Dark overlay gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(0.7f)
                        )
                    )
                )
        )

        // Play/pause indicator
        if (!isPlaying) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(0.4f))
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Text("▶", color = Color.White, fontSize = 28.sp)
            }
        }

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.TopCenter)
                .background(Color.White.copy(0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.value)
                    .background(Brush.horizontalGradient(listOf(Pink, Purple)))
            )
        }

        // Bottom info overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp, start = 14.dp, end = 60.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Creator info
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable {
                        vm.profileVibeCreator = video.creator
                        vm.showVibeShorts = false
                        vm.showProfileVibe = true
                    }
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp))
                    ) {
                        FragmentAvatar(paletteIndex = idx % PALETTES.size, seed = idx * 0.23f)
                    }
                    Text(video.creator, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                            .clickable { vm.showToast(video.creator, "Following! ✦", "low") }
                    ) {
                        Text("Follow", color = Color.White, fontSize = 11.sp)
                    }
                }
                Text(video.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text("${video.category} • ${video.duration}",
                    color = Color.White.copy(0.7f), fontSize = 12.sp)
            }
        }

        // Action buttons column (right side)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp, bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Creator avatar
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape)
                    .border(2.dp, Pink, CircleShape)
            ) {
                FragmentAvatar(paletteIndex = idx % PALETTES.size, seed = idx * 0.31f)
            }

            VibeActionBtn("${if (liked) "💜" else "🤍"}\n${video.likes + if (liked) 1 else 0}") {
                liked = !liked
                if (liked) vm.likeVibe(video.id)
            }
            VibeActionBtn("💬\n${video.comments.size}") {
                if (video.comments.isNotEmpty())
                    vm.showToast(video.creator, video.comments.random(), "low")
            }
            VibeActionBtn("📤\nShare") { vm.showToast("Vibe", "Shared ✦", "low") }
            VibeActionBtn("🔖\nSave") { vm.showToast("Vibe", "Saved ✦", "low") }
            VibeActionBtn("⋯\nMore") { vm.showToast("Vibe", "More options", "low") }
        }
    }
}

@Composable
private fun VibeActionBtn(label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        label.split("\n").forEachIndexed { idx, line ->
            Text(
                line,
                color = Color.White,
                fontSize = if (idx == 0) 22.sp else 11.sp,
                fontWeight = if (idx == 1) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
//   PROFILE VIBE OVERLAY
// ══════════════════════════════════════════════════════════
@Composable
fun ProfileVibeOverlay(vm: AppViewModel) {
    val c = vm.themeColors
    val creatorName = vm.profileVibeCreator
    val contact = vm.contacts.find { it.name == creatorName }
    val videos = vm.vibeVideos.filter { it.creator == creatorName }
        .ifEmpty { vm.vibeVideos.take(3) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                FragmentArt(
                    palette = contact?.let { PALETTES[it.paletteIndex % PALETTES.size] }
                        ?: listOf(Pink, Purple, Cyan),
                    seed = 0.6f, animating = true
                )
            }
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.45f)))
            NeuIconButton(
                icon = { Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                onClick = { vm.showProfileVibe = false },
                modifier = Modifier.padding(top = 52.dp, start = 16.dp).align(Alignment.TopStart)
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))) {
                    contact?.let { FragmentAvatar(paletteIndex = it.paletteIndex, seed = fragSeed(it.id)) }
                        ?: FragmentArt(palette = listOf(Pink, Purple, Cyan), seed = 0.5f)
                }
                Text(creatorName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    listOf("${videos.size} Videos", "4.2K Followers", "892 Following").forEach { stat ->
                        Text(stat, color = Color.White.copy(0.8f), fontSize = 12.sp)
                    }
                }
            }
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GradientButton("Follow ✦", {
                vm.showToast(creatorName, "Following $creatorName ✦", "low")
            }, modifier = Modifier.weight(1f))
            OutlineButton("Message", {
                contact?.let { vm.openChat(it.id) }
                vm.showProfileVibe = false
            }, modifier = Modifier.weight(1f))
        }

        // Video grid
        SectionLabel("Videos", modifier = Modifier.padding(horizontal = 16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.height(400.dp)
        ) {
            items(videos) { video ->
                val idx = vm.vibeVideos.indexOf(video).coerceAtLeast(0)
                Box(
                    modifier = Modifier
                        .aspectRatio(0.7f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            vm.showProfileVibe = false
                            vm.openVibeShorts(idx)
                        },
                    contentAlignment = Alignment.BottomStart
                ) {
                    FragmentArt(
                        palette = video.paletteColors.ifEmpty { PALETTES[idx % PALETTES.size] },
                        seed = idx * 0.41f
                    )
                    Text(
                        video.duration,
                        color = Color.White, fontSize = 10.sp,
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(0.5f))
                            .padding(horizontal = 2.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ══════════════════════════════════════════════════════════
//   EMERGENCY SCREEN
// ══════════════════════════════════════════════════════════
@Composable
fun EmergencyScreen(vm: AppViewModel) {
    var alarmSent by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "alarm")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alarmPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1A0000), Color(0xFF2D0000)),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showEmergency = false }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text("EMERGENCY MODE", color = Color(0xFFFF3333), fontSize = 14.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Alarm button
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .scale(if (alarmSent) 1f else scale)
                    .clip(CircleShape)
                    .background(
                        if (alarmSent)
                            Brush.radialGradient(listOf(Color(0xFF22C55E), Color(0xFF16A34A)))
                        else
                            Brush.radialGradient(listOf(Color(0xFFFF3333), Color(0xFF991111)))
                    )
                    .clickable { alarmSent = !alarmSent },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (alarmSent) Icons.Default.Check else Icons.Default.NotificationImportant,
                        null, tint = Color.White, modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        if (alarmSent) "ALARM SENT" else "SEND ALARM",
                        color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            if (alarmSent) {
                Text("Your emergency contacts have been notified",
                    color = Color(0xFF22C55E), fontSize = 13.sp)
            } else {
                Text("Tap to alert your emergency contacts",
                    color = Color.White.copy(0.6f), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Emergency contacts
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("EMERGENCY CONTACTS", color = Color.White.copy(0.5f), fontSize = 9.sp,
                    letterSpacing = 1.8.sp)
                vm.profile.emergencyContacts.forEach { ec ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(0.06f))
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(ec.name, color = Color.White, fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold)
                            Text(ec.type, color = Color.White.copy(0.5f), fontSize = 11.sp)
                        }
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(Color(0xFF22C55E).copy(0.2f))
                                .border(1.dp, Color(0xFF22C55E), CircleShape)
                                .clickable { vm.showToast(ec.name, "Calling ${ec.name}...", "high") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Call, null, tint = Color(0xFF22C55E),
                                modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
//   CALL SCREEN
// ══════════════════════════════════════════════════════════
@Composable
fun CallScreen(vm: AppViewModel) {
    val contact = vm.callingContact ?: return
    var callState by remember { mutableStateOf("calling") }
    var seconds by remember { mutableStateOf(0) }
    var muted by remember { mutableStateOf(false) }
    var speaker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000)
        callState = "connected"
        while (true) {
            delay(1000)
            seconds++
        }
    }

    val timeStr = "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0A0015), Color(0xFF110022))
                )
            )
    ) {
        // Animated fragment art background
        Box(
            modifier = Modifier.size(260.dp).align(Alignment.Center)
                .offset(y = (-60).dp)
        ) {
            val palette = PALETTES[contact.paletteIndex % PALETTES.size]
            FragmentArt(palette = palette, seed = fragSeed(contact.id), animating = callState == "connected")
        }

        // Dark overlay
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.45f)))

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // CLOESED badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Pink.copy(0.15f))
                    .border(1.dp, Pink.copy(0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("✦ CLOESED CALL", color = Pink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Avatar
            Box(modifier = Modifier.size(110.dp).clip(RoundedCornerShape(28.dp))) {
                FragmentAvatar(paletteIndex = contact.paletteIndex, seed = fragSeed(contact.id))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(contact.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                when (callState) {
                    "calling"   -> "Calling..."
                    "connected" -> timeStr
                    else        -> "Call Ended"
                },
                color = Color.White.copy(0.6f), fontSize = 16.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Call controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 60.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CallControlBtn(
                    icon = if (muted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = if (muted) "Unmute" else "Mute",
                    active = muted,
                    onClick = { muted = !muted }
                )
                CallControlBtn(
                    icon = if (speaker) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                    label = "Speaker",
                    active = speaker,
                    onClick = { speaker = !speaker }
                )
                CallControlBtn(
                    icon = Icons.Default.Videocam,
                    label = "Video",
                    onClick = {}
                )
                // End call
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFFFF3333), Color(0xFFCC0000))))
                        .clickable { vm.endCall() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CallEnd, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
private fun CallControlBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.size(54.dp).clip(CircleShape)
                .background(if (active) Color.White.copy(0.25f) else Color.White.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Text(label, color = Color.White.copy(0.6f), fontSize = 11.sp)
    }
}

// ══════════════════════════════════════════════════════════
//   GROUP CHAT VIEW
// ══════════════════════════════════════════════════════════
@Composable
fun GroupChatView(vm: AppViewModel) {
    val c = vm.themeColors
    val gc = vm.currentGroupChat() ?: return
    val members = vm.contacts.filter { gc.memberIds.contains(it.id) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showLeaveConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(gc.messages.size) {
        if (gc.messages.isNotEmpty()) listState.animateScrollToItem(gc.messages.size - 1)
    }

    Box(modifier = Modifier.fillMaxSize().background(c.bg).imePadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(modifier = Modifier.fillMaxWidth().background(c.surface2)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showGroupChatView = false }
                )
                Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))
                    .background(Brush.linearGradient(listOf(Purple.copy(0.3f), Pink.copy(0.3f)))),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Group, null, tint = Purple, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(gc.name, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("${members.size} members", color = c.textSub, fontSize = 11.sp)
                }
                NeuIconButton(
                    icon = { Icon(Icons.Default.MoreVert, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.currentGroupChatId = gc.id; vm.showGroupChatMenu = true }
                )
            }

            // Messages
            LazyColumn(state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 10.dp)) {
                items(gc.messages) { msg ->
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.sent) Arrangement.End else Arrangement.Start) {
                        Box(modifier = Modifier
                            .widthIn(max = 260.dp)
                            .clip(RoundedCornerShape(
                                topStart = 16.dp, topEnd = 16.dp,
                                bottomStart = if (msg.sent) 16.dp else 4.dp,
                                bottomEnd = if (msg.sent) 4.dp else 16.dp
                            ))
                            .background(
                                if (msg.sent)
                                    Brush.linearGradient(listOf(vm.bubbleSent1, vm.bubbleSent2))
                                else Brush.linearGradient(listOf(c.surface, c.surface))
                            )
                            .padding(horizontal = 13.dp, vertical = 9.dp)) {
                            Column {
                                if (!msg.sent) {
                                    val sender = members.getOrNull(gc.messages.indexOf(msg) % members.size)
                                    Text(sender?.name ?: "Member", color = Purple, fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(bottom = 2.dp))
                                }
                                Text(msg.text, color = if (msg.sent) Color.White else c.text, fontSize = 14.sp)
                                Text(msg.timestamp, color = if (msg.sent) Color.White.copy(0.6f) else c.textSub,
                                    fontSize = 9.sp, modifier = Modifier.align(Alignment.End).padding(top = 2.dp))
                            }
                        }
                    }
                }
            }

            // Input bar
            Row(modifier = Modifier.fillMaxWidth().background(c.surface2)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp))
                    .background(c.bg).border(1.dp, c.border2, RoundedCornerShape(20.dp))) {
                    BasicTextField(
                        value = vm.chatInput,
                        onValueChange = { vm.chatInput = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = c.text, fontSize = 14.sp),
                        decorationBox = { inner ->
                            if (vm.chatInput.isEmpty()) {
                                Text("Message ${gc.name}...", color = c.textSub, fontSize = 14.sp)
                            }
                            inner()
                        }
                    )
                }
                Box(modifier = Modifier.size(42.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Pink, Purple)))
                    .clickable {
                        vm.sendGroupMessage(vm.chatInput)
                        scope.launch { delay(100); if (gc.messages.isNotEmpty()) listState.animateScrollToItem(gc.messages.size - 1) }
                    },
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Leave group confirmation
        if (showLeaveConfirm) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.55f)),
                contentAlignment = Alignment.Center) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp), color = c.surface) {
                    Column(modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ExitToApp, null, tint = Color(0xFFEF4444),
                            modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Leave ${gc.name}?", color = c.text, fontSize = 17.sp,
                            fontWeight = FontWeight.Bold)
                        Text("Are you sure you want to leave this group?",
                            color = c.textSub, fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp))
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlineButton("Cancel", { showLeaveConfirm = false },
                                modifier = Modifier.weight(1f))
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFEF4444))
                                .clickable {
                                    vm.showGroupChatView = false
                                    vm.leaveGroupChat()
                                }
                                .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center) {
                                Text("Leave", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
