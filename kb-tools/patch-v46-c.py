# v46-c: WelcomeScreen = demo (re-applies v44)
import re
B='/home/user/Karbama/app/src/main/java/com/karbama/app/'
p=B+'ui/screens/StartScreens.kt'; s=open(p,encoding='utf-8').read()
start='    SystemBars(darkIcons = false)\n    Column(\n        Modifier\n            .fillMaxSize()\n            .background(Bg)\n            .verticalScroll(rememberScrollState())\n    ) {\n        // هدر'
end='@Composable\nprivate fun HowRow(icon: ImageVector, text: String) {'
i=s.index(start); j=s.index(end,i)
# also remove old HowRow body till its closing (next "\n}\n" after end)
k=s.index('\n}\n', j)+3
new='''    SystemBars(darkIcons = false)
    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // ---------- v44/v46: هیروی دمو (.hero) ----------
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF088080), Color(0xFF0A7F91), Color(0xFF0A7F91))))
        ) {
            // دایره‌های تزئینی دمو (hero:before / hero:after)
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 70.dp, y = (-90).dp)
                    .size(230.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-40).dp, y = 80.dp)
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BrandLogo(size = 74.dp, onDark = true)
                Spacer(Modifier.height(16.dp))
                Text(loc["app_name"], fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text(
                    loc["app_latin"],
                    fontSize = 11.sp,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBox(loc.n(jobsCount), loc["stat_jobs"], Modifier.weight(1f))
                    StatBox(loc.n(peopleCount), loc["stat_people"], Modifier.weight(1f))
                    StatBox(loc.n(13), loc["stat_cities"], Modifier.weight(1f))
                }
            }
        }

        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp)) {
            Text(
                loc["welcome_title"],
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Ink
            )
            Spacer(Modifier.height(8.dp))
            Text(
                loc["welcome_sub"],
                fontSize = 14.sp,
                color = Ink2,
                lineHeight = 28.sp
            )
            Spacer(Modifier.height(16.dp))

            // دمو: 📋 تکمیل مشخصات متقاضی · 🔎 آگهی‌های استخدام · 📜 تعهدنامه
            HowRow("📋", loc["how_1"])
            HowRow("🔎", loc["how_2"])
            HowRow("📜", loc["how_3"])

            Spacer(Modifier.height(14.dp))
            PrimaryButton(text = "🚀 " + loc["start_btn"], onClick = onStart)
            Spacer(Modifier.height(10.dp))
            GhostButton(
                text = "🔑 " + loc["login_title"],
                onClick = onLogin
            )
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLanguage),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🌐 " + loc["language"] + ": ",
                    fontSize = 13.sp,
                    color = Ink3
                )
                Text(
                    langLabel(loc.code),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
            }
        }
    }
}

@Composable
private fun StatBox(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

/** ردیف «چطور کار می‌کند» دمو: کاشی ۳۴dp روشن با ایموجی + متن */
@Composable
private fun HowRow(emoji: String, text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BrandLight),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            fontSize = 13.sp,
            color = Ink2,
            fontWeight = FontWeight.Bold,
            lineHeight = 24.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
'''
s=s[:i]+new+s[k:]
open(p,'w',encoding='utf-8').write(s)

# how_1..3 = demo labels (regTitle / greetings / und_short)
p=B+'ui/i18n/Strings.kt'; s=open(p,encoding='utf-8').read()
vals={'fa':['تکمیل مشخصات متقاضی','آگهی‌های استخدام','تعهدنامه'],
      'tr':['Aday Bilgileri','İş İlanları','Taahhütname'],
      'en':['Applicant details','Job listings','Undertaking'],
      'ar':['إكمال بيانات المتقدم','إعلانات التوظيف','تعهّد']}
lines=s.split('\n')
for n in (1,2,3):
    idx=[i for i,l in enumerate(lines) if re.match(r'\s*"how_%d" to '%n,l)]
    assert len(idx)==4,(n,idx)
    # map order of maps: fa, tr, en, ar (file order)
    for i,lang in zip(idx,['fa','tr','en','ar']):
        ind=re.match(r'(\s*)',lines[i]).group(1)
        lines[i]=f'{ind}"how_{n}" to "{vals[lang][n-1]}",'
open(p,'w',encoding='utf-8').write('\n'.join(lines))
print('v46-c OK')
