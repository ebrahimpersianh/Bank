#!/usr/bin/env python3
"""Pull blocks out of one giant @Composable into new functions that share the same state.

usage:
  ktextract.py SPEC.json            # apply
  ktextract.py --scan SPEC.json     # only list, per block, which outer locals it captures

SPEC = {
  "source": "path/Screen.kt",          # file holding the giant function
  "function": "Screen",                # (for --scan) name of the giant function
  "target": "path/ScreenParts.kt",     # new file (created or appended)
  "states": {"name": "Type", ...},     # `var name by remember { mutableStateOf(..) }` locals to share
  "blocks": [
    {"fn": "NewName",
     "start": "exact first line of the block (stripped)",
     "occurrence": 1,                  # optional: which match of `start`
     "inner": false,                   # true: move only what is inside the braces, keep the opener
     "receiver": "BoxScope",           # optional receiver (BoxScope / ColumnScope / LazyItemScope ...)
     "composable": true,               # false for LazyListScope DSL builders
     "params": [["loan", "LoanEntity"], ["applyPayment", "(P) -> Unit", "::applyPayment"]],
     "states": ["photoRowM"]}
  ]
}

Why behavior cannot change:
- Each shared `var x by remember(...) { mutableStateOf(...) }` becomes
  `val xState = remember(...) { mutableStateOf(...) }` + `var x by xState` - the same state object.
- The new function receives `xState: MutableState<T>` and opens with `var x by xState`, so the moved
  text is **verbatim**: every read and write still hits that one state object.
- Read-only values and local functions are passed in as parameters with the same names.
"""
import json
import re
import sys

scan = sys.argv[1] == '--scan'
spec = json.load(open(sys.argv[2] if scan else sys.argv[1]))
src = spec['source']
lines = open(src, encoding='utf-8').read().split('\n')


def indent_of(l):
    return len(l) - len(l.lstrip(' '))


def find_start(text, occ=1, line=None):
    if line:  # exact 1-based line number (checked against `text`)
        if lines[line - 1].strip() != text:
            sys.exit(f'line {line} is not {text!r}: {lines[line - 1].strip()!r}')
        return line - 1
    hits = [i for i, l in enumerate(lines) if l.strip() == text]
    if len(hits) < occ:
        sys.exit(f'start not found: {text!r} (hits={len(hits)})')
    return hits[occ - 1]


def find_end(i):
    ind = indent_of(lines[i])
    for j in range(i + 1, len(lines)):
        l = lines[j]
        if not l.strip():
            continue
        st = l.strip()
        # `) {` after a multi-line argument list opens the body - not the end
        if indent_of(l) == ind and st[0] in '})' and not st.endswith('{'):
            return j
        if indent_of(l) < ind and st[0] in '})':
            break
    sys.exit(f'no end for block at line {i + 1}: {lines[i].strip()}')


def scan_block(fn_name, i, j):
    """Outer names a block uses: declarations of the enclosing function above the block."""
    s = next(k for k, l in enumerate(lines) if re.match(r'^(internal |private )?fun ' + fn_name + r'\(', l))
    decl = {}
    k = s
    while True:  # function parameters
        for m in re.finditer(r'(\w+)\s*:', lines[k]):
            decl.setdefault(m.group(1), ('param', k + 1, lines[k].strip()[:100]))
        if lines[k].startswith(') {') or lines[k].startswith(')') or (k > s and lines[k].rstrip().endswith(') {')):
            break
        k += 1
    ind = indent_of(lines[i])
    for k in range(s, i):  # enclosing declarations above the block
        l = lines[k]
        if indent_of(l) > ind:
            continue
        for m in re.finditer(r'\b(?:val|var) (\w+)', l):
            decl[m.group(1)] = ('local', k + 1, l.strip()[:110])
        m = re.match(r'\s*fun (\w+)\(', l)
        if m:
            decl[m.group(1)] = ('fun', k + 1, l.strip()[:110])
        m = re.search(r'\{\s*([\w, ]+)\s*->\s*$', l)
        if m:
            for n in m.group(1).split(','):
                n = n.strip()
                if n and n != '_':
                    decl[n] = ('lambda', k + 1, l.strip()[:110])
    body = '\n'.join(lines[i:j + 1])
    return {n: d for n, d in decl.items() if re.search(r'(?<![\w.])' + re.escape(n) + r'\b', body)}


if scan:
    for b in spec['blocks']:
        i = find_start(b['start'], b.get('occurrence', 1), b.get('line'))
        j = find_end(i)
        print(f"== {b['fn']}  lines {i + 1}-{j + 1}")
        for n, (kind, ln, txt) in sorted(scan_block(spec['function'], i, j).items(), key=lambda x: x[1][1]):
            print(f"   {n:28s} {kind:6s} {ln:5d}  {txt}")
    sys.exit(0)

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
    i = find_start(b['start'], b.get('occurrence', 1), b.get('line'))
    j = find_end(i)
    call_ind = indent_of(lines[i]) + (4 if b.get('inner') else 0)
    if b.get('inner'):
        i, j = i + 1, j - 1
    located.append((i, j, b, call_ind))
located.sort(key=lambda t: t[0], reverse=True)
out_funcs = []
for i, j, b, call_ind in located:
    block = lines[i:j + 1]
    params = b.get('params', [])
    sts = b.get('states', [])
    sig = [f'    {p[0]}: {p[1]},' for p in params] + [f'    {s}State: MutableState<{state_types[s]}>,' for s in sts]
    head = [f'    var {s} by {s}State' for s in sts]
    recv = (b['receiver'] + '.') if b.get('receiver') else ''
    ann = ['@Composable'] if b.get('composable', True) else []
    body = [*ann, f'internal fun {recv}{b["fn"]}(', *sig, ') {', *head, *block, '}']
    out_funcs.append((i, body))
    args = [f'{p[0]} = {p[2] if len(p) > 2 else p[0]}' for p in params] + [f'{s}State = {s}State' for s in sts]
    ind = ' ' * call_ind
    call = [ind + b['fn'] + '('] + [f'{ind}    {a},' for a in args] + [ind + ')']
    lines[i:j + 1] = call
out_funcs.sort(key=lambda t: t[0])

# 3) write target: same package + needed source imports, then the functions
src_text = '\n'.join(lines)
pkg = next(l for l in lines if l.startswith('package '))
imports = [l for l in lines if l.startswith('import ')]
need = ['import androidx.compose.runtime.MutableState', 'import androidx.compose.runtime.getValue',
        'import androidx.compose.runtime.setValue', 'import androidx.compose.runtime.Composable']
for b in spec['blocks']:
    r = b.get('receiver')
    if r in ('BoxScope', 'ColumnScope', 'RowScope'):
        need.append(f'import androidx.compose.foundation.layout.{r}')
    elif r in ('LazyItemScope', 'LazyListScope'):
        need.append(f'import androidx.compose.foundation.lazy.{r}')
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
open(src, 'w', encoding='utf-8').write(src_text)
print(f'{src}: {len(src_text.splitlines())} lines; {target}: {len(out.splitlines())} lines; extracted {len(located)} blocks')
