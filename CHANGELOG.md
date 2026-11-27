# Changelog

## Unreleased - UI polish (Impeccable design pass)

**Scrcpy Flow (Windows)**
- Home: the Start mirroring button turns into Stop mirroring while the selected phone is live
- The Activity log moved off Home to its own Activity page (sidebar), with a full-height log and a Copy button
- Themes: Settings > Appearance > Theme, with a colour preview and an Apply button. Eight themes: Midnight (default), Twilight, Sky, Lavender, Khaki, Cream, Sky Light and Lavender Cream
- Design system documented with atomic design (atoms, molecules, organisms, template, pages) in DESIGN.md; UI resources grouped the same way
- Fixed missing spaces in the pairing steps and Game Mode tips
- Clearer text: larger body text (Fluent type ramp), sentence-case field labels, stronger contrast for hints and labels
- Buttons: deeper blue-violet fill so white labels are easy to read; soft shadow instead of a pulsing glow
- Keyboard focus ring on buttons, switches, menus and sliders; rounded tooltips that wrap
- Calmer motion: shorter transitions, no bounce, lists only animate new rows, follows the Windows "Animation effects" setting
- Busy screen gives an honest time estimate for long tasks (building the phone app)
- Removed gradient title text, the sidebar "MENU" label and the side bar on the selected menu item
- "Other ways to add a phone" uses columns instead of boxes inside a box
- Saved phones no longer repeat the model when it is the same as the name

**Flow Connect 1.4**
- Solid headings instead of gradient text
- Button fills with readable white labels
- All touch targets at least 48 dp
- Faster, smoother animations without overshoot
- More readable version text

## 1.0.0 - first public version

**Scrcpy Flow (Windows)**
- Dark, animated UI: Home, Devices, Game Mode, Recordings, Settings
- Wireless pairing with a QR code (a new random code every time) or a pairing code
- Saved phones with their own profiles; auto-reconnect when a phone comes back on Wi-Fi
- Profiles: Balanced, Best quality, Smooth (Wi-Fi), Gaming, plus your own
- Game Mode: high FPS, physical keyboard / mouse / controller (UHID), virtual display
- Recording (MP4 / MKV, background recording) and screenshots
- Optional animation add-ons (XamlFlair, LottieSharp)

**Flow Connect (Android 11+)**
- One-tap Wireless debugging, quick settings tile, keep-on after restarts
- Start mirroring on the PC from the phone (phone screen or second screen)
- Developer mode switch (Developer options, USB debugging, animation speed)
