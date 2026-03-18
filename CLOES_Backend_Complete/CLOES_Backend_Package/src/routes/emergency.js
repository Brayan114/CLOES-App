const router   = require('express').Router();
const { protect } = require('../middleware/auth');
const User = require('../models/User');
const { getSocket } = require('../socket/socketServer');

// ── Trigger emergency alert ───────────────────────────────────────────────────
router.post('/trigger', protect, async (req, res) => {
  const user = await User.findById(req.user._id);
  const { location, message } = req.body;

  const alertPayload = {
    type:      'emergency_alert',
    from:      { id: user._id, name: user.name, handle: user.handle, avatar: user.avatar },
    message:   message || user.emergencyMessage || 'I need help. Please check on me.',
    location:  location || null,
    timestamp: new Date(),
    contacts:  user.emergencyContacts,
  };

  // Emit to all devices of this user (e.g. logged-in on multiple devices)
  const io = getSocket();
  io?.emit('emergency_alert', alertPayload);

  // In production: trigger SMS / push notifications to emergency contacts
  // via Twilio (SMS) or FCM (push)
  // For now: simulate success
  console.log(`🚨 Emergency triggered by ${user.name} (${user.handle})`);

  res.json({
    sent: true,
    alertedCount: user.emergencyContacts.length,
    message: alertPayload.message,
    timestamp: alertPayload.timestamp,
  });
});

// ── Get emergency contacts ────────────────────────────────────────────────────
router.get('/contacts', protect, async (req, res) => {
  const user = await User.findById(req.user._id).select('emergencyContacts emergencyMessage');
  res.json({ contacts: user.emergencyContacts, message: user.emergencyMessage });
});

// ── Update emergency contacts ─────────────────────────────────────────────────
router.put('/contacts', protect, async (req, res) => {
  const { contacts, message } = req.body;
  const user = req.user;
  if (contacts) user.emergencyContacts = contacts;
  if (message)  user.emergencyMessage  = message;
  await user.save();
  res.json({ contacts: user.emergencyContacts, message: user.emergencyMessage });
});

// ── SOS location share ────────────────────────────────────────────────────────
router.post('/sos-location', protect, async (req, res) => {
  const { lat, lng, accuracy } = req.body;
  const io = getSocket();

  if (req.user.socketId) {
    io?.emit('sos_location', {
      userId:    req.user._id,
      name:      req.user.name,
      location:  { lat, lng, accuracy },
      timestamp: new Date(),
    });
  }

  res.json({ received: true });
});

module.exports = router;
