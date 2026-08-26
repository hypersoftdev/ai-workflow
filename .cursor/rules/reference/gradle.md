# Gradle, build config, catalog sections/naming, and dependency management

Full detail for `08-gradle.mdc`. Do not delete lines from this file — edit here and keep the rule stub in sync.

## Structure

- Root `build.gradle.kts`: plugins with `apply false` only — no dependencies
- Module scripts: `plugins` → `android { }` → `base { }` (`:app` only) → `dependencies { }`
- `settings.gradle.kts`: `FAIL_ON_PROJECT_REPOS`, filtered repositories

## Module `build.gradle.kts` order

1. `plugins { }` — catalog aliases only
2. `android { }` — sections in the order below (omit what does not belong on that module)
3. `base { }` — **`:app` only** (`archivesName`)
4. `dependencies { }` — **project modules first**, then libraries by section

### `:app` — `android { }` section order (mandatory)

```kotlin
plugins {
    alias(libs.plugins.android.application)
    // google-services / crashlytics / safe-args / parcelize when needed
    // compose: alias(libs.plugins.kotlin.compose) — Compose Compiler only, never kotlin-android
}

android {
    namespace = "com.company.app"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.company.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.1" // first Play / store build; bump both code + name together after that
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("path/to/App.jks") // or file("") if none
            storePassword = ""
            keyAlias = ""
            keyPassword = ""
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".testing"
            // no optimization — debug stays unminified
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true // R8 code shrinking + optimized resource shrinking (AGP 9.3+)
            }
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    bundle {
        language {
            enableSplit = false
        }
    }
}

base {
    archivesName = "App-Name-Account-v${android.defaultConfig.versionCode}(${android.defaultConfig.versionName})"
}
```

### Library modules (`:presentation`, `:data`, `:domain`, `:core-*`, …)

Same section order as `:app`, but **do not add** app-only pieces:

| Section                 | Library modules                                                    |
|-------------------------|--------------------------------------------------------------------|
| `plugins`               | `android.library` (+ parcelize / safe-args / `kotlin-compose` when needed). **Never** `kotlin-android` |
| `compileSdk`            | Yes — same `release(37) { minorApiLevel = 1 }` block as `:app`     |
| `defaultConfig`         | `minSdk` only (no `applicationId` / `versionCode` / `versionName`) |
| `signingConfigs`        | **Never**                                                          |
| `buildTypes`            | **Omit** unless flavors already exist. Never `optimization.enable`, `isMinifyEnabled`, or `proguardFiles` |
| `buildFeatures`         | xml UI: View Binding / `buildConfig`. compose `:feature-*` / `:core-design`: `compose = true` (no View Binding) |
| `compileOptions`        | Yes (Java 21)                                                      |
| `kotlin` / `jvm` / `kotlinOptions` | **Never** — AGP 9+ built-in Kotlin                        |
| `bundle`                | **Never** (app only)                                               |
| `base` / `archivesName` | **Never** (app only)                                               |

```kotlin
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.company.app.presentation"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
```

Library keep rules (consumer rules for the app’s R8 pass) live in `src/main/keepRules/*.keep` — **not** `consumerProguardFiles` / `proguard-rules.pro`. See **R8** below.

### `signingConfigs` (always on `:app`)

- If missing → **add** `signingConfigs { create("release") { … } }` and wire `release { signingConfig = signingConfigs.getByName("release") }`
- Search for `*.jks` in this order: **project root**, then **`:app`** directory
- If a `.jks` is found → set `storeFile = file("<absolute-or-relative-path>")`
- If **no** `.jks` → keep `storeFile` / `storePassword` / `keyAlias` / `keyPassword` as **empty strings** (`""`)
- Do not invent passwords; do not copy secrets into skills/docs. Prefer empty placeholders; fill locally or via CI / `local.properties` outside git

### `bundle` (always on `:app`)

Every project `:app` module must include:

```kotlin
bundle {
    language {
        enableSplit = false
    }
}
```

