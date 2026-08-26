---
name: hs-setup-design-system
description: Hypersoft Cursor Skill: Import a Figma design system into :core-ui (colors, type, ButtonStyle/TextStyle, Material theme, day/night). When uiFramework is compose, also write :core-design Color/Type/AppTheme from the same tokens. Creates :core-ui if missing. Theme-first — windowBackground, not layout colorSurface. Use when the user shares a Figma design-system URL or invokes /hs-setup-design-system. Do not use for a single screen layout (hs-figma-to-xml / hs-figma-to-compose).
---

# Setup Design System

Follow `.cursor/rules/` — especially `09-resources-xml` + [reference/resources-xml.md](../../../rules/reference/resources-xml.md), `08-gradle` + [reference/gradle.md](../../../rules/reference/gradle.md), `02-project-structure`, `12-naming-conventions`, `24-figma-assets`, `00-global`.

Obey `.cursor/project-settings.json` when present (`themeModes`, `orientation`, `applicationId`, `uiFramework`, optional `figmaDesignSystemUrl`).

Extraction file map + `:core-ui` scaffold: [reference.md](reference.md).

## Preconditions

1. **Figma URL** — `figma.com/design/...` in the prompt, or `figmaDesignSystemUrl` in `project-settings.json`. If neither exists, **ask**. Do not guess a file.
2. Persist the URL to `figmaDesignSystemUrl` in `.cursor/project-settings.json` (and `.claude/project-settings.json` if that file exists).
3. **`:core-ui`** — if it is not on the Gradle graph, **create it** (see [reference.md](reference.md)) and continue. Do not stop. Do not wait for another skill.
4. Read `themeModes`: `day` → `values/` only; `night` / `both` → also `values-night/`.

## Steps

1. Ensure `:core-ui` exists and `:app` `implementation(project(":core-ui"))`. If the module was missing, scaffold it first, then continue this list in the same turn.
2. Parse URL → `fileKey` + `nodeId` (`-` → `:`). Load **figma-design-to-code** before `get_design_context`; load **figma-use** before `use_figma`.
3. Extract tokens with **targeted** reads only (Foundation / Tokens pages, local text styles, semantic light/dark rows). Follow Figma `VARIABLE_ALIAS` — semantics reference primitives; do not flatten to hex. No whole-file variable dumps.
4. Write `:core-ui` resources per [reference.md](reference.md): primitive hex → semantic `@color/primitive_*` aliases → `md_theme_*` → Material theme attrs. Keep Material theming (`Theme.Material3.DayNight`, `colorPrimary` / `colorSurface` / `windowBackground`). Fonts, `TextStyle.*`, `ButtonStyle.*`, `ShapeAppearance.App.*`.
5. **Theme-first:** default screen color lives on the theme. Do **not** set `android:background="?attr/colorSurface"` (or equivalent) on default layout roots. Layout `android:background` only when that region is a **different** surface.
6. Do not invent screens, `dimens.xml`, or port the full icon/component library (screens stay `hs-figma-to-xml` or `hs-figma-to-compose`).
7. **compose:** also write `:core-design` `Color.kt` / `Type.kt` / `Theme.kt` (`AppTheme`) from the same tokens (`28-compose-ui`). Keep XML themes in `:core-ui` for splash / day-night if needed.
8. `assembleDebug`. Report what landed (palette, type family, button styles) and that layouts inherit `windowBackground`.

## Do not

- Stop because `:core-ui` is missing — create it and keep going
- Create `:presentation`, `:domain`, `:data`, Entrance, or `Parent*`
- Implement a product screen from this file (`hs-figma-to-xml` / `hs-figma-to-compose`)
- Paint default roots with `colorSurface`
- Duplicate hex on semantic (or `md_theme_*`) tokens — only primitives hold hex
- Drop Material theming / `md_theme_*` (widgets fall back to default purple)
- Add always-on rules; XML law stays in `09` + `reference/resources-xml.md`
