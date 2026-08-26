---
name: hsc-review-performance
description: Hypersoft Claude Skill: Review Android code for ANR risk, large dataset handling, dispatchers, and RecyclerView performance. Use when optimizing lists, fixing jank, ANR, or slow screens. Prefer hsc-review-complete for a full multi-check pass.
---

# Performance Review

Follow `.claude/rules/06-coroutines-flow.md`, `03-android-architecture.md`, `04-mvi-presentation.md`.

## Assume thousands of items / large files always

### Critical checks
- [ ] No `runBlocking`, sync disk, or network on Main
- [ ] Large `map`/`filter`/`sort` on `Default` or `IO`
- [ ] Repository/UseCase does heavy mapping — not Fragment/Adapter/`*ScreenContent`
- [ ] **xml:** `submitList` only after off-Main list prep
- [ ] **xml:** No `notifyDataSetChanged` on large RecyclerViews

### RecyclerView (xml) / Lazy lists (compose)
- [ ] **xml:** `ListAdapter` + `DiffUtil`
- [ ] **xml:** `app:layoutManager` / `android:orientation` / `app:spanCount` in XML — Kotlin only if dynamic
- [ ] Stable IDs / Compose `key()` when beneficial
- [ ] Avoid nested lists with heavy child rebind
- [ ] **xml:** Images via `loadImage` (Glide); cancelled/cleared on rebind — no `setImageBitmap` for remote/list art
- [ ] **compose:** Coil `AsyncImage`; no Glide in feature modules

### State / memory
- [ ] State does not hold full raw megabyte datasets unnecessarily
- [ ] Pagination / windowed load for open-ended data
- [ ] Prefer `Flow` streaming over load-all-in-memory

### Layout
- [ ] Shallow hierarchy (`09-resources-xml`) — overdraw / measure cost
- [ ] No unnecessary `layout-land` duplication if one responsive layout suffices

### Coroutines
- [ ] `flowOn` before collection for upstream IO
- [ ] No `GlobalScope`

## Report format

Number every actionable finding. Follow [fix-selection.md](../fix-selection.md) after the report.

```markdown
## Fix list
| # | Issue | Location | Severity | Fix |
|---|-------|----------|----------|-----|
| 1 | Main-thread map of 5000 items | `XFragment` | Critical | Move to ViewModel + Default |
| 2 | … | … | Warning | … |
```

Prioritize **Critical** = ANR or OOM risk on realistic data sizes.

After the report: **do not fix yet** — ask which numbers to fix per `fix-selection.md` (e.g. user replies `fix 1, 2, 4, 7`).
