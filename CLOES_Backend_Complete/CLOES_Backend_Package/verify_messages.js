const mongoose = require('mongoose');
const dotenv = require('dotenv');
const path = require('path');

dotenv.config();

const Message = require('./src/models/Message');
const User = require('./src/models/User');
const Conversation = require('./src/models/Conversation');

async function verify() {
  try {
    await mongoose.connect(process.env.MONGODB_URI);
    console.log('Connected to MongoDB');

    const convoCount = await Conversation.countDocuments();
    console.log(`TOTAL CONVERSATIONS IN DB: ${convoCount}`);

    const convos = await Conversation.find().populate('participants', 'handle name');
    convos.forEach((c, i) => {
      const parts = c.participants.map(p => `@${p.handle}`).join(', ');
      console.log(`${i+1}. Convo ID: ${c._id} [Type: ${c.type}] Participants: ${parts}`);
    });

    const msgCount = await Message.countDocuments();
    console.log(`TOTAL MESSAGES IN DB: ${msgCount}`);

    process.exit(0);
  } catch (err) {
    console.error('Error:', err);
    process.exit(1);
  }
}

verify();
