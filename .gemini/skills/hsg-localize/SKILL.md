---
name: hsg-localize
description: Hypersoft Gemini Skill: Interactive Android localization from @LanguageDataSource. Use in Android Studio Gemini to translate strings.xml / values-* resources. Confirm languages and complete vs missing-only before editing files.
---

# Localize — Android Localization Skill

## Purpose

`hsg-localize` is an interactive Android localization workflow.

Its job is to:

1. Discover supported languages from the project's `@LanguageDataSource`.
2. Ask the user to confirm the language codes before making changes.
3. Ask whether to localize:

    * the complete localization set, or
    * only missing strings/files.
4. Inspect the existing Android localization structure.
5. Translate strings naturally and idiomatically for native speakers.
6. Preserve Android/XML semantics, placeholders, formatting, and technical terminology.
7. Validate the resulting resources.
8. Provide a concise localization report.

This skill is intended for use inside an existing Android Studio project.

---

# 1. Core Principles

## 1.1 Interactive first

Do not immediately modify localization files.

Before making any localization changes, the skill MUST:

1. Find `@LanguageDataSource`.
2. Determine the available language codes.
3. Present the discovered languages to the user.
4. Ask the user to confirm which language codes to process.
5. Ask whether the user wants complete or missing-only localization.

Do not infer the user's desired languages or localization scope.

---

## 1.2 Native translation, not literal translation

The primary translation objective is **natural native-language localization**, not word-for-word translation.

For every string:

* Understand the meaning and UI context first.
* Translate the meaning rather than individual words.
* Use terminology naturally used by native speakers.
* Follow conventions used by high-quality native Android applications.
* Preserve the intended tone.
* Preserve the intent of buttons, labels, errors, dialogs, notifications, onboarding text, etc.
* Prefer concise UI language where the source string is UI text.
* Avoid unnatural machine-translation phrasing.
* Avoid unnecessarily formal language unless the source/context requires it.
* Maintain terminology consistency throughout the application.

The result should read as though it was originally written by a native speaker of the target language.

---

# 2. Initial Workflow

When the user invokes `hsg-localize`, start with language discovery.

Do not ask the user to manually provide languages before inspecting the project.

## Step 1 — Find `@LanguageDataSource`

Search the entire project for:

```text
@LanguageDataSource
```

Also inspect nearby declarations, enums, sealed classes, constants, annotations, or configuration objects that define the supported language codes.

Examples may include:

```kotlin
@LanguageDataSource
val languages = listOf(
    "en",
    "ur",
    "es"
)
```

or:

```kotlin
@LanguageDataSource
enum class Language(
    val code: String
)
```

or:

```kotlin
@LanguageDataSource
object SupportedLanguages {
    const val ENGLISH = "en"
    const val URDU = "ur"
}
```

Do not assume that the examples above represent the project's actual implementation.

Use the project's existing source of truth.

---

# 3. Language Confirmation MCQ

After discovering the language codes, show the user the detected languages.

Use an explicit MCQ-style question.

Example:

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

The user must explicitly choose.

If the user chooses **B**, ask them to provide the desired codes.

If the user chooses **C**, allow arbitrary language codes such as:

```text
ur
es
ms
fr-CA
pt-BR
zh-CN
```

Do not reject a user-provided language code merely because it was not present in `@LanguageDataSource`.

However, explicitly mention that the manually supplied code is outside the discovered language source.

---

# 4. Validate Language Codes

Before localization, validate the selected language codes against the project's resource structure.

Determine the project's existing convention.

Possible Android resource directories include:

```text
values/
values-ur/
values-es/
values-ms/
values-fr-rCA/
values-pt-rBR/
values-zh-rCN/
```

Do not blindly transform a language code.

For example:

```text
fr-CA
```

may correspond to:

```text
values-fr-rCA/
```

Use the project's existing convention where possible.

If the mapping is ambiguous, ask the user rather than guessing.

---

# 5. Localization Scope MCQ

After languages are confirmed, ask:

```text
What should I localize?

A. Localize completely
   Translate all applicable strings/resources for the selected languages.

B. Localize only missing
   Translate only strings/files that are currently missing for the selected languages.
```

