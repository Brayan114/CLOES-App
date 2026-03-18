const router   = require('express').Router();
const { protect } = require('../middleware/auth');
const { BloomEntry } = require('../models/Analytics');
const Conversation = require('../models/Conversation');
const { calcBloomScore, calcUrgency } = require('../services/bloomService');

// ── Get all bloom entries for current user ────────────────────────────────────
router.get('/', protect, async (req, res) => {
  const entries = await BloomEntry.find({ user: req.user._id })
    .populate('partner', 'name handle avatar paletteColors online')
    .populate('conversation', 'type name lastMessageAt connectionDays urgency')
    .sort({ score: -1 });
  res.json(entries);
});

// ── Get bloom for a specific conversation ─────────────────────────────────────
router.get('/conversation/:id', protect, async (req, res) => {
  const entry = await BloomEntry.findOne({
    conversation: req.params.id,
    user: req.user._id,
  }).populate('partner', 'name handle avatar');
  if (!entry) return res.json({ score: 50, urgency: 'low' });
  res.json(entry);
});

// ── Get top bloom connections (for Bloom page) ────────────────────────────────
router.get('/top', protect, async (req, res) => {
  const entries = await BloomEntry.find({ user: req.user._id })
    .populate('partner', 'name handle avatar paletteColors lightSeed online')
    .sort({ score: -1 })
    .limit(10);
  res.json(entries);
});

// ── Get urgency tint status (are any chats at HIGH urgency?) ──────────────────
router.get('/urgency-status', protect, async (req, res) => {
  const highEntry = await BloomEntry.findOne({ user: req.user._id, urgency: 'high' });
  res.json({ urgencyTintOn: !!highEntry, urgency: highEntry?.urgency || 'low' });
});

module.exports = router;
