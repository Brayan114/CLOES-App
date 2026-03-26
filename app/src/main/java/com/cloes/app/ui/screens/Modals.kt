package com.cloes.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cloes.app.data.*
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel

// ─── Base Modal Wrapper ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloesBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        containerColor = LocalCloesColors.current.surface2,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(top = 8.dp, bottom = 22.dp)
                .navigationBarsPadding()
        ) {
            SheetHandle()
            content()
        }
    }
}

// ─── Chat Menu ────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMenuSheet(vm: AppViewModel) {
    val c = vm.themeColors
    val contact = vm.currentContact()

    CloesBottomSheet(onDismiss = { vm.showChatMenu = false }) {
        Text(
            "${contact?.name ?: ""} — Options",
            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.text,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        data class MenuItem(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String, val action: () -> Unit)
        val menuItems = listOf(
            MenuItem(if (contact?.locked == true) Icons.Default.LockOpen else Icons.Default.Lock,
                if (contact?.locked == true) "Unlock Chat" else "Lock Chat",
                { vm.toggleChatLock(); vm.showChatMenu = false }),
            MenuItem(Icons.Default.PushPin, if (contact?.pinned == true) "Unpin Chat" else "Pin Chat",
                { vm.togglePin(); vm.showChatMenu = false }),
            MenuItem(Icons.Default.Timer, "Disappearing Messages",
                { vm.showDisappearModal = true; vm.showChatMenu = false }),
            MenuItem(Icons.Default.Wallpaper, "Chat Background from Gallery",
                { vm.showChatBgPicker = true; vm.showChatMenu = false }),
            MenuItem(Icons.Default.EmojiEmotions, "Create Sticker from Gallery",
                { vm.showStickerCreator = true; vm.showChatMenu = false }),
            MenuItem(Icons.Default.PieChart, "Create Poll",
                { vm.showPollModal = true; vm.showChatMenu = false }),
            MenuItem(Icons.Default.Edit, "Edit Contact",
                { vm.showEditContact = true; vm.showChatMenu = false }),
            MenuItem(Icons.Default.Call, "Voice Call",
                { contact?.let { c2 -> vm.triggerCall(c2) }; vm.showChatMenu = false }),
            MenuItem(Icons.Default.SaveAlt, "Export Chat History",
                { vm.exportChatHistory(); vm.showChatMenu = false }),
            MenuItem(Icons.Default.CleaningServices, "Clean Chat",
                { vm.showCleanChat = true; vm.showChatMenu = false })
        )
        menuItems.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(Purple.copy(0.05f)).clickable(onClick = item.action)
                .padding(horizontal = 16.dp, vertical = 12.dp).padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                Icon(item.icon, null, tint = Purple, modifier = Modifier.size(18.dp))
                Text(item.label, color = c.text, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        TextButton(
            onClick = { vm.showChatMenu = false },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Close", color = c.textSub) }
    }
}

// ─── Theme Modal ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSheet(vm: AppViewModel) {
    val c = vm.themeColors

    CloesBottomSheet(onDismiss = { vm.showThemeModal = false }) {
        Text("Themes & Fonts", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.text,
            modifier = Modifier.padding(bottom = 14.dp))

        SectionLabel("Theme")
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Default" to AppTheme.Default, "Dark" to AppTheme.Dark,
                "Rose" to AppTheme.Rose, "Forest" to AppTheme.Forest)
                .forEach { (label, theme) ->
                    TabChip(
                        text = label,
                        selected = vm.appTheme == theme,
                        onClick = { vm.setTheme(theme) },
                        modifier = Modifier.weight(1f)
                    )
                }
        }

        SectionLabel("Font")
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Default" to androidx.compose.ui.text.font.FontFamily.Default,
                "Serif"   to androidx.compose.ui.text.font.FontFamily.Serif,
                "Mono"    to androidx.compose.ui.text.font.FontFamily.Monospace,
                "Cursive" to androidx.compose.ui.text.font.FontFamily.Cursive
            ).forEach { (font, family) ->
                TabChip(
                    text = font,
                    selected = vm.appFont == font,
                    onClick = { vm.setFont(font, family) }
                )
            }
        }

        SectionLabel("✦ Sent Bubble Color")
        val bubblePresets = listOf(
            Triple("Violet Dream", Color(0xFF8B5CF6), Color(0xFFFF3385)),
            Triple("Ocean", Color(0xFF06B6D4), Color(0xFF3B82F6)),
            Triple("Sunset", Color(0xFFF59E0B), Color(0xFFEF4444)),
            Triple("Forest", Color(0xFF22C55E), Color(0xFF06B6D4)),
            Triple("Rose", Color(0xFFEC4899), Color(0xFFF43F5E)),
            Triple("Gold", Color(0xFFF59E0B), Color(0xFFD97706)),
            Triple("Midnight", Color(0xFF1a0a2e), Color(0xFF4C1D95)),
            Triple("Mint", Color(0xFF4DFFD4), Color(0xFF22C55E))
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            items(bubblePresets) { (name, c1, c2) ->
                val selected = vm.bubbleSent1 == c1 && vm.bubbleSent2 == c2
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(c1, c2)))
                        .border(
                            if (selected) 3.dp else 0.dp,
                            Color.White, CircleShape
                        )
                        .clickable { vm.setBubbleColor(c1, c2, name) }
                )
            }
        }

        // Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(vm.bubbleSent1, vm.bubbleSent2)))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Hello! How are you? ✓", color = Color.White, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionLabel("✦ Received Bubble Color")
        val recvPresets = listOf(
            Triple("Soft Lilac", Color(0xEDF5F0FF), Color(0xFF2A1A4A)),
            Triple("Pearl", Color(0xF2FFFFFF), Color(0xFF1A1A2E)),
            Triple("Cloud Blue", Color(0xF2DBEAFE), Color(0xFF1E3A5F)),
            Triple("Blush", Color(0xF2FEE2E2), Color(0xFF7F1D1D)),
            Triple("Night Dark", Color(0xE637235A), Color(0xFFF0E8FF)),
            Triple("Slate", Color(0xEB334155), Color(0xFFE2E8F0))
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            items(recvPresets) { (name, bg, txt) ->
                val selected = vm.bubbleRecvBg == bg
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(bg)
                        .border(
                            if (selected) 3.dp else 1.dp,
                            if (selected) Purple else Purple.copy(0.3f), CircleShape
                        )
                        .clickable {
                            vm.bubbleRecvBg = bg
                            vm.bubbleRecvTxt = txt
                            vm.showToast("Bubbles", "Received: $name ✓", "low")
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { vm.showThemeModal = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Done", color = c.textSub)
        }
    }
}

// ─── Disappear Modal ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisappearSheet(vm: AppViewModel) {
    CloesBottomSheet(onDismiss = { vm.showDisappearModal = false }) {
        Text("💨 Disappearing Messages", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = vm.themeColors.text, modifier = Modifier.padding(bottom = 14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0 to "Off", 30 to "30 seconds", 300 to "5 minutes",
                3600 to "1 hour", 86400 to "24 hours", 604800 to "1 week")
                .forEach { (secs, label) ->
                    OutlineButton(text = label, onClick = { vm.setDisappear(secs) },
                        modifier = Modifier.fillMaxWidth())
                }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { vm.showDisappearModal = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = vm.themeColors.textSub)
        }
    }
}

