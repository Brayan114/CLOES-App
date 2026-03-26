package com.cloes.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel

// Each hex must be UNIQUE — one per hue family
val SWATCHES = listOf(
    "#FF3385",  // Hot pink
    "#FF6B35",  // Orange-red
    "#F59E0B",  // Amber
    "#EAB308",  // Yellow
    "#84CC16",  // Lime
    "#22C55E",  // Green
    "#10B981",  // Emerald
    "#14B8A6",  // Teal
    "#06B6D4",  // Cyan
    "#0EA5E9",  // Sky
    "#3B82F6",  // Blue
    "#6366F1",  // Indigo
    "#8B5CF6",  // Violet
    "#A855F7",  // Purple
    "#EC4899",  // Rose-pink
    "#EF4444",  // Red
    "#F97316",  // Deep orange
    "#78716C",  // Stone
    "#64748B",  // Slate
    "#1E293B",  // Dark navy
    "#FFFFFF",  // White
    "#D4A853",  // Gold
    "#C084FC",  // Soft lavender
    "#34D399",  // Mint
    "#FB7185",  // Pastel rose
    "#38BDF8",  // Soft sky
    "#A3E635",  // Yellow-green
    "#2DD4BF",  // Aqua
    "#E879F9",  // Fuchsia
    "#FCA5A5",  // Blush
    "#6EE7B7",  // Pale mint
    "#93C5FD"   // Pale blue
)


val SWATCH_NAMES = mapOf(
    "#FF3385" to "Hot Pink",
    "#FF6B35" to "Orange-Red",
    "#F59E0B" to "Amber",
    "#EAB308" to "Yellow",
    "#84CC16" to "Lime",
    "#22C55E" to "Green",
    "#10B981" to "Emerald",
    "#14B8A6" to "Teal",
    "#06B6D4" to "Cyan",
    "#0EA5E9" to "Sky",
    "#3B82F6" to "Blue",
    "#6366F1" to "Indigo",
    "#8B5CF6" to "Violet",
    "#A855F7" to "Purple",
    "#EC4899" to "Rose",
    "#EF4444" to "Red",
    "#F97316" to "Deep Orange",
    "#78716C" to "Stone",
    "#64748B" to "Slate",
    "#1E293B" to "Dark Navy",
    "#FFFFFF" to "White",
    "#D4A853" to "Gold",
    "#C084FC" to "Lavender",
    "#34D399" to "Mint",
    "#FB7185" to "Pastel Rose",
    "#38BDF8" to "Soft Sky",
    "#A3E635" to "Yellow-Green",
    "#2DD4BF" to "Aqua",
    "#E879F9" to "Fuchsia",
    "#FCA5A5" to "Blush",
    "#6EE7B7" to "Pale Mint",
    "#93C5FD" to "Pale Blue"
)

fun colorNameFromHex(hex: String): String = SWATCH_NAMES[hex.uppercase()] ?: SWATCH_NAMES[hex] ?: "Color"

fun hexToColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) { Color.Gray }

@Composable
fun OnboardScreen(vm: AppViewModel) {
    val c = vm.themeColors

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            // Step indicator
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(4) { i ->
                    val active = i < vm.onboardStep
                    Box(
                        modifier = Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp))
                            .background(if (active) Purple else c.surface2)
                    )
                }
            }

            when (vm.onboardStep) {
                1    -> Step1Colors(vm, c)
                2    -> Step2Name(vm, c)
                3    -> Step3Handle(vm, c)
                else -> Step4Done(vm, c)
            }
        }
    }
}

