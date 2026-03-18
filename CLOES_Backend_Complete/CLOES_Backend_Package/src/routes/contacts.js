// ── contacts.js ───────────────────────────────────────────────────────────────
const router   = require('express').Router();
const { protect } = require('../middleware/auth');
const User = require('../models/User');
const Conversation = require('../models/Conversation');

// Search by handle/name
router.get('/search', protect, async (req, res) => {
  const { q } = req.query;
  if (!q) return res.json([]);
  const users = await User.find({
    _id: { $ne: req.user._id },
    $or: [
      { handle: { $regex: q, $options: 'i' } },
      { name:   { $regex: q, $options: 'i' } },
    ]
  }).select('name handle avatar paletteColors lightSeed online lastSeen').limit(20);
  res.json(users);
});

// Get contacts (all users the current user has conversations with)
router.get('/', protect, async (req, res) => {
  const convos = await Conversation.find({
    type: 'direct',
    participants: req.user._id
  }).populate('participants', 'name handle avatar paletteColors lightSeed online lastSeen');

  const contacts = convos
    .map(c => c.participants.find(p => p._id.toString() !== req.user._id.toString()))
    .filter(Boolean);

  res.json(contacts);
});

module.exports = router;
