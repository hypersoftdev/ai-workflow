---
name: hs-setup-old-project
description: Hypersoft Cursor Skill: Migrates an existing production Android app to the hs-setup-new-project architecture (multi-module Clean Architecture, MVI, Koin lazyModule, XML View Binding or Compose feature modules per uiFramework) while preserving product behavior. Detects MVC, MVVM, Activity-based, single-module, Gradle Groovy, Compose, and older SDK setups, then extracts existing code into the target layers. Use when the user says hs-setup-old-project, migrate an old/live app, or convert a shipping project to the template. Do not use for greenfield apps — use hs-setup-new-project.
---

# Setup Old Project

Follow `.cursor/rules/` — especially `00-global`, `02-project-structure`, `07-dependency-injection`, `08-gradle`, `09-resources-xml`, `17-navigation`, `19-base-ui`, `21-ads-billing`, `22-platform-firebase`, `23-app-startup`, `26-data-persistence`, `28-compose-ui` (when compose).

**Target architecture:** [hs-setup-new-project](../hs-setup-new-project/SKILL.md). Read that skill and treat it as the destination. Reuse its templates (`../hs-setup-new-project/templates/`). Do not invent a parallel stack.

Detection and extract patterns: [migration.md](migration.md).

## Purpose

**Preserve the product, migrate the architecture.**

When applied to an existing production Android project, migrate its architecture toward the same structure, patterns, modules, and conventions used by `hs-setup-new-project`.

The project may currently use MVC, MVVM, Activity-based architecture, single-module structure, Gradle Groovy, older SDK/configuration, etc. Detect the current setup and migrate it appropriately.

## Goal

`hs-setup-new-project` → Build the architecture from scratch.

`hs-setup-old-project` → **Preserve the existing product and migrate it to that architecture.**

## Critical rule

Do not rewrite or change stable product behavior unnecessarily.

Preserve existing:

* Business logic
* API behavior and data handling
* UI behavior
* Translations/localization
* Ads
* Analytics
* Database/persistence
* Third-party integrations
* Navigation and existing functionality

Prefer **moving/extracting existing code into the new architecture** rather than rewriting it.

For example, if API calling or response extraction currently exists inside an Activity, move it into the appropriate DataSource/Repository layer without changing its actual behavior.

Make the minimum necessary changes to achieve the architecture defined by `hs-setup-new-project`.

## How this skill differs from `hs-setup-new-project`

|                | `hs-setup-new-project`               | `hs-setup-old-project`                                                                                                     |
|----------------|-----------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| Starting point | Empty / greenfield                | Shipping production app                                                                                                 |
| Code           | Scaffold new files from templates | Move/extract existing code into the target slots                                                                        |
| Product        | Create defaults                   | Keep current behavior, strings, APIs, ads, analytics, DB, nav                                                           |
| Versions       | Latest stable catalog             | Keep existing library versions unless a bump is required for the architecture                                           |
| Firebase / FCM | Add deps; no MessagingService     | Relocate existing Firebase; **keep** an existing `FirebaseMessagingService` / push UI if present                        |
| Ads            | Always place `:gmaAds` from GitHub; ask implement yes/no | Keep existing ads if present (no blind overwrite); if none, place `:gmaAds` like new; **not** MVI unless user asks |
| Entrance       | New start destination             | Insert `EntranceFragment` as start, then route to the **previous** start screen so the user-visible flow stays the same |

Execute every `hs-setup-new-project` step as **migration**: if the slot already exists (prefs helper, API client, Room DB, analytics wrapper), move it into the target module/layer. Create a template file only when that piece is missing.

## Step 0 — Confirm `project-settings.json` (mandatory — stop)

**Do not** start Gradle conversion, module splits, or code moves until the user confirms settings.

Detect current values from Gradle / manifest / `strings.xml` / resource folders, **propose them**, and ask. Ask even if `.cursor/project-settings.json` already exists (confirm or change).

Write answers to **`.cursor/project-settings.json`**:

```json
{
  "writeTestsWithFeatures": true,
  "orientation": "both",
  "themeModes": "both",
  "applicationId": "com.company.app",
  "uiFramework": "xml",
  "appName": "App Display Name"
}
```

