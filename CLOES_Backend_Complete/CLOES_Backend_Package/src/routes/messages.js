const router       = require('express').Router();
const Conversation = require('../models/Conversation');
const Message      = require('../models/Message');
const User         = require('../models/User');
const { protect }  = require('../middleware/auth');
const { uploadChatMedia, uploadDocument } = require('../config/cloudinary');
const { getSocket } = require('../socket/socketServer');
const { updateBloomOnMessage } = require('../services/bloomService');
const { awardCoins } = require('../services/coinService');

// ── Get conversations list ────────────────────────────────────────────────────
router.get('/conversations', protect, async (req, res) => {
  const convos = await Conversation.find({ participants: req.user._id })
    .populate('participants', 'name handle avatar paletteColors lightSeed online lastSeen')
    .populate('lastMessage')
    .sort({ lastMessageAt: -1 });
  res.json(convos);
});

// ── Get or create direct conversation ────────────────────────────────────────
router.post('/conversations/direct', protect, async (req, res) => {
  const { userId } = req.body;
  if (!userId) return res.status(400).json({ error: 'userId required' });

  let convo = await Conversation.findOne({
    type: 'direct',
    participants: { $all: [req.user._id, userId], $size: 2 },
  }).populate('participants', 'name handle avatar paletteColors lightSeed online lastSeen');

  if (!convo) {
    convo = await Conversation.create({
      type: 'direct',
      participants: [req.user._id, userId],
    });
    await convo.populate('participants', 'name handle avatar paletteColors lightSeed online lastSeen');
  }

  res.json(convo);
});

// ── Get messages in a conversation ───────────────────────────────────────────
router.get('/conversations/:id/messages', protect, async (req, res) => {
  const { page = 1, limit = 50 } = req.query;
  const skip = (page - 1) * limit;

  const convo = await Conversation.findOne({
    _id: req.params.id,
    participants: req.user._id,
  });
  if (!convo) return res.status(403).json({ error: 'Not a member of this conversation' });

  const messages = await Message.find({ conversationId: req.params.id })
    .populate('sender', 'name handle avatar')
    .sort({ createdAt: -1 })
    .skip(skip)
    .limit(Number(limit));

  // Mark as read
  await Message.updateMany(
    { conversationId: req.params.id, sender: { $ne: req.user._id }, readBy: { $ne: req.user._id } },
    { $addToSet: { readBy: req.user._id } }
  );
  // Reset unread count
  await Conversation.updateOne(
    { _id: req.params.id, 'unreadCounts.userId': req.user._id },
    { $set: { 'unreadCounts.$.count': 0 } }
  );

  res.json(messages.reverse());
});

// ── Send text message ─────────────────────────────────────────────────────────
router.post('/conversations/:id/messages', protect, async (req, res) => {
  const { text, type = 'text', replyTo, disappearSecs } = req.body;

  const convo = await Conversation.findOne({ _id: req.params.id, participants: req.user._id });
  if (!convo) return res.status(403).json({ error: 'Not a member' });

  const expiresAt = disappearSecs ? new Date(Date.now() + disappearSecs * 1000) : null;

  const msg = await Message.create({
    conversationId: req.params.id,
    sender: req.user._id,
    text, type, replyTo, expiresAt,
  });

  await msg.populate('sender', 'name handle avatar');
  await updateConversationLastMessage(convo, msg, req.user._id);
  await updateBloomOnMessage(convo, req.user._id);

  // Emit real-time
  emitToConversation(req.params.id, 'new_message', msg, req.user._id);

  res.status(201).json(msg);
});

