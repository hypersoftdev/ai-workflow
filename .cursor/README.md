# Hypersoft AI Workflow — Cursor (v1)

Company-grade **rules** + **skills** for Clean Architecture Android apps (XML + View Binding **or** Jetpack Compose via `uiFramework`, MVI, Koin `lazyModule`).

This folder is part of the [ai-workflow](https://github.com/hypersoftdev/ai-workflow) template. Share with the team by copying `.cursor/` into a project. Claude Code teammates use [`.claude/`](../.claude/) instead — keep both in sync.

## Rules vs Skills

|      | **Rules** (`.cursor/rules/`)                   | **Skills** (`.cursor/skills/`)                             |
|------|------------------------------------------------|------------------------------------------------------------|
| Role | Company law — invariants, naming, architecture | Multi-step playbooks (+ templates)                         |
| When | Always-on or glob-matched while editing        | Invoked by name (`/` or agent pick) or trigger description |
| Size | Prefer clear invariants + BAD/GOOD             | Full workflows                                             |

**Long detail** lives under [`rules/reference/`](rules/reference/). `.mdc` rule files hold **short invariants + links** to those reference docs (so Agent context stays smaller). Do not delete reference files — edit them when rules change.

**Commands:** Prefer skills with `/` invoke. Do not grow a large `.cursor/commands/` tree unless needed.

## Hypersoft skill names

- Invoke as **`/hs-…`** (type `/hs` with no space after — Cursor matches the **folder** name)
- Description always starts with **Hypersoft Cursor Skill:**
- Skill folders are also `hs-*` (example: `ui/hs-figma-to-xml`)
- Claude Code twins in this repo use **`/hsc-…`** so the two lists do not look identical
- Do not pick Figma plugin skills (`figma:design-to-code`, “Created by Figma”) for Android screens — `/hs-figma-to-xml` / `/hs-figma-to-compose` load those internally

## Project settings

After `hs-setup-new-project` / `hs-setup-old-project` (or when joining an app), settings live in:

**[`.cursor/project-settings.json`](project-settings.json)**

All feature/UI/test skills **must read and obey** this file when present:

| Key                      | Values                            | Meaning                                                     |
|--------------------------|-----------------------------------|-------------------------------------------------------------|
| `writeTestsWithFeatures` | `true` / `false`                  | Write unit/integration/E2E tests while scaffolding features |
| `orientation`            | `portrait` / `landscape` / `both` | Which orientations layouts must support                     |
| `themeModes`             | `day` / `night` / `both`          | Day / night / both theme resources                          |
| `applicationId`          | string                            | Root package / applicationId                                |
| `appName`                | string                            | Display name                                                |
| `figmaDesignSystemUrl`   | Figma `/design/` URL or omit/`""` | Optional; Figma file for `hs-setup-design-system`              |
| `uiFramework`            | `xml` / `compose`                 | App UI stack. **xml** = View Binding + `:presentation`. **compose** = Jetpack Compose + `:feature-*` (AnimeHub). Default `xml` if missing |

## Skill map

Invoke as `/hs-<folder>`. Example: `ui/hs-figma-to-xml` → `/hs-figma-to-xml`.

```
project/hs-setup-new-project          Bootstrap multi-module app + persist settings (Firebase BOM + analytics/crashlytics/messaging + RC cache)
project/hs-setup-old-project          Migrate existing production app to hs-setup-new-project architecture; preserve product behavior; confirm settings first
project/hs-setup-design-system        Figma design-system file → :core-ui tokens/themes (+ :core-design AppTheme when compose)
feature/hs-create-mvi                 Presentation MVI only (no domain/data) — Fragment if xml, `*Screen` in `:feature-*` if compose
feature/hs-create-clean-architecture  Domain + data + core pieces as needed
ui/hs-figma-to-xml                    XML layouts (+ Figma design-to-code); `uiFramework` xml only
ui/hs-figma-to-compose                Compose screens (AnimeHub `*Screen` / `*ScreenContent`); `uiFramework` compose
ui/hs-create-dialog                   Dialog UI — XML if xml, Compose `*Dialog` if compose
ui/hs-create-bottom-sheet             Bottom sheet UI — XML if xml, Compose `*BottomSheet` if compose
ui/hs-create-custom-view              Custom View (xml) or reusable composable (compose)
review/hs-review-architecture         Architecture / MVI / boundaries
review/hs-review-performance          ANR / lists / dispatchers
review/hs-review-security             Secrets / manifest / PII
review/hs-review-complete             Runs all review-* + summary report
test/hs-test-unit                     Write+run JVM unit/Flow tests; consent before fix
test/hs-test-integration              Write+run multi-layer tests; consent before fix
test/hs-test-e2e                      Write+run E2E on device; consent before fix
test/hs-test-complete                 Full run + walkthrough; consent before fix
gradle/hs-gradle-organize             Catalog + android/base/dependencies section order (signingConfigs, bundle, R8 optimization/keepRules)
gradle/hs-gradle-update               Groovy→KTS if needed; AGP 9.3+; R8 optimization + keepRules; bump all deps (catalog + hardcodes)
build/hs-build-debug-apk              Debug APK → device install + launch
build/hs-build-release-apk           Release signing + APK → device install + launch
build/hs-build-release-bundle        Release signing + AAB (no device)
platform/hs-implement-in-app-update   Play In-App Updates
platform/hs-implement-in-app-review   Play In-App Review
platform/hs-implement-firebase-messaging  firebase-messaging dep (:core-platform) only
firebase/hs-implement-firebase-messaging  firebase-messaging dep (:core-platform) only
firebase/hs-implement-firebase-remote-config  First-time RC + SharedPref cache + Entrance fetch
firebase/hs-add-firebase-remote-config  Add RC keys to existing SharedPref + Remote Config classes
firebase/hs-implement-firebase-events  First-time full-app Analytics (EventsProvider; screens/buttons)
firebase/hs-add-firebase-events       Add Analytics events for selected screens
premium/hs-implement-in-app-billing   Greenfield Play billing (subs + in-app, v4 stack)
billing/hs-update-in-app-billing      Migrate hypersoft inappbilling v3 → v4.0.0
premium/hs-add-subscription-packages  Add subscription tiers to existing billing
premium/hs-add-inapp-packages         Add one-time in-app products to existing billing
release/pre-release                Ship checklist
```

### Typical feature flow

1. `hs-setup-new-project` (greenfield) or `hs-setup-old-project` (existing production app) — persist **`uiFramework`**
2. `hs-setup-design-system` — Figma tokens/themes in `:core-ui` (and `:core-design` when compose)
3. `hs-figma-to-xml` if `uiFramework` is `xml`; `hs-figma-to-compose` if `compose` (or dialog / bottom-sheet)
4. `hs-create-mvi` — Intent/State/Effect/VM + Fragment **or** `*Screen` in `:feature-*`
5. `hs-create-clean-architecture` — when new domain/data is required

Data patterns (Retrofit, Room, SharedPreferences) live in **rules** + [`.cursor/rules/reference/`](rules/reference/) — not separate skills.

## Rules index (`00`–`28`)

| File                   | Role                                                                                                                                    |
|------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| `00-global`            | Always-on stack + Always/Never law                                                                                                      |
| `01-feature-checklist` | Feature scaffolding checklist (not always-on)                                                                                           |
| `02`–`03`              | Modules + Clean Architecture (+ SOLID in `03`)                                                                                          |
| `04`–`07`              | MVI, Kotlin, coroutines, DI (`04`/`07` → `reference/`)                                                                                  |
| `08`–`10`              | Gradle (`08` → `reference/gradle.md`: section order, signingConfigs, bundle, `base`, R8 `optimization` / `keepRules`), resources/XML, manifest (`09` → `reference/`)     |
| `11`–`13`              | Testing, naming, libraries                                                                                                              |
| `14`–`16`              | Security (always), compatibility, logging (always)                                                                                      |
| `17`–`20`              | Nav, errors, base UI (`19` → `reference/`), permissions                                                                                 |
| `21`–`25`              | Ads/billing (**ads are not MVI** — keep existing ads architecture unless the user asks), Firebase, startup, Figma assets, in-app update |
| `26-data-persistence`  | Retrofit / Room / SharedPreferences patterns                                                                                            |
| `27-in-app-review`     | Play In-App Review placement (`InAppReviewManager`)                                                                                     |
| `28-compose-ui`        | Compose feature modules / Screen-Content / NavGraph (`uiFramework` compose) — [reference/compose-ui.md](rules/reference/compose-ui.md)   |

### `rules/reference/` (full detail)

| File                                                | Backed by rule            |
|-----------------------------------------------------|---------------------------|
| `resources-xml.md`                                  | `09-resources-xml`        |
| `dependency-injection.md`                           | `07-dependency-injection` |
| `gradle.md`                                         | `08-gradle`               |
| `mvi-presentation.md`                               | `04-mvi-presentation`     |
| `base-ui.md`                                        | `19-base-ui`              |
| `compose-ui.md`                                     | `28-compose-ui`           |
| `retrofit.md` / `room.md` / `shared-preferences.md` | `26-data-persistence`     |
| `premium-billing.md`                                | `21-ads-billing`          |

## Distribution

This **is** the dedicated template repo. Copy `.cursor/` (and `.claude/` for Claude Code) into each app. VERSION / CHANGELOG / sync scripts can come later — do not invent local divergent rules inside an app; update this template instead.

## How teammates use this

1. Open project with this `.cursor/` present.
2. Type `/` and pick a skill (e.g. `hs-figma-to-xml` / `hs-figma-to-compose`, `hs-create-mvi`).
3. Obey rules automatically while editing matching files.
4. Before PRs: `/hs-review-complete` or individual `review-*` skills.
