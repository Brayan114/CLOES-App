require('dotenv').config();
const mongoose = require('mongoose');
const User = require('./src/models/User');

async function run() {
  await mongoose.connect(process.env.MONGODB_URI);
  console.log('Connected to DB');

  const users = await User.find({ $or: [{ handle: { $exists: false } }, { handle: '' }, { handle: null }] });
  console.log(`Found ${users.length} users passing handle checks without a valid one`);

  for (const user of users) {
    let baseHandle = '';
    if (user.email) {
      baseHandle = user.email.split('@')[0].toLowerCase().replace(/[^a-z0-9]/g, '');
    } else if (user.name) {
      baseHandle = user.name.toLowerCase().replace(/[^a-z0-9]/g, '');
    } else {
      baseHandle = 'user' + Math.floor(Math.random() * 10000);
    }
    if (!baseHandle) baseHandle = 'user' + Math.floor(Math.random() * 10000);

    let handle = baseHandle;
    let counter = 1;
    while (true) {
      const exists = await User.findOne({ handle });
      if (!exists) break;
      handle = baseHandle + counter;
      counter++;
    }

    user.handle = handle;
    await user.save();
    console.log(`Assigned handle @${handle} to User ${user._id}`);
  }

  console.log('Migration Complete');
  process.exit(0);
}

run().catch(console.error);
