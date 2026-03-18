const { Server } = require('socket.io');
const jwt  = require('jsonwebtoken');
const User = require('../models/User');
const Conversation = require('../models/Conversation');

let io = null;

const initSocket = (server) => {
  io = new Server(server, {
    cors: {
      origin: ['http://localhost:3000', process.env.CLIENT_URL, 'http://10.0.2.2:5000'],
      methods: ['GET', 'POST'],
      credentials: true,
    },
    pingTimeout: 60000,
    maxHttpBufferSize: 50e6,  // 50MB for media
  });

  // ── Auth middleware ──────────────────────────────────────────────────────────
  io.use(async (socket, next) => {
    try {
      const token = socket.handshake.auth?.token || socket.handshake.query?.token;
      if (!token) return next(new Error('Authentication required'));

      const payload = jwt.verify(token, process.env.JWT_SECRET);
      const user    = await User.findById(payload.id).select('-password -cloesedKey');
      if (!user) return next(new Error('User not found'));

      socket.user = user;
      next();
    } catch (err) {
      next(new Error('Invalid token'));
    }
  });

  // ── Connection ───────────────────────────────────────────────────────────────
  io.on('connection', async (socket) => {
    const user = socket.user;
    console.log(`🟢 Connected: ${user.name} (${socket.id})`);

    // Mark online
    await User.findByIdAndUpdate(user._id, {
      online: true,
      socketId: socket.id,
      lastSeen: new Date(),
      activeSessionStart: new Date(),
    });

    // Join all conversation rooms
    const convos = await Conversation.find({ participants: user._id }).select('_id');
    convos.forEach(c => socket.join(c._id.toString()));

    // Broadcast online status to contacts
    socket.broadcast.emit('user_online', { userId: user._id, online: true });

    // ── Messaging ──────────────────────────────────────────────────────────────
    socket.on('join_conversation', (conversationId) => {
      socket.join(conversationId);
    });

    socket.on('leave_conversation', (conversationId) => {
      socket.leave(conversationId);
    });

    // ── Typing indicators ──────────────────────────────────────────────────────
    socket.on('typing_start', ({ conversationId }) => {
      socket.to(conversationId).emit('typing_start', {
        conversationId,
        user: { id: user._id, name: user.name, handle: user.handle },
      });
    });

    socket.on('typing_stop', ({ conversationId }) => {
      socket.to(conversationId).emit('typing_stop', {
        conversationId, userId: user._id,
      });
    });

    // ── Read receipts ─────────────────────────────────────────────────────────
    socket.on('messages_read', async ({ conversationId }) => {
      socket.to(conversationId).emit('messages_read', {
        conversationId, readBy: user._id,
      });
      // Update DB
      await User.findById(user._id);  // ensure user exists
      await Conversation.updateOne(
        { _id: conversationId, 'unreadCounts.userId': user._id },
        { $set: { 'unreadCounts.$.count': 0 } }
      );
    });

    // ── WebRTC Signaling ──────────────────────────────────────────────────────
    socket.on('call_offer', async ({ targetUserId, offer, callId, callType }) => {
      const target = await User.findById(targetUserId).select('socketId name');
      if (!target?.socketId) return;

      io.to(target.socketId).emit('incoming_call', {
        callId,
        callType,
        offer,
        caller: { id: user._id, name: user.name, handle: user.handle, avatar: user.avatar },
        iceServers: getIceServers(),
      });
    });

    socket.on('call_answer', async ({ callerId, answer, callId }) => {
      const caller = await User.findById(callerId).select('socketId');
      if (!caller?.socketId) return;
      io.to(caller.socketId).emit('call_answered', { callId, answer });
    });

    socket.on('call_ice_candidate', async ({ targetUserId, candidate, callId }) => {
      const target = await User.findById(targetUserId).select('socketId');
      if (!target?.socketId) return;
      io.to(target.socketId).emit('ice_candidate', { callId, candidate, from: user._id });
    });

    socket.on('call_reject', async ({ callerId, callId }) => {
      const caller = await User.findById(callerId).select('socketId');
      if (!caller?.socketId) return;
      io.to(caller.socketId).emit('call_rejected', { callId, by: user._id });
    });

    socket.on('call_end', async ({ targetUserId, callId, durationSecs }) => {
      const target = await User.findById(targetUserId).select('socketId');
      if (target?.socketId) {
        io.to(target.socketId).emit('call_ended', { callId, duration: durationSecs });
      }
    });

    // ── Emergency ─────────────────────────────────────────────────────────────
    socket.on('emergency_sos', async ({ message, location }) => {
      const fullUser = await User.findById(user._id).select('name handle emergencyContacts');
      socket.broadcast.emit('emergency_alert', {
        from: { id: fullUser._id, name: fullUser.name, handle: fullUser.handle },
        message: message || 'I need help!',
        location,
        contacts: fullUser.emergencyContacts,
        timestamp: new Date(),
      });
    });

    // ── Coin heartbeat ────────────────────────────────────────────────────────
    socket.on('session_heartbeat', async () => {
      const freshUser = await User.findById(user._id);
      if (!freshUser) return;

      const now = new Date();
      const tenMinsAgo = new Date(now - 10 * 60 * 1000);

      if (!freshUser.lastCoinAward || freshUser.lastCoinAward <= tenMinsAgo) {
        const amount = parseInt(process.env.COINS_PER_10_MINS) || 5;
        freshUser.coinBalance     += amount;
        freshUser.lastCoinAward   = now;
        await freshUser.save();

        socket.emit('coin_update', {
          balance:   freshUser.coinBalance,
          earned:    amount,
          reason:    '10 min active session',
          timestamp: now,
        });
      }
    });

    // ── Presence ping ─────────────────────────────────────────────────────────
    socket.on('ping_presence', () => {
      socket.emit('pong_presence', { ts: Date.now() });
    });

    // ── Disconnect ─────────────────────────────────────────────────────────────
    socket.on('disconnect', async (reason) => {
      console.log(`🔴 Disconnected: ${user.name} — ${reason}`);
      await User.findByIdAndUpdate(user._id, {
        online:    false,
        lastSeen:  new Date(),
        socketId:  '',
        activeSessionStart: null,
      });
      socket.broadcast.emit('user_online', { userId: user._id, online: false, lastSeen: new Date() });
    });
  });

  console.log('🔌 Socket.io initialized');
  return io;
};

const getSocket = () => io;

const getIceServers = () => {
  const servers = [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' },
  ];
  if (process.env.TURN_SERVER_URL) {
    servers.push({
      urls:       process.env.TURN_SERVER_URL,
      username:   process.env.TURN_USERNAME,
      credential: process.env.TURN_CREDENTIAL,
    });
  }
  return servers;
};

module.exports = { initSocket, getSocket };
