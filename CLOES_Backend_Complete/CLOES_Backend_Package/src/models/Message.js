const mongoose = require('mongoose');

const PollDataSchema = new mongoose.Schema({
  question: String,
  options:  [String],
  votes:    [{ type: Number, default: 0 }],
  voters:   [{ userId: mongoose.Schema.Types.ObjectId, optionIndex: Number }],
}, { _id: false });

const MessageSchema = new mongoose.Schema({
  conversationId: { type: mongoose.Schema.Types.ObjectId, ref: 'Conversation', required: true, index: true },
  sender:         { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },

  // ── Content ──────────────────────────────────────────────────────────────────
  text:           { type: String, default: '' },
  type:           {
    type: String,
    enum: ['text', 'image', 'video', 'audio', 'document', 'sticker', 'poll', 'link', 'deleted'],
    default: 'text'
  },

  // ── Media ────────────────────────────────────────────────────────────────────
  mediaUrl:       { type: String, default: '' },
  mediaPublicId:  { type: String, default: '' },
  mediaType:      { type: String, default: '' },  // mime type
  fileName:       { type: String, default: '' },
  fileSize:       { type: Number, default: 0 },
  thumbnailUrl:   { type: String, default: '' },
  duration:       { type: Number, default: 0 },   // seconds for audio/video

  // ── Poll ─────────────────────────────────────────────────────────────────────
  pollData:       { type: PollDataSchema },

  // ── Reactions ────────────────────────────────────────────────────────────────
  reactions: [{
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    emoji:  String,
  }],

  // ── Meta ─────────────────────────────────────────────────────────────────────
  replyTo:        { type: mongoose.Schema.Types.ObjectId, ref: 'Message', default: null },
  readBy:         [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],
  deliveredTo:    [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],
  expiresAt:      { type: Date, default: null },  // disappearing
  edited:         { type: Boolean, default: false },
  deleted:        { type: Boolean, default: false },

}, { timestamps: true });

// Auto-delete expired messages
MessageSchema.index({ expiresAt: 1 }, { expireAfterSeconds: 0 });
MessageSchema.index({ conversationId: 1, createdAt: -1 });

module.exports = mongoose.model('Message', MessageSchema);
