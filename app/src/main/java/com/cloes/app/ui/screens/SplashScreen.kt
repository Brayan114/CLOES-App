package com.cloes.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cloes.app.ui.components.*
import com.cloes.app.ui.theme.*
import com.cloes.app.viewmodel.AppViewModel

@Composable
fun SplashScreen(vm: AppViewModel) {
    val c = vm.themeColors
    val ctx = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )
    val rotate by infiniteTransition.animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rotate"
    )

    // Auto-check saved session on first launch
    LaunchedEffect(Unit) {
        if (!vm.hasCheckedSession) {
            vm.checkSavedSession(ctx)
        }
    }

    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        AuroraBackground(theme = vm.appTheme, modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(150.dp).offset(y = floatY.dp).rotate(rotate)
                    .clip(RoundedCornerShape(34.dp))
                    .shadow(elevation = 22.dp, shape = RoundedCornerShape(34.dp))
                    .background(Color.White)
            ) {
                FragmentArt(palette = listOf(Pink, Purple, Cyan), seed = 0.55f, animating = true)
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text("CLOES",
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.linearGradient(listOf(Pink, Purple, Cyan)),
                    fontSize = 60.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp
                )
            )
            Text("✦ PRIVATE · VIVID · YOURS", color = c.textSub, fontSize = 9.5.sp,
                letterSpacing = 2.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp, bottom = 36.dp))

            if (vm.isAuthLoading) {
                CircularProgressIndicator(color = Purple, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Checking session…", color = c.textSub, fontSize = 12.sp)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(11.dp),
                    modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth()) {
                    GradientButton(
                        text = "Create Account ✦",
                        onClick = { vm.showSignupPage = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, Purple, RoundedCornerShape(16.dp))
                            .clickable { vm.showLoginPage = true }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sign In", color = Purple, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("No phone number, no ads, no trace.",
                        color = c.textSub, fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp))
                }
            }
        }
    }
}

