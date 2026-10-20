#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────
#  push-to-github.sh — اتصال مخزن محلی کارباما به GitHub
#
#  استفاده:
#    GITHUB_USER=ntgalaxy GITHUB_TOKEN=ghp_xxx bash push-to-github.sh
#    (یا)  bash push-to-github.sh ntgalaxy ghp_xxx
#
#  کارهایی که انجام می‌دهد:
#    ۱) بررسی اعتبار توکن و دسترسی به مخزن
#    ۲) تنظیم remote با URL امن (توکن فقط در .git/config می‌ماند، نه در تاریخچه)
#    ۳) push شاخه‌ی main
#    ۴) پاک‌کردن توکن از remote پس از موفقیت
# ─────────────────────────────────────────────────────────────
set -euo pipefail

REPO="${REPO:-karbama34}"
USER_NAME="${1:-${GITHUB_USER:-}}"
TOKEN="${2:-${GITHUB_TOKEN:-}}"

if [[ -z "$USER_NAME" || -z "$TOKEN" ]]; then
  echo "✗ یوزرنیم یا توکن خالی است."
  echo "  مثال: GITHUB_USER=ntgalaxy GITHUB_TOKEN=ghp_xxx bash push-to-github.sh"
  exit 1
fi

cd "$(dirname "$0")"

echo "→ ۱) بررسی توکن…"
CHECK=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/vnd.github+json" \
  "https://api.github.com/user")
if [[ "$CHECK" != "200" ]]; then
  echo "✗ توکن نامعتبر یا منقضی است (HTTP $CHECK)."
  exit 2
fi
echo "   ✓ توکن معتبر است"

echo "→ ۲) بررسی مخزن $USER_NAME/$REPO…"
RC=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/vnd.github+json" \
  "https://api.github.com/repos/$USER_NAME/$REPO")
case "$RC" in
  200) echo "   ✓ مخزن موجود است" ;;
  404) echo "✗ مخزن پیدا نشد (یا توکن به آن دسترسی ندارد)."; exit 3 ;;
  *)   echo "✗ خطا در دسترسی به مخزن (HTTP $RC)."; exit 3 ;;
esac

echo "→ ۳) تنظیم remote…"
git remote remove origin 2>/dev/null || true
git remote add origin "https://${USER_NAME}:${TOKEN}@github.com/${USER_NAME}/${REPO}.git"
git branch -M main

echo "→ ۴) push…"
if git push -u origin main; then
  echo "→ ۵) پاک‌کردن توکن از remote…"
  git remote set-url origin "https://github.com/${USER_NAME}/${REPO}.git"
  echo
  echo "✅ انجام شد:"
  echo "   https://github.com/${USER_NAME}/${REPO}"
  echo
  echo "   یادآوری: APK را در Releases آپلود کن →"
  echo "   https://github.com/${USER_NAME}/${REPO}/releases/new"
else
  echo "✗ push ناموفق بود. توکن را از remote پاک می‌کنم…"
  git remote set-url origin "https://github.com/${USER_NAME}/${REPO}.git"
  exit 4
fi
