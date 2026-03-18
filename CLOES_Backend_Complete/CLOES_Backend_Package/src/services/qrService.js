// ── QR Service ────────────────────────────────────────────────────────────────
const QRCode = require('qrcode');

const generateQR = async (userId) => {
  const data = JSON.stringify({ type: 'cloes_profile', userId, ts: Date.now() });
  const qr   = await QRCode.toDataURL(data, {
    errorCorrectionLevel: 'H',
    width: 300,
    margin: 2,
    color: { dark: '#8B5CF6', light: '#FFFFFF' },
  });
  return qr; // base64 data URL
};

const generateQRBuffer = async (userId) => {
  const data = JSON.stringify({ type: 'cloes_profile', userId });
  return QRCode.toBuffer(data, { errorCorrectionLevel: 'H', width: 300 });
};

module.exports = { generateQR, generateQRBuffer };
