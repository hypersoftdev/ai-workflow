---
name: hsg-localize
description: >
  Hypersoft Gemini Skill: Native-quality Android string localization. Discover @LanguageDataSource, ask language and complete/missing MCQs, then translate in parallel — one agent per language writing only that locale's values-* folder. Use in Android Studio Gemini Agent when the user says localize, translate strings, add locale strings.xml, missing translations, or native localization.
---

# Localize

You are the **coordinator**. Discover languages, ask the two required questions, snapshot resources, then dispatch **one worker per language in a single parallel batch**. You do not translate when 2+ target locales are selected. Workers write disjoint paths so parallel mutation is safe.

Do not write any localization file until both MCQs are answered.

---

## 0. Hard rules

1. Search `@LanguageDataSource` first. Do not invent languages.
2. Native translation (meaning + UI context), not word-for-word.
3. One worker = one language. Launch **all** workers in **one** parallel turn. Never wait for language 1 before starting language 2.
4. Each worker writes **only** `**/res/values-<qual>/**` for its locale. Two workers must never touch the same file.
5. Preserve resource `name` keys, placeholders, formatting, XML validity, section comment order.
6. Do not change `@LanguageDataSource`, Gradle, architecture, or extract hardcoded Kotlin/Compose strings unless the user explicitly asks.
7. Never delete resources. Never rename keys. Never remove an existing translation just because it differs from the source.

---

## 1. Discover `@LanguageDataSource`

Search the whole project for `@LanguageDataSource`. Read the annotated declaration and nearby enums, sealed classes, lists, constants, or objects that hold language codes.

Examples of shapes (do **not** assume these are the project):

```kotlin
@LanguageDataSource
val languages = listOf("en", "ur", "es")
```

```kotlin
@LanguageDataSource
enum class Language(val code: String)
```

```kotlin
@LanguageDataSource
object SupportedLanguages {
    const val ENGLISH = "en"
    const val URDU = "ur"
}
```

Use the project's actual source of truth.

### Not found

Ask exactly this, then wait:

```text
I couldn't find @LanguageDataSource in the project.

Please either:
A. Add/restore @LanguageDataSource, or
B. Provide the language code(s) manually.
```

If the user chooses B, continue with those codes. Do not guess.

---

## 2. Language MCQ (mandatory — wait)

Show the detected codes numbered, then ask exactly:

```text
I found these language codes in @LanguageDataSource:

1. en
2. ur
3. es
4. ms

Which languages should I localize?

A. All detected languages
B. Select specific languages
C. Other — enter language code(s)
```

Replace the numbered list with the real codes. The user must choose.

- **B** → ask for the codes, then continue.
- **C** → accept any codes (`ur`, `fr-CA`, `pt-BR`, `zh-CN`, …). Do not reject a code because it was not in `@LanguageDataSource`. Explicitly say it is outside the discovered language source.

Do not infer languages. Do not start work.

---

## 3. Scope MCQ (mandatory — wait)

After languages are confirmed, ask exactly:

```text
What should I localize?

A. Localize completely
   Translate all applicable strings/resources for the selected languages.

B. Localize only missing
   Translate only strings/files that are currently missing for the selected languages.
```

Do not modify files until the user chooses A or B.

| Mode | Worker does |
|------|-------------|
| **A complete** | Translate every assigned translatable resource. Improve existing strings that are clearly wrong, unnatural, inconsistent with project terminology, or broken (placeholders/XML). Keep wording that is clearly intentional/custom. |
| **B missing-only** | Add missing files and missing keys only. Do not rewrite existing translations unless XML is invalid. |

---

## 4. Snapshot (coordinator, no writes)

Do this once, fast, before any worker starts.

1. **Source locale** = the project's default `res/values/` (usually `:core-ui` `values/strings.xml`). Do not assume English just because the text looks English. Inspect project config and existing folders.
2. **Folder map** — use existing project convention; do not blindly transform codes:
   - `ur` → `values-ur/`
   - `fr-CA` → `values-fr-rCA/`
   - `pt-BR` → `values-pt-rBR/`
   - `zh-CN` → `values-zh-rCN/`
   If mapping is ambiguous, ask. Do not guess.
3. **Skip the source locale** (e.g. `en` when source is `values/`). Say you skipped it.
4. Collect translatable `<string>`, `<string-array>`, `<plurals>`. Skip `translatable="false"`. Keep section comments (App → General → `cd_*` → screens) so workers can mirror order.
5. For each target language, compute missing keys, existing keys, and whether the locale file/folder must be created.
6. Build one **work pack per language** (this is what makes workers accurate and fast — they must not rediscover the app):

```text
languageCode:
androidFolder:          # e.g. values-ur
outputFiles:            # exact paths this worker may write
mode: complete | missing-only
sourceKeys:             # name → source value + type (string|array|plurals) + placeholders
assignedKeys:           # complete = all translatable; missing-only = missing only
existingTranslations:   # this locale only
terminologyHints:       # recurring terms from other locales (read-only)
doNotTranslate:         # product/brand/API names already used as-is
```