Ask, in this order (use AskQuestion when available):

1. **`applicationId` / root package** — propose the current `applicationId`. Confirm before changing.
2. **`appName`** — propose the current launcher / display name.
3. **`orientation`** — `portrait` / `landscape` / `both`.
    - Tell the user what the app does today (e.g. portrait-locked, both, landscape layouts missing).
    - Ask explicitly whether they want **landscape** support, **portrait only**, or **both**.
    - Default suggestion: `both` unless the product is already locked and they want to keep it.
4. **`themeModes`** — `day` / `night` / `both`.
    - Tell the user whether `values-night` (or equivalent) exists today.
    - Ask explicitly whether they want **night mode**, **day only**, or **both**.
5. **`writeTestsWithFeatures`** — `true` / `false`. Propose `true`.
6. **`uiFramework`** — `xml` / `compose`. Detect from the inventory (Compose screens vs XML Fragments). Propose the current stack. **Do not** convert XML ↔ Compose unless the user explicitly asks.
7. **Ads** — if a working ads stack already exists → **keep it** (do not convert to MVI; do **not** overwrite with a blind `:gmaAds` copy unless the user asks to migrate). If no ads module → **always place** `:gmaAds` from GitHub (package `{applicationId}.gmaAds` + host remaps only; see `hs-setup-new-project` Step — Place `:gmaAds` / [ads-gma.md](../../../rules/reference/ads-gma.md)). Ask: **Implement ads on screens now?** **yes** → `hs-implement-admob-ads` (existing screens only); **no** → module only.

All later skills **must read** `.cursor/project-settings.json` and obey it. Orientation / night resources are added or skipped **only** from this confirmation — do not add landscape or `values-night` unless the user chose `both` (or that single mode).

## Step 1 — Inventory

Scan the repo and record (do not change files yet):

- Gradle: Groovy vs Kotlin DSL; catalog present or not; AGP / SDK / `applicationId` / `versionCode`
- Modules: single-module vs already multi-module
- UI: Activities vs Fragments; MVC / MVVM / MVI; View Binding / Data Binding / `findViewById` / Compose
- DI: none / Hilt / Dagger / Koin (`module` vs `lazyModule`)
- Navigation: Activity-per-screen vs Navigation Component; current start destination / launcher
- Data: API calls in UI, Room, prefs, files
- Firebase: Analytics, Crashlytics, RC, Messaging (incl. existing service)
- Ads / billing / third-party SDKs
- Locales: all `values-*` string folders
- Signing: existing `.jks` / `signingConfigs` / `keystore.properties`

If the UI is **Jetpack Compose**, set `uiFramework` to `compose` and migrate toward AnimeHub **feature modules** (`28-compose-ui`) — do **not** convert Compose → XML unless the user explicitly asks. If the UI is XML, set `xml` and keep View Binding — do **not** convert XML → Compose unless asked.

Then migrate using [migration.md](migration.md). Prefer the detect → action table there.

## Step 2 — Gradle (minimum change)

Follow `08-gradle.mdc` + [reference/gradle.md](../../../rules/reference/gradle.md) and **`hs-gradle-organize`**.

