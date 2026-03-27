const mongoose = require('mongoose');
const dotenv = require('dotenv');

dotenv.config();

const User = require('./src/models/User');
const Message = require('./src/models/Message');

async function check() {
  await mongoose.connect(process.env.MONGODB_URI);
  const users = await User.find();
  console.log(`Found ${users.length} users:`);
  for (const u of users) {
    const msgCount = await Message.countDocuments({ sender: u._id });
    console.log(`- @${u.handle} (Name: ${u.name}, ID: ${u._id}) Messages: ${msgCount}`);
  }
  process.exit(0);
}

check();
