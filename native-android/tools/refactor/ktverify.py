#!/usr/bin/env python3
"""ktverify.py ORIG_PATH_IN_HEAD NEWFILE... : body lines (minus imports/package) identical modulo private->internal."""
import subprocess, sys, re
orig = subprocess.run(['git', 'show', 'HEAD:' + sys.argv[1]], capture_output=True, text=True, cwd='/home/user/Bank').stdout
def body(t): return [l for l in t.split('\n') if not l.startswith('import ') and not l.startswith('package ') and l.strip()]
new = []
for f in sys.argv[2:]:
    new += body(open('/home/user/Bank/' + f).read())
norm = lambda ls: sorted(re.sub(r'^internal ', 'private ', l) for l in ls)
ok = norm(body(orig)) == norm(new)
print(('OK ' if ok else 'MISMATCH ') + sys.argv[1].split('/')[-1], len(body(orig)), len(new))
sys.exit(0 if ok else 1)
