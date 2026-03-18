const router   = require('express').Router();
const { protect } = require('../middleware/auth');
const { uploadChatMedia, uploadDocument, uploadAny } = require('../config/cloudinary');
const path = require('path');
const fs   = require('fs');
const https = require('https');

// ── Generic media upload (Muse Dress, profile, etc.) ─────────────────────────
router.post('/upload', protect, uploadAny.single('file'), async (req, res) => {
  if (!req.file) return res.status(400).json({ error: 'No file' });
  res.json({
    url:       req.file.path || req.file.secure_url || `/uploads/misc/${req.file.filename}`,
    publicId:  req.file.filename || req.file.public_id || '',
    mimeType:  req.file.mimetype,
    fileName:  req.file.originalname,
    size:      req.file.size,
  });
});

// ── Upload multiple files ─────────────────────────────────────────────────────
router.post('/upload-many', protect, uploadChatMedia.array('files', 10), async (req, res) => {
  if (!req.files?.length) return res.status(400).json({ error: 'No files' });
  const results = req.files.map(f => ({
    url:      f.path || f.secure_url || `/uploads/chat/${f.filename}`,
    publicId: f.filename || f.public_id || '',
    mimeType: f.mimetype,
    fileName: f.originalname,
    size:     f.size,
  }));
  res.json(results);
});

// ── "Save to gallery" — returns download URL for mobile to save ───────────────
// Android app calls this, gets back a signed download URL
router.post('/save-to-gallery', protect, async (req, res) => {
  const { mediaUrl, fileName } = req.body;
  if (!mediaUrl) return res.status(400).json({ error: 'mediaUrl required' });

  // If Cloudinary URL — return it with download attachment header hint
  if (mediaUrl.includes('cloudinary.com')) {
    // Append fl_attachment to force download
    const downloadUrl = mediaUrl.replace('/upload/', '/upload/fl_attachment/');
    return res.json({ downloadUrl, fileName: fileName || 'cloes_media', method: 'cloudinary' });
  }

  // For local files — return the direct URL
  res.json({ downloadUrl: mediaUrl, fileName: fileName || 'cloes_media', method: 'direct' });
});

// ── Save to CLOES Echo (vault) ────────────────────────────────────────────────
router.post('/save-to-echo', protect, async (req, res) => {
  const { mediaUrl, mediaType, fileName } = req.body;
  if (!mediaUrl) return res.status(400).json({ error: 'mediaUrl required' });

  // In production: store encrypted reference in user's echo vault
  // For now: return confirmed
  res.json({
    saved: true,
    echoEntry: { url: mediaUrl, type: mediaType, name: fileName, savedAt: new Date() }
  });
});

// ── Export chat history as JSON ───────────────────────────────────────────────
// (Full export is in /exports route — this is the chat-level one)
router.get('/chat-export/:conversationId', protect, async (req, res) => {
  const Message      = require('../models/Message');
  const Conversation = require('../models/Conversation');

  const convo = await Conversation.findOne({
    _id: req.params.conversationId,
    participants: req.user._id
  }).populate('participants', 'name handle');

  if (!convo) return res.status(403).json({ error: 'Not a member' });

  const messages = await Message.find({ conversationId: req.params.conversationId })
    .populate('sender', 'name handle')
    .sort({ createdAt: 1 });

  const exportData = {
    exportedAt: new Date().toISOString(),
    conversation: {
      id: convo._id,
      type: convo.type,
      name: convo.name,
      participants: convo.participants,
    },
    messages: messages.map(m => ({
      from:      m.sender?.name,
      handle:    m.sender?.handle,
      text:      m.text,
      type:      m.type,
      mediaUrl:  m.mediaUrl,
      timestamp: m.createdAt,
    }))
  };

  res.setHeader('Content-Disposition', `attachment; filename="cloes-chat-${convo._id}.json"`);
  res.setHeader('Content-Type', 'application/json');
  res.json(exportData);
});

// ── Link preview scraper ──────────────────────────────────────────────────────
router.post('/link-preview', protect, async (req, res) => {
  const { url } = req.body;
  if (!url) return res.status(400).json({ error: 'URL required' });

  try {
    const axios = require('axios');
    const response = await axios.get(url, {
      timeout: 5000,
      headers: { 'User-Agent': 'CLOESBot/1.0' },
      maxRedirects: 3,
    });

    const html = response.data;
    const title  = html.match(/<title[^>]*>([^<]+)<\/title>/i)?.[1] || '';
    const ogImg  = html.match(/<meta[^>]+property="og:image"[^>]+content="([^"]+)"/i)?.[1] || '';
    const ogDesc = html.match(/<meta[^>]+property="og:description"[^>]+content="([^"]+)"/i)?.[1] || '';
    const favicon = `https://www.google.com/s2/favicons?domain=${new URL(url).hostname}`;

    res.json({ url, title, image: ogImg, description: ogDesc, favicon });
  } catch (e) {
    res.json({ url, title: url, image: '', description: '', favicon: '' });
  }
});

module.exports = router;
