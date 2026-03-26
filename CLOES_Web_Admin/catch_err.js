const { execSync } = require('child_process');
try {
  execSync('npm run build', { encoding: 'utf-8', stdio: 'pipe' });
  console.log("Build passed!");
} catch (e) {
  require('fs').writeFileSync('err.txt', (e.stdout || '') + '\n' + (e.stderr || ''));
  console.log("Error written to err.txt");
}
