# v49: applicant-side writes compatible with the published Firestore rules
#      (applicants never write paymentAt; profile save merges only profile fields)
B='/home/user/Karbama/app/src/main/java/com/karbama/app/'
def rep(f, pairs):
    p=B+f; s=open(p,encoding='utf-8').read()
    for a,b in pairs:
        assert s.count(a)==1,(f,a[:80],s.count(a)); s=s.replace(a,b)
    open(p,'w',encoding='utf-8').write(s)
rep('data/repo/ApplicantRepository.kt',[
('            db.collection(Config.COL_APPLICANTS).document(uid).set(saved.toMap()).await()',
 '''            // v49: «paymentAt» را فقط مدیر می‌نویسد (قوانین Firestore) → هنگام ساخت فرستاده نمی‌شود
            db.collection(Config.COL_APPLICANTS).document(uid).set(saved.toMap() - "paymentAt").await()'''),
('''            db.collection(Config.COL_APPLICANTS).document(applicant.id).update(
                mapOf(
                    "paymentStatus" to saved.payStatus,
                    "paymentAt" to saved.payAt,
                    "paymentMethod" to saved.payMethod,''','''            // v49: بدون paymentAt — زمان تأیید را مدیر ثبت می‌کند
            db.collection(Config.COL_APPLICANTS).document(applicant.id).update(
                mapOf(
                    "paymentStatus" to saved.payStatus,
                    "paymentMethod" to saved.payMethod,'''),
('''        return runCatching {
            db.collection(Config.COL_APPLICANTS).document(applicant.id)
                .set(applicant.toMap()).await()
        }
    }
}''','''        return runCatching {
            // v49: فقط فیلدهای پروفایل، به‌صورت ادغام (merge):
            //  · فیلدهای پرداخت را فقط مسیر پرداخت/مدیر تغییر می‌دهد (قوانین Firestore)
            //  · «status» مال مدیر است و نباید با نسخه‌ی قدیمی گوشی بازنویسی شود
            db.collection(Config.COL_APPLICANTS).document(applicant.id)
                .set(applicant.toMap() - ADMIN_OR_PAYMENT_KEYS, com.google.firebase.firestore.SetOptions.merge())
                .await()
        }
    }

    private val ADMIN_OR_PAYMENT_KEYS = setOf(
        "status", "paymentStatus", "paymentAt", "paymentMethod", "paymentReference", "paymentReceiptUrl"
    )
}'''),
])
print('v49-a OK')