### `base` archivesName (`:app` only)

Format: `{AppName}-{Account}-v{versionCode}({versionName})`

Examples from reference apps: `Qibla-Finder-HS-v35(1.3.5)`, `Music-Player-DGH-v8(1.0.8)`, `Speak-Translate-HS-v1(1.0.1)`.

## Dependencies block â€” sections (mandatory)

Group with **comment headers**. Prefer this order (omit empty sections):

```kotlin
dependencies {
    // Project modules (or list without a header if few)
    implementation(project(":core-common"))
    implementation(project(":core-ui"))
    implementation(project(":domain"))

    // Android Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Splash Screen Api
    // Lifecycle
    // Fragment Ktx
    // Navigational Components
    // Google
    // Google Play Services, Maps & Location
    // Firebase
    // Dependency Injection -> Koin
    // Kotlin Coroutines
    // CameraView / Glide / Lottie / Shimmer (feature libs)
    // Testing
    // Core library desugaring (java.time APIs support on older APIs)
}
```

Rules:

- One blank line between section groups when helpful
- Keep related libs together under the matching header
- Comment out unused deps with `#` / `//` â€” do not leave orphan versions without a section

## Version catalog (`libs.versions.toml`)

### File sections

Keep **three** TOML tables. Inside `[versions]` and `[libraries]`, use the **same comment section headers** as module scripts:

```toml
[versions]
# -------------- Plugins -------------- #
agp = "…"   # latest stable 9.x (9.3+)

# -------------- Dependencies -------------- #
coreKtx = "â€¦"
# Splash Screen Api
# Fragment Ktx
# Navigational Components
# Lifecycle
# Google
# Firebase
# Location & Coroutine (for await) calls
# Kotlin Coroutines
# Dependency Injection -> Koin
# Testing
# â€¦

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
# never: kotlin-android / org.jetbrains.kotlin.android
# compose apps only: kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }

# Splash Screen Api
androidx-core-splashscreen = { â€¦ }

# Dependency Injection -> Koin
koin-android = { â€¦ }
# â€¦
```

### Catalog naming

| Kind            | Convention      | Example                                                       |
|-----------------|-----------------|---------------------------------------------------------------|
| Version keys    | camelCase       | `coreKtx`, `koinAndroid`, `playServicesLocation`              |
| Library aliases | kebab-case      | `androidx-core-ktx`, `koin-android`, `play-services-location` |
| Plugin aliases  | kebab-case      | `android-application`, `android-library`, `navigation-safe-args` |
| Gradle accessor | dots from kebab | `libs.androidx.core.ktx`, `libs.koin.android`                 |

Rules:

- Alias name mirrors artifact intent (`androidx-fragment-ktx`, not `fragment`)
- Prefer `group` + `name` + `version.ref`; `module = "g:a"` only when matching existing style for that lib
- Plugins and libraries that share a version family share one version key (e.g. one `lifecycle` for viewmodel/runtime/process)
- **Never** hardcode `"group:artifact:version"` in module scripts

```kotlin
// âœ… GOOD
implementation(libs.androidx.core.ktx)

// âŒ BAD
implementation("androidx.core:core-ktx:1.12.0")
```

### Always use latest stable versions

- When **adding** or **updating** any dependency/plugin, use the **latest stable** release available at that time
- Look up current versions (Maven Central / Google Maven / library docs) — do not copy stale versions from memory or old projects
- Prefer stable over alpha/beta/rc unless the user explicitly asks for a pre-release
- Keep **AGP 9.3+** (latest stable 9.x) and the Gradle wrapper compatible when bumping. Do **not** apply `org.jetbrains.kotlin.android`
- Update the version in `[versions]` only — all aliases using `version.ref` pick it up
- When scaffolding a new project (`hs-setup-new-project`), seed the catalog with latest stable for the whole core stack
- **`hs-setup-new-project` mandatory:** Firebase BOM + `firebase-analytics` / `firebase-crashlytics` / `firebase-messaging` on **`:core-platform`**; `firebase-config` + `kotlinx-coroutines-play-services` on **`:data`** (see `hs-implement-firebase-messaging` — no MessagingService). Place `kotlinx-coroutines-play-services` under `# Kotlin Coroutines` / `// Kotlin Coroutines`.

