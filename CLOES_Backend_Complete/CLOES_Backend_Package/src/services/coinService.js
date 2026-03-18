// ── Coin Service ──────────────────────────────────────────────────────────────
const cron = require('node-cron');
const User = require('../models/User');
const { CoinTransaction } = require('../models/Analytics');

const COINS_PER_10_MINS = parseInt(process.env.COINS_PER_10_MINS) || 5;

// Award coins to a user
const awardCoins = async (userId, amount, reason, type = 'earn') => {
  try {
    const user = await User.findById(userId);
    if (!user) return null;

    user.coinBalance     += amount;
    user.totalCoinsEarned = (user.totalCoinsEarned || 0) + (amount > 0 ? amount : 0);
    await user.save();

    const tx = await CoinTransaction.create({
      user: userId, amount, reason, type,
      balance: user.coinBalance,
    });

    // Emit real-time coin update
    const { getSocket } = require('../socket/socketServer');
    const io = getSocket();
    if (io && user.socketId) {
      io.to(user.socketId).emit('coin_update', {
        balance:   user.coinBalance,
        earned:    amount,
        reason,
        timestamp: new Date(),
      });
    }

    return tx;
  } catch (err) {
    console.error('awardCoins error:', err.message);
  }
};

const spendCoins = async (userId, amount, reason) => {
  const user = await User.findById(userId);
  if (!user || user.coinBalance < amount) {
    throw new Error('Insufficient coin balance');
  }
  return awardCoins(userId, -amount, reason, 'spend');
};

// ── Cron: award coins every 10 minutes to active users ───────────────────────
const startCoinCron = () => {
  cron.schedule('*/10 * * * *', async () => {
    try {
      // Users who are online and had a session start
      const tenMinsAgo = new Date(Date.now() - 10 * 60 * 1000);
      const activeUsers = await User.find({
        online: true,
        activeSessionStart: { $lte: new Date() },
        $or: [
          { lastCoinAward: null },
          { lastCoinAward: { $lte: tenMinsAgo } }
        ]
      });

      let count = 0;
      for (const user of activeUsers) {
        await awardCoins(user._id, COINS_PER_10_MINS, '10 min active session');
        user.lastCoinAward = new Date();
        await user.save();
        count++;
      }

      if (count > 0) console.log(`💰 Awarded ${COINS_PER_10_MINS} coins to ${count} active users`);
    } catch (err) {
      console.error('Coin cron error:', err.message);
    }
  });

  console.log('💰 Coin cron started — awards every 10 minutes');
};

module.exports = { awardCoins, spendCoins, startCoinCron };
