'use strict';

/**
 * Express: serves the debug APK at GET /app-debug.apk on 0.0.0.0:3000
 * so devices on the same LAN can download the APK.
 */

const express = require('express');
const path = require('path');

const DEFAULT_PORT = 3000;

/**
 * @param {string} apkAbsolutePath
 * @param {number} [port]
 * @param {{ localIp?: string }} [options] If localIp is set, logs full LAN download URL on start.
 * @returns {Promise<import('http').Server>}
 */
function startServer(apkAbsolutePath, port = DEFAULT_PORT, options = {}) {
  const { localIp } = options;
  const apkPath = path.resolve(apkAbsolutePath);
  const app = express();

  app.get('/app-debug.apk', (req, res) => {
    res.setHeader('Content-Type', 'application/vnd.android.package-archive');
    res.setHeader('Content-Disposition', 'attachment; filename="app-debug.apk"');
    res.sendFile(apkPath, (err) => {
      if (err) {
        console.error('[server] Failed to send APK:', err.message);
        if (!res.headersSent) res.status(500).end();
      }
    });
  });

  return new Promise((resolve, reject) => {
    const server = app.listen(port, '0.0.0.0', () => {
      console.log(`[server] Listening on http://0.0.0.0:${port}`);
      console.log(`[server] Endpoint: /app-debug.apk`);
      if (localIp) {
        console.log(`[server] Download URL: http://${localIp}:${port}/app-debug.apk`);
      }
      resolve(server);
    });
    server.on('error', reject);
  });
}

function getLocalIPv4() {
  const os = require('os');
  const nets = os.networkInterfaces();
  for (const name of Object.keys(nets)) {
    for (const net of nets[name]) {
      const isV4 = net.family === 'IPv4' || net.family === 4;
      if (isV4 && !net.internal) return net.address;
    }
  }
  return '127.0.0.1';
}

/** CLI: APK_PATH=... PORT=3000 node scripts/server.js */
async function main() {
  const root = path.join(__dirname, '..');
  const apkPath =
    process.env.APK_PATH || path.join(root, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk');
  const port = Number(process.env.PORT) || DEFAULT_PORT;
  const ip = getLocalIPv4();

  const server = await startServer(apkPath, port, { localIp: ip });
  console.log(`[server] Serving file: ${path.resolve(apkPath)}`);
  process.on('SIGINT', () => {
    server.close(() => process.exit(0));
  });
}

if (require.main === module) {
  main().catch((err) => {
    console.error(err);
    process.exit(1);
  });
}

module.exports = { startServer, DEFAULT_PORT };
