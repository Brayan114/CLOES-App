// ════════════════════════════════════════════════════════════════════════════════
//  CloesApiClient.kt  — Drop into com/cloes/app/network/
//  Retrofit + OkHttp + Socket.io integration for the CLOES backend
// ════════════════════════════════════════════════════════════════════════════════
package com.cloes.app.network

import android.content.Context
import android.net.Uri
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

// ── 1. CONFIG — Change BASE_URL to your server IP ───────────────────────────
object CloesConfig {
    // For production:
    const val BASE_URL = "https://cloes-app-production.up.railway.app"

    // For Android emulator pointing to local machine:
    // const val BASE_URL = "http://10.0.2.2:5000"

    const val API_URL    = "$BASE_URL/api"
    const val SOCKET_URL = BASE_URL
}

// ── 2. TOKEN STORE ────────────────────────────────────────────────────────────
object TokenStore {
    private var token: String = ""
    fun set(t: String) { token = t }
    fun get(): String   = token
    fun clear()         { token = "" }
    fun hasToken()      = token.isNotBlank()
}

// ── 3. HTTP CLIENT ────────────────────────────────────────────────────────────
private val httpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val req = chain.request().newBuilder().apply {
            if (TokenStore.hasToken()) header("Authorization", "Bearer ${TokenStore.get()}")
            header("Content-Type", "application/json")
        }.build()
        chain.proceed(req)
    }
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

