package com.cloes.app.ui.theme

import androidx.compose.runtime.*
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// ─── Brand Colors ─────────────────────────────────────────────────────────────
val Pink   = Color(0xFFFF3385)
val Cyan   = Color(0xFF33A1FF)
val Purple = Color(0xFF8B5CF6)
val Mint   = Color(0xFF4DFFD4)
val Gold   = Color(0xFFF59E0B)
val Crimson = Color(0xFFEF4444)
val Lime   = Color(0xFF22C55E)
val Orange = Color(0xFFFF6B35)

// ─── Theme State ─────────────────────────────────────────────────────────────
enum class AppTheme { Default, Dark, Rose, Forest }

data class CloesColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val border: Color,
    val border2: Color,
    val shadowA: Color,
    val shadowB: Color,
    val text: Color,
    val textMid: Color,
    val textSub: Color,
    val bubbleSent1: Color = Color(0xFF8B5CF6),
    val bubbleSent2: Color = Color(0xFFFF3385),
    val bubbleRecvBg: Color = Color(0xEDF5F0FF),
    val bubbleRecvText: Color = Color(0xFF2A1A4A),
    val isDark: Boolean = false
)

val DefaultColors = CloesColors(
    bg        = Color(0xFFF2EEF9),
    surface   = Color(0x9EFFFFFF),
    surface2  = Color(0xE0FFFFFF),
    border    = Color(0xE6FFFFFF),
    border2   = Color(0x33B49BE6),
    shadowA   = Color(0x3D9473D2),
    shadowB   = Color(0xF0FFFFFF),
    text      = Color(0xFF18102A),
    textMid   = Color(0xFF5C4D74),
    textSub   = Color(0xFF9A8FB0),
    isDark    = false
)

val DarkColors = CloesColors(
    bg        = Color(0xFF0F0A1E),
    surface   = Color(0xCC1E1232),
    surface2  = Color(0xE6281941),
    border    = Color(0x66503278),
    border2   = Color(0x66503278),
    shadowA   = Color(0x80000000),
    shadowB   = Color(0x33503278),
    text      = Color(0xFFF0E8FF),
    textMid   = Color(0xFFC4B0E8),
    textSub   = Color(0xFF7A6A9A),
    bubbleRecvBg  = Color(0xE637235A),
    bubbleRecvText = Color(0xFFF0E8FF),
    isDark    = true
)

val RoseColors = CloesColors(
    bg        = Color(0xFFFFF0F5),
    surface   = Color(0x9EFFFFFF),
    surface2  = Color(0xF2FFF0F8),
    border    = Color(0xE6FFFFFF),
    border2   = Color(0x33B49BE6),
    shadowA   = Color(0x33FF6496),
    shadowB   = Color(0xF2FFF0F8),
    text      = Color(0xFF18102A),
    textMid   = Color(0xFF5C4D74),
    textSub   = Color(0xFF9A8FB0),
    isDark    = false
)

val ForestColors = CloesColors(
    bg        = Color(0xFFF0F7F0),
    surface   = Color(0x9EFFFFFF),
    surface2  = Color(0xF5F0FFF5),
    border    = Color(0xE6FFFFFF),
    border2   = Color(0x2E327850),
    shadowA   = Color(0x2E327850),
    shadowB   = Color(0xF5F0FFF5),
    text      = Color(0xFF18102A),
    textMid   = Color(0xFF5C4D74),
    textSub   = Color(0xFF9A8FB0),
    isDark    = false
)

val LocalCloesColors = compositionLocalOf { DefaultColors }

@Composable
fun cloesColors() = LocalCloesColors.current

val PALETTES = listOf(
    listOf(Color(0xFFFF3385), Color(0xFF8B5CF6), Color(0xFF33A1FF)),
    listOf(Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFFFF6B35)),
    listOf(Color(0xFF4DFFD4), Color(0xFF33A1FF), Color(0xFF8B5CF6)),
    listOf(Color(0xFF22C55E), Color(0xFF4DFFD4), Color(0xFF33A1FF)),
    listOf(Color(0xFFFF3385), Color(0xFFF59E0B), Color(0xFF22C55E)),
    listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFFF43F5E)),
    listOf(Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF8B5CF6)),
    listOf(Color(0xFFFF6B35), Color(0xFFF59E0B), Color(0xFFEF4444)),
    listOf(Color(0xFF4DFFD4), Color(0xFF22C55E), Color(0xFFF59E0B)),
    listOf(Color(0xFFFF3385), Color(0xFF33A1FF), Color(0xFF4DFFD4)),
    listOf(Color(0xFF8B5CF6), Color(0xFF4DFFD4), Color(0xFF22C55E)),
    listOf(Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFF4DFFD4))
)

// ─── Font families available ──────────────────────────────────────────────────

val FontDefault   = FontFamily.Default
val FontSerif     = FontFamily.Serif
val FontMonospace = FontFamily.Monospace
val FontCursive   = FontFamily.Cursive

val FONT_OPTIONS = listOf(
    "Default" to FontFamily.Default,
    "Serif"   to FontFamily.Serif,
    "Mono"    to FontFamily.Monospace,
    "Cursive" to FontFamily.Cursive
)

val LocalCloesFont = compositionLocalOf<androidx.compose.ui.text.font.FontFamily> { androidx.compose.ui.text.font.FontFamily.Default }
