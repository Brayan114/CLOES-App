const cloudinary = require('cloudinary').v2;
const { CloudinaryStorage } = require('multer-storage-cloudinary');
const multer = require('multer');
const path = require('path');
const fs = require('fs');

cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
  api_key:    process.env.CLOUDINARY_API_KEY,
  api_secret: process.env.CLOUDINARY_API_SECRET,
});

// ── Local fallback storage (when Cloudinary not configured) ───────────────────
const localStorage = multer.diskStorage({
  destination: (req, file, cb) => {
    let folder = 'uploads/chat';
    if (file.fieldname === 'avatar')    folder = 'uploads/avatars';
    if (file.fieldname === 'vibe')      folder = 'uploads/vibes';
    if (file.fieldname === 'document')  folder = 'uploads/documents';
    fs.mkdirSync(folder, { recursive: true });
    cb(null, folder);
  },
  filename: (req, file, cb) => {
    const uniqueName = `${Date.now()}-${Math.round(Math.random() * 1e9)}${path.extname(file.originalname)}`;
    cb(null, uniqueName);
  }
});

const useCloudinary = !!(
  process.env.CLOUDINARY_CLOUD_NAME &&
  process.env.CLOUDINARY_API_KEY &&
  process.env.CLOUDINARY_API_SECRET
);

// ── Cloudinary storages by type ───────────────────────────────────────────────
const makeCloudStorage = (folder, resourceType = 'auto') =>
  new CloudinaryStorage({
    cloudinary,
    params: { folder: `cloes/${folder}`, resource_type: resourceType, allowed_formats: null }
  });

// ── Multer instances ──────────────────────────────────────────────────────────
const fileFilter = (allowed) => (req, file, cb) => {
  const mime = file.mimetype;
  const ok = allowed.some(type => mime.startsWith(type) || mime === type);
  if (ok) cb(null, true);
  else cb(new Error(`File type not allowed: ${mime}`), false);
};

const uploadAvatar = multer({
  storage: useCloudinary ? makeCloudStorage('avatars', 'image') : localStorage,
  limits: { fileSize: 10 * 1024 * 1024 }, // 10 MB
  fileFilter: fileFilter(['image/']),
});

const uploadChatMedia = multer({
  storage: useCloudinary ? makeCloudStorage('chat') : localStorage,
  limits: { fileSize: 100 * 1024 * 1024 }, // 100 MB
  fileFilter: fileFilter(['image/', 'video/', 'audio/', 'application/']),
});

const uploadVibe = multer({
  storage: useCloudinary ? makeCloudStorage('vibes', 'video') : localStorage,
  limits: { fileSize: 500 * 1024 * 1024 }, // 500 MB
  fileFilter: fileFilter(['video/']),
});

const uploadDocument = multer({
  storage: useCloudinary ? makeCloudStorage('documents', 'raw') : localStorage,
  limits: { fileSize: 50 * 1024 * 1024 }, // 50 MB
});

const uploadAny = multer({
  storage: useCloudinary ? makeCloudStorage('misc') : localStorage,
  limits: { fileSize: 100 * 1024 * 1024 },
});

// ── Delete from Cloudinary ────────────────────────────────────────────────────
const deleteFromCloudinary = async (publicId, resourceType = 'image') => {
  if (!useCloudinary || !publicId) return;
  try { await cloudinary.uploader.destroy(publicId, { resource_type: resourceType }); }
  catch (e) { console.warn('Cloudinary delete failed:', e.message); }
};

module.exports = {
  cloudinary,
  uploadAvatar,
  uploadChatMedia,
  uploadVibe,
  uploadDocument,
  uploadAny,
  deleteFromCloudinary,
  useCloudinary,
};