@Composable
private fun Step1Colors(vm: AppViewModel, c: CloesColors) {
    val columns = 8

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Encode Your Light ✦", color = c.text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Pick 5–16 colors that feel like you. This becomes your unique Fragment.",
            color = c.textMid, fontSize = 13.sp, lineHeight = 20.sp,
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Fragment preview
        Box(
            modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            val palette = if (vm.onboardPalette.size >= 3) vm.onboardPalette
            else listOf(Purple, Pink, Cyan, Color(0xFFF59E0B))
            FragmentArt(palette = palette, seed = 0.5f, animating = true)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected chips
        if (vm.onboardPalette.isEmpty()) {
            Text(
                "Tap colors to encode your Light...",
                color = c.textSub, fontSize = 11.5.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                vm.onboardPalette.forEachIndexed { idx, color ->
                    // Find hex for this color to get the name
                    val hex = SWATCHES.firstOrNull { hexToColor(it) == color } ?: ""
                    val cName = colorNameFromHex(hex)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .clickable { vm.onboardPalette.removeAt(idx) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("×", color = Color.White.copy(0.9f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            cName,
                            fontSize = 7.sp,
                            color = c.textSub,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 42.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Color grid — each swatch is unique
        SWATCHES.chunked(columns).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowColors.forEach { hex ->
                    val color = hexToColor(hex)
                    val selected = vm.onboardPalette.contains(color)
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(color)
                            .then(
                                if (selected) Modifier.border(3.dp, Color.White, RoundedCornerShape(11.dp))
                                else Modifier
                            )
                            .clickable {
                                if (selected) vm.onboardPalette.remove(color)
                                else if (vm.onboardPalette.size < 16) vm.onboardPalette.add(color)
                                else vm.showToast("Palette", "Max 16 colors!", "mid")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                repeat(columns - rowColors.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        GradientButton(
            text = "Continue →",
            onClick = {
                if (vm.onboardPalette.size < 5)
                    vm.showToast("Palette", "Pick at least 5 colors ✦", "mid")
                else
                    vm.onboardStep = 2
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun Step2Name(vm: AppViewModel, c: CloesColors) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("What do they call you?", color = c.text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("This is your display name — it can be anything.", color = c.textMid, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(28.dp))
        OutlinedTextField(
            value = vm.onboardName,
            onValueChange = { vm.onboardName = it },
            label = { Text("Your name", color = c.textSub) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple, unfocusedBorderColor = c.border,
                focusedTextColor = c.text, unfocusedTextColor = c.text
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))
        GradientButton(
            text = "Continue →",
            onClick = {
                if (vm.onboardName.isBlank()) vm.showToast("Name", "Enter your name", "mid")
                else vm.onboardStep = 3
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Step3Handle(vm: AppViewModel, c: CloesColors) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Your @handle", color = c.text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("How people find you on CLOES.", color = c.textMid, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(28.dp))
        OutlinedTextField(
            value = vm.onboardHandle,
            onValueChange = { vm.onboardHandle = it.lowercase().replace(" ", ".") },
            label = { Text("@handle", color = c.textSub) },
            prefix = { Text("@", color = Purple, fontWeight = FontWeight.Bold) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple, unfocusedBorderColor = c.border,
                focusedTextColor = c.text, unfocusedTextColor = c.text
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))
        GradientButton(
            text = "Continue →",
            onClick = {
                if (vm.onboardHandle.isBlank()) vm.showToast("Handle", "Pick a handle", "mid")
                else vm.onboardStep = 4
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Step4Done(vm: AppViewModel, c: CloesColors) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp).clip(RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            FragmentArt(palette = vm.onboardPalette.ifEmpty { listOf(Purple, Pink, Cyan) }, seed = 0.5f, animating = true)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("You're in, ${vm.onboardName.ifBlank { "Lumina" }} ✦", color = c.text, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("@${vm.onboardHandle.ifBlank { "lumina.x" }}", color = Purple, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(32.dp))
        GradientButton(
            text = "Enter CLOES →",
            onClick = {
                vm.profile = vm.profile.copy(
                    name = vm.onboardName.ifBlank { "Lumina" },
                    handle = vm.onboardHandle.ifBlank { "lumina.x" },
                    palette = vm.onboardPalette.toList()
                )
                vm.currentScreen = "main"
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