Do not begin modifying files until the user chooses A or B.

---

# 6. Complete Localization

If the user chooses:

```text
A. Localize completely
```

For each selected language:

1. Identify the source/default localization.
2. Identify all relevant string resources.
3. Inspect existing translations.
4. Translate all applicable strings.
5. Update existing translations when necessary to ensure complete and consistent localization.
6. Preserve resource names and structure.
7. Verify that all relevant strings are represented in the target locale.

Do not blindly overwrite translations that contain intentional project-specific wording.

If an existing translation appears intentionally customized, preserve it unless there is a clear reason to change it.

---

# 7. Missing-Only Localization

If the user chooses:

```text
B. Localize only missing
```

Identify:

### Missing files

For example, if:

```text
values/
values-es/
values-ur/
```

exist but `values-ms/` does not, and `ms` is selected, the required localization file may need to be created.

### Missing strings

Compare the source/default resource set against each selected locale.

For example:

```xml
<!-- values/strings.xml -->

<string name="app_name">My App</string>
<string name="welcome">Welcome</string>
<string name="settings">Settings</string>
```

If:

```xml
<!-- values-ur/strings.xml -->

<string name="app_name">میری ایپ</string>
<string name="welcome">خوش آمدید</string>
```

exists, then:

```text
settings
```

is missing and should be localized.

Do not modify existing translations during missing-only localization unless required to repair an invalid resource.

---

# 8. Determine the Source Language

Determine the canonical source language from the project.

Usually this is:

```text
res/values/
```

or the project's existing default resource directory.

Do not assume English solely because the strings appear to be English.

Inspect the project configuration and existing localization structure.

Use the canonical/default resource as the primary semantic source unless project conventions indicate otherwise.

---

# 9. Translation Context

Never translate a string in isolation when context is available.

Before translating, inspect:

* string name
* surrounding strings
* source file
* resource type
* usages in Kotlin/Java
* usages in Compose
* screen/feature name
* comments
* surrounding UI
* plural resources
* formatting arguments
* existing translations
* project terminology

For example:

```xml
<string name="delete">Delete</string>
```

should be interpreted as a UI action rather than translated without context.

If the key or usage indicates that the text is:

* a button
* menu item
* title
* error
* confirmation dialog
* notification
* accessibility label
* placeholder
* tooltip

translate it according to that context.

---

# 10. Native Translation Rules

For each target language:

## Required

* Translate naturally.
* Use native grammar.
* Use native word order.
* Use culturally appropriate terminology.
* Use terminology commonly found in modern apps.
* Maintain consistent translations for recurring concepts.
* Preserve the source's intent and tone.
* Prefer concise UI wording.
* Respect locale-specific punctuation and typography.
* Respect locale-specific capitalization conventions.

## Avoid

* Literal word-by-word translation.
* English sentence structure copied into the target language.
* Awkward transliteration when a natural native term exists.
* Unnecessary English words.
* Inconsistent translations of the same product concept.
* Overly formal language when inappropriate.
* Machine-translation artifacts.
* Adding information that does not exist in the source.
* Removing meaning from the source.

---

# 11. Terminology Consistency

Build a lightweight terminology map while translating.

If the project repeatedly uses concepts such as:

```text
Account
Profile
Settings
Sign in
Sign out
Delete account
Notifications
Privacy
Password
Continue
Cancel
Retry
```

translate them consistently within the target language.

Existing high-quality translations in the project should be treated as terminology references.

Do not introduce multiple translations for the same concept without contextual justification.

For example, if the project's existing Urdu translation consistently uses one native term for "Settings", use that terminology throughout the application.

---

# 12. Product and Technical Terms

Do not translate the following unless the project's existing localization convention explicitly does so:

* Product names
* Company names
* Brand names
* API names
* Library names
* Framework names
* Programming languages
* Code identifiers
* URLs
* Email addresses
* Package names
* File names
* Technical identifiers

Examples:

```text
Android
Android Studio
Kotlin
Jetpack Compose
Firebase
Google
GitHub
API
URL
HTTP
JSON
```

may remain unchanged where appropriate.

Do not translate resource keys.

For example:

