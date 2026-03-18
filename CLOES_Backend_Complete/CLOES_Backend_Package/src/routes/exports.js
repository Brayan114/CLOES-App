const router   = require('express').Router();
const { protect } = require('../middleware/auth');
const archiver = require('archiver');
const User     = require('../models/User');
const Message  = require('../models/Message');
const Conversation = require('../models/Conversation');
const VibeVideo = require('../models/VibeVideo');
const { CoinTransaction } = require('../models/Analytics');

// ── Export all user data as JSON ──────────────────────────────────────────────
router.get('/my-data', protect, async (req, res) => {
  const user = await User.findById(req.user._id).select('-password -cloesedKey');

  const conversations = await Conversation.find({ participants: req.user._id })
    .populate('participants', 'name handle');

  const messages = await Message.find({
    conversationId: { $in: conversations.map(c => c._id) },
    sender: req.user._id
  }).populate('sender', 'name handle');

  const vibes = await VibeVideo.find({ creator: req.user._id });

  const coinHistory = await CoinTransaction.find({ user: req.user._id })
    .sort({ createdAt: -1 }).limit(500);

  const exportData = {
    exportedAt: new Date().toISOString(),
    profile:    user.toPublicJSON(),
    conversations: conversations.map(c => ({
      id: c._id, type: c.type, name: c.name,
      participants: c.participants,
      messageCount: c.messageCount,
      createdAt: c.createdAt,
    })),
    messages: messages.map(m => ({
      text: m.text, type: m.type, mediaUrl: m.mediaUrl,
      timestamp: m.createdAt,
    })),
    vibes: vibes.map(v => ({ title: v.title, views: v.views, likes: v.likes.length })),
    coins: { balance: user.coinBalance, history: coinHistory },
  };

  res.setHeader('Content-Disposition', `attachment; filename="cloes-data-${user.handle}.json"`);
  res.setHeader('Content-Type', 'application/json');
  res.json(exportData);
});

// ── Export chat as ZIP (messages + media list) ────────────────────────────────
router.get('/chat/:conversationId/zip', protect, async (req, res) => {
  const convo = await Conversation.findOne({
    _id: req.params.conversationId,
    participants: req.user._id,
  }).populate('participants', 'name handle');

  if (!convo) return res.status(403).json({ error: 'Not a member' });

  const messages = await Message.find({ conversationId: req.params.conversationId })
    .populate('sender', 'name handle')
    .sort({ createdAt: 1 });

  const chatText = messages.map(m => {
    const time = m.createdAt.toLocaleString();
    const sender = m.sender?.name || 'Unknown';
    if (m.type === 'text') return `[${time}] ${sender}: ${m.text}`;
    return `[${time}] ${sender}: [${m.type.toUpperCase()}] ${m.mediaUrl || m.text}`;
  }).join('\n');

  const mediaList = messages
    .filter(m => m.mediaUrl)
    .map(m => `${m.type}: ${m.mediaUrl}`)
    .join('\n');

  res.setHeader('Content-Type', 'application/zip');
  res.setHeader('Content-Disposition', `attachment; filename="cloes-chat.zip"`);

  const archive = archiver('zip', { zlib: { level: 9 } });
  archive.pipe(res);
  archive.append(chatText,  { name: 'chat.txt' });
  archive.append(mediaList, { name: 'media-links.txt' });
  archive.finalize();
});

module.exports = router;
