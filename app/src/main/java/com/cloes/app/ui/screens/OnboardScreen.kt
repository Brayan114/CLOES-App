package com.cloes.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel

val SWATCHES = listOf(
    "#FF3385","#FF6B35","#F59E0B","#22C55E","#4DFFD4","#33A1FF","#8B5CF6","#EC4899",
    "#EF4444","#06B6D4","#3B82F6","#14B8A6","#A855F7","#F97316","#84CC16","#6366F1",
    "#E11D48","#0EA5E9","#10B981","#8B5CF6","#FBBF24","#34D399","#60A5FA","#F472B6",
    "#FB923C","#A3E635","#38BDF8","#C084FC","#FCA5A5","#6EE7B7","#93C5FD","#F9A8D4"
)

fun hexToColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) { Color.Gray }

@Composable
fun OnboardScreen(vm: AppViewModel) {
    val c = vm.themeColors

    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    if (vm.onboardStep > 1) vm.onboardStep-- else vm.currentScreen = "splash"
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = c.textMid)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf(1, 2, 3).forEach { step ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (vm.onboardStep == step) 18.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        vm.onboardStep == step -> Purple
                                        step < vm.onboardStep  -> Lime
                                        else                   -> Purple.copy(0.2f)
                                    }
                                )
                        )
                    }
                }
            }

            AnimatedContent(targetState = vm.onboardStep, label = "step") { step ->
                when (step) {
                    1    -> Step1Colors(vm, c)
                    2    -> Step2Identity(vm, c)
                    else -> Step3Done(vm, c)
                }
            }
        }
    }
}

@Composable
private fun Step1Colors(vm: AppViewModel, c: CloesColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 8.dp)
    ) {
        Text("Encode Your Light ✦", color = c.text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            "Pick 5–16 colors that feel like you. This becomes your unique Fragment.",
            color = c.textMid, fontSize = 13.sp, lineHeight = 20.sp,
            modifier = Modifier.padding(top = 5.dp, bottom = 20.dp)
        )

        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(RoundedCornerShape(30.dp))
                .align(Alignment.CenterHorizontally)
        ) {
            val palette = if (vm.onboardPalette.size >= 3) vm.onboardPalette
            else listOf(Pink, Purple, Cyan)
            FragmentArt(palette = palette, seed = 0.5f, animating = true)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Chosen strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(Purple.copy(alpha = 0.06f))
                .border(1.5.dp, Purple.copy(alpha = 0.22f), RoundedCornerShape(13.dp))
                .defaultMinSize(minHeight = 54.dp)
                .padding(11.dp)
        ) {
            if (vm.onboardPalette.isEmpty()) {
                Text(
                    "Tap colors to encode your Light...",
                    color = c.textSub, fontSize = 11.5.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    vm.onboardPalette.forEachIndexed { idx, color ->
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .clickable { vm.onboardPalette.removeAt(idx) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val columns = 8
        SWATCHES.chunked(columns).forEach { rowColors ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp)
            ) {
                rowColors.forEach { hex ->
                    val color = hexToColor(hex)
                    val selected = vm.onboardPalette.contains(color)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
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
                // Fill empty columns
                repeat(columns - rowColors.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        GradientButton(
            text = "Next — Name My Light →",
            onClick = {
                if (vm.onboardPalette.size < 5)
                    vm.showToast("Palette", "Pick at least 5 colors ✦", "mid")
                else vm.onboardStep = 2
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun Step2Identity(vm: AppViewModel, c: CloesColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 8.dp)
    ) {
        Text("Name Your Fragment ✦", color = c.text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            "What should your connections call you?",
            color = c.textMid, fontSize = 13.sp,
            modifier = Modifier.padding(top = 5.dp, bottom = 20.dp)
        )
        SectionLabel("Display Name")
        CloesInput(
            value = vm.profile.name,
            onValueChange = { vm.profile = vm.profile.copy(name = it) },
            placeholder = "Your name",
            modifier = Modifier.padding(bottom = 13.dp)
        )
        SectionLabel("@Handle")
        CloesInput(
            value = vm.profile.handle,
            onValueChange = { vm.profile = vm.profile.copy(handle = it) },
            placeholder = "your.handle",
            modifier = Modifier.padding(bottom = 20.dp)
        )
        GradientButton(
            text = "Reveal My Fragment →",
            onClick = {
                if (vm.profile.name.isBlank()) {
                    vm.showToast("Name", "Please enter a display name", "mid"); return@GradientButton
                }
                vm.onboardStep = 3
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Step3Done(vm: AppViewModel, c: CloesColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(150.dp).clip(RoundedCornerShape(30.dp))) {
            FragmentArt(
                palette = vm.onboardPalette.ifEmpty { listOf(Pink, Purple, Cyan) },
                seed = 0.42f, animating = true
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("You're radiant, ${vm.profile.name} ✦", color = c.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("@${vm.profile.handle}", color = Purple, fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            vm.onboardPalette.take(5).forEach { color ->
                Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(color))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Your Fragment is your private identity key — it lets people recognize you without ever needing a phone number.",
            color = c.textMid, fontSize = 13.sp, lineHeight = 20.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        GradientButton("Sign In ✦", { vm.currentScreen = "main" }, modifier = Modifier.fillMaxWidth())
    }
}
