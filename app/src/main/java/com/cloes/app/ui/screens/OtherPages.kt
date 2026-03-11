package com.cloes.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.cloes.app.data.*
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.R
import com.cloes.app.viewmodel.AppViewModel
import androidx.compose.animation.*
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════
//   BLOOM PAGE — Connection Relationship Tracker
// ══════════════════════════════════════════════════════════
@Composable
fun BloomPage(vm: AppViewModel) {
    val c = vm.themeColors

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Bloom", color = c.text, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif)
                Text("How are your connections doing?", color = c.textSub, fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp))
            }
            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                .background(Purple.copy(0.1f))
                .padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text("${vm.contacts.size} connections", color = Purple, fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold)
            }
        }

        // Health summary strip
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            val thriving = vm.contacts.count { it.urgency == "low" && it.unread == 0 }
            val needsAttn = vm.contacts.count { it.urgency == "high" || it.unread > 0 }
            val quiet    = vm.contacts.count { it.urgency == "mid" }

            listOf(
                Triple("🌸", "Thriving", thriving),
                Triple("💛", "Quiet", quiet),
                Triple("🔥", "Needs you", needsAttn)
            ).forEach { (emoji, label, count) ->
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emoji, fontSize = 18.sp)
                        Text("$count", color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(label, color = c.textSub, fontSize = 9.5.sp, maxLines = 1)
                    }
                }
            }
        }

        // Contact relationship cards
        LazyColumn(
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(vm.contacts.sortedByDescending { it.unread + (if (it.urgency == "high") 2 else 0) }) { contact ->
                BloomContactCard(contact = contact, vm = vm, c = c)
            }
        }
    }
}

@Composable
private fun BloomContactCard(contact: Contact, vm: AppViewModel, c: CloesColors) {
    // Compute relationship health score 0-100
    val msgCount = contact.messages.size
    val hasRecent = contact.messages.lastOrNull() != null
    val isActive = contact.online
    val score = when {
        contact.urgency == "high" && contact.unread > 0 -> 85
        contact.urgency == "mid"  && contact.unread > 0 -> 65
        contact.urgency == "low"  && msgCount > 3       -> 90
        msgCount > 5                                     -> 78
        msgCount > 1                                     -> 55
        else                                             -> 30
    }
    val healthColor = when {
        score >= 80 -> Color(0xFF22C55E)
        score >= 55 -> Color(0xFFF59E0B)
        else        -> Color(0xFFEF4444)
    }
    val healthLabel = when {
        score >= 80 -> "Strong ✦"
        score >= 55 -> "Growing"
        else        -> "Needs love"
    }
    val lastMsg = contact.messages.lastOrNull()

    GlassCard(modifier = Modifier.fillMaxWidth()
        .clickable { vm.openChat(contact.id) }) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Avatar
                Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))) {
                    FragmentAvatar(paletteIndex = contact.paletteIndex, seed = fragSeed(contact.id))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(contact.name, color = c.text, fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold)
                        if (isActive) Box(modifier = Modifier.size(6.dp).clip(CircleShape)
                            .background(Lime))
                    }
                    Text(contact.group, color = c.textSub, fontSize = 11.sp)
                }
                // Health badge
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(healthColor.copy(0.13f))
                    .padding(horizontal = 9.dp, vertical = 4.dp)) {
                    Text(healthLabel, color = healthColor, fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Relationship health bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Connection health", color = c.textSub, fontSize = 10.sp)
                    Text("$score%", color = healthColor, fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold)
                }
                Box(modifier = Modifier.fillMaxWidth().height(5.dp)
                    .clip(RoundedCornerShape(3.dp)).background(Purple.copy(0.1f))) {
                    Box(modifier = Modifier
                        .fillMaxWidth(score / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(
                            listOf(healthColor.copy(0.7f), healthColor)
                        ))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatChip("💬", "${msgCount}", "msgs", c)
                StatChip("📅", if (hasRecent) lastMsg!!.timestamp else "—", "last", c)
                StatChip(
                    if (contact.urgency == "high") "🔴" else if (contact.urgency == "mid") "🟡" else "🟢",
                    contact.urgency.replaceFirstChar { it.uppercase() }, "urgency", c
                )
                Spacer(modifier = Modifier.weight(1f))
                // Nudge button
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(Pink.copy(0.15f), Purple.copy(0.15f))))
                    .border(1.dp, Purple.copy(0.25f), RoundedCornerShape(10.dp))
                    .clickable {
                        vm.openChat(contact.id)
                        vm.chatInput = "Hey! How are you? ✦"
                    }
                    .padding(horizontal = 9.dp, vertical = 5.dp)) {
                    Text("Say hi ✦", color = Purple, fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StatChip(emoji: String, value: String, label: String, c: CloesColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 12.sp)
        Text(value, color = c.text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            maxLines = 1)
        Text(label, color = c.textSub, fontSize = 9.sp)
    }
}

// ══════════════════════════════════════════════════════════
//   GROUPS PAGE — Circles with member mgmt & tone
// ══════════════════════════════════════════════════════════
@Composable
fun GroupsPage(vm: AppViewModel) {
    val c = vm.themeColors
    var selectedGroup by remember { mutableStateOf<ContactGroup?>(null) }

    if (selectedGroup != null) {
        GroupDetailScreen(
            group = selectedGroup!!,
            vm = vm,
            c = c,
            onBack = { selectedGroup = null }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Circles", color = c.text, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.GroupAdd, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showAddGroup = true }
                )
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(vm.groups) { group ->
                GroupCard(group = group, vm = vm, c = c, onClick = { selectedGroup = group })
            }
        }
    }
}