// ─── Poll Modal ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollSheet(vm: AppViewModel) {
    val c = vm.themeColors
    CloesBottomSheet(onDismiss = { vm.showPollModal = false }) {
        Text("📊 Create Poll", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = c.text, modifier = Modifier.padding(bottom = 14.dp))
        SectionLabel("Question")
        CloesInput(vm.pollQuestion, { vm.pollQuestion = it }, "Ask something...",
            modifier = Modifier.padding(bottom = 12.dp))
        SectionLabel("Options")
        CloesInput(vm.pollOptA, { vm.pollOptA = it }, "Option A",
            modifier = Modifier.padding(bottom = 8.dp))
        CloesInput(vm.pollOptB, { vm.pollOptB = it }, "Option B",
            modifier = Modifier.padding(bottom = 8.dp))
        CloesInput(vm.pollOptC, { vm.pollOptC = it }, "Option C (optional)",
            modifier = Modifier.padding(bottom = 16.dp))
        GradientButton("Send Poll", { vm.sendPoll() }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { vm.showPollModal = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = c.textSub)
        }
    }
}

// ─── File Share Modal ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileShareSheet(vm: AppViewModel) {
    val c = vm.themeColors
    CloesBottomSheet(onDismiss = { vm.showFileModal = false }) {
        Text("📎 Share File", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = c.text, modifier = Modifier.padding(bottom = 14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            listOf("photo" to "📷 Photo", "doc" to "📄 Document",
                "video" to "🎥 Video", "audio" to "🎵 Audio", "link" to "🔗 Link")
                .forEach { (type, label) ->
                    OutlineButton(text = label, onClick = {
                        val contact = vm.currentContact()
                        contact?.messages?.add(Message(
                            text = label, sent = true, timestamp = "now",
                            type = MsgType.File, fileType = type
                        ))
                        vm.showFileModal = false
                        vm.showToast("Share", "$label sent ✦", "low")
                    }, modifier = Modifier.fillMaxWidth())
                }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { vm.showFileModal = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = c.textSub)
        }
    }
}

// ─── Add Contact Modal ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactSheet(vm: AppViewModel) {
    val c = vm.themeColors
    var activeTab by remember { mutableStateOf("new") }

    CloesBottomSheet(onDismiss = { vm.showAddContact = false }) {
        Text("Add Connection ✦", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = c.text, modifier = Modifier.padding(bottom = 12.dp))

        // Tabs: New / From Contacts
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("new" to "New Contact", "existing" to "From Contacts").forEach { (id, label) ->
                TabChip(text = label, selected = activeTab == id,
                    onClick = { activeTab = id }, modifier = Modifier.weight(1f))
            }
        }

        if (activeTab == "existing") {
            // Pick from contacts not already added
            Text("Tap to add to your connections", color = c.textSub, fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 10.dp))
            val suggestions = listOf(
                Triple("Alex Rivera", "@alex.r", 2),
                Triple("Jamie Lee", "@jamie.lee", 5),
                Triple("Morgan Kim", "@morgan.k", 1),
                Triple("Casey Park", "@casey.p", 3)
            )
            suggestions.forEach { (name, handle, palIdx) ->
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(14.dp)).background(c.surface)
                    .clickable {
                        vm.newContactName = name
                        vm.newContactHandle = handle
                        vm.newContactFrag = palIdx
                        vm.addContact()
                    }
                    .padding(horizontal = 13.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))) {
                        FragmentAvatar(paletteIndex = palIdx, seed = palIdx * 0.17f)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(handle, color = c.textSub, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.Add, null, tint = Purple, modifier = Modifier.size(18.dp))
                }
            }
        } else {
            SectionLabel("Their Fragment")
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                PALETTES.forEachIndexed { idx, palette ->
                    Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
                        .border(if (vm.newContactFrag == idx) 3.dp else 0.dp, Purple, RoundedCornerShape(14.dp))
                        .clickable { vm.newContactFrag = idx }) {
                        FragmentArt(palette = palette, seed = idx * 0.13f + 0.05f)
                    }
                }
            }
            SectionLabel("User Handle")
            CloesInput(vm.newContactHandle, { vm.newContactHandle = it }, "their.handle",
                modifier = Modifier.padding(bottom = 12.dp))
            SectionLabel("Nickname (optional)")
            CloesInput(vm.newContactName, { vm.newContactName = it }, "What you call them",
                modifier = Modifier.padding(bottom = 16.dp))
            SectionLabel("Assign to Circle")
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                vm.groups.forEach { group ->
                    TabChip(text = "${group.emoji} ${group.name}",
                        selected = vm.newContactGroup == group.name,
                        onClick = { vm.newContactGroup = group.name })
                }
            }
            GradientButton("Add to My World ✦", { vm.addContact() }, modifier = Modifier.fillMaxWidth())
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { vm.showAddContact = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = c.textSub)
        }
    }
}

