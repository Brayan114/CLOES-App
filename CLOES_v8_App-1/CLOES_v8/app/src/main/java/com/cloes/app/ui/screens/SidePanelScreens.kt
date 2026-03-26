package com.cloes.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.*
import com.cloes.app.data.Message
import com.cloes.app.data.MsgType
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════════════════════════
//  MUSE SELL WITH INTRO POPUP
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun MuseSellWithIntro(vm: AppViewModel) {
    var showSeller by remember { mutableStateOf(false) }

    if (!showSeller) {
        // Full-screen tap-to-dismiss intro
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.75f))
                .clickable { showSeller = true },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.padding(40.dp)
            ) {
                Text("💸", fontSize = 64.sp)
                Text(
                    "Sell Anything",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(listOf(Pink, Purple, Cyan))
                    )
                )
                Text(
                    "Clothes, Electronics, Art\nand so much more",
                    color = Color.White.copy(0.85f),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Tap anywhere to continue",
                    color = Color.White.copy(0.45f),
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    } else {
        CloesSellerScreen(vm)
    }
}

@Composable
fun MuseClothingScreen(vm: AppViewModel) {
    val c = vm.themeColors

    if (vm.museClothingMode.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
            AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(c.surface2)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NeuIconButton(
                        icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                        onClick = { vm.showMuseClothing = false }
                    )
                    Text("Muse Clothing", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.weight(0.12f))
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("What would you like to do?", color = c.text, fontSize = 20.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Explore fashion your way", color = c.textSub, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        Triple("Cloes Buy 🛍️", "Browse our partner shops", "buy"),
                        Triple("Cloes Sell 💸", "Set up your seller space & earn", "sell"),
                        Triple("Cloes Dress ✦", "AI outfit stylist & generator", "dress")
                    ).forEach { (title, sub, mode) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Brush.linearGradient(listOf(Purple.copy(0.12f), Pink.copy(0.12f))))
                                .border(1.dp, Purple.copy(0.2f), RoundedCornerShape(20.dp))
                                .clickable { vm.museClothingMode = mode }
                                .padding(20.dp)
                        ) {
                            Column {
                                Text(title, color = c.text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text(sub, color = c.textSub, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Purple,
                                modifier = Modifier.align(Alignment.CenterEnd).size(22.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(0.3f))
            }
        }
        return
    }
    when (vm.museClothingMode) {
        "buy"   -> CloesBuyScreen(vm)
        "sell"  -> MuseSellWithIntro(vm)
        "dress" -> MuseDressScreen(vm)
    }
}

// ── Cloes Buy ─────────────────────────────────────────────────────────────────
@Composable
fun CloesBuyScreen(vm: AppViewModel) {
    val c = vm.themeColors
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.museClothingMode = "" }
                )
                Text("Cloes Buy 🛍️", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("PARTNER SHOPS", color = c.textSub, fontSize = 10.sp, letterSpacing = 2.sp)
                // LaraKenj Wares shop card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Purple.copy(0.18f), Pink.copy(0.18f))))
                        .border(1.dp, Purple.copy(0.3f), RoundedCornerShape(20.dp))
                        .clickable { uriHandler.openUri("https://larakenjwares.vercel.app/") }
                        .padding(20.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(listOf(Pink, Purple))),
                            contentAlignment = Alignment.Center
                        ) { Text("LK", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("LaraKenj Wares", color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Fashion & lifestyle goods", color = c.textSub, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Purple.copy(0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.OpenInNew, null, tint = Purple, modifier = Modifier.size(12.dp))
                                Text("Visit Shop", color = Purple, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(c.surface).padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("More shops coming soon ✦", color = c.textSub, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ── Cloes Seller Screen ────────────────────────────────────────────────────────
@Composable
fun CloesSellerScreen(vm: AppViewModel) {
    val c = vm.themeColors
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    if (submitted) {
        SubscriptionScreen(vm, onClose = { vm.museClothingMode = ""; vm.showMuseClothing = false })
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.museClothingMode = "" }
                )
                Text("Cloes Sellers Space 💸", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Create your seller account", color = c.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Buy, sell your goods and earn money on CLOES", color = c.textSub, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))

                listOf(
                    Triple("Full Name", name, Icons.Default.Person),
                    Triple("Email", email, Icons.Default.Email),
                    Triple("Shop Name", shopName, Icons.Default.Store),
                    Triple("Password", password, Icons.Default.Lock)
                ).forEachIndexed { i, (label, value, icon) ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = {
                            when (i) { 0 -> name = it; 1 -> email = it; 2 -> shopName = it; 3 -> password = it }
                        },
                        label = { Text(label, color = c.textSub) },
                        leadingIcon = { Icon(icon, null, tint = Purple) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (i == 3) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple, unfocusedBorderColor = c.border,
                            focusedTextColor = c.text, unfocusedTextColor = c.text
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Sign in with Google
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .border(1.dp, c.border, RoundedCornerShape(14.dp))
                        .clickable { vm.showToast("Google", "Google sign-in coming soon", "low") }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, null, tint = Color(0xFF4285F4), modifier = Modifier.size(20.dp))
                        Text("Continue with Google", color = c.text, fontSize = 14.sp)
                    }
                }

                GradientButton(
                    text = "Sign Up & Continue",
                    onClick = {
                        if (name.isBlank() || email.isBlank()) {
                            vm.showToast("Seller", "Please fill all fields", "mid")
                        } else {
                            submitted = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ── Subscription Screen ────────────────────────────────────────────────────────
@Composable
fun SubscriptionScreen(vm: AppViewModel, onClose: () -> Unit) {
    val c = vm.themeColors
    val plans = listOf(
        Triple("Free", "0", listOf("10 outfit images/day", "Basic Muse AI", "Browse shops")),
        Triple("Pro ✦", "4.99/mo", listOf("Unlimited outfit images", "Priority Muse AI", "Sell on Cloes", "Exclusive badges", "No ads")),
        Triple("Elite 💎", "9.99/mo", listOf("Everything in Pro", "Custom shop page", "Analytics dashboard", "Early feature access", "Dedicated support"))
    )
    var selected by remember { mutableStateOf(1) }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeuIconButton(icon = { Icon(Icons.Default.Close, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = onClose)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Choose your plan", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("✦ Unlock the full CLOES experience", color = c.textSub, fontSize = 13.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
                plans.forEachIndexed { i, (name, price, perks) ->
                    val isSelected = selected == i
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Brush.linearGradient(listOf(Purple.copy(0.2f), Pink.copy(0.2f)))
                                else Brush.horizontalGradient(listOf(c.surface, c.surface)))
                            .border(if (isSelected) 2.dp else 1.dp,
                                if (isSelected) Purple else c.border, RoundedCornerShape(20.dp))
                            .clickable { selected = i }
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(name, color = c.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(if (price == "0") "FREE" else "$$price", color = if (isSelected) Purple else c.textMid,
                                    fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            perks.forEach { perk ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, null, tint = if (isSelected) Purple else c.textSub,
                                        modifier = Modifier.size(14.dp))
                                    Text(perk, color = c.textSub, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                GradientButton(
                    text = if (selected == 0) "Continue Free" else "Subscribe — \$${plans[selected].second}",
                    onClick = {
                        vm.showToast("Subscription", if (selected == 0) "Continuing on Free plan" else "${plans[selected].first} plan activated! ✦", "low")
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Cancel anytime. No hidden fees.", color = c.textSub, fontSize = 11.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

// ── Muse Dress Screen ─────────────────────────────────────────────────────────
@Composable
fun MuseDressScreen(vm: AppViewModel) {
    val c = vm.themeColors
    // Always show welcome the first time user enters; info icon re-shows it later
    var showWelcome by remember { mutableStateOf(true) }
    var showSubscription by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // drawable outfit image names (14 outfits)
    val outfitImages = (1..14).map { "muse_outfit_$it" }

    if (showSubscription) {
        SubscriptionScreen(vm, onClose = { showSubscription = false }); return
    }

    Box(modifier = Modifier.fillMaxSize().background(c.bg).imePadding()) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.museClothingMode = ""; vm.showMuseClothing = false }
                )
                Text(
                    "Muse Dress ✦",
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(listOf(Pink, Purple)),
                        fontSize = 18.sp, fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
                // Info icon — shows welcome message again
                NeuIconButton(
                    icon = { Icon(Icons.Default.Info, null, tint = Purple, modifier = Modifier.size(18.dp)) },
                    onClick = { showWelcome = true }
                )
                // Images used counter
                Text(
                    "${vm.museDressImagesUsed}/10",
                    color = if (vm.museDressImagesUsed >= 10) Color(0xFFEF4444) else c.textSub,
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                )
            }

            // Messages list
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                reverseLayout = true
            ) {
                // Show generated outfit images
                items(vm.museDressMessages.reversed()) { msg ->
                    if (msg.type == MsgType.Image) {
                        // Outfit card — styled gradient preview
                        val outfitIndex = vm.museDressMessages.indexOf(msg)
                        val palettes = listOf(
                            listOf(Purple, Pink), listOf(Pink, Color(0xFFFF8C00)),
                            listOf(Color(0xFF06B6D4), Purple), listOf(Color(0xFF22C55E), Color(0xFF06B6D4)),
                            listOf(Color(0xFFF59E0B), Pink), listOf(Purple, Color(0xFF06B6D4))
                        )
                        val pal = palettes[outfitIndex % palettes.size]
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(220.dp)
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Brush.linearGradient(listOf(pal[0].copy(0.35f), pal[1].copy(0.25f)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Large outfit hanger icon
                                    Box(
                                        modifier = Modifier.size(90.dp)
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Brush.radialGradient(listOf(pal[0].copy(0.4f), pal[1].copy(0.2f)))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Checkroom, null, tint = pal[0], modifier = Modifier.size(54.dp))
                                    }
                                    // Outfit label extracted from the message text
                                    val label = msg.text.substringAfter("\"").substringBefore("\"").take(30)
                                    if (label.isNotBlank()) {
                                        Text(
                                            "Outfit for:\n\"$label\"",
                                            color = c.text,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                    // Style tags
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("✦ Curated", "AI Styled").forEach { tag ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(pal[0].copy(0.18f))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(tag, color = pal[0], fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                            Text(
                                "🤍 Muse loves you but it can make mistakes, don't forget to check",
                                color = c.textSub, fontSize = 10.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    } else {
                        val isSent = msg.sent
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSent) Brush.linearGradient(listOf(vm.bubbleSent1, vm.bubbleSent2))
                                        else Brush.horizontalGradient(listOf(c.surface, c.surface2))
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(msg.text, color = if (isSent) Color.White else c.text, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Input bar
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2).navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add image / camera
                NeuIconButton(
                    icon = { Icon(Icons.Default.AddPhotoAlternate, null, tint = Purple, modifier = Modifier.size(20.dp)) },
                    onClick = { vm.showToast("Muse Dress", "Image picker coming with camera integration", "low") }
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask Muse Dress anything...", color = c.textSub, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple, unfocusedBorderColor = c.border,
                        focusedTextColor = c.text, unfocusedTextColor = c.text
                    ),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3
                )
                // Send
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Pink, Purple)))
                        .clickable {
                            if (input.isBlank()) return@clickable
                            if (vm.museDressImagesUsed >= 10) { showSubscription = true; return@clickable }
                            val userMsg = input
                            input = ""
                            vm.museDressMessages.add(Message(text = userMsg, sent = true, timestamp = ""))
                            scope.launch {
                                delay(1200)
                                val outfitIdx = (vm.museDressImagesUsed % outfitImages.size)
                                vm.museDressImagesUsed++
                                vm.museDressMessages.add(
                                    Message(
                                        text = "Outfit suggestion for: \"$userMsg\"\n(${outfitImages[outfitIdx]})",
                                        sent = false, timestamp = "",
                                        type = MsgType.Image
                                    )
                                )
                                if (vm.museDressImagesUsed >= 10) showSubscription = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Welcome overlay — tap anywhere to dismiss
        if (showWelcome) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f))
                    .pointerInput(Unit) { detectTapGestures { showWelcome = false; vm.museDressWelcomeSeen = true } },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.padding(28.dp).fillMaxWidth()
                        .pointerInput(Unit) { detectTapGestures { /* swallow taps on card */ } },
                    shape = RoundedCornerShape(24.dp),
                    color = c.surface
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Welcome to Muse Dress ✦", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("I can help you find what to wear! Just:", color = c.textSub, fontSize = 13.sp)
                        listOf(
                            "📸 Take a picture or send me images of your clothes — I'll tell you how to swag them",
                            "🎉 Tell me the occasion e.g. Birthday party or Club night",
                            "👗 Ask if your outfit looks great and get tips or confirmation",
                            "✨ I can generate an outfit for a specific occasion from scratch"
                        ).forEach { tip ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(tip, color = c.text, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val subscribeText = buildAnnotatedString {
                            append("You have ")
                            withStyle(SpanStyle(color = Purple, fontWeight = FontWeight.Bold)) { append("10 images a day") }
                            append(" but you can ")
                            withStyle(SpanStyle(color = Pink, fontWeight = FontWeight.Bold)) {
                                pushStringAnnotation("subscribe", "subscribe")
                                append("subscribe")
                                pop()
                            }
                            append(" for more.")
                        }
                        Text(subscribeText, fontSize = 12.sp, color = c.textSub,
                            modifier = Modifier.clickable { showWelcome = false; showSubscription = true })
                        Spacer(modifier = Modifier.height(4.dp))
                        GradientButton("Got it! Let's style ✦", { showWelcome = false; vm.museDressWelcomeSeen = true },
                            modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  LOGOUT + DELETE ACCOUNT DIALOGS  (Feature 3)
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun LogoutDialog(vm: AppViewModel) {
    val c = vm.themeColors
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.6f)), contentAlignment = Alignment.Center) {
        Surface(modifier = Modifier.padding(32.dp).fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = c.surface) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("👋", fontSize = 36.sp)
                Text("Logging out", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Should CLOES save your account first?", color = c.textSub, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlineButton("Cancel", { vm.showLogoutDialog = false }, modifier = Modifier.weight(1f))
                    GradientButton("Yes, Save & Logout", { vm.logout() }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun DeleteAccountDialog(vm: AppViewModel) {
    val c = vm.themeColors
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.6f)), contentAlignment = Alignment.Center) {
        Surface(modifier = Modifier.padding(28.dp).fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = c.surface) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(40.dp))
                Text("Delete Account", color = Color(0xFFEF4444), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Hello, you are about to delete your account. All your Contacts, Muse Chats, Vibes, Circles, Coins, Profile and more will be permanently deleted.",
                    color = c.textSub, fontSize = 13.sp, textAlign = TextAlign.Center)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GradientButton("STOP", { vm.showDeleteAccountDialog = false }, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEF4444)).clickable { vm.deleteAccount() }.padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("PROCEED", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  MUSE HISTORY  (Feature 4)
// ══════════════════════════════════════════════════════════════════════════════
data class MuseHistoryEntry(val id: Long, val date: String, val day: String, val time: String,
    val year: String, val preview: String, val type: String,
    val month: String = "", val dayNum: Int = 0) // "muse" | "dress"

@Composable
fun MuseHistoryScreen(vm: AppViewModel) {
    val c = vm.themeColors
    var mode by remember { mutableStateOf("") } // "" = picker, "muse" | "dress"
    var selectedEntry by remember { mutableStateOf<MuseHistoryEntry?>(null) }
    // Drill-down states
    var selectedYear  by remember { mutableStateOf<String?>(null) }
    var selectedMonth by remember { mutableStateOf<String?>(null) }
    var selectedDay   by remember { mutableStateOf<Int?>(null) }
    var searchQuery   by remember { mutableStateOf("") }

    // Full history data with month + dayNum fields
    val allHistory = remember {
        listOf(
            MuseHistoryEntry(1,  "Mar 10", "Monday",    "9:41 AM",  "2026", "What should I wear today?",        "dress", "March",    10),
            MuseHistoryEntry(2,  "Mar 10", "Monday",    "11:20 AM", "2026", "I'm feeling anxious...",           "muse",  "March",    10),
            MuseHistoryEntry(3,  "Mar 9",  "Sunday",    "3:15 PM",  "2026", "Generate an outfit for clubbing",  "dress", "March",    9),
            MuseHistoryEntry(4,  "Mar 9",  "Sunday",    "8:02 PM",  "2026", "Help me plan my week",             "muse",  "March",    9),
            MuseHistoryEntry(5,  "Mar 8",  "Saturday",  "10:00 AM", "2026", "Does this look good?",             "dress", "March",    8),
            MuseHistoryEntry(6,  "Mar 7",  "Friday",    "6:45 PM",  "2026", "I need motivation",                "muse",  "March",    7),
            MuseHistoryEntry(7,  "Feb 20", "Friday",    "2:10 PM",  "2026", "Valentine's date outfit",          "dress", "February", 20),
            MuseHistoryEntry(8,  "Feb 14", "Saturday",  "9:00 AM",  "2026", "Help me feel confident today",     "muse",  "February", 14),
            MuseHistoryEntry(9,  "Jan 5",  "Sunday",    "7:30 PM",  "2026", "New year, new style?",             "dress", "January",  5),
            MuseHistoryEntry(10, "Dec 25", "Wednesday", "8:00 AM",  "2025", "Christmas party outfit",           "dress", "December", 25),
            MuseHistoryEntry(11, "Dec 31", "Tuesday",   "10:00 PM", "2025", "NYE outfit suggestions",           "dress", "December", 31),
            MuseHistoryEntry(12, "Nov 3",  "Sunday",    "11:00 AM", "2025", "I need help planning my goals",    "muse",  "November", 3),
        )
    }

    // ── Selected entry detail view ──────────────────────────────────────────
    if (selectedEntry != null) {
        val entry = selectedEntry!!
        Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(c.surface2).statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                        onClick = { selectedEntry = null })
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.preview, color = c.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${entry.day}, ${entry.date} ${entry.year} · ${entry.time}", color = c.textSub, fontSize = 11.sp)
                    }
                    NeuIconButton(icon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) },
                        onClick = { selectedEntry = null; vm.showToast("History", "Chat deleted", "low") })
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (entry.type == "dress") "👗" else "✦", fontSize = 40.sp)
                        Text("\"${entry.preview}\"", color = c.textSub, fontSize = 14.sp, textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp))
                        Text("This chat's full history would show here", color = c.textSub, fontSize = 12.sp)
                    }
                }
            }
        }
        return
    }

    // ── Mode picker (Muse AI vs Muse Dress) ────────────────────────────────
    if (mode.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
            AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(c.surface2).statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                        onClick = { vm.showMuseHistory = false })
                    Text("Muse History", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.weight(0.2f))
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf("muse" to ("Muse AI ✦" to "Your conversations with Muse"),
                           "dress" to ("Muse Dress 👗" to "Your outfit styling sessions")).forEach { (type, pair) ->
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                                .background(Brush.linearGradient(listOf(Purple.copy(0.12f), Pink.copy(0.12f))))
                                .border(1.dp, Purple.copy(0.2f), RoundedCornerShape(20.dp))
                                .clickable { mode = type; selectedYear = null; selectedMonth = null; selectedDay = null; searchQuery = "" }
                                .padding(20.dp)
                        ) {
                            Column {
                                Text(pair.first, color = c.text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text(pair.second, color = c.textSub, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Purple,
                                modifier = Modifier.align(Alignment.CenterEnd).size(22.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(0.4f))
            }
        }
        return
    }

    val filtered = allHistory.filter { it.type == mode }

    // ── Day detail — list conversations on a specific day ──────────────────
    if (selectedDay != null && selectedMonth != null && selectedYear != null) {
        val dayEntries = filtered.filter { it.year == selectedYear && it.month == selectedMonth && it.dayNum == selectedDay }
        Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(c.surface2).statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                        onClick = { selectedDay = null })
                    Column(modifier = Modifier.weight(1f)) {
                        Text("$selectedMonth ${selectedDay}, $selectedYear", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(if (dayEntries.isEmpty()) "No conversations" else "${dayEntries.size} conversation(s)", color = c.textSub, fontSize = 12.sp)
                    }
                }
                if (dayEntries.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("✦", fontSize = 36.sp)
                            Text("No conversations on this day", color = c.textSub, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(dayEntries) { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                    .background(c.surface).clickable { selectedEntry = entry }.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(40.dp).clip(CircleShape)
                                    .background(Purple.copy(0.15f)), contentAlignment = Alignment.Center) {
                                    Text(if (entry.type == "dress") "👗" else "✦", fontSize = 18.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.preview, color = c.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(entry.time, color = c.textSub, fontSize = 11.sp)
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = c.textSub, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // ── Month detail — show days of the selected month ─────────────────────
    if (selectedMonth != null && selectedYear != null) {
        val monthEntries = filtered.filter { it.year == selectedYear && it.month == selectedMonth }
        val daysInMonth = when (selectedMonth) {
            "February" -> if ((selectedYear?.toIntOrNull() ?: 2026) % 4 == 0) 29 else 28
            "April", "June", "September", "November" -> 30
            else -> 31
        }
        val activeDays = monthEntries.map { it.dayNum }.toSet()

        Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(c.surface2).statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                        onClick = { selectedMonth = null })
                    Text("$selectedMonth $selectedYear", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items((1..daysInMonth).toList()) { day ->
                        val hasConvos = day in activeDays
                        val dayEntries = monthEntries.filter { it.dayNum == day }
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(if (hasConvos) Purple.copy(0.08f) else c.surface)
                                .border(1.dp, if (hasConvos) Purple.copy(0.3f) else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { selectedDay = day }
                                .padding(horizontal = 16.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(modifier = Modifier.size(34.dp).clip(CircleShape)
                                    .background(if (hasConvos) Purple.copy(0.2f) else c.surface2),
                                    contentAlignment = Alignment.Center) {
                                    Text("$day", color = if (hasConvos) Purple else c.textSub,
                                        fontSize = 13.sp, fontWeight = if (hasConvos) FontWeight.Bold else FontWeight.Normal)
                                }
                                Column {
                                    Text("$selectedMonth $day", color = if (hasConvos) c.text else c.textSub, fontSize = 13.sp)
                                    if (hasConvos) Text("${dayEntries.size} conversation(s)", color = Purple, fontSize = 11.sp)
                                    else Text("No conversations", color = c.textSub, fontSize = 11.sp)
                                }
                            }
                            if (hasConvos) Icon(Icons.Default.ChevronRight, null, tint = Purple, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
        return
    }

    // ── Year detail — show months ────────────────────────────────────────────
    if (selectedYear != null) {
        val yearEntries = filtered.filter { it.year == selectedYear }
        val months = listOf("January","February","March","April","May","June",
                            "July","August","September","October","November","December")
        val activeMonths = yearEntries.map { it.month }.toSet()

        Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(c.surface2).statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                        onClick = { selectedYear = null })
                    Text(selectedYear!!, color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(months) { month ->
                        val hasConvos = month in activeMonths
                        val count = yearEntries.count { it.month == month }
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                .background(if (hasConvos) Brush.linearGradient(listOf(Purple.copy(0.1f), Pink.copy(0.07f)))
                                            else Brush.linearGradient(listOf(c.surface, c.surface)))
                                .border(1.dp, if (hasConvos) Purple.copy(0.25f) else Color.Transparent, RoundedCornerShape(14.dp))
                                .clickable(enabled = hasConvos) { if (hasConvos) selectedMonth = month }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(month, color = if (hasConvos) c.text else c.textSub, fontSize = 15.sp,
                                    fontWeight = if (hasConvos) FontWeight.SemiBold else FontWeight.Normal)
                                if (hasConvos) Text("$count conversation(s)", color = Purple, fontSize = 11.sp)
                                else Text("No activity", color = c.textSub, fontSize = 11.sp)
                            }
                            if (hasConvos) Icon(Icons.Default.ChevronRight, null, tint = Purple, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
        return
    }

    // ── Top-level: Year list + search bar ──────────────────────────────────
    val years = filtered.map { it.year }.distinct().sortedDescending()
    val searchYears = if (searchQuery.isBlank()) years
                      else years.filter { it.contains(searchQuery) }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2).statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { mode = "" })
                Text(if (mode == "muse") "Muse AI History" else "Muse Dress History",
                    color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            // Search bar
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(13.dp)).background(c.surface),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = c.textSub,
                    modifier = Modifier.padding(start = 12.dp, end = 8.dp).size(16.dp))
                TextField(value = searchQuery, onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by year...", color = c.textSub, fontSize = 13.sp) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent,
                        unfocusedTextColor = c.text, focusedTextColor = c.text),
                    modifier = Modifier.fillMaxWidth())
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (searchYears.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No history for \"$searchQuery\"", color = c.textSub, fontSize = 14.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(searchYears) { year ->
                        val yearCount = filtered.count { it.year == year }
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                                .background(Brush.linearGradient(listOf(Purple.copy(0.12f), Pink.copy(0.1f))))
                                .border(1.dp, Purple.copy(0.2f), RoundedCornerShape(18.dp))
                                .clickable { selectedYear = year; searchQuery = "" }
                                .padding(20.dp)
                        ) {
                            Column {
                                Text(year, color = c.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Text("$yearCount conversation(s)", color = c.textSub, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Purple,
                                modifier = Modifier.align(Alignment.CenterEnd).size(22.dp))
                        }
                    }
                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  AUDIO EXTRACTOR  (Feature 5)
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun AudioExtractorScreen(vm: AppViewModel) {
    val c = vm.themeColors
    var audioName by remember { mutableStateOf("") }
    var trimStart by remember { mutableStateOf(0f) }
    var trimEnd by remember { mutableStateOf(100f) }
    var videoLoaded by remember { mutableStateOf(false) }
    var extracted by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2).statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showAudioExtractor = false })
                Text("Audio Extractor 🎵", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Pick video
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                        .background(c.surface).border(2.dp, if (videoLoaded) Purple else c.border, RoundedCornerShape(20.dp))
                        .clickable { videoLoaded = true; extracted = false }
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(if (videoLoaded) Icons.Default.VideoFile else Icons.Default.VideoLibrary, null,
                            tint = if (videoLoaded) Purple else c.textSub, modifier = Modifier.size(40.dp))
                        Text(if (videoLoaded) "video_sample.mp4 ✓" else "Tap to select a video",
                            color = if (videoLoaded) Purple else c.textSub, fontSize = 13.sp)
                        if (!videoLoaded) Text("from your gallery or files", color = c.textSub, fontSize = 11.sp)
                    }
                }

                if (videoLoaded) {
                    // Waveform visual
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(14.dp)).background(c.surface)) {
                        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            repeat(40) { i ->
                                val h = ((i * 7 + 3) % 40 + 10).dp
                                Box(modifier = Modifier.width(3.dp).height(h).clip(RoundedCornerShape(2.dp))
                                    .background(if (i / 40f in trimStart / 100f..trimEnd / 100f) Purple else c.border))
                            }
                        }
                    }

                    // Trim sliders
                    Text("TRIM AUDIO", color = c.textSub, fontSize = 10.sp, letterSpacing = 2.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Start: ${trimStart.toInt()}s", color = c.textSub, fontSize = 11.sp)
                            Slider(value = trimStart, onValueChange = { trimStart = it.coerceAtMost(trimEnd - 1) },
                                valueRange = 0f..99f, colors = SliderDefaults.colors(thumbColor = Purple, activeTrackColor = Purple))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("End: ${trimEnd.toInt()}s", color = c.textSub, fontSize = 11.sp)
                            Slider(value = trimEnd, onValueChange = { trimEnd = it.coerceAtLeast(trimStart + 1) },
                                valueRange = 1f..100f, colors = SliderDefaults.colors(thumbColor = Pink, activeTrackColor = Pink))
                        }
                    }

                    // Name the audio
                    OutlinedTextField(
                        value = audioName,
                        onValueChange = { audioName = it },
                        label = { Text("Audio file name", color = c.textSub) },
                        leadingIcon = { Icon(Icons.Default.MusicNote, null, tint = Purple) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple, unfocusedBorderColor = c.border,
                            focusedTextColor = c.text, unfocusedTextColor = c.text),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Extract button
                    GradientButton(
                        text = if (extracted) "✓ Saved to device" else "Extract & Save Audio",
                        onClick = {
                            if (audioName.isBlank()) { vm.showToast("Audio", "Give your audio a name first", "mid"); return@GradientButton }
                            extracted = true
                            vm.showToast("Audio", "\"$audioName\" extracted & saved! ✦", "low")
                            vm.earnCoins(1, "Audio extracted")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (extracted) {
                        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF22C55E).copy(0.1f)).padding(14.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                                Text("\"${audioName.ifBlank { "audio" }}.mp3\" — ${trimEnd.toInt() - trimStart.toInt()}s — saved!",
                                    color = Color(0xFF22C55E), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  CLOES ECHO  (Feature 6) — Hidden vault
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun CloesEchoScreen(vm: AppViewModel) {
    val c = vm.themeColors
    var searchQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("Videos") }
    val tabs = listOf("Videos", "Photos", "Links", "More")
    var addingLink by remember { mutableStateOf(false) }
    var linkInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2).statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showCloesEcho = false; vm.echoUnlocked = false })
                Text(
                    "Cloes Echo 🔒",
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(listOf(Color(0xFF22C55E), Cyan)),
                        fontSize = 18.sp, fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (vm.echoUnlocked) {
                    Icon(Icons.Default.LockOpen, null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                }
            }

            // Search bar (also acts as CloesedKey entry)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { q ->
                    searchQuery = q
                    if (q == vm.cloesedKey && vm.cloesedKey.isNotBlank()) {
                        vm.echoUnlocked = true
                        searchQuery = ""
                        vm.showToast("Echo", "Vault unlocked 🔓", "low")
                    }
                },
                placeholder = { Text(if (vm.echoUnlocked) "Search by date..." else "Search or enter Cloesed Key...", color = c.textSub, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = c.textSub) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null, tint = c.textSub)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (vm.echoUnlocked) Color(0xFF22C55E) else Purple,
                    unfocusedBorderColor = c.border,
                    focusedTextColor = c.text, unfocusedTextColor = c.text
                ),
                shape = RoundedCornerShape(14.dp)
            )

            // Tabs
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEach { tab ->
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(if (activeTab == tab) Color(0xFF22C55E).copy(0.2f) else c.surface)
                            .border(1.dp, if (activeTab == tab) Color(0xFF22C55E) else c.border, RoundedCornerShape(20.dp))
                            .clickable { activeTab = tab }.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(tab, color = if (activeTab == tab) Color(0xFF22C55E) else c.textSub,
                            fontSize = 12.sp, fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            // Content
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (!vm.echoUnlocked) {
                    // Locked — always looks empty
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔒", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Nothing here", color = c.textSub, fontSize = 14.sp)
                        Text("Enter your Cloesed Key in the search bar to reveal", color = c.textSub, fontSize = 12.sp,
                            textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp, vertical = 6.dp))
                    }
                } else {
                    // Unlocked — show hidden content
                    val hasContent = when (activeTab) {
                        "Videos" -> vm.echoHiddenVideos.isNotEmpty()
                        "Photos" -> vm.echoHiddenPhotos.isNotEmpty()
                        "Links"  -> vm.echoHiddenLinks.isNotEmpty()
                        else -> false
                    }
                    if (!hasContent && !addingLink) {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔓", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Vault unlocked — $activeTab", color = c.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("No hidden $activeTab yet. Add one below.", color = c.textSub, fontSize = 12.sp)
                        }
                    } else {
                        val list = when (activeTab) {
                            "Videos" -> vm.echoHiddenVideos
                            "Photos" -> vm.echoHiddenPhotos
                            "Links"  -> vm.echoHiddenLinks
                            else -> mutableListOf()
                        }
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(list.size) { i ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                        .background(c.surface).padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        when (activeTab) { "Videos" -> Icons.Default.VideoFile; "Photos" -> Icons.Default.Photo; else -> Icons.Default.Link },
                                        null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp)
                                    )
                                    Text(list[i], color = c.text, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { list.removeAt(i) }) {
                                        Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Add button + link input
            if (vm.echoUnlocked) {
                Column(modifier = Modifier.fillMaxWidth().background(c.surface2).navigationBarsPadding()
                    .padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (addingLink && activeTab == "Links") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = linkInput, onValueChange = { linkInput = it },
                                placeholder = { Text("Paste link...", color = c.textSub, fontSize = 13.sp) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF22C55E),
                                    unfocusedBorderColor = c.border, focusedTextColor = c.text, unfocusedTextColor = c.text),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(Color(0xFF22C55E)).clickable {
                                    if (linkInput.isNotBlank()) { vm.echoHiddenLinks.add(linkInput); linkInput = ""; addingLink = false }
                                }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF22C55E).copy(0.15f))
                                .border(1.dp, Color(0xFF22C55E).copy(0.3f), RoundedCornerShape(14.dp))
                                .clickable {
                                    when (activeTab) {
                                        "Links" -> addingLink = !addingLink
                                        "Videos" -> { vm.echoHiddenVideos.add("hidden_video_${System.currentTimeMillis()}.mp4"); vm.showToast("Echo", "Video hidden ✦", "low") }
                                        "Photos" -> { vm.echoHiddenPhotos.add("hidden_photo_${System.currentTimeMillis()}.jpg"); vm.showToast("Echo", "Photo hidden ✦", "low") }
                                        else -> vm.showToast("Echo", "File hidden ✦", "low")
                                    }
                                }.padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, null, tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                                Text("Add $activeTab", color = Color(0xFF22C55E), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        // MuseEcho button
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(14.dp))
                                .background(Purple.copy(0.15f)).border(1.dp, Purple.copy(0.3f), RoundedCornerShape(14.dp))
                                .clickable { vm.showToast("MuseEcho", "Showing viewed content history ✦", "low") }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("MuseEcho", color = Purple, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SIGN UP / LOGIN SCREEN  (Feature 7)
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun SignUpScreen(vm: AppViewModel) {
    val c = vm.themeColors
    var isSignUp by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2).statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showSignupPage = false; vm.showLoginPage = false })
                Text(if (isSignUp) "Create Account" else "Sign In", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {

                Text(if (isSignUp) "Join CLOES ✦" else "Welcome back ✦",
                    style = androidx.compose.ui.text.TextStyle(brush = Brush.linearGradient(listOf(Pink, Purple)),
                        fontSize = 26.sp, fontWeight = FontWeight.Bold))
                Text(if (isSignUp) "Create your account to get started" else "Sign in to continue",
                    color = c.textSub, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))

                if (isSignUp) {
                    OutlinedTextField(value = name, onValueChange = { name = it },
                        label = { Text("Full Name", color = c.textSub) },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = Purple) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple, unfocusedBorderColor = c.border,
                            focusedTextColor = c.text, unfocusedTextColor = c.text),
                        shape = RoundedCornerShape(14.dp))
                }
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text("Email", color = c.textSub) },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = Purple) },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple, unfocusedBorderColor = c.border,
                        focusedTextColor = c.text, unfocusedTextColor = c.text),
                    shape = RoundedCornerShape(14.dp))
                OutlinedTextField(value = password, onValueChange = { password = it },
                    label = { Text("Password", color = c.textSub) },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = Purple) },
                    modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple, unfocusedBorderColor = c.border,
                        focusedTextColor = c.text, unfocusedTextColor = c.text),
                    shape = RoundedCornerShape(14.dp))

                // Google sign-in
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .border(1.dp, c.border, RoundedCornerShape(14.dp))
                        .clickable { vm.showToast("Google", "Google sign-in coming soon", "low") }.padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, null, tint = Color(0xFF4285F4), modifier = Modifier.size(20.dp))
                        Text("Continue with Google", color = c.text, fontSize = 14.sp)
                    }
                }

                GradientButton(
                    text = if (isSignUp) "Create Account" else "Sign In",
                    onClick = {
                        if (email.isBlank() || password.isBlank()) { vm.showToast("Auth", "Please fill all fields", "mid"); return@GradientButton }
                        vm.showToast("Welcome", if (isSignUp) "Account created! Welcome to CLOES ✦" else "Welcome back ✦", "low")
                        vm.showSignupPage = false; vm.showLoginPage = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Toggle sign in / sign up
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(if (isSignUp) "Already have an account? " else "Don't have an account? ", color = c.textSub, fontSize = 13.sp)
                    Text(if (isSignUp) "Sign In" else "Sign Up", color = Purple, fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.clickable { isSignUp = !isSignUp })
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  MUSE TASK  (Feature 8) — Notes & To-Dos
// ══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MuseTaskScreen(vm: AppViewModel) {
    val c = vm.themeColors
    var showEditor by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<AppViewModel.MuseNote?>(null) }
    var titleInput by remember { mutableStateOf("") }
    var bodyInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    if (showEditor) {
        Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(c.surface2).statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                        onClick = { showEditor = false; editingNote = null; titleInput = ""; bodyInput = "" })
                    Text(if (editingNote != null) "Edit Note" else "New Note", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f))
                    // Save
                    NeuIconButton(icon = { Icon(Icons.Default.Save, null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp)) },
                        onClick = {
                            if (titleInput.isBlank()) { vm.showToast("Note", "Add a title", "mid"); return@NeuIconButton }
                            val now = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                            if (editingNote != null) {
                                val idx = vm.museNotes.indexOfFirst { it.id == editingNote!!.id }
                                if (idx >= 0) vm.museNotes[idx] = editingNote!!.copy(title = titleInput, body = bodyInput)
                            } else {
                                vm.museNotes.add(0, AppViewModel.MuseNote(title = titleInput, body = bodyInput, createdAt = now))
                            }
                            showEditor = false; editingNote = null; titleInput = ""; bodyInput = ""
                            vm.showToast("Muse Task", "Note saved ✦", "low")
                        })
                    // Share
                    NeuIconButton(icon = { Icon(Icons.Default.Share, null, tint = Purple, modifier = Modifier.size(18.dp)) },
                        onClick = { vm.showToast("Share", "Sharing note...", "low") })
                }
                OutlinedTextField(value = titleInput, onValueChange = { titleInput = it },
                    placeholder = { Text("Title...", color = c.textSub, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = c.text, unfocusedTextColor = c.text),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                HorizontalDivider(color = c.border, modifier = Modifier.padding(horizontal = 16.dp))
                OutlinedTextField(value = bodyInput, onValueChange = { bodyInput = it },
                    placeholder = { Text("Start writing...", color = c.textSub, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = c.text, unfocusedTextColor = c.text),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp), maxLines = Int.MAX_VALUE)
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2).statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeuIconButton(icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showMuseTask = false })
                Text("Muse Task 📝", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                NeuIconButton(icon = { Icon(Icons.Default.Add, null, tint = Purple, modifier = Modifier.size(20.dp)) },
                    onClick = { titleInput = ""; bodyInput = ""; editingNote = null; showEditor = true })
            }

            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it },
                placeholder = { Text("Search notes...", color = c.textSub, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = c.textSub) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple, unfocusedBorderColor = c.border,
                    focusedTextColor = c.text, unfocusedTextColor = c.text),
                shape = RoundedCornerShape(14.dp))

            val filtered = vm.museNotes.filter {
                searchQuery.isBlank() || it.title.contains(searchQuery, true) || it.body.contains(searchQuery, true)
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📝", fontSize = 48.sp)
                        Text("No notes yet", color = c.textSub, fontSize = 14.sp)
                        Text("Tap + to create your first note", color = c.textSub, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered.size) { i ->
                        val note = filtered[i]
                        var showMenu by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                .background(c.surface)
                                .combinedClickable(onClick = {
                                    titleInput = note.title; bodyInput = note.body
                                    editingNote = note; showEditor = true
                                }, onLongClick = { showMenu = true })
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(note.title, color = c.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                if (note.body.isNotBlank()) Text(note.body.take(80) + if (note.body.length > 80) "..." else "",
                                    color = c.textSub, fontSize = 12.sp)
                                if (note.createdAt.isNotBlank()) Text(note.createdAt, color = c.textSub.copy(0.6f), fontSize = 10.sp)
                            }
                            if (showMenu) {
                                DropdownMenu(expanded = true, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Edit") }, onClick = {
                                        showMenu = false; titleInput = note.title; bodyInput = note.body; editingNote = note; showEditor = true })
                                    DropdownMenuItem(text = { Text("Share") }, onClick = { showMenu = false; vm.showToast("Share", "Sharing note...", "low") })
                                    DropdownMenuItem(text = { Text("Delete", color = Color(0xFFEF4444)) }, onClick = {
                                        showMenu = false; vm.museNotes.remove(note); vm.showToast("Task", "Note deleted", "low") })
                                    DropdownMenuItem(text = { Text("Details") }, onClick = {
                                        showMenu = false; vm.showToast(note.title, "Created: ${note.createdAt}", "low") })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun MuseDrawScreen(vm: AppViewModel) {
    val c = vm.themeColors
    var selectedTool by remember { mutableStateOf("pen") }
    var selectedColor by remember { mutableStateOf(Pink) }
    var strokeWidth by remember { mutableStateOf(6f) }
    var showColorPicker by remember { mutableStateOf(false) }
    var canvasCleared by remember { mutableStateOf(false) }
    val paths = remember { mutableStateListOf<DrawnPath>() }
    val redoStack = remember { mutableStateListOf<DrawnPath>() }
    var currentPath by remember { mutableStateOf<DrawnPath?>(null) }
    val scope = rememberCoroutineScope()

    val tools = listOf(
        "pen" to Icons.Default.Edit,
        "marker" to Icons.Default.BorderColor,
        "eraser" to Icons.Default.AutoFixNormal,
        "fill" to Icons.Default.FormatColorFill,
        "shape" to Icons.Default.Category,
        "text" to Icons.Default.TextFields
    )
    val palette = listOf(
        Pink, Purple, Cyan, Color(0xFFF59E0B), Color(0xFF22C55E),
        Color(0xFFEF4444), Color.White, Color.Black,
        Color(0xFF06B6D4), Color(0xFFFF6B35), Color(0xFF8B5CF6), Color(0xFF10B981)
    )

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeuIconButton(
                    icon = { Icon(Icons.Default.ArrowBack, null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
                    onClick = { vm.showMuseDraw = false }
                )
                Text(
                    "Muse Draw",
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(listOf(Pink, Purple)),
                        fontSize = 18.sp, fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
                // Undo
                NeuIconButton(
                    icon = { Icon(Icons.Default.Undo, null, tint = if (paths.isNotEmpty()) Purple else c.textSub, modifier = Modifier.size(18.dp)) },
                    onClick = { if (paths.isNotEmpty()) { redoStack.add(paths.removeLast()) } }
                )
                // Redo
                NeuIconButton(
                    icon = { Icon(Icons.Default.Redo, null, tint = if (redoStack.isNotEmpty()) Purple else c.textSub, modifier = Modifier.size(18.dp)) },
                    onClick = { if (redoStack.isNotEmpty()) { paths.add(redoStack.removeLast()) } }
                )
                // Clear
                NeuIconButton(
                    icon = { Icon(Icons.Default.DeleteSweep, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) },
                    onClick = { paths.clear(); redoStack.clear() }
                )
                // Save
                NeuIconButton(
                    icon = { Icon(Icons.Default.Save, null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp)) },
                    onClick = {
                        vm.earnCoins(2, "Muse Draw saved")
                        vm.showToast("Muse Draw", "Drawing saved! +2 coins ✦", "low")
                    }
                )
            }

            // ── Tool Bar ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tools.forEach { (tool, icon) ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTool == tool) Purple.copy(0.2f) else Color.Transparent)
                            .clickable { selectedTool = tool },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null,
                            tint = if (selectedTool == tool) Purple else c.textSub,
                            modifier = Modifier.size(20.dp))
                    }
                }
                // Color swatch
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(selectedColor)
                        .border(2.dp, if (showColorPicker) Purple else c.border, CircleShape)
                        .clickable { showColorPicker = !showColorPicker }
                )
            }

            // ── Stroke Slider ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().background(c.surface2)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.LineWeight, null, tint = c.textSub, modifier = Modifier.size(16.dp))
                Slider(
                    value = strokeWidth,
                    onValueChange = { strokeWidth = it },
                    valueRange = 2f..30f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = Purple, activeTrackColor = Purple)
                )
                Text("${strokeWidth.toInt()}px", color = c.textSub, fontSize = 11.sp)
            }

            // ── Canvas ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White)
                    .pointerInput(selectedTool, selectedColor, strokeWidth) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath = DrawnPath(
                                    color = if (selectedTool == "eraser") Color.White else selectedColor,
                                    strokeWidth = if (selectedTool == "eraser") strokeWidth * 3 else strokeWidth,
                                    points = mutableListOf(offset)
                                )
                            },
                            onDrag = { _, drag ->
                                currentPath?.points?.add(
                                    (currentPath!!.points.last() + drag)
                                )
                                currentPath = currentPath?.copy()
                            },
                            onDragEnd = {
                                currentPath?.let { paths.add(it); redoStack.clear() }
                                currentPath = null
                            }
                        )
                    }
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    (paths + listOfNotNull(currentPath)).forEach { dp ->
                        if (dp.points.size > 1) {
                            val path = Path()
                            path.moveTo(dp.points[0].x, dp.points[0].y)
                            dp.points.drop(1).forEach { pt -> path.lineTo(pt.x, pt.y) }
                            drawPath(
                                path = path,
                                color = dp.color,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = dp.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
                if (paths.isEmpty() && currentPath == null) {
                    Text(
                        "✦ Tap and drag to draw",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // ── Color Picker Row ─────────────────────────────────────────
            AnimatedVisibility(visible = showColorPicker) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().background(c.surface2).padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(palette) { col ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(if (selectedColor == col) 3.dp else 1.dp,
                                    if (selectedColor == col) Purple else c.border, CircleShape)
                                .clickable { selectedColor = col; showColorPicker = false }
                        )
                    }
                }
            }
        }
    }
}

data class DrawnPath(
    val color: Color,
    val strokeWidth: Float,
    val points: MutableList<androidx.compose.ui.geometry.Offset>
)

// ══════════════════════════════════════════════════════════════════════════════
//  MUSE CLOTHING SCREEN  (Feature 2) — choice picker
// ══════════════════════════════════════════════════════════════════════════════
