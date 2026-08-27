# Hypersoft AI Workflow

Company template for **Cursor**, **Claude Code**, and **Android Studio Gemini**. Single source of truth for Clean Architecture Android apps (XML + View Binding **or** Jetpack Compose via `uiFramework`, MVI, Koin `lazyModule`).

Apps copy these folders into the project root — do not fork rules/skills inside an app.

| Tool | Copy this folder |
|------|------------------|
| **Cursor** | [`.cursor/`](.cursor/) |
| **Claude Code** | [`.claude/`](.claude/) |
| **Android Studio Gemini** | [`.skills/`](.skills/) (localization) |

Keep `.cursor/` and `.claude/` in sync. If you change a rule or skill in one, update the twin in the other. `.skills/` is Gemini-only (localization) — it is not a Cursor/Claude twin.

## What’s in this repo

| Path | Role |
|------|------|
| [`.cursor/rules/`](.cursor/rules/) | Company law (architecture, naming, invariants) |
| [`.cursor/skills/`](.cursor/skills/) | Cursor playbooks (`/hs-skill-name`) |
| [`.claude/rules/`](.claude/rules/) | Same law, Claude Code format |
| [`.claude/skills/`](.claude/skills/) | Claude Code playbooks (`/hsc-skill-name`) |
| [`.skills/hsg-localize/`](.skills/hsg-localize/) | Android Studio Gemini localization (`hsg-localize`) |
| [`MUST_READ_BEFORE_SKILL_CHANGES.md`](MUST_READ_BEFORE_SKILL_CHANGES.md) | How to edit this template |

Full skill map and rules index:

- Cursor: [`.cursor/README.md`](.cursor/README.md)
- Claude Code: [`.claude/README.md`](.claude/README.md)
- Gemini localization: [`.skills/hsg-localize/SKILL.md`](.skills/hsg-localize/SKILL.md)

## How teammates use this in an app

1. Copy `.cursor/` and/or `.claude/` into the Android app root. For Android Studio Gemini localization, also copy `.skills/`.
2. Open the app in Cursor, run `claude` at the repo root, or open the app in Android Studio (Gemini).
3. **Cursor:** type `/hs` (for example `/hs-setup-new-project`). **Claude Code:** type `/hsc` (for example `/hsc-setup-new-project`). **Gemini:** type `/hsg` (for example `/hsg-localize`) for string resources. Cursor skills say **Hypersoft Cursor Skill:**; Claude skills say **Hypersoft Claude Skill:**; Gemini skills say **Hypersoft Gemini Skill:**. Do not pick Figma plugin skills (“Created by Figma”) for Android screens.
4. After setup, obey `.cursor/project-settings.json` (or `.claude/project-settings.json`) — especially `uiFramework`.

### Gemini localization (`hsg-localize`)

Use this from **Android Studio Gemini** after `.skills/` is in the app root. Invoke **`/hsg-localize`** (type `/hsg` with no space after). It does **not** appear in Cursor `/hs` or Claude `/hsc`. Description starts with **Hypersoft Gemini Skill:**.

1. Gemini finds `@LanguageDataSource` and lists language codes.
2. You confirm languages, then complete vs missing-only localization.
3. It translates Android `values-*` string resources (native phrasing, placeholders and XML preserved) and reports files created/modified.

### Typical feature flow

Cursor: `/hs-…` · Claude Code: `/hsc-…` (same steps)

1. `/hs-setup-new-project` or `/hsc-setup-new-project` (greenfield) · `/hs-setup-old-project` or `/hsc-setup-old-project` (existing app)
2. `/hs-setup-design-system` or `/hsc-setup-design-system` — Figma tokens/themes in `:core-ui`
3. `/hs-figma-to-xml` or `/hsc-figma-to-xml` if xml; `/hs-figma-to-compose` or `/hsc-figma-to-compose` if compose
4. `/hs-create-mvi` or `/hsc-create-mvi` — Intent / State / Effect / ViewModel + Fragment **or** `*Screen`
5. `/hs-create-clean-architecture` or `/hsc-create-clean-architecture` — only when new domain / data is required
6. `/hs-review-complete` or `/hsc-review-complete` before PR

## How to change this template

Read [`MUST_READ_BEFORE_SKILL_CHANGES.md`](MUST_READ_BEFORE_SKILL_CHANGES.md) **before** editing anything under `.cursor/` or `.claude/`. Edit Gemini localization in [`.skills/hsg-localize/SKILL.md`](.skills/hsg-localize/SKILL.md).

Hard rules:

- Do not invent local rules inside an app — update **this** repo, then copy into apps.
- Do not delete rule meaning; prefer move / merge / link.
- Update related skills/rules in the same change (including Cursor ↔ Claude twins).
- New product skills: learn from the reference apps, show a plan, wait for acceptance.

## Repo

https://github.com/hypersoftdev/ai-workflow
