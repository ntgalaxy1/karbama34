# v46-a: re-apply the v45 changes (world languages + all dial codes)
import re, os
B='/home/user/Karbama/app/src/main/java/com/karbama/app/'
def rep(p, pairs):
    p=B+p; s=open(p,encoding='utf-8').read()
    for a,b in pairs:
        assert s.count(a)==1,(p,a[:90],s.count(a)); s=s.replace(a,b)
    open(p,'w',encoding='utf-8').write(s)

# ---------- Ui.kt ----------
rep('ui/components/Ui.kt',[
('import com.karbama.app.R\n','import com.karbama.app.R\nimport com.karbama.app.util.PhoneCountry\nimport com.karbama.app.util.WorldLanguages\n'),
('''    searchPlaceholder: String = "",
    emptyOptionLabel: String? = null
) {
    var open by remember { mutableStateOf(false) }''','''    searchPlaceholder: String = "",
    emptyOptionLabel: String? = null,
    /** تطبیق سفارشی جست‌وجو (value, query) — مثلاً نام انگلیسی کشور یا +کد */
    matcher: ((String, String) -> Boolean)? = null
) {
    var open by remember { mutableStateOf(false) }'''),
('''        else options.filter { it.second.contains(query, ignoreCase = true) }
        Dialog(onDismissRequest = { open = false }) {''','''        else options.filter {
            it.second.contains(query.trim(), ignoreCase = true) ||
                (matcher?.invoke(it.first, query.trim()) == true)
        }
        Dialog(onDismissRequest = { open = false }) {'''),
])
p=B+'ui/components/Ui.kt'; s=open(p,encoding='utf-8').read()
anchor='@Composable\nfun SelectChip('
assert s.count(anchor)==1
s=s.replace(anchor, open('/home/user/kb-tools/v46-multipicker.kt.txt',encoding='utf-8').read()+'\n'+anchor)
s+='\n'+open('/home/user/kb-tools/v46-pickers.kt.txt',encoding='utf-8').read()
open(p,'w',encoding='utf-8').write(s)

# ---------- data ----------
rep('data/model/Models.kt',[('    val jobKeys: List<String> = emptyList(),\n','    val jobKeys: List<String> = emptyList(),\n    val spokenLangs: List<String> = emptyList(),   // v45: «زبان‌هایی که بلدم» (کدهای ISO 639)\n'),
  ('    val phoneCountry: String = "TR",    // TR | IR','    val phoneCountry: String = "TR",    // ISO2 هر کشور (v45)')])
rep('data/repo/FirestoreMapper.kt',[('    jobKeys = strList("jobKeys"),\n','    jobKeys = strList("jobKeys"),\n    spokenLangs = strList("spokenLangs"),\n'),
  ('    "jobKeys" to jobKeys,\n','    "jobKeys" to jobKeys,\n    "spokenLangs" to spokenLangs,\n')])
rep('data/local/AppPrefs.kt',[('    put("jobKeys", JSONArray(jobKeys))\n','    put("jobKeys", JSONArray(jobKeys))\n    put("spokenLangs", JSONArray(spokenLangs))\n'),
  ('    jobKeys = o.optJSONArray("jobKeys").toStringList(),\n','    jobKeys = o.optJSONArray("jobKeys").toStringList(),\n    spokenLangs = o.optJSONArray("spokenLangs").toStringList(),\n')])
rep('data/sample/Catalog.kt',[('            phoneCountry = cc, phoneLocal = local, undertaking = und\n        )',
'''            phoneCountry = cc, phoneLocal = local, undertaking = und,
            spokenLangs = when (id) {      // v45: نمونه‌ی «زبان‌هایی که بلدم»
                "p1" -> listOf("fa", "tr")
                "p2" -> listOf("fa", "azb", "tr", "en")
                "p3" -> listOf("tr", "en", "de")
                "p4" -> listOf("fa", "en", "ar")
                "p5" -> listOf("fa", "bqi")
                "p6" -> listOf("fa", "ar")
                else -> listOf("fa")
            }
        )''')])

