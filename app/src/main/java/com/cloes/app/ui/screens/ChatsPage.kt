package com.cloes.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.cloes.app.data.*
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel

@Composable
fun ChatsPage(vm: AppViewModel) {
    val c = vm.themeColors
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Messages", color = c.text, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.PersonAdd, "Add Contact", tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showAddContact = true }
                )
                NeuIconButton(
                    icon = { Icon(Icons.Default.GroupAdd, "Create Group", tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showCreateGroupChat = true }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(13.dp)).background(c.surface),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = c.textSub,
                modifier = Modifier.padding(start = 12.dp, end = 8.dp).size(16.dp))
            TextField(value = searchQuery, onValueChange = { searchQuery = it },
                placeholder = { Text("Search connections...", color = c.textSub, fontSize = 13.sp) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent,
                    unfocusedTextColor = c.text, focusedTextColor = c.text),
                modifier = Modifier.fillMaxWidth())
        }

        val keyTyped = vm.cloesedKey.isNotBlank() && searchQuery.trim() == vm.cloesedKey
        if (keyTyped && !vm.showLockedContacts) vm.showLockedContacts = true
        if (!keyTyped && vm.showLockedContacts) vm.showLockedContacts = false

        val filtered = vm.contacts.filter { contact ->
            val visibilityOk = !contact.locked || vm.showLockedContacts
            val searchOk = searchQuery.isBlank() || keyTyped ||
                contact.name.contains(searchQuery, true) || contact.handle.contains(searchQuery, true)
            visibilityOk && searchOk
        }.sortedWith(compareByDescending<Contact> { it.pinned }.thenByDescending { it.urgency == "high" })
        val grouped = filtered.groupBy { it.group }

        LazyColumn(modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {

            // Group chats scroll naturally (not pinned)
            if (vm.groupChats.isNotEmpty()) {
                item {
                    Text("GROUP CHATS", color = c.textSub, fontSize = 8.5.sp, letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp))
                }
                items(vm.groupChats, key = { "gc_${it.id}" }) { gc ->
                    GroupChatRow(gc = gc, vm = vm, c = c)
                }
                item { Spacer(modifier = Modifier.height(6.dp)) }
            }

            grouped.forEach { (group, contacts) ->
                val grp = vm.groups.find { it.name == group }
                item {
                    Text("${grp?.emoji ?: ""} $group", color = c.textSub, fontSize = 8.5.sp,
                        letterSpacing = 1.4.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 9.dp))
                }
                items(contacts, key = { it.id }) { contact ->
                    ContactRow(vm = vm, contact = contact, c = c)
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Profile viewer overlay
    val profileContact = vm.contacts.find { it.id == vm.profileViewContactId }
    if (profileContact != null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.92f))
                .pointerInput(Unit) { detectTapGestures(onTap = { vm.profileViewContactId = null }) },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.padding(32.dp)) {
                Box(modifier = Modifier.size(260.dp).clip(RoundedCornerShape(40.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = { vm.profileViewShowLight = !vm.profileViewShowLight })
                    }) {
                    if (vm.profileViewShowLight) {
                        FragmentAvatar(paletteIndex = profileContact.paletteIndex, seed = fragSeed(profileContact.id))
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(
                            Brush.radialGradient(listOf(Purple.copy(0.6f), Pink.copy(0.4f), Color(0xFF12061E)))),
                            contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.Person, null, tint = Color.White.copy(0.85f), modifier = Modifier.size(96.dp))
                                Text(profileContact.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(profileContact.handle, color = Color.White.copy(0.5f), fontSize = 13.sp)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (vm.profileViewShowLight) Icons.Default.AutoAwesome else Icons.Default.Person,
                        null, tint = Color.White.copy(0.45f), modifier = Modifier.size(14.dp))
                    Text(if (vm.profileViewShowLight) "Their Fragment light · double-tap for photo"
                         else "Profile photo · double-tap for their Fragment light",
                        color = Color.White.copy(0.45f), fontSize = 12.sp)
                }
            }
            Text("Tap anywhere to close", color = Color.White.copy(0.25f), fontSize = 11.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 36.dp))
        }
    }
    } // end Box
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupChatRow(gc: com.cloes.app.data.GroupChat, vm: AppViewModel, c: CloesColors) {
    Row(modifier = Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .combinedClickable(
            onClick = { vm.currentGroupChatId = gc.id; vm.showGroupChatView = true },
            onLongClick = { vm.currentGroupChatId = gc.id; vm.showLeaveGroup = true }
        )
        .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Purple.copy(0.3f), Pink.copy(0.3f)))),
            contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Group, null, tint = Purple, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(gc.name, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            val lastMsg = gc.messages.lastOrNull()
            if (lastMsg != null) Text(lastMsg.text, color = c.textSub, fontSize = 12.sp, maxLines = 1)
            else Text("${gc.memberIds.size} members · Tap to chat", color = c.textMid, fontSize = 12.sp)
        }
        Text(gc.messages.lastOrNull()?.timestamp ?: "", color = c.textSub, fontSize = 10.sp)
    }
}