1. Groovy → Kotlin DSL if needed (`settings.gradle.kts`, module scripts, catalog). **Same conversion contract as `hs-gradle-update` Step 0** — keep both docs aligned when this changes (`MUST_READ_BEFORE_SKILL_CHANGES.md`).
2. If AGP is below **9.3** or `kotlin-android` is applied → bump to **AGP 9.3+** and remove `org.jetbrains.kotlin.android` / `kotlin-kapt` / `android.kotlinOptions` (`hs-gradle-update` Step 0.5). Built-in Kotlin. Do not set `android.builtInKotlin=false`. Set `compileOptions` to `VERSION_21` and `compileSdk` 37.1. Then migrate R8 (`hs-gradle-update` Step 0.6).
3. `include` the mandatory module set from `hs-setup-new-project` (`:app`, `:domain`, `:data`, `:core-common`, `:core-ui`, `:core-platform`; **xml:** also `:presentation`; **compose:** also `:core-design` + `:feature-*` — keep `:gmaAds` / extra modules if they already exist).
4. Move existing dependencies into `libs.versions.toml` **at the same versions**. Do not run a full `hs-gradle-update` bump. Bump a library only if the new architecture cannot compile without it; tell the user what changed.
5. **xml:** View Binding on UI modules; Safe Args on `:presentation`. Remove Data Binding when replacing it with View Binding (keep layouts working). **compose:** Compose Compiler plugin (`kotlin-compose`) + `compose = true` on `:app` / `:core-design` / `:feature-*` — **not** `kotlin-android`; Coil 3; no View Binding on feature modules.
6. Preserve `signingConfigs`, `versionCode` / `versionName`, `applicationId`, existing `.jks` paths, `google-services.json`. Move ProGuard `.pro` bodies into `src/main/keepRules/rules.keep` (do not drop rules). Do not invent passwords.
7. `:app` script shape from `hs-setup-new-project` Step 1 (`android` section order, `bundle.language.enableSplit = false`, `base.archivesName`).
8. Every module `.gitignore` (`/build`; `:app` also `/release`).
9. Organize sections with **`hs-gradle-organize`**.

## Step 3 — Modules + composition root

Create missing modules. **Move** existing code into them — do not leave duplicates in `:app`.

```
app (Composition Root) — no values resources
 |
 ↓
xml: presentation → domain ← data
compose: feature-* → domain ← data   (+ :core-design)
 |
 ↓
core-common / core-ui / core-platform
```

- `:app` — `App`, manifest, `KoinModules` only; **no** `res/values/` (move themes/strings/colors to `:core-ui`; keep `mipmap` / backup `xml` / `google-services.json` as needed)
- Convert Hilt/Dagger/`module { }` → **`lazyModule` / `lazyModules` only** (same graph, new container)
- Theme after `startKoin` (`23-app-startup`); no `GlobalContext.getOrNull()` gates
- UseCases + repo **interfaces** → `:domain`; DataSources + repo **impls** → `:data`
- UI modules (`:presentation` or `:feature-*`) **never** depend on `:data`

Copy Parent*/anim/extensions/Glide/`PlatformFirebase` from [hs-setup-new-project templates](../hs-setup-new-project/templates/) **only when missing**. See `hs-setup-new-project` Steps 2, 6, 7.

## Step 4 — Extract data out of UI

Do not rewrite parsers, endpoints, headers, or DB queries.

1. API / JSON parsing in Activity/Fragment/Presenter → `:data` DataSource + Repository impl; domain interface + UseCase.
2. Room / SQLite / files / prefs → `:data`; keep schema, keys, file names, prefs name.
3. SharedPreferences: wrap the **existing** prefs file/name in `SharedPrefManager` (sync) + `SharedPrefRepository` (`26-data-persistence` + `reference/shared-preferences.md`). Do not change keys or defaults.
4. Remote Config: if present, keep **existing keys**; wrap with DataSource + cache-to-prefs (`hs-setup-new-project` Step 8 / `hs-implement-firebase-remote-config`). If missing, add the template RC stack.
5. Analytics: keep **event names and params**; move posting into `PlatformFirebase` + `EventsProvider`. Do not rename events.
6. Ads: if existing ads → keep managers / mediation; relocate into `:gmaAds` only if still inside `:app` and structure matches. If no ads → place GitHub `:gmaAds` (package + host remaps). Wire screens only if user said **yes** (`hs-implement-admob-ads`). **Not** MVI unless the user explicitly asks (`21-ads-billing`).
7. Billing, maps, login SDKs, etc.: keep behavior; place per `02-project-structure`.

Heavy mapping stays in Repository / UseCase — not in Fragments.

## Step 5 — Presentation + navigation

Preserve layouts / composables, click behavior, copy, and back-stack meaning.

**xml:**

