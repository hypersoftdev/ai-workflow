---
name: hsc-update-in-app-billing
description: Hypersoft Claude Skill: Migrate existing hypersoft inappbilling from v3.x to v4.0.0 — catalog bump, remove listeners, singleton BillingManager, Flow/suspend API, offerId threading. Use when upgrading inappbilling, Play Billing 9 migration, legacy BillingPurchaseListener, or /hsc-update-in-app-billing.
---

# Update In-App Billing (v3 → v4.0.0)

Follow `.claude/rules/21-ads-billing.mdc`, `00-global.mdc`, `04-mvi-presentation.mdc`, `07-dependency-injection.mdc`, `16-logging.mdc`.

Full detail: [`.claude/rules/reference/premium-billing.md`](../rules/reference/premium-billing.md)

Obey `.claude/project-settings.json` when present.

**Reference template:** `E:\SohaibAhmed\UnderWorking\Speak-Translate-HSAIAppsLab` (v4.0.0 stack). Copy architecture — do not invent a parallel `BillingClient` wrapper.

Cross-skills: greenfield → `hsc-implement-in-app-billing`; add tiers/products after migration → `hsc-add-subscription-packages` / `hsc-add-inapp-packages`.

---

## When to use / stop

| App state                                                                                         | Action                                            |
|---------------------------------------------------------------------------------------------------|---------------------------------------------------|
| No billing stack                                                                                  | Stop → **`hsc-implement-in-app-billing`**             |
| Already on `inappbilling` **4.0.0+** with Flow/suspend API                                        | Stop — only bump patch if user asks               |
| v3 listeners (`BillingPurchaseListener`, `setListener`, `startConnection()`) or catalog `< 4.0.0` | Continue below                                    |
| Raw `BillingClient` in features (no hypersoft lib)                                                | Stop — run **`hsc-implement-in-app-billing`** instead |

---

## Step 0 — Audit (before editing)

Search the project and document:

| Signal                 | v3 (migrate)                               | v4 (done)                                  |
|------------------------|--------------------------------------------|--------------------------------------------|
| Catalog `inappbilling` | `< 4.0.0`                                  | `4.0.0`                                    |
| Constructor            | `BillingManager(context, scope)`           | `BillingManager(context)`                  |
| Connect                | `setListener(…).startConnection()`         | `connect()` + observe `connectionState`    |
| Catalog fetch          | `fetchProductDetails(listener)`            | `productsState` Flow                       |
| Purchases              | `fetchPurchaseHistory(listener)`           | `purchasesState` Flow                      |
| Purchase               | `purchaseSubs(…, BillingPurchaseListener)` | `suspend purchaseSubs(…): PurchaseOutcome` |
| Imports                | `com.hypersoft.billing.data.entities.*`    | `com.hypersoft.billing.model.*`            |
| Instances              | Per-Activity / per-screen `BillingManager` | One singleton via `BillingDataSource`      |

Print audit summary. If both subs and in-app exist, note `setSubscriptions` / `setNonConsumables` / `setConsumables` IDs — migration must preserve them.

**Key reference files (Speak-Translate):**

| Layer | Path                                                                                          |
|-------|-----------------------------------------------------------------------------------------------|
| Data  | `data/.../billing/dataSource/BillingDataSource.kt`                                            |
| Data  | `data/.../billing/repository/BillingRepositoryImpl.kt`                                        |
| App   | `app/.../App.kt` (`BillingDataSource.start(applicationScope)`)                                |
| DI    | `data/.../di/DataModule.kt`                                                                   |
| UI    | `presentation/.../premium/ui/PremiumFragment.kt`, `splashPremium/ui/SplashPremiumFragment.kt` |
| Docs  | `docs/premium.md` §7 (existing-app update)                                                    |

---

## Step 1 — Gradle

1. **JitPack** in `settings.gradle.kts` (`dependencyResolutionManagement.repositories`):

```kotlin
maven(url = "https://jitpack.io")
```

2. **Catalog** under `# Billing`:

