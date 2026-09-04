---
name: hs-add-admob-banner
description: Hypersoft Cursor Skill: Add an AdMob banner placement to an existing :gmaAds app (AdKey, AdConfig, ad_ids, RC, BannerAdView, load/pause/resume/clear/destroy). Use when adding banner ads, or /hs-add-admob-banner. If ads were never wired, use hs-implement-admob-ads first.
---

# Add AdMob Banner

Follow `.cursor/rules/21-ads-billing.mdc` and [reference/ads-gma.md](../../rules/reference/ads-gma.md) (Banner section).

Obey `.cursor/project-settings.json` (`uiFramework`, `applicationId`).

If `:gmaAds` is missing or screens were never wired → redirect to **`hs-implement-admob-ads`**.

Do **not** edit `BannerAds`, `BannerAdController`, or `BannerAdValidator` unless changing engine behavior.

---

## Steps

1. Ask: placement name, host screen, slot (`TOP` / `BOTTOM` / `MREC` / `INLINE`), `cache`, fallback flags, `maxHeightDp` if INLINE.
2. Add `BannerAdKey` enum value.
3. Add matching row in `BannerAdConfig.placements` (format from RC for TOP/BOTTOM).
4. Add unit string in `ad_ids.xml`.
5. Add RC in `:data`. TOP/BOTTOM: `0` off, `1` adaptive, `2` collapsible. MREC/INLINE: `0` off, `!= 0` on.
6. UI:
    - **xml:** `BannerAdView` — width `0dp` or `wrap_content`, height `wrap_content` (do not hardcode 300×250 / inline max height)
    - **compose:** `AndroidView` with `BannerAdView`
7. Screen lifecycle:

```kotlin
loadBannerAd(BannerAdKey.YOURS, bannerAdView) // load+show only — no split show
resumeBannerAd(BannerAdKey.YOURS)             // onResume
pauseBannerAd(BannerAdKey.YOURS)              // onPause
bannerAdView.clearView()                      // onDestroyView — detach; keep if cache=true
destroyBannerAd(BannerAdKey.YOURS)            // onDestroy when leaving for good
```

8. No Entrance preload pattern. No `AdsManager` injection.

## Remove

Delete calls + view → config + key → `ad_ids` → RC → search leftovers.