1. `MainActivity` in `:presentation` extends `ParentActivity`; host `fcvContainerMain` + `NavHostFragment` + `@navigation/nav_graph`.
2. Map each feature Activity (or MVC screen) to a Fragment + MVI via **`hs-create-mvi`** patterns: move existing UI logic into Intent / State / Effect / ViewModel **without changing what the user sees**. Reuse the same XML (View Binding). See [migration.md](migration.md).
3. Activity-per-screen → single-activity `nav_graph`. Same destinations and back behavior.
4. **`app:startDestination="@id/entranceFragment"`** is mandatory. Implement `EntranceFragment` so it routes to whatever was the previous first screen (splash / language / onboarding / home). Do not insert a new user-facing splash unless the app already had one.
5. Copy `anim/` + `anim-ldrtl/` slide_* into `:core-ui` if missing; every forward `<action>` gets the four anim attrs (`17-navigation`).
6. Images: programmatic binds use `loadImage` (Glide / `ImageViewExtensions`).

**compose:**

1. `MainActivity` in `:app` `setContent { AppTheme { NavGraph() } }` — **never** `GlobalContext.get()`.
2. One `:feature-<kebab>` per screen (`hs-create-mvi` Compose path). Keep existing composables; wrap in `*Screen` / `*ScreenContent`.
3. Root `NavGraph.kt` in `:app`; `startDestination = ENTRANCE_ROUTE`. Entrance routes to the previous first screen.
4. Slide transitions on the `NavHost`. Features never take `NavController`.
5. Images: Coil `AsyncImage`.

**both:**

- Strings: **all** locales move to `:core-ui` — do not drop a translation file.
- Tests for new UseCases/ViewModels only if `writeTestsWithFeatures` is `true`. Fix existing tests’ packages/modules when they break; do not weaken assertions.

## Step 6 — Firebase / platform

Follow `hs-setup-new-project` Step 7 for **placement**:

- Analytics / Crashlytics / Messaging **dependency** on `:core-platform`
- Remote Config on `:data`
- `kotlinx-coroutines-play-services` on `:core-platform` and `:data`

If Messaging (or a `FirebaseMessagingService`) already exists, **keep it** — do not delete push behavior. If FCM was never there, add the **dependency only** (no new service / token UI) per `hs-implement-firebase-messaging`.

`PlatformFirebase` is an `object` with no Context field. Ads revenue helper: only if the app has ads; pass `Context` as an argument; keep this app’s prefs/event strings.

Dispatchers: register **without** `named("io")` / `named("default")`.

## Step 7 — Verify

`hs-setup-new-project` Step 9 checklist **plus**:

- [ ] User confirmed `project-settings.json` (orientation, themeModes, tests, applicationId, appName, **uiFramework**)
- [ ] Landscape / `values-night` match the confirmed settings — not added silently
- [ ] No product behavior rewrite: same APIs, prefs keys, DB schema, analytics event names, ad unit IDs, locales
- [ ] Ads: existing stack preserved **or** `:gmaAds` placed from GitHub; screens wired only if user said yes; not MVI unless explicitly requested
- [ ] Existing FCM service / billing / third-party flows still present
- [ ] Previous start screen still reachable from Entrance
- [ ] UI modules (`:presentation` or `:feature-*`) ↛ `:data`; `lazyModule` only
- [ ] `assembleDebug` succeeds

## Do not

- Use this skill on a greenfield app — use `hs-setup-new-project`
- Rewrite business logic, API parsing, or UI to “clean it up”
- Convert ads (`:gmaAds`, AdMob managers, ad ViewModels) to MVI unless the user **explicitly** asks
- Drop translations, change analytics event names, or rotate signing keys
- Full dependency bump (`hs-gradle-update`) unless required to compile
- Add Data Binding / Hilt. Do not add Compose when `uiFramework` is `xml`. Do not add XML Fragment screens when `uiFramework` is `compose`
- Convert Compose ↔ XML without explicit approval
- Add landscape or night resources unless `project-settings.json` says so
- Skip writing / confirming `project-settings.json`
- Delete an existing `FirebaseMessagingService` or push UI
- Read RC only from the SDK in Fragments (use prefs cache)
- Hardcode secrets

## After setup

Same as `hs-setup-new-project`: optional later `/hs-setup-design-system`; screens via `hs-figma-to-xml` **or** `hs-figma-to-compose` → `hs-create-mvi`; new domain/data via `hs-create-clean-architecture`; `hs-review-complete` before PR. Missing RC / Analytics coverage: `hs-implement-firebase-remote-config`, `hs-implement-firebase-events`, then `add-firebase-*` for later screens.
