// ── groups.js (Priority Circles) ─────────────────────────────────────────────
const router   = require('express').Router();
const { protect } = require('../middleware/auth');

// Simple in-memory approach — in production store in User document
// For now these are synced from the app

router.get('/', protect, (req, res) => {
  res.json([]);  // App manages groups locally; backend used for sync
});

module.exports = router;
