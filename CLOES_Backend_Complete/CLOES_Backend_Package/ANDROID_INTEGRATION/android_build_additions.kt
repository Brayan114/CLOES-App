// ════════════════════════════════════════════════════════════════════════════════
//  ADD THESE TO YOUR app/build.gradle  (inside the dependencies {} block)
// ════════════════════════════════════════════════════════════════════════════════

/*
dependencies {
    // ...existing dependencies...

    // ── Networking ────────────────────────────────────────────────────────────
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

    // ── Socket.io (real-time) ─────────────────────────────────────────────────
    implementation('io.socket:socket.io-client:2.1.0') {
        exclude group: 'org.json', module: 'json'
    }

    // ── Coroutines ────────────────────────────────────────────────────────────
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'

    // ── Image loading (already present — confirm) ─────────────────────────────
    implementation 'io.coil-kt:coil-compose:2.5.0'

    // ── Google Auth (for Google Sign-In button) ───────────────────────────────
    implementation 'com.google.android.gms:play-services-auth:21.0.0'

    // ── Firebase Messaging (push notifications) ───────────────────────────────
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-messaging-ktx'
}
*/

// ════════════════════════════════════════════════════════════════════════════════
//  ADD THIS TO AndroidManifest.xml  (inside <application>)
// ════════════════════════════════════════════════════════════════════════════════

/*
<!-- Deep link handler for Google OAuth callback -->
<activity android:name=".MainActivity" ...>
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="cloes" android:host="auth" />
    </intent-filter>
</activity>
*/

// ════════════════════════════════════════════════════════════════════════════════
//  HOW TO USE IN YOUR ViewModel / Composable
// ════════════════════════════════════════════════════════════════════════════════

/*
// ── LOGIN ─────────────────────────────────────────────────────────────────────
viewModelScope.launch {
    val result = AuthApi.login("user@email.com", "password123")
    result.onSuccess { json ->
        val token = json.getString("token")
        // TokenStore already saves it automatically
        // now connect socket:
        CloesSocket.connect()
    }
    result.onFailure { error -> /* show error */ }
}

// ── SEND MESSAGE ──────────────────────────────────────────────────────────────
viewModelScope.launch {
    MessageApi.sendText(conversationId, "Hello ✦")
    // Real-time: new_message event arrives via CloesSocket
}

// ── UPLOAD IMAGE (from gallery) ───────────────────────────────────────────────
// In your Composable:
val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    uri?.let {
        viewModelScope.launch {
            MediaApi.uploadMedia(context, it).onSuccess { json ->
                val url = json.getString("url")
                // Use url in message or Muse Dress
            }
        }
    }
}
// Trigger:
launcher.launch("image/*")   // images only
launcher.launch("*\/*")      // all files

// ── UPLOAD VIDEO TO VIBES ─────────────────────────────────────────────────────
val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    uri?.let {
        viewModelScope.launch {
            VibeApi.uploadVibe(context, it, "My Vibe ✦", "MOOD").onSuccess { json ->
                // vibe uploaded, json has the vibe object
            }
        }
    }
}
videoLauncher.launch("video/*")

// ── COIN HEARTBEAT (call every 10 mins while app is active) ───────────────────
// In your MainActivity or ViewModel init:
viewModelScope.launch {
    while (isActive) {
        delay(10 * 60 * 1000L)  // 10 minutes
        CloesSocket.heartbeat()  // via socket (instant)
        // OR: CoinApi.heartbeat()  // via REST
    }
}

// ── LISTEN FOR REAL-TIME COINS ────────────────────────────────────────────────
val coinBalance by CloesSocket.coinBalance.collectAsState()
// coinBalance updates whenever server awards coins

// ── EMERGENCY TRIGGER ─────────────────────────────────────────────────────────
CloesSocket.triggerEmergency("I need help!", lat, lng)
// OR:
viewModelScope.launch { EmergencyApi.trigger("I need help!", lat, lng) }

// ── INCOMING CALLS ────────────────────────────────────────────────────────────
val incomingCall by CloesSocket.incomingCall.collectAsState()
incomingCall?.let { call ->
    val callerId   = call.optString("caller")
    val callType   = call.optString("callType")  // "voice" or "video"
    val iceServers = call.optJSONArray("iceServers")
    // Show incoming call UI
}

// ── TYPING INDICATOR ─────────────────────────────────────────────────────────
// Start typing:
CloesSocket.typingStart(conversationId)
// Stop typing:
CloesSocket.typingStop(conversationId)

// Observe:
val typing by CloesSocket.typingStatus.collectAsState()

// ── GOOGLE SIGN IN ────────────────────────────────────────────────────────────
// Open Chrome Custom Tab:
val url = AuthApi.googleOAuthUrl()
// After redirect to cloes://auth?token=...
// In MainActivity.onNewIntent:
// intent.data?.getQueryParameter("token")?.let { AuthApi.handleDeepLink(it) }
*/