// ─── Add Group Modal ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupSheet(vm: AppViewModel) {
    val c = vm.themeColors
    val emojis = listOf("💜","🌸","⚡","🌊","🦋","✨","🔥","🌙","🎯","💎","🌿","🍀","🌺","🦄","🌈","⭐")
    CloesBottomSheet(onDismiss = { vm.showAddGroup = false }) {
        Text("New Priority Group", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = c.text, modifier = Modifier.padding(bottom = 5.dp))
        Text("Create a circle with its own Muse tone.",
            fontSize = 12.5.sp, color = c.textMid, modifier = Modifier.padding(bottom = 16.dp))

        SectionLabel("Icon")
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            emojis.forEach { emoji ->
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (vm.newGroupEmoji == emoji) Purple.copy(0.1f) else c.bg)
                        .border(
                            if (vm.newGroupEmoji == emoji) 2.dp else 0.dp,
                            Purple, RoundedCornerShape(12.dp)
                        )
                        .clickable { vm.newGroupEmoji = emoji },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 20.sp)
                }
            }
        }

        SectionLabel("Group Name")
        CloesInput(vm.newGroupName, { vm.newGroupName = it }, "FAMILY, BESTIES...",
            modifier = Modifier.padding(bottom = 12.dp))

        SectionLabel("Muse Tone")
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            listOf("warm","friendly","professional","playful","intimate","minimal").forEach { tone ->
                TabChip(
                    text = tone.replaceFirstChar { it.uppercase() },
                    selected = vm.newGroupTone == tone,
                    onClick = { vm.newGroupTone = tone }
                )
            }
        }

        GradientButton("Create Group ✦", { vm.saveGroup() }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { vm.showAddGroup = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = c.textSub)
        }
    }
}

