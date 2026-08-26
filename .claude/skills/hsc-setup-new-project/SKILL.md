---
name: hsc-setup-new-project
description: Hypersoft Claude Skill: Bootstrap a new Android multi-module app (domain, data, core-*, xml `:presentation` or compose `:feature-*`) with MainActivity, Entrance start destination, Parent* or Compose NavGraph, PlatformFirebase, Remote Config → SharedPreferences cache, and mandatory firebase-messaging on core-platform. Use when starting a greenfield project. Do not use on an existing production app — use hsc-setup-old-project. Confirms and persists project settings first including uiFramework.
---

# Setup New Project

Follow `.claude/rules/` — especially `00-global`, `02-project-structure`, `07-dependency-injection`, `08-gradle`, `09-resources-xml`, `17-navigation`, `19-base-ui`, `22-platform-firebase`, `23-app-startup`, `26-data-persistence`, `28-compose-ui` (when compose).

## Preconditions (ask if missing — then persist)

Confirm with the user before scaffolding. Write answers to **`.claude/project-settings.json`**:

```json
{
  "writeTestsWithFeatures": true,
  "orientation": "both",
  "themeModes": "both",
  "applicationId": "com.company.app",
  "appName": "App Display Name",
  "uiFramework": "xml",
  "figmaDesignSystemUrl": ""
}
```

| Setting                        | Allowed values                    | Effect                                                         |
|--------------------------------|-----------------------------------|----------------------------------------------------------------|
| `applicationId` / root package | e.g. `com.company.app`            | Module namespaces, package roots                               |
| `appName`                      | display name                      | Launcher label / strings                                       |
| `writeTestsWithFeatures`       | `true` / `false`                  | Whether later skills add UseCase/ViewModel tests with features |
| `orientation`                  | `portrait` / `landscape` / `both` | Layout orientation support (default `both`)                    |
| `themeModes`                   | `day` / `night` / `both`          | Theme resource folders (default `both`)                        |
| `figmaDesignSystemUrl`         | Figma `/design/` URL or `""`      | Optional; set when user picks design-system option **a**       |
| `uiFramework`                  | `xml` / `compose`                 | **Ask first.** xml = View Binding + `:presentation`. compose = Jetpack Compose + `:feature-*` (AnimeHub) |

Also ask:

0. **`uiFramework`** — `xml` or `compose`. Show both options. Default is `xml`. Compose follows AnimeHub (`:feature-*`, `:core-design`, `:app` `NavGraph.kt`) — see `28-compose-ui` + [templates/compose/](templates/compose/).
1. Optional: **ads** — do not add ad SDKs without approval. If adding later, copy the existing ads architecture from the reference app — do **not** convert ads to MVI unless the user **explicitly** asks
2. **Firebase Cloud Messaging** is **mandatory** for every new project: add `firebase-messaging` to the catalog + `implementation` on `:core-platform` only (dependency — no `FirebaseMessagingService` or push UI). See **`hsc-implement-firebase-messaging`**.
3. **Design system (Figma)** — ask the user to pick:
   - **a)** Provide a Figma link to build the design system (`figma.com/design/...`)
   - **b)** Ignore design system now (later)

   If **a**: save the URL as `figmaDesignSystemUrl` in `project-settings.json`. After Step 6 (`:core-ui` exists), run **`hsc-setup-design-system`** with that URL (theme-first tokens in `:core-ui`).  
   If **b**: leave `figmaDesignSystemUrl` empty; scaffold default Material3 `themes.xml` / `colors.xml` in Step 6. User can invoke `/hsc-setup-design-system` later.

All later skills **must read** `.claude/project-settings.json` and obey it.

## Module set (mandatory)

