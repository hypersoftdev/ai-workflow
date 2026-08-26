# Hypersoft AI Workflow — Claude Code

Production Android app. Kotlin, Clean Architecture, MVVM + MVI (Intent / State / Effect), Koin `lazyModule` only. UI from `.claude/project-settings.json` **`uiFramework`**: `xml` (View Binding + `:presentation`) or `compose` (Jetpack Compose + `:feature-*`). Never Data Binding. No Hilt unless the project already uses it.

## Law

Follow [`.claude/rules/`](rules/) (especially `00-global`, `14-security-secrets`, `16-logging`). Path-scoped rules load when matching files are touched. Full patterns: [`.claude/rules/reference/`](rules/reference/). Compose packaging: `28-compose-ui` + [reference/compose-ui.md](rules/reference/compose-ui.md).

When [`.claude/project-settings.json`](project-settings.json) exists, obey `uiFramework`, `writeTestsWithFeatures`, `orientation`, `themeModes`, `applicationId`, `appName`, optional `figmaDesignSystemUrl`.

## Typical feature flow

1. `/hsc-setup-new-project` (greenfield) or `/hsc-setup-old-project` (existing production app) — persist **`uiFramework`**
2. `/hsc-setup-design-system` — Figma tokens/themes in `:core-ui` (and `:core-design` when compose)
3. `/hsc-figma-to-xml` if xml; `/hsc-figma-to-compose` if compose (or `/hsc-create-dialog` / `/hsc-create-bottom-sheet`)
4. `/hsc-create-mvi` — Intent / State / Effect / ViewModel + Fragment **or** `*Screen` in `:feature-*`
5. `/hsc-create-clean-architecture` — only when new domain / data is required
6. `/hsc-review-complete` before PR

## Skills

Invoke with `/hsc-…` (for example `/hsc-create-mvi`, `/hsc-figma-to-xml`, `/hsc-figma-to-compose`). Descriptions start with **Hypersoft Claude Skill:**. Full map and rules index: [README.md](README.md).
