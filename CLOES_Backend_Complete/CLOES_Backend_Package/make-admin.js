#!/usr/bin/env node
/**
 * Promote a user to admin by email.
 * Usage:  node make-admin.js user@email.com
 */
require('dotenv').config();
const mongoose = require('mongoose');
const User     = require('./src/models/User');

const email = process.argv[2];
if (!email) {
  console.log('Usage:  node make-admin.js <email>');
  process.exit(1);
}

(async () => {
  try {
    await mongoose.connect(process.env.MONGODB_URI);
    const user = await User.findOneAndUpdate(
      { email: email.toLowerCase() },
      { isAdmin: true },
      { new: true }
    );
    if (!user) {
      console.log(`❌ No user found with email: ${email}`);
    } else {
      console.log(`✅ ${user.name} (${user.email}) is now an admin!`);
    }
  } catch (err) {
    console.error('Error:', err.message);
  } finally {
    await mongoose.disconnect();
  }
})();
