package com.cloes.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cloes.app.data.*
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatView(vm: AppViewModel) {
    val c = vm.themeColors
    val contact = vm.currentContact() ?: return
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var dragStart by remember { mutableStateOf(0f) }

    // Simulate typing then reply
    LaunchedEffect(contact.messages.size) {
        if (contact.messages.lastOrNull()?.sent == true) {
            vm.typingContactId = contact.id
            delay(1000)
            vm.typingContactId = null
            delay(300)
            val replies = listOf(
                "got it!", "okay ✦", "love that for us", "on it!",
                "haha yes exactly", "wait really??", "omg", "noted! 💜"
            )
            contact.messages.add(
                Message(text = replies.random(), sent = false, timestamp = vmTime())
            )
            scope.launch { listState.animateScrollToItem(contact.messages.size - 1) }
        }
    }
    LaunchedEffect(contact.messages.size) {
        if (contact.messages.lastOrNull()?.sent == true) {
            delay(1200)
            val replies = listOf(
                "😊", "got it!", "okay ✦", "love that for us 💜", "on it!",
                "haha yes exactly", "wait really??", "omg 🫶"
            )
            contact.messages.add(
                Message(text = replies.random(), sent = false, timestamp = vmTime())
            )
            scope.launch { listState.animateScrollToItem(contact.messages.size - 1) }
        }
    }

    LaunchedEffect(contact.id) {
        if (contact.messages.isNotEmpty()) {
            delay(100)
            listState.scrollToItem(contact.messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(c.bg)   // solid bg prevents bleed-through overlap
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragStart = 0f },
                    onHorizontalDrag = { _, dragAmount -> dragStart += dragAmount },
                    onDragEnd = { if (dragStart > 80f) vm.closeChat() }
                )
            }
    ) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())

        if (vm.urgencyTintOn) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEF4444).copy(alpha = 0.07f)))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            ChatTopBar(vm = vm, contact = contact, c = c)

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(c.surface)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Today", color = c.textSub, fontSize = 11.sp)
                        }
                    }
                }
                items(contact.messages, key = { it.id }) { msg ->
                    if (msg.expiresAt == null || System.currentTimeMillis() < msg.expiresAt) {
                        MessageBubble(msg = msg, vm = vm, c = c)
                    }
                }
            }

            AnimatedVisibility(visible = vm.showStickerPanel) {
                StickerPanel(vm = vm)
            }


            // Typing indicator — "Reply coming..."
            if (vm.typingContactId == contact.id) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Purple.copy(0.1f))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            "Reply coming... \u270e",
                            color = Purple,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            ChatInputBar(vm = vm, c = c, contact = contact, scope = scope, listState = listState)
        }
    }
}

