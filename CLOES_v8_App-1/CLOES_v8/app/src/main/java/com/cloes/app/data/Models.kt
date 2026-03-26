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
    var vibeVisibility: VibeVisibility = VibeVisibility.ALL  // NEW: per-contact vibe visibility
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
    val name: String = "Lumina",
    val handle: String = "lumina.x",
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
    val coinBalance: Int = 248,
    val theme: String = "",
    val font: String = "DM Sans",
    val emergencyContacts: MutableList<EmergencyContact> = mutableListOf(
        EmergencyContact("Mum 🌸", "FAMILY"),
        EmergencyContact("Best Friend", "FRIEND")
    )
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

// ─── Seed Data ───────────────────────────────────────────────────────────────
object SeedData {

    val CONTACTS = mutableListOf(
        Contact(1, "Nova·△7", "@nova.seven", true,  "high", 92, 1, "BESTIE",    0,
            mutableListOf(
                Message(1, "need to talk to you NOW 🔥", false, "9:41"),
                Message(2, "it's important please", false, "9:42"),
                Message(3, "ok omw 💜", true, "9:43")
            ), unread = 2, bloomScore = 92),
        Contact(2, "Mum 🌸", "@mum", false, "low",  78, 4, "FAMILY",   1,
            mutableListOf(
                Message(4, "Call me when you get this darling 💛", false, "8:30"),
                Message(5, "of course! Love you 🌸", true, "8:45")
            ), unread = 1, bloomScore = 78),
        Contact(3, "Sol ☀️", "@sol.bright", true,  "mid", 85, 0, "BESTIE",    2,
            mutableListOf(
                Message(6, "tonight?? 🌙", false, "Yesterday"),
                Message(7, "definitely ✦", true, "Yesterday")
            ), bloomScore = 85),
        Contact(4, "Ren", "@ren.quiet", false, "low", 61, 7, "FRIENDS",  3,
            mutableListOf(
                Message(8, "loved your last bloom post", false, "Mon"),
                Message(9, "ty!! 💜", true, "Mon")
            ), bloomScore = 61),
        Contact(5, "Zara·◈", "@zara.echo", true,  "low", 54, 2, "WORK",      4,
            mutableListOf(
                Message(10, "the deck looks 🔥", false, "Sun"),
                Message(11, "finally!! took forever", true, "Sun")
            ), bloomScore = 22),   // low bloom → triggers ritual
        Contact(6, "Ash 🌊", "@ash.waves", false, "low", 70, 12, "FRIENDS",  5,
            mutableListOf(
                Message(12, "miss you lots", false, "Sat"),
                Message(13, "same! coffee soon?", true, "Sat")
            ), bloomScore = 70),
        Contact(7, "Iris 🦋", "@iris.lux", true,  "mid", 88, 0, "BESTIE",    6,
            mutableListOf(
                Message(14, "saw that and thought of you 🦋", false, "Fri"),
                Message(15, "stoppp 🥺", true, "Fri")
            ), locked = true, bloomScore = 88),
        Contact(8, "Dev ⚡", "@dev.spark", true,  "high", 95, 0, "WORK",      7,
            mutableListOf(
                Message(16, "push the fix ASAP 🚨", false, "10:05"),
                Message(17, "on it now", true, "10:06")
            ), unread = 1, bloomScore = 95)
    )

    val GROUPS = mutableListOf(
        ContactGroup(1, "BESTIE",  "💜", "intimate",     mutableListOf(1, 3, 7), true),
        ContactGroup(2, "FAMILY",  "🌸", "warm",         mutableListOf(2),       false),
        ContactGroup(3, "FRIENDS", "✨", "friendly",     mutableListOf(4, 6),    false),
        ContactGroup(4, "WORK",    "⚡", "professional", mutableListOf(5, 8),    false)
    )

