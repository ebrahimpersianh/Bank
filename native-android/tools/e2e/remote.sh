#!/bin/bash
# آزمایشِ راه‌دور: روی رانرِ گیت‌هاب، فرمان‌ها را از شاخه‌ی e2e-cmd می‌خواند (e2e/cmd.sh + e2e/seq)،
# اجرا می‌کند و عکس/متنِ صفحه را در شاخه‌ی e2e-out می‌گذارد. Claude از لپ‌تاپ گوشی را می‌گرداند.
set -u
cd "$GITHUB_WORKSPACE"
export E2E="$GITHUB_WORKSPACE/native-android/tools/e2e"
export OUT=/tmp/e2eout
end=$(( $(date +%s) + ${E2E_MINUTES:-60} * 60 ))
# ۲۵ آبان: روی API 24 نصب بی‌صدا گیر کرد (~۹۸ دقیقه). حالا: صبر تا آماده‌شدنِ package manager،
# هر نصب با تایم‌اوت و تکرار، و هر مرحله در لاگ؛ اگر نصب نشد هم جلسه شروع می‌شود و خطا در log.txt است.
SETUP=/tmp/setup.log; : > "$SETUP"
say() { echo "[$(date +%H:%M:%S)] $*" | tee -a "$SETUP"; }
say "api=$(timeout 30 adb shell getprop ro.build.version.sdk | tr -d '')"
for i in $(seq 1 60); do
  timeout 20 adb shell pm path android 2>/dev/null | grep -q package: && break
  say "package manager not ready ($i)"; sleep 5
done
inst() {
  for t in 1 2 3; do
    say "install $(basename "$1") try $t"
    if timeout 240 adb install -r -g "$1" 2>&1 | tee -a "$SETUP" | grep -q Success; then say "ok"; return 0; fi
    say "failed/timeout; restarting adb"; adb kill-server; sleep 3; timeout 120 adb wait-for-device; sleep 10
  done
  say "GAVE UP: $1"; return 1
}
inst native-android/app/build/outputs/apk/myket/debug/app-myket-debug.apk
inst "$E2E/fakeblu.apk"
timeout 30 adb shell cmd notification allow_listener ir.sadteam.loancalc/ir.sadteam.loancalc.notifications.BankNotificationListener || true
timeout 30 adb shell settings put system screen_off_timeout 1800000 || true
say "setup done"
git config user.name "github-actions[bot]"; git config user.email "github-actions[bot]@users.noreply.github.com"
git worktree add -B e2e-out "$OUT" HEAD >/dev/null
mkdir -p "$OUT/e2e"
last=0
publish() {
  timeout 60 adb exec-out screencap -p > "$OUT/e2e/s.png"
  timeout 90 python3 "$E2E/ui.py" > "$OUT/e2e/ui.txt" 2>&1
  echo "$1" > "$OUT/e2e/done"
  (cd "$OUT" && git add -A e2e && git commit -q --amend -m "e2e $1" 2>/dev/null || git commit -q -m "e2e $1"; git push -q -f origin e2e-out)
}
(cd "$OUT" && git add -A e2e; git commit -q -m "e2e start"; true)
cp "$SETUP" "$OUT/e2e/log.txt"
publish 0
while [ "$(date +%s)" -lt "$end" ]; do
  git fetch -q origin e2e-cmd 2>/dev/null || { sleep 4; continue; }
  seq=$(git show origin/e2e-cmd:e2e/seq 2>/dev/null || echo 0)
  if [ "$seq" -gt "$last" ] 2>/dev/null; then
    last=$seq
    git show origin/e2e-cmd:e2e/cmd.sh > /tmp/cmd.sh
    [ "$(head -1 /tmp/cmd.sh)" = "#END" ] && break
    timeout 300 bash -c "source '$E2E/h.sh'; source /tmp/cmd.sh" > "$OUT/e2e/log.txt" 2>&1
    sleep 1
    publish "$seq"
  fi
  sleep 3
done
echo "remote session finished"
