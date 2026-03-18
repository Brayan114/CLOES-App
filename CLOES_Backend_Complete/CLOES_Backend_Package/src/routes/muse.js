const router   = require('express').Router();
const { protect } = require('../middleware/auth');
const mongoose = require('mongoose');

// ── Muse History schema ───────────────────────────────────────────────────────
const MuseSessionSchema = new mongoose.Schema({
  user:    { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  type:    { type: String, enum: ['muse', 'dress'], default: 'muse' },
  preview: { type: String, required: true },
  messages: [{
    role:    { type: String, enum: ['user', 'muse'] },
    text:    String,
    mediaUrl: String,
    ts:      { type: Date, default: Date.now },
  }],
  date:  { type: String },  // "Mar 10"
  day:   { type: String },  // "Monday"
  time:  { type: String },  // "9:41 AM"
  year:  { type: String },  // "2026"
  month: { type: String },  // "March"
  dayNum: { type: Number }, // 10
}, { timestamps: true });

const MuseSession = mongoose.model('MuseSession', MuseSessionSchema);

// ── Save muse session ─────────────────────────────────────────────────────────
router.post('/sessions', protect, async (req, res) => {
  const { type, preview, messages } = req.body;
  const now = new Date();
  const months = ['January','February','March','April','May','June',
                  'July','August','September','October','November','December'];
  const days   = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];

  const session = await MuseSession.create({
    user:     req.user._id,
    type:     type || 'muse',
    preview:  preview || 'Muse session',
    messages: messages || [],
    date:  `${months[now.getMonth()].slice(0,3)} ${now.getDate()}`,
    day:   days[now.getDay()],
    time:  `${now.getHours()}:${now.getMinutes().toString().padStart(2,'0')}`,
    year:  now.getFullYear().toString(),
    month: months[now.getMonth()],
    dayNum: now.getDate(),
  });

  res.status(201).json(session);
});

// ── Get all muse sessions ─────────────────────────────────────────────────────
router.get('/sessions', protect, async (req, res) => {
  const { type } = req.query;
  const filter = { user: req.user._id };
  if (type) filter.type = type;

  const sessions = await MuseSession.find(filter).sort({ createdAt: -1 }).limit(200);
  res.json(sessions);
});

// ── Get session by id ─────────────────────────────────────────────────────────
router.get('/sessions/:id', protect, async (req, res) => {
  const session = await MuseSession.findOne({ _id: req.params.id, user: req.user._id });
  if (!session) return res.status(404).json({ error: 'Not found' });
  res.json(session);
});

// ── Delete session ────────────────────────────────────────────────────────────
router.delete('/sessions/:id', protect, async (req, res) => {
  await MuseSession.findOneAndDelete({ _id: req.params.id, user: req.user._id });
  res.json({ deleted: true });
});

// ── Muse dress — track image usage ───────────────────────────────────────────
router.get('/dress/usage', protect, async (req, res) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const count = await MuseSession.countDocuments({
    user: req.user._id, type: 'dress',
    createdAt: { $gte: today }
  });
  res.json({ usedToday: count, limit: 10, remaining: Math.max(0, 10 - count) });
});

module.exports = router;