```xml
<string name="delete_account">...</string>
```

must retain:

```text
delete_account
```

---

# 13. Placeholder Preservation

Placeholders MUST NEVER be translated, removed, reordered incorrectly, or altered accidentally.

Examples:

```text
%s
%d
%f
%1$s
%2$d
```

If the source is:

```xml
<string name="welcome_user">Welcome, %1$s!</string>
```

the translated resource must retain:

```text
%1$s
```

exactly.

Before finalizing each translated string:

1. Detect placeholders in the source.
2. Detect placeholders in the translation.
3. Ensure the same placeholders exist.
4. Ensure numbered placeholders remain correctly numbered.

If placeholders do not match, fix the translation before completion.

---

# 14. Formatting Preservation

Preserve meaningful formatting.

Examples include:

```text
\n
\t
%s
%d
%1$s
HTML tags
CDATA
escaped apostrophes
escaped quotation marks
```

For example:

```xml
<string name="message">Hello\nWorld</string>
```

must preserve the newline escape.

Do not accidentally convert:

```text
\n
```

into an actual newline unless the project's resource format explicitly requires it.

---

# 15. XML Safety

All generated Android resource XML must remain valid.

Preserve or correctly escape characters such as:

```text
&
<
>
'
"
```

when required by Android XML/resource syntax.

Do not introduce malformed XML.

After modifying files, inspect the resulting XML for:

* malformed tags
* missing closing tags
* invalid escaping
* duplicate resource names
* invalid resource syntax

---

# 16. Resource Types

Handle localization resources according to their Android resource type.

Pay particular attention to:

```text
<string>
<string-array>
<plurals>
<quantity>
```

For plural resources:

```xml
<plurals name="items">
    <item quantity="one">%d item</item>
    <item quantity="other">%d items</item>
</plurals>
```

translate each quantity appropriately for the target locale.

Do not assume every language has the same pluralization behavior as English.

Preserve the resource's quantity categories where Android/project conventions require them, while applying the target locale's natural grammar.

---

# 17. Compose and Kotlin String Usage

Localization may be represented not only by XML resources.

Inspect relevant usages such as:

```kotlin
stringResource(...)
context.getString(...)
resources.getString(...)
```

and project-specific localization abstractions.

Do not automatically convert hardcoded strings unless the user explicitly asks for extraction/refactoring.

The `hsg-localize` skill's default responsibility is localization, not broad string-extraction refactoring.

If hardcoded user-facing strings are discovered, report them separately.

Example:

```text
Found 3 hardcoded user-facing strings that are not currently part of Android resources.
I did not modify them.
```

---

# 18. Missing File Creation

When missing-only localization requires a new resource file:

1. Follow the project's existing filename conventions.
2. Follow the correct Android locale directory convention.
3. Include only required resources.
4. Do not copy unrelated resources unnecessarily.
5. Ensure resource names match the canonical source resources.

Example:

```text
res/values-ms/strings.xml
```

should contain the required missing resources for `ms`.

Do not create duplicate resources across multiple files if the project's structure does not require it.

---

# 19. Existing Translation Quality

When existing translations are encountered:

### Complete mode

Review them for consistency and correctness.

You may improve an existing translation when:

* it is clearly incorrect,
* it is clearly unnatural,
* it conflicts with project terminology,
* it has an obvious placeholder/formatting issue,
* it does not convey the source meaning.

### Missing-only mode

Do not rewrite existing translations.

Only add missing resources, except for necessary structural/validity fixes.

---

# 20. Ambiguous Strings

If a string is genuinely ambiguous and its usage does not provide enough context, inspect usages before asking the user.

For example:

```xml
<string name="open">Open</string>
```

could mean:

* open a file
* open a screen
* open a link
* physically open something

Search for its usage.

If ambiguity remains significant and could materially change the translation, ask the user for clarification.

Do not invent context.

---

# 21. User Confirmation Before Destructive Changes

Never delete localization resources as part of the normal `hsg-localize` workflow.

Never rename resource keys automatically.

Never remove existing translations merely because they differ from the source.

If a structural change appears necessary, explain it and request confirmation.

---

# 22. Translation Quality Checklist

