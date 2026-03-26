const mongoose = require('mongoose');
const bcrypt   = require('bcryptjs');

const EmergencyContactSchema = new mongoose.Schema({
  name:  { type: String, required: true },
  phone: { type: String },
  type:  { type: String, enum: ['FAMILY', 'FRIEND', 'DOCTOR', 'OTHER'], default: 'FRIEND' },
}, { _id: false });

const UserSchema = new mongoose.Schema({
  // ── Identity ────────────────────────────────────────────────────────────────
  name:         { type: String, required: true, trim: true },
  handle:       { type: String, required: true, unique: true, trim: true, lowercase: true },
  email:        { type: String, unique: true, sparse: true, lowercase: true, trim: true },
  password:     { type: String, select: false },
  googleId:     { type: String, unique: true, sparse: true },
  authProvider: { type: String, enum: ['local', 'google'], default: 'local' },

  // ── Profile ─────────────────────────────────────────────────────────────────
  bio:          { type: String, default: '', maxlength: 200 },
  avatar:       { type: String, default: '' },      // URL
  avatarPublicId: { type: String, default: '' },    // Cloudinary public ID
  paletteColors: [{ type: String }],               // hex strings e.g. ["#FF3385", ...]
  lightSeed:    { type: Number, default: 0.42 },
  onboardingDone: { type: Boolean, default: false },

  // ── Settings ─────────────────────────────────────────────────────────────────
  theme:                { type: String, default: 'Default' },
  font:                 { type: String, default: 'Default' },
  bubbleSent1:          { type: String, default: '#8B5CF6' },
  bubbleSent2:          { type: String, default: '#FF3385' },
  notificationsEnabled: { type: Boolean, default: true },
  globalMuse:           { type: Boolean, default: false },
  lang:                 { type: String, default: 'en' },
  disappearingNavEnabled: { type: Boolean, default: true },
  fcmToken:             { type: String, default: '' },   // Firebase push token

  // ── Coins ────────────────────────────────────────────────────────────────────
  coinBalance:     { type: Number, default: 50 },
  totalCoinsEarned: { type: Number, default: 0 },
  activeSessionStart: { type: Date, default: null },  // for 10-min tracking
  lastCoinAward:   { type: Date, default: null },

  // ── CLOESED Key (hashed) ────────────────────────────────────────────────────
  cloesedKey:      { type: String, default: '', select: false },

  // ── Emergency ────────────────────────────────────────────────────────────────
  emergencyContacts: [EmergencyContactSchema],
  emergencyMessage: { type: String, default: 'I need help. Please check on me.' },

  // ── QR ───────────────────────────────────────────────────────────────────────
  qrCode:          { type: String, default: '' },  // base64 or URL

  // ── Bloom score ──────────────────────────────────────────────────────────────
  bloomScore:      { type: Number, default: 50 },

  // ── Status ───────────────────────────────────────────────────────────────────
  online:      { type: Boolean, default: false },
  lastSeen:    { type: Date, default: Date.now },
  socketId:    { type: String, default: '' },

  // ── Admin ───────────────────────────────────────────────────────────────
  isAdmin:     { type: Boolean, default: false },
  banned:      { type: Boolean, default: false },

}, { timestamps: true });

// ── Password hashing ──────────────────────────────────────────────────────────
UserSchema.pre('save', async function (next) {
  if (!this.isModified('password') || !this.password) return next();
  this.password = await bcrypt.hash(this.password, 12);
  next();
});

UserSchema.methods.comparePassword = async function (plain) {
  return bcrypt.compare(plain, this.password);
};

UserSchema.methods.toPublicJSON = function () {
  const obj = this.toObject();
  delete obj.password;
  delete obj.cloesedKey;
  delete obj.googleId;
  delete obj.__v;
  return obj;
};

module.exports = mongoose.model('User', UserSchema);
