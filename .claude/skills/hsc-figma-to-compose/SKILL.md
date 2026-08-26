---
name: hsc-figma-to-compose
description: Hypersoft Claude Skill: Create Jetpack Compose screens from a Figma URL or freeform request (FeatureScreen / ScreenContent / Item). Compose only — no XML layouts, no ViewModel/MVI. Use when uiFramework is compose, or the user asks for Figma-to-Compose. Loads Figma design-to-code, then adapts to AnimeHub feature-module patterns.
---

# Figma / Screen → Jetpack Compose (Compose only)

Follow `.claude/rules/28-compose-ui.md` + [reference/compose-ui.md](../../rules/reference/compose-ui.md), `12-naming-conventions.md`, `24-figma-assets.md`.

Obey `.claude/project-settings.json` when present (`uiFramework`, `orientation`, `themeModes`).

**If `uiFramework` is `xml`:** stop. Use `hsc-figma-to-xml` instead (unless the user explicitly asked to convert this app to Compose).

**Never** create ViewModel / Intent / State / Effect / DI here — point user to `hsc-create-mvi` / `hsc-create-clean-architecture` afterward.

Canonical layout: AnimeHub (`feature-*` modules, `*Screen` + `*ScreenContent`, route constants, NavGraph in `:app`).

## Workflow

### A — From Figma link

1. Parse Figma URL → `fileKey` + `nodeId` (`-` → `:` in node id)
2. **Mandatory:** load Cursor Figma plugin skill **`figma-design-to-code`** before calling `get_design_context`
3. Call `get_design_context` (+ screenshot if needed)
4. Adapt to **this** project: Material3 composables, `:core-ui` strings, Coil `AsyncImage`, AnimeHub package/file names — not Figma's raw Compose dump
5. Download assets with Figma MCP `download_assets`
6. Build composables + drawables + strings only

### B — Freeform (no Figma)

Same output rules as below — create the requested `*Screen` / `*Dialog` / `*BottomSheet` / `*Item` without Figma MCP.

## Output

Place UI in the matching **`:feature-<kebab>`** module (create the module if `hsc-create-mvi` has not yet). Stateless UI can land before the ViewModel exists.

| Type | Name | Location |
|------|------|----------|
| Screen | `<Feature>Screen` + private `<Feature>ScreenContent` | `feature/<name>/<Feature>Screen.kt` |
| List row | `<Feature>Item` | same file, or `components/` if reused |
| Dialog | `<Feature>XxxDialog` | `components/` or same file |
| Bottom sheet | `<Feature>XxxBottomSheet` | `components/` or same file |
| Route | `FEATURE_ROUTE` (+ `createRouteFeature(...)` if args) | top of `*Screen.kt` |

Dialog / bottom sheet from Figma: `hsc-create-dialog` / `hsc-create-bottom-sheet` orchestrate this skill when `uiFramework` is `compose`.

## Screen shape (mandatory)

```kotlin
const val HOME_ROUTE = "home"

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (String) -> Unit,
) {
    HomeScreenContent(
        modifier = modifier,
        onNavigateToDetail = onNavigateToDetail,
    )
}

@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (String) -> Unit,
) { /* Material3 layout */ }

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreenContent(onNavigateToDetail = {})
    }
}
```

- **Stateful** `*Screen` (later: `koinViewModel()`, `collectAsStateWithLifecycle`, `LaunchedEffect` for effects)
- **Stateless** `*ScreenContent` — state + lambdas only; no ViewModel, no `NavController`
- Split loading / empty / error / success into private composables when the Figma has those states
- `Modifier` first optional param after receiver; default `Modifier`
- All copy via `stringResource(R.string.*)` from `:core-ui`
- Images: Coil `AsyncImage` (remote) or `painterResource` (local vectors) — not Glide / XML `ImageView`
- Theme colors via `MaterialTheme.colorScheme` — not hardcoded hex (tokens live in `:core-design`)

## Assets — prefer SVG

Per `24-figma-assets`:

- Prefer **SVG** / `svgAssets` for icons, logos, simple vectors → `ic_svg_*` / `img_svg_*` / `bg_svg_*` in `:core-ui`
- Use `defaultFormat: "svg"` for vector nodes unless user asked for raster
- Fall back to PNG/WebP when photo, complex art, or SVG is broken/huge
- Compose usage: `painterResource(R.drawable.ic_svg_*)` or Coil `AsyncImage`

## Rules

Obey **all** Compose invariants in `28-compose-ui.md` + [reference/compose-ui.md](../../rules/reference/compose-ui.md). Also `12-naming-conventions.md`.

**Skill-specific:**

- **Portrait + landscape** — `BoxWithConstraints` / adaptive `GridCells` / window size; `@Preview` + landscape preview when `orientation` is `both` or `landscape` — **unless** `project-settings.json` `orientation` is `portrait` or `landscape` only
- Theme modes: Compose `AppTheme(darkTheme = …)` from `:core-design`; XML `values-night` still for splash/window when `themeModes` is `night` or `both`
- **Theme-first background:** default screen color from `MaterialTheme.colorScheme.background` / `surface`
- Material3 only (`Button`, `TextButton`, `IconButton`, `OutlinedCard`, `FilterChip`, …)
- No `dimens.xml` — inline `dp` / `sp` as multiples of 4
- Feature modules **do not** import `:data` or other `:feature-*` modules. Tab hosts take `@Composable () -> Unit` slots (AnimeHub `DashboardScreen`)

## Do not create

- ViewModel / Intent / State / Effect / Koin module (use `hsc-create-mvi`)
- Domain / data / repository layers (use `hsc-create-clean-architecture`)
- XML `fragment_*` / `activity_*` layouts for this screen
- `NavController` inside the feature composable — pass `onNavigate*` lambdas
- Strings outside `:core-ui` `strings.xml` (incl. `cd_*`)
- `findViewById` / View Binding / Data Binding
- Hardcoded user-facing strings or hex that duplicates theme tokens

## After UI

- Call out any asset that fell back from SVG → raster and why
- Tell user to wire via `hsc-create-mvi` (feature module MVI + NavGraph entry) and `hsc-create-clean-architecture` if new domain/data is needed
