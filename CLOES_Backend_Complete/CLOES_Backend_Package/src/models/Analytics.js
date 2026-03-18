const mongoose = require('mongoose');

// ── Coin Transactions ─────────────────────────────────────────────────────────
const CoinTransactionSchema = new mongoose.Schema({
  user:    { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  amount:  { type: Number, required: true },        // positive = earned, negative = spent
  reason:  { type: String, required: true },
  balance: { type: Number, required: true },         // balance after transaction
  type:    { type: String, enum: ['earn', 'spend', 'bonus'], default: 'earn' },
  meta:    { type: mongoose.Schema.Types.Mixed },
}, { timestamps: true });

// ── Bloom Entries ─────────────────────────────────────────────────────────────
const BloomEntrySchema = new mongoose.Schema({
  conversation: { type: mongoose.Schema.Types.ObjectId, ref: 'Conversation', required: true },
  user:         { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  partner:      { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },

  // Raw stats
  messagesSent:     { type: Number, default: 0 },
  messagesReceived: { type: Number, default: 0 },
  mediaShared:      { type: Number, default: 0 },
  callMinutes:      { type: Number, default: 0 },
  streakDays:       { type: Number, default: 0 },
  lastMessageAt:    { type: Date, default: null },

  // Computed bloom score 0-100
  score:   { type: Number, default: 50 },
  urgency: { type: String, enum: ['low', 'mid', 'high'], default: 'low' },

  date: { type: Date, default: Date.now },
}, { timestamps: true });

BloomEntrySchema.index({ conversation: 1, user: 1 }, { unique: true });

module.exports = {
  CoinTransaction: mongoose.model('CoinTransaction', CoinTransactionSchema),
  BloomEntry:      mongoose.model('BloomEntry', BloomEntrySchema),
};