// ─── Share Modal ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(vm: AppViewModel) {
    val c = vm.themeColors
    CloesBottomSheet(onDismiss = { vm.showShareModal = false }) {
        Text("Share Your Fragment ◈", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = c.text, modifier = Modifier.padding(bottom = 5.dp))
        Text("Let someone scan your Light. No number, no trace.",
            fontSize = 12.5.sp, color = c.textMid, modifier = Modifier.padding(bottom = 16.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .shadow(10.dp, RoundedCornerShape(24.dp))
                .align(Alignment.CenterHorizontally)
        ) {
            FragmentArt(
                palette = vm.profile.palette.ifEmpty { listOf(Pink, Purple, Cyan) },
                seed = vm.profile.lightSeed,
                animating = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("@${vm.profile.handle}", fontSize = 13.sp, color = c.textMid,
            modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Purple.copy(0.06f))
                .border(1.dp, Purple.copy(0.14f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("▣ ▣ ▣", fontSize = 24.sp, letterSpacing = 6.sp, color = Purple)
                Text("QR CODE · SCAN TO JOIN MY WORLD",
                    fontSize = 10.sp, color = c.textSub, modifier = Modifier.padding(top = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        GradientButton(
            text = "Copy Fragment Link ✦",
            onClick = {
                vm.showToast("Fragment", "Link copied to clipboard ✦", "low")
                vm.showShareModal = false
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { vm.showShareModal = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Done", color = c.textSub)
        }
    }
}

// ─── Add Group Chat Modal ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupChatSheet(vm: AppViewModel) {
    val c = vm.themeColors
    var gcName by remember { mutableStateOf("") }
    val selectedMembers = remember { mutableStateListOf<Long>() }
    CloesBottomSheet(onDismiss = { vm.showAddGroupChat = false }) {
        Text("New Group Chat 👥", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = c.text, modifier = Modifier.padding(bottom = 5.dp))
        Text("For team projects, friend circles, and more.",
            fontSize = 12.5.sp, color = c.textMid, modifier = Modifier.padding(bottom = 16.dp))
        SectionLabel("Group Name")
        CloesInput(gcName, { gcName = it }, "Project Alpha, Weekend Plans...",
            modifier = Modifier.padding(bottom = 12.dp))
        SectionLabel("Add Members")
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            vm.contacts.forEach { contact ->
                TabChip(
                    text = contact.name,
                    selected = selectedMembers.contains(contact.id),
                    onClick = {
                        if (selectedMembers.contains(contact.id)) selectedMembers.remove(contact.id)
                        else selectedMembers.add(contact.id)
                    }
                )
            }
        }
        GradientButton("Create Group Chat", {
            if (gcName.isBlank()) { vm.showToast("Group", "Enter a group name", "mid"); return@GradientButton }
            vm.showToast("Group Chat", "$gcName created with ${selectedMembers.size} members 👥", "low")
            vm.showAddGroupChat = false
        }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { vm.showAddGroupChat = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = c.textSub)
        }
    }
}

// ─── Upload Video Modal ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadVideoSheet(vm: AppViewModel) {
    val c = vm.themeColors
    var title by remember { mutableStateOf("") }
    CloesBottomSheet(onDismiss = { vm.showUploadVideo = false }) {
        Text("Upload Vibe Video ✦", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = c.text, modifier = Modifier.padding(bottom = 14.dp))
        SectionLabel("Video Title")
        CloesInput(title, { title = it }, "Give it a title...",
            modifier = Modifier.padding(bottom = 16.dp))
        OutlineButton(
            text = "📹 Pick from Gallery",
            onClick = { vm.showToast("Vibe", "Gallery picker opened", "low") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlineButton(
            text = "🎥 Record New Video",
            onClick = { vm.showToast("Vibe", "Camera opened", "low") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        GradientButton("Upload to Vibe ✦", {
            vm.addVibeVideo(title.ifBlank { "My Vibe ✦" }, vm.profile.name)
            vm.showUploadVideo = false
        }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { vm.showUploadVideo = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = c.textSub)
        }
    }
}

fun AppViewModel.currentTime2(): String {
    val cal = java.util.Calendar.getInstance()
    val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
    val m = cal.get(java.util.Calendar.MINUTE)
    return "${h}:${m.toString().padStart(2, '0')}"
}




// ─── Create Group Chat Sheet ──────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupChatSheet(vm: AppViewModel) {
    val c = vm.themeColors
    var groupName by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<Long>() }
    var searchQuery by remember { mutableStateOf("") }
    val filtered = vm.contacts.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
    }
    CloesBottomSheet(onDismiss = { vm.showCreateGroupChat = false }) {
        Text("Create Group Chat", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = c.text, modifier = Modifier.padding(bottom = 4.dp))
        Text("Up to 1,024 participants", color = c.textSub, fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 14.dp))
        SectionLabel("Group Name")
        CloesInput(groupName, { groupName = it }, "Name your group",
            modifier = Modifier.padding(bottom = 12.dp))
        SectionLabel("Add Members (${selectedIds.size})")
        CloesInput(searchQuery, { searchQuery = it }, "Search contacts...",
            modifier = Modifier.padding(bottom = 10.dp))
        Column(modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
            filtered.forEach { contact ->
                val selected = selectedIds.contains(contact.id)
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) Purple.copy(0.1f) else c.surface)
                    .clickable { if (selected) selectedIds.remove(contact.id) else selectedIds.add(contact.id) }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))) {
                        FragmentAvatar(paletteIndex = contact.paletteIndex, seed = fragSeed(contact.id))
                    }
                    Text(contact.name, color = c.text, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    if (selected) Icon(Icons.Default.CheckCircle, null, tint = Purple, modifier = Modifier.size(18.dp))
                    else Box(modifier = Modifier.size(18.dp).clip(CircleShape).border(1.5.dp, c.border2, CircleShape))
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        GradientButton("Create Group (${selectedIds.size} members)", {
            if (groupName.isBlank()) vm.showToast("Group", "Enter a group name", "mid")
            else vm.createGroupChat(groupName, selectedIds.toList())
        }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { vm.showCreateGroupChat = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = c.textSub)
        }
    }
}

// ─── Circle Broadcast Message Sheet ──────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleMessageSheet(vm: AppViewModel) {
    val c = vm.themeColors
    val group = vm.circleMessageGroup ?: return
    var message by remember { mutableStateOf("") }
    val members = vm.contacts.filter { group.members.contains(it.id) }
    CloesBottomSheet(onDismiss = { vm.showCircleMessage = false }) {
        Text("Message ${group.emoji} ${group.name}", fontSize = 18.sp,
            fontWeight = FontWeight.Bold, color = c.text, modifier = Modifier.padding(bottom = 4.dp))
        Text("Sends individually to ${members.size} members", color = c.textSub, fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 14.dp))
        SectionLabel("Your message")
        TextField(value = message, onValueChange = { message = it },
            placeholder = { Text("Type your message...", color = c.textSub) },
            maxLines = 4,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(c.bg).padding(bottom = 14.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = c.bg, focusedContainerColor = c.bg,
                unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent,
                unfocusedTextColor = c.text, focusedTextColor = c.text))
        GradientButton("Send to all ${members.size} members", {
            if (message.isBlank()) { vm.showToast("Message", "Write something first", "mid"); return@GradientButton }
            vm.sendCircleMessage(group, message)
            vm.showCircleMessage = false
        }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { vm.showCircleMessage = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = c.textSub)
        }
    }
}

// ─── Font Picker Sheet ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontPickerSheet(vm: AppViewModel) {
    val c = vm.themeColors
    val fonts = listOf(
        "Default"   to androidx.compose.ui.text.font.FontFamily.Default,
        "Serif"     to androidx.compose.ui.text.font.FontFamily.Serif,
        "Monospace" to androidx.compose.ui.text.font.FontFamily.Monospace,
        "Cursive"   to androidx.compose.ui.text.font.FontFamily.Cursive,
        "SansSerif" to androidx.compose.ui.text.font.FontFamily.SansSerif
    )
    CloesBottomSheet(onDismiss = { vm.showFontPicker = false }) {
        Text("Choose Font Style", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = c.text, modifier = Modifier.padding(bottom = 4.dp))
        Text("Applied across the entire app", color = c.textSub, fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            fonts.forEach { (name, family) ->
                val selected = vm.appFont == name
                Row(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) Purple.copy(0.12f) else c.surface)
                    .border(if (selected) 1.5.dp else 0.5.dp,
                        if (selected) Purple else c.border2, RoundedCornerShape(14.dp))
                    .clickable { vm.setFont(name, family); vm.showFontPicker = false }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("The quick brown fox", fontFamily = family,
                        color = if (selected) Purple else c.text, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(name, color = if (selected) Purple else c.textSub, fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                        if (selected) Icon(Icons.Default.CheckCircle, null, tint = Purple, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = { vm.showFontPicker = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = c.textSub)
        }
    }
}

// ─── Edit Contact Sheet ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactSheet(vm: AppViewModel) {
    val c = vm.themeColors
    val contact = vm.currentContact() ?: return
    var editName    by remember { mutableStateOf(contact.name) }
    var editHandle  by remember { mutableStateOf(contact.handle.removePrefix("@")) }
    var editGroup   by remember { mutableStateOf(contact.group) }
    var editUrgency by remember { mutableStateOf(contact.urgency) }
    var editCircle  by remember { mutableStateOf(vm.groups.firstOrNull { it.members.contains(contact.id) }?.name ?: "") }

    CloesBottomSheet(onDismiss = { vm.showEditContact = false }) {
        Text("Edit Contact", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = c.text, modifier = Modifier.padding(bottom = 14.dp))

        // Avatar preview
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(15.dp))) {
                FragmentAvatar(paletteIndex = contact.paletteIndex, seed = fragSeed(contact.id))
            }
            Column {
                Text(contact.name, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(contact.handle, color = c.textSub, fontSize = 12.sp)
            }
        }

        SectionLabel("Nickname")
        CloesInput(editName, { editName = it }, "Contact name",
            modifier = Modifier.padding(bottom = 12.dp))

        SectionLabel("Handle")
        CloesInput(editHandle, { editHandle = it }, "their.handle",
            modifier = Modifier.padding(bottom = 12.dp))

        SectionLabel("Group Label")
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("BESTIE","WORK","FAMILY","FRIENDS","OTHER").forEach { g ->
                TabChip(text = g, selected = editGroup == g, onClick = { editGroup = g })
            }
        }

        SectionLabel("Urgency Level")
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "low" to Color(0xFF22C55E),
                "mid" to Color(0xFFF59E0B),
                "high" to Color(0xFFEF4444)
            ).forEach { (level, color) ->
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                    .background(if (editUrgency == level) color.copy(0.15f) else c.surface)
                    .border(if (editUrgency == level) 1.5.dp else 0.5.dp,
                        if (editUrgency == level) color else c.border2, RoundedCornerShape(12.dp))
                    .clickable { editUrgency = level }
                    .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center) {
                    Text(level.replaceFirstChar { it.uppercase() },
                        color = if (editUrgency == level) color else c.textSub,
                        fontSize = 13.sp, fontWeight = if (editUrgency == level) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }

        SectionLabel("Assign to Circle")
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            TabChip(text = "None", selected = editCircle.isEmpty(), onClick = { editCircle = "" })
            vm.groups.forEach { group ->
                TabChip(text = "${group.emoji} ${group.name}", selected = editCircle == group.name,
                    onClick = { editCircle = group.name })
            }
        }

        GradientButton("Save Changes", {
            if (editName.isNotBlank()) {
                val idx = vm.contacts.indexOfFirst { it.id == contact.id }
                if (idx >= 0) {
                    vm.contacts[idx] = contact.copy(
                        name = editName,
                        handle = "@${editHandle.ifBlank { editName.lowercase().replace(" ", ".") }}",
                        group = editGroup,
                        urgency = editUrgency
                    )
                    // Update circle membership
                    vm.groups.forEach { group ->
                        if (group.name == editCircle) group.members.add(contact.id)
                        else group.members.remove(contact.id)
                    }
                }
                vm.showEditContact = false
                vm.showToast(editName, "Contact updated", "low")
            }
        }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { vm.showEditContact = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = c.textSub)
        }
    }
}

