---
name: hsc-gradle-update
description: Hypersoft Claude Skill: Bump all project dependencies to latest stable — AGP 9.3+ with built-in Kotlin (remove kotlin-android / kotlin-kapt). Migrate R8 to optimization { enable = true } + src/main/keepRules. Convert Groovy → Kotlin DSL when needed, bump catalog versions and any hardcoded group:artifact:version in modules. Migrate hardcodes into libs.versions.toml (create catalog if missing) and place deps under hsc-gradle-organize sections. Does not add brand-new libraries without approval.
---

# Gradle Update

Follow `.claude/rules/08-gradle.md` + [reference/gradle.md](../../rules/reference/gradle.md), `13-libraries-stack.md`.

This skill **bumps versions**. Section order / catalog layout → **`hsc-gradle-organize`** (run its logic as part of this flow when hardcodes or section placement are wrong). Groovy `*.gradle` → Kotlin DSL → do that **before** bumping (same conversion contract as `hsc-setup-old-project` Step 2). Then **AGP 9.3+ / built-in Kotlin** (Step 0.5) and **R8 `optimization` / `keepRules`** (Step 0.6) before or with the AGP bump.

When you change this skill’s Gradle behavior, also update the Gradle cluster: `hsc-gradle-organize`, `hsc-setup-old-project` (+ `migration.md`), `08-gradle` + `reference/gradle.md`, and `.cursor` twins (`MUST_READ_BEFORE_SKILL_CHANGES.md`).

## Scope

1. **Groovy → Kotlin DSL** when any `build.gradle` / `settings.gradle` (non-`.kts`) remains
2. **AGP 9.3+** (latest stable 9.x) with **built-in Kotlin** — remove `org.jetbrains.kotlin.android` / `kotlin-android` / `kotlin-kapt` (see Step 0.5). Migrate R8 to `optimization { enable = true }` + `src/main/keepRules/*.keep` (Step 0.6)
3. **All** dependency versions in the project:
    - `gradle/libs.versions.toml` `[versions]` (and shared plugin/library refs)
    - **Hardcoded** `"group:artifact:version"` / `group: "…", name: "…", version = "…"` in every `*.gradle.kts` / `*.gradle`
4. Create or extend `gradle/libs.versions.toml` if missing or incomplete
5. Root / module scripts only when a plugin id or apply style must change
6. Prefer **latest stable** — no alphas/betas/RCs unless user asks
7. **Do not** introduce brand-new libraries the project never had — migrating an **existing** hardcoded dep into the catalog **is required** and is not a “new library”

## Steps (mandatory)

### 0 — Groovy → Kotlin DSL (before bumps)

If the repo still has Groovy Gradle scripts:

1. Convert `settings.gradle` → `settings.gradle.kts`, root/module `build.gradle` → `build.gradle.kts`
2. Prefer Version Catalog (`libs.versions.toml`) shape from **`hsc-gradle-organize`** / [reference/gradle.md](../../rules/reference/gradle.md)
3. Keep existing dependency **versions** during conversion; bumping happens in later steps
4. Delete obsolete Groovy files after the `.kts` scripts work
5. Same conversion rules as `hsc-setup-old-project` Step 2 / [migration.md](../hsc-setup-old-project/migration.md) — keep those docs aligned when this step changes

Do **not** leave a mix of Groovy and Kotlin DSL module scripts after this skill finishes.

### 0.5 — AGP 9.3+ / built-in Kotlin (mandatory)

AGP 9+ compiles Kotlin **without** the Kotlin Android plugin. Keep module scripts simple: `com.android.application` / `com.android.library` (+ google-services / crashlytics / safe-args / parcelize / **compose compiler** when needed). Use **AGP 9.3+** so the R8 `optimization` / `keepRules` DSL in Step 0.6 is available.

If AGP is below **9.3** **or** any file still applies `org.jetbrains.kotlin.android` / `kotlin-android` / `kotlin-kapt` / `android.kotlinOptions`:

