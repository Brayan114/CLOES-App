package com.cloes.app.viewmodel

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.cloes.app.data.*
import com.cloes.app.ui.theme.*

class AppViewModel : ViewModel() {

    var profile by mutableStateOf(UserProfile())
    var appTheme by mutableStateOf(AppTheme.Default)
    var appFont  by mutableStateOf("Default")
    var appFontFamily: androidx.compose.ui.text.font.FontFamily
        by mutableStateOf(androidx.compose.ui.text.font.FontFamily.Default)

    var currentScreen by mutableStateOf("splash")
    var currentTab    by mutableStateOf("chats")
    var currentChatId by mutableStateOf<Long?>(null)

    val contacts   = mutableStateListOf<Contact>().also { it.addAll(SeedData.CONTACTS) }
    val groups     = mutableStateListOf<ContactGroup>().also { it.addAll(SeedData.GROUPS) }
    val vibeVideos = mutableStateListOf<VibeVideo>().also { it.addAll(SeedData.VIBE_VIDEOS) }
    val groupChats = mutableStateListOf<GroupChat>()

    // UI state
    var showAddContact         by mutableStateOf(false)
    var showAddGroup           by mutableStateOf(false)
    var showAddGroupChat       by mutableStateOf(false)
    var showThemeModal         by mutableStateOf(false)
    var showShareModal         by mutableStateOf(false)
    var showChatMenu           by mutableStateOf(false)
    var showDisappearModal     by mutableStateOf(false)
    var showPollModal          by mutableStateOf(false)
    var showFileModal          by mutableStateOf(false)
    var showEmergency          by mutableStateOf(false)
    var showCallScreen         by mutableStateOf(false)
    var showEditProfile        by mutableStateOf(false)
    var showSettings           by mutableStateOf(false)
    var showStickerPanel       by mutableStateOf(false)
    var showUploadVideo        by mutableStateOf(false)
    var showProfile            by mutableStateOf(false)
    var showVibeShorts         by mutableStateOf(false)
    var vibeShortStartIdx      by mutableStateOf(0)
    var showProfileVibe        by mutableStateOf(false)
    var profileVibeCreator     by mutableStateOf("")
    var showAnalyticsModal     by mutableStateOf(false)
    var showEditContact        by mutableStateOf(false)
    var showDeleteContact      by mutableStateOf(false)
    var showLeaveGroup         by mutableStateOf(false)
    var showDeleteBubble       by mutableStateOf(false)
    var showEditBubble         by mutableStateOf(false)
    var showCleanChat          by mutableStateOf(false)
    var selectedBubbleId       by mutableStateOf<Long?>(-1L)
    var showQrCode             by mutableStateOf(false)
    var profileViewContactId   by mutableStateOf<Long?>(-1L)
    var profileViewShowLight   by mutableStateOf(false)
    var showGroupChatMenu      by mutableStateOf(false)
    var showEditGroup          by mutableStateOf(false)
    var showGroupChatView      by mutableStateOf(false)
    var showCreateGroupChat    by mutableStateOf(false)
    var showHowToUse           by mutableStateOf(false)
    var showPhoneBackup        by mutableStateOf(false)
    var showChatBgPicker       by mutableStateOf(false)
    var showStickerCreator     by mutableStateOf(false)
    var showCircleMessage      by mutableStateOf(false)
    var showCoinsStore         by mutableStateOf(false)
    var showFontPicker         by mutableStateOf(false)
    var showComingSoon         by mutableStateOf(false)
    var comingSoonLabel        by mutableStateOf("")
    var circleMessageGroup: ContactGroup? by mutableStateOf(null)

    // Cloesed Key
    var cloesedKey         by mutableStateOf("")
    var showSetCloesedKey  by mutableStateOf(false)
    var showLockedContacts by mutableStateOf(false)
    var cloesedKeyPending  by mutableStateOf("")
    var currentGroupChatId by mutableStateOf<Long?>(null)

    // Onboard
    var onboardStep   by mutableStateOf(1)
    var onboardName   by mutableStateOf("")
    var onboardHandle by mutableStateOf("")
    val onboardPalette = mutableStateListOf<Color>()

    // Chat
    var chatInput        by mutableStateOf("")
    var callingContact   by mutableStateOf<Contact?>(null)
    var chatWallpaperUri by mutableStateOf<String?>(null)

    // Toast
    var toastVisible by mutableStateOf(false)
    var toastName    by mutableStateOf("")
    var toastMsg     by mutableStateOf("")
    var toastUrgency by mutableStateOf("low")

    // Bubble colors
    var bubbleSent1   by mutableStateOf(Color(0xFF8B5CF6))
    var bubbleSent2   by mutableStateOf(Color(0xFFFF3385))
    var bubbleRecvBg  by mutableStateOf(Color(0xEDF5F0FF))
    var bubbleRecvTxt by mutableStateOf(Color(0xFF2A1A4A))

