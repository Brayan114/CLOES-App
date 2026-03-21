const router       = require('express').Router();
const isAdmin      = require('../middleware/isAdmin');
const User         = require('../models/User');
const Message      = require('../models/Message');
const Conversation = require('../models/Conversation');
const VibeVideo    = require('../models/VibeVideo');
const { CoinTransaction } = require('../models/Analytics');

// All routes require admin auth
router.use(isAdmin);

// ══ DASHBOARD STATS ══════════════════════════════════════════════════════════
router.get('/stats', async (req, res) => {
  const now   = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const weekAgo = new Date(Date.now() - 7 * 86400000);

  const [
    users, onlineUsers, newUsersToday,
    messages, messagesToday,
    vibes, conversations, coins,
    userGrowth, msgActivity, authBreakdown, vibeCategories,
  ] = await Promise.all([
    User.countDocuments(),
    User.countDocuments({ online: true }),
    User.countDocuments({ createdAt: { $gte: today } }),
    Message.countDocuments(),
    Message.countDocuments({ createdAt: { $gte: today } }),
    VibeVideo.countDocuments(),
    Conversation.countDocuments(),
    CoinTransaction.aggregate([{ $match: { type: 'earn' } }, { $group: { _id: null, total: { $sum: '$amount' } } }]),
    // User growth last 7 days
    User.aggregate([
      { $match: { createdAt: { $gte: weekAgo } } },
      { $group: { _id: { $dateToString: { format: '%Y-%m-%d', date: '$createdAt' } }, count: { $sum: 1 } } },
      { $sort: { _id: 1 } },
    ]),
    // Message activity last 7 days
    Message.aggregate([
      { $match: { createdAt: { $gte: weekAgo } } },
      { $group: { _id: { $dateToString: { format: '%Y-%m-%d', date: '$createdAt' } }, count: { $sum: 1 } } },
      { $sort: { _id: 1 } },
    ]),
    // Auth provider breakdown
    User.aggregate([
      { $group: { _id: '$authProvider', count: { $sum: 1 } } },
    ]),
    // Vibe categories
    VibeVideo.aggregate([
      { $group: { _id: '$category', count: { $sum: 1 } } },
      { $sort: { count: -1 } },
      { $limit: 10 },
    ]),
  ]);

  res.json({
    totals: {
      users,
      onlineUsers,
      newUsersToday,
      messages,
      messagesToday,
      vibes,
      conversations,
      coins: coins[0]?.total || 0,
    },
    charts: {
      userGrowth,
      msgActivity,
      authBreakdown,
      vibeCategories,
    },
  });
});

// ══ USERS ════════════════════════════════════════════════════════════════════
router.get('/users', async (req, res) => {
  const { page = 1, limit = 20, search = '', filter = '' } = req.query;
  const skip = (page - 1) * limit;

  const query = {};
  if (search) {
    const re = new RegExp(search, 'i');
    query.$or = [{ name: re }, { handle: re }, { email: re }];
  }
  if (filter === 'online') query.online = true;
  if (filter === 'google') query.authProvider = 'google';
  if (filter === 'local')  query.authProvider = 'local';
  if (filter === 'admin')  query.isAdmin = true;

  const [users, total] = await Promise.all([
    User.find(query).select('-password -cloesedKey -__v').sort({ createdAt: -1 }).skip(skip).limit(Number(limit)),
    User.countDocuments(query),
  ]);

  res.json({ users, total });
});

router.patch('/users/:id', async (req, res) => {
  const allowed = ['isAdmin', 'banned', 'name', 'handle'];
  const updates = {};
  allowed.forEach(k => { if (req.body[k] !== undefined) updates[k] = req.body[k]; });

  const user = await User.findByIdAndUpdate(req.params.id, updates, { new: true }).select('-password -cloesedKey');
  if (!user) return res.status(404).json({ error: 'User not found' });
  res.json({ user });
});

router.post('/users/:id/ban', async (req, res) => {
  const { banned, reason } = req.body;
  const user = await User.findByIdAndUpdate(req.params.id, { banned }, { new: true }).select('-password -cloesedKey');
  if (!user) return res.status(404).json({ error: 'User not found' });
  res.json({ user });
});

router.post('/users/:id/coins', async (req, res) => {
  const { amount, reason = 'Admin adjustment' } = req.body;
  const user = await User.findById(req.params.id);
  if (!user) return res.status(404).json({ error: 'User not found' });

  user.coinBalance = (user.coinBalance || 0) + Number(amount);
  if (amount > 0) user.totalCoinsEarned = (user.totalCoinsEarned || 0) + Number(amount);
  await user.save();

  await CoinTransaction.create({
    user: user._id,
    amount: Number(amount),
    reason,
    balance: user.coinBalance,
    type: amount >= 0 ? 'earn' : 'spend',
  });

  res.json({ user: user.toPublicJSON() });
});

router.delete('/users/:id', async (req, res) => {
  const user = await User.findByIdAndDelete(req.params.id);
  if (!user) return res.status(404).json({ error: 'User not found' });
  res.json({ success: true });
});

// ══ MESSAGES ═════════════════════════════════════════════════════════════════
router.get('/messages', async (req, res) => {
  const { page = 1, limit = 30, search = '' } = req.query;
  const skip = (page - 1) * limit;

  const query = {};
  if (search) {
    query.text = new RegExp(search, 'i');
  }

  const [messages, total] = await Promise.all([
    Message.find(query)
      .populate('sender', 'name handle avatar')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(Number(limit)),
    Message.countDocuments(query),
  ]);

  res.json({ messages, total });
});

router.delete('/messages/:id', async (req, res) => {
  const msg = await Message.findByIdAndDelete(req.params.id);
  if (!msg) return res.status(404).json({ error: 'Message not found' });
  res.json({ success: true });
});

// ══ VIBES ════════════════════════════════════════════════════════════════════
router.get('/vibes', async (req, res) => {
  const { page = 1, limit = 18, search = '' } = req.query;
  const skip = (page - 1) * limit;

  const query = {};
  if (search) {
    query.title = new RegExp(search, 'i');
  }

  const [vibes, total] = await Promise.all([
    VibeVideo.find(query)
      .populate('creator', 'name handle avatar')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(Number(limit)),
    VibeVideo.countDocuments(query),
  ]);

  res.json({ vibes, total });
});

router.patch('/vibes/:id/status', async (req, res) => {
  const { status } = req.body;
  const vibe = await VibeVideo.findByIdAndUpdate(req.params.id, { status }, { new: true });
  if (!vibe) return res.status(404).json({ error: 'Vibe not found' });
  res.json({ vibe });
});

router.delete('/vibes/:id', async (req, res) => {
  const vibe = await VibeVideo.findByIdAndDelete(req.params.id);
  if (!vibe) return res.status(404).json({ error: 'Vibe not found' });
  res.json({ success: true });
});

// ══ COINS ════════════════════════════════════════════════════════════════════
router.get('/coins', async (req, res) => {
  const { page = 1, limit = 40 } = req.query;
  const skip = (page - 1) * limit;

  const [summary, topEarners, transactions, total] = await Promise.all([
    CoinTransaction.aggregate([
      { $group: { _id: '$type', total: { $sum: '$amount' }, count: { $sum: 1 } } },
    ]),
    User.find().sort({ totalCoinsEarned: -1 }).limit(10).select('name handle avatar totalCoinsEarned coinBalance'),
    CoinTransaction.find()
      .populate('user', 'name handle avatar')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(Number(limit)),
    CoinTransaction.countDocuments(),
  ]);

  res.json({ summary, topEarners, transactions, total });
});

module.exports = router;