1. Bump catalog `agp` to the **latest stable 9.x** (9.3+). Never leave 8.x after this skill.
2. Bump the Gradle wrapper to the version that AGP requires (see current AGP release notes). Android Studio new projects pair AGP 9.3.2 with Gradle **9.7.1** — look up the current pair; do not guess.
3. **Remove** `org.jetbrains.kotlin.android` and `kotlin-android` from every module `plugins { }`, the root `plugins { }` (`apply false`), and `[plugins]` in `libs.versions.toml`.
4. Drop catalog `kotlin = "…"` **unless** the app still needs it for the **Compose Compiler** plugin (`org.jetbrains.kotlin.plugin.compose` / `kotlin-compose`). That plugin is **not** `kotlin-android`.
5. Remove `android.kotlinOptions { }` and leftover `kotlin { }` / `jvmToolchain` on Android modules. JVM target follows `compileOptions` (**Java 21** / `VERSION_21`). Set `sourceCompatibility` / `targetCompatibility` to `JavaVersion.VERSION_21` on every Android module.
6. If the project uses **kapt**: migrate to **KSP**. Do **not** add `com.android.legacy-kapt`.
7. Remove `android.builtInKotlin=false` and `android.newDsl=false` from `gradle.properties` if present — migrate; do not opt out.
8. Set `compileSdk { version = release(37) { minorApiLevel = 1 } }` and `targetSdk = 37` on modules that still use a lower / integer-only compileSdk. Do **not** change an existing app’s `versionCode` / `versionName`.

Do **not** re-add `kotlin-android` to “fix” a Compose or annotation-processor build.

### 0.6 — R8 `optimization` + `keepRules` (AGP 9.3+, mandatory)

