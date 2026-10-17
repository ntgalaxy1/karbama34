const puppeteer = require('/tmp/node_modules/puppeteer-core');
const sleep = ms => new Promise(r => setTimeout(r, ms));
const clean = async p => { await p.evaluate(() => { try{ S.toast=null; document.querySelectorAll('.toast').forEach(e=>e.remove()); }catch(e){} }); await sleep(150); };
(async () => {
  const b = await puppeteer.launch({ executablePath: '/usr/bin/chromium', headless: 'new', args: ['--no-sandbox','--disable-dev-shm-usage'] });
  const p = await b.newPage();
  await p.setViewport({ width: 420, height: 900, deviceScaleFactor: 2 });
  await p.goto('file:///home/user/karbama-demo.html', { waitUntil: 'networkidle0' });
  await sleep(700);
  // پنجره‌ی تأیید حذف
  await p.evaluate(() => { S.lang='fa'; S.isAdmin=true; S.adminTab='jobs'; S.screen='admin'; render(); });
  await sleep(400);
  await p.evaluate(() => { delJob(S.jobs[0].id); });
  await sleep(600); await clean(p);
  await p.screenshot({ path: '/tmp/g26-confirm.png' });
  // دکمه‌ی ثابت: اسکرول وسط فهرست
  await p.evaluate(() => { closeConfirm(); const v=document.getElementById('view'); v.scrollTop = 700; });
  await sleep(500); await clean(p);
  await p.screenshot({ path: '/tmp/g26-fab-scrolled.png' });
  await b.close(); console.log('shots ok');
})();
