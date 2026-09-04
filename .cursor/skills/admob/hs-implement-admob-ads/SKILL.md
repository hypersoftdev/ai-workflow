---
name: hs-implement-admob-ads
description: Hypersoft Cursor Skill: First-time AdMob screen wiring with :gmaAds from hypersoftdev/Admob-Ads — place module if missing, match ref app strategy on existing screens only (Entrance, Language, Onboarding, Menu, Dashboard, etc.). Use when implementing ads for the first time, or /hs-implement-admob-ads. Not for a single new placement — use hs-add-admob-*.
---

# Implement AdMob Ads

Follow `.cursor/rules/21-ads-billing.mdc` and [reference/ads-gma.md](../../rules/reference/ads-gma.md).

Obey `.cursor/project-settings.json` when present (`applicationId`, `uiFramework`).

**Reference:** [hypersoftdev/Admob-Ads](https://github.com/hypersoftdev/Admob-Ads) (learn locally at `E:\SohaibAhmed\Github\Admob-Ads`). Copy `:gmaAds` from GitHub — do not invent a parallel stack. Do **not** convert ads to MVI.

Cross-skills: `hs-add-admob-banner`, `hs-add-admob-interstitial`, `hs-add-admob-native`, `hs-add-admob-rewarded`, `hs-add-admob-rewarded-interstitial`, `hs-add-admob-appOpen-Entrance`, `hs-add-admob-appOpen-lifecycle`. RC keys: `firebase/hs-add-firebase-remote-config` when only adding keys.

---

## Step 0 — Preconditions

1. Read `applicationId` and `uiFramework` from project settings.
2. If billing/premium exists, confirm ads stay gated on `isAppPurchased` (validators already do this via prefs).
3. If the app already has a different ads stack and the user did **not** ask to migrate → stop and ask before replacing.

---

## Step 1 — Place `:gmaAds` (if missing)

1. Download the `gmaAds` folder from `https://github.com/hypersoftdev/Admob-Ads` (clone/sparse or download zip — prefer GitHub over inventing files).
2. Copy into the project as module `:gmaAds`.
3. `include(":gmaAds")` in `settings.gradle.kts`.
4. Rename package / `namespace` only → `{applicationId}.gmaAds`.
5. Remap Gradle: ref `:core` / `:data` → this app’s `:core-common`, `:core-platform`, `:data` as appropriate; add catalog deps (`play-services-ads`, UMP) if missing.
6. Remap **host-only** imports inside `:gmaAds` to this app: `Constants.TAG_ADS`, `InternetManager`, `SharedPrefManager`, `launchWhenResumed`, `onBackPressedDispatcher`.
7. Register `gmaAdsModule` (`lazyModule`) in the composition root.
8. AdMob App ID in `:app` manifest (debug sample / release production). Keep Google sample units in `ad_ids.xml` for debug.
9. During place: **do not** edit controllers, validators, `AdsSdk`, `FullscreenAdGate`, `ConsentManager`, or catalog files.

If `:gmaAds` already exists from setup → skip copy; verify package + DI + App ID.

---

## Step 2 — Remote Config + prefs

Ensure `:data` has the RC keys / `SharedPrefManager` `rc*` properties that the shipped catalog `isEnabled` lambdas read (match ref Admob-Ads `:data`). Defaults in `RemoteConfigDataSource.DEFAULTS`; copy in `RemoteConfigRepositoryImpl`. Add missing keys via the same pattern as `hs-add-firebase-remote-config`.

Do **not** strip catalog keys just because a screen is missing — unused placements stay RC-off (`0`) until needed.

---

## Step 3 — Wire existing screens only

Match the ref `:app` strategy. **Skip** any screen that does not exist. **Do not** create Entrance / Language / Onboarding / Menu / etc. only for ads.

| Screen (or Compose equivalent) | Wire                                                                                                                                                                                             |
|--------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Entrance                       | `blockAppOpen()`; `ConsentManager`; on ads allowed → `loadAppOpenAd(ENTRANCE)` + `loadInterstitialAd(ENTRANCE)` (+ preload natives for next screens); navigate via `showAppOpenOrInterstitialAd` |
| Language                       | `loadBannerAd(LANGUAGE, …)`; `showNativeAd(LANGUAGE, …)` if preloaded                                                                                                                            |
| Onboarding                     | Banner + native + `loadInterstitialAd(ON_BOARDING)`; show inter on continue                                                                                                                      |
| Menu                           | `loadNativeAd(MENU, …)`                                                                                                                                                                          |
| Dashboard                      | `unblockAppOpen()`; `loadAppOpenAd(LIFECYCLE)` when not using loading-screen mode; `loadBannerAd(DASHBOARD, …)`; load inter `BOTTOM_NAVIGATION` / `EXIT` as ref                                  |
| Home                           | Native + inter + rewarded + rewarded interstitial `HOME`                                                                                                                                         |
| Trending / Settings            | Native `TRENDING` / `SETTING`                                                                                                                                                                    |
| Feature One / Two              | Banner + native + inter `BACK_PRESS`                                                                                                                                                             |

**xml:** call Fragment extensions from Fragments (View Binding containers: `BannerAdView`, `Native*View`).

**compose:** same keys and timing; call Activity/Fragment host extensions (or thin wrappers). No raw AdMob SDK in composables. Host banner/native views via `AndroidView` when needed.

Never inject `AdsManager` in screens.

---

## Step 4 — Verify

- Premium users do not load/show (validator).
- No production IDs in debug.
- `gmaAdsModule` registered; App ID present.
- No MVI Intent/State/Effect for ads load/show.

---

## Step 5 — End report (mandatory)

List every Fragment / `*Screen` **changed**, and every ref-mapped screen **skipped** (with reason, e.g. “OnboardingFragment not in project”).

Example:

```
Changed:
- EntranceFragment — consent + entrance AO/inter + native preload
- DashboardFragment — banner + lifecycle AO unblock/load

Skipped:
- LanguageFragment — not present
- OnboardingFragment — not present
```

---

## Forbidden

- Editing `:gmaAds` engine during place
- Creating missing product screens for ads
- Converting ads to MVI
- Inventing a custom ads module instead of GitHub `:gmaAds`
