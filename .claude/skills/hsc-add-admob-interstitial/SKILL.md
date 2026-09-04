---
name: hsc-add-admob-interstitial
description: Hypersoft Claude Skill: Add an AdMob interstitial placement to an existing :gmaAds app (AdKey, AdConfig, ad_ids, RC, load/show). Use when adding interstitial ads, or /hsc-add-admob-interstitial. If ads were never wired, use hsc-implement-admob-ads first.
---

# Add AdMob Interstitial

Follow `.claude/rules/21-ads-billing.md` and [reference/ads-gma.md](../../rules/reference/ads-gma.md) (Interstitial section).

Obey `.claude/project-settings.json` (`uiFramework`, `applicationId`).

If `:gmaAds` is missing or screens were never wired → redirect to **`hsc-implement-admob-ads`**.

Do **not** edit `InterstitialAds`, `InterstitialAdController`, `InterstitialAdValidator`, or `FullscreenAdGate` unless changing engine behavior.

---

## Steps

1. Ask: placement name, host screen, when to load, when to show, `navigateOn` (`IMPRESSION` vs `DISMISS`), optional frequency cap (`loadOnStart` + RC counter).
2. Add `InterstitialAdKey` enum value.
3. Add matching row in `InterstitialAdConfig.placements`.
4. Add unit string in `gmaAds/.../res/values/ad_ids.xml` (Google sample in debug / shipped samples OK).
5. Add RC in `:data`: key + `SharedPrefManager` `rc*` + `DEFAULTS` + `RemoteConfigRepositoryImpl` copy + Firebase console key. `isEnabled = { it.rcFlag != 0 }`. For caps, add counter RC property and `remoteCounter` / `loadOnStart`.
6. On the host screen (Fragment or Compose host):
    - `loadInterstitialAd(InterstitialAdKey.YOURS)` where inventory can wait
    - `showInterstitialAd(InterstitialAdKey.YOURS) { /* continue navigation */ }` on the event
7. Compose: same calls from Activity/Fragment host — no raw AdMob SDK in composables.
8. Confirm: no `AdsManager` injection; premium/RC gates left to validator.

## Remove

Delete load/show → config row + key → `ad_ids` → RC (and counter if any) → search for leftovers.
