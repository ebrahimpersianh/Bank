#!/usr/bin/env python3
"""صداکردنِ @Composable مستقیم داخلِ `LazyColumn`/`LazyRow` (بیرونِ item/items).

بدنه‌ی `LazyColumn { ... }` یه `LazyListScope`ه نه یه تابعِ @Composable، پس `remember`،
`collectAsState`، `rememberSaveable` و... فقط **داخلِ** `item { }` / `items { }` /
`stickyHeader { }` مجازن. بیرونشون خطای زمانِ کامپایلِ

    @Composable invocations can only happen from the context of a @Composable function

می‌ده - همون چیزی که بیلدِ ۴۹۴ رو شکست (`remember` برای کارتِ «تسویه‌ی زودتر»).

⚠️ هیچ‌کدوم از بررسی‌های ایمپورت‌محور این رو نمی‌گیرن، چون نماد **ایمپورت شده** و درست
نوشته شده - فقط جاش غلطه.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
SRC = [ROOT / "app/src/main/kotlin", ROOT / "data/src/main/kotlin"]

# نمادهایی که حتماً کانتکستِ @Composable می‌خوان و تو بدنه‌ی لیستِ تنبل زیاد وسوسه‌کننده‌ان.
COMPOSABLE_CALLS = re.compile(
    r'\b(remember|rememberSaveable|rememberCoroutineScope|rememberLazyListState|'
    r'rememberScrollState|collectAsState|animateFloatAsState|hiltViewModel)\s*[({]'
)
LAZY_OPEN = re.compile(r'\b(LazyColumn|LazyRow|LazyVerticalGrid|LazyHorizontalGrid)\s*\(')
# سازنده‌های اسکوپ که دوباره وارد کانتکستِ @Composable می‌شن.
ITEM_OPEN = re.compile(r'\b(item|items|itemsIndexed|stickyHeader)\s*[({]')


def scan(path: pathlib.Path) -> list[tuple[int, str]]:
    text = path.read_text(encoding="utf-8")
    lines = text.split("\n")
    problems: list[tuple[int, str]] = []

    depth = 0                 # عمقِ آکولاد از ابتدای فایل
    lazy_stack: list[int] = []   # عمقی که هر LazyColumn توش باز شده
    item_stack: list[int] = []   # عمقی که هر item توش باز شده
    # ⚠️ `LazyColumn(` و آکولادِ لامبداش معمولاً **رو دو خطِ جدا**ن (پارامترهای چندخطی)، پس
    # نمی‌شه همون خط انتظارِ `{` داشت - باید تا رسیدنِ آکولاد منتظر موند.
    pending_lazy = False
    pending_item = False

    for idx, raw in enumerate(lines, 1):
        line = re.sub(r'//.*$', '', raw)
        if not line.strip():
            depth += raw.count("{") - raw.count("}")
            continue

        if LAZY_OPEN.search(line):
            pending_lazy = True
        if ITEM_OPEN.search(line):
            pending_item = True

        # داخلِ یه لیستِ تنبل هستیم ولی داخلِ هیچ item ای نه؟
        inside_lazy = bool(lazy_stack)
        inside_item = bool(item_stack) and bool(lazy_stack) and item_stack[-1] >= lazy_stack[-1]

        if inside_lazy and not inside_item and not pending_lazy and not pending_item:
            m = COMPOSABLE_CALLS.search(line)
            if m:
                problems.append((idx, m.group(1)))

        before = depth
        depth += line.count("{") - line.count("}")
        if depth > before:
            # آکولادی که همین خط باز شد، مالِ کدوم سازنده‌ست؟ item از lazy مهم‌تره چون
            # `items(x) { ... }` هر دو الگو رو هم‌زمان می‌سازه.
            if pending_item:
                item_stack.append(before)
                pending_item = False
                pending_lazy = False
            elif pending_lazy:
                lazy_stack.append(before)
                pending_lazy = False
        while lazy_stack and depth <= lazy_stack[-1]:
            lazy_stack.pop()
        while item_stack and depth <= item_stack[-1]:
            item_stack.pop()

    return problems


def main() -> int:
    targets = [p for root in SRC for p in root.rglob("*.kt")]
    found = []
    for path in targets:
        for line_no, name in scan(path):
            found.append((path.relative_to(ROOT), line_no, name))

    if not found:
        print(f"✅ هیچ @Composableای مستقیم تو بدنه‌ی لیستِ تنبل صدا زده نشده ({len(targets)} فایل)")
        return 0

    print(f"❌ {len(found)} صداکردنِ @Composable بیرونِ item داخلِ لیستِ تنبل:")
    for rel, line_no, name in found:
        print(f"  {rel}:{line_no}\n     {name}(...) → ببرش داخلِ item {{ }}")
    return 1


if __name__ == "__main__":
    sys.exit(main())
