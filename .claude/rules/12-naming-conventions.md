---
description: Cross-cutting naming conventions reference — Kotlin, Gradle, packages, resources, logging, analytics, domain
paths:
  - "**/domain/**/*.kt"
  - "**/data/**/*.kt"
  - "**/presentation/**/*.kt"
  - "**/feature*/**/*.kt"
  - "**/core*/**/*.kt"
  - "**/res/**/*.xml"
  - "**/*.gradle.kts"
  - "**/libs.versions.toml"
---

## Kotlin

| Kind | Rule | Example |
|------|------|---------|
| Classes | PascalCase + role suffix | `HomeFragment` (xml), `HomeScreen` (compose), `UserRepositoryImpl` |
| Interfaces | No `I` prefix | `UserRepository` |
| Implementations | `Impl` suffix | `UserRepositoryImpl` |
| ViewModels | `*ViewModel` | `HomeViewModel` |
| UseCases | `*UseCase` | `GetUserUseCase` |
| MVI types | `*Intent` / `*State` / `*Effect` for **feature screens** only — not ads | `HomeIntent` |
| DI modules | `*Module` val | `homePresentationModule` |
| Functions | camelCase, verb-first | `handleIntent`, `savePreferences` |
| Backing flows | `_state`, `_effect` (private) | `_state.update { }` |
| Safe Args | Property always `navArgs` | `private val navArgs: XFragmentArgs by navArgs()` |

### Kotlin formatting (keep short expressions on one line)

- Simple single-expression overrides (`DiffUtil.ItemCallback`, trivial getters) → **one line**: `…): Boolean = oldItem.id == newItem.id`
- Do **not** break after `=` for short bodies
- Prefer wrapping only when a line is truly long; ~**140** columns is fine for DiffUtil / similar signatures

## Gradle modules

- kebab-case: `:core-ui`, `:feature-auth`, `:data`

## Packages

```
<applicationId>.<layer>.<feature>.<subpackage>
```

Feature folders: camelCase (`userProfile`, `onBoarding`). Compose Gradle modules: kebab-case `:feature-user-profile`.

## Resources

- Layouts/drawables/strings: snake_case with type prefix
- Screen layouts in `:presentation`; shared drawables/themes in `:core-ui`
- **All** strings in one `:core-ui` `strings.xml` (plus locale folders) — not per-feature string files
- Resource file section order: **app → general/common → content descriptions → screen-wise** (see `09-resources-xml`)
- Images (display only): `ShapeableImageView` (`siv`); contentDescription via `cd_*` strings
- Programmatic image loads: `siv.loadImage(...)` via Glide (`ImageViewExtensions`) — not `setImageResource` in adapters
- Clickable icons: `MaterialButton` + `ButtonStyle.IconButton` (`mb`); `app:icon` — not clickable `siv`
- Clickable language/chip selectors: `mb…` `MaterialButton` + `app:icon` end — not `mtv` + `bg_shape_*` / `drawableEnd`, not `ll` + `mtv` + `siv`
- **No `dimens.xml`** — inline sizes as multiples of 4 (`8dp`, `16dp`, `16sp`, …)
- Backgrounds: `bg_shape_*` (shapes for **non-button** surfaces / gradients / selectors), `bg_svg_*` (svg backgrounds), `bg_*` otherwise — **not** for `MaterialButton` solid+stroke (use tint/stroke/cornerRadius)

## Logging tags

- `Constants.TAG`, `Constants.TAG_ADS`, `Constants.TAG_FIREBASE`, `Constants.TAG_REMOTE_CONFIG`
- Message: `ClassName: functionName: State: details`

## Analytics events

- Screen/event keys in shared provider (`EventsProvider` or equivalent) in `:core-common`
- Prefer `EventsProvider.HOME_SCREEN.postFirebaseEvent()` over raw string literals

## Data / domain types

| Layer | Naming |
|-------|--------|
| Repository interface | `UserRepository` — **`:domain` only** |
| Repository impl | `UserRepositoryImpl` — **`:data` only** |
| UseCase | `GetUserUseCase` — **`:domain` only** (never `:data`) |
| DataSource | `SharedPrefManager` (sync, Context only), `UserRemoteDataSource` — **`:data`** |
| Entity (domain) | `User`, `GeoLocation` |
| UI model | `UserUiItem` |
| Mapper | `UserUiMapper.toUi(...)` — call from ViewModel; heavy data mapping in Repo/UseCase |
| Base UI | xml: `ParentFragment`, `BaseFragment`, `ParentActivity`, `ParentDialog`, `ParentSheet`. compose: `*Screen` / `*ScreenContent` / `AppTheme` |
| DI modules | `dataModule` / `useCaseModule` / `*PresentationModule` (xml) / `*FeatureModule` (compose) — always `lazyModule { }` |