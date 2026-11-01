# LightFit · 轻体 — Android (Java)

Low-pressure everyday health-recovery app. Habits before limits: a mood/body check-in
drives a personalized training plan, a posture tracker (self-check + on-device AI cam
scan) and a coach that works fully offline, in five languages.

**Status: v1.5** — feature-complete per the source spec; pending first on-device build
(see `BUILD-GUIDE.md`).

## Features

| Module | Details |
|---|---|
| Personalized plans | 5-level mood + 3-level body check-in; offline rules engine assets an easy (≤8 min) / standard / challenge plan, priority: neck → posture → shoulder → waist → back, max 6 moves |
| Plan player | Full-screen per-move countdown (start / pause / skip), auto-advance, taps exercise rows to launch; long-press edits the plan (remove a move, totals recompute) |
| Exercise library | 67 moves sourced from the LightFit web edition: 41 home + 26 gym (filter chips: all / neck repair / posture / gym); localized names + tips, detail dialog |
| Posture | 5-question self-check with score + recommendations; **AI camera scan** (CameraX + ML Kit pose detection, on-device bundled model): live skeleton overlay, shoulder-tilt & head-forward metrics, results stored encrypted only |
| AI health coach | Offline rule-based chat (off-topic safe fallback), greeting adapts to the latest check-in; nothing leaves the device |
| Reports | Week/month bar charts (self-drawn view, zero chart libs), sessions / minutes / active days / best streak |
| Profile | Nickname, height / weight → BMI (CN four-band), goal preset; persisted in encrypted store |
| Privacy & offline | Everything works with airplane mode on; all data AES-256-GCM (Android Keystore), prior zero-framework no-server design; clear-data wipes all local records |

## i18n

zh / en / ru / kk / ko — 402 keys per locale; UI restarts on switch; exercise data
falls back to English for ko until reviewed.

## Build & test (no build machine here — verified statically only)

- Open the folder in Android Studio (Hedgehog+); it provisions Gradle 8.2 + SDK 34
  and `Build > Build APK`. Foolproof steps: `BUILD-GUIDE.md`.
- On-device acceptance checklist: `TEST-PLAN.md`.
- Static cross-verification (XML well-formedness, all `@string/@drawable/@color/@style`
  refs closed, every `R.*` id resolves, braces balanced) — clean.

## Data sources

- LightFit web edition **v8.2** (source package) + **v8.4.3 demo** (behavioral spec): exercise
  catalog, check-in model, plan tiers.
- Product requirements: **LightFit Business Plan (En)** — five module lines (Emotion Fit /
  Posture Repair / Sedentary Recovery / Special Care / Home Services), 75% mainstream +
  25% special-care split, offline-first & >90% offline coverage targets.

## Anonymity policy

No personal names in code, data, metadata or git history. Commit identity:
**Kaito Ayano** only.