    // Misc
    var urgencyTintOn        by mutableStateOf(false)
    var coinBalance          by mutableStateOf(248)
    var notificationsEnabled by mutableStateOf(true)
    var globalMuse           by mutableStateOf(false)
    var currentLang          by mutableStateOf("en")

    // Temp inputs
    var newContactName    by mutableStateOf("")
    var newContactHandle  by mutableStateOf("")
    var newContactFrag    by mutableStateOf(0)
    var newContactGroup   by mutableStateOf("FRIENDS")
    var newGroupName      by mutableStateOf("")
    var newGroupEmoji     by mutableStateOf("💜")
    var newGroupTone      by mutableStateOf("warm")
    var pollQuestion      by mutableStateOf("")
    var pollOptA          by mutableStateOf("")
    var pollOptB          by mutableStateOf("")
    var pollOptC          by mutableStateOf("")
    var editContactName   by mutableStateOf("")
    var editContactHandle by mutableStateOf("")
    var editContactGroup  by mutableStateOf("")
    var phoneBackup       by mutableStateOf("")

    // ── Side Panel ────────────────────────────────────────────────────────────
    var showSidePanel by mutableStateOf(false)

    // ── Muse Draw ─────────────────────────────────────────────────────────────
    var showMuseDraw by mutableStateOf(false)

    // ── Muse Clothing ─────────────────────────────────────────────────────────
    var showMuseClothing     by mutableStateOf(false)
    var museClothingMode     by mutableStateOf("")
    var showMuseDress        by mutableStateOf(false)
    var museDressWelcomeSeen by mutableStateOf(false)
    var museDressImagesUsed  by mutableStateOf(0)
    val museDressMessages    = mutableStateListOf<Message>()

    // ── Muse History ──────────────────────────────────────────────────────────
    var showMuseHistory by mutableStateOf(false)
    var museHistoryMode by mutableStateOf("")

    // ── Audio Extractor ───────────────────────────────────────────────────────
    var showAudioExtractor by mutableStateOf(false)

    // ── Cloes Echo ────────────────────────────────────────────────────────────
    var showCloesEcho    by mutableStateOf(false)
    var echoUnlocked     by mutableStateOf(false)
    val echoHiddenVideos = mutableStateListOf<String>()
    val echoHiddenPhotos = mutableStateListOf<String>()
    val echoHiddenLinks  = mutableStateListOf<String>()

    // ── Muse Task ─────────────────────────────────────────────────────────────
    var showMuseTask by mutableStateOf(false)

    // ── Auth ──────────────────────────────────────────────────────────────────
    var showLoginPage           by mutableStateOf(false)
    var showSignupPage          by mutableStateOf(false)
    var showLogoutDialog        by mutableStateOf(false)
    var showDeleteAccountDialog by mutableStateOf(false)

    // ── Typing + Pin ──────────────────────────────────────────────────────────
    var typingContactId by mutableStateOf<Long?>(-1L)
    var pinnedBubbleId  by mutableStateOf<Long?>(-1L)

    // ── Nav Bar auto-hide ─────────────────────────────────────────────────────
    var navBarVisible         by mutableStateOf(true)
    var disappearingNavEnabled by mutableStateOf(true)   // toggle in settings

    // ── Muse AI logo ──────────────────────────────────────────────────────────
    var museLogoIndex by mutableStateOf(0)   // cycles 0-3 every 3 sec

    // ── Notes ─────────────────────────────────────────────────────────────────
    data class MuseNote(
        val id: Long = System.currentTimeMillis(),
        var title: String,
        var body: String,
        val createdAt: String = ""
    )
    val museNotes = mutableStateListOf<MuseNote>()

    // Theme
    val themeColors: CloesColors get() = when (appTheme) {
        AppTheme.Default -> DefaultColors
        AppTheme.Dark    -> DarkColors
        AppTheme.Rose    -> RoseColors
        AppTheme.Forest  -> ForestColors
    }

    fun currentContact(): Contact? = contacts.find { it.id == currentChatId }
    fun currentGroupChat(): GroupChat? = groupChats.find { it.id == currentGroupChatId }

    fun showToast(name: String, msg: String, urgency: String = "low") {
        toastName = name; toastMsg = msg; toastUrgency = urgency; toastVisible = true
    }

    // For incoming message notifications — respects the notifications toggle
    fun showIncomingToast(name: String, msg: String, urgency: String = "low") {
        if (notificationsEnabled) showToast(name, msg, urgency)
    }

    fun showToastIfEnabled(name: String, msg: String, urgency: String = "low") {
        if (notificationsEnabled) showToast(name, msg, urgency)
    }