| Module           | Required | Role                                                                                                          |
|------------------|----------|---------------------------------------------------------------------------------------------------------------|
| `:app`           | Must     | `App`, manifest, DI aggregation only — **no `res/values/`**                                                   |
| `:domain`        | Must     | Entities, repository interfaces, use cases                                                                    |
| `:data`          | Must     | Repository impls, DataSources, SharedPref + RC cache                                                          |
| `:presentation`  | xml only | Screens, MVI, nav graphs, MainActivity host UI. **Omit when compose**                                                                 |
| `:core-design`   | compose only | `Color.kt` / `Type.kt` / `AppTheme`                                                                 |
| `:feature-entrance` | compose only | Entrance start destination (`ENTRANCE_ROUTE`) |
| `:core-common`   | Required | `Constants` (TAGs), `EventsProvider`                                                                          |
| `:core-ui`       | Required | **All** themes/strings/colors/splash, Parent*, extensions                                                     |
| `:core-platform` | Required | `InternetManager`, `PlatformFirebase`, dispatchers DI, **Firebase BOM + analytics / crashlytics / messaging** |

```
app (Composition Root) — no values resources
 |
 ↓
xml: presentation → domain ← data
compose: feature-* → domain ← data   (:core-design under cores)
 |
 ↓
core-common / core-ui / core-platform  (+ :core-design when compose)
```

## Step 1 — Gradle

Follow `08-gradle.md` + [reference/gradle.md](../../rules/reference/gradle.md) (canonical `:app` / library scripts) and **`hsc-gradle-organize`** for catalog + dependency sections.

1. `settings.gradle.kts` — `include` all modules above
2. Root plugins `apply false` via catalog; **latest stable AGP 9.3+**. Do **not** apply `org.jetbrains.kotlin.android` — AGP has built-in Kotlin. `compileSdk { version = release(37) { minorApiLevel = 1 } }`; `targetSdk = 37`; `compileOptions` `VERSION_21`; first build `versionName = "1.0.1"`.
3. Catalog sections/naming per `08-gradle.md` / `hsc-gradle-organize`
4. Dependency graph: UI modules (`:presentation` or `:feature-*`) **never** → `:data`; `domain` → coroutines only
5. **xml:** View Binding on UI modules; Safe Args on `:presentation`. **compose:** Compose Compiler plugin (`kotlin-compose`) + `buildFeatures { compose = true }` on `:app`, `:core-design`, `:feature-*` — **not** `kotlin-android`; Compose BOM + Navigation Compose + Coil 3 + `koin-androidx-compose` in catalog (latest stable). No View Binding on feature modules.
6. **Remove** `:app` `src/main/res/values/` (and night) — move themes/strings/colors/themes into `:core-ui`
7. `:app` may keep only `mipmap` / `xml` backup rules if needed — **no** `strings.xml` / `themes.xml` / `colors.xml` at app level
8. **Every module** gets a `.gitignore`: libraries → `/build`; `:app` → `/build` + `/release` (see `02-project-structure`)
9. **Firebase (mandatory):** catalog BOM + analytics / crashlytics / **messaging** on **`:core-platform`**; **config** + `kotlinx-coroutines-play-services` on **`:data`**. Latest stable. **No** `FirebaseMessagingService` / FCM manifest / token UI during setup (see **`hsc-implement-firebase-messaging`**)

Organize dependency sections with **`hsc-gradle-organize`**.

### Module scripts (scaffold from reference)

Copy the `:app` and library shapes from [reference/gradle.md](../../rules/reference/gradle.md) — module order `plugins` → `android` → (`base` on `:app`) → `dependencies`.

**`:app` must include**

| Item                    | Rule                                                                                                                     |
|-------------------------|--------------------------------------------------------------------------------------------------------------------------|
| `android` section order | `defaultConfig` → `signingConfigs` → `buildTypes` → `buildFeatures` → `compileOptions` → `bundle` |
| `signingConfigs`        | Always — search `*.jks` in root then `app/`; set `storeFile` if found; else empty strings; do not invent passwords       |
| `bundle`                | Always `language { enableSplit = false }`                                                                                |
| `base.archivesName`     | `AppName-Account-v{versionCode}({versionName})` from `project-settings.json` `appName` + account when known              |
| First versions          | `versionCode = 1`, `versionName = "1.0.1"`                                                                               |
| R8                      | `:app` release `optimization { enable = true }` (code **and** resource shrinking). Keep rules in `src/main/keepRules/*.keep` on `:app` and on library modules that need them. Copy [templates/keepRules/](templates/keepRules/). **No** `proguardFiles` / `isMinifyEnabled` / `isShrinkResources` |

