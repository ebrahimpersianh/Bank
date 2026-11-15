import re, subprocess, xml.etree.ElementTree as ET
subprocess.run(["adb", "shell", "uiautomator", "dump", "/sdcard/ui.xml"], capture_output=True)
xml = subprocess.run(["adb", "exec-out", "cat", "/sdcard/ui.xml"], capture_output=True).stdout.decode("utf-8", "replace")
try:
    root = ET.fromstring(xml[xml.index("<"):])
except Exception as e:
    print("dump failed", e); raise SystemExit
for n in root.iter("node"):
    t = (n.get("text") or n.get("content-desc") or "").strip()
    if not t: continue
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", n.get("bounds")))
    print(("*" if n.get("clickable") == "true" else " "), f"({(x1+x2)//2},{(y1+y2)//2})", t[:80].replace("\n", " / "))