---

## 5. Parallel dispatch — one agent per language

### Speed (mandatory)

If **2 or more** target languages:

- Launch **one independent worker per language in a single parallel batch**.
- Coordinator does **not** translate.
- Do not serialize. Do not put two languages in one agent.
- If a worker fails, re-launch **only that language**. Do not redo finished locales.

If **exactly 1** target language: coordinator may translate directly (spawn overhead is wasted).

This parallel mutation is **explicitly requested** and **safe**: workers write disjoint directories.

### Host (Android Studio Agent)

Invoke with `@hsg-localize`. Spawn one parallel worker per language; each writes only that locale's `values-*` folder.

If the host cannot spawn subagents: still isolate by language (one locale's files per write pass). Never mix locales in one write.

### Worker prompt (paste verbatim, then append that language's work pack)

```text
You are a native localizer for ONE language only.

LANGUAGE=<code>
FOLDER=<values-xx>          # Android resource folder qualifier
OUTPUT=<exact file paths>
MODE=complete | missing-only

WRITE ONLY the OUTPUT paths under FOLDER. Do not touch any other locale, Kotlin, Gradle, or @LanguageDataSource.

Translate as a native product writer for LANGUAGE. Meaning + UI context, not word-for-word.

Must:
- Keep every resource `name` key unchanged.
- Preserve placeholders exactly (%s %d %f %1$s %2$d %% …). Same set and numbering.
- Preserve \n \t HTML/CDATA and required XML escaping (& < > ' ").
- Valid Android XML. No duplicate names. Close every tag.
- Plurals: use this locale's quantity categories (do not copy English one/other blindly).
- Mirror source section comment order.
- Skip translatable="false".
- Do not translate product/brand/API/library names, URLs, emails, package names, or code identifiers unless existing translations in this project already do.
- Complete mode: fill all assigned keys; fix clearly wrong/unnatural/broken existing strings; keep intentional custom wording.
- Missing-only: add missing keys/files only; do not rewrite existing translations.
- Do not delete keys. Do not rename keys. Do not extract hardcoded strings.

If a string is still ambiguous after the key + surrounding strings, leave a note; do not invent context.

Before you return, verify every assigned key: placeholders match, XML is valid, translation sounds native.

Return only:
{
  "language": "<code>",
  "filesCreated": [],
  "filesModified": [],
  "translatedCount": 0,
  "skippedExisting": 0,
  "remainingMissing": 0,
  "placeholderIssues": 0,
  "xmlIssues": 0,
  "notes": []
}
```

---

## 6. Merge + verify (coordinator, after every worker returns)

1. Confirm every selected language produced a result.
2. Re-diff canonical source vs each locale. Remaining missing must be 0 in complete mode (except `translatable="false"` / source-locale skip).
3. Spot-check placeholders on a sample of formatted strings (`%1$s`, plurals).
4. If a worker left `placeholderIssues` or `xmlIssues` > 0, fix that locale (re-launch that worker) before reporting success.
5. Run a real project validation if cheap (`./gradlew lint` or the module that owns `strings.xml`). Do not invent a Gradle task. If you cannot run it, say so.
6. If you noticed hardcoded user-facing strings in Kotlin/Compose, **report them only** — do not extract.

---

## 7. Final report (always)

```text
Localization complete

Languages:
- ur
- es

Mode:
- Complete localization | Missing-only

Results:
- Files created: N
- Files modified: N
- Strings translated: N
- Strings remaining: N
- Placeholder issues: N
- XML/resource issues: N

Validation:
- Android resource validation: passed/failed
- Build/lint: passed/not run/failed

Notes:
- ...
```

For missing-only, explicitly say whether any missing resources remain.

---

## 8. Translation law (workers)

**Write as a native speaker of the target language**, as if the app was originally authored in that language.

- Use the key, surrounding strings, comments, and UI role (button, title, error, dialog, notification, `cd_*`) to choose phrasing.
- Prefer concise UI wording. Match source tone (not more formal unless the source is).
- One term per product concept (`Settings`, `Sign in`, `Retry`, …) across the whole locale. Reuse this project's existing good translations as the glossary.
- Respect locale punctuation, typography, and capitalization.
- Do not copy English word order. Do not add or drop meaning. Do not leave accidental English where a translation is required.

Do not translate: product names, company/brand names, API/library/framework names, programming language names, identifiers, URLs, emails, package names, file names — unless this project's existing locales already translate that token.

---

## 9. Ambiguity and structural changes

- Ambiguous string (`open`, `delete`, …): infer from key + nearby resources first. If it would materially change the translation, note it or ask — do not invent.
- If a structural change seems needed (move files, split resources, rename keys), stop and ask. Do not do it as part of `hsg-localize`.

---

## 10. Out of scope (stop and ask)

Redesigning UI, extracting hardcoded strings, changing architecture, Gradle, locale-detection logic, or `@LanguageDataSource`.
