package com.cloes.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cloes.app.ui.theme.*
import androidx.compose.foundation.Canvas

// ─── Gradient Button ──────────────────────────────────────────────────────────
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "btn")

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = if (enabled)
                        listOf(Pink, Purple) else listOf(Color.Gray, Color.DarkGray)
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 22.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ─── Outline Button ───────────────────────────────────────────────────────────
@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = cloesColors()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(c.surface)
            .border(1.5.dp, Purple.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Purple, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ─── Icon Button (neumorphic) ─────────────────────────────────────────────────
@Composable
fun NeuIconButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = cloesColors()
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(c.bg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

// ─── Toggle ───────────────────────────────────────────────────────────────────
@Composable
fun CloesToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val track = if (checked) Brush.linearGradient(listOf(Pink, Purple))
    else Brush.horizontalGradient(listOf(Purple.copy(alpha = 0.18f), Purple.copy(alpha = 0.18f)))
    val thumbOffset by animateFloatAsState(if (checked) 20f else 0f, label = "tog")

    Box(
        modifier = modifier
            .width(46.dp)
            .height(26.dp)
            .clip(CircleShape)
            .background(brush = track)
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .padding(start = (4 + thumbOffset).dp, top = 4.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

// ─── Neumorphic Input Field ───────────────────────────────────────────────────
@Composable
fun CloesInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    val c = cloesColors()
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = c.textSub, fontSize = 14.sp) },
        singleLine = singleLine,
        maxLines = maxLines,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = c.bg,
            focusedContainerColor = c.bg,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedTextColor = c.text,
            focusedTextColor = c.text
        ),
        shape = RoundedCornerShape(15.dp),
        modifier = modifier.fillMaxWidth()
    )
}

// ─── Section Label ────────────────────────────────────────────────────────────
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        color = cloesColors().textSub,
        fontSize = 9.sp,
        letterSpacing = 1.8.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

// ─── Sheet Handle ─────────────────────────────────────────────────────────────
@Composable
fun ColumnScope.SheetHandle() {
    Box(
        modifier = Modifier
            .padding(bottom = 18.dp)
            .width(38.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Purple.copy(alpha = 0.2f))
            .align(Alignment.CenterHorizontally)
    )
}

// ─── Toast ────────────────────────────────────────────────────────────────────
@Composable
fun CloesToast(
    visible: Boolean,
    name: String,
    msg: String,
    urgency: String,
    paletteIndex: Int = 0,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    val c = cloesColors()
    val urgencyColor = when (urgency) {
        "high" -> Color(0xFFEF4444)
        "mid"  -> Color(0xFFF59E0B)
        else   -> Color(0xFF33A1FF)
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit  = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .zIndex(500f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(c.surface2)
                .border(1.dp, c.border, RoundedCornerShape(18.dp))
                .clickable { onClick() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
            ) {
                FragmentAvatar(paletteIndex = paletteIndex, seed = paletteIndex * 0.13f + 0.05f)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                Text(msg, fontSize = 11.sp, color = c.textMid, maxLines = 1)
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(urgencyColor)
            )
        }
    }
}

// ─── Tab Chip ─────────────────────────────────────────────────────────────────
@Composable
fun TabChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = cloesColors()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Purple.copy(alpha = 0.11f) else Color.Transparent)
            .border(
                1.5.dp,
                if (selected) Purple else Purple.copy(alpha = 0.22f),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(
            text, fontSize = 12.5.sp,
            color = if (selected) Purple else c.textMid,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ─── Glass Card ───────────────────────────────────────────────────────────────
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val c = cloesColors()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(c.surface)
            .border(1.dp, c.border, RoundedCornerShape(18.dp)),
        content = content
    )
}

// ─── Urgency Pulse ────────────────────────────────────────────────────────────
@Composable
fun UrgencyPulse(urgency: String, modifier: Modifier = Modifier) {
    val color = when (urgency) {
        "high" -> Color(0xFFEF4444)
        "mid"  -> Color(0xFFF59E0B)
        else   -> Color(0xFF33A1FF)
    }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    Box(
        modifier = modifier
            .size(8.dp)
            .scale(if (urgency != "low") scale else 1f)
            .clip(CircleShape)
            .background(color)
    )
}

// ─── Aurora Background ────────────────────────────────────────────────────────
@Composable
fun AuroraBackground(
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        when (theme) {
            AppTheme.Dark -> {
                drawRect(brush = Brush.linearGradient(listOf(Color(0xFF0F0A1E), Color(0xFF1E1232))))
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF8B5CF6).copy(0.2f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.2f),
                        radius = w * 0.6f
                    ),
                    radius = w * 0.6f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.2f)
                )
            }
            AppTheme.Rose -> {
                drawRect(brush = Brush.linearGradient(listOf(Color(0xFFFFF0F5), Color(0xFFFFE0EE))))
            }
            AppTheme.Forest -> {
                drawRect(brush = Brush.linearGradient(listOf(Color(0xFFF0F7F0), Color(0xFFE0F5E0))))
            }
            else -> {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFF0ECFD), Color(0xFFFFF0F8), Color(0xFFEBF5FF)),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(w, h)
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFA064FF).copy(0.18f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(w * 0.05f, h * 0.15f),
                        radius = w * 0.65f
                    ),
                    radius = w * 0.65f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.05f, h * 0.15f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFFF3385).copy(0.13f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(w * 0.95f, h * 0.05f),
                        radius = w * 0.5f
                    ),
                    radius = w * 0.5f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.95f, h * 0.05f)
                )
            }
        }
    }
}
