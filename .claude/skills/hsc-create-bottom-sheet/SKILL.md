---
name: hsc-create-bottom-sheet
description: Hypersoft Claude Skill: Create a bottom sheet UI from a Figma link or name. XML bottom_sheet_*.xml when uiFramework is xml; Compose *BottomSheet (ModalBottomSheet) when compose. Use when the user asks for a bottom sheet without Kotlin BottomSheetDialogFragment wiring.
---

# Create Bottom Sheet Layout

Follow `.claude/rules/09-resources-xml.md` + [reference/resources-xml.md](../../rules/reference/resources-xml.md) when xml; `28-compose-ui.md` + [reference/compose-ui.md](../../rules/reference/compose-ui.md) when compose.

Obey `.claude/project-settings.json` when present (`uiFramework`).

## Orchestration

**compose:** run **`hsc-figma-to-compose`**. Output `<Name>BottomSheet` (`ModalBottomSheet`) in the feature module. No `bottom_sheet_*.xml`. Then stop (skip XML steps).

**xml:**

1. If user provided a **Figma URL**, run the **`hsc-figma-to-xml`** workflow (including mandatory `figma-design-to-code` before `get_design_context`)
2. Force output type **Bottom sheet** → `bottom_sheet_<feature>_<purpose>.xml` in `:presentation` `res/layout/`
3. If no Figma — create sheet XML using the structure below + XML invariants from `09` + `reference/resources-xml.md` / `hsc-figma-to-xml`

## Output

`:presentation` `res/layout/bottom_sheet_<feature>_<purpose>.xml`

Example: `bottom_sheet_filter_options.xml`, `bottom_sheet_compass_guidelines.xml`

## Structure

- Root `ConstraintLayout` or `LinearLayout` with top rounded corners (`bg_shape_*` top radius)
- Optional drag handle view
- Title `mtv`, content area, primary/secondary `mb`
- Scrollable content: `NestedScrollView` only if needed — keep shallow when possible

## Rules

Obey **all** XML invariants in `09-resources-xml.md` + [reference/resources-xml.md](../../rules/reference/resources-xml.md). Do not re-invent Material / ID / string rules here.

**Skill-specific (bottom sheet):**

- View Binding IDs with sheet context (`mtvSheetTitle`, `mbSheetApply`)
- Root may use `bg_shape_*` for **top rounded sheet surface** (non-button) — button solid+stroke still uses Material attrs on the button (see `09` / reference)
- Portrait + landscape — avoid fixed heights that break landscape — unless `project-settings.json` says otherwise
- No Kotlin `BottomSheetDialogFragment` unless user requests separately
- Prefer `ParentSheet` / project base sheet when wiring later

## vs dialog

- Use bottom sheet for contextual actions / filters / medium content
- Use `hsc-create-dialog` for small confirmations

## After layout

Tell user to wire via `hsc-create-mvi` or existing host if Kotlin not requested.
