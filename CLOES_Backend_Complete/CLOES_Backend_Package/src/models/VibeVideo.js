const mongoose = require('mongoose');

const CommentSchema = new mongoose.Schema({
  author:  { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  text:    { type: String, required: true, maxlength: 500 },
  likes:   [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],
}, { timestamps: true });

const VibeVideoSchema = new mongoose.Schema({
  creator:     { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  title:       { type: String, required: true, trim: true, maxlength: 100 },
  description: { type: String, default: '', maxlength: 500 },
  category:    { type: String, default: 'MOOD', uppercase: true },

  // ── Media ────────────────────────────────────────────────────────────────────
  videoUrl:        { type: String, required: true },
  videoPublicId:   { type: String, default: '' },
  thumbnailUrl:    { type: String, default: '' },
  thumbnailPublicId: { type: String, default: '' },
  duration:        { type: Number, default: 0 },  // seconds
  fileSize:        { type: Number, default: 0 },

  // ── Stats ────────────────────────────────────────────────────────────────────
  views:    { type: Number, default: 0 },
  likes:    [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],
  comments: [CommentSchema],

  // ── Visual ───────────────────────────────────────────────────────────────────
  paletteColors: [{ type: String }],  // hex colors for card gradient

  // ── Status ───────────────────────────────────────────────────────────────────
  status:    { type: String, enum: ['processing', 'ready', 'failed'], default: 'processing' },
  isPublic:  { type: Boolean, default: true },

}, { timestamps: true });

VibeVideoSchema.index({ creator: 1 });
VibeVideoSchema.index({ createdAt: -1 });
VibeVideoSchema.index({ category: 1, createdAt: -1 });

module.exports = mongoose.model('VibeVideo', VibeVideoSchema);
