const router   = require('express').Router();
const { protect } = require('../middleware/auth');
const { CoinTransaction } = require('../models/Analytics');
const { awardCoins, spendCoins } = require('../services/coinService');
const User = require('../models/User');

// ── Get balance + history ─────────────────────────────────────────────────────
router.get('/', protect, async (req, res) => {
  const user = await User.findById(req.user._id).select('coinBalance totalCoinsEarned');
  const history = await CoinTransaction.find({ user: req.user._id })
    .sort({ createdAt: -1 })
    .limit(50);

  res.json({ balance: user.coinBalance, totalEarned: user.totalCoinsEarned, history });
});

// ── Award coins (session heartbeat every 10 min from app) ────────────────────
router.post('/heartbeat', protect, async (req, res) => {
  const user = req.user;
  const now  = new Date();
  const tenMinsAgo = new Date(now.getTime() - 10 * 60 * 1000);

  // Only award if last award was 10+ minutes ago
  if (!user.lastCoinAward || user.lastCoinAward <= tenMinsAgo) {
    const amount = parseInt(process.env.COINS_PER_10_MINS) || 5;
    await awardCoins(user._id, amount, '10 min active session');
    user.lastCoinAward = now;
    await user.save();
    return res.json({ awarded: amount, balance: user.coinBalance + amount });
  }

  res.json({ awarded: 0, balance: user.coinBalance, nextAwardIn: Math.ceil((user.lastCoinAward.getTime() + 600000 - now.getTime()) / 1000) });
});

// ── Spend coins for free browsing data ───────────────────────────────────────
router.post('/spend-browsing', protect, async (req, res) => {
  const { coins } = req.body;
  if (!coins || coins < 1) return res.status(400).json({ error: 'Invalid coin amount' });

  await spendCoins(req.user._id, coins, 'Free browsing data');
  const mbGranted = coins * (parseInt(process.env.COINS_FREE_BROWSING_MB_PER_COIN) || 10);

  res.json({
    spent: coins,
    mbGranted,
    balance: req.user.coinBalance - coins,
    message: `${mbGranted} MB of free browsing unlocked ✦`,
  });
});

// ── Manual earn (admin or bonus actions) ─────────────────────────────────────
router.post('/earn', protect, async (req, res) => {
  const { amount, reason } = req.body;
  if (!amount || amount < 1 || amount > 100) return res.status(400).json({ error: 'Invalid amount' });
  const tx = await awardCoins(req.user._id, amount, reason || 'Bonus');
  res.json(tx);
});

module.exports = router;
