---
name: hsc-review-security
description: Hypersoft Claude Skill: Review Android code for secrets, exported components, permissions, logging leaks, and R8 keep rules. Use when security review, release prep, or auditing sensitive data handling. Prefer hsc-review-complete for a full multi-check pass.
---

# Security Review

Follow `.claude/rules/14-security-secrets.md`, `10-manifest.md`, `21-ads-billing.md`, `16-logging.md`.

## Secrets
- [ ] No API keys, tokens, keystore passwords in source, Gradle, or docs
- [ ] `local.properties` / CI env for secrets
- [ ] Debug uses sample ad/billing IDs — not production
- [ ] No secrets in logs (`16-logging`)

## Manifest
- [ ] `exported="false"` default for non-launcher components
- [ ] Intent-filters always have explicit `exported`
- [ ] Custom actions namespaced with `applicationId`
- [ ] Minimum permissions declared

## Data
- [ ] HTTPS for remote endpoints
- [ ] No PII/tokens in Analytics or Log calls
- [ ] External input validated before use (deep links, nav args)

## Storage
- [ ] Sensitive prefs encrypted if required by product
- [ ] No world-readable files

## Build
- [ ] Release R8 enabled on `:app` (`optimization { enable = true }` — code **and** resource shrinking)
- [ ] Signing credentials not hardcoded in `build.gradle.kts`
- [ ] Keep rules in `src/main/keepRules/*.keep` only what’s needed (no `proguard-rules.pro`)

## Report

Number every actionable finding. Follow [fix-selection.md](../fix-selection.md) after the report.

```markdown
## Fix list
1. [Critical] leaked secret / exported receiver / logged token
2. [High] missing runtime permission UX / cleartext traffic
3. [Medium] over-broad permissions
```

Never paste actual secrets into the review report — reference file/line only.

After the report: **do not fix yet** — ask which numbers to fix per `fix-selection.md` (e.g. user replies `fix 1, 2, 4, 7`).
