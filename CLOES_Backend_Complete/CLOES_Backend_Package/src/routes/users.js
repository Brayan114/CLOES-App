const router = require('express').Router();
const User   = require('../models/User');
const { protect } = require('../middleware/auth');
const { uploadAvatar, deleteFromCloudinary } = require('../config/cloudinary');
const { generateQR } = require('../services/qrService');

// ── Get user profile ──────────────────────────────────────────────────────────
router.get('/:id', protect, async (req, res) => {
  const user = await User.findById(req.params.id).select('-password -cloesedKey');
  if (!user) return res.status(404).json({ error: 'User not found' });
  res.json(user.toPublicJSON());
});

// ── Search users by handle or name ───────────────────────────────────────────
router.get('/search/:query', protect, async (req, res) => {
  const q = req.params.query.toLowerCase();
  const users = await User.find({
    _id: { $ne: req.user._id },
    $or: [
      { handle: { $regex: q, $options: 'i' } },
      { name:   { $regex: q, $options: 'i' } }
    ]
  }).select('name handle avatar paletteColors lightSeed online lastSeen').limit(20);
  res.json(users);
});

// ── Update profile ────────────────────────────────────────────────────────────
router.patch('/me', protect, async (req, res) => {
  const allowed = ['name', 'bio', 'theme', 'font', 'bubbleSent1', 'bubbleSent2',
    'notificationsEnabled', 'globalMuse', 'lang', 'disappearingNavEnabled',
    'paletteColors', 'lightSeed', 'emergencyMessage', 'fcmToken'];

  const updates = {};
  allowed.forEach(k => { if (req.body[k] !== undefined) updates[k] = req.body[k]; });

  // Handle update
  if (req.body.handle) {
    const h = req.body.handle.toLowerCase().replace(/\s+/g, '.');
    const taken = await User.findOne({ handle: h, _id: { $ne: req.user._id } });
    if (taken) return res.status(409).json({ error: 'Handle already taken' });
    updates.handle = h;
  }

  const user = await User.findByIdAndUpdate(req.user._id, updates, { new: true, runValidators: true });
  res.json(user.toPublicJSON());
});

// ── Upload avatar ─────────────────────────────────────────────────────────────
router.post('/me/avatar', protect, uploadAvatar.single('avatar'), async (req, res) => {
  if (!req.file) return res.status(400).json({ error: 'No file uploaded' });

  const user = req.user;

  // Delete old avatar from Cloudinary
  if (user.avatarPublicId) await deleteFromCloudinary(user.avatarPublicId, 'image');

  user.avatar        = req.file.path || req.file.secure_url || req.file.location;
  user.avatarPublicId = req.file.filename || req.file.public_id || '';
  await user.save();

  res.json({ avatar: user.avatar, message: 'Avatar updated' });
});

// ── Delete avatar (reset to Fragment art) ────────────────────────────────────
router.delete('/me/avatar', protect, async (req, res) => {
  const user = req.user;
  if (user.avatarPublicId) await deleteFromCloudinary(user.avatarPublicId, 'image');
  user.avatar = '';
  user.avatarPublicId = '';
  await user.save();
  res.json({ message: 'Avatar removed' });
});

// ── Update palette colors (onboarding or settings) ───────────────────────────
router.post('/me/palette', protect, async (req, res) => {
  const { paletteColors, lightSeed } = req.body;
  if (!Array.isArray(paletteColors) || paletteColors.length < 3) {
    return res.status(400).json({ error: 'Provide at least 3 hex color strings' });
  }
  req.user.paletteColors = paletteColors;
  if (lightSeed !== undefined) req.user.lightSeed = lightSeed;
  await req.user.save();
  res.json({ paletteColors: req.user.paletteColors, lightSeed: req.user.lightSeed });
});

// ── Get/regen QR code ────────────────────────────────────────────────────────
router.get('/me/qr', protect, async (req, res) => {
  if (!req.user.qrCode || req.query.refresh === '1') {
    req.user.qrCode = await generateQR(req.user._id.toString());
    await req.user.save();
  }
  res.json({ qrCode: req.user.qrCode });
});

// ── Update CLOESED key ────────────────────────────────────────────────────────
router.post('/me/cloesed-key', protect, async (req, res) => {
  const { key } = req.body;
  if (!key || key.length < 4) return res.status(400).json({ error: 'Key must be at least 4 chars' });
  const user = await User.findById(req.user._id).select('+cloesedKey');
  user.cloesedKey = key;  // stored plain (it's a display key, not a secret password)
  await user.save();
  res.json({ message: 'CLOESED key saved' });
});

// ── Verify CLOESED key ────────────────────────────────────────────────────────
router.post('/me/verify-cloesed-key', protect, async (req, res) => {
  const { key } = req.body;
  const user = await User.findById(req.user._id).select('+cloesedKey');
  const valid = user.cloesedKey === key;
  res.json({ valid });
});

// ── Update emergency contacts ─────────────────────────────────────────────────
router.post('/me/emergency-contacts', protect, async (req, res) => {
  const { contacts } = req.body;  // [{ name, phone, type }]
  req.user.emergencyContacts = contacts;
  await req.user.save();
  res.json({ emergencyContacts: req.user.emergencyContacts });
});

// ── Update FCM token for push notifications ───────────────────────────────────
router.post('/me/fcm-token', protect, async (req, res) => {
  const { fcmToken } = req.body;
  req.user.fcmToken = fcmToken;
  await req.user.save();
  res.json({ message: 'FCM token updated' });
});

// ── Set active session start (for coin tracking) ──────────────────────────────
router.post('/me/session-start', protect, async (req, res) => {
  req.user.activeSessionStart = new Date();
  await req.user.save();
  res.json({ sessionStart: req.user.activeSessionStart });
});

module.exports = router;
