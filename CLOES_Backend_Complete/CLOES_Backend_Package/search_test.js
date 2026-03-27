const mongoose = require('mongoose');
require('dotenv').config();
const User = require('./src/models/User');

async function test() {
  await mongoose.connect(process.env.MONGODB_URI);
  console.log('Connected to MongoDB');

  const handle = 'brayan.admin';
  const match = await User.findOne({ handle: { $regex: new RegExp('^' + handle + '$', 'i') } });
  
  if (match) {
    console.log(`MATCH_FOUND handle=${match.handle} ID=${match._id} name=${match.name}`);
  } else {
    console.log(`MATCH_NOT_FOUND handle=${handle}`);
    const all = await User.find();
    console.log('All handles in DB:');
    all.forEach(u => console.log(' - ' + u.handle));
  }
  process.exit(0);
}
test();
