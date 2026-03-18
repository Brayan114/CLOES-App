const router     = require('express').Router();
const VibeVideo  = require('../models/VibeVideo');
const { protect } = require('../middleware/auth');
const { uploadVibe, uploadChatMedia } = require('../config/cloudinary');
const { awardCoins } = require('../services/coinService');
const { getSocket } = require('../socket/socketServer');

// ── Get vibe feed (paginated) ─────────────────────────────────────────────────
router.get('/', protect, async (req, res) => {
  const { page = 1, limit = 20, category } = req.query;
  const filter = { status: 'ready', isPublic: true };
  if (category) filter.category = category.toUpperCase();

  const vibes = await VibeVideo.find(filter)
    .populate('creator', 'name handle avatar paletteColors')
    .sort({ createdAt: -1 })
    .skip((page - 1) * limit)
    .limit(Number(limit));

  const total = await VibeVideo.countDocuments(filter);
  res.json({ vibes, total, page: Number(page), pages: Math.ceil(total / limit) });
});

// ── Get single vibe ───────────────────────────────────────────────────────────
router.get('/:id', protect, async (req, res) => {
  const vibe = await VibeVideo.findById(req.params.id)
    .populate('creator', 'name handle avatar')
    .populate('comments.author', 'name handle avatar');
  if (!vibe) return res.status(404).json({ error: 'Not found' });

  // Increment view count
  vibe.views += 1;
  await vibe.save();

  res.json(vibe);
});

// ── Upload vibe video ─────────────────────────────────────────────────────────
router.post('/upload',
  protect,
  uploadVibe.fields([
    { name: 'vibe', maxCount: 1 },
    { name: 'thumbnail', maxCount: 1 }
  ]),
  async (req, res) => {
    if (!req.files?.vibe?.[0]) return res.status(400).json({ error: 'Video file required' });

    const video = req.files.vibe[0];
    const thumb = req.files?.thumbnail?.[0];

    const vibe = await VibeVideo.create({
      creator:     req.user._id,
      title:       req.body.title || 'Untitled Vibe',
      description: req.body.description || '',
      category:    (req.body.category || 'MOOD').toUpperCase(),
      videoUrl:    video.path || video.secure_url || `/uploads/vibes/${video.filename}`,
      videoPublicId: video.filename || video.public_id || '',
      thumbnailUrl:  thumb ? (thumb.path || thumb.secure_url || `/uploads/vibes/${thumb.filename}`) : '',
      fileSize:    video.size,
      status:      'ready',
      paletteColors: req.body.paletteColors ? JSON.parse(req.body.paletteColors) : ['#FF3385', '#8B5CF6'],
    });

    await vibe.populate('creator', 'name handle avatar');

    // Award coins for uploading
    await awardCoins(req.user._id, 5, 'Vibe video uploaded');

    // Broadcast to all online users
    const io = getSocket();
    io?.emit('new_vibe', vibe);

    res.status(201).json(vibe);
  }
);

// ── Like / Unlike vibe ────────────────────────────────────────────────────────
router.post('/:id/like', protect, async (req, res) => {
  const vibe = await VibeVideo.findById(req.params.id);
  if (!vibe) return res.status(404).json({ error: 'Not found' });

  const liked = vibe.likes.includes(req.user._id);
  if (liked) {
    vibe.likes.pull(req.user._id);
  } else {
    vibe.likes.push(req.user._id);
    await awardCoins(req.user._id, 1, 'Liked a vibe');
  }
  await vibe.save();

  const io = getSocket();
  io?.emit('vibe_like_update', { vibeId: vibe._id, likes: vibe.likes.length, liked: !liked });

  res.json({ likes: vibe.likes.length, liked: !liked });
});

// ── Add comment ───────────────────────────────────────────────────────────────
router.post('/:id/comments', protect, async (req, res) => {
  const { text } = req.body;
  if (!text?.trim()) return res.status(400).json({ error: 'Comment text required' });

  const vibe = await VibeVideo.findById(req.params.id);
  if (!vibe) return res.status(404).json({ error: 'Not found' });

  vibe.comments.push({ author: req.user._id, text });
  await vibe.save();
  await vibe.populate('comments.author', 'name handle avatar');

  const newComment = vibe.comments[vibe.comments.length - 1];

  const io = getSocket();
  io?.emit('vibe_new_comment', { vibeId: vibe._id, comment: newComment });

  res.status(201).json(newComment);
});

// ── Like a comment ────────────────────────────────────────────────────────────
router.post('/:id/comments/:commentId/like', protect, async (req, res) => {
  const vibe = await VibeVideo.findById(req.params.id);
  if (!vibe) return res.status(404).json({ error: 'Not found' });

  const comment = vibe.comments.id(req.params.commentId);
  if (!comment) return res.status(404).json({ error: 'Comment not found' });

  const liked = comment.likes.includes(req.user._id);
  if (liked) comment.likes.pull(req.user._id);
  else comment.likes.push(req.user._id);
  await vibe.save();

  res.json({ likes: comment.likes.length, liked: !liked });
});

// ── Delete comment ────────────────────────────────────────────────────────────
router.delete('/:id/comments/:commentId', protect, async (req, res) => {
  const vibe = await VibeVideo.findById(req.params.id);
  if (!vibe) return res.status(404).json({ error: 'Not found' });

  const comment = vibe.comments.id(req.params.commentId);
  if (!comment) return res.status(404).json({ error: 'Not found' });
  if (comment.author.toString() !== req.user._id.toString() &&
      vibe.creator.toString() !== req.user._id.toString()) {
    return res.status(403).json({ error: 'Forbidden' });
  }

  comment.deleteOne();
  await vibe.save();
  res.json({ message: 'Comment deleted' });
});

// ── Delete vibe ───────────────────────────────────────────────────────────────
router.delete('/:id', protect, async (req, res) => {
  const vibe = await VibeVideo.findById(req.params.id);
  if (!vibe) return res.status(404).json({ error: 'Not found' });
  if (vibe.creator.toString() !== req.user._id.toString()) {
    return res.status(403).json({ error: 'Forbidden' });
  }
  await VibeVideo.findByIdAndDelete(req.params.id);
  res.json({ message: 'Deleted' });
});

module.exports = router;