// ─── Delete Contact Confirmation ──────────────────────────────────────────────
@Composable
fun DeleteContactDialog(vm: AppViewModel) {
    val c = vm.themeColors
    val contact = vm.currentContact()
    ConfirmDialog(
        title = "Delete Contact",
        body = "Remove ${contact?.name ?: "this contact"}? This will delete all messages too.",
        confirmLabel = "Delete",
        confirmColor = Color(0xFFEF4444),
        onConfirm = {
            val id = vm.currentChatId ?: return@ConfirmDialog
            vm.contacts.removeIf { it.id == id }
            vm.groups.forEach { it.members.remove(id) }
            vm.closeChat()
            vm.showDeleteContact = false
            vm.showToast("Contacts", "Contact deleted", "low")
        },
        onDismiss = { vm.showDeleteContact = false }
    )
}

// ─── Leave Group Confirmation ─────────────────────────────────────────────────
@Composable
fun LeaveGroupDialog(vm: AppViewModel) {
    val gc = vm.currentGroupChat()
    ConfirmDialog(
        title = "Leave Group",
        body = "Leave \"${gc?.name ?: "this group"}\"? You won't receive messages anymore.",
        confirmLabel = "Leave",
        confirmColor = Color(0xFFEF4444),
        onConfirm = { vm.leaveGroupChat() },
        onDismiss = { vm.showLeaveGroup = false }
    )
}

// ─── Delete Bubble Dialog ─────────────────────────────────────────────────────
@Composable
fun DeleteBubbleDialog(vm: AppViewModel) {
    val c = vm.themeColors
    val msgId = vm.selectedBubbleId ?: return
    AlertDialog(
        onDismissRequest = { vm.showDeleteBubble = false; vm.selectedBubbleId = null },
        containerColor = c.surface,
        title = { Text("Delete message", color = c.text, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = { Text("Who should this be deleted for?", color = c.textSub, fontSize = 13.sp) },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.deleteBubbleForEveryone(msgId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Delete for Everyone", color = Color.White)
                }
                OutlinedButton(onClick = { vm.deleteBubbleForMe(msgId) },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, c.border2)) {
                    Text("Delete for Myself", color = c.text)
                }
                TextButton(onClick = { vm.showDeleteBubble = false; vm.selectedBubbleId = null },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel", color = c.textSub)
                }
            }
        },
        dismissButton = {}
    )
}