### `hs-gradle-update` (mandatory behavior)

When the user runs **`hs-gradle-update`** (or asks to bump dependencies):

1. If Groovy `build.gradle` / `settings.gradle` remain → convert to Kotlin DSL **first** (same contract as `hs-setup-old-project` Step 2 / `hs-gradle-update` Step 0). Keep versions during conversion; bump after.
2. Bump to **AGP 9.3+** (latest stable 9.x). Remove `kotlin-android` / `kotlin-kapt` / `android.kotlinOptions` / `android.builtInKotlin=false` (`hs-gradle-update` Step 0.5). Built-in Kotlin — do not re-add the Kotlin Android plugin. Then migrate legacy minify/ProGuard to `optimization { enable = true }` + `src/main/keepRules/*.keep` (Step 0.6).
3. Inventory **both** `libs.versions.toml` `[versions]` **and** every hardcoded `"group:artifact:version"` in module Gradle scripts
4. Resolve latest stable for **each** (do not skip Glide / ads / Firebase / etc. because they were hardcoded)
5. If `gradle/libs.versions.toml` is missing → **create** it (`[versions]` / `[plugins]` / `[libraries]` + section comments per this doc / `hs-gradle-organize`)
6. Migrate each hardcoded dep into the catalog (version key + library alias under the correct section, e.g. `# Glide`), then replace with `implementation(libs.…)`
7. Place module `implementation` lines under the matching `//` header (`// Glide`, not under `// Testing`)
8. Migrating an **existing** hardcoded dependency into the catalog is **not** “adding a new library” — it is required on every update run
9. Leave **zero** hardcoded Maven coordinates in `*.gradle.kts` when the update finishes
10. Leave **zero** Groovy module/settings scripts when conversion was possible

Editing this section → also update `hs-gradle-update`, `hs-gradle-organize`, `hs-setup-old-project` (+ `migration.md`), `08-gradle.mdc`, and `.claude` twins.

```kotlin
// ❌ BAD — leftover hardcode after hs-gradle-update
implementation("com.github.bumptech.glide:glide:5.0.5")

// ✅ GOOD — catalog + correct section
// Glide
implementation(libs.glide)
```

```toml
# Glide
glide = "5.0.9"
# …
# Glide
glide = { group = "com.github.bumptech.glide", name = "glide", version.ref = "glide" }
```

## SDK defaults (adjust only on project-wide request)

- minSdk = 24
- **compileSdk 37.1** via `compileSdk { version = release(37) { minorApiLevel = 1 } }`
- **targetSdk = 37**
- First store build: `versionCode = 1`, `versionName = "1.0.1"` (new projects only — never rewrite an existing app’s versions)
- Java 21 (`compileOptions` `VERSION_21`). No `kotlinOptions` / `kotlin { }` / `jvmToolchain`
- **AGP 9.3+** (latest stable 9.x) with built-in Kotlin (no `kotlin-android` plugin). Pair the Gradle wrapper with that AGP (Android Studio new projects: AGP 9.3.2 + Gradle 9.7.1 — look up the current pair)
- Kotlin official code style

## Dependency scope

| Scope                       | When                                            |
|-----------------------------|-------------------------------------------------|
| `implementation`            | Default for almost everything                   |
| `api`                       | Rare â€” only when types must leak to consumers |
| `testImplementation`        | Unit tests                                      |
| `androidTestImplementation` | Instrumentation tests                           |
| `coreLibraryDesugaring`     | `desugar_jdk_libs` when using `java.time`       |

## Module rules

