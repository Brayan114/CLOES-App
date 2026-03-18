 

 

 

**CLOES**

**Backend Setup & Integration Guide**

*✦ Real-time · Auth · Media · Coins · Calls · Bloom*

Version 1.0  ·  Node.js \+ MongoDB \+ Socket.io  ·  March 2026

# **Table of Contents**

1\. Architecture Overview

2\. Prerequisites

3\. Backend Setup

4\. Environment Variables

5\. Database Setup (MongoDB Atlas)

6\. Google OAuth Setup

7\. Cloudinary Setup

8\. Firebase Setup (Push Notifications)

9\. Running the Server

10\. Android App Integration

11\. API Reference

12\. Socket.io Events

13\. Feature Integration Guide

14\. Deployment to Production

15\. Troubleshooting

# **1\. Architecture Overview**

The CLOES backend is a Node.js/Express REST API with Socket.io for real-time features. Here is the full system:

 

**Android App  ←→  CLOES Backend (Node.js)  ←→  MongoDB Atlas**

The backend provides:

● REST API at /api/\* — all CRUD operations over HTTPS

● Socket.io at ws:// — real-time messaging, typing, calls, coins, emergency

● Cloudinary — media storage (avatars, chat images, videos, documents)

● MongoDB Atlas — all persistent data

● Firebase Admin — push notifications when app is backgrounded

● Node-cron — scheduled coin awards, bloom score recalculation

─────────────────────────────────────────────────────────────────

# **2\. Prerequisites**

Install these tools on your machine before starting:

 

## **Required Software**

● Node.js 18+ — https://nodejs.org  (check: node \--version)

● npm 9+  — comes with Node.js  (check: npm \--version)

● Git — https://git-scm.com

 

## **Required Accounts (all free tiers work)**

● MongoDB Atlas — https://www.mongodb.com/atlas  (free 512MB cluster)

● Cloudinary — https://cloudinary.com  (free 25GB storage)

● Google Cloud Console — https://console.cloud.google.com  (for OAuth)

● Firebase Console — https://console.firebase.google.com  (for push notifications)

─────────────────────────────────────────────────────────────────

# **3\. Backend Setup — Step by Step**

 

## **Step 1 — Get the Backend Files**

Unzip the CLOES backend package. You will have a folder called cloes-backend/.

Open a terminal / command prompt and navigate into it:

cd cloes-backend

 

## **Step 2 — Install Dependencies**

Run this command to install all packages (takes 1–2 minutes):

npm install

You should see node\_modules/ folder created. If you see errors, make sure Node.js 18+ is installed.

 

## **Step 3 — Create Your .env File**

Copy the example file:

cp .env.example .env

Then open .env in any text editor and fill in the values. The next sections explain each one.

**⚠ NOTE:** Never commit your .env file to Git. It contains secret keys.

─────────────────────────────────────────────────────────────────

# **4\. Environment Variables**

Open the .env file and fill in each variable:

 

## **Server Settings**

PORT=5000

NODE\_ENV=development

CLIENT\_URL=http://localhost:3000

**✦ TIP:** For Android emulator testing, CLIENT\_URL can be left as localhost. For real device testing, use your computer's LAN IP address.

 

## **JWT Secret**

JWT\_SECRET=make\_this\_very\_long\_and\_random\_at\_least\_64\_chars

Generate a random secret by running this in your terminal:

node \-e "console.log(require('crypto').randomBytes(64).toString('hex'))"

Copy the output and paste it as your JWT\_SECRET.

 

## **Coins Settings**

COINS\_PER\_10\_MINS=5

COINS\_FREE\_BROWSING\_MB\_PER\_COIN=10

Users earn COINS\_PER\_10\_MINS coins every 10 minutes of active app use. Each coin gives COINS\_FREE\_BROWSING\_MB\_PER\_COIN MB of data credit.

─────────────────────────────────────────────────────────────────

# **5\. MongoDB Atlas Setup**

 

**1\.** Go to https://www.mongodb.com/atlas and sign up for a free account.

**2\.** Click "Build a Database" → choose FREE (M0 tier) → select your nearest region.

**3\.** Create a database user: click "Database Access" → "Add New Database User". Username: cloes\_user, Password: a strong random password. Save both.

**4\.** Whitelist your IP: click "Network Access" → "Add IP Address" → "Allow Access from Anywhere" (0.0.0.0/0) for development. In production, use your server's IP only.

