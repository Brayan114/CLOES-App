const router   = require('express').Router();
const { protect } = require('../middleware/auth');
const User = require('../models/User');
const { getSocket } = require('../socket/socketServer');
const { updateBloomOnCall } = require('../services/bloomService');
const Conversation = require('../models/Conversation');

// ── Initiate a call ───────────────────────────────────────────────────────────
router.post('/initiate', protect, async (req, res) => {
  const { targetUserId, conversationId, callType = 'voice' } = req.body;

  const target = await User.findById(targetUserId).select('socketId name online');
  if (!target) return res.status(404).json({ error: 'User not found' });
  if (!target.online || !target.socketId) {
    return res.status(400).json({ error: 'User is offline' });
  }

  const io = getSocket();
  const callData = {
    callId:         `call_${Date.now()}`,
    caller:         { id: req.user._id, name: req.user.name, handle: req.user.handle, avatar: req.user.avatar },
    conversationId,
    callType,       // 'voice' | 'video'
    timestamp:      new Date(),
    // ICE / TURN server config for the Android app
    iceServers: [
      { urls: 'stun:stun.l.google.com:19302' },
      { urls: 'stun:stun1.l.google.com:19302' },
      ...(process.env.TURN_SERVER_URL ? [{
        urls:       process.env.TURN_SERVER_URL,
        username:   process.env.TURN_USERNAME,
        credential: process.env.TURN_CREDENTIAL,
      }] : []),
    ],
  };

  // Send incoming call event to target
  io?.to(target.socketId).emit('incoming_call', callData);

  res.json({ callId: callData.callId, status: 'ringing', iceServers: callData.iceServers });
});

// ── Answer a call ─────────────────────────────────────────────────────────────
router.post('/answer', protect, async (req, res) => {
  const { callId, callerId, accept } = req.body;

  const caller = await User.findById(callerId).select('socketId name');
  if (!caller) return res.status(404).json({ error: 'Caller not found' });

  const io = getSocket();
  io?.to(caller.socketId).emit(accept ? 'call_accepted' : 'call_rejected', {
    callId, answeredBy: { id: req.user._id, name: req.user.name }
  });

  res.json({ callId, accepted: accept });
});

// ── End a call ────────────────────────────────────────────────────────────────
router.post('/end', protect, async (req, res) => {
  const { callId, targetUserId, conversationId, durationSecs = 0 } = req.body;

  const target = await User.findById(targetUserId).select('socketId');
  const io = getSocket();

  if (target?.socketId) {
    io?.to(target.socketId).emit('call_ended', { callId, duration: durationSecs });
  }

  // Update bloom scores
  if (conversationId && durationSecs > 0) {
    const durationMins = durationSecs / 60;
    await updateBloomOnCall(conversationId, req.user._id, durationMins);
    await updateBloomOnCall(conversationId, targetUserId, durationMins);
  }

  res.json({ callId, duration: durationSecs, ended: true });
});

// ── WebRTC signaling passthrough ──────────────────────────────────────────────
router.post('/signal', protect, async (req, res) => {
  const { targetUserId, signal, callId } = req.body;

  const target = await User.findById(targetUserId).select('socketId');
  if (!target?.socketId) return res.status(400).json({ error: 'Target offline' });

  const io = getSocket();
  io?.to(target.socketId).emit('webrtc_signal', {
    from: req.user._id,
    callId,
    signal,
  });

  res.json({ delivered: true });
});

// ── Get ICE server config ──────────────────────────────────────────────────────
router.get('/ice-servers', protect, (req, res) => {
  const iceServers = [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' },
  ];
  if (process.env.TURN_SERVER_URL) {
    iceServers.push({
      urls:       process.env.TURN_SERVER_URL,
      username:   process.env.TURN_USERNAME,
      credential: process.env.TURN_CREDENTIAL,
    });
  }
  res.json({ iceServers });
});

module.exports = router;
