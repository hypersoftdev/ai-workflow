---
name: hsc-add-admob-appOpen-Entrance
description: Hypersoft Claude Skill: Wire AdMob App Open ENTRANCE placement (consent, blockAppOpen, load with interstitial, showAppOpenOrInterstitialAd). Use for splash/entrance app open, or /hsc-add-admob-appOpen-Entrance. If ads were never wired, use hsc-implement-admob-ads first. For resume/lifecycle app open use hsc-add-admob-appOpen-lifecycle.
---

# Add AdMob App Open — Entrance

Follow `.claude/rules/21-ads-billing.md` and [reference/ads-gma.md](../../rules/reference/ads-gma.md) (App Open section).

Obey `.claude/project-settings.json` (`uiFramework`, `applicationId`).

If `:gmaAds` is missing or Entrance was never wired for ads → prefer **`hsc-implement-admob-ads`**, or continue here only when module + DI already exist.

Do **not** edit `AppOpenAds`, `AppOpenAdController`, `AppOpenAdValidator`, `AppOpenLifecycle`, or `ConsentManager` unless changing engine behavior.

Sibling: `hsc-add-admob-appOpen-lifecycle` for `LIFECYCLE`.

---

## Steps

1. Confirm Entrance screen exists (Fragment or Compose start destination). If missing → stop; do not create Entrance only for ads.
2. Ensure `AppOpenAdKey.ENTRANCE` + catalog row + `ad_ids.xml` + RC (`!= 0` on) exist (shipped with module or add via shared placement steps).
3. On Entrance start: `blockAppOpen()` so process `ON_START` does not steal the splash ad.
4. Run `ConsentManager` (UMP). On ads allowed → load inventory:

```kotlin
loadAppOpenAd(AppOpenAdKey.ENTRANCE) { /* settled */ }
loadInterstitialAd(InterstitialAdKey.ENTRANCE) { /* settled */ }
// optional: preload natives for Language / next screens
```

5. Before navigate to Language / Onboarding / Menu:

```kotlin
showAppOpenOrInterstitialAd(AppOpenAdKey.ENTRANCE, InterstitialAdKey.ENTRANCE) {
    // navigate
}
```

App open shows first; interstitial only if app open did not display. This clears the block when appropriate — keep blocked through splash until this show path.

6. Do **not** call `unblockAppOpen()` on Entrance — Dashboard / main shell does that (lifecycle skill).
7. Compose: same ordering from the host Activity/Fragment. No `AdsManager` injection.

## Remove

Drop entrance load/show; Entrance may `showInterstitialAd` only or navigate with no fullscreen. If removing the key entirely, follow shared remove steps in `ads-gma.md`.
