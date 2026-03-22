const router   = require('express').Router();
const { protect } = require('../middleware/auth');
const mongoose = require('mongoose');
const axios    = require('axios');

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
  date:  { type: String },
  day:   { type: String },
  time:  { type: String },
  year:  { type: String },
  month: { type: String },
  dayNum: { type: Number },
}, { timestamps: true });

const MuseSession = mongoose.model('MuseSession', MuseSessionSchema);

// ── Groq config ───────────────────────────────────────────────────────────────
const GROQ_URL = 'https://api.groq.com/openai/v1/chat/completions';
const GROQ_MODEL = 'llama-3.3-70b-versatile';

const MUSE_SYSTEM = `You are Muse ✦ — CLOES's built-in AI companion. You're warm, witty, fashionable, and emotionally intelligent. You speak like a cool best friend who's always hyped to help. Keep responses concise but impactful. Use emojis sparingly but naturally. You help with:
- General chat & advice
- Creative ideas & writing
- Emotional support & motivation
- Tech & homework help
- Anything the user asks
Never break character. You ARE Muse, not an "AI assistant".`;

const DRESS_SYSTEM = `You are Muse Dress ✦ — CLOES's fashion AI. You analyze outfit photos and give styling advice. You're like a fashionable best friend who's honest but kind. Give specific, actionable feedback on:
- Color coordination & palette harmony
- Fit & silhouette
- Occasion appropriateness
- Accessory suggestions
- Alternative styling ideas
Keep responses short (2-3 paragraphs max). Be encouraging but real.`;

// ── Chat with Muse (Groq) ────────────────────────────────────────────────────
router.post('/chat', protect, async (req, res) => {
  const { message, history = [], type = 'muse', imageUrl } = req.body;

  if (!message && !imageUrl) {
    return res.status(400).json({ error: 'Message or image required' });
  }

  if (!process.env.GROQ_API_KEY) {
    return res.status(503).json({ error: 'Muse AI not configured — add GROQ_API_KEY to .env' });
  }

  // Build conversation history for Groq
  const messages = [
    { role: 'system', content: type === 'dress' ? DRESS_SYSTEM : MUSE_SYSTEM },
  ];

  // Include recent history (last 20 messages max)
  const recent = history.slice(-20);
  for (const msg of recent) {
    messages.push({
      role: msg.role === 'muse' ? 'assistant' : 'user',
      content: msg.text || '',
    });
  }

  // Add current message
  let userContent = message || '';
  if (imageUrl) {
    userContent += `\n\n[User shared an image: ${imageUrl}]`;
  }
  messages.push({ role: 'user', content: userContent });

  try {
    const response = await axios.post(GROQ_URL, {
      model: GROQ_MODEL,
      messages,
      temperature: 0.8,
      max_tokens: 1024,
      top_p: 0.9,
    }, {
      headers: {
        'Authorization': `Bearer ${process.env.GROQ_API_KEY}`,
        'Content-Type': 'application/json',
      },
      timeout: 30000,
    });

    const reply = response.data.choices?.[0]?.message?.content || 'Hmm, I lost my train of thought ✦';

    res.json({
      reply,
      model: GROQ_MODEL,
      usage: response.data.usage,
    });
  } catch (err) {
    console.error('Groq error:', err.response?.data || err.message);
    const status = err.response?.status || 500;
    const msg = status === 429
      ? 'Muse is taking a breather — too many requests. Try again in a sec ✦'
      : 'Muse had a hiccup. Try again! ✦';
    res.status(status).json({ error: msg });
  }
});

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
