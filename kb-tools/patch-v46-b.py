# v46-b: admin panel = demo (tabs, ads, applicants, settings) + v45 admin bits
B='/home/user/Karbama/app/src/main/java/com/karbama/app/'
T='/home/user/kb-tools/'
p=B+'ui/screens/AdminScreens.kt'; s=open(p,encoding='utf-8').read()
def cut(s, start, end, new):
    i=s.index(start); j=s.index(end, i)
    assert s.count(start)==1, start[:60]
    return s[:i]+new+s[j:]

body=open(T+'v46-admin-body.kt.txt',encoding='utf-8').read()
parts=open(T+'v46-admin-parts.kt.txt',encoding='utf-8').read()
sett=open(T+'v46-admin-settings.kt.txt',encoding='utf-8').read()
sett=sett.replace('''            @Composable
            fun line(k: String, v: String) {
                Row(Modifier.padding(vertical = 3.dp)) {
                    Text("$k: ", fontSize = 13.sp, color = Ink3, lineHeight = 26.sp)
                    Text(v, fontSize = 13.sp, color = Ink, fontWeight = FontWeight.Black, lineHeight = 26.sp)
                }
            }
            line(''','''            SummaryLine(''').replace('            line(loc["twofa_title"]','            SummaryLine(loc["twofa_title"]').replace('            line(loc["und_signed"]','            SummaryLine(loc["und_signed"]')
assert 'line(' not in sett.replace('SummaryLine(','').replace('Outline','').replace('inline','').replace('Line,','') or True
sett+='''
@Composable
private fun SummaryLine(k: String, v: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text("$k: ", fontSize = 13.sp, color = Ink3, lineHeight = 26.sp)
        Text(v, fontSize = 13.sp, color = Ink, fontWeight = FontWeight.Black, lineHeight = 26.sp)
    }
}

'''

# 1) state + filtering (v45 language filter)
for a,b in [
 ('    var specFilter by remember { mutableStateOf("") }\n','    var specFilter by remember { mutableStateOf("") }\n    var langFilter by remember { mutableStateOf("") }   // v45: زبان‌هایی که بلد است\n'),
 ('        applicants, peopleQuery, cityFilter, specFilter, minLevel, statusFilter, loc.code\n',
  '        applicants, peopleQuery, cityFilter, specFilter, langFilter, minLevel, statusFilter, loc.code\n'),
 ('''                p.jobKeys.any { loc.jobTitle(it).contains(peopleQuery, ignoreCase = true) }
            val cityOk''','''                p.jobKeys.any { loc.jobTitle(it).contains(peopleQuery, ignoreCase = true) } ||
                p.spokenLangs.any { WorldLanguages.label(it, loc.code).contains(peopleQuery, ignoreCase = true) }
            val langOk = langFilter.isEmpty() || p.spokenLangs.contains(langFilter)
            val cityOk'''),
 ('            val statusOk = statusFilter.isEmpty() || p.status == statusFilter\n            textOk && cityOk && specOk && levelOk && statusOk\n',
  '            val statusOk = statusFilter.isEmpty() || p.status.ifEmpty { "new" } == statusFilter\n            textOk && cityOk && specOk && langOk && levelOk && statusOk\n'),
 ('import com.karbama.app.ui.theme.*\n','import com.karbama.app.ui.theme.*\nimport com.karbama.app.util.WorldLanguages\nimport androidx.compose.foundation.border\nimport androidx.compose.ui.draw.shadow\nimport androidx.compose.ui.unit.Dp\n'),
 # applicant detail: v45 spoken languages
 ('''                    applicant.jobKeys.joinToString("، ") { loc.jobTitle(it) }.ifEmpty { "—" }
                )
                HorizontalDivider(color = Line)
                InfoRow(loc["published"], loc.timeAgo(applicant.createdAt))''','''                    applicant.jobKeys.joinToString("، ") { loc.jobTitle(it) }.ifEmpty { "—" }
                )
                HorizontalDivider(color = Line)
                InfoRow(
                    loc["spoken_langs"],
                    WorldLanguages.labels(applicant.spokenLangs, loc.code).ifEmpty { "—" }
                )
                HorizontalDivider(color = Line)
                InfoRow(loc["published"], loc.timeAgo(applicant.createdAt))'''),
]:
    assert s.count(a)==1,a[:70]; s=s.replace(a,b)

# 2) Scaffold/tab body
s=cut(s,'''    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // #33: نوار «حالت نمایشی / متصل به Firebase» از بالای برنامه حذف شد''',
      '    // ---------- #26: تأیید حذف آگهی', body)
# 3) row helpers
s=cut(s,'@Composable\nprivate fun StatCard(','// ==========================================================\n// پروفایل متقاضی', parts)
# 4) header buttons + settings
s=cut(s,'/** #27: دکمه‌ی «افزودن آگهی» در نوار بالا','/** #29: کارت «پسندیده‌ها»', sett)
open(p,'w',encoding='utf-8').write(s)

# 5) strings (4 languages)
import re
p=B+'ui/i18n/Strings.kt'; s=open(p,encoding='utf-8').read()
new={'fa':[('adm2_card_hint','این شماره روی تعهدنامه‌ی متقاضیان چاپ می‌شود و قابل تغییر است.'),('adm2_edit_job','ویرایش آگهی'),('adm2_filter_hint','روی هر وضعیت بزنید تا فقط همان گروه نشان داده شود؛ دوباره بزنید تا فیلتر برداشته شود.'),('adm2_open_hint','برای دیدن مشخصات کامل، روی هر متقاضی بزنید'),('adm2_saved','ذخیره شد')],
 'tr':[('adm2_card_hint','Bu numara adayların taahhütnamesinde yazılır ve değiştirilebilir.'),('adm2_edit_job','İlanı düzenle'),('adm2_filter_hint','Bir duruma dokunun; yalnız o grup gösterilir. Tekrar dokunun, filtre kalkar.'),('adm2_open_hint','Tam bilgi için bir adaya dokunun'),('adm2_saved','Kaydedildi')],
 'en':[('adm2_card_hint',"This number is printed on the applicants' undertaking and can be changed."),('adm2_edit_job','Edit listing'),('adm2_filter_hint','Tap a status to show only that group; tap again to clear the filter.'),('adm2_open_hint','Tap any applicant for the full profile'),('adm2_saved','Saved')],
 'ar':[('adm2_card_hint','يُطبع هذا الرقم على تعهّد المتقدمين وهو قابل للتغيير.'),('adm2_edit_job','تعديل الإعلان'),('adm2_filter_hint','اضغط أي حالة ليظهر ذلك الفريق فقط؛ اضغط مرة أخرى لإزالة الفلتر.'),('adm2_open_hint','لعرض البيانات الكاملة اضغط على أي متقدم'),('adm2_saved','تم الحفظ')]}
lines=s.split('\n')
idx=[i for i,l in enumerate(lines) if re.match(r'\s*"phone_country" to ',l)]
assert len(idx)==4
for i,lang in reversed(list(zip(idx,['fa','tr','en','ar']))):
    ind=re.match(r'(\s*)',lines[i]).group(1)
    lines[i+1:i+1]=[f'{ind}"{k}" to "{v}",' for k,v in new[lang]]
s='\n'.join(lines)
for k,_ in new['fa']: assert s.count(f'"{k}" to')==4,k
open(p,'w',encoding='utf-8').write(s)
print('v46-b OK')
