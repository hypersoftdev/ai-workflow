---
name: hs-add-admob-rewarded
description: Hypersoft Cursor Skill: Add an AdMob rewarded placement to an existing :gmaAds app (AdKey, AdConfig, ad_ids, RC, load/show with granted). Use when adding rewarded ads, or /hs-add-admob-rewarded. If ads were never wired, use hs-implement-admob-ads first.
---

# Add AdMob Rewarded

Follow `.cursor/rules/21-ads-billing.mdc` and [reference/ads-gma.md](../../rules/reference/ads-gma.md) (Rewarded section).

Obey `.cursor/project-settings.json` (`uiFramework`, `applicationId`).

If `:gmaAds` is missing or screens were never wired → redirect to **`hs-implement-admob-ads`**.

Do **not** edit `RewardedAds`, `RewardedAdController`, or `RewardedAdValidator` unless changing engine behavior.

---

## Steps

1. Ask: placement name, load screen, show trigger (button), fallback flags.
2. Add `RewardedAdKey` enum value.
3. Add matching row in `RewardedAdConfig.placements`.
4. Add unit string in `ad_ids.xml`.
5. Add RC in `:data`. `isEnabled = { it.rcFlag != 0 }`.
6. Calls:

```kotlin
loadRewardedAd(RewardedAdKey.YOURS)
showRewardedAd(RewardedAdKey.YOURS) { granted ->
    if (granted) { /* grant reward */
    }
}
```

Always check `granted` — failed/skipped show reports `false`.

7. Compose: same from host Activity/Fragment. No raw AdMob in composables. No `AdsManager` injection.

## Remove

Delete load/show → config + key → `ad_ids` → RC → search leftovers.
