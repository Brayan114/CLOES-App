// ── Bloom Service ─────────────────────────────────────────────────────────────
const cron       = require('node-cron');
const Conversation = require('../models/Conversation');
const { BloomEntry } = require('../models/Analytics');
const User = require('../models/User');

// ── Calculate bloom score (0–100) ────────────────────────────────────────────
const calcBloomScore = (entry) => {
  const msgScore  = Math.min(entry.messagesSent * 0.5 + entry.messagesReceived * 0.3, 40);
  const mediaScore = Math.min(entry.mediaShared * 2, 15);
  const callScore  = Math.min(entry.callMinutes * 0.5, 20);
  const streakScore = Math.min(entry.streakDays * 1.5, 20);
  const recencyBonus = entry.lastMessageAt &&
    (Date.now() - entry.lastMessageAt.getTime() < 24 * 3600 * 1000) ? 5 : 0;

  return Math.min(Math.round(msgScore + mediaScore + callScore + streakScore + recencyBonus), 100);
};

// ── Determine urgency from bloom score ───────────────────────────────────────
const calcUrgency = (score) => {
  if (score >= 80) return 'high';
  if (score >= 50) return 'mid';
  return 'low';
};

// ── Called every time a message is sent ──────────────────────────────────────
const updateBloomOnMessage = async (convo, senderId, isMedia = false) => {
  try {
    if (convo.type !== 'direct') return;
    const partnerId = convo.participants.find(p => p.toString() !== senderId.toString());
    if (!partnerId) return;

    let entry = await BloomEntry.findOne({ conversation: convo._id, user: senderId });
    if (!entry) {
      entry = await BloomEntry.create({
        conversation: convo._id,
        user:    senderId,
        partner: partnerId,
      });
    }

    entry.messagesSent    += 1;
    if (isMedia) entry.mediaShared += 1;
    entry.lastMessageAt = new Date();

    // Check streak: if last message was yesterday, increment streak
    const lastMsg = convo.lastMessageAt;
    if (lastMsg) {
      const daysSinceLast = Math.floor((Date.now() - lastMsg.getTime()) / (86400 * 1000));
      if (daysSinceLast <= 1) entry.streakDays = Math.min(entry.streakDays + 1, 365);
      else if (daysSinceLast > 2) entry.streakDays = 0;
    }

    entry.score   = calcBloomScore(entry);
    entry.urgency = calcUrgency(entry.score);
    await entry.save();

    // Update conversation urgency
    await Conversation.findByIdAndUpdate(convo._id, {
      urgency: entry.urgency,
      lastInteraction: new Date(),
      connectionDays: entry.streakDays,
    });

    // Also update partner's received count
    let partnerEntry = await BloomEntry.findOne({ conversation: convo._id, user: partnerId });
    if (partnerEntry) {
      partnerEntry.messagesReceived += 1;
      partnerEntry.score   = calcBloomScore(partnerEntry);
      partnerEntry.urgency = calcUrgency(partnerEntry.score);
      await partnerEntry.save();
    }
  } catch (err) {
    console.error('updateBloomOnMessage error:', err.message);
  }
};

// ── Called when a call ends ───────────────────────────────────────────────────
const updateBloomOnCall = async (conversationId, userId, durationMins) => {
  try {
    let entry = await BloomEntry.findOne({ conversation: conversationId, user: userId });
    if (!entry) return;
    entry.callMinutes += durationMins;
    entry.score   = calcBloomScore(entry);
    entry.urgency = calcUrgency(entry.score);
    await entry.save();
  } catch (err) {
    console.error('updateBloomOnCall error:', err.message);
  }
};

// ── Cron: recalculate all bloom scores daily ──────────────────────────────────
const startBloomCron = () => {
  cron.schedule('0 2 * * *', async () => {
    try {
      const entries = await BloomEntry.find();
      for (const e of entries) {
        e.score   = calcBloomScore(e);
        e.urgency = calcUrgency(e.score);
        await e.save();
      }
      console.log(`🌸 Bloom scores recalculated for ${entries.length} entries`);
    } catch (err) {
      console.error('Bloom cron error:', err.message);
    }
  });
  console.log('🌸 Bloom cron started — recalculates daily at 2am');
};

module.exports = { updateBloomOnMessage, updateBloomOnCall, startBloomCron, calcBloomScore, calcUrgency };