**Library modules** (`:presentation` xml, `:feature-*` compose, `:data`, `:domain`, `:core-*`): same relative order; **omit** `signingConfigs`, `bundle`, `base`, and app-only `defaultConfig` fields. xml UI modules get View Binding; compose UI modules get `compose = true`; `:domain` / `:core-common` may omit `buildFeatures`.

## Step 2 — Application + DI

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            lazyModules(KoinModules().getKoinModules())
        }
        // Theme / DynamicColors after Koin — never use GlobalContext.getOrNull()
        applyAppTheme()
    }
}
```

- Aggregate with **`lazyModule` only** (convert any `module` → `lazyModule`, `modules` → `lazyModules`): `appModule`, `coreModule`, `corePlatformModule`, `dataModule`, `useCaseModule`, `entrancePresentationModule` (xml) or `entranceFeatureModule` (compose), …
- Theme: apply **after** `startKoin` in Application; Activity DynamicColors needs no GlobalContext gate (`07-dependency-injection`, `23-app-startup`)
- Manifest: `android:name=".App"`, `android:theme="@style/Theme.App.Starting"`, `supportsRtl="true"`
- Orientation: follow `project-settings.json` — default portrait **and** landscape; do not lock unless `orientation` is single-mode and product requires lock
- Theme modes: create `values` / `values-night` per `themeModes`
- UseCases + repo interfaces → `:domain`; DataSources + repo impls → `:data` (`dataModule` with `//// DataSources` / `//// Repositories`)

## Step 3 — MainActivity + host (xml)

- `MainActivity` extends `ParentActivity` in `:presentation`
- `activity_main.xml` with `fcvContainerMain` + `NavHostFragment` + `@navigation/nav_graph`
- Call `installSplashTheme()` in `onPreCreated()` when using splash

**compose:** skip this step. Copy [templates/compose/MainActivity.kt](templates/compose/MainActivity.kt) into `:app` `ui/`. `ComponentActivity` + `setContent { AppTheme { NavGraph() } }`. **Never** `GlobalContext.get()`.

## Step 4 — Navigation (mandatory Entrance)

**xml:** `nav_graph.xml` **must** use `app:startDestination="@id/entranceFragment"`. Class: `EntranceFragment` under `presentation/entrance/ui/`. Layout: `fragment_entrance.xml`. No Home/Splash/Main as start destination. Copy anims from [templates/anim/](templates/anim/) + [templates/anim-ldrtl/](templates/anim-ldrtl/) into `:core-ui`. Every forward `<action>` must include the four slide anim attrs (`17-navigation`).

**compose:** copy [templates/compose/NavGraph.kt](templates/compose/NavGraph.kt) to `:app` `navigation/NavGraph.kt`. `startDestination = ENTRANCE_ROUTE`. Slide `enterTransition` / `exitTransition` on the `NavHost`. No XML `nav_*.xml`. Feature screens do not receive `NavController`.

### Nav transition anims (xml — mandatory)

Create in **`:core-ui`** (copy from [templates/anim/](templates/anim/) + [templates/anim-ldrtl/](templates/anim-ldrtl/)). Skip this block when `uiFramework` is `compose`.

```
res/anim/slide_in_right.xml | slide_out_left.xml | slide_in_left.xml | slide_out_right.xml
res/anim-ldrtl/… (same four names — RTL mirrors)
```

Every forward `<action>` must include:

```xml
app:enterAnim="@anim/slide_in_right"app:exitAnim="@anim/slide_out_left"app:popEnterAnim="@anim/slide_in_left"app:popExitAnim="@anim/slide_out_right"
```

See `17-navigation.md`.

## Step 5 — Entrance MVI

**xml:** scaffold `presentation/entrance/{di,intent,state,effect,viewModel,ui}` via **`hsc-create-mvi`**. `EntranceFragment` extends `ParentFragment`. Register `entrancePresentationModule`.