    val VIBE_VIDEOS = mutableListOf(
        VibeVideo(1, "Golden Hour Vibes ✨", "Nova·△7",    "0:28", "12.4K", "MOOD",     8420,
            listOf(Color(0xFFFF3385), Color(0xFF8B5CF6)),
            listOf("omg obsessed 💜", "this is everything", "need this energy rn")),
        VibeVideo(2, "Muse Session 🌙",     "Sol ☀️",     "0:45", "8.1K",  "MUSE",     5210,
            listOf(Color(0xFF33A1FF), Color(0xFF4DFFD4)),
            listOf("so calming", "sending this to everyone I know")),
        VibeVideo(3, "Morning Ritual ☕",   "Iris 🦋",    "0:32", "21K",   "DAILY",    14300,
            listOf(Color(0xFFF59E0B), Color(0xFFFF6B35)),
            listOf("this is my routine exactly", "goals ☕")),
        VibeVideo(4, "Deep Work Flow ⚡",   "Dev ⚡",     "1:02", "4.2K",  "FOCUS",    2100,
            listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)),
            listOf("coding to this rn", "🔥🔥")),
        VibeVideo(5, "Night Walk 🌃",       "Zara·◈",     "0:38", "6.7K",  "AMBIENT",  3800,
            listOf(Color(0xFF0F0A1E), Color(0xFF8B5CF6)),
            listOf("city lights hit different", "lofi vibes ✦")),
        VibeVideo(6, "Bloom Journaling 📓", "Mum 🌸",    "0:55", "9.3K",  "WELLNESS", 6100,
            listOf(Color(0xFFFF3385), Color(0xFFF59E0B)),
            listOf("I needed this today 🌸", "healing era"))
    )

    val BLOOM_POSTS = listOf(
        BloomPost(1, "Nova·△7",  "@nova.seven",  "manifesting only good things this season ✦ letting go of what no longer serves.", "2m ago", 47, 8,  0, "🌙"),
        BloomPost(2, "Sol ☀️",   "@sol.bright",  "sometimes the most radical thing you can do is rest. 🌿",                        "8m ago", 92, 14, 2, "🌿"),
        BloomPost(3, "Iris 🦋",  "@iris.lux",    "made coffee. lit a candle. watched the rain. small joys > everything.",          "15m ago",63, 11, 6, "✨"),
        BloomPost(4, "Ash 🌊",   "@ash.waves",   "the version of me from a year ago would be so proud. growth is quiet but real.", "1h ago", 118,22, 5, "🌊"),
        BloomPost(5, "Zara·◈",   "@zara.echo",   "reminder: you don't owe anyone a constant performance of your best self.",       "3h ago", 201,31, 4, "◈")
    )

    val COIN_HISTORY = listOf(
        Triple("Muse AI session", "+3 coins",  "2m ago"),
        Triple("20% data saved", "+5 coins",  "20m ago"),
        Triple("Daily check-in",  "+2 coins",  "1h ago"),
        Triple("Vibe video sent", "+1 coin",   "3h ago"),
        Triple("Bought Dark Theme","−25 coins","Yesterday")
    )

    val MEET_USERS = mutableListOf(
        MeetUser(101, "Kai ✦",     "@kai.bloom",   "Designer, dreamer, collector of sunsets 🌅",
            listOf("Design", "Music", "Travel"), 0, 2, "2 km away"),
        MeetUser(102, "Lyra 🌙",   "@lyra.night",  "Writer. Night owl. Making sense of the noise.",
            listOf("Writing", "Film", "Coffee"), 0, 5, "5 km away"),
        MeetUser(103, "Orion ⚡",  "@orion.dev",   "Building things that matter. Coffee → code.",
            listOf("Tech", "Gaming", "Music"), 0, 1, "8 km away"),
        MeetUser(104, "Sage 🌿",   "@sage.quiet",  "Therapist off-duty. Plant parent. Still figuring it out.",
            listOf("Wellness", "Books", "Nature"), 0, 3, "1 km away"),
        MeetUser(105, "Echo ◈",    "@echo.space",  "Musician. Everything is a sample if you're brave enough.",
            listOf("Music", "Art", "Poetry"), 0, 0, "12 km away"),
        MeetUser(106, "River 🌊",  "@river.flow",  "Sports coach + amateur philosopher. Hot takes available.",
            listOf("Sports", "Philosophy", "Food"), 0, 4, "3 km away")
    )
}
