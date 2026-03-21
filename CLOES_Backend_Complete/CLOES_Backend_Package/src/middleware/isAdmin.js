const passport = require('passport');

/**
 * Middleware stack: authenticate JWT then check isAdmin.
 * Usage:  router.get('/admin/stats', isAdmin, handler)
 */
const isAdmin = [
  passport.authenticate('jwt', { session: false }),
  (req, res, next) => {
    if (!req.user || !req.user.isAdmin) {
      return res.status(403).json({ error: 'Admin access required' });
    }
    next();
  },
];

module.exports = isAdmin;
