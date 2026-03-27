const mongoose = require('mongoose');
require('dotenv').config();
const User = require('./src/models/User');
const Message = require('./src/models/Message');
const Conversation = require('./src/models/Conversation');

async function debug() {
  await mongoose.connect(process.env.MONGODB_URI);
  console.log('--- USER AUDIT ---');
  const b1 = await User.findOne({ handle: 'brayan101' });
  const b2 = await User.findOne({ handle: 'brayan.admin' });
  
  if (b1) console.log(`Found @brayan101: ${b1._id}`);
  if (b2) console.log(`Found @brayan.admin: ${b2._id}`);

  console.log('--- ALL MESSAGES ---');
  const allMsgs = await Message.find().populate('sender', 'handle');
  console.log(`Count: ${allMsgs.length}`);
  allMsgs.forEach(m => console.log(`[${m.createdAt}] FROM @${m.sender?.handle}: ${m.text}`));

  console.log('--- ALL CONVERSATIONS ---');
  const allConvos = await Conversation.find().populate('participants', 'handle');
  console.log(`Count: ${allConvos.length}`);
  allConvos.forEach(c => console.log(`CONVO ${c._id}: ${c.participants.map(p => p.handle).join(', ')}`));

  process.exit(0);
}
debug();
