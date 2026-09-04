---
name: hsc-add-admob-appOpen-lifecycle
description: Hypersoft Claude Skill: Wire AdMob App Open LIFECYCLE placement (Dashboard unblock/preload, AppOpenLifecycle or AppOpenLoadingActivity). Use for resume/foreground app open, or /hsc-add-admob-appOpen-lifecycle. For splash entrance use hsc-add-admob-appOpen-Entrance. If ads were never wired, use hsc-implement-admob-ads first.
---

# Add AdMob App Open — Lifecycle

Follow `.claude/rules/21-ads-billing.md` and [reference/ads-gma.md](../../rules/reference/ads-gma.md) (App Open section).

Obey `.claude/project-settings.json` (`uiFramework`, `applicationId`).

If `:gmaAds` is missing → redirect to **`hsc-implement-admob-ads`**.

Do **not** edit `AppOpenAds`, controller, validator, or `AppOpenLifecycle` engine unless changing engine behavior. Catalog / flag only for placement config.

Sibling: `hsc-add-admob-appOpen-Entrance` for splash `ENTRANCE`.

---

## Steps

1. Ask which mode (`AppOpenAdConfig.LOAD_LIFECYCLE_WITH_LOADING_SCREEN`):

| Flag      | Behavior                                                                                                                 |
|-----------|--------------------------------------------------------------------------------------------------------------------------|
| **true**  | `AppOpenLoadingActivity` loads then shows `LIFECYCLE`. Do **not** preload on Dashboard or reload after dismiss.          |
| **false** | Load `LIFECYCLE` on Dashboard (or main shell); `AppOpenLifecycle` shows on process foreground and reloads after dismiss. |

2. Ensure `AppOpenAdKey.LIFECYCLE` + catalog row + `ad_ids.xml` + RC exist.
3. Confirm `AppOpenLifecycle` is registered in `gmaAdsModule` (shipped with module — do not reinvent).
4. On Dashboard / main shell when ads are active:

```kotlin
unblockAppOpen()
// only when LOAD_LIFECYCLE_WITH_LOADING_SCREEN == false:
loadAppOpenAd(AppOpenAdKey.LIFECYCLE)
```

5. Keep Entrance blocked through splash (`blockAppOpen` on Entrance) so lifecycle does not steal the entrance ad.
6. Usually extend `LIFECYCLE` rather than adding a third app-open key for resume ads.
7. Compose: call `unblockAppOpen` / load from the host Activity that owns the main shell.

## Remove

Stop Dashboard preload / loading-activity usage; if removing `LIFECYCLE` entirely, also stop relying on `AppOpenLifecycle` for show — follow `ads-gma.md` remove steps.
