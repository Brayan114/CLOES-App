const router   = require('express').Router();
const passport = require('passport');
const jwt      = require('jsonwebtoken');
const { body, validationResult } = require('express-validator');
const User     = require('../models/User');
const { protect, signToken, signRefreshToken } = require('../middleware/auth');
const { generateQR } = require('../services/qrService');

// ── Register (email + password) ───────────────────────────────────────────────
router.post('/register', [
  body('name').trim().notEmpty().withMessage('Name required'),
  body('email').isEmail().normalizeEmail(),
  body('password').isLength({ min: 6 }).withMessage('Min 6 characters'),
], async (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

  const { name, email, password, paletteColors, lightSeed } = req.body;

  const exists = await User.findOne({ email });
  if (exists) return res.status(409).json({ error: 'Email already registered' });

  const handle = await generateUniqueHandle(name);
  const user = await User.create({
    name, email, password, handle,
    paletteColors: paletteColors || [],
    lightSeed: lightSeed || Math.random(),
    authProvider: 'local',
  });

  // Generate QR code for user profile
  user.qrCode = await generateQR(user._id.toString());
  await user.save();

  const token = signToken(user._id);
  res.status(201).json({ token, user: user.toPublicJSON() });
});

// ── Login ─────────────────────────────────────────────────────────────────────
router.post('/login', [
  body('email').isEmail().normalizeEmail(),
  body('password').notEmpty(),
], async (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

  const { email, password } = req.body;
  const user = await User.findOne({ email }).select('+password');
  if (!user || !user.password) return res.status(401).json({ error: 'Invalid credentials' });

  const match = await user.comparePassword(password);
  if (!match) return res.status(401).json({ error: 'Invalid credentials' });

  user.online = true;
  user.lastSeen = new Date();
  await user.save();

  const token = signToken(user._id);
  res.json({ token, user: user.toPublicJSON() });
});

// ── Google OAuth — initiate ───────────────────────────────────────────────────
router.get('/google',
  passport.authenticate('google', { scope: ['profile', 'email'], session: false })
);

// ── Google OAuth — callback ───────────────────────────────────────────────────
router.get('/google/callback',
  passport.authenticate('google', { session: false, failureRedirect: '/api/auth/google/failed' }),
  async (req, res) => {
    const user = req.user;

    // Generate QR if new user
    if (!user.qrCode) {
      user.qrCode = await generateQR(user._id.toString());
      await user.save();
    }

    const token = signToken(user._id);

    // For mobile: redirect with token in query param (deep link)
    // The Android app should handle cloes://auth?token=...
    const onboarded = user.onboardingDone ? '1' : '0';
    res.redirect(`cloes://auth?token=${token}&onboarded=${onboarded}`);
  }
);

router.get('/google/failed', (req, res) =>
  res.status(401).json({ error: 'Google authentication failed' })
);

// ── Refresh token ─────────────────────────────────────────────────────────────
router.post('/refresh', async (req, res) => {
  const { refreshToken } = req.body;
  if (!refreshToken) return res.status(400).json({ error: 'Refresh token required' });
  try {
    const payload = jwt.verify(refreshToken, process.env.REFRESH_TOKEN_SECRET);
    const user = await User.findById(payload.id);
    if (!user) return res.status(401).json({ error: 'User not found' });
    const token = signToken(user._id);
    const newRefresh = signRefreshToken(user._id);
    res.json({ token, refreshToken: newRefresh });
  } catch {
    res.status(401).json({ error: 'Invalid refresh token' });
  }
});

// ── Complete onboarding (save palette + handle) ───────────────────────────────
router.post('/onboard', protect, async (req, res) => {
  const { name, handle, paletteColors, lightSeed } = req.body;
  const user = req.user;

  if (handle) {
    const taken = await User.findOne({ handle, _id: { $ne: user._id } });
    if (taken) return res.status(409).json({ error: 'Handle already taken' });
    user.handle = handle.toLowerCase().replace(/\s+/g, '.');
  }

  if (name)           user.name          = name;
  if (paletteColors)  user.paletteColors = paletteColors;
  if (lightSeed)      user.lightSeed     = lightSeed;

  user.onboardingDone = true;
  if (!user.qrCode) user.qrCode = await generateQR(user._id.toString());
  await user.save();

  res.json({ user: user.toPublicJSON() });
});

// ── Get current user ──────────────────────────────────────────────────────────
router.get('/me', protect, (req, res) => {
  res.json({ user: req.user.toPublicJSON() });
});

// ── Logout ────────────────────────────────────────────────────────────────────
router.post('/logout', protect, async (req, res) => {
  req.user.online = false;
  req.user.lastSeen = new Date();
  req.user.socketId = '';
  await req.user.save();
  res.json({ message: 'Logged out' });
});

// ── Delete account ────────────────────────────────────────────────────────────
router.delete('/account', protect, async (req, res) => {
  await User.findByIdAndDelete(req.user._id);
  res.json({ message: 'Account deleted' });
});

// ── Helper ────────────────────────────────────────────────────────────────────
async function generateUniqueHandle(name) {
  const base = name.toLowerCase().replace(/\s+/g, '.').replace(/[^a-z0-9.]/g, '');
  let handle = base;
  let i = 0;
  while (await User.findOne({ handle })) {
    handle = `${base}.${Math.floor(Math.random() * 9000) + 1000}`;
    if (++i > 10) break;
  }
  return handle;
}

module.exports = router;
