const passport = require('passport');
const jwt      = require('jsonwebtoken');
const User     = require('../models/User');

// ── JWT auth middleware ───────────────────────────────────────────────────────
const protect = (req, res, next) => {
  passport.authenticate('jwt', { session: false }, (err, user, info) => {
    if (err)   return next(err);
    if (!user) return res.status(401).json({ error: 'Unauthorized — invalid or expired token' });
    req.user = user;
    next();
  })(req, res, next);
};

// ── Optional auth (attach user if token present, don't fail if absent) ────────
const optionalAuth = (req, res, next) => {
  passport.authenticate('jwt', { session: false }, (err, user) => {
    if (user) req.user = user;
    next();
  })(req, res, next);
};

// ── Token generator ───────────────────────────────────────────────────────────
const signToken = (userId) =>
  jwt.sign({ id: userId }, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRES_IN || '30d',
  });

const signRefreshToken = (userId) =>
  jwt.sign({ id: userId }, process.env.REFRESH_TOKEN_SECRET, { expiresIn: '90d' });

module.exports = { protect, optionalAuth, signToken, signRefreshToken };
