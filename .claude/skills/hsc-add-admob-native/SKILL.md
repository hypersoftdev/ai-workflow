---
name: hsc-add-admob-native
description: Hypersoft Claude Skill: Add an AdMob native placement to an existing :gmaAds app (AdKey, AdConfig, ad_ids, RC, container, load/show/destroy). Use when adding native ads, or /hsc-add-admob-native. If ads were never wired, use hsc-implement-admob-ads first.
---

# Add AdMob Native

Follow `.claude/rules/21-ads-billing.md` and [reference/ads-gma.md](../../rules/reference/ads-gma.md) (Native section).

Obey `.claude/project-settings.json` (`uiFramework`, `applicationId`).

If `:gmaAds` is missing or screens were never wired → redirect to **`hsc-implement-admob-ads`**.

Do **not** edit `NativeAds`, `NativeAdController`, or `NativeAdValidator` unless changing engine behavior.

---

## Steps

1. Ask: placement name, host screen(s), preload-elsewhere vs same-screen, `cache` true/false, fallback flags, which view (`NativeSmallView` / `NativeLargeView` / `NativeLargeSecondView`).
2. Add `NativeAdKey` enum value.
3. Add matching row in `NativeAdConfig.placements`.
4. Add unit string in `ad_ids.xml`.
5. Add RC in `:data` (`rc*` + defaults + repository copy). `isEnabled = { it.rcFlag != 0 }`.
6. UI container:
    - **xml:** add the native view class to the layout
    - **compose:** `AndroidView` hosting the same view class
7. Calls:
    - Preload: `loadNativeAd(key)` on screen A; `showNativeAd(key, container)` on screen B
    - Same screen: `loadNativeAd(key, container)`
    - When `cache = false` and screen is finished: `destroyNativeAd(key)`
8. No `AdsManager` injection; no raw AdMob in composables.

## Remove

Delete load/show/destroy + container → config + key → `ad_ids` → RC → search leftovers.