**compose:** module `:feature-entrance`. Copy [templates/compose/EntranceScreen.kt](templates/compose/EntranceScreen.kt) + [templates/compose/feature-module.gradle.kts](templates/compose/feature-module.gradle.kts). Then **`hsc-create-mvi`** Compose path (Intent/State/Effect/ViewModel/`entranceFeatureModule`). Wire `navigateToNext` in `NavGraph` when the first real destination exists.

- Strings only in `:core-ui`
- Tests: only if `writeTestsWithFeatures` is `true`

## Step 6 — `:core-ui` Parent* bases (xml) / `:core-design` (compose)

**compose:** skip ParentFragment / View Binding templates. Copy [templates/compose/Color.kt](templates/compose/Color.kt), [Type.kt](templates/compose/Type.kt), [Theme.kt](templates/compose/Theme.kt) into `:core-design`. Keep `:core-ui` for `strings.xml` / `colors.xml` / splash XML theme / drawables (`09`). Add Compose BOM deps on `:core-design`. If design-system **a**, run **`hsc-setup-design-system`** (writes XML tokens **and** maps them into `AppTheme`). Then continue Step 7.

**xml:** mirror reference hierarchy below.

```
core/ui/base/
  activity/ParentActivity.kt
  fragment/ParentFragment.kt
  dialog/ParentDialogDismissal.kt + ParentDialog.kt
  sheet/ParentSheetDismissal.kt + ParentSheet.kt
core-ui …/extensions/
  FragmentExtensions.kt   # viewLifecycleOwner collectWhen* / launchWhen* + navigateTo / popFrom
  ActivityExtensions.kt   # Activity collectWhen* / launchWhen*
  ContextExtensions.kt    # showToast(String) / showToast(@StringRes)
  ImageViewExtensions.kt  # loadImage via Glide (ShapeableImageView / ImageView)

presentation …/base/
  activity/BaseActivity.kt
  fragment/BasePermissionFragment.kt + BaseFragment.kt
  sheets/BaseDialog.kt + BaseSheet.kt
```

See [templates/base/README.md](templates/base/README.md) for hierarchy and notes.

### ParentFragment

- Generic `ViewBinding` + `bindingFactory`
- Clear `_binding` in `onDestroyView`
- Hooks: `initObservers()`, `onViewCreated()`, abstract `onViewCreated()`

### ParentActivity

- Generic `ViewBinding` + edge-to-edge + window insets padding flags
- `installSplashTheme()` → `installSplashScreen()`
- Abstract `onCreated()`; optional `onPreCreated()` / `initObservers()`

### ParentDialog (+ Dismissal)

- `ParentDialogDismissal` : `DialogFragment` with `onDismissCallback` + `safeShow` / `safeDismiss` helpers
- `ParentDialog` : ViewBinding via `MaterialAlertDialogBuilder.setView(binding.root)`
- Null-safe binding; clear in `onDestroyView`
- Improvements vs fragile patterns: never access binding after destroy; use `dismissAllowingStateLoss` only via `safeDismiss`

### ParentSheet (+ Dismissal)

- `ParentSheetDismissal` : `BottomSheetDialogFragment` with `dismissCallback` + `safeShow` / `safeDismiss`
- `ParentSheet` : inflate with `bindingFactory`, null-safe `_binding` (same as Fragment — **not** `!!`), `onSheetCreated()`, `initObservers()`
- Improvements: remove unused dialog imports; optional `skipCollapsed` / expanded state in `onStart` when product needs it; keep Binding lifecycle identical to Fragment

Also add: Fragment/Activity/Context/ImageView extensions (`FragmentExtensions` / `ActivityExtensions` / `ContextExtensions.showToast` / `ImageViewExtensions.loadImage` via Glide; Fragment uses `viewLifecycleOwner`), `themes.xml` (include `ButtonStyle.IconButton` parent of `Widget.Material3.Button.IconButton`), **`splash.xml`**, `strings.xml` / `colors.xml` with **app → general → screen-wise** sections (`09-resources-xml`). Add Glide to version catalog + `implementation(libs.glide)` on `:core-ui`.