```toml
inappbilling = "4.0.0"

hypersoft-inappbilling = { group = "com.github.hypersoftdev", name = "inappbilling", version.ref = "inappbilling" }
```

3. `implementation(libs.hypersoft.inappbilling)` on `:data` and `:presentation` (remove any hardcoded `com.github.hypersoftdev:inappbilling` lines).
4. Ensure `const val TAG_BILLING = "TAG_BILLING"` in `:core-common` `Constants` (add if missing).
5. Sync / compile — fix import errors before logic changes.

---

## Step 2 — Remove v3 API (delete, do not wrap)

Remove entirely:

- `BillingConnectionListener`, `BillingPurchaseListener`, `BillingProductDetailsListener`, `BillingPurchaseHistoryListener`
- `setListener(...)`, `startConnection()` on manager
- `fetchProductDetails(...)`, `fetchPurchaseHistory(...)` with callbacks
- `BillingManager(context, scope)` — drop the scope argument
- Imports from `com.hypersoft.billing.data.entities.*`

Do **not** keep listener adapters around v4 — v4 is suspend + Flow only.

### 3.x → 4.0.0 API map

| 3.x                                        | 4.0.0                                                            |
|--------------------------------------------|------------------------------------------------------------------|
| `BillingManager(context, scope)`           | `BillingManager(context.applicationContext)`                     |
| `setListener(…).startConnection()`         | `connect()`; observe `connectionState`                           |
| `fetchProductDetails(listener)`            | `productsState`                                                  |
| `fetchPurchaseHistory(listener)`           | `purchasesState`                                                 |
| `purchaseSubs(…, BillingPurchaseListener)` | `suspend purchaseSubs(…): PurchaseOutcome` + **`offerId`**       |
| `purchaseInApp(…, listener)`               | `suspend purchaseInApp(…): PurchaseOutcome` + optional `offerId` |
| `com.hypersoft.billing.data.entities.*`    | `com.hypersoft.billing.model.*`                                  |

---

## Step 3 — Data: singleton `BillingDataSource`

Replace v3 wiring with one app-lifetime owner (match Speak-Translate):

```kotlin
class BillingDataSource(
    context: Context,
    private val sharedPrefManager: SharedPrefManager,
) {
    val billingManager: BillingManager = BillingManager(context.applicationContext)

    fun start(scope: CoroutineScope) {
        billingManager
            .setSubscriptions(BillingProductIds.allSubscriptionProductIds) // preserve existing IDs
        // .setNonConsumables(...) / .setConsumables(...) if app had in-app products
        scope.launch {
            val state = billingManager.connect()
            Log.d(TAG_BILLING, "BillingDataSource: start: Connection: $state")
            billingManager.purchasesState.collect { purchaseState ->
                when (purchaseState) {
                    is UiState.Success -> {
                        val hasPurchase = purchaseState.data.isNotEmpty()
                        sharedPrefManager.isAppPurchased = hasPurchase
                        Log.d(TAG_BILLING, "BillingDataSource: syncPurchases: Success: hasPurchase=$hasPurchase")
                    }
                    is UiState.Error ->
                        Log.e(TAG_BILLING, "BillingDataSource: syncPurchases: Failed: ${purchaseState.message}")
                    is UiState.Loading -> Unit
                }
            }
        }
    }
}
```

Koin (`dataModule`):

```kotlin
single { BillingDataSource(androidContext(), get()) }
single { get<BillingDataSource>().billingManager }
single<BillingRepository> { BillingRepositoryImpl(billingDataSource = get()) }
```

**Application** (after Koin ready):

```kotlin
koin.get<BillingDataSource>().start(applicationScope)
```

Delete: per-screen `BillingManager` construction, v3 `startConnection()` calls from ViewModels/Repositories.

---

## Step 4 — Data: `BillingRepositoryImpl`

1. Map **`productsState`** → domain offers (not listener callbacks).
2. Map **`purchasesState`** → `observeHasActivePurchase()` if used.
3. Implement **prefer-trial** mapper (required for v4 trials):

