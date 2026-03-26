package com.cloes.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════════════════════════
//  MUSE CREATE — Full Animation Studio (FlipaClip-style)
// ══════════════════════════════════════════════════════════════════════════════

data class DrawPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val tool: String  // "pen" | "marker" | "eraser" | "fill"
)

data class AnimFrame(
    val id: Int,
    val paths: MutableList<DrawPath> = mutableListOf()
)

@Composable
fun MuseCreateLandingScreen(vm: AppViewModel) {
    val c = vm.themeColors
    var showHamburger by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2)
                    .statusBarsPadding().padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showMuseCreate = false }
                )
                Text(
                    "Muse Create ✦",
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(listOf(Pink, Purple, Cyan)),
                        fontSize = 20.sp, fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
                NeuIconButton(
                    icon = { Icon(Icons.Default.Menu, null, tint = c.textMid, modifier = Modifier.size(20.dp)) },
                    onClick = { showHamburger = true }
                )
            }

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Hero action
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(Purple.copy(0.3f), Pink.copy(0.3f))))
                        .border(1.dp, Purple.copy(0.4f), RoundedCornerShape(24.dp))
                        .clickable { vm.showMuseCanvas = true },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Add, null, tint = Pink, modifier = Modifier.size(42.dp))
                        Text("New Animation", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Start a fresh canvas", color = c.textSub, fontSize = 12.sp)
                    }
                }

                // Open Project
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(c.surface)
                        .border(1.dp, c.border, RoundedCornerShape(18.dp))
                        .clickable { vm.showMuseCreateProjects = true }
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                            .background(Cyan.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.FolderOpen, null, tint = Cyan, modifier = Modifier.size(24.dp)) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Open Project", color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("${vm.museAnimations.size} saved animation${if (vm.museAnimations.size != 1) "s" else ""}", color = c.textSub, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = c.textSub, modifier = Modifier.size(18.dp))
                }


                // AnimaForge Button
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF0d0d18), Color(0xFF131320))))
                        .border(1.dp, Color(0xFFe8142a).copy(0.5f), RoundedCornerShape(18.dp))
                        .clickable { vm.showAnimaForge = true }
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFe8142a).copy(0.3f), Color(0xFF00e5ff).copy(0.1f)))),
                        contentAlignment = Alignment.Center
                    ) { Text("⚡", fontSize = 22.sp) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AnimaForge", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("AI Animation Pipeline", color = Color(0xFF52526a), fontSize = 12.sp)
                    }
                    Icon(Icons.Default.OpenInNew, null, tint = Color(0xFFe8142a), modifier = Modifier.size(18.dp))
                }

                // Canvas size options preview
                SectionLabel("Canvas Sizes")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val sizes = listOf(
                        Triple("YouTube 1080p", 1920, 1080),
                        Triple("YouTube 720p", 1280, 720),
                        Triple("Instagram 16×9", 1080, 607),
                        Triple("Instagram 1×1", 1080, 1080),
                        Triple("TikTok 1080p", 1080, 1920),
                        Triple("TikTok 720p", 720, 1280),
                        Triple("Vimeo 1080p", 1920, 1080),
                        Triple("Facebook 720p", 1280, 720),
                        Triple("Tumblr 16×9", 1280, 720),
                        Triple("Tumblr 4×3", 1024, 768)
                    )
                    items(sizes) { (label, w, h) ->
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                .background(c.surface)
                                .border(1.dp, c.border, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(label, color = c.text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("${w}×${h}", color = Purple, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        // Hamburger menu overlay
        if (showHamburger) {
            MuseCreateHamburger(vm = vm, onDismiss = { showHamburger = false })
        }
    }
}

@Composable
fun MuseCreateHamburger(vm: AppViewModel, onDismiss: () -> Unit) {
    val c = vm.themeColors
    val scope = rememberCoroutineScope()
    var saveTitle by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.TopEnd
    ) {
        Column(
            modifier = Modifier
                .width(240.dp)
                .clip(RoundedCornerShape(bottomStart = 20.dp))
                .background(Color(0xFF12092B))
                .padding(vertical = 8.dp)
                .clickable(onClick = {}) // absorb
        ) {
            listOf(
                Triple(Icons.Default.Save,          "Save Animation",   "save"),
                Triple(Icons.Default.FolderOpen,    "Open Project",     "open"),
                Triple(Icons.Default.ContentCopy,   "Duplicate Frame",  "dup"),
                Triple(Icons.Default.Download,      "Export GIF/MP4",   "export"),
                Triple(Icons.Default.Settings,      "Canvas Settings",  "canvas"),
                Triple(Icons.Default.HelpOutline,   "Tutorial",         "help")
                // NOTE: Lip Sync deliberately removed per request
            ).forEach { (icon, label, key) ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable {
                            onDismiss()
                            when (key) {
                                "save"   -> showSaveDialog = true
                                "open"   -> vm.showMuseCreateProjects = true
                                "export" -> vm.showToast("Muse Create", "Export coming soon ✦", "low")
                                "canvas" -> vm.showToast("Muse Create", "Canvas settings coming soon ✦", "low")
                                else     -> vm.showToast("Muse Create", "$label ✦", "low")
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = Purple, modifier = Modifier.size(20.dp))
                    Text(label, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Animation", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = saveTitle,
                    onValueChange = { saveTitle = it },
                    label = { Text("Title", color = Color(0xFF7A6A9A)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Purple,
                        unfocusedBorderColor = Color(0xFF2D1F5C)
                    )
                )
            },
            confirmButton = {
                GradientButton(
                    text = "Save ✦",
                    onClick = {
                        vm.saveMuseAnimation(saveTitle, frameCount = 1)
                        showSaveDialog = false
                        saveTitle = ""
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel", color = Color(0xFF7A6A9A)) }
            },
            containerColor = Color(0xFF12092B),
            titleContentColor = Color.White
        )
    }
}

@Composable
fun MuseCreateProjectsScreen(vm: AppViewModel) {
    val c = vm.themeColors

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2)
                    .statusBarsPadding().padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showMuseCreateProjects = false }
                )
                Text(
                    "My Animations",
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(listOf(Pink, Purple)),
                        fontSize = 20.sp, fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text("${vm.museAnimations.size}", color = c.textSub, fontSize = 13.sp)
            }

            if (vm.museAnimations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("🎬", fontSize = 52.sp)
                        Text("No animations yet", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Start a new animation and save it here", color = c.textSub, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        GradientButton(
                            text = "New Animation ✦",
                            onClick = { vm.showMuseCreateProjects = false }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vm.museAnimations) { anim ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                .background(c.surface)
                                .border(1.dp, c.border, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))
                                    .background(Brush.linearGradient(listOf(Purple.copy(0.3f), Pink.copy(0.3f)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎬", fontSize = 24.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(anim.title, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("${anim.frameCount} frame${if (anim.frameCount != 1) "s" else ""} · ${anim.canvasWidth}×${anim.canvasHeight}", color = c.textSub, fontSize = 11.sp)
                                Text(anim.createdAt, color = Purple.copy(0.7f), fontSize = 10.sp)
                            }
                            // Open
                            NeuIconButton(
                                icon = { Icon(Icons.Default.PlayArrow, null, tint = Purple, modifier = Modifier.size(18.dp)) },
                                onClick = { vm.showMuseCanvas = true; vm.showMuseCreateProjects = false }
                            )
                            // Delete
                            NeuIconButton(
                                icon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) },
                                onClick = { vm.museAnimations.remove(anim); vm.showToast("Deleted", "${anim.title} removed", "low") }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Full Muse Create Canvas ────────────────────────────────────────────────────
@Composable
fun MuseCreateCanvasScreen(vm: AppViewModel) {
    val c = vm.themeColors
    val scope = rememberCoroutineScope()

    // Canvas state
    val canvasSizes = listOf(
        Triple("YouTube 720p", 1280, 720),
        Triple("Instagram 1×1", 1080, 1080),
        Triple("TikTok 1080p", 1080, 1920),
        Triple("Custom", 800, 600)
    )
    var selectedCanvasSizeIdx by remember { mutableStateOf(0) }
    var showCanvasPicker by remember { mutableStateOf(false) }
    var showHamburger by remember { mutableStateOf(false) }

    // Frames
    val frames = remember { mutableStateListOf(AnimFrame(0)) }
    var currentFrameIdx by remember { mutableStateOf(0) }
    val currentFrame = frames.getOrNull(currentFrameIdx) ?: frames.first()

    // Drawing state
    var selectedTool by remember { mutableStateOf("pen") }
    var selectedColor by remember { mutableStateOf(Pink) }
    var strokeWidth by remember { mutableStateOf(6f) }
    var showColorPicker by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    // Copy/paste
    var copiedFrame by remember { mutableStateOf<AnimFrame?>(null) }
    var selectionRect by remember { mutableStateOf<Rect?>(null) }
    var showCopyPasteMenu by remember { mutableStateOf(false) }

    // Undo/redo per frame
    val undoStacks = remember { mutableStateMapOf<Int, MutableList<DrawPath>>() }
    val redoStacks = remember { mutableStateMapOf<Int, MutableList<DrawPath>>() }

    val palette = listOf(
        Pink, Purple, Cyan, Color(0xFFF59E0B), Color(0xFF22C55E),
        Color(0xFFEF4444), Color.White, Color.Black,
        Color(0xFF06B6D4), Color(0xFFFF6B35), Color(0xFF8B5CF6), Color(0xFF10B981)
    )

    val tools = listOf(
        "pen" to Icons.Default.Edit,
        "marker" to Icons.Default.BorderColor,
        "eraser" to Icons.Default.AutoFixNormal,
        "fill" to Icons.Default.FormatColorFill,
        "select" to Icons.Default.SelectAll,
        "shape" to Icons.Default.Category
    )

    // Playback
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                currentFrameIdx = (currentFrameIdx + 1) % frames.size
                kotlinx.coroutines.delay(100)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top Bar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2)
                    .statusBarsPadding().padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showMuseCreate = false }
                )
                Text(
                    "Muse Create",
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(listOf(Pink, Purple)),
                        fontSize = 16.sp, fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
                // Canvas size picker
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        .background(c.surface)
                        .clickable { showCanvasPicker = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(canvasSizes[selectedCanvasSizeIdx].first, color = c.textMid, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
                // Undo
                NeuIconButton(
                    icon = { Icon(Icons.Default.Undo, null, tint = if (currentFrame.paths.isNotEmpty()) Purple else c.textSub, modifier = Modifier.size(16.dp)) },
                    onClick = {
                        if (currentFrame.paths.isNotEmpty()) {
                            val removed = currentFrame.paths.removeLast()
                            redoStacks.getOrPut(currentFrameIdx) { mutableListOf() }.add(removed)
                        }
                    }
                )
                // Redo
                NeuIconButton(
                    icon = { Icon(Icons.Default.Redo, null, tint = if ((redoStacks[currentFrameIdx]?.isNotEmpty()) == true) Purple else c.textSub, modifier = Modifier.size(16.dp)) },
                    onClick = {
                        redoStacks[currentFrameIdx]?.let { stack ->
                            if (stack.isNotEmpty()) currentFrame.paths.add(stack.removeLast())
                        }
                    }
                )
                // Clear frame
                NeuIconButton(
                    icon = { Icon(Icons.Default.DeleteSweep, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp)) },
                    onClick = { currentFrame.paths.clear() }
                )
                // Hamburger
                NeuIconButton(
                    icon = { Icon(Icons.Default.Menu, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { showHamburger = true }
                )
            }

            // ── Tool Bar ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tools.forEach { (tool, icon) ->
                    Box(
                        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTool == tool) Purple.copy(0.22f) else Color.Transparent)
                            .clickable { selectedTool = tool },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = if (selectedTool == tool) Purple else c.textSub, modifier = Modifier.size(19.dp))
                    }
                }
                // Color swatch
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape)
                        .background(selectedColor)
                        .border(2.dp, if (showColorPicker) Purple else c.border, CircleShape)
                        .clickable { showColorPicker = !showColorPicker }
                )
                // Stroke size indicator
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape)
                        .background(c.surface2)
                        .border(1.dp, c.border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size((strokeWidth / 2.5f).dp.coerceIn(4.dp, 20.dp)).clip(CircleShape)
                            .background(selectedColor)
                    )
                }
                // Copy frame / paste frame buttons
                NeuIconButton(
                    icon = { Icon(Icons.Default.ContentCopy, null, tint = Cyan, modifier = Modifier.size(16.dp)) },
                    onClick = { copiedFrame = currentFrame.copy(paths = currentFrame.paths.toMutableList()); vm.showToast("Muse Create", "Frame copied", "low") }
                )
                NeuIconButton(
                    icon = { Icon(Icons.Default.ContentPaste, null, tint = if (copiedFrame != null) Cyan else c.textSub, modifier = Modifier.size(16.dp)) },
                    onClick = {
                        copiedFrame?.let { src ->
                            currentFrame.paths.addAll(src.paths.map { it.copy() })
                            vm.showToast("Muse Create", "Frame pasted", "low")
                        }
                    }
                )
            }

            // ── Stroke Width Slider ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2)
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Size", color = c.textSub, fontSize = 10.sp)
                Slider(
                    value = strokeWidth,
                    onValueChange = { strokeWidth = it },
                    valueRange = 1f..40f,
                    modifier = Modifier.weight(1f).height(28.dp),
                    colors = SliderDefaults.colors(thumbColor = Purple, activeTrackColor = Purple)
                )
                Text("${strokeWidth.toInt()}px", color = Purple, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            // ── Color Picker Row ─────────────────────────────────────────
            if (showColorPicker) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(c.surface)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    palette.forEach { col ->
                        Box(
                            modifier = Modifier.size(28.dp).clip(CircleShape).background(col)
                                .border(2.dp, if (selectedColor == col) Color.White else Color.Transparent, CircleShape)
                                .clickable { selectedColor = col; showColorPicker = false }
                        )
                    }
                }
            }

            // ── Drawing Canvas ────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.White)) {
                // Ghost layer (previous frame, semi-transparent)
                if (currentFrameIdx > 0) {
                    val prevFrame = frames[currentFrameIdx - 1]
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        prevFrame.paths.forEach { path ->
                            drawAnimPath(path, alpha = 0.25f)
                        }
                    }
                }

                // Current frame
                var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

                Canvas(
                    modifier = Modifier.fillMaxSize().pointerInput(selectedTool, selectedColor, strokeWidth) {
                        detectDragGestures(
                            onDragStart = { offset -> currentPoints = listOf(offset) },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPoints = currentPoints + change.position
                            },
                            onDragEnd = {
                                if (selectedTool == "fill") {
                                    // Flood fill — add solid fill path covering entire canvas
                                    currentFrame.paths.add(DrawPath(
                                        points = listOf(Offset(0f, 0f), Offset(size.width.toFloat(), size.height.toFloat())),
                                        color = selectedColor,
                                        strokeWidth = 0f,
                                        tool = "fill"
                                    ))
                                } else if (currentPoints.size > 1) {
                                    currentFrame.paths.add(DrawPath(
                                        points = currentPoints,
                                        color = if (selectedTool == "eraser") Color.White else selectedColor,
                                        strokeWidth = if (selectedTool == "eraser") strokeWidth * 3 else strokeWidth,
                                        tool = selectedTool
                                    ))
                                }
                                currentPoints = emptyList()
                                redoStacks[currentFrameIdx]?.clear()
                            }
                        )
                    }
                ) {
                    // Draw saved paths
                    currentFrame.paths.forEach { path ->
                        drawAnimPath(path)
                    }
                    // Draw in-progress stroke
                    if (currentPoints.size > 1) {
                        val composePath = Path().apply {
                            moveTo(currentPoints.first().x, currentPoints.first().y)
                            currentPoints.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = composePath,
                            color = if (selectedTool == "eraser") Color.White else selectedColor,
                            style = Stroke(
                                width = if (selectedTool == "eraser") strokeWidth * 3 else strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            // ── Frame Strip (timeline) ─────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth().background(c.surface2)) {
                // Playback controls
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeuIconButton(
                        icon = { Icon(Icons.Default.SkipPrevious, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                        onClick = { currentFrameIdx = 0 }
                    )
                    NeuIconButton(
                        icon = { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Purple, modifier = Modifier.size(20.dp)) },
                        onClick = { isPlaying = !isPlaying }
                    )
                    NeuIconButton(
                        icon = { Icon(Icons.Default.SkipNext, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                        onClick = { currentFrameIdx = (frames.size - 1).coerceAtLeast(0) }
                    )
                    Text("Frame ${currentFrameIdx + 1}/${frames.size}", color = c.textSub, fontSize = 11.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    // Add frame
                    NeuIconButton(
                        icon = { Icon(Icons.Default.AddBox, null, tint = Cyan, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            frames.add(AnimFrame(frames.size))
                            currentFrameIdx = frames.size - 1
                        }
                    )
                    // Duplicate frame
                    NeuIconButton(
                        icon = { Icon(Icons.Default.FileCopy, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp)) },
                        onClick = {
                            val dup = AnimFrame(frames.size, currentFrame.paths.map { it.copy() }.toMutableList())
                            frames.add(currentFrameIdx + 1, dup)
                            currentFrameIdx += 1
                        }
                    )
                    // Delete frame
                    NeuIconButton(
                        icon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) },
                        onClick = {
                            if (frames.size > 1) {
                                frames.removeAt(currentFrameIdx)
                                currentFrameIdx = (currentFrameIdx - 1).coerceAtLeast(0)
                            }
                        }
                    )
                }

                // Frame thumbnails strip
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(frames) { idx, frame ->
                        Box(
                            modifier = Modifier.size(width = 54.dp, height = 38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(
                                    2.dp,
                                    if (idx == currentFrameIdx) Purple else c.border,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { currentFrameIdx = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                frame.paths.take(5).forEach { path -> drawAnimPath(path, scale = 0.06f) }
                            }
                            Text("${idx + 1}", color = if (idx == currentFrameIdx) Purple else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    item {
                        Box(
                            modifier = Modifier.size(width = 54.dp, height = 38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Purple.copy(0.1f))
                                .border(1.dp, Purple.copy(0.3f), RoundedCornerShape(8.dp))
                                .clickable {
                                    frames.add(AnimFrame(frames.size))
                                    currentFrameIdx = frames.size - 1
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = Purple, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Canvas size picker overlay
        if (showCanvasPicker) {
            CanvasSizePickerOverlay(
                currentIdx = selectedCanvasSizeIdx,
                onSelect = { selectedCanvasSizeIdx = it; showCanvasPicker = false },
                onDismiss = { showCanvasPicker = false }
            )
        }

        // Hamburger menu
        if (showHamburger) {
            MuseCreateHamburger(
                vm = vm,
                onDismiss = { showHamburger = false }
            )
        }
    }
}

private fun DrawScope.drawAnimPath(path: DrawPath, alpha: Float = 1f, scale: Float = 1f) {
    if (path.tool == "fill") {
        drawRect(color = path.color.copy(alpha = alpha))
        return
    }
    if (path.points.size < 2) return
    val composePath = Path().apply {
        val pts = if (scale != 1f) path.points.map { Offset(it.x * scale, it.y * scale) } else path.points
        moveTo(pts.first().x, pts.first().y)
        pts.drop(1).forEach { lineTo(it.x, it.y) }
    }
    drawPath(
        path = composePath,
        color = path.color.copy(alpha = alpha),
        style = Stroke(width = path.strokeWidth * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

@Composable
private fun CanvasSizePickerOverlay(currentIdx: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    val sizes = listOf(
        Triple("YouTube 1080p", 1920, 1080),
        Triple("YouTube 720p", 1280, 720),
        Triple("Instagram 16×9", 1080, 607),
        Triple("Instagram 1×1", 1080, 1080),
        Triple("TikTok 1080p", 1080, 1920),
        Triple("TikTok 720p", 720, 1280),
        Triple("Vimeo 1080p", 1920, 1080),
        Triple("Facebook 720p", 1280, 720),
        Triple("Tumblr 16×9", 1280, 720),
        Triple("Tumblr 4×3", 1024, 768)
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.6f)).clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(320.dp).clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF12092B))
                .clickable(onClick = {})
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Canvas Size", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color(0xFF7A6A9A)) }
            }
            // Width/Height display (mirrors FlipaClip style)
            val sel = sizes[currentIdx]
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("WIDTH", color = Color(0xFF7A6A9A), fontSize = 9.sp, letterSpacing = 1.sp)
                    Text("${sel.second}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("px", color = Color(0xFF7A6A9A), fontSize = 11.sp)
                }
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF2D1F5C)).align(Alignment.CenterVertically), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Link, null, tint = Purple, modifier = Modifier.size(14.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("HEIGHT", color = Color(0xFF7A6A9A), fontSize = 9.sp, letterSpacing = 1.sp)
                    Text("${sel.third}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("px", color = Color(0xFF7A6A9A), fontSize = 11.sp)
                }
            }
            HorizontalDivider(color = Color(0xFF2D1F5C))
            LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                itemsIndexed(sizes) { idx, (label, w, h) ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(if (idx == currentIdx) Purple.copy(0.12f) else Color.Transparent)
                            .clickable { onSelect(idx) }
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, color = Color.White, fontSize = 14.sp)
                        Box(
                            modifier = Modifier.size(22.dp).clip(CircleShape)
                                .background(if (idx == currentIdx) Pink else Color(0xFF2D1F5C)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (idx == currentIdx) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Projects Screen (with edit/delete) ────────────────────────────────────────
// (Replaces the one in the file above — the one above has the base implementation,
//  this extension adds per-project actions)
