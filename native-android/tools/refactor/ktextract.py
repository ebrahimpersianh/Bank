#!/usr/bin/env python3
"""Pull top-level blocks out of one giant @Composable into new composables, sharing state.

usage: ktextract.py SPEC.json

SPEC = {
  "source": "path/Screen.kt",          # file holding the giant function
  "target": "path/ScreenParts.kt",     # new file (created or appended)
  "states": {"name": "Type", ...},     # `var name by remember { mutableStateOf(..) }` locals to share
  "blocks": [
    {"fn": "NewComposableName",
     "start": "exact first line of the block (stripped)",
     "occurrence": 1,                  # optional: which match of `start`
     "params": [["loan", "LoanEntity"], ...],          # read-only values, passed as-is
     "states": ["photoRowM", ...]}                       # shared MutableState locals used inside
  ]
}

How it stays behavior-identical:
- Each shared `var x by remember(...) { mutableStateOf(...) }` becomes
  `val xState = remember(...) { mutableStateOf(...) }` + `var x by xState`: same state object.
- The extracted composable receives `xState: MutableState<T>` and starts with `var x by xState`,
  so the moved block text is **verbatim** - every read and write hits the same state.
- A block is the line matching `start` (at 4-space indent) through its matching `    }` line.
"""
import json
import re
import sys

spec = json.load(open(sys.argv[1]))
src = spec['source']
lines = open(src, encoding='utf-8').read().split('\n')


def find_start(text, occ=1):
    hits = [i for i, l in enumerate(lines) if l.strip() == text and l.startswith('    ') and not l.startswith('     ')]
    if len(hits) < occ:
        sys.exit(f'start not found: {text!r} (hits={len(hits)})')
    return hits[occ - 1]


def find_end(i):
    for j in range(i + 1, len(lines)):
        if lines[j] == '    }':
            return j
        if lines[j] and lines[j][0] not in ' \t':
            break
    sys.exit(f'no end for block at line {i + 1}')


# 1) state declarations -> shared MutableState
decl_re = re.compile(r'^(\s*)var (\w+) by (remember(?:\([^)]*\))? \{ mutableStateOf.*\})\s*$')
state_types = spec['states']
seen = set()
for k, l in enumerate(lines):
    m = decl_re.match(l)
    if m and m.group(2) in state_types:
        ind, name, rhs = m.groups()
        lines[k] = f'{ind}val {name}State = {rhs}\n{ind}var {name} by {name}State'
        seen.add(name)
missing = set(state_types) - seen
if missing:
    sys.exit(f'state declarations not found: {sorted(missing)}')

# 2) cut blocks (locate all first, then cut bottom-up)
located = []
for b in spec['blocks']:
    i = find_start(b['start'], b.get('occurrence', 1))
    j = find_end(i)
    located.append((i, j, b))
located.sort(key=lambda t: t[0], reverse=True)
out_funcs = []
for i, j, b in located:
    block = lines[i:j + 1]
    params = b.get('params', [])
    sts = b.get('states', [])
    sig = [f'    {p[0]}: {p[1]},' for p in params] + [f'    {s}State: MutableState<{state_types[s]}>,' for s in sts]
    head = [f'    var {s} by {s}State' for s in sts]
    body = ['@Composable', f'internal fun {b["fn"]}(', *sig, ') {', *head, *block, '}']
    out_funcs.append((i, body))
    # third element = expression at the call site (e.g. `::applyPayment` for a local function)
    args = [f'{p[0]} = {p[2] if len(p) > 2 else p[0]}' for p in params] + [f'{s}State = {s}State' for s in sts]
    call = ['    ' + b['fn'] + '('] + [f'        {a},' for a in args] + ['    )']
    lines[i:j + 1] = call
out_funcs.sort(key=lambda t: t[0])

# 3) write target: same package + all source imports (+ MutableState), then the functions
src_text = '\n'.join(lines)
pkg = next(l for l in lines if l.startswith('package '))
imports = [l for l in lines if l.startswith('import ')]
need = ['import androidx.compose.runtime.MutableState', 'import androidx.compose.runtime.getValue',
        'import androidx.compose.runtime.setValue', 'import androidx.compose.runtime.Composable']
for n in need:
    if n not in imports:
        imports.append(n)
funcs_text = '\n\n'.join('\n'.join(f) for _, f in out_funcs)
kept = []
for imp in imports:
    spec_ = imp[len('import '):].strip()
    simple = spec_.split(' as ')[1].strip() if ' as ' in spec_ else spec_.split('.')[-1]
    if simple in ('*', 'getValue', 'setValue') or re.search(r'(?<![\w])' + re.escape(simple) + r'\b', funcs_text):
        kept.append(imp)
target = spec['target']
try:
    existing = open(target, encoding='utf-8').read()
except FileNotFoundError:
    existing = ''
if existing:
    have = set(l for l in existing.split('\n') if l.startswith('import '))
    add = [k for k in kept if k not in have]
    tl = existing.rstrip('\n').split('\n')
    last = max(n for n, l in enumerate(tl) if l.startswith('import '))
    tl[last + 1:last + 1] = add
    out = '\n'.join(tl) + '\n\n' + funcs_text + '\n'
else:
    out = pkg + '\n\n' + '\n'.join(kept) + '\n\n' + funcs_text + '\n'
open(target, 'w', encoding='utf-8').write(out)
if 'import androidx.compose.runtime.MutableState' not in src_text:
    pass  # source only uses `val xState = remember{...}`, no explicit type needed
open(src, 'w', encoding='utf-8').write(src_text)
print(f'{src}: {len(src_text.splitlines())} lines; {target}: {len(out.splitlines())} lines; extracted {len(located)} blocks')
