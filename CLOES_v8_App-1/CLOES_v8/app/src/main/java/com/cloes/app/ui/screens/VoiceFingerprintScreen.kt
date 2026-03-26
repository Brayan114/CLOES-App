package com.cloes.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import androidx.compose.ui.unit.*
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel

// ══════════════════════════════════════════════════════════════════════════════
//  VOICE FINGERPRINT SCREEN
//  "Over time Muse learns the rhythm, pace, and tone of your voice notes
//   with specific people. It tells you: You speak 30% faster with Jade
//   than anyone else."
// ══════════════════════════════════════════════════════════════════════════════

data class VoiceProfile(
    val contactId: Long,
    val contactName: String,
    val paletteIndex: Int,
    val sampleCount: Int,
    val avgSpeedWPM: Int,       // words per minute
    val dominantMood: String,   // "warm" | "playful" | "calm" | "tense"
    val insight: String,        // the key generated insight
    val speedDelta: Int         // % faster or slower vs baseline (negative = slower)
)

@Composable
fun VoiceFingerprintScreen(vm: AppViewModel) {
    val c = vm.themeColors

    // Build demo fingerprints from existing contacts
    val fingerprints = remember {
        vm.contacts.mapIndexed { i, contact ->
            val samples = (contact.messages.size * 0.4).toInt().coerceAtLeast(0)
            val speed   = listOf(120, 145, 98, 160, 112, 88, 175, 130)[i % 8]
            val delta   = listOf(24, -12, 38, -8, 15, -22, 42, 5)[i % 8]
            val moods   = listOf("warm", "playful", "calm", "tense", "warm", "calm", "playful", "warm")
            val mood    = moods[i % moods.size]
            val insight = when {
                samples < 3 -> "Send more voice notes to unlock insights ✦"
                delta > 30  -> "You speak ${delta}% faster with ${contact.name} than anyone else."
                delta < -15 -> "Something about ${contact.name} slows you down — comfortable silences."
                mood == "warm"    -> "Your voice is warmest when talking to ${contact.name} 🌸"
                mood == "playful" -> "You're at your most playful with ${contact.name} ✦"
                mood == "calm"    -> "These conversations bring out your calmest self."
                else              -> "You have ${samples} voice moments with ${contact.name} ✦"
            }
            VoiceProfile(
                contactId    = contact.id,
                contactName  = contact.name,
                paletteIndex = contact.paletteIndex,
                sampleCount  = samples,
                avgSpeedWPM  = speed,
                dominantMood = mood,
                insight      = insight,
                speedDelta   = delta
            )
        }.filter { it.sampleCount > 0 }
    }

    var selectedProfile by remember { mutableStateOf<VoiceProfile?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2)
                    .statusBarsPadding().padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showVoiceFingerprint = false }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Voice Fingerprints",
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.linearGradient(listOf(Pink, Purple, Cyan)),
                            fontSize = 18.sp, fontWeight = FontWeight.Bold
                        )
                    )
                    Text("How you sound with each person ✦", color = c.textSub, fontSize = 11.sp)
                }
                Text("🎙", fontSize = 22.sp)
            }

            // ── Intro banner ─────────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth()
                    .padding(14.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Purple.copy(0.18f), Pink.copy(0.12f))))
                    .border(1.dp, Purple.copy(0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🎙", fontSize = 26.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Muse is listening — quietly", color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Over time, Muse learns the rhythm, pace, and tone of your voice with each person. After enough voice notes, it tells you something true about yourself.",
                            color = c.textSub, fontSize = 12.sp, lineHeight = 18.sp
                        )
                    }
                }
            }

            // ── Fingerprint cards ─────────────────────────────────────────────
            if (fingerprints.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("🎙", fontSize = 52.sp)
                        Text("No voice notes yet", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Send voice messages to build your fingerprints", color = c.textSub, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(fingerprints) { fp ->
                        VoiceFingerprintCard(fp = fp, c = c, onClick = { selectedProfile = fp })
                    }
                }
            }
        }

        // ── Detail popup ─────────────────────────────────────────────────────
        selectedProfile?.let { fp ->
            VoiceFingerprintDetail(fp = fp, c = c, onDismiss = { selectedProfile = null })
        }
    }
}