# ---------- Strings ----------
p=B+'ui/i18n/Strings.kt'; s=open(p,encoding='utf-8').read()
new={'fa':[('spoken_langs','زبان‌هایی که بلدم'),('spoken_langs_ph','انتخاب زبان‌ها…'),('spoken_langs_hint','همه‌ی زبان‌های زنده‌ی دنیا — هر چند زبان که بلدید انتخاب کنید'),('search_lang','جست‌وجوی زبان (فارسی یا انگلیسی)…'),('confirm_sel','تأیید'),('clear_sel','پاک کردن'),('search_country','جست‌وجوی کشور یا پیش‌شماره (مثلاً ۴۹+)…'),('err_phone_intl','شماره تلفن برای کشور انتخاب‌شده معتبر نیست'),('filter_spoken','زبان')],
 'tr':[('spoken_langs','Bildiğim diller'),('spoken_langs_ph','Dil seçin…'),('spoken_langs_hint','Dünyadaki tüm yaşayan diller — bildiğiniz kadarını seçin'),('search_lang','Dil ara…'),('confirm_sel','Tamam'),('clear_sel','Temizle'),('search_country','Ülke veya kod ara (örn. +49)…'),('err_phone_intl','Seçilen ülke için telefon numarası geçerli değil'),('filter_spoken','Dil')],
 'en':[('spoken_langs','Languages I speak'),('spoken_langs_ph','Choose languages…'),('spoken_langs_hint','All living world languages — pick as many as you speak'),('search_lang','Search language…'),('confirm_sel','Done'),('clear_sel','Clear'),('search_country','Search country or code (e.g. +49)…'),('err_phone_intl','The phone number is not valid for the selected country'),('filter_spoken','Language')],
 'ar':[('spoken_langs','اللغات التي أتقنها'),('spoken_langs_ph','اختر اللغات…'),('spoken_langs_hint','جميع لغات العالم الحية — اختر كل ما تتقنه'),('search_lang','ابحث عن لغة…'),('confirm_sel','تأكيد'),('clear_sel','مسح'),('search_country','ابحث عن دولة أو رمز (مثلاً +49)…'),('err_phone_intl','رقم الهاتف غير صالح للدولة المختارة'),('filter_spoken','اللغة')]}
lines=s.split('\n')
idx=[i for i,l in enumerate(lines) if re.match(r'\s*"phone_country" to ',l)]
assert len(idx)==4, idx
for i,lang in reversed(list(zip(idx,['fa','tr','en','ar']))):
    ind=re.match(r'(\s*)',lines[i]).group(1)
    lines[i+1:i+1]=[f'{ind}"{k}" to "{v}",' for k,v in new[lang]]
s='\n'.join(lines)
for k,_ in new['fa']: assert s.count(f'"{k}" to')==4,k
open(p,'w',encoding='utf-8').write(s)

# ---------- Registration ----------
rep('ui/screens/RegistrationScreen.kt',[
('    val jobKeys = remember { mutableStateListOf<String>() }\n','    val jobKeys = remember { mutableStateListOf<String>() }\n    var spokenLangs by remember { mutableStateOf(listOf<String>()) }   // v45\n'),
('''            !PhoneUtils.isValid(phoneCountry, phone) ->
                loc[if (phoneCountry == PhoneCountry.IRAN) "err_phone_ir" else "err_phone"]''',
'''            !PhoneUtils.isValid(phoneCountry, phone) -> loc[phoneErrorKey(phoneCountry)]'''),
('''                                            jobKeys = jobKeys.toList(),
                                            trLevel = level,''','''                                            jobKeys = jobKeys.toList(),
                                            spokenLangs = spokenLangs,
                                            trLevel = level,'''),
('''                        Text(
                            loc["phone_country"],
                            style = MaterialTheme.typography.labelMedium,
                            color = Ink2,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PhoneCountry.values().forEach { c ->
                                SelectChip(
                                    text = "${c.flag} +${c.code}",
                                    selected = phoneCountry == c,
                                    onClick = { phoneCountry = c; error = null }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        KbField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = loc["phone"],
                            placeholder = phoneCountry.example,''','''                        // v45: همه‌ی پیش‌شماره‌های دنیا با جست‌وجو
                        PhoneCountryPicker(
                            label = loc["phone_country"],
                            country = phoneCountry,
                            uiLang = loc.code,
                            searchPlaceholder = loc["search_country"],
                            onSelect = { phoneCountry = it; error = null }
                        )
                        Spacer(Modifier.height(12.dp))
                        KbField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = loc["phone"],
                            placeholder = phoneCountry.example.ifEmpty { "123 456 789" },'''),
('''                        Text(
                            "🗣 " + loc.levelBand(level),
                            style = MaterialTheme.typography.titleSmall,
                            color = BrandDark,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }''','''                        Text(
                            "🗣 " + loc.levelBand(level),
                            style = MaterialTheme.typography.titleSmall,
                            color = BrandDark,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        // v45: «زبان‌هایی که بلدم» — همه‌ی زبان‌های زنده‌ی دنیا
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Line)
                        Spacer(Modifier.height(16.dp))
                        SpokenLangsField(
                            loc = loc,
                            selected = spokenLangs,
                            onChange = { spokenLangs = it }
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            loc["spoken_langs_hint"],
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink3
                        )
                    }'''),
('fun isValidTrPhone(input: String): Boolean = PhoneUtils.isValid(PhoneCountry.TURKIYE, input)\n',
'''fun isValidTrPhone(input: String): Boolean = PhoneUtils.isValid(PhoneCountry.TURKIYE, input)

/** v45: کلید پیام خطای شماره بر اساس کشور */
fun phoneErrorKey(c: PhoneCountry): String = when (c.iso) {
    "IR" -> "err_phone_ir"
    "TR" -> "err_phone"
    else -> "err_phone_intl"
}
'''),
])