**5\.** Get your connection string: click "Connect" on your cluster → "Connect your application". Copy the string, it looks like:

mongodb+srv://cloes\_user:\<password\>@cluster0.xxxxx.mongodb.net/

**6\.** Replace \<password\> with your actual password and set it in .env:

MONGODB\_URI=mongodb+srv://cloes\_user:yourpassword@cluster0.xxxxx.mongodb.net/cloes?retryWrites=true\&w=majority

**✦ TIP:** The /cloes part at the end is the database name. MongoDB creates it automatically on first use.

─────────────────────────────────────────────────────────────────

# **6\. Google OAuth Setup**

 

**1\.** Go to https://console.cloud.google.com and create a new project called "CLOES".

**2\.** Click "APIs & Services" → "OAuth consent screen". Choose External → fill in app name "CLOES", your email as support email → Save.

**3\.** Click "Credentials" → "Create Credentials" → "OAuth 2.0 Client IDs".

**4\.** Application type: Web application. Name: CLOES Backend.

**5\.** Under "Authorized redirect URIs" add:

http://localhost:5000/api/auth/google/callback

(Add your production URL too when you deploy, e.g. https://yourapi.com/api/auth/google/callback)

**6\.** Click Create. Copy the Client ID and Client Secret into .env:

GOOGLE\_CLIENT\_ID=123456789-abc.apps.googleusercontent.com

GOOGLE\_CLIENT\_SECRET=GOCSPX-your\_secret\_here

GOOGLE\_CALLBACK\_URL=http://localhost:5000/api/auth/google/callback

**7\.** For Android: also create an Android OAuth Client ID with your app's SHA-1 fingerprint. This is needed for the Google Sign-In SDK in the app.

**⚠ NOTE:** To get your debug SHA-1: run ./gradlew signingReport in Android Studio terminal.

─────────────────────────────────────────────────────────────────

# **7\. Cloudinary Setup (Media Storage)**

Cloudinary stores all profile pictures, chat images, videos, and documents. The free tier gives 25GB which is plenty to start.

 

**1\.** Go to https://cloudinary.com and sign up for a free account.

**2\.** Once logged in, go to your Dashboard. You will see your Cloud Name, API Key, and API Secret.

**3\.** Copy them into .env:

CLOUDINARY\_CLOUD\_NAME=your\_cloud\_name

CLOUDINARY\_API\_KEY=123456789012345

CLOUDINARY\_API\_SECRET=your\_api\_secret\_here

**4\.** In Cloudinary Dashboard → Settings → Upload → scroll to "Upload presets". Create a preset called "cloes\_unsigned" with Signing Mode: Unsigned. This is used for direct uploads from Android.

**✦ TIP:** If Cloudinary is not configured, the backend automatically falls back to storing files in the local uploads/ folder. This works fine for development.

─────────────────────────────────────────────────────────────────

# **8\. Firebase Setup (Push Notifications)**

Firebase is used to send push notifications when users receive messages while the app is closed.

 

**1\.** Go to https://console.firebase.google.com → Create project → name it "CLOES".

**2\.** Add an Android app: click the Android icon, enter package name com.cloes.app, register.

**3\.** Download google-services.json and put it inside your Android project at: app/google-services.json

**4\.** In Firebase Console → Project Settings → Service Accounts → Generate New Private Key. Download the JSON file.

**5\.** Copy the values into .env:

FIREBASE\_PROJECT\_ID=cloes-12345

FIREBASE\_PRIVATE\_KEY="-----BEGIN PRIVATE KEY-----\\n...\\n-----END PRIVATE KEY-----\\n"

FIREBASE\_CLIENT\_EMAIL=firebase-adminsdk@cloes-12345.iam.gserviceaccount.com

**⚠ NOTE:** The FIREBASE\_PRIVATE\_KEY must keep the \\n characters and be wrapped in double quotes in .env.

**✦ TIP:** Firebase is optional for local development. The app works fully without it — you just won't get background push notifications.

─────────────────────────────────────────────────────────────────

# **9\. Running the Server**

 

## **Development Mode (with auto-restart)**

npm run dev

You should see:

✅ MongoDB connected: cluster0.xxxxx.mongodb.net

�� Socket.io initialized

�� Coin cron started — awards every 10 minutes

�� Bloom cron started — recalculates daily at 2am

�� CLOES Backend running on port 5000

 

## **Test the Health Endpoint**

Open your browser or Postman and visit:

http://localhost:5000/api/health

You should get back: { "status": "ok", "version": "1.0.0" }

 

## **Production Mode**

npm start

**✦ TIP:** For production, use PM2 to keep the server running: npm install \-g pm2 && pm2 start src/index.js \--name cloes-api

─────────────────────────────────────────────────────────────────

# **10\. Android App Integration**

The ANDROID\_INTEGRATION/ folder contains everything you need to connect the app to the backend.

 

## **Step 1 — Add Dependencies to app/build.gradle**

Open your app/build.gradle and add inside the dependencies {} block:

implementation 'com.squareup.okhttp3:okhttp:4.12.0'

implementation('io.socket:socket.io-client:2.1.0') {

    exclude group: 'org.json', module: 'json'

}

implementation 'com.google.android.gms:play-services-auth:21.0.0'

implementation platform('com.google.firebase:firebase-bom:32.7.0')

implementation 'com.google.firebase:firebase-messaging-ktx'

 

## **Step 2 — Copy CloesApiClient.kt**

Copy the file ANDROID\_INTEGRATION/CloesApiClient.kt into your Android project at:

app/src/main/java/com/cloes/app/network/CloesApiClient.kt

Create the network/ folder if it does not exist.

 

## **Step 3 — Set Your Server URL**

In CloesApiClient.kt, find the CloesConfig object and set your server URL:

// For Android emulator:

const val BASE\_URL \= "http://10.0.2.2:5000"

 

// For real phone on same WiFi:

// const val BASE\_URL \= "http://192.168.1.XXX:5000"

// (replace XXX with your computer's LAN IP)

To find your LAN IP on Windows: run ipconfig in cmd. On Mac/Linux: run ifconfig.

 

## **Step 4 — Add Deep Link for Google OAuth**

In AndroidManifest.xml, inside your MainActivity \<activity\> tag, add:

\<intent-filter android:autoVerify="true"\>

    \<action android:name="android.intent.action.VIEW" /\>

    \<category android:name="android.intent.category.DEFAULT" /\>

    \<category android:name="android.intent.category.BROWSABLE" /\>

    \<data android:scheme="cloes" android:host="auth" /\>

\</intent-filter\>

 

## **Step 5 — Add google-services.json**

Place the google-services.json file (downloaded from Firebase) at:

app/google-services.json

Also add to the top of app/build.gradle (if not already there):

apply plugin: 'com.google.gms.google-services'

 

## **Step 6 — Connect Socket on Login**

After a successful login, connect the socket:

// In your ViewModel after AuthApi.login() succeeds:

CloesSocket.connect()

And disconnect on logout:

CloesSocket.disconnect()

─────────────────────────────────────────────────────────────────

# **11\. API Reference**

All endpoints require Authorization: Bearer \<token\> header unless marked as public.

 

## **Authentication**

| Method | Endpoint | Description |
| :---- | :---- | :---- |
| **POST** | /api/auth/register | Register with email \+ password \+ palette colors |
| **POST** | /api/auth/login | Login with email \+ password, returns JWT |
| **GET** | /api/auth/google | Initiate Google OAuth (PUBLIC — open in browser) |
| **GET** | /api/auth/google/callback | Google OAuth callback — redirects to app with token |
| **POST** | /api/auth/onboard | Complete onboarding (name, handle, palette) |
| **GET** | /api/auth/me | Get current authenticated user |
| **POST** | /api/auth/logout | Logout, marks user offline |
| **DELETE** | /api/auth/account | Delete account permanently |

 

## **Users & Profile**

| Method | Endpoint | Description |
| :---- | :---- | :---- |
| **PATCH** | /api/users/me | Update profile (name, bio, theme, font, etc.) |
| **POST** | /api/users/me/avatar | Upload profile picture from gallery (multipart) |
| **DELETE** | /api/users/me/avatar | Remove profile picture (reset to Fragment art) |
| **POST** | /api/users/me/palette | Update Fragment light palette colors |
| **GET** | /api/users/me/qr | Get or regenerate QR code (base64) |
| **POST** | /api/users/me/cloesed-key | Set CLOESED locked contacts key |
| **POST** | /api/users/me/fcm-token | Update Firebase push token |
| **GET** | /api/users/search/:query | Search users by name or handle |

 

## **Messages & Conversations**

| Method | Endpoint | Description |
| :---- | :---- | :---- |
| **GET** | /api/messages/conversations | Get all conversations for current user |
| **POST** | /api/messages/conversations/direct | Get or create DM conversation |
| **GET** | /api/messages/conversations/:id/messages | Get messages (paginated) |
| **POST** | /api/messages/conversations/:id/messages | Send text message |
| **POST** | /api/messages/conversations/:id/media | Send image/video/audio (multipart) |
| **POST** | /api/messages/conversations/:id/document | Send document/file (multipart) |
| **POST** | /api/messages/conversations/:id/link | Send link with preview |
| **POST** | /api/messages/messages/:id/react | React to message with emoji |
| **DELETE** | /api/messages/messages/:id/for-me | Delete message for me only |
| **DELETE** | /api/messages/messages/:id/for-all | Delete message for everyone |
| **PATCH** | /api/messages/messages/:id | Edit message text |
| **POST** | /api/messages/conversations/:id/wallpaper-upload | Upload chat background |
| **PATCH** | /api/messages/conversations/:id/disappear | Set disappearing message timer |
| **PATCH** | /api/messages/conversations/:id/lock | Lock/unlock chat (CLOESED) |
| **DELETE** | /api/messages/conversations/:id/messages | Clean/clear all messages |

 

## **Vibe Videos**

| Method | Endpoint | Description |
| :---- | :---- | :---- |
| **GET** | /api/vibes | Get vibe feed (paginated, filter by category) |
| **GET** | /api/vibes/:id | Get single vibe (increments view count) |
| **POST** | /api/vibes/upload | Upload vibe video (multipart: vibe \+ thumbnail) |
| **POST** | /api/vibes/:id/like | Toggle like on vibe |
| **POST** | /api/vibes/:id/comments | Add comment to vibe |
| **DELETE** | /api/vibes/:id | Delete vibe (creator only) |

 

## **Coins**

| Method | Endpoint | Description |
| :---- | :---- | :---- |
| **GET** | /api/coins | Get balance \+ transaction history |
| **POST** | /api/coins/heartbeat | Award coins for 10-min active session |
| **POST** | /api/coins/spend-browsing | Spend coins for free browsing data MB |
| **POST** | /api/coins/earn | Manually earn coins (bonus actions) |

 

## **Bloom, QR, Emergency, Calls**

| Method | Endpoint | Description |
| :---- | :---- | :---- |
| **GET** | /api/bloom | Get all bloom scores for current user |
| **GET** | /api/bloom/top | Get top 10 connections by bloom score |
| **GET** | /api/bloom/urgency-status | Check if any chat is at HIGH urgency |
| **GET** | /api/qr/me | Get my QR code as base64 |
| **GET** | /api/qr/me/image | Download QR code as PNG file |
| **POST** | /api/qr/scan | Resolve scanned QR data to user profile |
| **POST** | /api/emergency/trigger | Trigger emergency alert to all contacts |
| **POST** | /api/emergency/contacts | Update emergency contacts list |
| **POST** | /api/calls/initiate | Initiate voice/video call to user |
| **POST** | /api/calls/signal | Pass WebRTC signaling data |
| **GET** | /api/calls/ice-servers | Get ICE/TURN server configuration |

─────────────────────────────────────────────────────────────────

# **12\. Socket.io Events**

Connect with your JWT token: CloesSocket.connect()

 

## **Events You EMIT (from Android)**

| Method | Endpoint | Description |
| :---- | :---- | :---- |
| **emit** | join\_conversation | Join a conversation room to receive its messages |
| **emit** | typing\_start | { conversationId } — tell others you're typing |
| **emit** | typing\_stop | { conversationId } — tell others you stopped typing |
| **emit** | messages\_read | { conversationId } — mark all messages as read |
| **emit** | session\_heartbeat | Awards coins if 10 min have passed |
| **emit** | call\_offer | { targetUserId, offer, callId, callType } — initiate call |
| **emit** | call\_answer | { callerId, answer, callId } — answer call |
| **emit** | call\_ice\_candidate | { targetUserId, candidate, callId } — WebRTC ICE |
| **emit** | call\_reject | { callerId, callId } — reject incoming call |
| **emit** | call\_end | { targetUserId, callId, durationSecs } — end active call |
| **emit** | emergency\_sos | { message, location } — trigger emergency alert |

 

## **Events You RECEIVE (listen in Android)**

| Method | Endpoint | Description |
| :---- | :---- | :---- |
| **on** | new\_message | Full message object — add to conversation |
| **on** | message\_edited | Updated message object — update in UI |
| **on** | message\_deleted | { messageId } — remove from UI |
| **on** | message\_reaction | { messageId, reactions } — update emoji reactions |
| **on** | typing\_start | { conversationId, user } — show typing indicator |
| **on** | typing\_stop | { conversationId, userId } — hide typing indicator |
| **on** | messages\_read | { conversationId, readBy } — show read receipts |
| **on** | user\_online | { userId, online, lastSeen } — update presence dot |
| **on** | coin\_update | { balance, earned, reason } — update coin display |
| **on** | incoming\_call | { callId, caller, callType, iceServers } — show call UI |
| **on** | call\_answered | { callId } — other party accepted |
| **on** | call\_rejected | { callId } — other party declined |
| **on** | call\_ended | { callId, duration } — call ended |
| **on** | ice\_candidate | { candidate, callId, from } — WebRTC ICE candidate |
| **on** | emergency\_alert | { from, message, location } — emergency received |
| **on** | new\_vibe | New vibe video uploaded — add to feed |
| **on** | vibe\_like\_update | { vibeId, likes } — update like count |
| **on** | vibe\_new\_comment | { vibeId, comment } — add comment to vibe |

─────────────────────────────────────────────────────────────────

# **13\. Feature Integration Guide**

 

## **�� Google Sign In**

The app uses Chrome Custom Tabs to open the Google OAuth URL, then receives the token via deep link.

**1\.** Open the URL in a Chrome Custom Tab: AuthApi.googleOAuthUrl()

**2\.** Google redirects to cloes://auth?token=XXX\&onboarded=0

**3\.** In MainActivity, catch this in onNewIntent():

intent?.data?.let { uri \-\>

    if (uri.scheme \== "cloes" && uri.host \== "auth") {

        val token \= uri.getQueryParameter("token")

        val onboarded \= uri.getQueryParameter("onboarded") \== "1"

        token?.let { AuthApi.handleDeepLink(it); CloesSocket.connect() }

    }

}

 

## **�� Change Profile Picture from Gallery**

The file picker and upload are handled in one call:

val launcher \= rememberLauncherForActivityResult(

    ActivityResultContracts.GetContent()) { uri \-\>

    uri?.let {

        scope.launch { UserApi.uploadAvatar(context, it) }

    }

}

launcher.launch("image/\*")  // Opens gallery

 

## **�� Send Images/Docs in Chat**

// Image from gallery:

launcher.launch("image/\*")

// In callback: MessageApi.sendMedia(context, conversationId, uri)

 

// Document from files:

launcher.launch("application/\*")

// In callback: MessageApi.sendDocument(context, conversationId, uri)

 

// Link with preview:

MessageApi.sendLink(conversationId, url, "Title")

 

## **�� Change Chat Wallpaper**

// Upload from gallery:

MessageApi.uploadAndSetWallpaper(context, conversationId, uri)

 

// OR set from URL:

MessageApi.setWallpaper(conversationId, "https://...")

 

## **�� Muse Dress — Send Images**

// Upload image to Muse for outfit analysis:

MediaApi.uploadMedia(context, uri).onSuccess { json \-\>

    val imageUrl \= json.getString("url")

    // Include imageUrl in your Muse Dress API message

}

 

## **�� Coins — 10-Minute Tracking**

Set up a periodic heartbeat in your ViewModel or MainActivity:

// In AppViewModel.init:

viewModelScope.launch {

    while (isActive) {

        delay(10 \* 60 \* 1000L)  // 10 minutes

        CloesSocket.heartbeat() // Awards coins via socket

    }

}

 

// Observe coin balance in real-time:

val coins by CloesSocket.coinBalance.collectAsState()

 

## **�� Voice & Video Calls**

Calls use WebRTC through the backend as a signaling server. ICE/TURN servers are provided.

**1\.** Caller: CloesSocket.sendCallOffer(targetUserId, offer, callId, "voice")

**2\.** Callee receives: incoming\_call event on socket

**3\.** Callee answers: CloesSocket.sendCallAnswer(callerId, answer, callId)

**4\.** Both exchange ICE candidates via ice\_candidate events

**5\.** Either party ends: CloesSocket.emit("call\_end", ...)

**✦ TIP:** For production calls that work across networks, you need a TURN server. Free options: Metered.ca or Open Relay Project.

 

## **�� Emergency Page**

Trigger from the Emergency screen:

// Via socket (instant, no internet needed if on LAN):

CloesSocket.triggerEmergency("I need help\!", lat, lng)

 

// Via REST (more reliable, also sends push notifications):

EmergencyApi.trigger("I need help\!", lat, lng)

 

## **�� Bloom — Active Light Urgency**

Bloom scores are calculated automatically on every message sent. The app can poll for urgency status:

BloomApi.getUrgencyStatus().onSuccess { json \-\>

    val urgencyTintOn \= json.optBoolean("urgencyTintOn")

    val urgency \= json.optString("urgency") // "low", "mid", "high"

}

 

## **�� Export Chat History**

Get the export download URL and open it in a browser or download manager:

val exportUrl \= MediaApi.exportChatHistory(conversationId)

// Opens as JSON download — includes all messages, timestamps, media links

─────────────────────────────────────────────────────────────────

# **14\. Deployment to Production**

To make the backend accessible from anywhere (not just your local network), deploy it to a cloud server.

 

## **Option A — Railway (Easiest, Free Tier Available)**

**1\.** Go to https://railway.app and sign in with GitHub.

**2\.** Click "New Project" → "Deploy from GitHub repo" (push your backend to GitHub first).

**3\.** Add all your .env variables under "Variables" in Railway dashboard.

**4\.** Railway gives you a URL like: https://cloes-api-production.up.railway.app

**5\.** Update CloesConfig.BASE\_URL in Android to your Railway URL.

 

## **Option B — Render (Free Tier)**

**1\.** Go to https://render.com → New → Web Service → connect your GitHub repo.

**2\.** Build command: npm install. Start command: node src/index.js.

**3\.** Add environment variables in the Render dashboard.

 

## **Option C — VPS (DigitalOcean / Hetzner)**

For full control. After SSH into your server:

git clone your-repo && cd cloes-backend

npm install && cp .env.example .env && nano .env

npm install \-g pm2

pm2 start src/index.js \--name cloes-api

pm2 save && pm2 startup

**⚠ NOTE:** After deploying, update GOOGLE\_CALLBACK\_URL and CLIENT\_URL in your .env to your production domain.

─────────────────────────────────────────────────────────────────

# **15\. Troubleshooting**

 

## **Cannot connect from Android emulator**

● Make sure the backend is running (npm run dev shows "running on port 5000")

● Android emulator uses 10.0.2.2 to reach localhost. BASE\_URL should be http://10.0.2.2:5000

● Check your firewall is not blocking port 5000

 

## **Cannot connect from real phone**

● Your phone and computer must be on the SAME WiFi network

● Find your computer's LAN IP: ipconfig (Windows) or ifconfig (Mac/Linux)

● Set BASE\_URL to http://192.168.1.XXX:5000 (your LAN IP)

 

## **MongoDB connection failed**

● Check MONGODB\_URI is correct in .env

● Check your IP is whitelisted in MongoDB Atlas → Network Access

● Check username/password are correct (no special chars in password without URL-encoding)

 

## **Google OAuth not working**

● GOOGLE\_CALLBACK\_URL must exactly match what you entered in Google Console

● For local dev: http://localhost:5000/api/auth/google/callback

● Make sure GOOGLE\_CLIENT\_ID and GOOGLE\_CLIENT\_SECRET are correct

 

## **Socket not connecting**

● Check token is being sent in socket handshake auth: { token: "Bearer ..." }

● Check SERVER is running (not just the Android app)

● In CloesSocket.connect(), make sure TokenStore.get() returns a valid token

 

## **Coins not being awarded**

● Socket heartbeat must be emitted every 10 minutes from the app

● CloesSocket.heartbeat() triggers the server-side coin award

● Check the server console for "�� Awarded coins" log messages

 

**✦ CLOES Backend — Built for Privacy, Vibrancy, and Connection ✦**

*Node.js · Express · MongoDB · Socket.io · Cloudinary · Firebase*

