# tap.py "text" [nth]
import re, subprocess, sys, xml.etree.ElementTree as ET
want = sys.argv[1]; nth = int(sys.argv[2]) if len(sys.argv) > 2 else 0
subprocess.run(["adb", "shell", "uiautomator", "dump", "/sdcard/ui.xml"], capture_output=True)
xml = subprocess.run(["adb", "exec-out", "cat", "/sdcard/ui.xml"], capture_output=True).stdout.decode("utf-8", "replace")
root = ET.fromstring(xml[xml.index("<"):])
t = lambda n: (n.get("text") or n.get("content-desc") or "").strip()
nodes = [n for n in root.iter("node") if t(n) == want] or [n for n in root.iter("node") if want in t(n)]
if len(nodes) <= nth:
    print("NOT FOUND:", want); sys.exit(1)
x1, y1, x2, y2 = map(int, re.findall(r"\d+", nodes[nth].get("bounds")))
cx, cy = str((x1+x2)//2), str((y1+y2)//2)
subprocess.run(["adb", "shell", "input", "tap", cx, cy])
print("tapped", want, cx, cy)
