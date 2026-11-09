# توابعِ کمکی برای فرمان‌های راه‌دور (source می‌شود).
tap() { python3 "$E2E/tap.py" "$@"; sleep "${WAIT:-2}"; }
ui() { python3 "$E2E/ui.py"; }
shot() { mkdir -p "$OUT/e2e/shots"; adb exec-out screencap -p > "$OUT/e2e/shots/$1.png"; }
swipe_up() { adb shell input swipe 540 1700 540 700 400; sleep 1.5; }
swipe_down() { adb shell input swipe 540 700 540 1700 400; sleep 1.5; }
back() { adb shell input keyevent 4; sleep 1.5; }
txt() { adb shell input text "$1"; sleep 1; }
# اعلانِ بانکیِ ساختگی از برنامه‌ی آزمایشیِ «blu»
notif() { adb shell am broadcast -n ir.sadteam.testblu/.Post --es t "$(printf %s "$1" | base64 -w0)" --es b "$(printf %s "$2" | base64 -w0)"; sleep 3; }