```kotlin
val preferred = matches.firstOrNull { detail ->
    detail.pricingDetails.any { it.recurringMode == RecurringMode.FREE } ||
            detail.offerId.isNotEmpty()
} ?: matches.first()
```

4. Persist `offerId = preferred.offerId.takeIf { it.isNotEmpty() }` on `SubscriptionOffer`.
5. `startConnection()` → log-only delegate (connection owned by `Application`).

Move hardcoded product/plan strings from Fragments into `BillingProductIds` if not already there.

---

## Step 5 — Domain

Ensure entities support v4 purchase threading:

- `SubscriptionOffer`: `productId`, `planId`, **`offerId: String?`**, `priceFormatted`, `hasFreeTrial`, `freeTrialDays`
- `BillingPurchaseResult` sealed class for presentation mapping
- `BillingRepository.observeOffers(): Flow<List<SubscriptionOffer>>`

Add `offerId` to MVI effects if missing:

```kotlin
data class LaunchSubsPurchase(
    val productId: String,
    val planId: String,
    val offerId: String?,
) : PremiumEffect()
```

---

## Step 6 — Presentation

1. Inject **`BillingManager`** from Koin (singleton) — never `BillingManager(requireContext())`.
2. Replace listener purchase calls with **suspend** in Fragment `lifecycleScope`:

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    val outcome = billingManager.purchaseSubs(
        activity = act,
        productId = productId,
        planId = planId,
        offerId = offerId, // from SubscriptionOffer — required for trials
    )
    viewModel.handleIntent(PremiumIntent.PurchaseResultReceived(outcome.toDomain()))
}
```

3. Map `PurchaseOutcome` → `BillingPurchaseResult` (Success / AlreadyOwned / UserCancelled / Failed).
4. **DEBUG:** keep fake purchase path only — never in release.
5. Thread `offerId` from ViewModel State/Effect — omitting it buys the default paid offer (no trial sheet).

Same pattern for `purchaseInApp` when app has one-time products.

---

## Step 7 — Entitlement & ads

- Sync `isAppPurchased` from `purchasesState` (Step 3) **and** on `PurchaseOutcome.Success` / `AlreadyOwned`.
- Gate ads managers on `isAppPurchased` — do not convert ads to MVI unless user explicitly asks.
- Do not log purchase tokens or PII.

---

## Step 8 — Tests

When `writeTestsWithFeatures: true`:

- Update fake `BillingRepository` to emit `SubscriptionOffer` with non-null `offerId` for trial plans.
- Assert `LaunchSubsPurchase` / purchase effects carry `offerId` from mapped offer — not `null` when trial exists.
- Remove tests that mock v3 listeners.

---

## Step 9 — Verify

**Build**

- [ ] Catalog `inappbilling = "4.0.0"`, JitPack, deps on `:data` + `:presentation`
- [ ] No v3 listener imports remain
- [ ] Single `BillingManager` instance

**Trials / offers**

- [ ] Prefer-trial mapper in `BillingRepositoryImpl`
- [ ] `purchaseSubs(..., offerId = offer.offerId)` in release Fragments
- [ ] Play Console trial **offer** active on intended plan

**Runtime**

- [ ] `Application` calls `BillingDataSource.start(applicationScope)`
- [ ] `isAppPurchased` restored from `purchasesState` on cold start
- [ ] Ads hidden when purchased
- [ ] Fake purchase debug only

**QA (internal track + license tester)**

- [ ] New tester: Play sheet shows free trial when UI promises trial
- [ ] Same account after trial consumed: paid offer only
- [ ] Cancel purchase: no error toast, still not premium
- [ ] Reinstall / restore: premium persists, no ads

After migration, use **`hsc-add-subscription-packages`** or **`hsc-add-inapp-packages`** to expand SKUs — not this skill.

---

## Do not

- Ship with v3 listeners kept “for compatibility”
- Construct `BillingManager` per screen or pass `viewModelScope` into constructor
- Call `purchaseSubs` without `offerId` when UI advertises a free trial
- Skip `purchasesState` collection (premium lost on reinstall)
- Use raw `BillingClient` in presentation/domain
- Log purchase tokens, emails, or other PII