    fun openChat(id: Long) {
        val c = contacts.find { it.id == id } ?: return
        currentChatId = id; c.unread = 0
        if (c.urgency == "high") urgencyTintOn = true
    }

    fun closeChat() { currentChatId = null; urgencyTintOn = false; showStickerPanel = false }

    fun sendMessage(text: String) {
        val c = currentContact() ?: return
        if (text.isBlank()) return
        c.messages.add(Message(text = text, sent = true, timestamp = currentTime()))
        chatInput = ""
    }

    fun sendPoll() {
        val c = currentContact() ?: return
        if (pollQuestion.isBlank() || pollOptA.isBlank() || pollOptB.isBlank()) {
            showToast("Poll", "Fill in question and at least 2 options", "mid"); return
        }
        val opts = listOfNotNull(pollOptA.ifBlank { null }, pollOptB.ifBlank { null }, pollOptC.ifBlank { null })
        c.messages.add(Message(text = "📊 $pollQuestion", sent = true, timestamp = currentTime(),
            type = MsgType.Poll, pollData = PollData(pollQuestion, opts, MutableList(opts.size) { 0 })))
        pollQuestion = ""; pollOptA = ""; pollOptB = ""; pollOptC = ""
        showPollModal = false; showToast("Poll", "Poll sent!", "low")
    }

    fun toggleChatLock() {
        val c = currentContact() ?: return
        c.locked = !c.locked
        val idx = contacts.indexOfFirst { it.id == c.id }
        contacts[idx] = c.copy(locked = c.locked)
        showToast(c.name, if (c.locked) "Chat locked" else "Chat unlocked", "low")
        if (c.locked && cloesedKey.isBlank()) showSetCloesedKey = true
    }

    fun editGroupChat(id: Long, newName: String, circleId: String?) {
        val idx = groupChats.indexOfFirst { it.id == id }
        if (idx < 0) return
        groupChats[idx] = groupChats[idx].copy(name = newName, circleId = circleId)
        showEditGroup = false; showToast(newName, "Group updated", "low")
    }

    fun togglePin() {
        val c = currentContact() ?: return
        val idx = contacts.indexOfFirst { it.id == c.id }
        contacts[idx] = c.copy(pinned = !c.pinned)
        showToast(c.name, if (contacts[idx].pinned) "Chat pinned" else "Chat unpinned", "low")
    }

    fun pinBubble(msgId: Long) {
        pinnedBubbleId = if (pinnedBubbleId == msgId) -1L else msgId
    }

    fun setDisappear(secs: Int) {
        val c = currentContact() ?: return
        val idx = contacts.indexOfFirst { it.id == c.id }
        contacts[idx] = c.copy(disappear = secs)
        showDisappearModal = false
        val label = when (secs) { 0->"Off";30->"30 seconds";300->"5 minutes";3600->"1 hour";86400->"24 hours";604800->"1 week";else->"Custom" }
        showToast(c.name, "Disappearing messages: $label", "low")
    }

    fun addContact() {
        if (newContactName.isBlank()) { showToast("Add", "Enter a nickname", "mid"); return }
        val nc = Contact(id = System.currentTimeMillis(), name = newContactName,
            handle = "@${newContactHandle.ifBlank { newContactName.lowercase().replace(" ", ".") }}",
            group = newContactGroup, paletteIndex = newContactFrag)
        contacts.add(nc)
        groups.find { it.name == newContactGroup }?.members?.add(nc.id)
        newContactName = ""; newContactHandle = ""; showAddContact = false
        showToast("Added", "${nc.name} joined your world", "low")
    }

    fun saveGroup() {
        if (newGroupName.isBlank()) { showToast("Group", "Enter a group name", "mid"); return }
        groups.add(ContactGroup(name = newGroupName.uppercase(), emoji = newGroupEmoji, tone = newGroupTone))
        newGroupName = ""; showAddGroup = false
        showToast("Groups", "Circle created", "low")
    }

    fun saveProfile() { showEditProfile = false; showToast("Profile", "Profile updated", "low") }

    fun setTheme(theme: AppTheme) {
        appTheme = theme
        val names = mapOf(AppTheme.Default to "Default", AppTheme.Dark to "Dark", AppTheme.Rose to "Rose", AppTheme.Forest to "Forest")
        showToast("Theme", "${names[theme]} applied", "low")
    }

    fun setFont(font: String, family: androidx.compose.ui.text.font.FontFamily) {
        appFont = font; appFontFamily = family; showToast("Font", "$font applied", "low")
    }

    fun setBubbleColor(c1: Color, c2: Color, name: String) {
        bubbleSent1 = c1; bubbleSent2 = c2; showToast("Bubbles", "$name applied", "low")
    }