// ── 4. BASE API CALL ──────────────────────────────────────────────────────────
suspend fun apiCall(method: String, path: String, body: JSONObject? = null): Result<JSONObject> {
    return try {
        val url = "${CloesConfig.API_URL}$path"
        val reqBody = body?.toString()?.toRequestBody("application/json".toMediaType())

        val request = Request.Builder().url(url).apply {
            when (method.uppercase()) {
                "GET"    -> get()
                "POST"   -> post(reqBody ?: "{}".toRequestBody("application/json".toMediaType()))
                "PUT"    -> put(reqBody ?: "{}".toRequestBody("application/json".toMediaType()))
                "PATCH"  -> patch(reqBody ?: "{}".toRequestBody("application/json".toMediaType()))
                "DELETE" -> delete(reqBody)
            }
        }.build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: "{}"
        if (response.isSuccessful) {
            Result.success(JSONObject(responseBody))
        } else {
            val err = try { JSONObject(responseBody).optString("error", "Request failed") } catch (e: Exception) { "Request failed" }
            Result.failure(IOException("$err (${response.code})"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// ── 5. AUTH API ───────────────────────────────────────────────────────────────
object AuthApi {
    suspend fun register(name: String, email: String, password: String, paletteColors: List<String>, lightSeed: Float): Result<JSONObject> {
        val body = JSONObject().apply {
            put("name", name); put("email", email); put("password", password)
            put("paletteColors", JSONArray(paletteColors)); put("lightSeed", lightSeed)
        }
        return apiCall("POST", "/auth/register", body).also { result ->
            result.getOrNull()?.optString("token")?.let { TokenStore.set(it) }
        }
    }

    suspend fun login(email: String, password: String): Result<JSONObject> {
        val body = JSONObject().apply { put("email", email); put("password", password) }
        return apiCall("POST", "/auth/login", body).also { result ->
            result.getOrNull()?.optString("token")?.let { TokenStore.set(it) }
        }
    }

    // Google OAuth — open this URL in a Chrome Custom Tab / WebView
    fun googleOAuthUrl() = "${CloesConfig.API_URL}/auth/google"

    // After Google redirect, token comes via deep link: cloes://auth?token=...
    fun handleDeepLink(token: String) { TokenStore.set(token) }

    suspend fun getMe(): Result<JSONObject> = apiCall("GET", "/auth/me")

    suspend fun logout(): Result<JSONObject> {
        val result = apiCall("POST", "/auth/logout")
        TokenStore.clear()
        return result
    }

    suspend fun completeOnboarding(name: String, handle: String, paletteColors: List<String>, lightSeed: Float): Result<JSONObject> {
        val body = JSONObject().apply {
            put("name", name); put("handle", handle)
            put("paletteColors", JSONArray(paletteColors)); put("lightSeed", lightSeed)
        }
        return apiCall("POST", "/auth/onboard", body)
    }
}

// ── 6. USERS / PROFILE API ────────────────────────────────────────────────────
object UserApi {
    suspend fun updateProfile(updates: Map<String, Any>): Result<JSONObject> {
        val body = JSONObject(updates)
        return apiCall("PATCH", "/users/me", body)
    }

    suspend fun uploadAvatar(context: Context, uri: Uri): Result<JSONObject> {
        return try {
            val file = uriToFile(context, uri) ?: return Result.failure(IOException("Cannot read file"))
            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("avatar", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
                .build()
            val request = Request.Builder()
                .url("${CloesConfig.API_URL}/users/me/avatar")
                .post(body)
                .header("Authorization", "Bearer ${TokenStore.get()}")
                .build()
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: "{}"
            if (response.isSuccessful) Result.success(JSONObject(responseBody))
            else Result.failure(IOException("Upload failed: ${response.code}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePalette(colors: List<String>, lightSeed: Float): Result<JSONObject> {
        val body = JSONObject().apply {
            put("paletteColors", JSONArray(colors)); put("lightSeed", lightSeed)
        }
        return apiCall("POST", "/users/me/palette", body)
    }

    suspend fun getQrCode(): Result<JSONObject> = apiCall("GET", "/users/me/qr")

    suspend fun setCloesedKey(key: String): Result<JSONObject> {
        return apiCall("POST", "/users/me/cloesed-key", JSONObject().put("key", key))
    }

    suspend fun verifyCloesedKey(key: String): Result<JSONObject> {
        return apiCall("POST", "/users/me/verify-cloesed-key", JSONObject().put("key", key))
    }

    suspend fun updateEmergencyContacts(contacts: List<Map<String, String>>): Result<JSONObject> {
        val arr = JSONArray(contacts.map { JSONObject(it) })
        return apiCall("PUT", "/emergency/contacts", JSONObject().put("contacts", arr))
    }

    suspend fun searchUsers(query: String): Result<JSONObject> = apiCall("GET", "/users/search/$query")

    suspend fun updateFcmToken(token: String): Result<JSONObject> {
        return apiCall("POST", "/users/me/fcm-token", JSONObject().put("fcmToken", token))
    }
}

// ── 7. MESSAGES API ───────────────────────────────────────────────────────────
object MessageApi {
    suspend fun getConversations(): Result<JSONObject> = apiCall("GET", "/messages/conversations")

    suspend fun getOrCreateDirect(userId: String): Result<JSONObject> {
        return apiCall("POST", "/messages/conversations/direct", JSONObject().put("userId", userId))
    }

    suspend fun getMessages(conversationId: String, page: Int = 1): Result<JSONObject> =
        apiCall("GET", "/messages/conversations/$conversationId/messages?page=$page")

    suspend fun sendText(conversationId: String, text: String, replyTo: String? = null): Result<JSONObject> {
        val body = JSONObject().apply {
            put("text", text)
            replyTo?.let { put("replyTo", it) }
        }
        return apiCall("POST", "/messages/conversations/$conversationId/messages", body)
    }

    suspend fun sendMedia(context: Context, conversationId: String, uri: Uri, caption: String = ""): Result<JSONObject> {
        return try {
            val file = uriToFile(context, uri) ?: return Result.failure(IOException("Cannot read file"))
            val mime = getMimeType(context, uri)
            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("media", file.name, file.asRequestBody(mime.toMediaTypeOrNull()))
                .addFormDataPart("caption", caption)
                .build()
            val request = Request.Builder()
                .url("${CloesConfig.API_URL}/messages/conversations/$conversationId/media")
                .post(body)
                .header("Authorization", "Bearer ${TokenStore.get()}")
                .build()
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: "{}"
            if (response.isSuccessful) Result.success(JSONObject(responseBody))
            else Result.failure(IOException("Upload failed: ${response.code}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendDocument(context: Context, conversationId: String, uri: Uri): Result<JSONObject> {
        return try {
            val file = uriToFile(context, uri) ?: return Result.failure(IOException("Cannot read file"))
            val mime = getMimeType(context, uri)
            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("document", file.name, file.asRequestBody(mime.toMediaTypeOrNull()))
                .build()
            val request = Request.Builder()
                .url("${CloesConfig.API_URL}/messages/conversations/$conversationId/document")
                .post(body)
                .header("Authorization", "Bearer ${TokenStore.get()}")
                .build()
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: "{}"
            if (response.isSuccessful) Result.success(JSONObject(responseBody))
            else Result.failure(IOException("Upload failed: ${response.code}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendLink(conversationId: String, url: String, title: String = ""): Result<JSONObject> {
        val body = JSONObject().apply { put("url", url); put("preview", JSONObject().put("title", title)) }
        return apiCall("POST", "/messages/conversations/$conversationId/link", body)
    }

    suspend fun reactToMessage(messageId: String, emoji: String): Result<JSONObject> {
        return apiCall("POST", "/messages/messages/$messageId/react", JSONObject().put("emoji", emoji))
    }

    suspend fun deleteForMe(messageId: String): Result<JSONObject> =
        apiCall("DELETE", "/messages/messages/$messageId/for-me")

    suspend fun deleteForAll(messageId: String): Result<JSONObject> =
        apiCall("DELETE", "/messages/messages/$messageId/for-all")

    suspend fun editMessage(messageId: String, text: String): Result<JSONObject> {
        return apiCall("PATCH", "/messages/messages/$messageId", JSONObject().put("text", text))
    }

    suspend fun setWallpaper(conversationId: String, wallpaperUrl: String): Result<JSONObject> {
        return apiCall("PATCH", "/messages/conversations/$conversationId/wallpaper",
            JSONObject().put("wallpaperUrl", wallpaperUrl))
    }

    suspend fun uploadAndSetWallpaper(context: Context, conversationId: String, uri: Uri): Result<JSONObject> {
        return try {
            val file = uriToFile(context, uri) ?: return Result.failure(IOException("Cannot read file"))
            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("wallpaper", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
                .build()
            val request = Request.Builder()
                .url("${CloesConfig.API_URL}/messages/conversations/$conversationId/wallpaper-upload")
                .post(body)
                .header("Authorization", "Bearer ${TokenStore.get()}")
                .build()
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: "{}"
            if (response.isSuccessful) Result.success(JSONObject(responseBody))
            else Result.failure(IOException("Upload failed: ${response.code}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setDisappear(conversationId: String, seconds: Int): Result<JSONObject> {
        return apiCall("PATCH", "/messages/conversations/$conversationId/disappear",
            JSONObject().put("seconds", seconds))
    }

    suspend fun lockConversation(conversationId: String, locked: Boolean): Result<JSONObject> {
        return apiCall("PATCH", "/messages/conversations/$conversationId/lock",
            JSONObject().put("locked", locked))
    }
}

// ── 8. VIBES API ─────────────────────────────────────────────────────────────
object VibeApi {
    suspend fun getFeed(page: Int = 1, category: String? = null): Result<JSONObject> {
        val query = "?page=$page${category?.let { "&category=$it" } ?: ""}"
        return apiCall("GET", "/vibes$query")
    }

    suspend fun uploadVibe(context: Context, uri: Uri, title: String, category: String): Result<JSONObject> {
        return try {
            val file = uriToFile(context, uri) ?: return Result.failure(IOException("Cannot read file"))
            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("vibe", file.name, file.asRequestBody("video/*".toMediaTypeOrNull()))
                .addFormDataPart("title", title)
                .addFormDataPart("category", category)
                .build()
            val request = Request.Builder()
                .url("${CloesConfig.API_URL}/vibes/upload")
                .post(body)
                .header("Authorization", "Bearer ${TokenStore.get()}")
                .build()
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: "{}"
            if (response.isSuccessful) Result.success(JSONObject(responseBody))
            else Result.failure(IOException("Upload failed: ${response.code}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun like(vibeId: String): Result<JSONObject> = apiCall("POST", "/vibes/$vibeId/like")
    suspend fun comment(vibeId: String, text: String): Result<JSONObject> =
        apiCall("POST", "/vibes/$vibeId/comments", JSONObject().put("text", text))
    suspend fun getVibe(vibeId: String): Result<JSONObject> = apiCall("GET", "/vibes/$vibeId")
}

// ── 9. COINS API ─────────────────────────────────────────────────────────────
object CoinApi {
    suspend fun getBalance(): Result<JSONObject> = apiCall("GET", "/coins")
    suspend fun heartbeat(): Result<JSONObject>  = apiCall("POST", "/coins/heartbeat")
    suspend fun spendForBrowsing(coins: Int): Result<JSONObject> =
        apiCall("POST", "/coins/spend-browsing", JSONObject().put("coins", coins))
}

// ── 10. QR API ────────────────────────────────────────────────────────────────
object QrApi {
    suspend fun getMyQr(): Result<JSONObject>       = apiCall("GET", "/qr/me")
    suspend fun scanQr(data: String): Result<JSONObject> =
        apiCall("POST", "/qr/scan", JSONObject().put("data", data))
}

// ── 11. EMERGENCY API ────────────────────────────────────────────────────────
object EmergencyApi {
    suspend fun trigger(message: String? = null, lat: Double? = null, lng: Double? = null): Result<JSONObject> {
        val body = JSONObject().apply {
            message?.let { put("message", it) }
            if (lat != null && lng != null) put("location", JSONObject().put("lat", lat).put("lng", lng))
        }
        return apiCall("POST", "/emergency/trigger", body)
    }
    suspend fun getContacts(): Result<JSONObject> = apiCall("GET", "/emergency/contacts")
}

// ── 12. BLOOM API ────────────────────────────────────────────────────────────
object BloomApi {
    suspend fun getAll(): Result<JSONObject>         = apiCall("GET", "/bloom")
    suspend fun getTop(): Result<JSONObject>         = apiCall("GET", "/bloom/top")
    suspend fun getUrgencyStatus(): Result<JSONObject> = apiCall("GET", "/bloom/urgency-status")
    suspend fun getForConversation(id: String): Result<JSONObject> =
        apiCall("GET", "/bloom/conversation/$id")
}

// ── 13. MUSE API ─────────────────────────────────────────────────────────────
object MuseApi {
    suspend fun saveMuseSession(type: String, preview: String, messages: List<Map<String, String>>): Result<JSONObject> {
        val body = JSONObject().apply {
            put("type", type); put("preview", preview)
            put("messages", JSONArray(messages.map { JSONObject(it) }))
        }
        return apiCall("POST", "/muse/sessions", body)
    }
    suspend fun getHistory(type: String? = null): Result<JSONObject> {
        val query = type?.let { "?type=$it" } ?: ""
        return apiCall("GET", "/muse/sessions$query")
    }
    suspend fun getDressUsage(): Result<JSONObject> = apiCall("GET", "/muse/dress/usage")
}

// ── 14. MEDIA API ────────────────────────────────────────────────────────────
object MediaApi {
    suspend fun uploadMedia(context: Context, uri: Uri): Result<JSONObject> {
        return try {
            val file = uriToFile(context, uri) ?: return Result.failure(IOException("Cannot read file"))
            val mime = getMimeType(context, uri)
            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, file.asRequestBody(mime.toMediaTypeOrNull()))
                .build()
            val request = Request.Builder()
                .url("${CloesConfig.API_URL}/media/upload")
                .post(body)
                .header("Authorization", "Bearer ${TokenStore.get()}")
                .build()
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: "{}"
            if (response.isSuccessful) Result.success(JSONObject(responseBody))
            else Result.failure(IOException("Upload failed: ${response.code}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveToGallery(mediaUrl: String, fileName: String): Result<JSONObject> {
        val body = JSONObject().apply { put("mediaUrl", mediaUrl); put("fileName", fileName) }
        return apiCall("POST", "/media/save-to-gallery", body)
    }

    suspend fun getLinkPreview(url: String): Result<JSONObject> {
        return apiCall("POST", "/media/link-preview", JSONObject().put("url", url))
    }

    suspend fun exportChatHistory(conversationId: String): String {
        return "${CloesConfig.API_URL}/media/chat-export/$conversationId"
    }
}

// ── 15. SOCKET.IO CLIENT ─────────────────────────────────────────────────────
object CloesSocket {
    private var socket: Socket? = null

    // Coin flow — observe from ViewModel
    val coinBalance = MutableStateFlow(0)
    val onlineStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val incomingCall = MutableStateFlow<JSONObject?>(null)
    val newMessages = MutableStateFlow<JSONObject?>(null)
    val typingStatus = MutableStateFlow<Pair<String, Boolean>?>(null)
    val emergencyAlert = MutableStateFlow<JSONObject?>(null)

    fun connect() {
        if (socket?.connected() == true) return
        val opts = IO.Options.builder()
            .setAuth(mapOf("token" to TokenStore.get()))
            .build()
        socket = IO.socket(CloesConfig.SOCKET_URL, opts)
        socket?.apply {
            on(Socket.EVENT_CONNECT)          { Log.d("CLOES", "Socket connected") }
            on(Socket.EVENT_DISCONNECT)        { Log.d("CLOES", "Socket disconnected") }
            on(Socket.EVENT_CONNECT_ERROR)     { Log.e("CLOES", "Socket error: $it") }

            on("coin_update")                  { args -> coinBalance.tryEmit((args[0] as? JSONObject)?.optInt("balance") ?: 0) }
            on("user_online")                  { args ->
                val obj = args[0] as? JSONObject ?: return@on
                val map = onlineStatus.value.toMutableMap()
                map[obj.optString("userId")] = obj.optBoolean("online")
                onlineStatus.tryEmit(map)
            }
            on("new_message")                  { args -> newMessages.tryEmit(args[0] as? JSONObject) }
            on("typing_start")                 { args ->
                val obj = args[0] as? JSONObject ?: return@on
                typingStatus.tryEmit(Pair(obj.optString("conversationId"), true))
            }
            on("typing_stop")                  { args ->
                val obj = args[0] as? JSONObject ?: return@on
                typingStatus.tryEmit(Pair(obj.optString("conversationId"), false))
            }
            on("incoming_call")                { args -> incomingCall.tryEmit(args[0] as? JSONObject) }
            on("emergency_alert")              { args -> emergencyAlert.tryEmit(args[0] as? JSONObject) }
        }
        socket?.connect()
    }

    fun disconnect() { socket?.disconnect(); socket = null }

    fun joinConversation(id: String)  { socket?.emit("join_conversation", id) }
    fun leaveConversation(id: String) { socket?.emit("leave_conversation", id) }
    fun typingStart(conversationId: String) { socket?.emit("typing_start", JSONObject().put("conversationId", conversationId)) }
    fun typingStop(conversationId: String)  { socket?.emit("typing_stop",  JSONObject().put("conversationId", conversationId)) }
    fun markRead(conversationId: String)    { socket?.emit("messages_read", JSONObject().put("conversationId", conversationId)) }
    fun heartbeat() { socket?.emit("session_heartbeat") }
    fun triggerEmergency(message: String, lat: Double?, lng: Double?) {
        val payload = JSONObject().apply {
            put("message", message)
            if (lat != null && lng != null) put("location", JSONObject().put("lat", lat).put("lng", lng))
        }
        socket?.emit("emergency_sos", payload)
    }
    fun sendCallOffer(targetUserId: String, offer: String, callId: String, callType: String) {
        val payload = JSONObject().apply {
            put("targetUserId", targetUserId); put("offer", offer)
            put("callId", callId); put("callType", callType)
        }
        socket?.emit("call_offer", payload)
    }
    fun sendCallAnswer(callerId: String, answer: String, callId: String) {
        socket?.emit("call_answer", JSONObject().apply {
            put("callerId", callerId); put("answer", answer); put("callId", callId)
        })
    }
    fun sendIceCandidate(targetUserId: String, candidate: String, callId: String) {
        socket?.emit("call_ice_candidate", JSONObject().apply {
            put("targetUserId", targetUserId); put("candidate", candidate); put("callId", callId)
        })
    }

    fun on(event: String, callback: (Array<Any>) -> Unit) { socket?.on(event, callback) }
}

// ── 16. HELPERS ───────────────────────────────────────────────────────────────
private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val ext = context.contentResolver.getType(uri)?.let { mime ->
            when { mime.contains("jpeg") || mime.contains("jpg") -> ".jpg"
                   mime.contains("png")  -> ".png"
                   mime.contains("mp4")  -> ".mp4"
                   mime.contains("pdf")  -> ".pdf"
                   mime.contains("mp3")  -> ".mp3"
                   else -> ""
            }
        } ?: ""
        val file = File(context.cacheDir, "cloes_upload_${System.currentTimeMillis()}$ext")
        file.outputStream().use { out -> inputStream.copyTo(out) }
        file
    } catch (e: Exception) {
        Log.e("CLOES", "uriToFile error: ${e.message}")
        null
    }
}

private fun getMimeType(context: Context, uri: Uri): String {
    return context.contentResolver.getType(uri) ?: "application/octet-stream"
}
