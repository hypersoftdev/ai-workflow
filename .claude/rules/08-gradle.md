---
description: Gradle, build config, catalog sections/naming, and dependency management
paths:
  - "**/*.gradle.kts"
  - "**/libs.versions.toml"
  - "**/gradle.properties"
---

# Gradle (invariants)

**Full detail (android section order, signingConfigs, bundle, `base`, catalog, R8 `optimization` / `keepRules`):** [reference/gradle.md](reference/gradle.md)

## Must follow

- Root `build.gradle.kts`: plugins `apply false` only
- Prefer **Kotlin DSL only** (`*.gradle.kts`). If Groovy `*.gradle` / `settings.gradle` remain, convert before organize/update (`hsc-gradle-update` Step 0 / `hsc-setup-old-project` Step 2)
- Module order: `plugins` → `android` → (`base` on `:app` only) → `dependencies`
- `:app` `android` order: `namespace` → `compileSdk` → `defaultConfig` → **`signingConfigs`** → `buildTypes` → `buildFeatures` → `compileOptions` → **`bundle`**. No `kotlin { }` / `jvmToolchain` / `android.kotlinOptions`. `compileSdk { version = release(37) { minorApiLevel = 1 } }`; `targetSdk = 37`; new apps `versionName = "1.0.1"`
- Library modules: same order; **omit** `signingConfigs`, `bundle`, `base`, app-only `defaultConfig` fields, and minify/`proguardFiles` `buildTypes`
- Always add `:app` `signingConfigs` if missing — find `*.jks` in root then `app/`; empty strings if none
- Always add `:app` `bundle { language { enableSplit = false } }`
- `:app` `base { archivesName = "AppName-Account-v{versionCode}({versionName})" }`
- Dependencies: project modules first, then section headers (Android Core, Lifecycle, Firebase, Koin, Testing, …)
- Version catalog: `[versions]` / `[plugins]` / `[libraries]`; camelCase version keys; kebab-case aliases; **never** hardcode `g:a:v` in modules
- **`hsc-gradle-update` must** convert Groovy → Kotlin DSL when needed; bump to **AGP 9.3+** and remove `kotlin-android`; migrate R8 to `optimization { enable = true }` + `src/main/keepRules/*.keep`; bump catalog **and** any hardcoded deps; migrate hardcodes into `libs.versions.toml` (create file if missing); place under `hsc-gradle-organize` section headers (e.g. Glide → `// Glide`, not under Testing)
- Latest stable on add/update; **AGP 9.3+** (never 8.x after `hsc-gradle-update`); Java 21 (`VERSION_21`); minSdk 24; compileSdk **37.1** / targetSdk **37**. **No** `org.jetbrains.kotlin.android` / `kotlin-android` / `kotlin-kapt` — AGP built-in Kotlin. xml UI: View Binding on; compose: `buildFeatures { compose = true }` + Compose Compiler plugin (`kotlin-compose`) only — never Data Binding
- Signing secrets: empty placeholders or `local.properties` / CI — never commit real keystore passwords into docs/skills
- R8: `:app` release `optimization { enable = true }` (code **and** resource shrinking). No `isMinifyEnabled` / `isShrinkResources` / `proguardFiles` / `proguard-rules.pro`. Keep rules in `src/main/keepRules/*.keep` on **app and library modules** that need them (domain entities, presentation/feature state/intent/effect/model, ads when present)
- Changing Gradle law here → also update `hsc-gradle-update`, `hsc-gradle-organize`, `hsc-setup-old-project` Groovy→KTS steps, `reference/gradle.md`, and `.cursor` twins

Read [reference/gradle.md](reference/gradle.md) when editing Gradle or the catalog.
