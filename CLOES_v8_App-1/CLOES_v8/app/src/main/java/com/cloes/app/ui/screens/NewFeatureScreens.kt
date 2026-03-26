package com.cloes.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.cloes.app.data.*
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay

// ══════════════════════════════════════════════════════════════════════════════
//  MEET PAGE — discover people beyond your contacts
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun MeetPage(vm: AppViewModel) {
    val c = vm.themeColors

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2)
                    .statusBarsPadding().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showMeetPage = false })
                Column(modifier = Modifier.weight(1f)) {
                    Text("Meet ✦", color = c.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("People beyond your world", color = c.textSub, fontSize = 11.sp)
                }
                // Discoverable toggle
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        .background(if (vm.meetDiscoverableEnabled) Purple.copy(0.2f) else c.surface)
                        .border(1.dp, if (vm.meetDiscoverableEnabled) Purple else c.border, RoundedCornerShape(20.dp))
                        .clickable { vm.meetDiscoverableEnabled = !vm.meetDiscoverableEnabled }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape)
                        .background(if (vm.meetDiscoverableEnabled) Purple else c.textSub))
                    Text(
                        if (vm.meetDiscoverableEnabled) "Visible" else "Hidden",
                        color = if (vm.meetDiscoverableEnabled) Purple else c.textSub,
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Discoverable banner
            AnimatedVisibility(visible = vm.meetDiscoverableEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Purple.copy(0.1f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Visibility, null, tint = Purple, modifier = Modifier.size(16.dp))
                    Text("You're visible to people beyond your contacts", color = Purple, fontSize = 12.sp)
                }
            }

            // Filter chips
            var activeFilter by remember { mutableStateOf("All") }
            val filters = listOf("All", "Nearby", "Interests", "Mutual")
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { f ->
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(if (activeFilter == f) Purple else c.surface)
                            .border(1.dp, if (activeFilter == f) Purple else c.border, RoundedCornerShape(20.dp))
                            .clickable { activeFilter = f }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(f, color = if (activeFilter == f) Color.White else c.textMid, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // People grid
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vm.meetUsers) { user ->
                    MeetUserCard(user = user, vm = vm, c = c)
                }
            }
        }
    }
}

