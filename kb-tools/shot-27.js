const puppeteer = require('/tmp/node_modules/puppeteer-core');
const fs = require('fs');
(async () => {
  const browser = await puppeteer.launch({ executablePath: '/usr/bin/chromium', headless: 'new',
    args: ['--no-sandbox', '--disable-dev-shm-usage'] });
  fs.mkdirSync('/home/user/kb-tools/shots', { recursive: true });
  for (const [w, h, tag] of [[420, 880, '420'], [360, 780, '360']]) {
    const page = await browser.newPage();
    await page.setViewport({ width: w, height: h, deviceScaleFactor: 2 });
    await page.goto('file:///home/user/karbama-demo.html');
    await page.evaluate(() => localStorage.clear());
    await page.reload();
    await page.waitForFunction(() => document.querySelector('#view button'));
    await page.evaluate(() => { const b = document.querySelector('#view .langbtn'); if (b) b.click(); });
    await new Promise(r => setTimeout(r, 500));
    await page.evaluate(() => { S.lang = 'fa'; S.screen = 'admin'; S.adminTab = 'jobs'; S.admin = true; render(); });
    await new Promise(r => setTimeout(r, 600));
    await page.screenshot({ path: `/home/user/kb-tools/shots/appbar-${tag}.png` });
    // باز کردن پنجره‌ی تأیید حذف
    await page.evaluate(() => { const b = [...document.querySelectorAll('#view button')].find(x => /حذف/.test(x.textContent) && !/ویرایش/.test(x.textContent)); if (b) b.click(); return !!b; });
    await new Promise(r => setTimeout(r, 600));
    await page.screenshot({ path: `/home/user/kb-tools/shots/confirm-${tag}.png` });
    await page.close();
  }
  await browser.close(); console.log('shots ok');
})();