@Composable
private fun GroupCard(group: ContactGroup, vm: AppViewModel, c: CloesColors, onClick: () -> Unit) {
    val members = vm.contacts.filter { contact -> group.members.contains(contact.id) }
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Group profile picture or emoji fallback
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                        .background(Purple.copy(0.12f)), contentAlignment = Alignment.Center) {
                        if (group.photoUri != null) {
                            AsyncImage(
                                model = group.photoUri,
                                contentDescription = "Group photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(group.emoji, fontSize = 20.sp)
                        }
                    }
                    Column {
                        Text(group.name, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("${members.size} members · ${group.tone}", color = c.textSub, fontSize = 11.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (group.muse) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            .background(Purple.copy(0.1f)).padding(horizontal = 6.dp, vertical = 3.dp)) {
                            Text("✦ Muse", color = Purple, fontSize = 9.sp)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = c.textSub, modifier = Modifier.size(16.dp))
                }
            }
            if (members.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    members.take(5).forEachIndexed { idx, contact ->
                        Box(modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp))
                            .border(2.dp, c.bg, RoundedCornerShape(9.dp)).zIndex(idx.toFloat())) {
                            FragmentAvatar(paletteIndex = contact.paletteIndex, seed = fragSeed(contact.id))
                        }
                    }
                    if (members.size > 5) {
                        Box(modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp))
                            .background(Purple.copy(0.2f)).border(2.dp, c.bg, RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center) {
                            Text("+${members.size - 5}", color = Purple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

val TONE_OPTIONS = listOf(
    "Friendly" to "😊",
    "Intimate" to "💜",
    "Professional" to "💼",
    "Playful" to "🎉",
    "Supportive" to "🤗",
    "Casual" to "👋"
)

@Composable
private fun GroupDetailScreen(group: ContactGroup, vm: AppViewModel, c: CloesColors, onBack: () -> Unit) {
    val members = vm.contacts.filter { group.members.contains(it.id) }.toMutableList()
    var showAddMember by remember { mutableStateOf(false) }
    var currentTone by remember { mutableStateOf(group.tone) }
    var museEnabled by remember { mutableStateOf(group.muse) }
    // Track current group photo uri (from vm.groups)
    val groupIdx = vm.groups.indexOfFirst { it.id == group.id }
    var groupPhotoUri by remember { mutableStateOf(group.photoUri) }

    Column(modifier = Modifier.fillMaxSize().background(c.bg)) {
        // Header
        Row(modifier = Modifier.fillMaxWidth().background(c.surface2)
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NeuIconButton(
                icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                onClick = onBack
            )
            // Group avatar — tap to change photo
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                .background(Purple.copy(0.12f))
                .clickable {
                    // Cycle through sample palette images as "photo picker" simulation
                    val samplePhotos = listOf(null, "sample_1", "sample_2")
                    val next = (samplePhotos.indexOf(groupPhotoUri) + 1) % samplePhotos.size
                    groupPhotoUri = samplePhotos[next]
                    if (groupIdx >= 0) vm.groups[groupIdx] = vm.groups[groupIdx].copy(photoUri = samplePhotos[next])
                    vm.showToast(group.name, if (samplePhotos[next] == null) "Photo removed" else "Group photo updated ✦", "low")
                },
                contentAlignment = Alignment.Center) {
                if (groupPhotoUri != null) {
                    AsyncImage(
                        model = groupPhotoUri,
                        contentDescription = "Group photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(group.emoji, fontSize = 22.sp)
                }
                // Camera badge
                Box(modifier = Modifier.align(Alignment.BottomEnd).size(16.dp)
                    .clip(CircleShape).background(Purple),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(9.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(group.name, color = c.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("${members.size} members · tap photo to change", color = c.textSub, fontSize = 11.sp)
            }
            NeuIconButton(
                icon = { Icon(Icons.Default.Send, null, tint = Purple, modifier = Modifier.size(16.dp)) },
                onClick = { vm.circleMessageGroup = group; vm.showCircleMessage = true }
            )
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)) {

            // Muse tone section
            SectionLabel("Muse Response Tone")
            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Purple, modifier = Modifier.size(16.dp))
                            Text("Muse AI for this circle", color = c.text, fontSize = 13.sp)
                        }
                        CloesToggle(checked = museEnabled, onCheckedChange = { museEnabled = it })
                    }
                    if (museEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Response tone", color = c.textSub, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            TONE_OPTIONS.forEach { (tone, emoji) ->
                                val selected = currentTone == tone
                                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                    .background(if (selected) Purple.copy(0.15f) else c.bg)
                                    .border(1.5.dp, if (selected) Purple else c.border2, RoundedCornerShape(20.dp))
                                    .clickable { currentTone = tone; vm.showToast(group.name, "Tone: $tone $emoji", "low") }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)) {
                                    Text("$emoji $tone", color = if (selected) Purple else c.textMid, fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }

            // Members section
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                SectionLabel("Members (${members.size})")
                TextButton(onClick = { showAddMember = true }) {
                    Icon(Icons.Default.PersonAdd, null, tint = Purple, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", color = Purple, fontSize = 12.sp)
                }
            }

            members.forEach { contact ->
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(14.dp)).background(c.surface)
                    .padding(horizontal = 13.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))) {
                        FragmentAvatar(paletteIndex = contact.paletteIndex, seed = fragSeed(contact.id))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(contact.name, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("@${contact.handle}", color = c.textSub, fontSize = 11.sp)
                    }
                    IconButton(onClick = {
                        group.members.remove(contact.id)
                        vm.showToast(contact.name, "Removed from ${group.name}", "low")
                    }) {
                        Icon(Icons.Default.PersonRemove, null, tint = c.textSub, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Non-members you can add
            if (showAddMember) {
                Spacer(modifier = Modifier.height(8.dp))
                SectionLabel("Add from contacts")
                val nonMembers = vm.contacts.filter { !group.members.contains(it.id) }
                if (nonMembers.isEmpty()) {
                    Text("All contacts are already in this circle", color = c.textSub, fontSize = 13.sp)
                } else {
                    nonMembers.forEach { contact ->
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(14.dp)).background(c.surface)
                            .clickable {
                                group.members.add(contact.id)
                                vm.showToast(contact.name, "Added to ${group.name}", "low")
                            }
                            .padding(horizontal = 13.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))) {
                                FragmentAvatar(paletteIndex = contact.paletteIndex, seed = fragSeed(contact.id))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contact.name, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(contact.group, color = c.textSub, fontSize = 11.sp)
                            }
                            Icon(Icons.Default.Add, null, tint = Purple, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
//   MUSE AI PAGE
// ══════════════════════════════════════════════════════════
@Composable
fun MuseAIPage(vm: AppViewModel) {
    val c = vm.themeColors
    val messages = remember {
        mutableStateListOf(Pair(false, "Hi ✦ I'm Muse, your private AI companion. What's on your mind?"))
    }
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Muse logo cycling every 3 seconds (0-3 maps to muse_logo_1..4)
    val museLogoImages = listOf(
        R.drawable.muse_logo_1, R.drawable.muse_logo_2,
        R.drawable.muse_logo_3, R.drawable.muse_logo_4
    )
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            vm.museLogoIndex = (vm.museLogoIndex + 1) % museLogoImages.size
        }
    }
    var showFullLogo by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(Pink.copy(0.15f), Purple.copy(0.15f))))
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                // Cycling Muse logo — tap to see full image
                Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(20.dp))
                    .clickable { showFullLogo = true }) {
                    AnimatedContent(
                        targetState = vm.museLogoIndex,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "muse_logo"
                    ) { idx ->
                        AsyncImage(
                            model = museLogoImages[idx],
                            contentDescription = "Muse",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Muse ✦", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Your private AI companion", color = c.textSub, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf("Schedule 🗓", "Task ✅", "Attach 📎").forEach { action ->
                        TabChip(text = action, selected = false, onClick = {
                            vm.showToast("Muse", "$action — coming soon ✦", "low")
                        })
                    }
                }
            }
        }



        LazyColumn(
            state = listState, modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.size) { idx ->
                val (fromUser, text) = messages[idx]
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start) {
                    Box(
                        modifier = Modifier.widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(
                                topStart = 16.dp, topEnd = 16.dp,
                                bottomStart = if (fromUser) 16.dp else 4.dp,
                                bottomEnd = if (fromUser) 4.dp else 16.dp
                            ))
                            .background(
                                if (fromUser) Brush.linearGradient(listOf(Pink, Purple))
                                else Brush.horizontalGradient(listOf(c.surface, c.surface))
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(text, color = if (fromUser) Color.White else c.text,
                            fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(c.surface2)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(c.bg)) {
                TextField(
                    value = input, onValueChange = { input = it },
                    placeholder = { Text("Ask Muse anything...", color = c.textSub, fontSize = 14.sp) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent,
                        unfocusedTextColor = c.text, focusedTextColor = c.text
                    )
                )
            }
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(13.dp))
                    .background(Brush.linearGradient(listOf(Pink, Purple)))
                    .clickable(enabled = input.isNotBlank()) {
                        val query = input; input = ""
                        messages.add(Pair(true, query))
                        scope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                            delay(900)
                            val replies = listOf(
                                "I understand you completely — you deserve to feel heard 💜",
                                "That sounds really meaningful. I'm glad you shared that ✦",
                                "You have such a beautiful way of seeing things 🌙",
                                "That's exactly the kind of thing worth holding onto 🌿",
                                "I hear you. Whatever you're going through — you've got this ✦"
                            )
                            messages.add(Pair(false, replies.random()))
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }   // end Column

    // Full-screen Muse logo overlay (shown above everything)
    if (showFullLogo) {
        Box(modifier = Modifier.fillMaxSize()
            .background(Color.Black.copy(0.92f))
            .pointerInput(Unit) { detectTapGestures(onTap = { showFullLogo = false }) },
            contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = vm.museLogoIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "muse_logo_full"
            ) { idx ->
                val museLogoImages2 = listOf(
                    R.drawable.muse_logo_1, R.drawable.muse_logo_2,
                    R.drawable.muse_logo_3, R.drawable.muse_logo_4
                )
                AsyncImage(
                    model = museLogoImages2[idx],
                    contentDescription = "Muse full",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().padding(32.dp)
                )
            }
            Text("Tap anywhere to close", color = Color.White.copy(0.3f), fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp))
        }
    }

    }   // end outer Box
}