// ── Send media (image / video / audio) ───────────────────────────────────────
router.post('/conversations/:id/media',
  protect,
  uploadChatMedia.single('media'),
  async (req, res) => {
    const convo = await Conversation.findOne({ _id: req.params.id, participants: req.user._id });
    if (!convo) return res.status(403).json({ error: 'Not a member' });
    if (!req.file) return res.status(400).json({ error: 'No file' });

    const mime = req.file.mimetype;
    let type = 'image';
    if (mime.startsWith('video/')) type = 'video';
    else if (mime.startsWith('audio/')) type = 'audio';

    const { disappearSecs } = req.body;
    const expiresAt = disappearSecs ? new Date(Date.now() + disappearSecs * 1000) : null;

    const msg = await Message.create({
      conversationId: req.params.id,
      sender:    req.user._id,
      text:      req.body.caption || '',
      type,
      mediaUrl:  req.file.path || req.file.secure_url || `/uploads/chat/${req.file.filename}`,
      mediaPublicId: req.file.filename || req.file.public_id || '',
      mediaType: mime,
      fileName:  req.file.originalname,
      fileSize:  req.file.size,
      expiresAt,
    });

    await msg.populate('sender', 'name handle avatar');
    await updateConversationLastMessage(convo, msg, req.user._id);
    await updateBloomOnMessage(convo, req.user._id, true);

    emitToConversation(req.params.id, 'new_message', msg, req.user._id);
    res.status(201).json(msg);
  }
);

// ── Send document ─────────────────────────────────────────────────────────────
router.post('/conversations/:id/document',
  protect,
  uploadDocument.single('document'),
  async (req, res) => {
    const convo = await Conversation.findOne({ _id: req.params.id, participants: req.user._id });
    if (!convo) return res.status(403).json({ error: 'Not a member' });
    if (!req.file) return res.status(400).json({ error: 'No file' });

    const msg = await Message.create({
      conversationId: req.params.id,
      sender:    req.user._id,
      text:      req.file.originalname,
      type:      'document',
      mediaUrl:  req.file.path || req.file.secure_url || `/uploads/documents/${req.file.filename}`,
      mediaPublicId: req.file.filename || req.file.public_id || '',
      mediaType: req.file.mimetype,
      fileName:  req.file.originalname,
      fileSize:  req.file.size,
    });

    await msg.populate('sender', 'name handle avatar');
    await updateConversationLastMessage(convo, msg, req.user._id);
    emitToConversation(req.params.id, 'new_message', msg, req.user._id);
    res.status(201).json(msg);
  }
);

// ── Send link ─────────────────────────────────────────────────────────────────
router.post('/conversations/:id/link', protect, async (req, res) => {
  const { url, preview } = req.body;
  if (!url) return res.status(400).json({ error: 'URL required' });

  const convo = await Conversation.findOne({ _id: req.params.id, participants: req.user._id });
  if (!convo) return res.status(403).json({ error: 'Not a member' });

  const msg = await Message.create({
    conversationId: req.params.id,
    sender: req.user._id,
    text: url,
    type: 'link',
    mediaUrl: preview?.image || '',
    fileName: preview?.title || url,
  });

  await msg.populate('sender', 'name handle avatar');
  await updateConversationLastMessage(convo, msg, req.user._id);
  emitToConversation(req.params.id, 'new_message', msg, req.user._id);
  res.status(201).json(msg);
});

// ── React to message ─────────────────────────────────────────────────────────
router.post('/messages/:id/react', protect, async (req, res) => {
  const { emoji } = req.body;
  const msg = await Message.findById(req.params.id);
  if (!msg) return res.status(404).json({ error: 'Message not found' });

  // Toggle reaction
  const idx = msg.reactions.findIndex(r => r.userId.toString() === req.user._id.toString());
  if (idx >= 0 && msg.reactions[idx].emoji === emoji) {
    msg.reactions.splice(idx, 1);
  } else if (idx >= 0) {
    msg.reactions[idx].emoji = emoji;
  } else {
    msg.reactions.push({ userId: req.user._id, emoji });
  }
  await msg.save();

  const io = getSocket();
  io?.to(msg.conversationId.toString()).emit('message_reaction', {
    messageId: msg._id, reactions: msg.reactions
  });

  res.json({ reactions: msg.reactions });
});

// ── Delete message for me ─────────────────────────────────────────────────────
router.delete('/messages/:id/for-me', protect, async (req, res) => {
  const msg = await Message.findById(req.params.id);
  if (!msg) return res.status(404).json({ error: 'Not found' });
  msg.deleted = true;
  msg.text = 'This message was deleted';
  msg.type = 'deleted';
  await msg.save();
  res.json({ message: 'Deleted for you' });
});

