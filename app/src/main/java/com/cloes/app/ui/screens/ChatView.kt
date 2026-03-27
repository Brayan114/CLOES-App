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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun vmTime(): String {
    val cal = java.util.Calendar.getInstance()
    val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
    val m = cal.get(java.util.Calendar.MINUTE)
    return "$h:${m.toString().padStart(2,'0')}"
}

@Composable
fun ChatView(vm: AppViewModel) {
    val c = vm.themeColors
    val contact = vm.currentContact() ?: return
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()



    LaunchedEffect(contact.id) {
        if (contact.messages.isNotEmpty()) { delay(100); listState.scrollToItem(contact.messages.size - 1) }
    }

    // Jump to message when tap reply preview
    LaunchedEffect(vm.scrollToBubbleId) {
        val targetId = vm.scrollToBubbleId ?: return@LaunchedEffect
        if (targetId == -1L) return@LaunchedEffect
        val idx = contact.messages.indexOfFirst { it.id == targetId }
        if (idx >= 0) { listState.animateScrollToItem(idx) }
        vm.scrollToBubbleId = -1L
    }

    // Scroll arrow state
    val isAtBottom by remember { derivedStateOf { listState.firstVisibleItemIndex >= contact.messages.size - 3 } }
    var arrowTapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    // Visible date label while scrolling
    val visibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val visibleDateLabel = remember(visibleItemIndex) {
        contact.messages.getOrNull(visibleItemIndex)?.let { msg ->
            try {
                val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                sdf.format(Date(msg.id)) // id doubles as rough timestamp seed
            } catch (e: Exception) { null }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().imePadding().background(c.bg)
    ) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        if (vm.urgencyTintOn) Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEF4444).copy(alpha = 0.07f)))

        Column(modifier = Modifier.fillMaxSize()) {
            ChatTopBar(vm = vm, contact = contact, c = c, listState = listState)

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(c.surface).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                Text("Today", color = c.textSub, fontSize = 11.sp)
                            }
                        }
                    }
                    items(contact.messages, key = { it.id }) { msg ->
                        if (msg.expiresAt == null || System.currentTimeMillis() < msg.expiresAt) {
                            SwipeableMessageBubble(msg = msg, vm = vm, c = c, onSwipeToReply = { vm.startReply(msg) })
                        }
                    }
                }

                // Floating date label while scrolling
                val showDateLabel = listState.isScrollInProgress && !isAtBottom && visibleDateLabel != null
                if (showDateLabel) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            visibleDateLabel ?: "",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Scroll arrow — tap once = go to bottom, double-tap = go to top
                if (!isAtBottom) {
                    val goingUp = arrowTapCount % 2 == 1
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Purple.copy(alpha = 0.88f))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        val now = System.currentTimeMillis()
                                        arrowTapCount = if (now - lastTapTime < 400L) arrowTapCount + 1 else 1
                                        lastTapTime = now
                                        scope.launch {
                                            if (arrowTapCount % 2 == 0) {
                                                // Double tap -> top
                                                listState.animateScrollToItem(0)
                                            } else {
                                                // Single tap -> bottom
                                                if (contact.messages.isNotEmpty())
                                                    listState.animateScrollToItem(contact.messages.size - 1)
                                            }
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (goingUp) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (goingUp) "Go to top" else "Go to bottom",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Typing indicator
            if (vm.typingContactId == contact.id) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Purple.copy(0.1f)).padding(horizontal = 12.dp, vertical = 5.dp)) {
                        Text("Reply coming... ✎", color = Purple, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
            }

            // Reply preview bar
            AnimatedVisibility(visible = vm.replyingToMessage != null) {
                vm.replyingToMessage?.let { replyMsg ->
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Purple.copy(0.12f))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(3.dp).height(32.dp).clip(RoundedCornerShape(2.dp)).background(Purple))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Replying to", color = Purple, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(replyMsg.text.take(60), color = c.textMid, fontSize = 12.sp, maxLines = 1)
                        }
                        IconButton(onClick = { vm.cancelReply() }) {
                            Icon(Icons.Default.Close, null, tint = c.textSub, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            AnimatedVisibility(visible = vm.showStickerPanel) { StickerPanel(vm = vm) }
            ChatInputBar(vm = vm, c = c, contact = contact, scope = scope, listState = listState)
        }
    }
}

@Composable
private fun SwipeableMessageBubble(
    msg: Message,
    vm: AppViewModel,
    c: CloesColors,
    onSwipeToReply: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val threshold = 80f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = offsetX.dp.coerceAtLeast(0.dp))   // only drag right (to reply)
            .pointerInput(msg.id) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > threshold) onSwipeToReply()
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, delta ->
                        if (delta > 0) offsetX = (offsetX + delta / 3f).coerceIn(0f, 100f)
                    }
                )
            }
    ) {
        // Reply arrow hint
        if (offsetX > 20f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Purple.copy(alpha = (offsetX / threshold).coerceIn(0f, 0.9f))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Reply, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        MessageBubble(msg = msg, vm = vm, c = c)
    }
}

