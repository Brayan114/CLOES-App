const router       = require('express').Router();
const Conversation = require('../models/Conversation');
const Message      = require('../models/Message');
const { protect }  = require('../middleware/auth');
const { uploadAvatar } = require('../config/cloudinary');
const { getSocket } = require('../socket/socketServer');

// ── Create group chat ─────────────────────────────────────────────────────────
router.post('/', protect, async (req, res) => {
  const { name, description, memberIds } = req.body;
  if (!name) return res.status(400).json({ error: 'Group name required' });

  const participants = [req.user._id, ...(memberIds || [])];
  const convo = await Conversation.create({
    type: 'group',
    name, description,
    participants,
    admin: req.user._id,
  });
  await convo.populate('participants', 'name handle avatar paletteColors online');
  res.status(201).json(convo);
});

// ── Get group chats ───────────────────────────────────────────────────────────
router.get('/', protect, async (req, res) => {
  const groups = await Conversation.find({ type: 'group', participants: req.user._id })
    .populate('participants', 'name handle avatar paletteColors online')
    .populate('lastMessage')
    .sort({ lastMessageAt: -1 });
  res.json(groups);
});

// ── Update group (name, description) ─────────────────────────────────────────
router.patch('/:id', protect, async (req, res) => {
  const convo = await Conversation.findOne({ _id: req.params.id, participants: req.user._id });
  if (!convo) return res.status(404).json({ error: 'Not found' });

  const { name, description, circleId } = req.body;
  if (name)        convo.name        = name;
  if (description) convo.description = description;
  if (circleId)    convo.circleId    = circleId;
  await convo.save();

  const io = getSocket();
  io?.to(req.params.id).emit('group_updated', convo);
  res.json(convo);
});

// ── Upload group avatar ───────────────────────────────────────────────────────
router.post('/:id/avatar', protect, uploadAvatar.single('avatar'), async (req, res) => {
  if (!req.file) return res.status(400).json({ error: 'No file' });
  const url = req.file.path || req.file.secure_url || `/uploads/avatars/${req.file.filename}`;
  const convo = await Conversation.findOneAndUpdate(
    { _id: req.params.id, participants: req.user._id },
    { groupAvatar: url },
    { new: true }
  );
  res.json({ groupAvatar: convo.groupAvatar });
});

// ── Add member ────────────────────────────────────────────────────────────────
router.post('/:id/members', protect, async (req, res) => {
  const { userId } = req.body;
  const convo = await Conversation.findOneAndUpdate(
    { _id: req.params.id, participants: req.user._id },
    { $addToSet: { participants: userId } },
    { new: true }
  ).populate('participants', 'name handle avatar');
  res.json(convo);
});

// ── Remove member ─────────────────────────────────────────────────────────────
router.delete('/:id/members/:userId', protect, async (req, res) => {
  const convo = await Conversation.findOneAndUpdate(
    { _id: req.params.id, $or: [{ admin: req.user._id }, { participants: req.params.userId }] },
    { $pull: { participants: req.params.userId } },
    { new: true }
  ).populate('participants', 'name handle avatar');
  if (!convo) return res.status(403).json({ error: 'Forbidden' });
  res.json(convo);
});

// ── Leave group ───────────────────────────────────────────────────────────────
router.post('/:id/leave', protect, async (req, res) => {
  await Conversation.findByIdAndUpdate(req.params.id, {
    $pull: { participants: req.user._id }
  });
  res.json({ left: true });
});

// ── Send message to group ─────────────────────────────────────────────────────
router.post('/:id/messages', protect, async (req, res) => {
  const convo = await Conversation.findOne({ _id: req.params.id, participants: req.user._id });
  if (!convo) return res.status(403).json({ error: 'Not a member' });

  const msg = await Message.create({
    conversationId: req.params.id,
    sender: req.user._id,
    text:   req.body.text,
    type:   req.body.type || 'text',
  });
  await msg.populate('sender', 'name handle avatar');

  convo.lastMessage   = msg._id;
  convo.lastMessageAt = new Date();
  await convo.save();

  const io = getSocket();
  io?.to(req.params.id).emit('new_message', msg);
  res.status(201).json(msg);
});

module.exports = router;
