---
name: hs-create-dialog
description: Hypersoft Cursor Skill: Create a dialog UI from a Figma link or name. XML dialog_*.xml when uiFramework is xml; Compose *Dialog when compose. Use when the user asks for a dialog layout or shares a Figma dialog node.
---

# Create Dialog Layout

Follow `.cursor/rules/09-resources-xml.mdc` + [reference/resources-xml.md](../../../rules/reference/resources-xml.md) when xml; `28-compose-ui.mdc` + [reference/compose-ui.md](../../../rules/reference/compose-ui.md) when compose. `12-naming-conventions.mdc`.

Obey `.cursor/project-settings.json` when present (`uiFramework`).

## Orchestration

**compose:** run **`hs-figma-to-compose`**. Output `<Name>Dialog` (`AlertDialog` / project dialog pattern) in the feature module. No `dialog_*.xml`. Then stop (skip XML steps).

**xml:**

1. If user provided a **Figma URL**, run the **`hs-figma-to-xml`** workflow (including mandatory `figma-design-to-code` before `get_design_context`)
2. Force output type **Dialog** → `dialog_<name>.xml` in `:presentation` `res/layout/`
3. If no Figma — create dialog XML using the same Material / ID / string rules as `hs-figma-to-xml` / `09` + `reference/resources-xml.md`

## Output

| Type   | Name              |
|--------|-------------------|
| Dialog | `dialog_<name>.xml` |

## Rules

Obey **all** XML invariants in `09-resources-xml.mdc` + [reference/resources-xml.md](../../../rules/reference/resources-xml.md) (and naming via `12-naming-conventions.mdc`). Do not re-invent Material / ID / string rules here.

**Skill-specific (dialog):**

- **Portrait + landscape** — responsive ConstraintLayout; add `layout-land/` if needed — unless `project-settings.json` says otherwise
- View IDs for dialogs: Hungarian + context suffix (`mtvTitleDialog`, `mbConfirmDialog`, `clRootDialog`)
- Prefer small confirmations / alerts as dialogs; medium filters → `hs-create-bottom-sheet`
- Prefer `ParentDialog` / project base dialog when wiring later

## Do not create

- Kotlin DialogFragment/ViewModel (use `hs-create-mvi` or wire to existing host)
- Strings in presentation module — add to shared `strings.xml` (incl. `cd_*`)
- `dimens.xml`
- `findViewById` references
- Plain `ImageView`
- Clickable `ShapeableImageView` used as a button

## After layout

Tell user to wire via `hs-create-mvi` or existing Fragment / `*Screen` if Kotlin not requested.
