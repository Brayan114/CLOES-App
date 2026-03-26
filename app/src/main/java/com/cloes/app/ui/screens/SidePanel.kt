package com.cloes.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cloes.app.R
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel

@Composable
fun SidePanel(vm: AppViewModel) {
    val c = vm.themeColors
    val panelBg = if (c.isDark) Color(0xFF12092B) else c.bg
    val headerBg = if (c.isDark) Color(0xFF1E1040) else c.surface2
    val dividerColor = if (c.isDark) Color(0xFF2D1F5C) else c.border
    val labelColor = if (c.isDark) Color.White else c.text
    val subColor = if (c.isDark) Color(0xFF7A6A9A) else c.textSub

    // Dim backdrop
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.52f))
            .pointerInput(Unit) { detectTapGestures { vm.showSidePanel = false } }
    )

    // Panel
    Box(
        modifier = Modifier.fillMaxHeight().width(290.dp).shadow(24.dp)
            .background(panelBg)
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Box(
                modifier = Modifier.fillMaxWidth().background(headerBg)
                    .statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Brush.linearGradient(listOf(Pink, Purple)), RoundedCornerShape(16.dp))
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.cloes_logo),
                            contentDescription = "CLOES Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text(vm.profile.name.ifBlank { "CLOES User" }, color = labelColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("@${vm.profile.handle.ifBlank { "user" }}", color = Purple, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip("${vm.coinBalance} 🪙", Gold)
                        if (vm.ghostModeEnabled) StatusChip("👻 Ghost", Purple)
                        if (vm.presenceOpen) StatusChip("🌿 Open", Color(0xFF22C55E))
                    }
                }
            }

            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

            val items = listOf(
                PanelItem("Muse Create",       Icons.Default.Animation,      Pink,                  "Animation studio",               "create"),
                PanelItem("Muse Clothing",      Icons.Default.Checkroom,      Purple,                "Buy, sell & dress with Muse",    "clothing"),
                PanelItem("Muse History",       Icons.Default.History,        Cyan,                  "All your Muse conversations",    "history"),
                PanelItem("Shared Spaces",      Icons.Default.Cabin,          Color(0xFF34D399),     "Your shared rooms",              "spaces"),
                PanelItem("Audio Extractor",    Icons.Default.AudioFile,      Color(0xFFF59E0B),     "Extract audio from videos",      "audio"),
                PanelItem("Cloes Echo",         Icons.Default.Lock,           Color(0xFF22C55E),     "Hidden vault for your files",    "echo"),
                PanelItem("Meet People",        Icons.Default.PeopleAlt,      Color(0xFFFF6B35),     "Discover beyond your contacts",  "meet"),
                PanelItem("Bloom History",      Icons.Default.Timeline,       Color(0xFFEC4899),     "Relationship timelines",         "bloomhist"),
                PanelItem("Voice Fingerprint",  Icons.Default.Mic,            Color(0xFF06B6D4),     "Your voice identity",            "voice"),
                PanelItem("Unsent Drawer",      Icons.Default.EditNote,       Color(0xFFC084FC),     "Things you never sent",          "unsent"),
                PanelItem("Muse Task",          Icons.Default.TaskAlt,        Color(0xFF06B6D4),     "Notes and to-dos",               "task")
            )

            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 6.dp)) {
                items(items) { item ->
                    SidePanelMenuItem(item = item, labelColor = labelColor, subColor = subColor) {
                        vm.showSidePanel = false
                        when (item.key) {
                            "create"    -> vm.showMuseCreate      = true
                            "clothing"  -> { vm.museClothingMode = ""; vm.showMuseClothing = true }
                            "history"   -> vm.showMuseHistory     = true
                            "spaces"    -> { vm.sharedSpaceContact = vm.contacts.firstOrNull(); vm.showSharedSpace = true }
                            "audio"     -> vm.showAudioExtractor  = true
                            "echo"      -> { vm.echoUnlocked = false; vm.showCloesEcho = true }
                            "meet"      -> vm.showMeetPage        = true
                            "bloomhist" -> { vm.bloomHistoryContactId = vm.contacts.firstOrNull()?.id; vm.showBloomHistory = true }
                            "voice"     -> vm.showVoiceFingerprint = true
                            "unsent"    -> vm.showUnsentDrawer    = true
                            "task"      -> vm.showMuseTask        = true
                        }
                    }
                }
            }

            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionRow("Logout", Icons.Default.Logout, Color(0xFFF59E0B)) { vm.showSidePanel = false; vm.showLogoutDialog = true }
                ActionRow("Delete Account", Icons.Default.DeleteForever, Color(0xFFEF4444)) { vm.showSidePanel = false; vm.showDeleteAccountDialog = true }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(color.copy(0.25f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ActionRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(tint.copy(0.13f))
            .clickable(onClick = onClick).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Text(label, color = tint, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

private data class PanelItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color, val subtitle: String, val key: String)

@Composable
private fun SidePanelMenuItem(item: PanelItem, labelColor: Color, subColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(item.color.copy(0.18f)), contentAlignment = Alignment.Center) {
            Icon(item.icon, null, tint = item.color, modifier = Modifier.size(21.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.label, color = labelColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(item.subtitle, color = subColor, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = subColor, modifier = Modifier.size(16.dp))
    }
}

