#!/usr/bin/env python3
"""Check a ktextract.py run: HEAD version of SOURCE == current SOURCE (calls removed, state
declarations folded back) + the bodies of the new functions in TARGET (wrappers removed).

usage: ktxverify.py SOURCE TARGET [FN ...]   # FN = only these functions of TARGET (default: all)
Comparison is a multiset of non-blank lines, after `private`/`internal` normalisation.
"""
import collections
import re
import subprocess
import sys

src_path, tgt_path = sys.argv[1], sys.argv[2]
only = set(sys.argv[3:])
root = subprocess.run(['git', 'rev-parse', '--show-toplevel'], capture_output=True, text=True).stdout.strip()
rel = subprocess.run(['git', 'ls-files', '--full-name', src_path], capture_output=True, text=True).stdout.strip()
orig = subprocess.run(['git', 'show', 'HEAD:' + rel], capture_output=True, text=True, cwd=root).stdout.split('\n')
try:
    tgt_head_rel = subprocess.run(['git', 'ls-files', '--full-name', tgt_path], capture_output=True, text=True).stdout.strip()
    tgt_head = subprocess.run(['git', 'show', 'HEAD:' + tgt_head_rel], capture_output=True, text=True, cwd=root).stdout.split('\n') if tgt_head_rel else []
except Exception:
    tgt_head = []
src = open(src_path, encoding='utf-8').read().split('\n')
tgt = open(tgt_path, encoding='utf-8').read().split('\n')

# functions in target that are new (not in HEAD target)
head_fns = set(re.findall(r'^internal fun (?:\w+\.)?(\w+)\(', '\n'.join(tgt_head), re.M))
fns = [m for m in re.findall(r'^internal fun (?:\w+\.)?(\w+)\(', '\n'.join(tgt), re.M) if m not in head_fns]
if only:
    fns = [f for f in fns if f in only]
fnset = set(fns)

out = []
i = 0
while i < len(src):
    l = src[i]
    m = re.match(r'^(\s*)val (\w+)State = (remember.*)$', l)
    if m and i + 1 < len(src) and src[i + 1].strip() == f'var {m.group(2)} by {m.group(2)}State':
        out.append(f'{m.group(1)}var {m.group(2)} by {m.group(3)}')
        i += 2
        continue
    m = re.match(r'^(\s*)(\w+)\($', l)
    if m and m.group(2) in fnset:
        j = i + 1
        while src[j] != m.group(1) + ')':
            j += 1
        i = j + 1
        continue
    out.append(l)
    i += 1

body = []
k = 0
while k < len(tgt):
    l = tgt[k]
    m = re.match(r'^internal fun (?:\w+\.)?(\w+)\($', l)
    if m and m.group(1) in fnset:
        if body and body[-1] == '@Composable':
            body.pop()
        k += 1
        while tgt[k] != ') {':
            k += 1
        k += 1
        while re.match(r'^    var \w+ by \w+State$', tgt[k]):
            k += 1
        while tgt[k] != '}':
            body.append(tgt[k])
            k += 1
        k += 1
        continue
    k += 1

norm = lambda ls: collections.Counter(
    re.sub(r'^internal ', 'private ', x) for x in ls
    if x.strip() and not x.startswith(('import ', 'package ')) and x != '@Composable')
a, b = norm(orig), norm(out + body)
missing, extra = a - b, b - a
print('functions:', ', '.join(fns))
print('OK' if not missing and not extra else 'MISMATCH')
for x, n in list(missing.items())[:10]:
    print('  missing', n, repr(x[:100]))
for x, n in list(extra.items())[:10]:
    print('  extra  ', n, repr(x[:100]))
sys.exit(0 if not missing and not extra else 1)