@Composable
private fun MessageBubble(msg: Message, vm: AppViewModel, c: CloesColors) {
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
            // Reply-to preview
            msg.replyToText?.let { preview ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(c.surface2)
                        .border(1.dp, Purple.copy(0.2f), RoundedCornerShape(10.dp))
                        .clickable { msg.replyToId?.let { vm.jumpToMessage(it) } }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(2.dp).height(24.dp).clip(RoundedCornerShape(1.dp)).background(Purple))
                        Text(preview, color = c.textMid, fontSize = 11.sp, maxLines = 1)
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
            }

            when (msg.type) {
                MsgType.Poll    -> PollBubble(msg, vm, c)
                MsgType.Sticker -> Text(msg.text, fontSize = 48.sp)
                MsgType.File    -> FileBubble(msg, c)
                else            -> TextBubble(msg, vm, c, showReactionPicker) { showReactionPicker = it }
            }

            msg.reactionEmoji?.let {
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(c.surface).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(it, fontSize = 14.sp)
                }
            }

            // Unsent glyph
            if (msg.isUnsent) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)) {
                    Text("✦", color = Purple, fontSize = 9.sp)
                    Text("written a while ago", color = c.textSub, fontSize = 9.sp, fontStyle = FontStyle.Italic)
                }
            }

            Text(msg.timestamp, color = c.textSub, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TextBubble(msg: Message, vm: AppViewModel, c: CloesColors, showPicker: Boolean, onTogglePicker: (Boolean) -> Unit) {
    val bubbleBg = if (msg.sent) Brush.linearGradient(listOf(vm.bubbleSent1, vm.bubbleSent2))
                   else Brush.horizontalGradient(listOf(vm.bubbleRecvBg, vm.bubbleRecvBg))
    val textColor = if (msg.sent) Color.White else vm.bubbleRecvTxt
    val isDeleted = msg.type == MsgType.Deleted

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = if (msg.sent) 18.dp else 4.dp, bottomEnd = if (msg.sent) 4.dp else 18.dp))
            .background(brush = if (isDeleted) Brush.horizontalGradient(listOf(c.surface2, c.surface2)) else bubbleBg)
            .combinedClickable(onClick = {}, onLongClick = { vm.selectedBubbleId = msg.id; onTogglePicker(!showPicker) })
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        if (isDeleted) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Block, null, tint = c.textSub, modifier = Modifier.size(13.dp))
                Text(msg.text, color = c.textSub, fontSize = 13.sp, fontStyle = FontStyle.Italic)
            }
        } else {
            Text(msg.text, color = textColor, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }

    if (showPicker && !isDeleted) {
        Column(modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(c.surface2).padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 4.dp)) {
                listOf("💜", "❤️", "😂", "😮", "😢", "👍").forEach { emoji ->
                    Text(emoji, fontSize = 20.sp, modifier = Modifier.clickable {
                        val contact = vm.currentContact()
                        val idx = contact?.messages?.indexOfFirst { it.id == msg.id } ?: -1
                        if (idx >= 0) contact!!.messages[idx] = msg.copy(reactionEmoji = emoji)
                        onTogglePicker(false)
                    })
                }
            }
            HorizontalDivider(color = c.border2, thickness = 0.5.dp)
            // Reply
            BubbleMenuRow(Icons.Default.Reply, "Reply", Purple) {
                vm.startReply(msg); onTogglePicker(false)
            }
            if (msg.sent) BubbleMenuRow(Icons.Default.Edit, "Edit message", Purple) {
                vm.selectedBubbleId = msg.id; vm.showEditBubble = true; onTogglePicker(false)
            }
            BubbleMenuRow(Icons.Default.PushPin, if (vm.pinnedBubbleId == msg.id) "Unpin Convo" else "Pin Convo",
                if (vm.pinnedBubbleId == msg.id) Gold else Purple) {
                vm.pinBubble(msg.id); onTogglePicker(false)
            }
            BubbleMenuRow(Icons.Default.Delete, "Delete message", Color(0xFFEF4444)) {
                vm.selectedBubbleId = msg.id; vm.showDeleteBubble = true; onTogglePicker(false)
            }
        }
    }
}

@Composable
private fun BubbleMenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(15.dp))
        Text(label, color = LocalCloesColors.current.text, fontSize = 13.sp)
    }
}

@Composable
private fun FileBubble(msg: Message, c: CloesColors) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(c.surface)
            .border(1.dp, c.border, RoundedCornerShape(14.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AttachFile, null, tint = Purple, modifier = Modifier.size(20.dp))
        Column {
            Text(msg.text, color = c.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(msg.fileType ?: "File", color = c.textSub, fontSize = 10.sp)
        }
    }
}

