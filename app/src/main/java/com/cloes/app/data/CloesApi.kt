package com.cloes.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object CloesApi {
    const val BASE_URL = "https://cloes-app-production.up.railway.app"
    private val client = OkHttpClient()
    private val JSON_TYPE = "application/json; charset=utf-8".toMediaType()

    // ── Token persistence ────────────────────────────────────────────────────
    private const val PREFS_NAME = "cloes_auth"
    private const val KEY_TOKEN = "jwt_token"

    fun saveToken(ctx: Context, token: String) {
        prefs(ctx).edit().putString(KEY_TOKEN, token).apply()
    }
    fun getToken(ctx: Context): String? = prefs(ctx).getString(KEY_TOKEN, null)
    fun clearToken(ctx: Context) { prefs(ctx).edit().remove(KEY_TOKEN).apply() }
    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Auth Endpoints ───────────────────────────────────────────────────────

    data class AuthResult(
        val success: Boolean,
        val token: String? = null,
        val userName: String? = null,
        val userHandle: String? = null,
        val onboarded: Boolean = false,
        val error: String? = null
    )

    suspend fun register(name: String, email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val body = JSONObject().apply {
                    put("name", name)
                    put("email", email)
                    put("password", password)
                }
                val req = Request.Builder()
                    .url("$BASE_URL/api/auth/register")
                    .post(body.toString().toRequestBody(JSON_TYPE))
                    .build()
                val resp = client.newCall(req).execute()
                val json = JSONObject(resp.body?.string() ?: "{}")
                if (resp.isSuccessful) {
                    val user = json.optJSONObject("user")
                    AuthResult(
                        success = true,
                        token = json.optString("token"),
                        userName = user?.optString("name"),
                        userHandle = user?.optString("handle"),
                        onboarded = user?.optBoolean("onboardingDone", false) ?: false
                    )
                } else {
                    AuthResult(success = false, error = json.optString("error", "Registration failed"))
                }
            } catch (e: Exception) {
                AuthResult(success = false, error = e.message ?: "Network error")
            }
        }

    suspend fun login(email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val body = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }
                val req = Request.Builder()
                    .url("$BASE_URL/api/auth/login")
                    .post(body.toString().toRequestBody(JSON_TYPE))
                    .build()
                val resp = client.newCall(req).execute()
                val json = JSONObject(resp.body?.string() ?: "{}")
                if (resp.isSuccessful) {
                    val user = json.optJSONObject("user")
                    AuthResult(
                        success = true,
                        token = json.optString("token"),
                        userName = user?.optString("name"),
                        userHandle = user?.optString("handle"),
                        onboarded = user?.optBoolean("onboardingDone", false) ?: false
                    )
                } else {
                    AuthResult(success = false, error = json.optString("error", "Login failed"))
                }
            } catch (e: Exception) {
                AuthResult(success = false, error = e.message ?: "Network error")
            }
        }

    suspend fun fetchMe(token: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("$BASE_URL/api/auth/me")
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                val resp = client.newCall(req).execute()
                val json = JSONObject(resp.body?.string() ?: "{}")
                if (resp.isSuccessful) {
                    val user = json.optJSONObject("user")
                    AuthResult(
                        success = true,
                        token = token,
                        userName = user?.optString("name"),
                        userHandle = user?.optString("handle"),
                        onboarded = user?.optBoolean("onboardingDone", false) ?: false
                    )
                } else {
                    AuthResult(success = false, error = "Session expired")
                }
            } catch (e: Exception) {
                AuthResult(success = false, error = e.message ?: "Network error")
            }
        }

    suspend fun completeOnboard(token: String, name: String, handle: String, paletteColors: List<String>): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val body = JSONObject().apply {
                    put("name", name)
                    put("handle", handle)
                    val arr = org.json.JSONArray()
                    paletteColors.forEach { arr.put(it) }
                    put("paletteColors", arr)
                }
                val req = Request.Builder()
                    .url("$BASE_URL/api/auth/onboard")
                    .addHeader("Authorization", "Bearer $token")
                    .post(body.toString().toRequestBody(JSON_TYPE))
                    .build()
                val resp = client.newCall(req).execute()
                val json = JSONObject(resp.body?.string() ?: "{}")
                if (resp.isSuccessful) {
                    val user = json.optJSONObject("user")
                    AuthResult(
                        success = true,
                        token = token,
                        userName = user?.optString("name"),
                        userHandle = user?.optString("handle"),
                        onboarded = true
                    )
                } else {
                    AuthResult(success = false, error = json.optString("error", "Onboard failed"))
                }
            } catch (e: Exception) {
                AuthResult(success = false, error = e.message ?: "Network error")
            }
        }

    suspend fun logout(token: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("$BASE_URL/api/auth/logout")
                    .addHeader("Authorization", "Bearer $token")
                    .post("{}".toRequestBody(JSON_TYPE))
                    .build()
                client.newCall(req).execute().isSuccessful
            } catch (_: Exception) { false }
        }

    suspend fun deleteAccount(token: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("$BASE_URL/api/auth/account")
                    .addHeader("Authorization", "Bearer $token")
                    .delete()
                    .build()
                client.newCall(req).execute().isSuccessful
            } catch (_: Exception) { false }
        }

    // ── Contacts & Conversations ────────────────────────────────────────────────
 
    data class ConversationDto(
        val _id: String,
        val type: String,
        val participants: List<UserDto>,
        val lastMessageAt: String? = null,
        val lastMessageText: String? = null,
        val lastMessageSender: String? = null,
        val lastMessageId: String? = null
    )

    data class MessageDto(
        val _id: String,
        val senderId: String,
        val text: String,
        val type: String,
        val createdAt: String
    )

    data class UserDto(
        val _id: String,
        val name: String,
        val handle: String,
        val avatar: String,
        val paletteColors: List<String>,
        val lightSeed: Double,
        val online: Boolean,
        val lastSeen: String
    )

    suspend fun searchUser(token: String, handle: String): UserDto? =
        withContext(Dispatchers.IO) {
            try {
                val cleanHandle = handle.removePrefix("@")
                val req = Request.Builder()
                    .url("$BASE_URL/api/contacts/search?q=$cleanHandle")
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                val resp = client.newCall(req).execute()
                if (resp.isSuccessful) {
                    val arr = org.json.JSONArray(resp.body?.string() ?: "[]")
                    val targetHandle = handle.removePrefix("@")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val h = obj.optString("handle")
                        if (h.equals(targetHandle, ignoreCase = true)) {
                            val palArr = obj.optJSONArray("paletteColors")
                            val colors = mutableListOf<String>()
                            if (palArr != null) {
                                for (j in 0 until palArr.length()) colors.add(palArr.getString(j))
                            }
                            return@withContext UserDto(
                                _id = obj.getString("_id"),
                                name = obj.getString("name"),
                                handle = h,
                                avatar = obj.optString("avatar"),
                                paletteColors = colors,
                                lightSeed = obj.optDouble("lightSeed", 0.42),
                                online = obj.optBoolean("online", false),
                                lastSeen = obj.optString("lastSeen")
                            )
                        }
                    }
                }
                null
            } catch (e: Exception) { null }
        }

    suspend fun createDirectConversation(token: String, userId: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val body = JSONObject().apply { put("userId", userId) }
                val req = Request.Builder()
                    .url("$BASE_URL/api/messages/conversations/direct")
                    .addHeader("Authorization", "Bearer $token")
                    .post(body.toString().toRequestBody(JSON_TYPE))
                    .build()
                val resp = client.newCall(req).execute()
                if (resp.isSuccessful) {
                    val json = JSONObject(resp.body?.string() ?: "{}")
                    json.optString("_id")
                } else null
            } catch (e: Exception) { null }
        }

    suspend fun fetchConversations(token: String): List<ConversationDto> =
        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("$BASE_URL/api/messages/conversations")
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                val resp = client.newCall(req).execute()
                if (resp.isSuccessful) {
                    val arr = org.json.JSONArray(resp.body?.string() ?: "[]")
                    val list = mutableListOf<ConversationDto>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val partArr = obj.getJSONArray("participants")
                        val lastMsgObj = obj.optJSONObject("lastMessage")
                        val participants = mutableListOf<UserDto>()
                        for (j in 0 until partArr.length()) {
                            val p = partArr.getJSONObject(j)
                            participants.add(UserDto(
                                _id = p.getString("_id"),
                                name = p.getString("name"),
                                handle = p.getString("handle"),
                                avatar = p.optString("avatar", ""),
                                paletteColors = listOf(), // simplified
                                lightSeed = p.optDouble("lightSeed", 0.42),
                                online = p.optBoolean("online", false),
                                lastSeen = p.optString("lastSeen", "")
                            ))
                        }
                        list.add(ConversationDto(
                            _id = obj.getString("_id"),
                            type = obj.getString("type"),
                            participants = participants,
                            lastMessageAt = obj.optString("lastMessageAt"),
                            lastMessageText = lastMsgObj?.optString("text"),
                            lastMessageSender = lastMsgObj?.optString("sender"),
                            lastMessageId = lastMsgObj?.optString("_id")
                        ))
                    }
                    list
                } else emptyList()
            } catch (e: Exception) { emptyList() }
        }

    suspend fun fetchMessages(token: String, convoId: String): List<MessageDto> =
        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("$BASE_URL/api/messages/conversations/$convoId/messages")
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                val resp = client.newCall(req).execute()
                if (resp.isSuccessful) {
                    val arr = org.json.JSONArray(resp.body?.string() ?: "[]")
                    val list = mutableListOf<MessageDto>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val senderObj = obj.optJSONObject("sender")
                        val senderId = senderObj?.optString("handle") ?: obj.optString("sender")
                        list.add(MessageDto(
                            _id = obj.getString("_id"),
                            senderId = senderId,
                            text = obj.getString("text"),
                            type = obj.optString("type", "text"),
                            createdAt = obj.optString("createdAt")
                        ))
                    }
                    list
                } else emptyList()
            } catch (e: Exception) { emptyList() }
        }

    suspend fun sendMessage(token: String, convoId: String, text: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val body = JSONObject().apply { put("text", text) }
                val req = Request.Builder()
                    .url("$BASE_URL/api/messages/conversations/$convoId/messages")
                    .addHeader("Authorization", "Bearer $token")
                    .post(body.toString().toRequestBody(JSON_TYPE))
                    .build()
                val resp = client.newCall(req).execute()
                resp.isSuccessful
            } catch (e: Exception) { false }
        }

    suspend fun chatWithMuse(token: String, message: String, history: List<Pair<Boolean, String>>): String? =
        withContext(Dispatchers.IO) {
            try {
                val historyArr = org.json.JSONArray()
                history.takeLast(20).forEach { (isUser, text) ->
                    val obj = JSONObject()
                    obj.put("role", if (isUser) "user" else "muse")
                    obj.put("text", text)
                    historyArr.put(obj)
                }
                
                val body = JSONObject().apply {
                    put("message", message)
                    put("history", historyArr)
                }
                val req = Request.Builder()
                    .url("$BASE_URL/api/muse/chat")
                    .addHeader("Authorization", "Bearer $token")
                    .post(body.toString().toRequestBody(JSON_TYPE))
                    .build()
                val resp = client.newCall(req).execute()
                if (resp.isSuccessful) {
                    val json = JSONObject(resp.body?.string() ?: "{}")
                    json.optString("reply", null)
                } else null
            } catch (e: Exception) { null }
        }

    /** Build the Google OAuth URL for the WebView */
    fun googleAuthUrl(): String = "$BASE_URL/api/auth/google"
}