# ---------- Profile edit ----------
rep('ui/screens/FormScreens.kt',[
('    val jobKeys = remember { mutableStateListOf<String>().apply { addAll(applicant.jobKeys) } }\n',
 '    val jobKeys = remember { mutableStateListOf<String>().apply { addAll(applicant.jobKeys) } }\n    var spokenLangs by remember { mutableStateOf(applicant.spokenLangs) }   // v45\n'),
('''                                !PhoneUtils.isValid(phoneCountry, phone) ->
                                    error = loc[if (phoneCountry == PhoneCountry.IRAN) "err_phone_ir" else "err_phone"]''',
'''                                !PhoneUtils.isValid(phoneCountry, phone) ->
                                    error = loc[phoneErrorKey(phoneCountry)]'''),
('''                                            jobKeys = jobKeys.toList(),
                                            trLevel = level
''','''                                            jobKeys = jobKeys.toList(),
                                            spokenLangs = spokenLangs,
                                            trLevel = level
'''),
('''                Text(
                    loc["phone_country"],
                    style = MaterialTheme.typography.labelMedium,
                    color = Ink2,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PhoneCountry.values().forEach { c ->
                        SelectChip(
                            text = "${c.flag} +${c.code}",
                            selected = phoneCountry == c,
                            onClick = { phoneCountry = c }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                KbField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = loc["phone"],
                    placeholder = phoneCountry.example,''','''                PhoneCountryPicker(
                    label = loc["phone_country"],
                    country = phoneCountry,
                    uiLang = loc.code,
                    searchPlaceholder = loc["search_country"],
                    onSelect = { phoneCountry = it }
                )
                Spacer(Modifier.height(12.dp))
                KbField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = loc["phone"],
                    placeholder = phoneCountry.example.ifEmpty { "123 456 789" },'''),
('''                Text(
                    loc.levelBand(level),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(24.dp))''','''                Text(
                    loc.levelBand(level),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Line)
                Spacer(Modifier.height(16.dp))
                SpokenLangsField(
                    loc = loc,
                    selected = spokenLangs,
                    onChange = { spokenLangs = it }
                )
            }
            Spacer(Modifier.height(24.dp))'''),
])

# ---------- Profile display ----------
rep('ui/screens/ProfileScreen.kt',[
('import com.karbama.app.ui.theme.*\n','import com.karbama.app.ui.theme.*\nimport com.karbama.app.util.WorldLanguages\n'),
('''                    Text(
                        loc.levelBand(applicant.trLevel),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ---------------- مدارک ----------------''','''                    Text(
                        loc.levelBand(applicant.trLevel),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    // v45: «زبان‌هایی که بلدم»
                    Spacer(Modifier.height(16.dp))
                    SectionTitle("🗣 " + loc["spoken_langs"])
                    Spacer(Modifier.height(8.dp))
                    Text(
                        WorldLanguages.labels(applicant.spokenLangs, loc.code).ifEmpty { "—" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ---------------- مدارک ----------------'''),
])
print("v46-a OK")
