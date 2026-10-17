#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""#26 اندروید: پنجره‌ی «مطمئن هستید؟» قبل از حذف آگهی (+ کلیدهای ترجمه)"""
import pathlib

BASE = pathlib.Path('/home/user/Karbama/app/src/main/java/com/karbama/app')

def patch(rel, pairs):
    p = BASE / rel
    s = p.read_text(); n0 = len(s)
    for old, new, expect in pairs:
        c = s.count(old)
        assert c == expect, f"{rel}: expected {expect}, found {c} for {old[:90]!r}"
        s = s.replace(old, new)
    p.write_text(s); print(f"  {rel}: {n0} → {len(s)}")

# ==================================================================
# ۱) کلیدهای ترجمه (سه زبان)
# ==================================================================
patch('ui/i18n/Strings.kt', [
 ('    "delete" to "حذف",',
  '''    "delete" to "حذف",
    "del_confirm_title" to "حذف آگهی",
    "del_confirm_sure" to "آیا مطمئن هستید؟ این آگهی برای همیشه حذف می‌شود و برگشتی ندارد.",
    "del_yes" to "بله، حذف شود",
    "del_no" to "انصراف",''', 1),
 ('    "delete" to "Sil",',
  '''    "delete" to "Sil",
    "del_confirm_title" to "İlanı sil",
    "del_confirm_sure" to "Emin misiniz? Bu ilan kalıcı olarak silinir ve geri alınamaz.",
    "del_yes" to "Evet, sil",
    "del_no" to "Vazgeç",''', 1),
 ('    "delete" to "Delete",',
  '''    "delete" to "Delete",
    "del_confirm_title" to "Delete listing",
    "del_confirm_sure" to "Are you sure? This listing will be deleted permanently and cannot be undone.",
    "del_yes" to "Yes, delete",
    "del_no" to "Cancel",''', 1),
])

# ==================================================================
# ۲) حالت «آگهی در انتظار حذف» + پنجره‌ی تأیید در AdminContent
# ==================================================================
patch('ui/screens/AdminScreens.kt', [
 ("    var statusFilter by remember { mutableStateOf(\"\") }",
  """    var statusFilter by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<Job?>(null) }   // #26: آگهی‌ای که منتظر تأیید حذف است""", 1),
 ("                                onDelete = { onDeleteJob(job) }",
  """                                onDelete = { pendingDelete = job }   // #26: اول می‌پرسد، بعد پاک می‌کند""", 1),
 ("""                                onClick = { onOpenApplicant(p) },
                                onSetStatus = { st -> onSetStatus(p.id, st) }
                            )
                        }
                    }
                }
            }
        }
    }
}""",
  """                                onClick = { onOpenApplicant(p) }
                                onSetStatus = { st -> onSetStatus(p.id, st) }
                            )
                        }
                    }
                }
            }
        }
    }

    // ---------- #26: تأیید حذف آگهی («مطمئن هستید؟») ----------
    val toDelete = pendingDelete
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = {
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(DangerLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🗑", fontSize = 24.sp)
                }
            },
            title = {
                Text(
                    loc["del_confirm_title"],
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandLight,
                        border = BorderStroke(1.dp, Color(0x3810B39F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = loc.jobTitle(toDelete.titleKey) +
                                (if (toDelete.company.isBlank()) "" else " · " + toDelete.company),
                            style = MaterialTheme.typography.labelMedium,
                            color = BrandDark,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 9.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        loc["del_confirm_sure"],
                        style = MaterialTheme.typography.bodySmall,
                        color = Graphite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteJob(toDelete)
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Danger,
                        contentColor = Color.White
                    )
                ) {
                    Text(loc["del_yes"], fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(loc["del_no"], color = Ink2)
                }
            }
        )
    }
}""", 1),
])

print("android #26 applied ✅")