Recommended DSL: [Enable app optimization](https://developer.android.com/topic/performance/app-optimization/enable-app-optimization). `optimization { enable = true }` turns on **code shrinking and optimized resource shrinking** together — same outcome as the old `isMinifyEnabled = true` + `isShrinkResources = true`. Default Android keep rules (`proguard-android-optimize.txt` equivalent) are included unless `keepRules.includeDefault = false`.

If any module still has `isMinifyEnabled`, `isShrinkResources`, `proguardFiles`, `consumerProguardFiles`, `proguard-rules.pro`, or `consumer-rules.pro`:

1. **`:app` release:** replace those flags with:
   ```kotlin
   optimization {
       enable = true
   }
   ```
   Keep `signingConfig`. Drop minify / shrink / `proguardFiles` from **debug** (debug stays unminified; keep `applicationIdSuffix` if present).
2. **Keep rules:** copy every custom rule from `proguard-rules.pro` / `consumer-rules.pro` into `src/main/keepRules/rules.keep` on the **same module**. Create the folder. Do not declare the file in Gradle.
3. Delete `proguard-rules.pro` and `consumer-rules.pro`. Remove `consumerProguardFiles(...)` from `defaultConfig`.
4. **Library modules:** do **not** set `optimization.enable`. Remove minify/`proguardFiles` `buildTypes` (omit `buildTypes` unless flavors already exist). Library `.keep` files are consumer rules for the app’s R8 pass.
5. Do **not** mix legacy minify DSL and `optimization { }` on the same build type.
6. Seed missing keeps from [reference/gradle.md](../../rules/reference/gradle.md) (entities, MVI packages, Crashlytics line numbers) if the old `.pro` was empty or missing.

Do **not** skip resource shrinking — `optimization.enable = true` includes it. Do **not** re-add `proguardFiles` “to be safe”.

### 1 — Inventory

Scan the whole repo for versions to bump:

- Every key under `[versions]` in `libs.versions.toml` (create the file with `[versions]` / `[plugins]` / `[libraries]` if absent — see `hsc-gradle-organize` / [reference/gradle.md](../../rules/reference/gradle.md))
- Every module script string like `implementation("com.github.bumptech.glide:glide:5.0.5")`
- `buildscript` / plugin version literals still outside the catalog
- Gradle wrapper (`gradle-wrapper.properties`) when AGP requires a newer Gradle

### 2 — Resolve latest stable

For **each** inventoried artifact (catalog **and** hardcoded), look up the latest stable (Maven Central / Google Maven / official docs). Do not skip Glide, Firebase BOM children, mediation SDKs, etc. because they were hardcoded.

Example: Glide `com.github.bumptech.glide:glide` — bump `5.0.5` → current stable (e.g. `5.0.9`), never leave an old hardcoded pin.

Keep **AGP 9.3+** ↔ Gradle wrapper compatible. Built-in Kotlin ships with AGP — do not pin `org.jetbrains.kotlin.android`. If KSP or Compose Compiler is present, use versions that match the AGP-bundled Kotlin (AGP upgrades a too-old KSP automatically).

### 3 — Migrate hardcodes into the catalog (before or while bumping)

For each hardcoded `group:artifact:version`:

1. Add a camelCase version key under the correct `[versions]` section comment (e.g. `# Glide` → `glide = "5.0.9"`)
2. Add a kebab-case `[libraries]` alias under the same section (e.g. `glide = { group = "com.github.bumptech.glide", name = "glide", version.ref = "glide" }`)
3. Replace the module line with `implementation(libs.glide)` (or matching accessor)
4. Place the `implementation(…)` under the correct `//` section header in that module (`// Glide`, not under `// Testing`)

If the catalog file does not exist → **create** `gradle/libs.versions.toml` in `hsc-gradle-organize` format, wire Version Catalog if needed, then migrate.

### 4 — Bump catalog keys

- Update `[versions]` only (aliases with `version.ref` follow)
- One shared key per family (`lifecycle`, `koin`, …)
- Update AGP 9.3+ / wrapper together; drop `kotlin-android`. Run Step 0.6 (R8). Compose Compiler / KSP only if the project already uses them

### 5 — Apply `hsc-gradle-organize` placement

After bumps/migrations:

- Project modules first, then library sections with exact headers from `hsc-gradle-organize`
- No hardcoded Maven coordinates left in `*.gradle.kts`
- Glide under `// Glide`; tests under `// Testing`; etc.

### 6 — Verify

- Sync / `assembleDebug` if practical
- Fix breakages with minimal diffs

## Report

```markdown
## Gradle update summary

- Groovy → Kotlin DSL: … (converted / already .kts / none found)
- AGP 9.3+ / built-in Kotlin: … (agp version; kotlin-android removed / already gone)
- R8 optimization / keepRules: … (migrated / already new DSL)
- compileOptions Java 21 / compileSdk 37.1: …
- Bumped (catalog): …
- Bumped / migrated (was hardcoded): … (e.g. Glide 5.0.5 → 5.0.9 → libs.glide)
- Catalog created / extended: …
- Section placement fixed: …
- Left unchanged (already latest / blocked pre-release): …
- Compatibility notes: …
- Build: Pass/Fail/Not run
```

## Do not

- Leave Groovy `build.gradle` / `settings.gradle` after an update run when conversion is possible
- Leave any `"group:artifact:version"` in module scripts after an update run
- Skip bumping a dep because it was hardcoded instead of in the catalog
- Add Compose / Hilt / new stacks the project never used (`13-libraries-stack`) without approval. **Exception:** when `uiFramework` is `compose`, bumping the existing Compose BOM / Navigation Compose / Coil is in scope
- Change `api` vs `implementation` casually
- Leave AGP 8.x or 9.0–9.2, or keep `kotlin-android` / `kotlin-kapt` / `android.kotlinOptions` / `android.builtInKotlin=false` after an update run
- Leave `isMinifyEnabled` / `isShrinkResources` / `proguardFiles` / `proguard-rules.pro` after an update run — use `optimization { enable = true }` + `src/main/keepRules/*.keep`
- Turn off resource shrinking (it is included when `optimization.enable = true`)
- Change an existing app’s `versionCode` / `versionName` as part of a dep bump
- Force incompatible AGP / Gradle-wrapper pairs
- Commit secrets from `local.properties`
- Dump new catalog entries at the bottom without the correct section comment
- Change only this skill when Groovy → DSL / catalog / organize rules change — update the whole Gradle cluster (`MUST_READ_BEFORE_SKILL_CHANGES.md`)