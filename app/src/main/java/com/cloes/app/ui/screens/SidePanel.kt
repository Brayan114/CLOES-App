package com.cloes.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel

// ══════════════════════════════════════════════════════════════════════════════
//  SIDE PANEL
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun SidePanel(vm: AppViewModel) {
    val c = vm.themeColors

    // Dim backdrop — tap to close
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.52f))
            .pointerInput(Unit) { detectTapGestures { vm.showSidePanel = false } }
    )

    // Panel
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(286.dp)
            .shadow(24.dp)
            .background(Brush.verticalGradient(listOf(c.bg, c.surface, c.bg)))
            .pointerInput(Unit) { detectTapGestures { /* absorb */ } }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Pink.copy(0.2f), Purple.copy(0.2f))))
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Brush.linearGradient(listOf(Pink, Purple)), RoundedCornerShape(16.dp))
                    ) {
                        FragmentArt(
                            palette = vm.profile.palette.ifEmpty { listOf(Pink, Purple, Cyan) },
                            seed = vm.profile.lightSeed, animating = false
                        )
                    }
                    Text(vm.profile.name, color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("@${vm.profile.handle}", color = Purple, fontSize = 12.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Gold.copy(0.18f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("${vm.coinBalance} \uD83E\uDE99", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            HorizontalDivider(color = c.border, thickness = 0.5.dp)

            // ── Menu Items ────────────────────────────────────────────────
            val items = listOf(
                PanelItem("Muse Draw",       Icons.Default.Draw,          Pink,                  "AI drawing canvas",             "draw"),
                PanelItem("Muse Clothing",   Icons.Default.Checkroom,     Purple,                "Buy, sell & dress with Muse",   "clothing"),
                PanelItem("Muse History",    Icons.Default.History,       Cyan,                  "All your Muse conversations",   "history"),
                PanelItem("Audio Extractor", Icons.Default.AudioFile,     Color(0xFFF59E0B),     "Extract audio from videos",     "audio"),
                PanelItem("Cloes Echo",      Icons.Default.Lock,          Color(0xFF22C55E),     "Hidden vault for your files",   "echo"),
                PanelItem("Sign Up / Login", Icons.Default.AccountCircle, Color(0xFFFF6B35),     "Create or access your account", "signup"),
                PanelItem("Muse Task",       Icons.Default.TaskAlt,       Color(0xFF06B6D4),     "Notes and to-dos",              "task")
            )

            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 6.dp)) {
                items(items) { item ->
                    SidePanelMenuItem(item = item, c = c) {
                        vm.showSidePanel = false
                        when (item.key) {
                            "draw"     -> vm.showMuseDraw      = true
                            "clothing" -> { vm.museClothingMode = ""; vm.showMuseClothing = true }
                            "history"  -> vm.showMuseHistory   = true
                            "audio"    -> vm.showAudioExtractor = true
                            "echo"     -> { vm.echoUnlocked = false; vm.showCloesEcho = true }
                            "signup"   -> vm.showSignupPage    = true
                            "task"     -> vm.showMuseTask      = true
                        }
                    }
                }
            }

            // ── Logout + Delete ───────────────────────────────────────────
            HorizontalDivider(color = c.border, thickness = 0.5.dp)
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF59E0B).copy(0.12f))
                        .clickable { vm.showSidePanel = false; vm.showLogoutDialog = true }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Logout, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                    Text("Logout", color = Color(0xFFF59E0B), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEF4444).copy(0.1f))
                        .clickable { vm.showSidePanel = false; vm.showDeleteAccountDialog = true }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteForever, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    Text("Delete Account", color = Color(0xFFEF4444), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private data class PanelItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color, val subtitle: String, val key: String)

@Composable
private fun SidePanelMenuItem(item: PanelItem, c: CloesColors, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(item.color.copy(0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, null, tint = item.color, modifier = Modifier.size(21.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.label, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(item.subtitle, color = c.textSub, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = c.textSub, modifier = Modifier.size(16.dp))
    }
}
