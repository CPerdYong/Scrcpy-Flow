# Scrcpy Flow design system

Scrcpy Flow (Windows, WPF) and Flow Connect (Android) are built with **atomic design**: small tokens (atoms) combine into controls (molecules), controls into sections (organisms), sections into a page layout (template), and the layout is filled with real content (pages).

Where it lives:

- Windows: the `<Window.Resources>` block in `scrcpy-flow.ps1` is split into ATOMS, MOLECULES and ORGANISMS sections. Rows and badges made in code use the builders listed under "Atomic design builders".
- Android: `UiKit.java` holds the atoms and small parts; `MainActivity.java` builds the organisms and the page.

Both apps are **Operate** surfaces: people come to connect a phone and start mirroring. Clarity, familiar controls and fast feedback come before decoration.

---

## 1. Atoms

### Colour tokens (Midnight, the default theme)

| Token | Value | Role |
|---|---|---|
| `BrBg` | `#0F1115` | Window background |
| `GrCard` | `#1C212B` to `#161A21` | Card surface |
| `BrCard2` | `#20252F` | Button and control surface |
| `BrField` | `#11141A` | Input background |
| `BrLine` | `#2A303B` | Borders, dividers |
| `BrText` | `#E8EAED` | Primary text |
| `BrSub` | `#A3ACB9` | Hints, metadata |
| Label | `#B7BFCB` | Field labels |
| `BrAccent` | `#5B8CFF` | Icons, links, focus, busy status |
| `GrAccent` | `#3F6BE6` to `#7A4FF0` | Primary button fill |
| `BrGood` / `BrWarn` / `BrBad` | `#3DDC97` / `#FFB547` / `#FF5C6C` | Status only |
| On accent | `#FFFFFE` | Text on the accent fill |
| On hero | `#FFFFFD` | Text on the Home and Game Mode hero cards |
| Page title / subtitle | `#FFFFFC` / `#FFFFFB` | Text that sits directly on the window background |

Rules: the accent marks the primary action, the current selection and state, never decoration. No gradient text.

### Type tokens

Windows: Segoe UI Variable. Android: system sans, sizes in sp.

| Role | Windows | Android |
|---|---|---|
| Page title | 28 SemiBold | 28 |
| Card heading (`H2`) | 16 SemiBold | 18 |
| Body | 14 | 14 to 16 |
| Hint (`Hint`) | 13, line height 19, max width 760 | 13 |
| Field label (`Label`) | 12 SemiBold, sentence case | n/a |
| Caption | 12 | 12 |

### Shape, space, depth, motion, icons

- Radius: controls 9, cards 14 (Android 22 dp), badges 7, hero 18.
- Space: page padding 32, card padding 20, gap between cards 16, label gap 14 above and 6 below.
- Depth: the main action buttons have a soft shadow with a 4 px downward offset. No glowing halos.
- Motion: 150 to 250 ms with an ease-out curve, no bounce. Page change: 200 ms fade and 8 px rise. Off when Windows "Animation effects" is off.
- Icons: Segoe Fluent Icons on Windows, vector drawables on Android. No emoji.

## 2. Molecules

| Molecule | Windows style / builder | Notes |
|---|---|---|
| Primary button | `AccentBtn`, `BigAccentBtn` | Accent fill, on-accent text |
| Secondary button | `BtnBase` | Control surface, 1 px line |
| Quiet button | `GhostBtn`, `IconBtn`, `New-IconBtn` | No fill until hover |
| Danger button | `DangerBtn` | Red tint |
| Switch | implicit `CheckBox` style | Knob slides 200 ms |
| Select | implicit `ComboBox` style | 36 px tall |
| Text field | implicit `TextBox` style | Focus turns the border accent |
| Slider | implicit `Slider` style | Accent track |
| Nav item | `NavBtn` | Tinted background when selected, no side bar |
| Badge | `New-Badge` | Colour at 15% behind the label |
| Status dot | `Set-DotColor` | 8 px circle |
| Icon circle | `New-IconCircle` | 42 px circle with a status dot |
| Focus ring | `FocusRing` | 2 px outline, keyboard only |
| Tooltip | implicit `ToolTip` style | Rounded, wraps at 320 |

Every molecule has default, hover, pressed, disabled and keyboard focus states. Android touch targets are at least 48 dp.

## 3. Organisms

- **Card** (`CardBox`): heading, optional hint, content. Never put a card inside a card; group with spacing or a divider line.
- **Hero** (Home, Game Mode): phone identity, stat chips, the main action.
- **Device row** (`New-Row` in `Render-Saved`, `Render-Detected`): icon circle, name and details, actions.
- **Session row** (`Add-SessionRow`): live phone, REC badge, timer, Stop.
- **Pairing card**: QR slot, numbered steps, Show QR code.
- **Sidebar**: logo, nav items, adb and mirroring status.
- **Feedback**: toast (short messages), busy overlay (long tasks, with a realistic time estimate), dialog (decisions only).

## 4. Template

Sidebar (236 px) on the left. The main area has a page header (title, subtitle, status pill) and a scrolling page with 32 px padding, made of stacked cards. On Android: header, power button, status, then a stack of cards.

## 5. Pages

Home, Devices, Game Mode, Recordings, Settings (Windows). Flow Connect has one main screen.

---

## Themes

Settings > Appearance > Theme. The app restarts to switch.

| Theme | Kind | Palette (shown as squares in Settings) |
|---|---|---|
| Midnight | dark | `#0F1115` `#1C212B` `#3F6BE6` `#7A4FF0` (the tables above) |
| Twilight | dark, flat | `#453C67` `#6D67E4` `#46C2CB` `#F2F7A1` |
| Sky | blue, flat | `#2373F4` `#578EF5` `#65D0F4` `#F2F7A0`: blue background, yellow buttons |
| Lavender | mid, flat | `#898AC4` `#A2AADB` `#C0C9EE` `#FFF2E0`: lavender background, cream buttons |
| Khaki | light | `#EDE7D6` `#F8F4EA` `#556B2F` `#7B5E34` |
| Cream | light, flat | `#FFF2C6` `#FFF8DE` `#AAC4F5` `#8CA9FF` |
| Sky Light | light, flat | `#F2F7A0` `#65D0F4` `#578EF5` `#2373F4`: yellow background, blue buttons |
| Lavender Cream | light, flat | `#FFF2E0` `#C0C9EE` `#A2AADB` `#898AC4`: cream background, lavender buttons |

Settings > Appearance: pick a theme, check the colour squares, then press Apply (the app restarts).

How it works: the UI is written in Midnight colours. A theme is a map from every Midnight colour to its replacement (`$Themes` in `scrcpy-flow.ps1`). The map is applied to the XAML before the window is built, and to every colour the code creates (`Map-Color`). Mapping both ends of a gradient to the same value makes it flat. To add a theme, copy a map and change the values; every Midnight colour needs an entry.

Text contrast is at least 4.5:1 in every theme. Where a palette colour was a little too light for white text, the button uses a slightly deeper shade (for example `#1F69E6` instead of `#2373F4`).
