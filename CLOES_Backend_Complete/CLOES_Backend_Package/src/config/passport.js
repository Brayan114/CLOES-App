const passport        = require('passport');
const GoogleStrategy  = require('passport-google-oauth20').Strategy;
const { Strategy: JwtStrategy, ExtractJwt } = require('passport-jwt');
const User = require('../models/User');

// ── JWT Strategy ──────────────────────────────────────────────────────────────
passport.use(new JwtStrategy(
  {
    jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
    secretOrKey: process.env.JWT_SECRET,
  },
  async (payload, done) => {
    try {
      const user = await User.findById(payload.id).select('-password');
      if (!user) return done(null, false);
      return done(null, user);
    } catch (err) {
      return done(err, false);
    }
  }
));

// ── Google OAuth Strategy ────────────────────────────────────────────────────
passport.use(new GoogleStrategy(
  {
    clientID:     process.env.GOOGLE_CLIENT_ID,
    clientSecret: process.env.GOOGLE_CLIENT_SECRET,
    callbackURL:  process.env.GOOGLE_CALLBACK_URL,
    scope: ['profile', 'email'],
  },
  async (accessToken, refreshToken, profile, done) => {
    try {
      // Find existing user by googleId or email
      let user = await User.findOne({
        $or: [
          { googleId: profile.id },
          { email: profile.emails?.[0]?.value }
        ]
      });

      if (user) {
        // Update google info if linking
        if (!user.googleId) {
          user.googleId = profile.id;
          user.avatar   = user.avatar || profile.photos?.[0]?.value;
          await user.save();
        }
        return done(null, user);
      }

      // Create new user from Google profile
      user = await User.create({
        googleId:    profile.id,
        email:       profile.emails?.[0]?.value,
        name:        profile.displayName,
        handle:      generateHandle(profile.displayName),
        avatar:      profile.photos?.[0]?.value,
        authProvider: 'google',
        onboardingDone: false,
      });

      return done(null, user);
    } catch (err) {
      return done(err, false);
    }
  }
));

function generateHandle(name) {
  const base = name.toLowerCase().replace(/\s+/g, '.').replace(/[^a-z0-9.]/g, '');
  return `${base}.${Math.floor(Math.random() * 9000) + 1000}`;
}