// ══════════════════════════════════════════════════════════
//   VIBE PAGE
// ══════════════════════════════════════════════════════════
@Composable
fun VibePage(vm: AppViewModel) {
    val c = vm.themeColors
    var activeTab by remember { mutableStateOf("for-you") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Vibe ▶", color = c.text, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.Add, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showUploadVideo = true }
                )
                NeuIconButton(
                    icon = { Icon(Icons.Default.Search, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showToast("Vibe", "Search videos 🔍", "low") }
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            val tabs = listOf("for-you" to "For You", "following" to "Following",
                "vibers" to "Vibers 🌟", "trending" to "Trending 🔥", "premium" to "Premium ⭐")
            items(tabs.size) { idx ->
                val (tabId, label) = tabs[idx]
                TabChip(text = label, selected = activeTab == tabId,
                    onClick = { activeTab = tabId; vm.showToast("Vibe", label, "low") })
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(vm.vibeVideos) { idx, video ->
                VibeVideoCard(video = video, vm = vm, idx = idx, c = c)
            }
        }
    }
}

@Composable
private fun VibeVideoCard(video: VibeVideo, vm: AppViewModel, idx: Int, c: CloesColors) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)
                .clickable { vm.openVibeShorts(idx) }) {
                FragmentArt(
                    palette = video.paletteColors.ifEmpty { PALETTES[idx % PALETTES.size] },
                    seed = idx * 0.37f + 0.1f, animating = false
                )
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape)
                        .background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) {
                        Text("▶", color = Color.White, fontSize = 20.sp)
                    }
                }
                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    .clip(RoundedCornerShape(6.dp)).background(Color.Black.copy(0.6f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(video.duration, color = Color.White, fontSize = 10.sp)
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(video.title, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("${video.creator} • ${video.views} views • ${video.category}",
                    color = c.textSub, fontSize = 11.sp,
                    modifier = Modifier.padding(top = 3.dp, bottom = 10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf(
                        "💜 ${video.likes}" to { vm.likeVibe(video.id) },
                        "💬 ${video.comments.size}" to { if (video.comments.isNotEmpty()) vm.showToast(video.creator, video.comments.random(), "low") },
                        "📤 Share" to { vm.showToast("Vibe", "Shared ✦", "low") },
                        "🔖 Save" to { vm.showToast("Vibe", "Saved ✦", "low") }
                    ).forEach { (label, action) ->
                        Text(label, color = c.textMid, fontSize = 12.sp,
                            modifier = Modifier.clickable(onClick = action))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
//   COINS PAGE
// ══════════════════════════════════════════════════════════
@Composable
fun CoinsPage(vm: AppViewModel) {
    val c = vm.themeColors
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)) {
                Box(modifier = Modifier.size(110.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Gold, Color(0xFFD97706)))),
                    contentAlignment = Alignment.Center) {
                    Text("🪙", fontSize = 44.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("${vm.coinBalance}", color = c.text, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Text("Vibe Coins", color = c.textSub, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(Purple.copy(0.1f)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text("${(vm.coinBalance * 0.2).toInt()} MB data saved", color = Purple, fontSize = 12.sp)
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(Triple("💜", "Streak", "7 days"),
                    Triple("🎯", "Saved", "${(vm.coinBalance * 0.2).toInt()} MB"),
                    Triple("⭐", "Level", "Vibe Star")).forEach { (emoji, label, value) ->
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji, fontSize = 18.sp)
                            Text(value, color = c.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(label, color = c.textSub, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        item {
            Text("Recent Activity", color = c.textSub, fontSize = 9.sp, letterSpacing = 1.8.sp,
                modifier = Modifier.padding(bottom = 10.dp))
        }
        items(SeedData.COIN_HISTORY) { (label, amount, time) ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                .clip(RoundedCornerShape(14.dp)).background(c.surface).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, color = c.text, fontSize = 13.sp)
                Column(horizontalAlignment = Alignment.End) {
                    Text(amount, color = if (amount.startsWith("+")) Lime else Crimson,
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(time, color = c.textSub, fontSize = 10.sp)
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
            GradientButton("Earn More Coins ✦",
                { vm.coinBalance += 5; vm.showToast("Coins", "+5 coins earned!", "low") },
                modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Text("Use Your Coins", color = c.textSub, fontSize = 9.sp, letterSpacing = 1.8.sp,
                modifier = Modifier.padding(bottom = 10.dp))
        }
        items(listOf(
            Triple("Premium Sticker Pack", 50, Icons.Default.EmojiEmotions),
            Triple("Custom Bubble Theme", 80, Icons.Default.Palette),
            Triple("Boost Vibe Reach", 30, Icons.Default.ShowChart),
            Triple("Extended Disappear Timer", 20, Icons.Default.Timer),
            Triple("Exclusive Fragment Art", 100, Icons.Default.AutoAwesome),
            Triple("Chat Background Pack", 40, Icons.Default.Wallpaper)
        )) { (label, cost, icon) ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                .clip(RoundedCornerShape(14.dp)).background(c.surface).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))
                    .background(Gold.copy(0.15f)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Gold, modifier = Modifier.size(20.dp))
                }
                Text(label, color = c.text, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(if (vm.coinBalance >= cost) Gold.copy(0.15f) else c.surface2)
                    .clickable(enabled = vm.coinBalance >= cost) {
                        vm.coinBalance -= cost
                        vm.showToast("Coins", "$label unlocked!", "low")
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("$cost 🪙", color = if (vm.coinBalance >= cost) Gold else c.textSub,
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

// ══════════════════════════════════════════════════════════
//   PROFILE OVERLAY
// ══════════════════════════════════════════════════════════
@Composable
fun ProfileOverlay(vm: AppViewModel) {
    val c = vm.themeColors
    Column(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Row(modifier = Modifier.fillMaxWidth().background(c.surface2)
            .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("My Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.text,
                fontFamily = FontFamily.Serif)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.Share, null, tint = c.textMid, modifier = Modifier.size(16.dp)) },
                    onClick = { vm.showShareModal = true })
                NeuIconButton(
                    icon = { Icon(Icons.Default.Edit, null, tint = c.textMid, modifier = Modifier.size(16.dp)) },
                    onClick = { vm.showEditProfile = true })
                NeuIconButton(
                    icon = { Icon(Icons.Default.Settings, null, tint = c.textMid, modifier = Modifier.size(16.dp)) },
                    onClick = { vm.showSettings = true })
            }
        }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)) {
            Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(24.dp))
                .align(Alignment.CenterHorizontally)) {
                FragmentArt(palette = vm.profile.palette.ifEmpty { listOf(Pink, Purple, Cyan) },
                    seed = vm.profile.lightSeed, animating = true)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(vm.profile.name, color = c.text, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally))
            Text("@${vm.profile.handle}", color = Purple, fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel("My Fragment Colors")
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                vm.profile.palette.take(8).forEach { color ->
                    Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(color))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("${vm.contacts.size}" to "Connections",
                    "${vm.coinBalance}" to "Coins",
                    "${vm.vibeVideos.count { it.creator == vm.profile.name }}" to "Vibes"
                ).forEach { (num, label) ->
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(num, color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(label, color = c.textSub, fontSize = 10.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("My Vibe Videos")
            val myVideos = vm.vibeVideos.filter { it.creator == vm.profile.name }
                .ifEmpty { vm.vibeVideos.take(3) }
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                myVideos.forEachIndexed { idx, video ->
                    Column(modifier = Modifier.width(100.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { vm.openVibeShorts(vm.vibeVideos.indexOf(video)) },
                            contentAlignment = Alignment.Center) {
                            FragmentArt(palette = video.paletteColors.ifEmpty { PALETTES[0] },
                                seed = idx * 0.41f, animating = false)
                            Text("▶", color = Color.White, fontSize = 18.sp)
                        }
                        Text(video.duration, color = c.textSub, fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlineButton("Upload New Video ✦", { vm.showUploadVideo = true },
                modifier = Modifier.fillMaxWidth())
        }
    }
}

// ══════════════════════════════════════════════════════════
//   SETTINGS OVERLAY
// ══════════════════════════════════════════════════════════
@Composable
fun SettingsOverlay(vm: AppViewModel) {
    val c = vm.themeColors
    var activeSection by remember { mutableStateOf("main") }

    // Coming Soon popup
    if (vm.showComingSoon) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.6f))
            .clickable { vm.showComingSoon = false }, contentAlignment = Alignment.Center) {
            GlassCard(modifier = Modifier.padding(horizontal = 40.dp)) {
                Column(modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🚀", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Coming Soon", color = c.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(vm.comingSoonLabel, color = c.textSub, fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp))
                    Spacer(modifier = Modifier.height(18.dp))
                    GradientButton("Got it", { vm.showComingSoon = false })
                }
            }
        }
        return
    }

    when (activeSection) {
        "vibes"   -> VibeManagerScreen(vm = vm, c = c, onBack = { activeSection = "main" })
        "howto"   -> HowToUseScreen(c = c, onBack = { activeSection = "main" })
        "backup"  -> PhoneBackupScreen(vm = vm, c = c, onBack = { activeSection = "main" })
        "emergency" -> EmergencyContactsScreen(vm = vm, c = c, onBack = { activeSection = "main" })
        "cloesedkey" -> CloesedKeySettingsScreen(vm = vm, c = c, onBack = { activeSection = "main" })
        "editprofile" -> EditProfileOverlay(vm = vm, onBack = { activeSection = "main" })
        else -> Column(modifier = Modifier.fillMaxSize().background(c.bg).systemBarsPadding()) {
            Row(modifier = Modifier.fillMaxWidth().background(c.surface2)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showSettings = false })
                Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.text,
                    fontFamily = FontFamily.Serif)
            }
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)) {

                // Edit Profile button
                SectionLabel("Profile")
                SettingsCard {
                    SettingsRow(Icons.Default.Person, "Edit Profile", "Name, handle, bio, photo",
                        onClick = { activeSection = "editprofile" })
                }
                Spacer(modifier = Modifier.height(12.dp))

                SectionLabel("Appearance")
                SettingsCard {
                    SettingsRow(Icons.Default.Palette, "Theme", vm.appTheme.name,
                        onClick = { vm.showThemeModal = true })
                    HorizontalDivider(color = c.border2, thickness = 0.5.dp)
                    SettingsRow(Icons.Default.TextFields, "Font Style", vm.appFont,
                        onClick = { vm.showFontPicker = true })
                }
                Spacer(modifier = Modifier.height(12.dp))

                SectionLabel("Notifications")
                SettingsCard {
                    SettingsRow(Icons.Default.Notifications, "All Notifications",
                        if (vm.notificationsEnabled) "On" else "Off — all alerts paused",
                        toggle = true, toggleOn = vm.notificationsEnabled,
                        onToggle = { vm.notificationsEnabled = it })
                    HorizontalDivider(color = c.border2, thickness = 0.5.dp)
                    SettingsRow(Icons.Default.Circle, "Urgency Lights",
                        toggle = true, toggleOn = true, onToggle = {})
                    HorizontalDivider(color = c.border2, thickness = 0.5.dp)
                    SettingsRow(Icons.Default.AutoAwesome, "Global Muse AI",
                        toggle = true, toggleOn = vm.globalMuse, onToggle = { vm.globalMuse = it })
                    HorizontalDivider(color = c.border2, thickness = 0.5.dp)
                    SettingsRow(Icons.Default.HideSource, "Auto-hide Nav Bar",
                        "Hides bottom nav after 3 seconds",
                        toggle = true, toggleOn = vm.disappearingNavEnabled,
                        onToggle = { vm.disappearingNavEnabled = it; if (!it) vm.navBarVisible = true })
                }
                Spacer(modifier = Modifier.height(12.dp))

                SectionLabel("Language")
                SettingsCard {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp)
                        .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf("en" to "EN","fr" to "FR","es" to "ES","pt" to "PT","de" to "DE","ja" to "JA").forEach { (code, label) ->
                            val isEn = code == "en"
                            TabChip(text = label, selected = vm.currentLang == code,
                                onClick = {
                                    if (isEn) { vm.currentLang = code }
                                    else {
                                        vm.comingSoonLabel = "$label language support"
                                        vm.showComingSoon = true
                                    }
                                })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                SectionLabel("Privacy & Security")
                SettingsCard {
                    SettingsRow(Icons.Default.Lock, "Lock App", "Biometrics or passcode",
                        toggle = true, toggleOn = false, onToggle = {
                            vm.showToast("Security", "App lock ${if (it) "enabled" else "disabled"}", "low")
                        })
                    HorizontalDivider(color = c.border2, thickness = 0.5.dp)
                    SettingsRow(Icons.Default.Key, "CLOESED KEY",
                        if (vm.cloesedKey.isBlank()) "Not set — lock a chat to create" else "Set — manages hidden chats",
                        onClick = if (vm.cloesedKey.isBlank()) null else ({
                            activeSection = "cloesedkey"
                        }))
                    HorizontalDivider(color = c.border2, thickness = 0.5.dp)
                    SettingsRow(Icons.Default.Timer, "Disappearing Messages", "Default timer",
                        toggle = true, toggleOn = false, onToggle = {})
                    HorizontalDivider(color = c.border2, thickness = 0.5.dp)
                    SettingsRow(Icons.Default.VisibilityOff, "Stealth Mode", "Hide read receipts",
                        toggle = true, toggleOn = false, onToggle = {})
                }
                Spacer(modifier = Modifier.height(12.dp))

                SectionLabel("Vibe Videos")
                SettingsCard {
                    SettingsRow(Icons.Default.PlayCircle, "Manage Vibes",
                        "${vm.vibeVideos.size} videos", onClick = { activeSection = "vibes" })
                    HorizontalDivider(color = c.border2, thickness = 0.5.dp)
                    SettingsRow(Icons.Default.AddCircle, "Upload New Vibe",
                        onClick = { vm.showUploadVideo = true })
                    HorizontalDivider(color = c.border2, thickness = 0.5.dp)
                    SettingsRow(Icons.Default.PlayArrow, "Auto-play Vibes",
                        toggle = true, toggleOn = true, onToggle = {})
                }
                Spacer(modifier = Modifier.height(12.dp))

                SectionLabel("Safety")
                SettingsCard {
                    SettingsRow(Icons.Default.Warning, "Emergency Mode",
                        onClick = { vm.showEmergency = true })
                    HorizontalDivider(color = c.border2, thickness = 0.5.dp)
                    SettingsRow(Icons.Default.Phone, "Emergency Contacts",
                        "${vm.profile.emergencyContacts.size} contacts",
                        onClick = { activeSection = "emergency" })
                }
                Spacer(modifier = Modifier.height(12.dp))

                SectionLabel("Backup & Support")
                SettingsCard {
                    SettingsRow(Icons.Default.Smartphone, "Phone Number Backup",
                        onClick = { activeSection = "backup" })
                    HorizontalDivider(color = c.border2, thickness = 0.5.dp)
                    SettingsRow(Icons.Default.Help, "How to Use CLOES",
                        onClick = { activeSection = "howto" })
                }
                Spacer(modifier = Modifier.height(12.dp))

                SectionLabel("About")
                SettingsCard {
                    SettingsRow(Icons.Default.Info, "CLOES v7.1", "Private · Vivid · Yours")
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ─── Emergency Contacts Screen ────────────────────────────────────────────────
@Composable
private fun EmergencyContactsScreen(vm: AppViewModel, c: CloesColors, onBack: () -> Unit) {
    var showAddNew by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf("FRIEND") }

    Column(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Row(modifier = Modifier.fillMaxWidth().background(c.surface2)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeuIconButton(
                icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                onClick = onBack)
            Text("Emergency Contacts", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.text)
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp)) {

            Text("These contacts will be alerted in emergency mode.",
                color = c.textSub, fontSize = 13.sp, modifier = Modifier.padding(bottom = 16.dp))

            // Add from existing contacts
            SectionLabel("Add from your contacts")
            val addable = vm.contacts.filter { contact ->
                vm.profile.emergencyContacts.none { ec -> ec.name == contact.name }
            }.take(5)
            addable.forEach { contact ->
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(14.dp)).background(c.surface)
                    .clickable { vm.addEmergencyContact(contact.name, contact.group) }
                    .padding(horizontal = 13.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))) {
                        FragmentAvatar(paletteIndex = contact.paletteIndex, seed = fragSeed(contact.id))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(contact.name, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(contact.group, color = c.textSub, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.Add, null, tint = Purple, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            SectionLabel("Current emergency contacts")
            if (vm.profile.emergencyContacts.isEmpty()) {
                Text("None added yet", color = c.textSub, fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp))
            } else {
                vm.profile.emergencyContacts.toList().forEach { ec ->
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(14.dp)).background(c.surface)
                        .padding(horizontal = 13.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp).padding(end = 2.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ec.name, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(ec.type, color = c.textSub, fontSize = 11.sp)
                        }
                        IconButton(onClick = { vm.profile.emergencyContacts.remove(ec) }) {
                            Icon(Icons.Default.Close, null, tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            if (showAddNew) {
                SectionLabel("New emergency contact")
                CloesInput(newName, { newName = it }, "Contact name",
                    modifier = Modifier.padding(bottom = 10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 14.dp)) {
                    listOf("FAMILY","FRIEND","DOCTOR","OTHER").forEach { type ->
                        TabChip(text = type, selected = newType == type, onClick = { newType = type })
                    }
                }
                GradientButton("Add Contact", {
                    if (newName.isNotBlank()) {
                        vm.addEmergencyContact(newName, newType)
                        newName = ""; showAddNew = false
                    }
                }, modifier = Modifier.fillMaxWidth())
            } else {
                OutlineButton("+ Add New Emergency Contact", { showAddNew = true },
                    modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

// ─── VibeManagerScreen ────────────────────────────────────────────────────────
@Composable
private fun VibeManagerScreen(vm: AppViewModel, c: CloesColors, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Row(modifier = Modifier.fillMaxWidth().background(c.surface2)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeuIconButton(
                icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                onClick = onBack)
            Text("Manage Vibes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.text,
                fontFamily = FontFamily.Serif, modifier = Modifier.weight(1f))
            NeuIconButton(
                icon = { Icon(Icons.Default.Add, null, tint = Purple, modifier = Modifier.size(18.dp)) },
                onClick = { vm.showUploadVideo = true })
        }
        if (vm.vibeVideos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.VideoLibrary, null, tint = c.textSub, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No vibes yet", color = c.textSub, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    GradientButton("Upload First Vibe", { vm.showUploadVideo = true })
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()) {
                itemsIndexed(vm.vibeVideos) { idx, video ->
                    Row(modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)).background(c.surface).padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp))) {
                            FragmentArt(palette = video.paletteColors.ifEmpty { PALETTES[idx % PALETTES.size] },
                                seed = idx * 0.37f, animating = false)
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(video.title, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text("${video.duration} · ${video.views} views", color = c.textSub, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { vm.openVibeShorts(idx) }) {
                                Icon(Icons.Default.PlayCircle, null, tint = Purple, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = {
                                vm.vibeVideos.removeAt(idx)
                                vm.showToast("Vibe", "${video.title} deleted", "low")
                            }) {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── How To Use Screen ────────────────────────────────────────────────────────
@Composable
private fun HowToUseScreen(c: CloesColors, onBack: () -> Unit) {
    val steps = listOf(
        Icons.Default.Person       to "Create your Fragment" to "Your Fragment is your identity — a unique light pattern only you have.",
        Icons.Default.ChatBubble   to "Start a conversation" to "Find someone by their handle and tap to start a private, encrypted chat.",
        Icons.Default.Favorite     to "Bloom — check your bonds" to "See how your relationships are doing. Bloom tracks connection health over time.",
        Icons.Default.Groups       to "Circles" to "Group your contacts. Send circle-wide messages, set a Muse tone per group.",
        Icons.Default.AutoAwesome  to "Meet Muse" to "Your private AI companion. Schedule, set tasks, or just talk.",
        Icons.Default.PlayCircle   to "Vibe" to "Watch short videos shared by your world. React and share your own.",
        Icons.Default.MonetizationOn to "Coins" to "Earn and spend CLOES Coins for premium features and stickers.",
        Icons.Default.Warning      to "Emergency mode" to "One tap to alert your emergency contacts with your location.",
        Icons.Default.Lock         to "Privacy first" to "All messages are end-to-end encrypted. No ads. No data selling."
    )
    Column(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Row(modifier = Modifier.fillMaxWidth().background(c.surface2)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeuIconButton(
                icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                onClick = onBack)
            Text("How to Use CLOES", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.text)
        }
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(steps) { idx, (pair, desc) ->
                val (icon, title) = pair
                Row(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)).background(c.surface)
                    .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                        .background(Purple.copy(0.12f)), contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = Purple, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("${idx + 1}. $title", color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(desc, color = c.textSub, fontSize = 12.sp,
                            modifier = Modifier.padding(top = 3.dp))
                    }
                }
            }
        }
    }
}

// ─── Phone Backup Screen ──────────────────────────────────────────────────────
@Composable
private fun PhoneBackupScreen(vm: AppViewModel, c: CloesColors, onBack: () -> Unit) {
    var phone by remember { mutableStateOf(vm.phoneBackup) }
    Column(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Row(modifier = Modifier.fillMaxWidth().background(c.surface2)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeuIconButton(
                icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                onClick = onBack)
            Text("Phone Backup", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.text)
        }
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp)) {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Smartphone, null, tint = Purple, modifier = Modifier.size(18.dp))
                        Text("Phone Number Backup", color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text("Link your phone number for account recovery. Never shared or used for ads.",
                        color = c.textSub, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp, bottom = 12.dp))
                    CloesInput(phone, { phone = it }, "+1 (555) 000-0000",
                        modifier = Modifier.padding(bottom = 12.dp))
                    GradientButton("Save Backup Number", {
                        vm.phoneBackup = phone
                        vm.showToast("Backup", "Phone number saved securely", "low")
                        onBack()
                    }, modifier = Modifier.fillMaxWidth())
                }
            }
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, null, tint = Purple, modifier = Modifier.size(18.dp))
                        Text("Your number is", color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    listOf("End-to-end encrypted", "Never sold or shared", "Used only for account recovery").forEach { item ->
                        Row(modifier = Modifier.padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF22C55E), modifier = Modifier.size(14.dp))
                            Text(item, color = c.textSub, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ─── CLOESED KEY Settings Screen ─────────────────────────────────────────────
@Composable
private fun CloesedKeySettingsScreen(vm: AppViewModel, c: CloesColors, onBack: () -> Unit) {
    var showChangeDialog by remember { mutableStateOf(false) }
    var showKey by remember { mutableStateOf(false) }
    val lockedCount = vm.contacts.count { it.locked }

    Column(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Row(modifier = Modifier.fillMaxWidth().background(c.surface2)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeuIconButton(
                icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                onClick = onBack)
            Text("CLOESED KEY", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.text)
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 20.dp)) {

            // Info card
            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, null, tint = Purple, modifier = Modifier.size(22.dp))
                        Text("Your CLOESED KEY", color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Your 4-digit key hides selected chats from your contact list. Type it in the search bar to temporarily reveal them. Only you know this key.",
                        color = c.textSub, fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)).background(Purple.copy(0.08f))
                        .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Hidden chats", color = c.textSub, fontSize = 11.sp)
                            Text("$lockedCount locked", color = c.text, fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold)
                        }
                        Icon(Icons.Default.Lock, null, tint = Purple, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Current Key display — show/hide with eye icon
            SectionLabel("Current Key")
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                .clip(RoundedCornerShape(14.dp)).background(c.surface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    if (showKey) vm.cloesedKey else "•".repeat(vm.cloesedKey.length),
                    color = c.text, fontSize = 20.sp, letterSpacing = 6.sp
                )
                IconButton(onClick = { showKey = !showKey }) {
                    Icon(
                        if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle visibility",
                        tint = Purple, modifier = Modifier.size(20.dp)
                    )
                }
            }

            GradientButton("Change CLOESED KEY", { showChangeDialog = true },
                modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(12.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    listOf(
                        Icons.Default.Visibility to "Type in search bar to reveal hidden chats",
                        Icons.Default.VisibilityOff to "Hidden chats disappear after leaving search",
                        Icons.Default.Key to "Key must be exactly 4 characters",
                        Icons.Default.Warning to "CLOES cannot recover a lost key"
                    ).forEach { (icon, text) ->
                        Row(modifier = Modifier.padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, null, tint = c.textMid, modifier = Modifier.size(15.dp))
                            Text(text, color = c.textSub, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (showChangeDialog) {
        ChangeCloesedKeyDialog(vm = vm, onDismiss = { showChangeDialog = false })
    }
}


@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    val c = cloesColors()
    Column(modifier = Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(c.surface)
        .border(1.dp, c.border2, RoundedCornerShape(16.dp)),
        content = content)
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    sub: String = "",
    toggle: Boolean = false,
    toggleOn: Boolean = false,
    onToggle: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val c = cloesColors()
    Row(modifier = Modifier.fillMaxWidth()
        .clickable(enabled = onClick != null || toggle) { onClick?.invoke() }
        .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)) {
        Icon(icon, null, tint = c.textMid, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = c.text, fontSize = 14.sp)
            if (sub.isNotBlank()) Text(sub, color = c.textSub, fontSize = 12.sp)
        }
        if (toggle && onToggle != null) {
            CloesToggle(checked = toggleOn, onCheckedChange = onToggle)
        } else if (onClick != null) {
            Icon(Icons.Default.ChevronRight, null, tint = c.textSub, modifier = Modifier.size(16.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════
//   EDIT PROFILE OVERLAY — separate from settings
// ══════════════════════════════════════════════════════════
@Composable
fun EditProfileOverlay(vm: AppViewModel, onBack: (() -> Unit)? = null) {
    val c = vm.themeColors
    var editName   by remember { mutableStateOf(vm.profile.name) }
    var editHandle by remember { mutableStateOf(vm.profile.handle.removePrefix("@")) }
    var editBio    by remember { mutableStateOf(vm.profile.bio) }
    var showLight  by remember { mutableStateOf(false) }   // double-tap shows light

    Column(modifier = Modifier.fillMaxSize().background(c.bg)) {
        // Top bar
        Row(modifier = Modifier.fillMaxWidth().background(c.surface2)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            NeuIconButton(
                icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                onClick = { if (onBack != null) onBack() else vm.showEditProfile = false })
            Spacer(modifier = Modifier.weight(1f))
            Text("Edit Profile", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(Pink, Purple)))
                .clickable {
                    vm.profile = vm.profile.copy(
                        name = editName,
                        handle = "@${editHandle}",
                        bio = editBio
                    )
                    vm.saveProfile()
                    if (onBack != null) onBack()
                }
                .padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp)) {

            // Profile picture / Light — tap light to reveal QR code
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(24.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (showLight) vm.showQrCode = true   // tap light → QR
                            },
                            onDoubleTap = { showLight = !showLight }
                        )
                    }
                ) {
                    if (showLight) {
                        FragmentArt(palette = vm.profile.palette.ifEmpty { listOf(Pink, Purple, Cyan) },
                            seed = vm.profile.lightSeed, animating = true)
                        // Small QR hint badge
                        Box(modifier = Modifier.align(Alignment.BottomEnd)
                            .size(22.dp).clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(0.55f)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.QrCodeScanner, null, tint = Color.White,
                                modifier = Modifier.size(14.dp))
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(
                            Brush.linearGradient(listOf(Purple.copy(0.3f), Pink.copy(0.3f)))),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = Color.White.copy(0.6f),
                                modifier = Modifier.size(48.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (showLight) "Tap your light to share QR · Double-tap for photo"
                else "Double-tap to switch to your light",
                color = c.textSub, fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Photo picker
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = { vm.showToast("Profile", "Photo picker coming soon", "low") }) {
                    Icon(Icons.Default.PhotoLibrary, null, tint = c.textSub, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change Photo", color = c.textSub, fontSize = 13.sp)
                }
                TextButton(onClick = { vm.showToast("Profile", "Light regenerated", "low") }) {
                    Icon(Icons.Default.Refresh, null, tint = c.textSub, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Light", color = c.textSub, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel("Display Name")
            CloesInput(editName, { editName = it }, "Your name",
                modifier = Modifier.padding(bottom = 13.dp))
            SectionLabel("@Handle")
            CloesInput(editHandle, { editHandle = it }, "your.handle",
                modifier = Modifier.padding(bottom = 13.dp))
            SectionLabel("Bio")
            TextField(
                value = editBio, onValueChange = { editBio = it },
                placeholder = { Text("A little about you...", color = c.textSub) },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
                    .background(c.bg).padding(bottom = 16.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = c.bg, focusedContainerColor = c.bg,
                    unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent,
                    unfocusedTextColor = c.text, focusedTextColor = c.text
                )
            )
        }
    }

    // ── QR Code full-screen overlay ───────────────────────────────────────────
    if (vm.showQrCode) {
        ProfileQrOverlay(vm = vm, onDismiss = { vm.showQrCode = false })
    }
}


// ══════════════════════════════════════════════════════════
//   EDIT CONTACT OVERLAY
// ══════════════════════════════════════════════════════════
@Composable
fun EditContactOverlay(vm: AppViewModel) {
    val c = vm.themeColors
    val contact = vm.currentContact() ?: run { vm.showEditContact = false; return }

    var editName    by remember { mutableStateOf(contact.name) }
    var editHandle  by remember { mutableStateOf(contact.handle.removePrefix("@")) }
    var editGroup   by remember { mutableStateOf(contact.group) }
    var editUrgency by remember { mutableStateOf(contact.urgency) }
    var editFrag    by remember { mutableStateOf(contact.paletteIndex) }
    // Which circles this contact belongs to
    val memberCircles = remember {
        mutableStateListOf<Long>().also { list ->
            list.addAll(vm.groups.filter { g -> g.members.contains(contact.id) }.map { it.name.hashCode().toLong() })
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(c.bg)) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth().background(c.surface2)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            NeuIconButton(
                icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                onClick = { vm.showEditContact = false })
            Spacer(modifier = Modifier.weight(1f))
            Text("Edit Contact", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            // Save button
            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(Pink, Purple)))
                .clickable {
                    val idx = vm.contacts.indexOfFirst { it.id == contact.id }
                    if (idx >= 0) {
                        vm.contacts[idx] = contact.copy(
                            name = editName.ifBlank { contact.name },
                            handle = "@${editHandle.ifBlank { contact.handle.removePrefix("@") }}",
                            group = editGroup,
                            urgency = editUrgency,
                            paletteIndex = editFrag
                        )
                        // Update circles membership
                        vm.groups.forEach { group ->
                            val wasIn = group.members.contains(contact.id)
                            val shouldBeIn = memberCircles.contains(group.name.hashCode().toLong())
                            if (!wasIn && shouldBeIn) group.members.add(contact.id)
                            else if (wasIn && !shouldBeIn) group.members.remove(contact.id)
                        }
                        vm.showToast(editName, "Contact updated", "low")
                    }
                    vm.showEditContact = false
                }
                .padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 18.dp)) {

            // ── Fragment / Avatar preview ─────────────────────────────────
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(22.dp))
                .align(Alignment.CenterHorizontally)) {
                FragmentAvatar(paletteIndex = editFrag, seed = fragSeed(contact.id))
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Fragment picker
            SectionLabel("Fragment Style")
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PALETTES.forEachIndexed { idx, palette ->
                    Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(13.dp))
                        .border(if (editFrag == idx) 3.dp else 0.dp, Purple, RoundedCornerShape(13.dp))
                        .clickable { editFrag = idx }) {
                        FragmentArt(palette = palette, seed = idx * 0.13f + 0.05f)
                    }
                }
            }

            // ── Name ─────────────────────────────────────────────────────
            SectionLabel("Display Name")
            CloesInput(editName, { editName = it }, "Contact name",
                modifier = Modifier.padding(bottom = 13.dp))

            // ── Handle ───────────────────────────────────────────────────
            SectionLabel("@Handle")
            CloesInput(editHandle, { editHandle = it }, "their.handle",
                modifier = Modifier.padding(bottom = 13.dp))

            // ── Group (category label) ────────────────────────────────────
            SectionLabel("Group Category")
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("BESTIE", "FRIENDS", "WORK", "FAMILY", "SCHOOL", "OTHER").forEach { grp ->
                    TabChip(text = grp, selected = editGroup == grp, onClick = { editGroup = grp })
                }
            }

            // ── Urgency ───────────────────────────────────────────────────
            SectionLabel("Urgency Level")
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "low"  to ("Low" to Color(0xFF22C55E)),
                    "mid"  to ("Medium" to Color(0xFFF59E0B)),
                    "high" to ("High" to Color(0xFFEF4444))
                ).forEach { (level, pair) ->
                    val (label, color) = pair
                    val sel = editUrgency == level
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(if (sel) color.copy(0.15f) else c.surface)
                        .border(if (sel) 1.5.dp else 0.5.dp,
                            if (sel) color else c.border2, RoundedCornerShape(14.dp))
                        .clickable { editUrgency = level }
                        .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                            Text(label, color = if (sel) color else c.textMid, fontSize = 12.sp,
                                fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }
                }
            }

            // ── Circles membership ────────────────────────────────────────
            SectionLabel("Circles")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 20.dp)) {
                vm.groups.forEach { group ->
                    val inCircle = memberCircles.contains(group.name.hashCode().toLong())
                    Row(modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (inCircle) Purple.copy(0.08f) else c.surface)
                        .border(if (inCircle) 1.5.dp else 0.5.dp,
                            if (inCircle) Purple else c.border2, RoundedCornerShape(14.dp))
                        .clickable {
                            val key = group.name.hashCode().toLong()
                            if (inCircle) memberCircles.remove(key)
                            else memberCircles.add(key)
                        }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(group.emoji, fontSize = 18.sp)
                        Text(group.name, color = if (inCircle) Purple else c.text, fontSize = 14.sp,
                            fontWeight = if (inCircle) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f))
                        if (inCircle) Icon(Icons.Default.CheckCircle, null, tint = Purple,
                            modifier = Modifier.size(18.dp))
                        else Box(modifier = Modifier.size(18.dp).clip(CircleShape)
                            .border(1.5.dp, c.border2, CircleShape))
                    }
                }
            }

            // ── Danger zone ───────────────────────────────────────────────
            SectionLabel("Danger Zone")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Block
                    Row(modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { vm.showToast(contact.name, "Contact blocked", "low"); vm.showEditContact = false }
                        .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Block, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                        Text("Block ${contact.name}", color = Color(0xFFF59E0B), fontSize = 13.sp)
                    }
                    // Delete
                    Row(modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            vm.contacts.remove(contact)
                            vm.showToast("Removed", "${contact.name} removed", "low")
                            vm.closeChat()
                            vm.showEditContact = false
                        }
                        .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonRemove, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Text("Remove ${contact.name}", color = Color(0xFFEF4444), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
//   PROFILE QR OVERLAY
// ──────────────────────────────────────────────────────────────────────────────
@Composable
fun ProfileQrOverlay(vm: AppViewModel, onDismiss: () -> Unit) {
    val c = vm.themeColors
    val handle = vm.profile.handle.ifBlank { "@${vm.profile.name.lowercase().replace(" ", ".")}" }
    val profileUrl = "cloes://join/$handle"

    // Generate a deterministic "QR-like" bit matrix from the profile URL
    val seed = profileUrl.hashCode().toLong()
    val qrSize = 25
    val bits = remember(profileUrl) {
        val rng = java.util.Random(seed)
        // Corner finder patterns (7x7 squares) + random data modules
        Array(qrSize) { row ->
            BooleanArray(qrSize) { col ->
                // Top-left finder
                val tlFinder = row < 7 && col < 7
                val tlInner = row in 1..5 && col in 1..5
                val tlCore  = row in 2..4 && col in 2..4
                // Top-right finder
                val trFinder = row < 7 && col >= qrSize - 7
                val trInner = row in 1..5 && col >= qrSize - 6 && col <= qrSize - 2
                val trCore  = row in 2..4 && col >= qrSize - 5 && col <= qrSize - 3
                // Bottom-left finder
                val blFinder = row >= qrSize - 7 && col < 7
                val blInner = row >= qrSize - 6 && row <= qrSize - 2 && col in 1..5
                val blCore  = row >= qrSize - 5 && row <= qrSize - 3 && col in 2..4

                when {
                    tlFinder -> !tlInner || tlCore
                    trFinder -> !trInner || trCore
                    blFinder -> !blInner || blCore
                    // Timing pattern
                    row == 6 && col in 8 until qrSize - 8 -> col % 2 == 0
                    col == 6 && row in 8 until qrSize - 8 -> row % 2 == 0
                    // Alignment pattern (bottom-right area)
                    row in 16..20 && col in 16..20 -> {
                        val ar = row - 18; val ac = col - 18
                        ar in -2..2 && ac in -2..2 && (ar !in -1..1 || ac !in -1..1 || (ar == 0 && ac == 0))
                    }
                    else -> rng.nextBoolean()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .padding(32.dp)
                .clickable(enabled = false) {} // prevent close when tapping card
        ) {
            // Header
            Text(
                "Scan to join my world",
                color = Color(0xFF1A0A2E),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(handle, color = Color(0xFF8B5CF6), fontSize = 13.sp)

            // Fragment light watermark + QR grid
            Box(contentAlignment = Alignment.Center) {
                // QR canvas
                Canvas(modifier = Modifier.size(220.dp)) {
                    val cellSize = size.width / qrSize
                    val quietZone = cellSize * 0.5f
                    bits.forEachIndexed { row, rowBits ->
                        rowBits.forEachIndexed { col, on ->
                            if (on) {
                                // Rounded cells for data, square for finder patterns
                                val isFinderOrTiming = (row < 8 && col < 8) ||
                                    (row < 8 && col >= qrSize - 8) ||
                                    (row >= qrSize - 8 && col < 8) ||
                                    row == 6 || col == 6
                                val r = if (isFinderOrTiming) 0f else cellSize * 0.3f
                                drawRoundRect(
                                    color = android.graphics.Color.valueOf(0.1f, 0.04f, 0.18f, 1f)
                                        .let { androidx.compose.ui.graphics.Color(it.toArgb()) },
                                    topLeft = androidx.compose.ui.geometry.Offset(
                                        col * cellSize + quietZone * 0.1f,
                                        row * cellSize + quietZone * 0.1f
                                    ),
                                    size = androidx.compose.ui.geometry.Size(
                                        cellSize - quietZone * 0.2f,
                                        cellSize - quietZone * 0.2f
                                    ),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
                                )
                            }
                        }
                    }
                }

                // Fragment light in the center (logo area)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(3.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    FragmentArt(
                        palette = vm.profile.palette.ifEmpty { listOf(Pink, Purple, Cyan) },
                        seed = vm.profile.lightSeed,
                        animating = false
                    )
                }
            }

            // Handle label
            Text(
                profileUrl,
                color = Color(0xFF6B7280),
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )

            // Share button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(listOf(Pink, Purple))
                    )
                    .clickable { vm.showToast("QR", "Share link copied!", "low") }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("Share my world", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Text(
            "Tap anywhere to close",
            color = Color.White.copy(0.3f),
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        )
    }
}
