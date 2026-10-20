# v46-d: small fixes + demo emoji icons (run after v46-a, v46-b, v46-c)
B='/home/user/Karbama/app/src/main/java/com/karbama/app/'
def rep(f, pairs):
    p=B+f; s=open(p,encoding='utf-8').read()
    for a,b in pairs:
        assert s.count(a)==1,(f,a[:60],s.count(a)); s=s.replace(a,b)
    open(p,'w',encoding='utf-8').write(s)
def addimp(f, imp, after):
    p=B+f; s=open(p,encoding='utf-8').read()
    if imp+'\n' not in s:
        assert s.count(after+'\n')==1,(f,after); s=s.replace(after+'\n',after+'\n'+imp+'\n')
    open(p,'w',encoding='utf-8').write(s)

p=B+'ui/screens/AdminScreens.kt'; s=open(p,encoding='utf-8').read()
if not s.startswith('@file:OptIn'):
    s='@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)\n\n'+s
open(p,'w',encoding='utf-8').write(s)

addimp('ui/screens/StartScreens.kt','import androidx.compose.foundation.shape.CircleShape','import androidx.compose.foundation.shape.RoundedCornerShape')
addimp('MainActivity.kt','import androidx.compose.ui.unit.sp','import androidx.compose.ui.unit.dp')

rep('MainActivity.kt',[('''                            Icon(
                                if (adsUnlocked) Icons.Filled.Home else Icons.Filled.Lock,
                                contentDescription = null
                            )''','''                            Text(if (adsUnlocked) "🏠" else "🔒", fontSize = 20.sp)   // v46: ایموجی دمو'''),
 ('icon = { Icon(Icons.Filled.Person, contentDescription = null) },','icon = { Text("👤", fontSize = 20.sp) },'),
 ('icon = { Icon(Icons.Filled.Call, contentDescription = null) },','icon = { Text("☎️", fontSize = 20.sp) },')])
rep('ui/screens/HomeScreen.kt',[
 ('Icon(Icons.Filled.Language, contentDescription = null, tint = Color.White)','Text("🌐", fontSize = 20.sp)'),
 ('Icon(Icons.Filled.Search, contentDescription = null, tint = Ink3)','Text("🔎", fontSize = 16.sp)'),
 ('Icon(Icons.Filled.Refresh, contentDescription = null, tint = Danger)','Text("🔄", fontSize = 22.sp)')])
rep('ui/components/JobCard.kt',[
 ('''Icon(
                            Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = Ink3,
                            modifier = Modifier.size(13.dp)''','''Text(
                            "🕒",
                            fontSize = 11.sp'''),
 ('Icon(Icons.Filled.LocationOn, contentDescription = null, tint = BrandDark, modifier = Modifier.size(13.dp))','Text("📍", fontSize = 11.sp)')])
rep('ui/screens/JobDetailScreen.kt',[('''Icon(
                                    Icons.Filled.Business,
                                    contentDescription = null,
                                    tint = Ink3,
                                    modifier = Modifier.size(14.dp)''','''Text(
                                    "🏢",
                                    fontSize = 12.sp''')])
rep('ui/screens/ProfileScreen.kt',[('''Icon(
                                    Icons.Filled.Favorite,
                                    contentDescription = loc["unlike"],
                                    tint = Pink
                                )''','Text("❤️", fontSize = 18.sp)')])
rep('ui/screens/LoginScreen.kt',[('Icon(Icons.Filled.Security, contentDescription = null, tint = BrandDark)','Text("🔑", fontSize = 20.sp)')])
print('v46-d OK')
