const router   = require('express').Router();
const { protect } = require('../middleware/auth');
const { generateQR, generateQRBuffer } = require('../services/qrService');
const User = require('../models/User');

// ── Get my QR code (base64) ───────────────────────────────────────────────────
router.get('/me', protect, async (req, res) => {
  const user = await User.findById(req.user._id);
  if (!user.qrCode || req.query.refresh === '1') {
    user.qrCode = await generateQR(user._id.toString());
    await user.save();
  }
  res.json({ qrCode: user.qrCode, userId: user._id });
});

// ── Get my QR as PNG image ────────────────────────────────────────────────────
router.get('/me/image', protect, async (req, res) => {
  const buf = await generateQRBuffer(req.user._id.toString());
  res.setHeader('Content-Type', 'image/png');
  res.setHeader('Content-Disposition', `attachment; filename="cloes-qr-${req.user.handle}.png"`);
  res.send(buf);
});

// ── Scan/resolve a QR code ────────────────────────────────────────────────────
router.post('/scan', protect, async (req, res) => {
  const { data } = req.body;
  if (!data) return res.status(400).json({ error: 'QR data required' });

  try {
    const parsed = JSON.parse(data);
    if (parsed.type !== 'cloes_profile' || !parsed.userId) {
      return res.status(400).json({ error: 'Invalid CLOES QR code' });
    }
    const user = await User.findById(parsed.userId)
      .select('name handle avatar paletteColors lightSeed bio online');
    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json({ user });
  } catch {
    res.status(400).json({ error: 'Could not parse QR code' });
  }
});

module.exports = router;