    fun openVibeShorts(idx: Int) { vibeShortStartIdx = idx; showVibeShorts = true }
    fun triggerCall(contact: Contact) { callingContact = contact; showCallScreen = true }
    fun endCall() { showCallScreen = false; callingContact = null }

    fun addEmergencyContact(name: String, type: String) {
        profile.emergencyContacts.add(EmergencyContact(name, type))
        showToast("Emergency", "$name added", "low")
    }

    fun createGroupChat(name: String, memberIds: List<Long>) {
        groupChats.add(GroupChat(id = System.currentTimeMillis(), name = name, memberIds = memberIds.toMutableList()))
        showCreateGroupChat = false; showToast("Group", "$name created", "low")
    }

    fun sendCircleMessage(group: ContactGroup, message: String) {
        val members = contacts.filter { group.members.contains(it.id) }
        members.forEach { c -> c.messages.add(Message(text = message, sent = true, timestamp = currentTime())) }
        showToast(group.name, "Message sent to ${members.size} members", "low")
    }

    fun totalUnread(): Int = contacts.sumOf { it.unread }

    fun likeVibe(id: Int) {
        vibeVideos.find { it.id == id } ?: return
        earnCoins(1, "Vibe liked"); showToast("Vibe", "Liked! ✦", "low")
    }

    fun addVibeVideo(title: String, creator: String) {
        vibeVideos.add(0, VibeVideo(id = System.currentTimeMillis().toInt(), title = title, creator = creator,
            duration = "0:30", views = "0", category = "NEW", likes = 0,
            paletteColors = listOf(Color(0xFFFF3385), Color(0xFF8B5CF6))))
        showToast("Vibe", "Video uploaded", "low"); earnCoins(5, "Vibe uploaded")
    }

    fun deleteContact() {
        val c = currentContact() ?: return
        contacts.removeAll { it.id == c.id }
        groups.forEach { g -> g.members.remove(c.id) }
        currentChatId = null; showDeleteContact = false
        showToast("Deleted", "${c.name} removed from your world", "low")
    }

    fun sendGroupMessage(text: String) {
        val gc = currentGroupChat() ?: return
        if (text.isBlank()) return
        gc.messages.add(Message(text = text, sent = true, timestamp = currentTime()))
        chatInput = ""
    }

    fun leaveGroupChat() {
        val id = currentGroupChatId ?: return
        groupChats.removeAll { it.id == id }
        currentGroupChatId = null; showLeaveGroup = false
        showToast("Left", "You left the group", "low")
    }

    fun deleteBubbleForMe(msgId: Long) {
        val c = currentContact() ?: return
        val idx = c.messages.indexOfFirst { it.id == msgId }
        if (idx >= 0) c.messages[idx] = c.messages[idx].copy(type = MsgType.Deleted, text = "This message was deleted")
        showDeleteBubble = false; selectedBubbleId = null
    }

    fun deleteBubbleForEveryone(msgId: Long) {
        val c = currentContact() ?: return
        val idx = c.messages.indexOfFirst { it.id == msgId }
        if (idx >= 0) c.messages.removeAt(idx)
        showDeleteBubble = false; selectedBubbleId = null
        showToast(c.name, "Message deleted for everyone", "low")
    }

    fun editBubble(msgId: Long, newText: String) {
        val c = currentContact() ?: return
        val idx = c.messages.indexOfFirst { it.id == msgId }
        if (idx >= 0) c.messages[idx] = c.messages[idx].copy(text = "$newText (edited)")
        showEditBubble = false; selectedBubbleId = null
    }

    fun cleanChat(forAll: Boolean = false) {
        val c = currentContact() ?: return
        c.messages.clear(); showCleanChat = false
        showToast(c.name, if (forAll) "Chat cleared for everyone" else "Chat cleared", "low")
    }

    fun exportChatHistory() { showToast(currentContact()?.name ?: "", "Chat history exported", "low") }

    fun earnCoins(amount: Int, reason: String = "") {
        coinBalance += amount
        if (reason.isNotBlank()) showToast("Coins", "+$amount coins — $reason", "low")
    }

    fun logout() {
        showLogoutDialog = false; currentScreen = "splash"
        showToast("CLOES", "See you soon ✦", "low")
    }

    fun deleteAccount() {
        contacts.clear(); groups.clear(); groupChats.clear()
        museNotes.clear(); museDressMessages.clear(); coinBalance = 0
        showDeleteAccountDialog = false; currentScreen = "splash"
    }

    fun currentTime(): String {
        val cal = java.util.Calendar.getInstance()
        val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val m = cal.get(java.util.Calendar.MINUTE)
        return "$h:${m.toString().padStart(2, '0')}"
    }
}