If the user chose design-system **a**, **stop here and run `hsc-setup-design-system`** with `figmaDesignSystemUrl` before Step 7. That replaces default Material3 colors/type with Figma tokens and sets `android:windowBackground` — do **not** paint default layouts with `?attr/colorSurface`. If **b**, keep the Step 6 default theme and continue.

## Step 7 — `:core-platform`

### Firebase (catalog + `:core-platform` / `:data`)

Follow **`hsc-implement-firebase-messaging`** for FCM. Every new project **must** include Messaging; Analytics + Crashlytics + Remote Config match Speak-Translate.

```toml
# Firebase
firebaseBom = "…"   # latest stable
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics" }
firebase-crashlytics = { group = "com.google.firebase", name = "firebase-crashlytics" }
firebase-config = { group = "com.google.firebase", name = "firebase-config" }
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging" }

# Kotlin Coroutines
kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutines" }
```

```kotlin
// Firebase — :core-platform
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.analytics)
implementation(libs.firebase.crashlytics)
implementation(libs.firebase.messaging)

// Kotlin Coroutines — :core-platform and :data (Task.await)
implementation(libs.kotlinx.coroutines.android)
implementation(libs.kotlinx.coroutines.play.services)

// Firebase — :data
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.config)
```

On **`:core-platform`**: analytics, crashlytics, messaging. On **`:data`**: config + `kotlinx-coroutines-play-services`. Do **not** add `FirebaseMessagingService`, FCM manifest entries, or token handling during setup.

### Dispatchers (no named qualifiers)

```kotlin
val corePlatformModule = lazyModule {

    //// Dispatchers
    single { Dispatchers.IO }
    single { Dispatchers.Default }

    //// Managers
    single { InternetManager(androidContext()) }
}
```

Repository injects by type:

```kotlin
class XRepositoryImpl(
    private val dataSource: XDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
)
```

Koin resolves `Dispatchers.IO` / `Dispatchers.Default` as distinct `CoroutineDispatcher` instances — **do not** use `named("io")` / `named("default")`.

> If both are type `CoroutineDispatcher`, prefer constructor defaults `= Dispatchers.IO` in repos (reference style) **or** inject only IO via DI and use `Dispatchers.Default` explicitly for CPU work. Do **not** introduce named qualifiers.

### PlatformFirebase — `object`, no Context field

Copy [templates/firebase/PlatformFirebase.kt](templates/firebase/PlatformFirebase.kt) → `:core-platform` `firebase/PlatformFirebase.kt`. Replace `YOUR.PACKAGE`.

```kotlin
object PlatformFirebase {

    fun Throwable.recordException(log: String) {
        Log.e(TAG_FIREBASE, "PlatformFirebase: recordException: Failed: $log")
        FirebaseCrashlytics.getInstance().log(log)
        FirebaseCrashlytics.getInstance().recordException(this)
    }

    fun String.postFirebaseEvent() {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, this@postFirebaseEvent)
        }
        Firebase.analytics.logEvent(this, bundle)
        Log.d(TAG_FIREBASE, "PlatformFirebase: postFirebaseEvent: Success: event=$this")
    }

    fun getDeviceToken() {
        FirebaseInstallations.getInstance().getToken(false)
            .addOnCompleteListener { task ->
                when {
                    task.isSuccessful && task.result != null ->
                        Log.d(TAG_FIREBASE, "PlatformFirebase: getDeviceToken: Success")
                    else ->
                        Log.e(TAG_FIREBASE, "PlatformFirebase: getDeviceToken: Failed")
                }
            }
    }
}
```

- **No** `Context` field / constructor on the object
- Event name constants in `:core-common` `EventsProvider`
- Do **not** log the Installation token value (`14-security-secrets`)
- Ads revenue (`fun Float.logRevenueEvent(context: Context, threshold: Float = 0.1f)`): add **only** when the app has ads. Pass `Context` as an argument. Use this app's prefs name / cache key / event string — never copy Speak-Translate `rossPref` / `TaichiTroasCache`

### InternetManager

- Connectivity check used by RC / network repos

## Step 8 — SharedPreferences + Remote Config cache

### SharedPref (`data/sharedPreferences/`)

