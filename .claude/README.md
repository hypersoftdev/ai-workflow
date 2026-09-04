# Hypersoft AI Workflow — Claude Code (v1)

Company-grade **rules** + **skills** for Clean Architecture Android apps (XML + View Binding **or** Jetpack Compose via `uiFramework`, MVI, Koin `lazyModule`).

This folder is part of the [ai-workflow](https://github.com/hypersoftdev/ai-workflow) template (Cursor users copy [`.cursor/`](../.cursor/) instead). Share with Claude Code teammates by copying `.claude/` into a project. New rules and skills should be added to **both** `.cursor/` and `.claude/`.

## Rules vs Skills

|      | **Rules** (`.claude/rules/`)                   | **Skills** (`.claude/skills/`)                             |
|------|------------------------------------------------|------------------------------------------------------------|
| Role | Company law — invariants, naming, architecture | Multi-step playbooks (+ templates)                         |
| When | Always-on or glob-matched while editing        | Invoked by name (`/` or agent pick) or trigger description |
| Size | Prefer clear invariants + BAD/GOOD             | Full workflows                                             |

**Long detail** lives under [`rules/reference/`](rules/reference/). `.md` rule files hold **short invariants + links** to those reference docs (so Agent context stays smaller). Do not delete reference files — edit them when rules change.

**Commands:** Prefer skills with `/` invoke. Do not grow a large `.claude/commands/` tree unless needed.

## Hypersoft skill names

- Invoke as **`/hsc-…`** (type `/hsc` with no space after — Claude matches the **folder** name)
- Description always starts with **Hypersoft Claude Skill:**
- Skill folders are also `hsc-*` (example: `.claude/skills/hsc-figma-to-xml/`)
- Do not pick Figma plugin skills (`figma:design-to-code`, “Created by Figma”) for Android screens — `/hsc-figma-to-xml` / `/hsc-figma-to-compose` load those internally

## Project settings

After `hsc-setup-new-project` / `hsc-setup-old-project` (or when joining an app), settings live in:

**[`.claude/project-settings.json`](project-settings.json)**

All feature/UI/test skills **must read and obey** this file when present:

| Key                      | Values                            | Meaning                                                     |
|--------------------------|-----------------------------------|-------------------------------------------------------------|
| `writeTestsWithFeatures` | `true` / `false`                  | Write unit/integration/E2E tests while scaffolding features |
| `orientation`            | `portrait` / `landscape` / `both` | Which orientations layouts must support                     |
| `themeModes`             | `day` / `night` / `both`          | Day / night / both theme resources                          |
| `applicationId`          | string                            | Root package / applicationId                                |
| `appName`                | string                            | Display name                                                |
| `figmaDesignSystemUrl`   | Figma `/design/` URL or omit/`""` | Optional; Figma file for `hsc-setup-design-system`              |
| `uiFramework`            | `xml` / `compose`                 | App UI stack. **xml** = View Binding + `:presentation`. **compose** = Jetpack Compose + `:feature-*` (AnimeHub). Default `xml` if missing |

## Skill map

All skills live flat under `.claude/skills/<folder>/SKILL.md` and are invoked as `/hsc-<folder>`.

```
hsc-setup-new-project          Bootstrap multi-module app + persist settings (Firebase BOM + analytics/crashlytics/messaging + RC cache)
hsc-setup-old-project          Migrate existing production app to hsc-setup-new-project architecture; preserve product behavior; confirm settings first
hsc-setup-design-system        Figma design-system file → :core-ui tokens/themes (+ :core-design AppTheme when compose)
hsc-create-mvi                 Presentation MVI only (no domain/data) — Fragment if xml, `*Screen` in `:feature-*` if compose
hsc-create-clean-architecture  Domain + data + core pieces as needed
hsc-figma-to-xml               XML layouts (+ Figma design-to-code); `uiFramework` xml only
hsc-figma-to-compose            Compose screens (AnimeHub `*Screen` / `*ScreenContent`); `uiFramework` compose
hsc-create-dialog               Dialog UI — XML if xml, Compose `*Dialog` if compose
hsc-create-bottom-sheet         Bottom sheet UI — XML if xml, Compose `*BottomSheet` if compose
hsc-create-custom-view          Custom View (xml) or reusable composable (compose)
hsc-review-architecture         Architecture / MVI / boundaries
hsc-review-performance           ANR / lists / dispatchers
hsc-review-security              Secrets / manifest / PII
hsc-review-complete              Runs all review-* + summary report
hsc-test-unit                    Write+run JVM unit/Flow tests; consent before fix
hsc-test-integration              Write+run multi-layer tests; consent before fix
hsc-test-e2e                      Write+run E2E on device; consent before fix
hsc-test-complete                 Full run + walkthrough; consent before fix
hsc-gradle-organize               Catalog + android/base/dependencies section order (signingConfigs, bundle)
hsc-gradle-update                 Groovy→KTS if needed; bump all deps (catalog + hardcodes); migrate to libs.versions.toml + sections
hsc-build-debug-apk               Debug APK → device install + launch
hsc-build-release-apk             Release signing + APK → device install + launch
hsc-build-release-bundle          Release signing + AAB (no device)
hsc-implement-in-app-update       Play In-App Updates
hsc-implement-in-app-review       Play In-App Review
hsc-implement-firebase-messaging  firebase-messaging dep (:core-platform) only
hsc-implement-firebase-remote-config  First-time RC + SharedPref cache + Entrance fetch
hsc-add-firebase-remote-config    Add RC keys to existing SharedPref + Remote Config classes
hsc-implement-firebase-events     First-time full-app Analytics (EventsProvider; screens/buttons)
hsc-add-firebase-events           Add Analytics events for selected screens
hsc-implement-in-app-billing      Greenfield Play billing (subs + in-app, v4 stack)
hsc-update-in-app-billing         Migrate hypersoft inappbilling v3 → v4.0.0
hsc-add-subscription-packages     Add subscription tiers to existing billing
hsc-add-inapp-packages            Add one-time in-app products to existing billing
hsc-implement-admob-ads           First-time AdMob screen wiring (:gmaAds from GitHub)
hsc-add-admob-banner              Add banner placement to existing :gmaAds
hsc-add-admob-interstitial        Add interstitial placement
hsc-add-admob-native              Add native placement
hsc-add-admob-rewarded            Add rewarded placement
hsc-add-admob-rewarded-interstitial  Add rewarded interstitial placement
hsc-add-admob-appOpen-Entrance    Wire App Open ENTRANCE (splash/consent)
hsc-add-admob-appOpen-lifecycle   Wire App Open LIFECYCLE (resume)
```

### Typical feature flow

1. `hsc-setup-new-project` (greenfield) or `hsc-setup-old-project` (existing production app) — persist **`uiFramework`**
2. `hsc-setup-design-system` — Figma tokens/themes in `:core-ui` (and `:core-design` when compose)
3. `hsc-figma-to-xml` if `uiFramework` is `xml`; `hsc-figma-to-compose` if `compose` (or dialog / bottom-sheet)
4. `hsc-create-mvi` — Intent/State/Effect/VM + Fragment **or** `*Screen` in `:feature-*`
5. `hsc-create-clean-architecture` — when new domain/data is required

Data patterns (Retrofit, Room, SharedPreferences) live in **rules** + [`.claude/rules/reference/`](rules/reference/) — not separate skills.

## Rules index (`00`–`28`)

| File                   | Role                                                                                                                                    |
|------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| `00-global`            | Always-on stack + Always/Never law                                                                                                      |
| `01-feature-checklist` | Feature scaffolding checklist (not always-on)                                                                                           |
| `02`–`03`              | Modules + Clean Architecture (+ SOLID in `03`)                                                                                          |
| `04`–`07`              | MVI, Kotlin, coroutines, DI (`04`/`07` → `reference/`)                                                                                  |
| `08`–`10`              | Gradle (`08` → `reference/gradle.md`: section order, signingConfigs, bundle, `base`), resources/XML, manifest (`09` → `reference/`)     |
| `11`–`13`              | Testing, naming, libraries                                                                                                              |
| `14`–`16`              | Security (always), compatibility, logging (always)                                                                                      |
| `17`–`20`              | Nav, errors, base UI (`19` → `reference/`), permissions                                                                                 |
| `21`–`25`              | Ads/billing (**ads are not MVI** — `:gmaAds` from GitHub; see `hsc-implement-admob-ads` / `hsc-add-admob-*` + `reference/ads-gma.md`), Firebase, startup, Figma assets, in-app update |
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
| `ads-gma.md`                                        | `21-ads-billing`          |

## Distribution

This **is** the dedicated template repo. Copy `.claude/` (and `.cursor/` for Cursor) into each app. VERSION / CHANGELOG / sync scripts can come later — do not invent local divergent rules inside an app; update this template instead.

## How teammates use this

1. Copy `.claude/` into the project root (this folder includes [`CLAUDE.md`](CLAUDE.md)).
2. Open Android Studio, terminal at the repo root, run `claude`.
3. Type `/` and pick a skill (e.g. `/hsc-figma-to-xml`, `/hsc-figma-to-compose`, `/hsc-create-mvi`).
4. Path-scoped rules load when matching files are touched; `00-global`, `14-security-secrets`, and `16-logging` always apply.
5. Before PRs: `/hsc-review-complete` or individual `review-*` skills.