// ─── Edit Bubble Dialog ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBubbleDialog(vm: AppViewModel) {
    val c = vm.themeColors
    val msgId = vm.selectedBubbleId ?: return
    val contact = vm.currentContact()
    val msg = contact?.messages?.find { it.id == msgId }
    var editText by remember { mutableStateOf(msg?.text?.removeSuffix(" (edited)") ?: "") }

    AlertDialog(
        onDismissRequest = { vm.showEditBubble = false; vm.selectedBubbleId = null },
        containerColor = c.surface,
        title = { Text("Edit message", color = c.text, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            TextField(value = editText, onValueChange = { editText = it },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = c.bg, focusedContainerColor = c.bg,
                    unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent,
                    unfocusedTextColor = c.text, focusedTextColor = c.text))
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { vm.showEditBubble = false; vm.selectedBubbleId = null },
                    modifier = Modifier.weight(1f)) { Text("Cancel", color = c.textSub) }
                Button(onClick = { if (editText.isNotBlank()) vm.editBubble(msgId, editText) },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                    modifier = Modifier.weight(1f)) { Text("Save") }
            }
        },
        dismissButton = {}
    )
}

// ─── Clean Chat Overlay (select messages or clear all) ───────────────────────
@Composable
fun CleanChatDialog(vm: AppViewModel) {
    val c = vm.themeColors
    val contact = vm.currentContact()
    val messages = contact?.messages ?: return
    val selected = remember { mutableStateListOf<Long>() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f))) {
        Column(modifier = Modifier.fillMaxSize().background(c.bg)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth().background(c.surface2)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.Close, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showCleanChat = false })
                Text("Clean Chat", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                if (selected.isNotEmpty()) {
                    Text("${selected.size} selected", color = Purple, fontSize = 13.sp)
                }
            }
            // Hint
            Text(
                if (selected.isEmpty()) "Tap messages to select, or Clear All below"
                else "${selected.size} message(s) selected — tap Delete to remove",
                color = c.textSub, fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            // Message list
            LazyColumn(modifier = Modifier.weight(1f)
                .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 8.dp)) {
                items(messages) { msg ->
                    val isSelected = selected.contains(msg.id)
                    Row(modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFFEF4444).copy(0.1f) else c.surface)
                        .border(if (isSelected) 1.5.dp else 0.dp,
                            if (isSelected) Color(0xFFEF4444) else Color.Transparent,
                            RoundedCornerShape(12.dp))
                        .clickable {
                            if (isSelected) selected.remove(msg.id)
                            else selected.add(msg.id)
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp))
                        } else {
                            Box(modifier = Modifier.size(18.dp).clip(CircleShape)
                                .border(1.5.dp, c.border2, CircleShape))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (msg.sent) "You" else contact.name,
                                color = if (msg.sent) Purple else c.textMid,
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(msg.text.take(60), color = c.text, fontSize = 13.sp, maxLines = 1)
                        }
                        Text(msg.timestamp, color = c.textSub, fontSize = 10.sp)
                    }
                }
            }
            // Bottom action bar
            Column(modifier = Modifier.fillMaxWidth().background(c.surface2)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selected.isNotEmpty()) {
                    Button(onClick = {
                        selected.forEach { id -> messages.removeIf { it.id == id } }
                        vm.showCleanChat = false
                        vm.showToast(contact.name, "${selected.size} messages deleted", "low")
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Selected (${selected.size})")
                    }
                }
                Button(onClick = {
                    messages.clear()
                    vm.showCleanChat = false
                    vm.showToast(contact.name, "Chat cleared", "low")
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = if (selected.isEmpty()) 1f else 0.6f)),
                    modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear All")
                }
                OutlinedButton(onClick = { vm.showCleanChat = false },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, c.border2)) {
                    Text("Cancel", color = c.text)
                }
            }
        }
    }
}

// ─── Shared ConfirmDialog ─────────────────────────────────────────────────────
@Composable
fun ConfirmDialog(
    title: String, body: String, confirmLabel: String,
    confirmColor: Color = Purple,
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    val c = cloesColors()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        title = { Text(title, color = c.text, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = { Text(body, color = c.textSub, fontSize = 13.sp) },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, c.border2)) { Text("Cancel", color = c.text) }
                Button(onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                    modifier = Modifier.weight(1f)) { Text(confirmLabel, color = Color.White) }
            }
        },
        dismissButton = {}
    )
}

// ─── Group Chat Menu Sheet ────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatMenuSheet(vm: AppViewModel) {
    val c = vm.themeColors
    val gc = vm.currentGroupChat() ?: return

    CloesBottomSheet(onDismiss = { vm.showGroupChatMenu = false }) {
        Text("${gc.name} — Options", fontSize = 18.sp, fontWeight = FontWeight.Bold,
            color = c.text, modifier = Modifier.padding(bottom = 14.dp))

        data class MenuItem(val icon: androidx.compose.ui.graphics.vector.ImageVector,
                            val label: String, val tint: Color = Purple, val action: () -> Unit)
        val menuItems = listOf(
            MenuItem(Icons.Default.PushPin, "Pin Group") {
                vm.showToast(gc.name, "Group pinned", "low"); vm.showGroupChatMenu = false },
            MenuItem(Icons.Default.Timer, "Disappearing Messages") {
                vm.showDisappearModal = true; vm.showGroupChatMenu = false },
            MenuItem(Icons.Default.Wallpaper, "Chat Background from Gallery") {
                vm.showChatBgPicker = true; vm.showGroupChatMenu = false },
            MenuItem(Icons.Default.EmojiEmotions, "Create Sticker from Gallery") {
                vm.showStickerCreator = true; vm.showGroupChatMenu = false },
            MenuItem(Icons.Default.PieChart, "Create Poll") {
                vm.showPollModal = true; vm.showGroupChatMenu = false },
            MenuItem(Icons.Default.Edit, "Edit Group") {
                vm.showEditGroup = true; vm.showGroupChatMenu = false },
            MenuItem(Icons.Default.SaveAlt, "Export Chat History") {
                vm.exportChatHistory(); vm.showGroupChatMenu = false },
            MenuItem(Icons.Default.CleaningServices, "Clean Chat") {
                vm.showCleanChat = true; vm.showGroupChatMenu = false },
            MenuItem(Icons.Default.ExitToApp, "Exit Group", Color(0xFFEF4444)) {
                vm.showLeaveGroup = true; vm.showGroupChatMenu = false }
        )

        menuItems.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(item.tint.copy(0.05f)).clickable(onClick = item.action)
                .padding(horizontal = 16.dp, vertical = 12.dp).padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                Icon(item.icon, null, tint = item.tint, modifier = Modifier.size(18.dp))
                Text(item.label, color = if (item.tint == Color(0xFFEF4444)) item.tint else c.text,
                    fontSize = 14.sp, fontWeight = if (item.tint == Color(0xFFEF4444)) FontWeight.SemiBold else FontWeight.Normal)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        TextButton(onClick = { vm.showGroupChatMenu = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Close", color = c.textSub)
        }
    }
}