- `SharedPrefManager(context)` — **sync only, no dispatcher** (see `.claude/rules/reference/shared-preferences.md` + `26-data-persistence.md`)
- Domain `SharedPrefRepository` + `SharedPrefRepositoryImpl` with `withContext(ioDispatcher)`
- Include RC cache properties (ints/bools/strings) written by RC repository

### Remote Config (cache-to-prefs architecture)

```
domain: RemoteConfigRepository { suspend fun fetchAndCache(): Boolean }
data:
  remoteConfig/dataSource/RemoteConfigDataSource.kt   # Firebase RC only
  remoteConfig/repository/RemoteConfigRepositoryImpl.kt
```

**RemoteConfigDataSource** (no dispatcher). Copy [templates/remoteconfig/RemoteConfigDataSource.kt](templates/remoteconfig/RemoteConfigDataSource.kt) (same shape as `hsc-implement-firebase-remote-config` templates). Replace `YOUR.PACKAGE`.

- `private val fetchMutex = Mutex()` + `by lazy { FirebaseRemoteConfig.getInstance() }`
- `minimumFetchIntervalInSeconds(0L)` always
- `setConfigSettingsAsync(settings).await()` then `fetchAndActivate().await()` (needs `kotlinx-coroutines-play-services`)
- Live listener method name: **`addConfigUpdateListener(onUpdated: () -> Unit)`** — not `addLiveUpdateListener`
- Getters: `getLong(key, default)`, `getInt(key)` (default `0`), `getBoolean(key, default)`, `getString(key, default)` via `runCatching`
- Log with `TAG_REMOTE_CONFIG` (`Success: activated=$activated`)

**RemoteConfigRepositoryImpl** (dispatcher here; `listenerRegistered` lives here, not in the DataSource):

1. Check `InternetManager` — if offline, return `false` (keep last prefs cache)
2. `remoteConfigDataSource.fetchAndActivate()`
3. If **activated**: **`saveValues()`** (copy every needed RC key into `SharedPrefManager`) then `addConfigUpdateListener { saveValues() }` once
4. If fetch fails, prefs still hold last cache — app reads cache via `SharedPrefRepository`

**Read path for features:** Prefer **cached prefs** (`SharedPrefRepository` / managers) for flags used at runtime — not live RC SDK in UI.

**DI (`lazyModule` — sectioned by concern):**

```kotlin
val dataModule = lazyModule {

    //// DataSources
    single { RemoteConfigDataSource() }
    single { SharedPrefManager(androidContext()) }

    //// Repositories
    single<SharedPrefRepository> { SharedPrefRepositoryImpl(get()) }
    single<RemoteConfigRepository> { RemoteConfigRepositoryImpl(get(), get(), get()) }
}

val useCaseModule = lazyModule {

    //// RemoteConfig
    factory { FetchRemoteConfigUseCase(get()) }
}
```

- Interfaces + `FetchRemoteConfigUseCase` in **`:domain`**
- Register both modules in `KoinModules`

Wire `FetchRemoteConfigUseCase` and call early from Entrance / App startup flow (non-blocking UX).

## Step 9 — Verify

