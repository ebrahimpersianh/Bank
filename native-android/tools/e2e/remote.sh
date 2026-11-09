#!/bin/bash
# آزمایشِ راه‌دور: روی رانرِ گیت‌هاب، فرمان‌ها را از شاخه‌ی e2e-cmd می‌خواند (e2e/cmd.sh + e2e/seq)،
# اجرا می‌کند و عکس/متنِ صفحه را در شاخه‌ی e2e-out می‌گذارد. Claude از لپ‌تاپ گوشی را می‌گرداند.
set -u
cd "$GITHUB_WORKSPACE"
export E2E="$GITHUB_WORKSPACE/native-android/tools/e2e"
export OUT=/tmp/e2eout
end=$(( $(date +%s) + ${E2E_MINUTES:-60} * 60 ))
adb install -r -g native-android/app/build/outputs/apk/myket/debug/app-myket-debug.apk
adb install -r -g "$E2E/fakeblu.apk"
adb shell cmd notification allow_listener ir.sadteam.loancalc/ir.sadteam.loancalc.notifications.BankNotificationListener || true
adb shell settings put system screen_off_timeout 1800000
git config user.name "github-actions[bot]"; git config user.email "github-actions[bot]@users.noreply.github.com"
git worktree add -B e2e-out "$OUT" HEAD >/dev/null
mkdir -p "$OUT/e2e"
last=0
publish() {
  adb exec-out screencap -p > "$OUT/e2e/s.png"
  python3 "$E2E/ui.py" > "$OUT/e2e/ui.txt" 2>&1
  echo "$1" > "$OUT/e2e/done"
  (cd "$OUT" && git add -A e2e && git commit -q --amend -m "e2e $1" 2>/dev/null || git commit -q -m "e2e $1"; git push -q -f origin e2e-out)
}
(cd "$OUT" && git add -A e2e; git commit -q -m "e2e start"; true)
echo "start" > "$OUT/e2e/log.txt"
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
