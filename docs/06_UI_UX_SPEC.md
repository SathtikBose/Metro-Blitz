# 06 — UI / UX Specification

Principle: **the game is the focus.** Only the UI that is needed: pause button, score, coins. Everything else appears only in menus and popups.

## 1. Visual style
- Colours from the palette in `04`. UI accent = Sunny Yellow `#FFD93D` buttons with Deep Navy `#1B1F3B` text and 3 dp navy outline; panels = white `#FFFFFF` at 95% with 20 dp rounded corners.
- Font: a bundled rounded OFL font (e.g., "Baloo 2" or "Fredoka") — put in `res/font/`, add to `CREDITS.md`. Fallback: system sans-serif bold.
- Text sizes: HUD score 28 sp, coins 22 sp, buttons 20 sp, title 40 sp. Minimum 16 sp.
- Touch targets ≥ 48 dp. Safe-area padding for notches (use window insets).

## 2. Screens

### 2.1 Loading (≤ 1.5 s)
Sky-blue background, text "METRO BLITZ" and a small spinner. No progress bar needed.

### 2.2 Menu (character on screen)
- 3D scene visible: track, buildings, Blitz in `Idle` animation, camera close.
- Top centre: game title logo text "METRO BLITZ" (title style, white with navy outline).
- Bottom centre: **"TAP TO PLAY"** pulsing (scale 1.0→1.08, 1 s loop).
- Top right: small sound toggle icon button. Bottom left small text: best score `BEST 1234`; bottom right: coin icon + total coins.
- **Tap anywhere** (not on the toggle) starts the game. Transition: camera pulls back to the gameplay position over 0.5 s while Blitz begins `Run`. Menu texts fade out in 0.2 s.

### 2.3 HUD (while playing)
| Element | Position | Content |
|---------|----------|---------|
| Pause button | Top right, 48 dp round, two bars icon | Opens pause menu |
| Score | Top left | Number, e.g. `1260` (small label "SCORE") |
| Coins | Top centre-left under score or next to it | Coin icon + count |
Nothing else. Semi-transparent navy backing pill behind text for readability. Debug builds also show FPS in bottom-left.

### 2.4 Pause menu (popup over frozen game, dim 50% navy)
Title "PAUSED". Buttons stacked: **Resume** (primary), **Restart**, **Home**. Small toggles row: Sound, Music, Vibration.
- Resume → Countdown screen.
- Restart → new run immediately (no countdown).
- Home → menu.

### 2.5 Resume countdown
Game frozen and visible. Huge centred number **3 → 2 → 1** (1 s each) with scale-pop animation each second, text "GET READY" above. Tapping does nothing. Pause button hidden. After 1 the game resumes at the exact state. Total 3 s. (If the app is backgrounded during countdown it returns to PAUSED.)

### 2.6 Game over popup (after 0.8 s hit animation)
Panel: title "OOPS!" (friendly), rows: Score, Best (with "NEW BEST!" ribbon if beaten), Coins collected this run. Buttons: **PLAY AGAIN** (primary, large), **Home**. Popup slides up 0.25 s. Play Again starts a fresh run immediately.

### 2.7 Error screen
Friendly message + Retry button if assets fail to load.

## 3. Flow diagram
```
Loading → Menu --tap--> Playing <--> Paused → Countdown → Playing
                          │ hit
                          ▼
                     Game Over --Play Again--> Playing
                          └--Home--> Menu
```

## 4. Feedback and juice (cheap but important)
Coin pickup pop + sound · small camera shake on hit · lane-change whoosh · landing dust puff (a few cubes, optional) · speed lines at high speed (optional, low alpha) · button press scale 0.95.

## 5. Gestures
Swipe anywhere on the 3D view. Swipe must be recognised on the first movement past threshold so the game feels instant. No on-screen tutorial text except a one-time hint on first launch for 3 s on the first run: "Swipe ← → ↑ ↓" shown as faint arrows (saved flag `hint_shown`).

## 6. Accessibility and comfort
High contrast text · sound/vibration toggles · no strobing · colour is never the only signal (obstacle shapes differ: barricade low, beam high, train big) · works on 360–440 dp wide screens · screen stays on during play.

## 7. Copy (exact strings)
`METRO BLITZ`, `TAP TO PLAY`, `PAUSED`, `Resume`, `Restart`, `Home`, `GET READY`, `OOPS!`, `Score`, `Best`, `Coins`, `NEW BEST!`, `PLAY AGAIN`, `Could not load game assets`, `Retry`. Strings go in `strings.xml`.

## 8. App icon and splash
Icon: Blitz's face with cap on a Sky Blue circle, rendered from Blender (1024×1024), adaptive icon foreground/background. Splash: Android 12 splash API with Sky Blue background and the icon.