@Composable
private fun MeetUserCard(user: MeetUser, vm: AppViewModel, c: CloesColors) {
    var connected by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(c.surface).border(1.dp, c.border, RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(15.dp))) {
            FragmentAvatar(paletteIndex = user.paletteIndex, seed = user.id.toFloat() / 1000f)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(user.name, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                if (user.mutualCount > 0) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Purple.copy(0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("${user.mutualCount} mutual", color = Purple, fontSize = 10.sp)
                    }
                }
            }
            Text(user.bio, color = c.textMid, fontSize = 12.sp, maxLines = 1, modifier = Modifier.padding(top = 2.dp))
            if (user.distance.isNotEmpty()) {
                Text(user.distance, color = c.textSub, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
            }
            // Interest chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 6.dp)) {
                items(user.interests.take(3)) { interest ->
                    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Cyan.copy(0.12f)).padding(horizontal = 7.dp, vertical = 2.dp)) {
                        Text(interest, color = Cyan, fontSize = 9.sp)
                    }
                }
            }
        }
        // Connect button
        Box(
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                .then(if (connected) Modifier.background(c.surface) else Modifier.background(Brush.linearGradient(listOf(Purple, Pink))))
                .border(1.dp, if (connected) c.border else Color.Transparent, RoundedCornerShape(12.dp))
                .clickable {
                    if (!connected) {
                        connected = true
                        vm.showToast(user.name, "Connection request sent ✦", "low")
                    }
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(if (connected) "Sent ✓" else "Connect", color = if (connected) c.textMid else Color.White,
                fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  BLOOM HISTORY / RELATIONSHIP TIMELINE
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun BloomHistoryScreen(vm: AppViewModel) {
    val c = vm.themeColors
    val contactId = vm.bloomHistoryContactId
    val contact = vm.contacts.find { it.id == contactId } ?: vm.contacts.firstOrNull()

    // Synthetic timeline data
    val timelinePoints = remember(contact?.id) {
        listOf(
            Pair("Jan", 45), Pair("Feb", 62), Pair("Mar", 78), Pair("Apr", 55),
            Pair("May", 30), Pair("Jun", 88), Pair("Jul", 92), Pair("Aug", 70),
            Pair("Sep", 40), Pair("Oct", 58), Pair("Nov", 85), Pair("Dec", contact?.bloomScore ?: 60)
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2)
                    .statusBarsPadding().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showBloomHistory = false })
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bloom History", color = c.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(contact?.name ?: "Select a contact", color = c.textSub, fontSize = 11.sp)
                }
                // Contact switcher
                NeuIconButton(icon = { Icon(Icons.Default.SwitchAccount, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { /* cycle contacts */ val idx = vm.contacts.indexOfFirst { it.id == contactId }; vm.bloomHistoryContactId = vm.contacts.getOrNull((idx + 1) % vm.contacts.size)?.id })
            }

            contact?.let { con ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header card
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                                .background(c.surface).border(1.dp, c.border, RoundedCornerShape(20.dp)).padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))) {
                                FragmentAvatar(paletteIndex = con.paletteIndex, seed = fragSeed(con.id))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(con.name, color = c.text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text(con.handle, color = Purple, fontSize = 12.sp)
                                val bloomCol = vm.bloomColor(con.bloomScore)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(bloomCol))
                                    Text("Bloom Score: ${con.bloomScore}", color = bloomCol, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Visual timeline graph
                    item {
                        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                            .background(c.surface).border(1.dp, c.border, RoundedCornerShape(20.dp)).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("RELATIONSHIP HEALTH — 2025", color = c.textSub, fontSize = 10.sp, letterSpacing = 2.sp)
                            // Bar chart
                            Row(
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                timelinePoints.forEach { (month, score) ->
                                    val bloomCol = vm.bloomColor(score)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(0.6f)
                                                .height((score * 1.0f).dp.coerceIn(8.dp, 100.dp))
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(Brush.verticalGradient(listOf(bloomCol, bloomCol.copy(0.4f))))
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(month.take(1), color = c.textSub, fontSize = 8.sp)
                                    }
                                }
                            }
                            // Peak/valley annotation
                            val peak = timelinePoints.maxByOrNull { it.second }
                            val valley = timelinePoints.minByOrNull { it.second }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                peak?.let { Chip("🔥 Peak: ${it.first}", Color(0xFFFF9020)) }
                                valley?.let { Chip("❄️ Quiet: ${it.first}", Color(0xFF4A5A8A)) }
                            }
                        }
                    }

                    // Milestones
                    item {
                        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                            .background(c.surface).border(1.dp, c.border, RoundedCornerShape(20.dp)).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("MOMENTS", color = c.textSub, fontSize = 10.sp, letterSpacing = 2.sp)
                            listOf(
                                Triple("🎵", "Jun", "3 voice memos exchanged"),
                                Triple("📞", "Jul", "Longest call — 1h 42m"),
                                Triple("🌸", "Aug", "Sent each other Bloom posts"),
                                Triple("✦", "Nov", "7-day streak of daily messages")
                            ).forEach { (emoji, month, label) ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(emoji, fontSize = 20.sp)
                                    Column {
                                        Text(label, color = c.text, fontSize = 13.sp)
                                        Text(month, color = c.textSub, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Seasonal summary
                    item {
                        val seasons = listOf(
                            Triple("Winter 2025", "❄️", "12 messages · 1 call"),
                            Triple("Spring 2025", "🌸", "47 messages · 3 calls · 2 vibes"),
                            Triple("Summer 2025", "☀️", "91 messages · 7 calls · 4 vibes"),
                            Triple("Autumn 2025", "🍂", "38 messages · 2 calls")
                        )
                        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                            .background(c.surface).border(1.dp, c.border, RoundedCornerShape(20.dp)).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("SEASONS", color = c.textSub, fontSize = 10.sp, letterSpacing = 2.sp)
                            seasons.forEach { (label, emoji, stats) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                        .background(c.surface2).padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(emoji, fontSize = 24.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(label, color = c.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(stats, color = c.textSub, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String, color: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  BLOOM RITUAL DIALOG
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun BloomRitualDialog(vm: AppViewModel) {
    val c = vm.themeColors
    val contact = vm.bloomRitualContact ?: return

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.88f).clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF1E1040), c.surface)))
                .border(1.dp, Purple.copy(0.3f), RoundedCornerShape(28.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🌸", fontSize = 48.sp)
            Text("Bloom Ritual", color = c.text, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Purple.copy(0.1f)).padding(14.dp)) {
                Text(
                    "You haven't reached out to ${contact.name} in a while. Their bloom is fading.\n\n\"A tiny message can mean more than you know.\"",
                    color = c.text, fontSize = 13.sp, lineHeight = 20.sp, textAlign = TextAlign.Center
                )
            }

            // Suggested actions
            val actions = listOf(
                "💬" to "Send a quick thinking-of-you",
                "🎵" to "Send a voice note",
                "✦" to "Share a Bloom post with them",
                "📞" to "Give them a call"
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                actions.forEach { (emoji, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(c.surface2).border(1.dp, c.border, RoundedCornerShape(14.dp))
                            .clickable {
                                vm.openChat(contact.id)
                                vm.chatInput = when (emoji) {
                                    "💬" -> "hey, just thinking of you ✦"
                                    "🎵" -> "🎵 [voice note]"
                                    else -> ""
                                }
                                vm.showBloomRitual = false
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(emoji, fontSize = 20.sp)
                        Text(label, color = c.text, fontSize = 13.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = Purple, modifier = Modifier.size(16.dp))
                    }
                }
            }

            TextButton(onClick = { vm.showBloomRitual = false }) {
                Text("Maybe later", color = c.textSub, fontSize = 13.sp)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  MOOD-AWARE MESSAGE DIALOG
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun MoodMessageDialog(vm: AppViewModel) {
    val c = vm.themeColors
    val contact = vm.currentContact() ?: return

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.65f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.88f).clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF1E1040), c.surface)))
                .border(1.dp, Purple.copy(0.3f), RoundedCornerShape(28.dp))
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("🌙", fontSize = 42.sp)
            Text("It's pretty late…", color = c.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                "${contact.name} is probably asleep right now. What would you like to do?",
                color = c.textMid, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 20.sp
            )

            val opts = listOf(
                Triple(Icons.Default.Send,          "Send now",               "send"),
                Triple(Icons.Default.WbSunny,       "Schedule for morning",   "schedule"),
                Triple(Icons.Default.EditNote,       "Save as a thought",      "save")
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                opts.forEach { (icon, label, key) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(c.surface2).border(1.dp, c.border, RoundedCornerShape(14.dp))
                            .clickable {
                                when (key) {
                                    "send"     -> vm.sendMessageNow(vm.moodPendingMessage)
                                    "schedule" -> vm.scheduleMessage(vm.moodPendingMessage)
                                    "save"     -> vm.saveAsThought(vm.moodPendingMessage)
                                }
                            }.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, null, tint = Purple, modifier = Modifier.size(18.dp))
                        Text(label, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = c.textSub, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VIBE VISIBILITY SETTINGS
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun VibeVisibilitySettingsScreen(vm: AppViewModel) {
    val c = vm.themeColors
    val videoId = vm.vibeVisibilitySettingsVideoId
    val video = vm.vibeVideos.find { it.id == videoId } ?: return

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2)
                    .statusBarsPadding().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showVibeVisibilitySettings = false })
                Column(modifier = Modifier.weight(1f)) {
                    Text("Vibe Visibility", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(video.title, color = c.textSub, fontSize = 11.sp)
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var selectedVisibility by remember { mutableStateOf(video.visibility) }

                Text("WHO CAN SEE THIS VIDEO", color = c.textSub, fontSize = 10.sp, letterSpacing = 2.sp)

                listOf(
                    Triple(VibeVideoVisibility.ALL,         "🌍", "Everyone"),
                    Triple(VibeVideoVisibility.ONLY_LISTED, "✓",  "Only selected contacts"),
                    Triple(VibeVideoVisibility.BLOCK_LISTED,"🚫", "Everyone except blocked")
                ).forEach { (vis, emoji, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(if (selectedVisibility == vis) Purple.copy(0.12f) else c.surface)
                            .border(1.dp, if (selectedVisibility == vis) Purple else c.border, RoundedCornerShape(16.dp))
                            .clickable { selectedVisibility = vis }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(emoji, fontSize = 22.sp)
                        Text(label, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        if (selectedVisibility == vis) Icon(Icons.Default.Check, null, tint = Purple, modifier = Modifier.size(20.dp))
                    }
                }

                if (selectedVisibility != VibeVideoVisibility.ALL) {
                    Text("SELECT CONTACTS", color = c.textSub, fontSize = 10.sp, letterSpacing = 2.sp)
                    vm.contacts.forEach { contact ->
                        val isListed = video.allowedContactIds.contains(contact.id)
                        val isBlocked = video.blockedContactIds.contains(contact.id)
                        val checked = if (selectedVisibility == VibeVideoVisibility.ONLY_LISTED) isListed else isBlocked

                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                .background(c.surface).border(1.dp, c.border, RoundedCornerShape(14.dp))
                                .clickable {
                                    if (selectedVisibility == VibeVideoVisibility.ONLY_LISTED) {
                                        if (isListed) video.allowedContactIds.remove(contact.id) else video.allowedContactIds.add(contact.id)
                                    } else {
                                        if (isBlocked) video.blockedContactIds.remove(contact.id) else video.blockedContactIds.add(contact.id)
                                    }
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))) {
                                FragmentAvatar(paletteIndex = contact.paletteIndex, seed = fragSeed(contact.id))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contact.name, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(contact.group, color = c.textSub, fontSize = 11.sp)
                            }
                            Box(
                                modifier = Modifier.size(22.dp).clip(RoundedCornerShape(6.dp))
                                    .background(if (checked) Purple else c.surface2)
                                    .border(1.dp, if (checked) Purple else c.border, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (checked) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                GradientButton(
                    text = "Save Settings",
                    onClick = {
                        vm.showToast("Vibe", "Visibility settings saved ✦", "low")
                        vm.showVibeVisibilitySettings = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ── Meet tab inline (embedded inside MainScreen content area, no separate back) ──
@Composable
fun MeetPageTab(vm: AppViewModel) {
    val c = vm.themeColors
    Column(modifier = Modifier.fillMaxSize()) {
        // Header row inside the main content
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Meet ✦", color = c.text, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                Text("Discover people beyond your world", color = c.textSub, fontSize = 12.sp)
            }
            // Discoverable toggle pill
            Row(
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(if (vm.meetDiscoverableEnabled) Purple.copy(0.18f) else c.surface)
                    .border(1.dp, if (vm.meetDiscoverableEnabled) Purple else c.border, RoundedCornerShape(20.dp))
                    .clickable { vm.meetDiscoverableEnabled = !vm.meetDiscoverableEnabled; vm.showToast("Meet", if (vm.meetDiscoverableEnabled) "You're now visible ✦" else "Hidden from Meet", "low") }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(if (vm.meetDiscoverableEnabled) Purple else c.textSub))
                Text(if (vm.meetDiscoverableEnabled) "Visible" else "Hidden",
                    color = if (vm.meetDiscoverableEnabled) Purple else c.textSub,
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        AnimatedVisibility(visible = vm.meetDiscoverableEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Purple.copy(0.08f)).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Visibility, null, tint = Purple, modifier = Modifier.size(14.dp))
                Text("People beyond your contacts can see you here", color = Purple, fontSize = 11.sp)
            }
        }

        var activeFilter by remember { mutableStateOf("All") }
        val filters = listOf("All", "Nearby", "Interests", "Mutual")
        LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { f ->
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        .background(if (activeFilter == f) Purple else c.surface)
                        .border(1.dp, if (activeFilter == f) Purple else c.border, RoundedCornerShape(20.dp))
                        .clickable { activeFilter = f }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) { Text(f, color = if (activeFilter == f) Color.White else c.textMid, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            }
        }

        LazyColumn(contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(vm.meetUsers) { user ->
                var connected by remember(user.id) { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(c.surface).border(1.dp, c.border, RoundedCornerShape(18.dp)).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))) {
                        FragmentAvatar(paletteIndex = user.paletteIndex, seed = user.id.toFloat() / 1000f)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(user.name, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            if (user.mutualCount > 0) Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Purple.copy(0.12f)).padding(horizontal = 5.dp, vertical = 2.dp)) {
                                Text("${user.mutualCount} mutual", color = Purple, fontSize = 9.sp)
                            }
                        }
                        Text(user.bio, color = c.textMid, fontSize = 11.sp, maxLines = 1, modifier = Modifier.padding(top = 1.dp))
                        if (user.distance.isNotEmpty()) Text(user.distance, color = c.textSub, fontSize = 10.sp)
                    }
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            .then(if (connected) Modifier.background(c.surface2) else Modifier.background(Brush.linearGradient(listOf(Purple, Pink))))
                            .border(1.dp, if (connected) c.border else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { if (!connected) { connected = true; vm.showToast(user.name, "Request sent ✦", "low") } }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) { Text(if (connected) "Sent ✓" else "Connect", color = if (connected) c.textMid else Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}
