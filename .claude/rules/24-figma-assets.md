---
description: Figma MCP asset download preferences for Android XML — prefer SVG
paths:
  - "**/res/drawable*/**"
  - "**/res/layout/**/*.xml"
  - "**/feature*/**/*.kt"
  - "**/core/design/**/*.kt"
---

## When this applies

Figma → Android XML **or Compose** work: `download_assets`, `get_design_context`, or implementing a Figma screen/link.

## Prefer SVG (default)

When downloading assets via Figma MCP (`download_assets`):

1. **Prefer SVG** whenever the asset is (or can be) vector:
   - Icons, logos, simple illustrations, UI glyphs, flat shapes
   - When `svgAssets` is returned for that node/layer — **use those first**
   - For node export, prefer `defaultFormat: "svg"` when the node is vector-friendly and the user did not demand PNG/JPG
2. Save into `:core-ui` (or project drawable module) with project prefixes:
   - Icons → `ic_svg_*`
   - Large vectors → `img_svg_*`
   - Vector backgrounds → `bg_svg_*`
3. Convert / place as Android Vector Drawable XML when possible (not a loose `.svg` in `drawable` unless the project already uses a SVG loader)

## When not to force SVG

Use raster (`png` / `webp` / `jpg` from `rawImages` or export) when:

- Photograph, complex illustration, soft shadows/gradients that break as SVG
- SVG export fails, is empty, overly huge/complex, or looks wrong vs Figma
- User explicitly asks for PNG/JPG/@2x/@3x raster
- Asset is an uploaded bitmap fill in Figma (`rawImages`) with no useful vector

Then name with `ic_png_*` / `img_png_*` / `bg_*` per `09-resources-xml`.

## Usage in layouts (xml) / composables (compose)

- **xml:** Always `ShapeableImageView` (`siv`) for **display-only** images — never plain `ImageView`. Bind with `siv.loadImage(...)` (Glide)
- **compose:** Coil `AsyncImage` for remote/dynamic; `painterResource` for local vectors — never Glide in feature modules
- Clickable icons / toolbar actions → xml `MaterialButton` + `ButtonStyle.IconButton`; compose `IconButton` / `FilledIconButton`
- Solid + stroke button surfaces from Figma → xml MaterialButton tint/stroke/cornerRadius; compose `Button`/`OutlinedButton` colors from `MaterialTheme`
- `contentDescription` via `cd_*` strings (`stringResource` in Compose)
- Do not invent icons by hand when Figma provided an export — use the downloaded asset

## Decision summary

| Asset kind | Prefer |
|------------|--------|
| Icon / logo / simple vector | **SVG** → `ic_svg_*` / vector drawable |
| Photo / complex raster | PNG/WebP → `ic_png_*` / `img_png_*` |
| Unclear | Try SVG first; fall back to raster if broken |

## Forbidden

- Defaulting to PNG for simple icons when SVG is available
- Hand-drawing replacement paths when MCP exported the real asset
- Hardcoded contentDescription text
- Putting Figma assets in `:app` `values` / random folders — use shared drawable module (`:core-ui`)
