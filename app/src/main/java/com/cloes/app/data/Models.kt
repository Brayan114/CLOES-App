package com.cloes.app.data

import androidx.compose.ui.graphics.Color

// ─── Message ──────────────────────────────────────────────────────────────────
data class Message(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val sent: Boolean,
    val timestamp: String,
    val type: MsgType = MsgType.Text,
    val reactionEmoji: String? = null,
    val expiresAt: Long? = null,
    val pollData: PollData? = null,
    val fileType: String? = null,
    val isUnsent: Boolean = false,
    val replyToId: Long? = null,      // NEW: swipe-to-reply target
    val replyToText: String? = null   // NEW: preview snippet of replied msg
)

enum class MsgType { Text, Sticker, Poll, File, Image, Audio, Deleted, Video }

data class PollData(
    val question: String,
    val options: List<String>,
    val votes: MutableList<Int> = mutableListOf()
)

// ─── Contact ──────────────────────────────────────────────────────────────────
data class Contact(
    val id: Long,
    val name: String,
    val handle: String,
    val online: Boolean = false,
    val urgency: String = "low",
    val bloom: Int = 50,
    val days: Int = 0,
    val group: String = "FRIENDS",
    val paletteIndex: Int = 0,
    val messages: MutableList<Message> = mutableListOf(),
    var locked: Boolean = false,
    var pinned: Boolean = false,
    var disappear: Int = 0,
    var unread: Int = 0,
    val bloomScore: Int = 50,
    var vibeVisibility: VibeVisibility = VibeVisibility.ALL,  // NEW: per-contact vibe visibility
    val backendId: String = ""                                // NEW: MongoDB _id mapping
)

// NEW: Vibe visibility control
enum class VibeVisibility { ALL, NONE, CONTACTS_ONLY }

// ─── Group / Priority Circle ─────────────────────────────────────────────────
data class ContactGroup(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val emoji: String,
    val tone: String = "warm",
    val members: MutableList<Long> = mutableListOf(),
    val muse: Boolean = false,
    val photoUri: String? = null
)

// ─── Vibe Video ───────────────────────────────────────────────────────────────
data class VibeVideo(
    val id: Int,
    val title: String,
    val creator: String,
    val duration: String,
    val views: String,
    val category: String,
    val likes: Int,
    val paletteColors: List<Color>,
    val comments: List<String> = listOf(),
    val visibility: VibeVideoVisibility = VibeVideoVisibility.ALL,  // NEW
    val allowedContactIds: MutableList<Long> = mutableListOf(),     // NEW: whitelist
    val blockedContactIds: MutableList<Long> = mutableListOf()      // NEW: blacklist
)

// NEW: Vibe video visibility
enum class VibeVideoVisibility { ALL, ONLY_LISTED, BLOCK_LISTED }

// ─── User Profile ─────────────────────────────────────────────────────────────
data class UserProfile(
    val name: String = "",
    val handle: String = "",
    val bio: String = "",
    val palette: List<Color> = listOf(
        Color(0xFFFF3385), Color(0xFF8B5CF6), Color(0xFF33A1FF),
        Color(0xFF4DFFD4), Color(0xFFF59E0B)
    ),
    val lightSeed: Float = 0.42f,
    val photoUri: String? = null,
    val lang: String = "en",
    val globalMuse: Boolean = false,
    val saveRate: Int = 5,
    val coinBalance: Int = 0,
    val theme: String = "",
    val font: String = "DM Sans",
    val emergencyContacts: MutableList<EmergencyContact> = mutableListOf()
)

data class EmergencyContact(val name: String, val type: String)

// ─── Bloom Post ───────────────────────────────────────────────────────────────
data class BloomPost(
    val id: Int,
    val authorName: String,
    val authorHandle: String,
    val content: String,
    val time: String,
    val likes: Int,
    val comments: Int,
    val paletteIndex: Int,
    val mood: String
)

// ─── Group Chat ───────────────────────────────────────────────────────────────
data class GroupChat(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val memberIds: MutableList<Long> = mutableListOf(),
    val messages: MutableList<Message> = mutableListOf(),
    val circleId: String? = null,
    val photoUri: String? = null,
    var description: String = ""
)

// ─── Meet User (new MEET feature) ────────────────────────────────────────────
data class MeetUser(
    val id: Long,
    val name: String,
    val handle: String,
    val bio: String,
    val interests: List<String>,
    val paletteIndex: Int,
    val mutualCount: Int = 0,
    val distance: String = ""
)

sealed class MainTab(val route: String, val label: String, val emoji: String) {
    object Chats    : MainTab("chats",  "Messages", "💬")
    object Bloom    : MainTab("bloom",  "Bloom",    "🌸")
    object Groups   : MainTab("groups", "Circles",  "◉")
    object Muse     : MainTab("muse",   "Muse AI",  "✦")
    object Vibe     : MainTab("vibe",   "Vibe",     "▶")
    object Coins    : MainTab("coins",  "Coins",    "🪙")
}

// ─── Seed Data (empty — all data comes from backend) ─────────────────────────
object SeedData {

    val CONTACTS = mutableListOf<Contact>()

    val GROUPS = mutableListOf(
        ContactGroup(1, "BESTIE",  "💜", "intimate",     mutableListOf(), true),
        ContactGroup(2, "FAMILY",  "🌸", "warm",         mutableListOf(), false),
        ContactGroup(3, "FRIENDS", "✨", "friendly",     mutableListOf(), false),
        ContactGroup(4, "WORK",    "⚡", "professional", mutableListOf(), false)
    )

    val VIBE_VIDEOS = mutableListOf<VibeVideo>()

    val BLOOM_POSTS = listOf<BloomPost>()

    val COIN_HISTORY = listOf<Triple<String, String, String>>()

    val MEET_USERS = mutableListOf<MeetUser>()
}