// ─── Edit Group Sheet ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupSheet(vm: AppViewModel) {
    val c = vm.themeColors
    val gc = vm.currentGroupChat() ?: return
    var editName    by remember { mutableStateOf(gc.name) }
    var editDesc    by remember { mutableStateOf(gc.description) }
    var editCircle  by remember { mutableStateOf(gc.circleId ?: "") }

    CloesBottomSheet(onDismiss = { vm.showEditGroup = false }) {
        Text("Edit Group", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = c.text, modifier = Modifier.padding(bottom = 14.dp))

        // Group avatar preview
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(15.dp))
                .background(Brush.linearGradient(listOf(Purple.copy(0.3f), Pink.copy(0.3f)))),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Group, null, tint = Purple, modifier = Modifier.size(26.dp))
            }
            Column {
                Text(gc.name, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("${gc.memberIds.size} members", color = c.textSub, fontSize = 12.sp)
            }
        }

        SectionLabel("Group Name")
        CloesInput(editName, { editName = it }, "Group name",
            modifier = Modifier.padding(bottom = 12.dp))

        SectionLabel("Description")
        CloesInput(editDesc, { editDesc = it }, "What is this group about?",
            modifier = Modifier.padding(bottom = 12.dp))

        SectionLabel("Add to Circle")
        Row(modifier = Modifier.fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TabChip(text = "None", selected = editCircle.isEmpty(), onClick = { editCircle = "" })
            vm.groups.forEach { group ->
                TabChip(
                    text = "${group.emoji} ${group.name}",
                    selected = editCircle == group.name,
                    onClick = { editCircle = group.name }
                )
            }
        }

        GradientButton("Save Changes", {
            if (editName.isNotBlank()) {
                vm.editGroupChat(gc.id, editName, editCircle.ifBlank { null })
                gc.description = editDesc
            }
        }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { vm.showEditGroup = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = c.textSub)
        }
    }
}

// ─── CLOESED KEY Setup Dialog (shown on first chat lock) ─────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloesedKeySetupDialog(vm: AppViewModel) {
    val c = vm.themeColors
    var key1 by remember { mutableStateOf("") }
    var key2 by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var showKey1 by remember { mutableStateOf(false) }
    var showKey2 by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { vm.showSetCloesedKey = false },
        containerColor = c.surface,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Text("\uD83D\uDD10", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Set Your CLOESED KEY", color = c.text, fontWeight = FontWeight.Bold,
                    fontSize = 18.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("4 characters exactly", color = c.textSub, fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp))
            }
        },
        text = {
            Column {
                Text(
                    "Hidden chats are revealed by typing this key in search. Keep it private.",
                    color = c.textSub, fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                // Key 1 with show/hide
                Row(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.bg)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = key1,
                        onValueChange = { if (it.length <= 4) { key1 = it; error = "" } },
                        placeholder = { Text("Choose key (4 chars)", color = c.textSub, fontSize = 13.sp) },
                        singleLine = true,
                        visualTransformation = if (showKey1) androidx.compose.ui.text.input.VisualTransformation.None
                                               else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent,
                            unfocusedTextColor = c.text, focusedTextColor = c.text
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showKey1 = !showKey1 }) {
                        Icon(if (showKey1) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null, tint = c.textSub, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Key 2 with show/hide
                Row(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.bg)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = key2,
                        onValueChange = { if (it.length <= 4) { key2 = it; error = "" } },
                        placeholder = { Text("Confirm key", color = c.textSub, fontSize = 13.sp) },
                        singleLine = true,
                        visualTransformation = if (showKey2) androidx.compose.ui.text.input.VisualTransformation.None
                                               else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent,
                            unfocusedTextColor = c.text, focusedTextColor = c.text
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showKey2 = !showKey2 }) {
                        Icon(if (showKey2) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null, tint = c.textSub, modifier = Modifier.size(18.dp))
                    }
                }
                if (error.isNotEmpty()) {
                    Text(error, color = Color(0xFFEF4444), fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp))
                }
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GradientButton("Set CLOESED KEY", {
                    when {
                        key1.isBlank() -> error = "Key cannot be empty"
                        key1.length != 4 -> error = "Key must be exactly 4 characters"
                        key1 != key2 -> error = "Keys don\'t match"
                        else -> {
                            vm.cloesedKey = key1
                            vm.showSetCloesedKey = false
                            vm.showToast("CLOESED KEY", "Your secret key has been set", "low")
                        }
                    }
                }, modifier = Modifier.fillMaxWidth())
                OutlinedButton(onClick = { vm.showSetCloesedKey = false },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, c.border2)) {
                    Text("Cancel", color = c.textMid)
                }
            }
        },
        dismissButton = {}
    )
}

