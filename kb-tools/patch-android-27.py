import pathlib
p = pathlib.Path('/home/user/Karbama/app/src/main/java/com/karbama/app/ui/screens/AdminScreens.kt')
s = p.read_text(); n0 = len(s); errs = []

# 1) FAB اضافه شد به اپبار: حذف بلوک floatingActionButton
old_fab = """        floatingActionButton = {
            if (tab == 0) {
                FloatingActionButton(
                    onClick = onNewJob,
                    containerColor = Accent,
                    contentColor = Color(0xFF3D2606)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = loc["add_listing"])
                }
            }
        }
"""
if old_fab in s:
    s = s.replace(old_fab, "", 1)
else:
    errs.append('fab block')

# 2) دکمه‌ی افزودن آگهی در اپ‌بار، کنار دکمه‌ی خروج (#27)
old_bar = """            TealTopBar(title = loc["admin_panel"]) {
                AdminExitButton(text = loc["logout"], onClick = onExit)
            }"""
new_bar = """            TealTopBar(title = loc["admin_panel"]) {
                AdminAddButton(text = loc["add_listing"], onClick = onNewJob)   // #27
                AdminExitButton(text = loc["logout"], onClick = onExit)
            }"""
if old_bar in s:
    s = s.replace(old_bar, new_bar, 1)
else:
    errs.append('topbar')

# 3) دیگر FAB نیست ⇒ پدینگ پایین فهرست آگهی‌ها از ۹۶ به ۲۴
old_pad = """                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 96.dp),"""
new_pad = """                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp),"""
if old_pad in s:
    s = s.replace(old_pad, new_pad, 1)
else:
    errs.append('padding')

# 4) کامپوزبل AdminAddButton با گرادیان طلایی، هم‌سبک دمو
old_exit = """private fun AdminExitButton(text: String, onClick: () -> Unit) {"""
add_btn = '''/** #27: دکمه‌ی «افزودن آگهی» در نوار بالا — ثابت، بدون هم‌پوشانی با «حذف» */
@Composable
private fun AdminAddButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(1.4.dp, Color.White.copy(alpha = 0.65f)),
        shadowElevation = 6.dp
    ) {
        Row(
            Modifier
                .background(Brush.linearGradient(listOf(Color(0xFFFFD15C), Color(0xFFF59E0B))))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = null,
                tint = Color(0xFF3D2606),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                color = Color(0xFF3D2606),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun AdminExitButton(text: String, onClick: () -> Unit) {'''
if old_exit in s:
    s = s.replace(old_exit, add_btn, 1)
else:
    errs.append('exit anchor')

p.write_text(s)
print('errs=', errs, 'len', n0, '->', len(s))