// ── Delete message for everyone ───────────────────────────────────────────────
router.delete('/messages/:id/for-all', protect, async (req, res) => {
  const msg = await Message.findById(req.params.id);
  if (!msg || msg.sender.toString() !== req.user._id.toString()) {
    return res.status(403).json({ error: 'Not your message' });
  }
  await Message.findByIdAndDelete(req.params.id);

  const io = getSocket();
  io?.to(msg.conversationId.toString()).emit('message_deleted', { messageId: msg._id });

  res.json({ message: 'Deleted for everyone' });
});

// ── Edit message ──────────────────────────────────────────────────────────────
router.patch('/messages/:id', protect, async (req, res) => {
  const msg = await Message.findById(req.params.id);
  if (!msg || msg.sender.toString() !== req.user._id.toString()) {
    return res.status(403).json({ error: 'Not your message' });
  }
  msg.text = `${req.body.text} (edited)`;
  msg.edited = true;
  await msg.save();

  const io = getSocket();
  io?.to(msg.conversationId.toString()).emit('message_edited', msg);

  res.json(msg);
});

// ── Update chat wallpaper ─────────────────────────────────────────────────────
router.patch('/conversations/:id/wallpaper', protect, async (req, res) => {
  const { wallpaperUrl } = req.body;
  const convo = await Conversation.findOneAndUpdate(
    { _id: req.params.id, participants: req.user._id },
    { wallpaperUrl },
    { new: true }
  );
  if (!convo) return res.status(404).json({ error: 'Conversation not found' });
  res.json({ wallpaperUrl: convo.wallpaperUrl });
});

// ── Upload wallpaper image ────────────────────────────────────────────────────
router.post('/conversations/:id/wallpaper-upload', protect, uploadChatMedia.single('wallpaper'), async (req, res) => {
  if (!req.file) return res.status(400).json({ error: 'No file' });
  const url = req.file.path || req.file.secure_url || `/uploads/chat/${req.file.filename}`;
  const convo = await Conversation.findOneAndUpdate(
    { _id: req.params.id, participants: req.user._id },
    { wallpaperUrl: url },
    { new: true }
  );
  res.json({ wallpaperUrl: convo.wallpaperUrl });
});

// ── Set disappearing messages ─────────────────────────────────────────────────
router.patch('/conversations/:id/disappear', protect, async (req, res) => {
  const { seconds } = req.body;
  const convo = await Conversation.findOneAndUpdate(
    { _id: req.params.id, participants: req.user._id },
    { disappearSecs: seconds },
    { new: true }
  );
  res.json({ disappearSecs: convo.disappearSecs });
});

// ── Lock / unlock conversation ────────────────────────────────────────────────
router.patch('/conversations/:id/lock', protect, async (req, res) => {
  const { locked } = req.body;
  const convo = await Conversation.findOneAndUpdate(
    { _id: req.params.id, participants: req.user._id },
    { locked },
    { new: true }
  );
  res.json({ locked: convo.locked });
});

// ── Clean chat ────────────────────────────────────────────────────────────────
router.delete('/conversations/:id/messages', protect, async (req, res) => {
  const convo = await Conversation.findOne({ _id: req.params.id, participants: req.user._id });
  if (!convo) return res.status(403).json({ error: 'Not a member' });
  await Message.deleteMany({ conversationId: req.params.id });
  convo.lastMessage = null;
  await convo.save();
  res.json({ message: 'Chat cleared' });
});

// ── Helpers ───────────────────────────────────────────────────────────────────
async function updateConversationLastMessage(convo, msg, senderId) {
  convo.lastMessage   = msg._id;
  convo.lastMessageAt = new Date();
  convo.messageCount  = (convo.messageCount || 0) + 1;

  // Increment unread for all participants except sender
  for (const pid of convo.participants) {
    if (pid.toString() === senderId.toString()) continue;
    const entry = convo.unreadCounts.find(u => u.userId?.toString() === pid.toString());
    if (entry) entry.count += 1;
    else convo.unreadCounts.push({ userId: pid, count: 1 });
  }
  await convo.save();
}

function emitToConversation(convoId, event, data, exceptUserId) {
  const io = getSocket();
  if (!io) return;
  io.to(convoId.toString()).emit(event, data);
}

module.exports = router;