// ── Contact row with emoji long-press ─────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactRow(vm: AppViewModel, contact: Contact, c: CloesColors) {
    var showEmojiPicker by remember { mutableStateOf(false) }
    var rowEmoji by remember { mutableStateOf<String?>(null) }

    Column {
        Row(modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = {
                    showEmojiPicker = false
                    if (contact.locked) vm.showToast("Locked", "Type your CLOESED KEY in search to reveal", "mid")
                    else vm.openChat(contact.id)
                },
                onLongClick = { showEmojiPicker = !showEmojiPicker }
            )
            .padding(horizontal = 13.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            Box(modifier = Modifier.size(46.dp)) {
                Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { vm.profileVibeCreator = contact.name; vm.showProfileVibe = true },
                            onLongPress = { vm.profileViewShowLight = false; vm.profileViewContactId = contact.id }
                        )
                    }) {
                    FragmentAvatar(paletteIndex = contact.paletteIndex, seed = fragSeed(contact.id))
                }
                Box(modifier = Modifier.size(10.dp).align(Alignment.BottomEnd)
                    .clip(CircleShape).background(if (contact.online) Lime else c.textSub)
                    .border(1.5.dp, c.bg, CircleShape))
                if (contact.pinned) Text("📌", fontSize = 8.sp, modifier = Modifier.align(Alignment.TopStart))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(contact.name, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    rowEmoji?.let { Text(it, fontSize = 15.sp) }
                }
                Text(contact.messages.lastOrNull()?.text ?: "...", color = c.textMid, fontSize = 13.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (contact.unread > 0) {
                    Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(Pink),
                        contentAlignment = Alignment.Center) {
                        Text("${contact.unread}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                UrgencyPulse(urgency = contact.urgency)
                contact.messages.lastOrNull()?.let { Text(it.timestamp, color = c.textSub, fontSize = 10.sp) }
            }
        }

        // Emoji picker (appears below the row after long press)
        if (showEmojiPicker) {
            ContactEmojiPicker(
                c = c,
                currentEmoji = rowEmoji,
                onEmojiSelected = { emoji -> rowEmoji = emoji; showEmojiPicker = false },
                onClear = { rowEmoji = null; showEmojiPicker = false },
                onDismiss = { showEmojiPicker = false }
            )
        }
    }
}

// ── Quick emoji picker strip ───────────────────────────────────────────────────
@Composable
private fun ContactEmojiPicker(
    c: CloesColors,
    currentEmoji: String?,
    onEmojiSelected: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val quickEmojis = listOf("💜", "❤️", "😂", "😮", "😢", "👍", "🔥", "🌙", "✦", "💫", "🫶", "😍")
    val extrasPool = listOf("🎉","🦋","🌸","⚡","🌊","🦄","💎","🎭","🌈","🪩","🫧","🌿","🍀","🎯","🦊","🐝","🌻","🫰","🤙","🙌","👏","🥹")

    Box(modifier = Modifier.fillMaxWidth()
        .padding(start = 58.dp, end = 12.dp, bottom = 6.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(c.surface2)
        .border(1.dp, Purple.copy(0.25f), RoundedCornerShape(18.dp))
        .padding(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("React", color = c.textSub, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (currentEmoji != null) {
                        TextButton(onClick = onClear,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                            Text("Clear", color = Color(0xFFEF4444), fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(22.dp)) {
                        Icon(Icons.Default.Close, null, tint = c.textSub, modifier = Modifier.size(13.dp))
                    }
                }
            }
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically) {
                quickEmojis.forEach { emoji ->
                    val selected = currentEmoji == emoji
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(if (selected) Purple.copy(0.2f) else Color.Transparent)
                        .border(if (selected) 1.5.dp else 0.dp,
                            if (selected) Purple else Color.Transparent, CircleShape)
                        .clickable { onEmojiSelected(emoji) },
                        contentAlignment = Alignment.Center) {
                        Text(emoji, fontSize = 19.sp)
                    }
                }
                // + button picks a random emoji from extended pool (simulates keyboard emoji)
                Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(Purple.copy(0.12f))
                    .border(1.dp, Purple.copy(0.3f), CircleShape)
                    .clickable { onEmojiSelected(extrasPool.random()) },
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, null, tint = Purple, modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}