@Composable
private fun ChatTopBar(vm: AppViewModel, contact: Contact, c: com.cloes.app.ui.theme.CloesColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface2)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NeuIconButton(
            icon = {
                Icon(Icons.Default.ArrowBack, "Back", tint = c.textMid, modifier = Modifier.size(18.dp))
            },
            onClick = { vm.closeChat() }
        )
        var showFullPhoto by remember { mutableStateOf(false) }
        var showLightMode by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { showFullPhoto = true },
                        onDoubleTap = { showLightMode = !showLightMode }
                    )
                }
        ) {
            FragmentAvatar(paletteIndex = contact.paletteIndex, seed = fragSeed(contact.id))
        }

        // Full screen profile photo overlay (long press)
        if (showFullPhoto) {
            Box(modifier = Modifier.fillMaxSize()
                .background(Color.Black.copy(0.9f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showFullPhoto = false },
                        onDoubleTap = { showLightMode = !showLightMode }
                    )
                },
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(32.dp))
                    ) {
                        // Toggle between photo and light on double tap
                        if (showLightMode) {
                            FragmentArt(
                                palette = com.cloes.app.ui.theme.PALETTES[contact.paletteIndex % com.cloes.app.ui.theme.PALETTES.size],
                                seed = fragSeed(contact.id), animating = true
                            )
                        } else {
                            // Show profile picture (fragment art as stand-in for real photo)
                            FragmentAvatar(paletteIndex = contact.paletteIndex, seed = fragSeed(contact.id))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(contact.name, color = Color.White, fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(if (showLightMode) "Their light ✦ — double-tap to see photo" else "Double-tap to see their light ✦",
                        color = Color.White.copy(0.6f), fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Tap anywhere to close", color = Color.White.copy(0.4f), fontSize = 11.sp)
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(contact.name, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(
                if (contact.online) "Active now" else "Last seen recently — Cloed",
                color = if (contact.online) Lime else c.textSub,
                fontSize = 11.sp
            )
        }
        val groupHasMuse = vm.groups.find { it.name == contact.group }?.muse == true
        if (groupHasMuse || vm.globalMuse) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Pink.copy(0.15f), Purple.copy(0.15f))))
                    .border(1.dp, Purple.copy(0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("✦ Muse", color = Purple, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        NeuIconButton(
            icon = { Icon(Icons.Default.Call, "Call", tint = c.textMid, modifier = Modifier.size(16.dp)) },
            onClick = { vm.triggerCall(contact) }
        )
        NeuIconButton(
            icon = { Icon(Icons.Default.MoreVert, "Menu", tint = c.textMid, modifier = Modifier.size(16.dp)) },
            onClick = { vm.showChatMenu = true }
        )
    }
}

@Composable
private fun MessageBubble(msg: Message, vm: AppViewModel, c: com.cloes.app.ui.theme.CloesColors) {
    var showReactionPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = if (msg.sent) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (msg.sent) Alignment.End else Alignment.Start
        ) {
            when (msg.type) {
                MsgType.Poll    -> PollBubble(msg, vm, c)
                MsgType.Sticker -> Text(msg.text, fontSize = 48.sp)
                MsgType.File    -> FileBubble(msg, c)
                else            -> TextBubble(msg, vm, c, showReactionPicker) { showReactionPicker = it }
            }

            msg.reactionEmoji?.let {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(c.surface)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) { Text(it, fontSize = 14.sp) }
            }

            Text(
                msg.timestamp, color = c.textSub, fontSize = 9.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TextBubble(
    msg: Message, vm: AppViewModel, c: com.cloes.app.ui.theme.CloesColors,
    showPicker: Boolean, onTogglePicker: (Boolean) -> Unit
) {
    val bubbleBg = if (msg.sent)
        Brush.linearGradient(listOf(vm.bubbleSent1, vm.bubbleSent2))
    else Brush.horizontalGradient(listOf(vm.bubbleRecvBg, vm.bubbleRecvBg))
    val textColor = if (msg.sent) Color.White else vm.bubbleRecvTxt
    val isDeleted = msg.type == MsgType.Deleted

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(
                topStart = 18.dp, topEnd = 18.dp,
                bottomStart = if (msg.sent) 18.dp else 4.dp,
                bottomEnd = if (msg.sent) 4.dp else 18.dp
            ))
            .background(brush = if (isDeleted) Brush.horizontalGradient(listOf(c.surface2, c.surface2)) else bubbleBg)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    vm.selectedBubbleId = msg.id
                    onTogglePicker(!showPicker)
                }
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        if (isDeleted) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Block, null, tint = c.textSub, modifier = Modifier.size(13.dp))
                Text(msg.text, color = c.textSub, fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        } else {
            Text(msg.text, color = textColor, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }

    if (showPicker && !isDeleted) {
        Column(modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(c.surface2).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Reaction row
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 4.dp)) {
                listOf("💜", "❤️", "😂", "😮", "😢", "👍").forEach { emoji ->
                    Text(emoji, fontSize = 20.sp,
                        modifier = Modifier.clickable {
                            val contact = vm.currentContact()
                            val idx = contact?.messages?.indexOfFirst { it.id == msg.id } ?: -1
                            if (idx >= 0) contact!!.messages[idx] = msg.copy(reactionEmoji = emoji)
                            onTogglePicker(false)
                        })
                }
            }
            HorizontalDivider(color = c.border2, thickness = 0.5.dp)
            // Edit (only own messages)
            if (msg.sent) {
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .clickable { vm.selectedBubbleId = msg.id; vm.showEditBubble = true; onTogglePicker(false) }
                    .padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, null, tint = Purple, modifier = Modifier.size(15.dp))
                    Text("Edit message", color = c.text, fontSize = 13.sp)
                }
            }
            // Pin Convo
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                .clickable { vm.pinBubble(msg.id); onTogglePicker(false) }
                .padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PushPin, null,
                    tint = if (vm.pinnedBubbleId == msg.id) Gold else Purple,
                    modifier = Modifier.size(15.dp))
                Text(if (vm.pinnedBubbleId == msg.id) "Unpin Convo" else "Pin Convo",
                    color = c.text, fontSize = 13.sp)
            }
            // Delete
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                .clickable { vm.selectedBubbleId = msg.id; vm.showDeleteBubble = true; onTogglePicker(false) }
                .padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                Text("Delete message", color = Color(0xFFEF4444), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun PollBubble(msg: Message, vm: AppViewModel, c: com.cloes.app.ui.theme.CloesColors) {
    val poll = msg.pollData ?: return
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(c.surface)
            .border(1.dp, Purple.copy(0.2f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("📊 ${poll.question}", color = c.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        poll.options.forEachIndexed { idx, opt ->
            val votes = poll.votes.getOrElse(idx) { 0 }
            val total = poll.votes.sum().coerceAtLeast(1)
            val pct = (votes.toFloat() / total * 100).toInt()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Purple.copy(0.08f))
                    .clickable {
                        if (poll.votes.size <= idx) {
                            repeat(idx + 1 - poll.votes.size) { poll.votes.add(0) }
                        }
                        poll.votes[idx]++
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(opt, color = c.text, fontSize = 13.sp)
                    Text("$pct%", color = c.textSub, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun FileBubble(msg: Message, c: com.cloes.app.ui.theme.CloesColors) {
    val fileIcon = when (msg.fileType) {
        "photo" -> Icons.Default.Image
        "doc"   -> Icons.Default.Description
        "video" -> Icons.Default.Videocam
        "audio" -> Icons.Default.Audiotrack
        else    -> Icons.Default.AttachFile
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(c.surface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(fileIcon, null, tint = Purple, modifier = Modifier.size(24.dp))
        Text(msg.text, color = c.text, fontSize = 13.sp)
    }
}

@Composable
private fun StickerPanel(vm: AppViewModel) {
    val c = vm.themeColors
    val stickers = listOf(
        "🌙","✦","💜","🌸","⚡","🦋","🌊","🔥","🌿","💫","❄️","🎯",
        "🌈","💎","🦄","🌺","🍀","🌙","💜","✨","🎭","🎪","🎨","🌟",
        "🦊","🐝","🌻","🦋","🫧","🪩","🌴","🎋"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(c.surface2)
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        val chunked = stickers.chunked(4)
        chunked.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp)
            ) {
                row.forEach { sticker ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Purple.copy(0.06f))
                            .clickable {
                                val contact = vm.currentContact()
                                contact?.messages?.add(
                                    Message(
                                        text = sticker, sent = true,
                                        timestamp = vmTime(), type = MsgType.Sticker
                                    )
                                )
                                vm.showStickerPanel = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(sticker, fontSize = 24.sp)
                    }
                }
                // Fill remaining slots
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    vm: AppViewModel,
    c: com.cloes.app.ui.theme.CloesColors,
    contact: Contact,
    scope: kotlinx.coroutines.CoroutineScope,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface2)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        NeuIconButton(
            icon = { Text("😊", fontSize = 18.sp) },
            onClick = { vm.showStickerPanel = !vm.showStickerPanel }
        )
        NeuIconButton(
            icon = {
                Icon(Icons.Default.AttachFile, null, tint = c.textMid, modifier = Modifier.size(16.dp))
            },
            onClick = { vm.showFileModal = true }
        )

        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(c.bg)) {
            TextField(
                value = vm.chatInput,
                onValueChange = { vm.chatInput = it },
                placeholder = { Text("Message...", color = c.textSub, fontSize = 14.sp) },
                maxLines = 4,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedTextColor = c.text,
                    focusedTextColor = c.text
                )
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    brush = if (vm.chatInput.isNotBlank())
                        Brush.linearGradient(listOf(Pink, Purple))
                    else Brush.horizontalGradient(listOf(c.surface, c.surface))
                )
                .clickable(enabled = vm.chatInput.isNotBlank()) {
                    vm.sendMessage(vm.chatInput)
                    scope.launch {
                        delay(100)
                        if (contact.messages.isNotEmpty()) {
                            listState.animateScrollToItem(contact.messages.size - 1)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Send, "Send",
                tint = if (vm.chatInput.isNotBlank()) Color.White else c.textSub,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun vmTime(): String {
    val cal = java.util.Calendar.getInstance()
    val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
    val m = cal.get(java.util.Calendar.MINUTE)
    return "${h}:${m.toString().padStart(2, '0')}"
}
