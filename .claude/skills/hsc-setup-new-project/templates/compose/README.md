# Compose bootstrap templates

Copied by `hsc-setup-new-project` when `uiFramework` is `compose`. Replace `YOUR.PACKAGE` and `AppTheme` / app name.

| File | Copy to |
|------|---------|
| `MainActivity.kt` | `:app` `…/ui/MainActivity.kt` |
| `NavGraph.kt` | `:app` `…/navigation/NavGraph.kt` |
| `Color.kt` `Type.kt` `Theme.kt` | `:core-design` `…/core/design/` |
| `EntranceScreen.kt` | `:feature-entrance` `…/feature/entrance/EntranceScreen.kt` |
| `feature-module.gradle.kts` | each `:feature-*` `build.gradle.kts` (adjust namespace + project deps) |

MVI (`intent` / `state` / `effect` / `viewModel` / `di`) for Entrance is scaffolded via `hsc-create-mvi` Compose path.
