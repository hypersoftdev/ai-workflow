---
description: Dependency injection conventions
paths:
  - "**/di/**/*.kt"
  - "**/*Module*.kt"
  - "**/App.kt"
---

# Dependency injection (invariants)

**Full detail (bootstrap, sections, examples):** [reference/dependency-injection.md](reference/dependency-injection.md)

## Must follow

- Default framework: **Koin** — constructor inject / `by inject()` / `by viewModel()`
- **Every** module: `lazyModule { }` — never `module { }`; load via `lazyModules(...)` only
- `startKoin` **only** in Application; theme / Dynamic Colors **after** `startKoin` — never `GlobalContext` probes; never start Koin in Activity
- Every `lazyModule` uses `//// Section` headers (DataSources → Repositories; area UseCases; ViewModels; composition root Core → Data → Domain → Presentation or Feature → Ads)
- Domain owns UseCases + repository **interfaces** + `useCaseModule`; data owns impls + DataSources + `dataModule` — never UseCases or repo interfaces in `:data`
- Bindings: `single` (repos/sources/managers/dispatchers), `factory` (UseCases), `viewModel` (VMs)
- Ads DI keeps existing managers / ad ViewModels (not MVI Intent/State/Effect) unless the user explicitly asks (`21-ads-billing`)
- Register every new module in the composition root; bind `single<XRepository> { XRepositoryImpl(get()) }`
- Dispatchers: `single { Dispatchers.IO }` / `Dispatchers.Default` — no `named("io")` unless true parallel instances (e.g. ad placements)

Read [reference/dependency-injection.md](reference/dependency-injection.md) when adding or changing DI.