- Keep modules independent â€” no circular deps
- Feature modules must not depend on each other directly
- xml UI modules: View Binding on — never Data Binding
- compose `:feature-*` / `:core-design`: `buildFeatures { compose = true }` + Compose Compiler plugin (`kotlin-compose`) — never `kotlin-android`, never Data Binding, skip View Binding
- App release: R8 on (`optimization { enable = true }` — code **and** resource shrinking). Library modules: do **not** enable optimization

## Build types

- Debug (`:app`): `applicationIdSuffix = ".testing"`. No `optimization` block (R8 off)
- Release (`:app`): `signingConfig = signingConfigs.getByName("release")` + `optimization { enable = true }`
- Library modules: omit `buildTypes` unless flavors already exist. Never `optimization.enable` / `isMinifyEnabled` / `proguardFiles`
- Prefer no product flavors unless product requires them
- Signing: always declare `signingConfigs` on `:app` (see above); passwords empty unless already present in the project — prefer CI / `local.properties` over committing secrets

## R8 (AGP 9.3+ — recommended)

Do **not** use the legacy DSL on AGP 9.3+: no `isMinifyEnabled`, `isShrinkResources`, `proguardFiles()`, `consumerProguardFiles()`, or `proguard-rules.pro`.

`optimization { enable = true }` on `:app` **release** is the replacement for `isMinifyEnabled = true` + `isShrinkResources = true`. It turns on R8 code shrinking **and** optimized resource shrinking together. Default Android platform keep rules (equivalent to `proguard-android-optimize.txt`) are included automatically. Set `optimization.keepRules.includeDefault = false` only if you must manage every rule yourself.

### Keep rules files (`*.keep`)

Put rules in `src/main/keepRules/` with a `.keep` suffix (e.g. `src/main/keepRules/rules.keep`). AGP combines every `.keep` file in that source set. **Do not** declare the files in Gradle.

Place rules **next to the code they protect**:

| Module | File | Typical keeps |
|--------|------|----------------|
| `:app` | `app/src/main/keepRules/rules.keep` | SourceFile/LineNumberTable (Crashlytics), Parcelable `CREATOR` |
| `:domain` | `…/src/main/keepRules/rules.keep` | `domain.entity.**` |
| `:presentation` (xml) | `…/src/main/keepRules/rules.keep` | `presentation.**.state.**` / `intent.**` / `effect.**` / `model.**` |
| `:feature-*` (compose) | `…/src/main/keepRules/rules.keep` | `feature.**.state.**` / `intent.**` / `effect.**` / `model.**` |
| `:gmaAds` (when present) | `…/src/main/keepRules/rules.keep` | ads entity packages |
| `:data` | only if Room minify breaks entities | Room `@Entity` types |

Library `.keep` files are **consumer rules** — packaged into the AAR and applied when the app runs R8. That replaces `consumerProguardFiles("consumer-rules.pro")`.

```
# app/src/main/keepRules/rules.keep
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
```

Do **not** disable obfuscation to “fix” crashes — add a targeted keep instead.

### Migrating old ProGuard files

When `proguard-rules.pro` / `consumer-rules.pro` / `proguardFiles` / `isMinifyEnabled` still exist (`hs-gradle-update` Step 0.6 / `hs-setup-old-project`):

1. Copy rule bodies into `src/main/keepRules/rules.keep` on the **same module** (create the folder). Update class names if packages moved.
2. Delete the `.pro` files.
3. `:app` release: replace minify/shrink/`proguardFiles` with `optimization { enable = true }`. Drop those lines from debug.
4. Libraries: remove `buildTypes` minify/`proguardFiles` and `consumerProguardFiles`. Keep rules stay in `src/main/keepRules/*.keep` only.

Do **not** leave a mix of legacy minify DSL and `optimization { }` on the same build type.

## Other

- **Always** set `bundle.language.enableSplit = false` on `:app` (all locales in one APK/AAB)
- Enable core library desugaring when using `java.time` below API 26
- Do **not** add `kotlin { }` / `jvmToolchain` / `android.kotlinOptions` on Android modules — AGP 9+ built-in Kotlin follows `compileOptions` (`VERSION_21`)
