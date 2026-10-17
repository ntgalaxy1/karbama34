#!/bin/bash
# نصب ابزار تست مرورگری روی سندباکس تازه (puppeteer-core + Chrome for Testing + کتابخانه‌های سیستمی)
# اجرا:  bash kb-tools/setup-tools.sh
set -e
mkdir -p /tmp/node_modules /tmp/browsers
cd /tmp
if [ ! -d /tmp/node_modules/puppeteer-core ]; then
  npm i puppeteer-core@21.11.0 --no-save --prefix /tmp > /tmp/npm-pptr.log 2>&1
fi
if [ ! -x /tmp/browsers/chrome/linux-155.0.8059.79/chrome-linux64/chrome ]; then
  npx --yes @puppeteer/browsers install chrome@155.0.8059.79 --path /tmp/browsers > /tmp/chrome-dl.log 2>&1
fi
sudo ln -sf /tmp/browsers/chrome/linux-155.0.8059.79/chrome-linux64/chrome /usr/bin/chromium
if ! /usr/bin/chromium --version >/dev/null 2>&1; then
  sudo apt-get update -qq > /tmp/apt-update.log 2>&1
  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y -qq \
    libatk1.0-0 libatk-bridge2.0-0 libcups2 libdrm2 libxkbcommon0 libxcomposite1 \
    libxdamage1 libxfixes3 libxrandr2 libgbm1 libasound2t64 libpango-1.0-0 libcairo2 \
    libnss3 libnspr4 libglib2.0-0 libx11-xcb1 libxcb1 libxext6 libxi6 libxtst6 \
    fonts-liberation libappindicator3-1 > /tmp/apt-chrome.log 2>&1
fi
/usr/bin/chromium --version
echo "=== TOOLS READY ==="