@Composable
private fun VoiceFingerprintCard(fp: VoiceProfile, c: CloesColors, onClick: () -> Unit) {
    val moodColor = when (fp.dominantMood) {
        "warm"    -> Color(0xFFFF9020)
        "playful" -> Pink
        "calm"    -> Cyan
        "tense"   -> Color(0xFFEF4444)
        else      -> Purple
    }

    // Animate card glow based on mood
    val glowColor by animateColorAsState(
        targetValue = moodColor.copy(0.15f),
        animationSpec = tween(800),
        label = "voiceGlow"
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))) {
                FragmentAvatar(paletteIndex = fp.paletteIndex, seed = fp.contactId.toFloat() % 1f)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(fp.contactName, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    // Mood badge
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            .background(moodColor.copy(0.15f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(fp.dominantMood, color = moodColor, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp)
                    }
                }

                if (fp.sampleCount >= 3) {
                    Text(fp.insight, color = c.textMid, fontSize = 12.sp, lineHeight = 17.sp)
                } else {
                    Text("${fp.sampleCount} voice note${if (fp.sampleCount != 1) "s" else ""} — keep talking ✦",
                        color = c.textSub, fontSize = 12.sp)
                }
            }

            // Speed indicator
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${fp.avgSpeedWPM}", color = Purple,
                    fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace, lineHeight = 20.sp)
                Text("wpm", color = c.textSub, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun VoiceFingerprintDetail(fp: VoiceProfile, c: CloesColors, onDismiss: () -> Unit) {
    val moodColor = when (fp.dominantMood) {
        "warm"    -> Color(0xFFFF9020)
        "playful" -> Pink
        "calm"    -> Cyan
        "tense"   -> Color(0xFFEF4444)
        else      -> Purple
    }

    val moodEmoji = when (fp.dominantMood) {
        "warm"    -> "🌸"
        "playful" -> "✨"
        "calm"    -> "🌊"
        "tense"   -> "⚡"
        else      -> "✦"
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black.copy(0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable(onClick = {})  // absorb
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp))) {
                        FragmentAvatar(paletteIndex = fp.paletteIndex, seed = fp.contactId.toFloat() % 1f)
                    }
                    Column {
                        Text(fp.contactName, color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${fp.sampleCount} voice samples analysed", color = c.textSub, fontSize = 12.sp)
                    }
                }

                // Main insight
                if (fp.sampleCount >= 3) {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(moodColor.copy(0.12f))
                            .border(1.dp, moodColor.copy(0.3f), RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(moodEmoji, fontSize = 22.sp)
                            Text(fp.insight, color = c.text, fontSize = 14.sp, lineHeight = 21.sp)
                        }
                    }
                }

                // Stats grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        Triple("Speed",    "${fp.avgSpeedWPM} wpm",   Purple),
                        Triple("Mood",     fp.dominantMood,            moodColor),
                        Triple("vs avg",   "${if (fp.speedDelta >= 0) "+" else ""}${fp.speedDelta}%", if (fp.speedDelta >= 0) Color(0xFF22C55E) else Pink)
                    ).forEach { (label, value, color) ->
                        Column(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(c.surface).padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace)
                            Text(label, color = c.textSub, fontSize = 10.sp, letterSpacing = 0.5.sp)
                        }
                    }
                }

                if (fp.sampleCount < 3) {
                    Text(
                        "Send ${3 - fp.sampleCount} more voice note${if (3 - fp.sampleCount != 1) "s" else ""} to unlock your fingerprint with ${fp.contactName}",
                        color = c.textSub, fontSize = 13.sp, lineHeight = 19.sp
                    )
                }

                GradientButton("Close", onClick = { onDismiss() })
            }
        }
    }
}