@Composable
private fun PollBubble(msg: Message, vm: AppViewModel, c: CloesColors) {
    val poll = msg.pollData ?: return
    Column(
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(c.surface)
            .border(1.dp, Purple.copy(0.2f), RoundedCornerShape(14.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("📊 ${poll.question}", color = c.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        poll.options.forEachIndexed { idx, opt ->
            val votes = poll.votes.getOrElse(idx) { 0 }
            val total = poll.votes.sum().coerceAtLeast(1)
            val pct = (votes.toFloat() / total * 100).toInt()
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Purple.copy(0.08f))
                    .clickable {
                        if (poll.votes.size <= idx) repeat(idx + 1 - poll.votes.size) { poll.votes.add(0) }
                        poll.votes[idx] = poll.votes[idx] + 1
                    }.padding(10.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth(pct / 100f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Purple).align(Alignment.BottomStart))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(opt, color = c.text, fontSize = 13.sp)
                    Text("$pct%", color = Purple, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ChatTopBar(vm: AppViewModel, contact: Contact, c: CloesColors, listState: androidx.compose.foundation.lazy.LazyListState) {
    Row(
        modifier = Modifier.fillMaxWidth().background(c.surface2)
            .statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, "Back", tint = c.textMid, modifier = Modifier.size(18.dp)) }, onClick = { vm.closeChat() })

        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))) {
            FragmentAvatar(paletteIndex = contact.paletteIndex, seed = fragSeed(contact.id))
            // Ambient presence pulse
            if (vm.ambientPresenceEnabled && contact.online) {
                Box(
                    modifier = Modifier.size(10.dp).align(Alignment.BottomEnd)
                        .clip(CircleShape).background(Color(0xFF22C55E).copy(0.85f))
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(contact.name, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                if (contact.locked) Icon(Icons.Default.Lock, null, tint = Purple, modifier = Modifier.size(12.dp))
            }
            val bloom = contact.bloomScore
            val bloomCol = vm.bloomColor(bloom)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(bloomCol))
                Text("Bloom $bloom", color = bloomCol, fontSize = 10.sp)
                if (contact.online) Text("· active now", color = c.textSub, fontSize = 10.sp)
            }
        }

        // Pin indicator — tapping jumps to pinned message
        if (vm.pinnedBubbleId != null && vm.pinnedBubbleId != -1L) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.18f))
                    .border(1.dp, Gold.copy(alpha = 0.55f), CircleShape)
                    .clickable {
                        val idx = contact.messages.indexOfFirst { it.id == vm.pinnedBubbleId }
                        if (idx >= 0) {
                            val scope2 = kotlinx.coroutines.MainScope()
                            scope2.launch { listState.animateScrollToItem(idx) }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("📌", fontSize = 13.sp)
            }
        }

        // Unsent drawer shortcut
        NeuIconButton(icon = { Icon(Icons.Default.EditNote, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
            onClick = { vm.showUnsentDrawer = true })
        NeuIconButton(icon = { Icon(Icons.Default.MoreVert, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
            onClick = { vm.showChatMenu = true })
    }
}

@Composable
private fun ChatInputBar(vm: AppViewModel, c: CloesColors, contact: Contact, scope: kotlinx.coroutines.CoroutineScope, listState: androidx.compose.foundation.lazy.LazyListState) {
    Row(
        modifier = Modifier.fillMaxWidth().background(c.surface2)
            .navigationBarsPadding().padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NeuIconButton(icon = { Icon(Icons.Default.EmojiEmotions, null, tint = c.textMid, modifier = Modifier.size(20.dp)) },
            onClick = { vm.showStickerPanel = !vm.showStickerPanel })

        OutlinedTextField(
            value = vm.chatInput,
            onValueChange = { vm.chatInput = it },
            placeholder = { Text("Message…", color = c.textSub, fontSize = 14.sp) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple.copy(0.5f), unfocusedBorderColor = c.border,
                focusedTextColor = c.text, unfocusedTextColor = c.text,
                focusedContainerColor = c.surface, unfocusedContainerColor = c.surface
            ),
            shape = RoundedCornerShape(22.dp),
            maxLines = 4
        )

        // Send
        Box(
            modifier = Modifier.size(42.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Purple, Pink)))
                .clickable {
                    if (vm.chatInput.isNotBlank()) {
                        vm.sendMessage(vm.chatInput)
                        scope.launch { delay(100); if (contact.messages.isNotEmpty()) listState.animateScrollToItem(contact.messages.size - 1) }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun StickerPanel(vm: AppViewModel) {
    val c = vm.themeColors
    val stickers = listOf("😊","😂","🥺","😍","🔥","💜","✦","🌙","☀️","💫","🌸","⚡","🦋","🌊","🎵","✨","🫶","👻","🌿","💎")
    Column(modifier = Modifier.fillMaxWidth().background(c.surface2).padding(12.dp)) {
        Text("STICKERS", color = c.textSub, fontSize = 10.sp, letterSpacing = 1.5.sp, modifier = Modifier.padding(bottom = 8.dp))
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(stickers) { s ->
                Text(s, fontSize = 30.sp, modifier = Modifier.clickable {
                    val contact = vm.currentContact() ?: return@clickable
                    contact.messages.add(Message(text = s, sent = true, timestamp = vmTime(), type = MsgType.Sticker))
                    vm.showStickerPanel = false
                })
            }
        }
    }
}