Before completing localization for each language, verify:

* [ ] Translation is semantically correct.
* [ ] Translation sounds native.
* [ ] UI terminology is natural.
* [ ] Tone matches the source.
* [ ] Terminology is consistent.
* [ ] Resource keys are unchanged.
* [ ] Placeholders are preserved.
* [ ] Formatting is preserved.
* [ ] XML remains valid.
* [ ] No accidental English remains where translation is expected.
* [ ] Product/technical terms were handled appropriately.
* [ ] Pluralization was handled appropriately.
* [ ] Existing translations were respected according to the selected mode.

---

# 23. Final Verification

After making changes, compare the canonical source resources against each selected locale.

For each language, report:

* Total source resources.
* Existing resources.
* Newly translated resources.
* Remaining missing resources.
* Files created.
* Files modified.
* Potential issues.

If possible, run appropriate Android/project validation.

Examples:

```text
./gradlew lint
```

or the project's relevant build/test task.

Do not invent a Gradle task if the project uses a different build setup.

If validation cannot be run, state that clearly.

---

# 24. Final Report

Always finish with a concise report.

Use this structure:

```text
Localization complete

Languages:
- ur
- es
- ms

Mode:
- Complete localization

Results:
- Files created: 2
- Files modified: 5
- Strings translated: 184
- Strings remaining: 0
- Placeholder issues: 0
- XML/resource issues: 0

Validation:
- Android resource validation: passed
- Build/lint: passed/not run/failed

Notes:
- ...
```

For missing-only mode, explicitly report whether any missing resources remain.

---

# 25. Failure Handling

If `@LanguageDataSource` cannot be found:

Do not guess the supported languages.

Tell the user:

```text
I couldn't find @LanguageDataSource in the project.

Please either:
A. Add/restore @LanguageDataSource, or
B. Provide the language code(s) manually.
```

If the user chooses manual codes, continue using those codes.

If a requested locale cannot be mapped confidently to an Android resource directory, ask for confirmation.

If translation context is insufficient for a critical string, ask rather than guessing.

---

# 26. Important Behavioral Rules

The skill MUST follow these rules:

1. Search for `@LanguageDataSource` first.
2. Confirm language selection with the user.
3. Ask for localization scope.
4. Do not modify files before the required confirmations.
5. Translate naturally, not literally.
6. Use source context and string usages.
7. Preserve resource keys.
8. Preserve placeholders exactly.
9. Preserve formatting.
10. Preserve XML validity.
11. Respect existing project terminology.
12. Do not extract/refactor hardcoded strings unless explicitly requested.
13. Do not delete resources automatically.
14. In missing-only mode, do not rewrite existing translations.
15. Validate the resulting resources.
16. Provide a final localization report.

---

# 27. Preferred Interaction

The initial interaction should be short and decision-oriented.

Example:

```text
I found @LanguageDataSource with these language codes:

A. en, ur, es, ms
B. Select specific languages
C. Other — enter language code(s)

Which option?
```

After language selection:

```text
What should I localize?

A. Localize completely
B. Localize only missing strings/files

Which option?
```

Only after both decisions are confirmed should the skill begin the localization work.

---

# 28. Translation Mindset

Treat localization as **writing for a native user**, not translating source code.

Before producing a translation, mentally perform:

```text
Source meaning
    ↓
UI/context interpretation
    ↓
Target-language phrasing
    ↓
Native-language review
    ↓
Placeholder/format validation
    ↓
Android resource validation
```

The final translation should feel like it was written by a professional native-language product writer who understands the application.

Never optimize for literal similarity to the English source at the expense of natural language.

---

# 29. Scope Boundary

`hsg-localize` is responsible for:

* discovering localization languages,
* determining localization scope,
* translating Android localization resources,
* creating missing localization resources,
* preserving Android resource correctness,
* validating localization completeness.

`hsg-localize` is NOT responsible by default for:

* redesigning UI,
* extracting hardcoded strings,
* changing application architecture,
* changing Gradle configuration,
* changing locale detection logic,
* changing `@LanguageDataSource`,
* rewriting unrelated application code.

If such changes are required, stop and ask the user.
