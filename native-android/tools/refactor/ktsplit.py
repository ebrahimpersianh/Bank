#!/usr/bin/env python3
"""Move top-level Kotlin declarations from one file into new sibling files.

usage: ktsplit.py SOURCE.kt NEWFILE.kt:Name1,Name2 [NEWFILE2.kt:...] [--dry]

- A declaration = a top-level item starting at column 0 (fun/val/class/...), plus the
  annotations and comment block directly above it, up to the line before the next one.
- New files get the same package line and the full import block of the source.
- Every top-level `private` declaration (in the source or the new files) that is
  referenced from a *different* file of the split becomes `internal`.
- Prints conflicts: names made internal that are also declared top-level elsewhere in
  the same package directory.
"""
import os
import re
import sys

DECL = re.compile(r'^(?:(?:private|internal|public)\s+)?(?:(?:inline|suspend|data|enum|sealed|abstract|open|const|lateinit|tailrec|operator|infix)\s+)*'
                  r'(fun|val|var|class|object|interface|typealias)\b\s*(?:<[^>]*>\s*)?(?:[A-Za-z_][\w.]*\.)?([A-Za-z_]\w*)')


def parse(path):
    lines = open(path, encoding='utf-8').read().split('\n')
    # header = package + imports (+ blank lines) up to first non-import code/comment
    i = 0
    while i < len(lines) and (lines[i].startswith('package ') or lines[i].startswith('import ') or lines[i].strip() == ''):
        i += 1
    header_end = i
    # find declaration starts
    starts = []
    for n in range(header_end, len(lines)):
        m = DECL.match(lines[n])
        if m:
            starts.append((n, m.group(2)))
    # extend each start upward over annotations / comments / blank-free doc blocks
    blocks = []
    for idx, (n, name) in enumerate(starts):
        s = n
        while s - 1 >= header_end:
            prev = lines[s - 1]
            if prev.startswith('@') or prev.startswith('/**') or prev.startswith(' *') or prev.startswith('*/') \
                    or prev.startswith('//') or prev.startswith('/*'):
                s -= 1
            else:
                break
        blocks.append([s, n, name])
    for idx in range(len(blocks)):
        end = blocks[idx + 1][0] if idx + 1 < len(blocks) else len(lines)
        blocks[idx].append(end)
    return lines, header_end, blocks


def strip_comments(text):
    text = re.sub(r'/\*.*?\*/', '', text, flags=re.S)
    text = re.sub(r'//[^\n]*', '', text)
    text = re.sub(r'"(?:\\.|[^"\\])*"', '""', text)
    return text


def main():
    args = [a for a in sys.argv[1:] if a != '--dry']
    dry = '--dry' in sys.argv
    src = args[0]
    plan = []
    for spec in args[1:]:
        fname, names = spec.split(':', 1)
        plan.append((fname, [x for x in names.split(',') if x]))
    lines, header_end, blocks = parse(src)
    header = '\n'.join(lines[:header_end]).rstrip('\n') + '\n'
    by_name = {}
    for b in blocks:
        by_name.setdefault(b[2], []).append(b)
    moved = {}
    for fname, names in plan:
        for nm in names:
            if nm not in by_name:
                sys.exit(f'not found: {nm}')
            for b in by_name[nm]:
                moved[b[0]] = fname
    files = {src: []}
    # file-level comment between the imports and the first declaration stays on top of the source
    preamble = lines[header_end:blocks[0][0]] if blocks else []
    if any(l.strip() for l in preamble):
        files[src].append(('<preamble>', preamble))
    for fname, _ in plan:
        files[os.path.join(os.path.dirname(src), fname)] = []
    for b in blocks:
        s, n, name, e = b
        target = moved.get(s)
        key = os.path.join(os.path.dirname(src), target) if target else src
        files[key].append((name, lines[s:e]))
    # visibility fix
    texts = {}
    for f, items in files.items():
        # raw text on purpose: string templates ("${fn()}") are real code
        texts[f] = '\n'.join(l for _, blk in items for l in blk)
    made_internal = set()
    for f, items in files.items():
        for k, (name, blk) in enumerate(items):
            # first declaration line in block
            for j, line in enumerate(blk):
                if DECL.match(line):
                    break
            if not blk[j].startswith('private '):
                continue
            used_elsewhere = any(re.search(r'(?<![\w])' + re.escape(name) + r'\b', t)
                                 for g, t in texts.items() if g != f)
            if used_elsewhere:
                blk[j] = 'internal ' + blk[j][len('private '):]
                made_internal.add(name)
    # write (imports pruned per file; operator/delegate imports and wildcards always kept)
    keep_always = {'getValue', 'setValue', 'provideDelegate', 'plus', 'minus', 'times', 'div', 'rem',
                   'unaryMinus', 'unaryPlus', 'not', 'contains', 'get', 'set', 'invoke', 'compareTo',
                   'rangeTo', 'rangeUntil', 'iterator', 'next', 'hasNext', 'plusAssign', 'minusAssign',
                   'component1', 'component2', 'component3', 'component4', 'component5'}
    header_lines = lines[:header_end]
    for f, items in files.items():
        body = '\n'.join('\n'.join(blk).rstrip('\n') for _, blk in items)
        code = body  # raw: string templates use imports too; extra imports are harmless
        kept = []
        for hl in header_lines:
            if hl.startswith('import '):
                spec = hl[len('import '):].strip()
                alias = spec.split(' as ')[1].strip() if ' as ' in spec else None
                simple = alias or spec.split('.')[-1]
                if simple == '*' or simple in keep_always or re.search(r'(?<![\w])' + re.escape(simple) + r'\b', code):
                    kept.append(hl)
            elif hl.strip():
                kept.append(hl)
        pkg = [h for h in kept if h.startswith('package ')]
        imps = [h for h in kept if h.startswith('import ')]
        out = '\n'.join(pkg) + '\n\n' + '\n'.join(imps) + ('\n\n' if imps else '\n') + body.strip('\n') + '\n'
        if dry:
            print(f'{f}: {out.count(chr(10))} lines, decls={[n for n, _ in items]}')
        else:
            open(f, 'w', encoding='utf-8').write(out)
            print(f'{f}: {out.count(chr(10))} lines')
    print('made internal:', sorted(made_internal))
    # conflicts with other files of the package
    d = os.path.dirname(src)
    ours = set(files)
    for other in sorted(os.listdir(d)):
        p = os.path.join(d, other)
        if not other.endswith('.kt') or p in ours:
            continue
        for line in open(p, encoding='utf-8'):
            m = DECL.match(line)
            if m and m.group(2) in made_internal:
                print(f'CONFLICT: {m.group(2)} also top-level in {other}')


if __name__ == '__main__':
    main()
