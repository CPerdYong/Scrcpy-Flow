# Scrcpy Flow design system

The look of Scrcpy Flow (Windows, WPF) and Flow Connect (Android). Read this before changing the UI. The source of truth is the `<Window.Resources>` block in `scrcpy-flow.ps1` and `UiKit.java`.

## Mode

Both apps are **Operate** surfaces: people come to connect a phone and start mirroring. Clarity, familiar controls and fast feedback come before decoration. The brand shows in the details: the blue-violet gradient, the dark room-friendly theme and the power button on the phone.

## Colour

| Role | Value | Use |
|---|---|---|
| Background | `#0F1115` | Window and app background |
| Surface | `#1C212B` to `#161A21` (gradient) | Cards |
| Line | `#2A303B` | Card borders, dividers |
| Text | `#E8EAED` | Primary text |
| Secondary text | `#A3ACB9` | Hints, metadata (7:1 on cards) |
| Field label | `#B7BFCB` | Labels above inputs |
| Accent | `#5B8CFF` | Links, icons, focus, status on dark |
| Accent fill | `#3F6BE6` to `#7A4FF0` | Primary buttons (white text passes 4.5:1) |
| Good / Warn / Bad | `#3DDC97` / `#FFB547` / `#FF5C6C` | Status only, never decoration |

Rules:

- The accent is for the primary action, the current selection and state. Inactive things stay neutral.
- Never put white text on the light accent `#5B8CFF`; use the accent fill gradient.
- No gradient text. Emphasis comes from weight and size.

## Type

Windows: Segoe UI Variable (Fluent ramp). Android: system sans (Roboto), sizes in sp.

| Role | Windows | Android |
|---|---|---|
| Page title | 28 SemiBold | 28 |
| Card heading | 16 SemiBold | 18 |
| Body | 14 | 14 to 16 |
| Hint / metadata | 13, line height 19, max width 760 | 13 |
| Field label | 12 SemiBold, sentence case | n/a |
| Caption | 12 | 12 |

Field labels are sentence case ("Frame rate"), not all caps. There are no eyebrow labels above headings.

## Shape and depth

- Cards: radius 14 (Windows) / 22 dp (Android), 1 px border, no nested cards. Group inside a card with spacing or a divider line.
- Controls: radius 9. Pills and badges: radius 7.
- The main action buttons (Start mirroring, Launch Game Mode) have a soft shadow with a real downward offset. No glowing halos.

## Motion

- Routine changes take 150 to 250 ms with an ease-out curve (CubicEase or Decelerate). No bounce or overshoot.
- Page change: 200 ms fade and 8 px rise. Lists animate only rows that are new.
- The phone's power button entrance (420 ms) is the one authored moment.
- Windows: animations switch off when Settings > Accessibility > Visual effects > Animation effects is off. Android: the system animation scale applies automatically.

## Interaction

- Every control has hover, pressed, disabled and keyboard focus states. The focus ring is a 2 px `#8FB0FF` outline that appears only for keyboard users.
- Android touch targets are at least 48 dp.
- Long tasks show a realistic time estimate in the busy screen; short ones say "This can take a few seconds."
- Write in plain words. Buttons name their action ("Show QR code", "Pair and connect"). Errors say what went wrong and what to do next.
