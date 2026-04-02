'use strict';

/**
 * Local deploy: Gradle assembleDebug → verify APK → LAN IP → Express :3000 → qr.png → instructions.
 *
 * Usage: node scripts/deploy.js
 * Requires: Node ≥ 18, same Wi-Fi as phone, port 3000 reachable on the LAN.
 */

const { spawnSync } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');
const QRCode = require('qrcode');
const { startServer, DEFAULT_PORT } = require('./server');

const ROOT = path.join(__dirname, '..');
const APK_REL = path.join('app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk');
const APK_PATH = path.join(ROOT, APK_REL);
const QR_FILE = path.join(ROOT, 'qr.png');
const PORT = Number(process.env.PORT) || DEFAULT_PORT;

function gradleExecutable() {
  return process.platform === 'win32'
    ? path.join(ROOT, 'gradlew.bat')
    : path.join(ROOT, 'gradlew');
}

function runGradleAssembleDebug() {
  const exe = gradleExecutable();
  if (!fs.existsSync(exe)) {
    console.error('\n[deploy] ERROR: Gradle wrapper not found.');
    console.error(`[deploy] Expected: ${exe}`);
    console.error('[deploy] Run this from the Android project root (where gradlew lives).\n');
    process.exit(1);
  }

  console.log('[deploy] Step 1/5: Building APK (./gradlew assembleDebug) …\n');
  const result = spawnSync(exe, ['assembleDebug'], {
    cwd: ROOT,
    stdio: 'inherit',
    env: process.env,
    shell: process.platform === 'win32',
  });

  const code = result.status;
  if (code !== 0) {
    console.error('\n[deploy] ERROR: Gradle build failed.');
    console.error(
      `[deploy] assembleDebug exited with code ${code === null ? 'unknown' : code}.`
    );
    console.error('[deploy] Fix the Android build errors above, then run again.\n');
    process.exit(code === null ? 1 : code);
  }
  console.log('\n[deploy] Build finished successfully.\n');
}

function verifyApkExists() {
  console.log('[deploy] Step 2/5: Verifying APK output …');
  if (!fs.existsSync(APK_PATH)) {
    console.error('\n[deploy] ERROR: APK not found after build.');
    console.error(`[deploy] Expected: ${APK_PATH}`);
    console.error('[deploy] Check that assembleDebug produced app-debug.apk.\n');
    process.exit(1);
  }
  console.log(`[deploy] OK: ${APK_PATH}\n`);
}

function getLocalIPv4() {
  const nets = os.networkInterfaces();
  for (const name of Object.keys(nets)) {
    for (const net of nets[name]) {
      const isV4 = net.family === 'IPv4' || net.family === 4;
      if (isV4 && !net.internal) {
        return net.address;
      }
    }
  }
  return '127.0.0.1';
}

async function main() {
  runGradleAssembleDebug();
  verifyApkExists();

  console.log('[deploy] Step 3/5: Detecting local IPv4 …');
  const ip = getLocalIPv4();
  const url = `http://${ip}:${PORT}/app-debug.apk`;
  console.log(`[deploy] Local IP: ${ip}`);
  if (ip === '127.0.0.1') {
    console.warn(
      '[deploy] WARNING: No non-loopback IPv4 found. Phone may not reach this PC — check Wi-Fi.'
    );
  }
  console.log('');

  console.log('[deploy] Step 4/5: Starting local server …');
  let server;
  try {
    server = await startServer(APK_PATH, PORT, { localIp: ip });
  } catch (e) {
    if (e && e.code === 'EADDRINUSE') {
      console.error(`\n[deploy] ERROR: Port ${PORT} is already in use.`);
      console.error(`[deploy] Stop the other process or run: set PORT=3001 && node scripts/deploy.js\n`);
    }
    throw e;
  }
  console.log('');

  console.log('[deploy] Step 5/5: Generating QR code …');
  await QRCode.toFile(QR_FILE, url, {
    type: 'png',
    width: 400,
    margin: 2,
    errorCorrectionLevel: 'M',
  });

  const line = '='.repeat(60);
  console.log(`\n  ${line}`);
  console.log('  LOCAL DEPLOY READY');
  console.log(`  ${line}\n`);
  console.log('  Local IP:       ', ip);
  console.log('  Download URL:   ', url);
  console.log('  QR image:       ', QR_FILE);
  console.log('');
  console.log('  Scan the QR code from your phone (same Wi-Fi as this PC).');
  console.log('  The APK will download — open it and install manually.');
  console.log('');
  console.log('  Android: Enable "Install from unknown sources" (or allow this');
  console.log('           browser/source) when prompted. The QR does not auto-install.');
  console.log('');
  console.log(`  Server is running on port ${PORT}. Press Ctrl+C to stop.\n`);

  const shutdown = () => {
    server.close(() => process.exit(0));
  };
  process.on('SIGINT', shutdown);
  process.on('SIGTERM', shutdown);
}

main().catch((err) => {
  console.error('\n[deploy] ERROR:', err.message || err);
  process.exit(1);
});
