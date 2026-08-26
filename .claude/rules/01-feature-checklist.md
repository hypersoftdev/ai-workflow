---
description: New feature / screen scaffolding checklist — use when adding a feature, hsc-create-mvi, hsc-create-clean-architecture, or verifying a feature is complete
---

## New feature checklist

- [ ] Feature package: `{di,intent,state,effect,viewModel,ui}` (+ adapter/mapper as needed) — **compose:** `:feature-<kebab>` with `*Screen.kt` at feature root instead of `ui/` Fragment
- [ ] Extend `Parent*` / `Base*` UI bases (xml) **or** `*Screen` / `*ScreenContent` (`28-compose-ui`) when `uiFramework` is compose; layouts / composables work in **portrait and landscape**
- [ ] Fragment member order per `19-base-ui` when xml: `onViewCreated` (inline clicks, no `setupClicks`) → helpers → `initObservers` → `renderState` → `handleEffect` → teardown (if any). Compose: `*Screen` collects state/effects → private `*ScreenContent` renders
- [ ] Icon actions use `ButtonStyle.IconButton` (`mb` + `app:icon`) — not clickable `siv`
- [ ] Domain UseCase(s) + repository **interface** in `:domain` (never under `:data`)
- [ ] Data repository impl + DataSource if needed (heavy work / DTO mapping off Main)
- [ ] Mapping: Repo/UseCase for heavy work; `toUi()` in ViewModel if needed — not in UI classes
- [ ] DI: **`lazyModule` / `lazyModules` only** with `//// Section` headers; theme after `startKoin` (never `GlobalContext` probes); UseCases in domain `useCaseModule`
- [ ] Strings in the **single** shared strings file (+ translations)
- [ ] Logs use `Constants.TAG*` format; prefer Repository; ViewModel sparse (`handleError` for failures)
- [ ] Permissions via Intent → Effect → base permission helper when needed
- [ ] Analytics event via shared EventsProvider when screen should be tracked
- [ ] R8 keep rules in `src/main/keepRules/*.keep` cover new `state`/`intent`/`effect`/`model` types
- [ ] Unit tests for UseCase / ViewModel
- [ ] Verify no `:presentation` → `:data` dependency
- [ ] Large-list path: xml `ListAdapter` + DiffUtil / compose `LazyColumn`/`LazyVerticalGrid` + stable keys; `layoutManager` in XML unless dynamic (xml only)
- [ ] Images: xml `ShapeableImageView` + Glide `loadImage`; compose Coil `AsyncImage` / `painterResource`
- [ ] If ads/premium: gate on entitlement + RC; use existing ads managers / ad ViewModels — not raw SDK in Fragments. **Do not** wrap ads in MVI unless the user explicitly asks (`21-ads-billing`)
- [ ] Obey `.claude/project-settings.json` (tests / orientation / theme modes / **`uiFramework`**)
- [ ] Presentation-only scaffold via `hsc-create-mvi` (feature screens only — not ads); new domain/data via `hsc-create-clean-architecture`
