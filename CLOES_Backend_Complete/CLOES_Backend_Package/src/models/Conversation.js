const mongoose = require('mongoose');

const ConversationSchema = new mongoose.Schema({
  type:        { type: String, enum: ['direct', 'group'], default: 'direct' },
  participants: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],

  // ── Direct chat fields ───────────────────────────────────────────────────────
  locked:      { type: Boolean, default: false },
  pinned:      [{ type: mongoose.Schema.Types.ObjectId }],  // user IDs who pinned
  urgency:     { type: String, enum: ['low', 'mid', 'high'], default: 'low' },
  disappearSecs: { type: Number, default: 0 },
  wallpaperUrl: { type: String, default: '' },    // per-chat background

  // ── Group chat fields ────────────────────────────────────────────────────────
  name:        { type: String, default: '' },
  description: { type: String, default: '' },
  groupAvatar: { type: String, default: '' },
  groupAvatarPublicId: { type: String, default: '' },
  admin:       { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
  circleId:    { type: String, default: '' },

  // ── Bloom tracking ───────────────────────────────────────────────────────────
  messageCount:    { type: Number, default: 0 },
  lastInteraction: { type: Date, default: Date.now },
  connectionDays:  { type: Number, default: 0 },  // streak

  // ── Last message (denormalized for list view) ────────────────────────────────
  lastMessage:   { type: mongoose.Schema.Types.ObjectId, ref: 'Message' },
  lastMessageAt: { type: Date, default: Date.now },

  // ── Unread counts per user ───────────────────────────────────────────────────
  unreadCounts: [{
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    count:  { type: Number, default: 0 },
  }],

}, { timestamps: true });

ConversationSchema.index({ participants: 1 });
ConversationSchema.index({ lastMessageAt: -1 });

module.exports = mongoose.model('Conversation', ConversationSchema);
