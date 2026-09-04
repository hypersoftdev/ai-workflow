---
description: AdMob :gmaAds and in-app billing / premium patterns
paths:
  - "**/gmaAds/**"
  - "**/admob*/**"
  - "**/billing/**"
  - "**/premium/**"
  - "**/*.gradle.kts"
---

Apply when the project includes ads and/or IAP. Setup skills **always place** `:gmaAds` from GitHub; screen wiring needs user yes (or `/hsc-implement-admob-ads`).

## Ads are not MVI

- Ads stay on **`:gmaAds`** (facade + controllers + Fragment/Activity extensions) — **not** Intent / State / Effect
- Do **not** apply `hsc-create-mvi` to ads; do not inject `AdsManager` in screens
- Screens call extensions (`loadBannerAd`, `showInterstitialAd`, …) with placement keys
- Convert ads to MVI **only** if the user **explicitly** asks

Full playbook: [reference/ads-gma.md](reference/ads-gma.md)

Skills: `hsc-implement-admob-ads`, `hsc-add-admob-banner`, `hsc-add-admob-interstitial`, `hsc-add-admob-native`, `hsc-add-admob-rewarded`, `hsc-add-admob-rewarded-interstitial`, `hsc-add-admob-appOpen-Entrance`, `hsc-add-admob-appOpen-lifecycle`

## Placing `:gmaAds`

1. Download from [hypersoftdev/Admob-Ads](https://github.com/hypersoftdev/Admob-Ads) — exact module copy
2. Package / namespace only: `{applicationId}.gmaAds`
3. Remap Gradle + host imports (`Constants.TAG_ADS`, `InternetManager`, `SharedPrefManager`, extensions) to this app’s `:core-common` / `:core-platform` / `:data`
4. During place: **do not** edit engine or catalog (controllers, validators, `AdsSdk`, `FullscreenAdGate`, `ConsentManager`, `*AdConfig`)
5. Later placements may edit catalog + `:data` RC — leave engine alone unless changing engine behavior

## Ads usage invariants

- Gate load/show on **premium** + **Remote Config** ints in `SharedPrefManager` (placements read prefs, not live Firebase)
- Debug = Google sample unit IDs; release = production; App ID via manifest/`resValue` — not hardcoded in Kotlin
- New placement = `*AdKey` + `*AdConfig` row + `ad_ids.xml` + RC in `:data` + screen load/show — **not** controller/validator edits
- Compose: same strategy via host Activity/Fragment extensions — no raw AdMob in composables
- Feature screens remain MVI; ads calls stay outside Intent/State/Effect ownership

## Billing / premium

- Billing manager lives behind a domain `BillingRepository` (impl in `:data`)
- Persist entitlement (e.g. `isAppPurchased`) in SharedPreferences / DataStore via repository
- Premium screens follow normal MVI feature packages (`premium/`)
- Product IDs: constants in data/domain — not duplicated in UI
- After purchase success: update entitlement, then let ads/UI react
- Full subs + in-app playbook: [reference/premium-billing.md](reference/premium-billing.md)
- Skills: `hsc-implement-in-app-billing`, `hsc-add-subscription-packages`, `hsc-add-inapp-packages`

## Forbidden

- Loading ads when user is premium (unless product says otherwise)
- Production ad IDs in debug builds
- Calling BillingClient / MobileAds directly from random Fragments when `:gmaAds` / managers exist
- Hardcoding secrets or keystore passwords next to billing setup
- Converting ads to MVI unless the user explicitly asks
- Inventing a parallel ads stack instead of copying `:gmaAds` from GitHub
- Editing engine files when only adding a placement
