require('dotenv').config();
require('express-async-errors');

const express    = require('express');
const http       = require('http');
const cors       = require('cors');
const helmet     = require('helmet');
const morgan     = require('morgan');
const compression = require('compression');
const cookieParser = require('cookie-parser');
const passport   = require('passport');
const path       = require('path');

const connectDB  = require('./config/db');
const { initSocket } = require('./socket/socketServer');
const { startCoinCron } = require('./services/coinService');
const { startBloomCron } = require('./services/bloomService');
const errorHandler = require('./middleware/errorHandler');

// ── Route imports ─────────────────────────────────────────────────────────────
const authRoutes      = require('./routes/auth');
const userRoutes      = require('./routes/users');
const contactRoutes   = require('./routes/contacts');
const messageRoutes   = require('./routes/messages');
const groupRoutes     = require('./routes/groups');
const groupChatRoutes = require('./routes/groupChats');
const vibeRoutes      = require('./routes/vibes');
const mediaRoutes     = require('./routes/media');
const coinRoutes      = require('./routes/coins');
const qrRoutes        = require('./routes/qr');
const bloomRoutes     = require('./routes/bloom');
const emergencyRoutes = require('./routes/emergency');
const callRoutes      = require('./routes/calls');
const museRoutes      = require('./routes/muse');
const exportRoutes    = require('./routes/exports');
const adminRoutes     = require('./routes/admin');

require('./config/passport');

const app    = express();
const server = http.createServer(app);

// ── DB ────────────────────────────────────────────────────────────────────────
connectDB();

// ── Socket.io ─────────────────────────────────────────────────────────────────
initSocket(server);

// ── Middleware ────────────────────────────────────────────────────────────────
app.use(helmet({
  crossOriginResourcePolicy: { policy: 'cross-origin' },
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      scriptSrc: ["'self'", "'unsafe-inline'", "'unsafe-eval'", "https://cdnjs.cloudflare.com", "https://unpkg.com"],
      styleSrc: ["'self'", "'unsafe-inline'", "https://fonts.googleapis.com"],
      fontSrc: ["'self'", "https://fonts.gstatic.com"],
      imgSrc: ["'self'", "data:", "https:", "blob:"],
      connectSrc: ["'self'", "https:", "wss:"],
    },
  },
}));
app.use(cors({
  origin: function (origin, cb) {
    // Allow: no origin (mobile apps, local HTML files), localhost, and configured CLIENT_URL
    if (!origin || origin === 'null' || /localhost/.test(origin) || origin === process.env.CLIENT_URL) return cb(null, true);
    cb(null, true); // Allow all in dev; restrict in prod if needed
  },
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Session-Id']
}));
app.use(compression());
app.use(morgan(process.env.NODE_ENV === 'production' ? 'combined' : 'dev'));
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));
app.use(cookieParser());
app.use(passport.initialize());

// Serve uploaded files locally (dev only — use Cloudinary in prod)
app.use('/uploads', express.static(path.join(__dirname, '..', 'uploads')));

// Serve admin portal
app.use(express.static(path.join(__dirname, '..', 'public')));

// ── Routes ────────────────────────────────────────────────────────────────────
app.use('/api/auth',        authRoutes);
app.use('/api/users',       userRoutes);
app.use('/api/contacts',    contactRoutes);
app.use('/api/messages',    messageRoutes);
app.use('/api/groups',      groupRoutes);
app.use('/api/group-chats', groupChatRoutes);
app.use('/api/vibes',       vibeRoutes);
app.use('/api/media',       mediaRoutes);
app.use('/api/coins',       coinRoutes);
app.use('/api/qr',          qrRoutes);
app.use('/api/bloom',       bloomRoutes);
app.use('/api/emergency',   emergencyRoutes);
app.use('/api/calls',       callRoutes);
app.use('/api/muse',        museRoutes);
app.use('/api/exports',     exportRoutes);
app.use('/api/admin',       adminRoutes);

// ── Health check ──────────────────────────────────────────────────────────────
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', version: '1.0.0', timestamp: new Date().toISOString() });
});

// ── Error handler ─────────────────────────────────────────────────────────────
app.use(errorHandler);

// ── Cron jobs ─────────────────────────────────────────────────────────────────
startCoinCron();
startBloomCron();

// ── Start ─────────────────────────────────────────────────────────────────────
const PORT = process.env.PORT || 5000;
server.listen(PORT, () => {
  console.log(`\n🚀 CLOES Backend running on port ${PORT}`);
  console.log(`🌍 Environment: ${process.env.NODE_ENV}`);
  console.log(`🔗 Health: http://localhost:${PORT}/api/health\n`);
});

module.exports = { app, server };
