# :gmaAds — AdMob playbook

Source of truth for module behavior: [hypersoftdev/Admob-Ads](https://github.com/hypersoftdev/Admob-Ads) `gmaAds/README.md`. Local learn path: `E:\SohaibAhmed\Github\Admob-Ads`.

Skills: `hsc-implement-admob-ads`, `hsc-add-admob-*`. Rule stub: `21-ads-billing`.

## Architecture

Screens **never** talk to the AdMob SDK or inject `AdsManager`. They call Fragment / Activity extensions with a placement key.

Each format is a pipeline: **config → validation → (interstitial counter) → controller**.

| Piece                     | Role                                                                                           |
|---------------------------|------------------------------------------------------------------------------------------------|
| `*AdConfig.kt` + `*AdKey` | Catalog: ad unit, RC on/off, fallback, cache, navigate-on, banner slot/format                  |
| Validator                 | Blocks premium, RC-off, no internet, bad ad unit, unsafe activity                              |
| Controller                | Load inventory, fallback, show, cache, destroy                                                 |
| `AdsManager`              | Facade: `appOpen` / `banner` / `interstitial` / `native` / `rewarded` / `rewardedInterstitial` |
| Extensions                | What `:app` / presentation / feature screens call                                              |

Consent runs on Entrance (`ConsentManager`). SDK init is inside each `load`. Fullscreen formats share `FullscreenAdGate`.

Catalog / extensions / `ad_ids.xml` / Koin order: **App Open → Banner → Interstitial → Native → Rewarded → Rewarded Interstitial**.

## Placing `:gmaAds` into an app

1. Download the `gmaAds` module from GitHub (`hypersoftdev/Admob-Ads`) — do **not** invent a parallel module.
2. Copy into the project exactly (engine + catalog as shipped).
3. Rename package / namespace only: `{applicationId}.gmaAds`.
4. Remap Gradle deps and **host-only** imports so it compiles against AndroidAI modules:
    - Ref `:core` → `:core-common` + `:core-platform` (as appropriate)
    - Ref `:data` → `:data`
    - Rewrite imports for `Constants.TAG_ADS`, `InternetManager`, `SharedPrefManager`, `launchWhenResumed`, `onBackPressedDispatcher` to the target app packages
5. Do **not** edit controllers, validators, `AdsSdk`, `FullscreenAdGate`, `ConsentManager`, or catalog during place.
6. Later placement work (`add-admob-*`) may edit catalog files (`*AdKey`, `*AdConfig`, `ad_ids.xml`) + `:data` RC — still leave the engine alone unless changing engine behavior on purpose.

## Call shapes

| Format                | Load                                                                          | Show                                                             | Destroy / lifecycle                                                                                                    |
|-----------------------|-------------------------------------------------------------------------------|------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| App Open              | `loadAppOpenAd(key)`                                                          | `showAppOpenAd(key)` or `showAppOpenOrInterstitialAd(ao, inter)` | `blockAppOpen` / `unblockAppOpen`                                                                                      |
| Banner                | `loadBannerAd(key, container)` only (load+show)                               | —                                                                | `pause` / `resume`; `container.clearView()` in `onDestroyView`; `destroyBannerAd` in `onDestroy` when leaving for good |
| Interstitial          | `loadInterstitialAd(key)`                                                     | `showInterstitialAd(key) { continue }`                           | none                                                                                                                   |
| Native                | `loadNativeAd(key)` preload **or** `loadNativeAd(key, container)` same screen | `showNativeAd(key, container)` after preload                     | `destroyNativeAd` when the screen is gone and `cache = false`                                                          |
| Rewarded              | `loadRewardedAd(key)`                                                         | `showRewardedAd(key) { granted -> }`                             | none                                                                                                                   |
| Rewarded interstitial | `loadRewardedInterstitialAd(key)`                                             | `showRewardedInterstitialAd(key) { granted -> }`                 | none                                                                                                                   |

Do not inject `AdsManager` in Fragments / Compose screens.

**Compose:** same keys and timing; call Activity / Fragment extensions from the host (or thin wrappers). No raw AdMob SDK in composables.

## Add / remove a placement

### Shared steps (all formats)

**Add**

1. Enum value on `*AdKey` (string id is log-only).
2. Matching row in that format’s `*AdConfig` `placements` map.
3. AdMob unit in `src/main/res/values/ad_ids.xml`.
4. Remote Config in `:data`:
    - Key constant + `rc*` property on `SharedPrefManager`
    - Default in `RemoteConfigDataSource.DEFAULTS`
    - Copy into prefs in `RemoteConfigRepositoryImpl`
    - Same key in Firebase Remote Config
5. `isEnabled = { it.rcYourFlag != 0 }` on the placement (banner TOP/BOTTOM also uses the same RC int for format: `1` adaptive, `2` collapsible).
6. `load` / `show` on the screen.

**Remove**

1. Delete load/show (and container) from the screen.
2. Delete the config row and enum value.
3. Delete the `ad_ids.xml` string.
4. Delete the RC key from `:data` and Firebase.
5. Search the key name and confirm nothing still references it.

RC `0` turns a placement off without deleting it.

---

### App Open

Files: `appOpen/AppOpenAdConfig.kt`, `AppOpenAdKey`

| Key         | When                                                                        |
|-------------|-----------------------------------------------------------------------------|
| `ENTRANCE`  | Load on Entrance with interstitial. Show via `showAppOpenOrInterstitialAd`. |
| `LIFECYCLE` | Process foreground.                                                         |

`AppOpenAdConfig.LOAD_LIFECYCLE_WITH_LOADING_SCREEN`:

- **true** — `AppOpenLoadingActivity` loads then shows `LIFECYCLE`. Do not preload or reload after dismiss.
- **false** — load `LIFECYCLE` on Dashboard; `AppOpenLifecycle` shows it and reloads after dismiss.

Entrance calls `blockAppOpen()`. Keep blocked through splash. Dashboard calls `unblockAppOpen()`. Skills: `hsc-add-admob-appOpen-Entrance`, `hsc-add-admob-appOpen-lifecycle`.

---

### Banner

Files: `banner/BannerAdConfig.kt`, `BannerAdKey`

| Slot             | Format                         | RC                                     |
|------------------|--------------------------------|----------------------------------------|
| `TOP` / `BOTTOM` | adaptive or collapsible        | `0` off, `1` adaptive, `2` collapsible |
| `MREC`           | 300×250                        | `0` off, `!= 0` on                     |
| `INLINE`         | inline adaptive, `maxHeightDp` | `0` off, `!= 0` on                     |

Fallback never crosses slots. `cache = true` keeps the `AdView`; `cache = false` consumes on show.

XML: `BannerAdView`, width `0dp` or `wrap_content`, height `wrap_content`. No hardcoded 300×250 or inline max height.

```kotlin
loadBannerAd(BannerAdKey.YOURS, binding.bannerAdViewYours)
resumeBannerAd(BannerAdKey.YOURS) // onResume
pauseBannerAd(BannerAdKey.YOURS)  // onPause
binding.bannerAdViewYours.clearView() // onDestroyView
destroyBannerAd(BannerAdKey.YOURS)    // onDestroy when leaving for good
```

No Entrance preload. No split load then show. Skill: `add-admob-banner`.

---

### Interstitial

Files: `interstitial/InterstitialAdConfig.kt`, `InterstitialAdKey`

`navigateOn`: `IMPRESSION` continues when shown; `DISMISS` waits until close.

Frequency cap (`loadOnStart` not null): RC counter `n` means load on the n-th eligible call.

- `loadOnStart = true`, n = 5 → 5 (load), 1, 2, 3, 4 (load)…
- `loadOnStart = false`, n = 5 → 1, 2, 3, 4 (load)…
- `loadOnStart = null` → load every call

```kotlin
loadInterstitialAd(InterstitialAdKey.YOURS)
showInterstitialAd(InterstitialAdKey.YOURS) { /* navigate */ }
```

Skill: `add-admob-interstitial`.

---

### Native

Files: `nativeAd/NativeAdConfig.kt`, `NativeAdKey`

Containers: `NativeSmallView`, `NativeLargeView`, `NativeLargeSecondView`.

```kotlin
// Preload on A, show on B
loadNativeAd(NativeAdKey.LANGUAGE)
showNativeAd(NativeAdKey.LANGUAGE, binding.native)

// Same screen
loadNativeAd(NativeAdKey.HOME, binding.nativeAdViewHome)

destroyNativeAd(NativeAdKey.FEATURE_ONE) // cache = false, screen finished
```

Skill: `add-admob-native`.

---

### Rewarded / Rewarded interstitial

```kotlin
loadRewardedAd(RewardedAdKey.HOME)
showRewardedAd(RewardedAdKey.HOME) { granted -> /* only if true */ }

loadRewardedInterstitialAd(RewardedInterstitialAdKey.HOME)
showRewardedInterstitialAd(RewardedInterstitialAdKey.HOME) { granted -> }
```

Always check `granted`. Skills: `hsc-add-admob-rewarded`, `hsc-add-admob-rewarded-interstitial`.

---

## Fallback rules

- `canUseAvailableFallback` — may take another placement’s unused ad.
- `canBeUsedAsFallback` — others may take this placement’s unused ad.
- Native/banner: no fallback after impression.
- Banner: same `BannerSlot` only.

## Remote Config cheat sheet

| Format                                                          | Off | On                            |
|-----------------------------------------------------------------|-----|-------------------------------|
| App Open, interstitial, native, rewarded, rewarded interstitial | `0` | `!= 0`                        |
| Banner TOP/BOTTOM                                               | `0` | `1` adaptive, `2` collapsible |
| Banner MREC/INLINE                                              | `0` | `!= 0`                        |
| Interstitial counter                                            | —   | integer `n` for n-1 cap       |

RC is fetched into `SharedPrefManager`. Placements read prefs at load time, not Firebase live.

## Ref app screen map (implement strategy)

Wire **only screens that already exist** in the target app. Skip missing ones; do not create funnel screens just for ads.

| Screen              | Typical ads                                                                                                      |
|---------------------|------------------------------------------------------------------------------------------------------------------|
| Entrance            | Consent → load AO `ENTRANCE` + inter `ENTRANCE` + preload natives; `showAppOpenOrInterstitialAd`; `blockAppOpen` |
| Language            | Banner `LANGUAGE`; show native `LANGUAGE`                                                                        |
| Onboarding          | Banner + native + inter `ON_BOARDING`                                                                            |
| Menu                | Native `MENU`                                                                                                    |
| Dashboard           | Banner `DASHBOARD`; `unblockAppOpen` + load AO `LIFECYCLE`; inter `BOTTOM_NAVIGATION` / `EXIT`                   |
| Home                | Native + inter + rewarded + rewarded inter `HOME`                                                                |
| Trending / Settings | Native `TRENDING` / `SETTING`                                                                                    |
| Feature One / Two   | Banner + native + inter `BACK_PRESS`                                                                             |

End every implement pass with a list of changed Fragments / `*Screen`s (and skipped with reason).

## What not to change for a new placement

Leave alone unless changing engine behavior: `*Ads.kt`, `*AdController.kt`, `*AdValidator.kt`, `AdsSdk`, `FullscreenAdGate`, `ConsentManager`. A new **format** (not placement) needs that stack plus Koin in `GmaAdsModule.kt` and `AdsManager`.

## Forbidden

- Converting ads to MVI unless the user explicitly asks
- Raw AdMob SDK calls scattered in every screen
- Injecting `AdsManager` in Fragments / Compose screens
- Production ad unit IDs in debug builds
- Editing engine files when only adding a placement
- Inventing a parallel ads module instead of copying from GitHub
