/* ساخت همه‌ی باندل‌های تست بدون مرورگر: stub + demo + tests */
const fs = require('fs');
const pairs = [
  ['tests-media.js',   'run-media-impl.js'],
  ['tests-contact.js', 'run-contact-impl.js'],
  ['tests-tabs.js',    'run-tabs.js'],
  ['tests-fee.js',     'run-fee.js'],
  ['tests-regress.js', 'run-regress.js'],
  ['tests-2fa.js',     'run-2fa.js'],
  ['tests-paywall.js', 'run-paywall.js']
];
const rd = f => fs.readFileSync('/home/user/kb-tools/' + f, 'utf8');
for (const [t, out] of pairs) {
  if (!fs.existsSync('/home/user/kb-tools/' + t)) continue;
  fs.writeFileSync('/home/user/kb-tools/' + out, [rd('stub.js'), rd('demo.js'), rd(t)].join('\n'));
  console.log('ساخته شد:', out);
}