// ─── CLOESED KEY Change Dialog (from Settings) ───────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeCloesedKeyDialog(vm: AppViewModel, onDismiss: () -> Unit) {
    val c = vm.themeColors
    var currentKey by remember { mutableStateOf("") }
    var newKey1    by remember { mutableStateOf("") }
    var newKey2    by remember { mutableStateOf("") }
    var step       by remember { mutableStateOf(1) }  // 1=verify, 2=new key
    var error      by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        title = { Text(if (step == 1) "Verify Current Key" else "Set New CLOESED KEY",
            color = c.text, fontWeight = FontWeight.Bold) },
        text = {
            var showCurrent by remember { mutableStateOf(false) }
            var showNew1 by remember { mutableStateOf(false) }
            var showNew2 by remember { mutableStateOf(false) }
            Column {
                if (step == 1) {
                    Text("Enter your current 4-character CLOESED KEY.", color = c.textSub,
                        fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)).background(c.bg)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = currentKey,
                            onValueChange = { if (it.length <= 4) { currentKey = it; error = "" } },
                            placeholder = { Text("Current key", color = c.textSub, fontSize = 13.sp) },
                            singleLine = true,
                            visualTransformation = if (showCurrent) androidx.compose.ui.text.input.VisualTransformation.None
                                                   else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent,
                                unfocusedTextColor = c.text, focusedTextColor = c.text),
                            modifier = Modifier.weight(1f))
                        IconButton(onClick = { showCurrent = !showCurrent }) {
                            Icon(if (showCurrent) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = c.textSub, modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)).background(c.bg)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = newKey1,
                            onValueChange = { if (it.length <= 4) { newKey1 = it; error = "" } },
                            placeholder = { Text("New key (4 chars)", color = c.textSub, fontSize = 13.sp) },
                            singleLine = true,
                            visualTransformation = if (showNew1) androidx.compose.ui.text.input.VisualTransformation.None
                                                   else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent,
                                unfocusedTextColor = c.text, focusedTextColor = c.text),
                            modifier = Modifier.weight(1f))
                        IconButton(onClick = { showNew1 = !showNew1 }) {
                            Icon(if (showNew1) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = c.textSub, modifier = Modifier.size(18.dp))
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)).background(c.bg)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = newKey2,
                            onValueChange = { if (it.length <= 4) { newKey2 = it; error = "" } },
                            placeholder = { Text("Confirm new key", color = c.textSub, fontSize = 13.sp) },
                            singleLine = true,
                            visualTransformation = if (showNew2) androidx.compose.ui.text.input.VisualTransformation.None
                                                   else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent,
                                unfocusedTextColor = c.text, focusedTextColor = c.text),
                            modifier = Modifier.weight(1f))
                        IconButton(onClick = { showNew2 = !showNew2 }) {
                            Icon(if (showNew2) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = c.textSub, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                if (error.isNotEmpty()) {
                    Text(error, color = Color(0xFFEF4444), fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp))
                }
            }
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, c.border2)) { Text("Cancel", color = c.text) }
                Button(onClick = {
                    if (step == 1) {
                        if (currentKey == vm.cloesedKey) { step = 2; error = "" }
                        else error = "Incorrect key"
                    } else {
                        when {
                            newKey1.isBlank() -> error = "Key cannot be empty"
                            newKey1.length != 4 -> error = "Must be exactly 4 characters"
                            newKey1 != newKey2 -> error = "Keys don\'t match"
                            else -> { vm.cloesedKey = newKey1; onDismiss()
                                vm.showToast("CLOESED KEY", "Key updated successfully", "low") }
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Purple),
                    modifier = Modifier.weight(1f)) {
                    Text(if (step == 1) "Verify" else "Save")
                }
            }
        },
        dismissButton = {}
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  COIN GIFT SHEET
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun CoinGiftSheet(vm: AppViewModel) {
    val c = vm.themeColors
    val contact = vm.coinGiftContact ?: return
    var amount by remember { mutableStateOf("10") }
    var message by remember { mutableStateOf("") }

    Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(0.6f))
            .clickable { vm.showCoinGift = false },
        contentAlignment = androidx.compose.ui.Alignment.BottomCenter
    ) {
        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(c.surface)
                .clickable(onClick = {})
                .padding(24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🪙 Gift Coins to ${contact.name}", color = c.text,
                fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "This costs you real coins — which makes it mean something.",
                color = c.textSub, fontSize = 13.sp
            )
            Text("Your balance: ${vm.coinBalance} coins", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { c -> c.isDigit() }) amount = it },
                label = { Text("Amount to gift", color = c.textSub) },
                singleLine = true,
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = c.text, unfocusedTextColor = c.text,
                    focusedBorderColor = Gold, unfocusedBorderColor = c.border
                )
            )

            // Preset amounts
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("5", "10", "25", "50").forEach { preset ->
                    Box(
                        modifier = androidx.compose.ui.Modifier.clip(RoundedCornerShape(10.dp))
                            .background(if (amount == preset) Gold.copy(0.2f) else c.bg)
                            .border(1.dp, if (amount == preset) Gold else c.border, RoundedCornerShape(10.dp))
                            .clickable { amount = preset }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) { Text(preset, color = if (amount == preset) Gold else c.textSub, fontSize = 13.sp) }
                }
            }

            GradientButton(
                text = "Gift ${amount.ifBlank { "0" }} coins to ${contact.name} ✦",
                onClick = { vm.giftCoins(contact, amount.toIntOrNull() ?: 0) },
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                enabled = amount.toIntOrNull() != null && (amount.toIntOrNull() ?: 0) > 0
            )
        }
    }
}
