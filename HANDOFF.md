# CLOES Project — Handoff Documentation

## Project Deliverables

### 1. Android App (Kotlin)
**Location:** `app/` folder in the project root

A full-featured social communication app with:
- **Auth** — Email/password + Google OAuth sign-in
- **Real-time messaging** — Text, images, videos, audio, documents, links (Socket.io)
- **Vibe Videos** — TikTok-style short video feed with likes + comments
- **Voice & Video Calls** — WebRTC-based P2P calling
- **Coins** — In-app currency earned through active usage (5 coins / 10 min)
- **Bloom** — Connection health scores between users
- **Emergency SOS** — Alert emergency contacts with GPS location
- **Fragment UI** — Unique palette-based profile art system
- **QR codes** — Add contacts by scanning
- **Muse AI** — AI dress/outfit assistant

**To build:** Open the **project root** folder in Android Studio → Sync Gradle → Build & Run.

---

### 2. Admin Portal (React Web App)
**Location:** `CLOES_Backend_Complete/CLOES_Backend_Package/public/admin.html`

A single-file React dashboard for managing the platform:
- **Dashboard** — Live stats (users, messages, vibes, coins) + charts
- **User Management** — Search, ban/unban, grant/revoke admin, adjust coins, delete
- **Message Moderation** — Search and delete messages
- **Vibe Moderation** — Approve, hide, or delete vibe videos
- **Coin Analytics** — Top earners, transaction history, economy overview
- **Bloom Analytics** — Connection health overview
- **Settings** — API URL config, stack info

**Live URL:** https://cloes-app-production.up.railway.app/admin.html  
**Login:** `brayanjunior101@gmail.com` / `admin123`

No build step needed — it's served directly by the backend.

---

### 3. Backend (Node.js + Express + MongoDB)
**Location:** `CLOES_Backend_Complete/CLOES_Backend_Package/`

**Deployed at:** https://cloes-app-production.up.railway.app  
**Health check:** https://cloes-app-production.up.railway.app/api/health

#### Tech Stack
| Layer | Technology |
|---|---|
| Runtime | Node.js 18+ |
| Framework | Express.js |
| Database | MongoDB Atlas (cloud) |
| Real-time | Socket.io |
| Media Storage | Cloudinary |
| Push Notifications | Firebase Admin SDK |
| Auth | JWT + Google OAuth 2.0 + Passport.js |
| Hosting | Railway |

#### Key Files
```
src/
├── index.js          # Server entry point
├── config/           # Passport, Cloudinary, Firebase config
├── middleware/        # Auth, admin check, error handler, upload
├── models/           # User, Message, Conversation, VibeVideo, Analytics
├── routes/           # All API routes (auth, users, messages, vibes, coins, admin, etc.)
├── services/         # QR generation, cron jobs
└── socket/           # Socket.io event handlers
make-admin.js         # CLI: node make-admin.js <email>
public/admin.html     # Admin portal
```

#### API Routes Summary
| Route | Endpoints | Purpose |
|---|---|---|
| `/api/auth` | 8 | Register, login, Google OAuth, me, logout |
| `/api/users` | 8 | Profile, avatar, settings, search |
| `/api/messages` | 12 | Conversations, send/receive, media, reactions |
| `/api/vibes` | 6 | Upload, feed, likes, comments |
| `/api/coins` | 4 | Balance, earn, spend |
| `/api/bloom` | 3 | Scores, top connections, urgency |
| `/api/calls` | 3 | WebRTC signaling, ICE servers |
| `/api/emergency` | 2 | SOS trigger, contacts |
| `/api/admin` | 12 | Dashboard stats, user/message/vibe/coin management |
| `/api/qr` | 3 | QR code generation and scanning |
| `/api/muse` | 1 | AI assistant |
| `/api/exports` | 1 | Chat history export |

---

## Data Persistence & Storage

CLOES uses a **Cloud-First + In-Memory** architecture designed for privacy and multi-device sync:

1. **Permanent Storage (Cloud):** 
   - All messages and conversations are stored in a **MongoDB Atlas** database.
   - **Backend Model:** [Message.js](file:///c:/Users/braya/.gemini/antigravity/scratch/CLOES_v2_Fixed/CLOES_Backend_Complete/CLOES_Backend_Package/src/models/Message.js) and [Conversation.js](file:///c:/Users/braya/.gemini/antigravity/scratch/CLOES_v2_Fixed/CLOES_Backend_Complete/CLOES_Backend_Package/src/models/Conversation.js).
   - Sending a message POSTs it to the cloud immediately.

2. **Mobile Persistence (Android):**
   - The app stores messages in a **reactive in-memory cache** within `AppViewModel`.
   - **Frontend Model:** [Models.kt](file:///c:/Users/braya/.gemini/antigravity/scratch/CLOES_v2_Fixed/app/src/main/java/com/cloes/app/data/Models.kt).
   - **Behavior:** For security, the local cache is wiped on logout. History is then **eagerly refetched** from the backend upon the next login and when a chat is opened.

3. **No Local DB:** 
   - The Android app **does not utilize a persistent local database** (like SQLite or Room) for chat history. This ensures that no chat data remains on the device's storage after an account is closed or logged out, maintaining the "CLOES" (Cloaked) privacy standard.

---

## Setup From Scratch

### Backend
```bash
cd CLOES_Backend_Complete/CLOES_Backend_Package
npm install
cp .env.example .env    # Fill in your credentials
npm run dev             # Starts on port 5000
```

### Admin Portal
```bash
# After backend is running:
node make-admin.js your-email@example.com
# Open: http://localhost:5000/admin.html
```

### Android App
1. Open **project root** in Android Studio
2. Set `BASE_URL` in `CloesApiClient.kt` to your backend URL
3. Place `google-services.json` in `app/`
4. Build & Run

---

## External Services Required

| Service | Purpose | Dashboard |
|---|---|---|
| MongoDB Atlas | Database | https://cloud.mongodb.com |
| Cloudinary | Media storage | https://cloudinary.com/console |
| Firebase | Push notifications + google-services.json | https://console.firebase.google.com |
| Google Cloud | OAuth credentials | https://console.cloud.google.com |
| Railway | Backend hosting | https://railway.app/dashboard |

---

## Full API Documentation
See [CLOES.md](file:///c:/Users/braya/.gemini/antigravity/scratch/CLOES_v2_Fixed/CLOES.md) for the complete 800+ line setup guide covering:
- All API endpoints with request/response formats
- Socket.io events (emit + listen)
- Feature integration guide (Google Sign-In, media upload, calls, etc.)
- Deployment options (Railway, Render, VPS)
- Troubleshooting