- [ ] `.claude/project-settings.json` written and valid (incl. `uiFramework` + `figmaDesignSystemUrl` if option **a`)
- [ ] Every module has `.gitignore` (`/build`; `:app` also `/release`)
- [ ] No `:app/src/main/res/values/` (themes/strings/colors live in `:core-ui`)
- [ ] **xml** modules: app, domain, data, presentation, core-common, core-ui, core-platform
- [ ] **compose** modules: app, domain, data, core-design, feature-entrance, core-common, core-ui, core-platform — **no** `:presentation`
- [ ] `:app` `android` section order: defaultConfig → signingConfigs → buildTypes → buildFeatures → compileOptions → bundle
- [ ] `:app` has `signingConfigs` (`.jks` path if found, else empty strings) + `bundle.language.enableSplit = false` + `base.archivesName`
- [ ] `:app` release `optimization { enable = true }`; `src/main/keepRules/rules.keep` on `:app` and on `:domain` / UI modules; no `proguard-rules.pro`
- [ ] `compileSdk` 37.1 block, `targetSdk = 37`, `versionName = "1.0.1"`, `compileOptions` `VERSION_21`
- [ ] Library modules omit `signingConfigs` / `bundle` / `base`
- [ ] UseCases + repo interfaces only in `:domain`; `dataModule` has `//// DataSources` then `//// Repositories`
- [ ] All DI uses `lazyModule` / `lazyModules` only; theme applied after `startKoin` (no `GlobalContext` probes)
- [ ] **xml:** `nav_graph` startDestination = `entranceFragment`; **compose:** `NavGraph.kt` `startDestination = ENTRANCE_ROUTE`
- [ ] **xml:** `:core-ui` has `anim/` + `anim-ldrtl/` slide_* set; nav actions use the four anim attrs. **compose:** slide `enterTransition` / `exitTransition` on `NavHost`
- [ ] **xml:** ParentActivity / ParentFragment / ParentDialog / ParentSheet (+ Dismissal) exist
- [ ] **xml:** `FragmentExtensions.kt` + `ActivityExtensions.kt` + `ContextExtensions.kt` + `ImageViewExtensions.kt` (`showToast` / `loadImage`; Fragment collectors on `viewLifecycleOwner`; `navigateTo` / `popFrom`)
- [ ] **xml:** Glide on `:core-ui` (+ presentation if needed); all programmatic image binds use `loadImage`. **compose:** Coil 3; no Glide in feature modules
- [ ] **compose:** `:core-design` `AppTheme`; `:app` `MainActivity` uses `koinInject()` — never `GlobalContext.get()`
- [ ] Firebase BOM + analytics/crashlytics/messaging on `:core-platform`; `firebase-config` on `:data` (no MessagingService)
- [ ] `kotlinx-coroutines-play-services` on `:core-platform` and `:data`
- [ ] `PlatformFirebase` is `object` without a Context field; poster uses `Param.ITEM_NAME` + `Firebase.analytics`
- [ ] `getDeviceToken` logs Success/Failed without the token value
- [ ] Dispatchers registered **without** `named("io")` / `named("default")`
- [ ] RC DataSource: Mutex, lazy instance, `await()`, `addConfigUpdateListener`, getters with defaults
- [ ] RC `minimumFetchIntervalInSeconds(0)` + cache write to `SharedPrefManager` only when activate succeeds
- [ ] UI modules (`:presentation` or `:feature-*`) ↛ `:data`
- [ ] `assembleDebug` succeeds; orientation / theme modes match `project-settings.json`
- [ ] Design system: option **a** ran `hsc-setup-design-system` (theme `windowBackground`, no default layout `colorSurface`); or **b** left default Material3 theme

## Do not

- Use on an existing production app — use `hsc-setup-old-project`
- Data Binding / Hilt / Compose when `uiFramework` is `xml`
- XML Fragments / View Binding feature UI when `uiFramework` is `compose`
- `GlobalContext.get()` in Compose `MainActivity`
- App-level `values` resources
- Named dispatcher qualifiers
- `PlatformFirebase` holding Context (ads revenue may take `Context` as a parameter only)
- `addLiveUpdateListener` — method is `addConfigUpdateListener`
- Logging Installation / FCM tokens
- Reading RC only from SDK in Fragments (use prefs cache)
- Hardcode secrets / lock orientation unless product requires
- Skip writing `project-settings.json`
- Add `FirebaseMessagingService` / FCM manifest / push UI during setup (dependency only)

## After setup

Next: if design system was skipped, optional `/hsc-setup-design-system`; then language / onboarding / home via **`hsc-figma-to-xml`** (xml) or **`hsc-figma-to-compose`** (compose) → `hsc-create-mvi`; new domain/data via `hsc-create-clean-architecture`; wire Entrance Effects in `nav_graph.xml` (xml) or `NavGraph.kt` (compose). Existing apps missing RC or Analytics: `hsc-implement-firebase-remote-config`, `hsc-implement-firebase-events`; later screens: `hsc-add-firebase-events`; extra RC keys: `hsc-add-firebase-remote-config`.
