---
name: hsc-add-admob-rewarded-interstitial
description: Hypersoft Claude Skill: Add an AdMob rewarded interstitial placement to an existing :gmaAds app (AdKey, AdConfig, ad_ids, RC, load/show with granted). Use when adding rewarded interstitial ads, or /hsc-add-admob-rewarded-interstitial. If ads were never wired, use hsc-implement-admob-ads first.
---

# Add AdMob Rewarded Interstitial

Follow `.claude/rules/21-ads-billing.md` and [reference/ads-gma.md](../../rules/reference/ads-gma.md) (Rewarded interstitial section).

Obey `.claude/project-settings.json` (`uiFramework`, `applicationId`).

If `:gmaAds` is missing or screens were never wired → redirect to **`hsc-implement-admob-ads`**.

Do **not** edit `RewardedInterstitialAds`, controller, or validator unless changing engine behavior.

Same checklist as rewarded — different AdMob format and inventory (`RewardedInterstitialAdKey` / `RewardedInterstitialAdConfig`).

---

## Steps

1. Ask: placement name, load screen, show trigger, fallback flags.
2. Add `RewardedInterstitialAdKey` enum value.
3. Add matching row in `RewardedInterstitialAdConfig.placements`.
4. Add unit string in `ad_ids.xml`.
5. Add RC in `:data`. `isEnabled = { it.rcFlag != 0 }`.
6. Calls:

```kotlin
loadRewardedInterstitialAd(RewardedInterstitialAdKey.YOURS)
showRewardedInterstitialAd(RewardedInterstitialAdKey.YOURS) { granted ->
    if (granted) { /* grant reward */ }
}
```

Always check `granted`.

7. Compose: host Activity/Fragment extensions. No `AdsManager` injection.

## Remove

Delete load/show → config + key → `ad_ids` → RC → search leftovers.
