# 12 — Release Checklist (Phase 8)

## 1. Product polish
- [ ] Sound, music, vibration toggles work and persist
- [ ] App icon (Blender render) and splash screen
- [ ] App name "Metro Blitz" everywhere; version name `1.0.0`, version code `1`
- [ ] No debug overlay, invincibility or keyboard input in release
- [ ] First-run hint shows once
- [ ] All strings in `strings.xml`, no hard-coded UI text
- [ ] Credits file lists every font and sound with licence

## 2. Technical
- [ ] `assembleRelease` with R8 passes; model loading, DataStore, SceneView verified on the release APK
- [ ] Permissions: `VIBRATE` only; no INTERNET
- [ ] `android:allowBackup` decided (false is fine), `android:screenOrientation="portrait"`
- [ ] `lintRelease` has no errors
- [ ] Release APK < 40 MB
- [ ] 10-minute soak passes on the release build
- [ ] Full regression (all bundles) green

## 3. Signing (college build)
Debug-signed or a self-created keystore is enough for sharing the APK. Create a keystore only if you plan to publish:
`keytool -genkey -v -keystore metroblitz.jks -keyalg RSA -keysize 2048 -validity 10000 -alias metroblitz`. **Never commit the keystore or passwords.** Keep them outside the repo.

## 4. Optional Google Play notes (not required for the project)
Target API must meet Play's current requirement; produce an `.aab`; add privacy policy statement "This game does not collect or share any data"; content rating questionnaire (everyone); Families policy if targeting children (no ads/analytics already satisfies most points); store listing needs 512×512 icon, feature graphic 1024×500, 2–8 screenshots (portrait).

## 5. Repository and docs
- [ ] `README.md`: description, GIF/screenshots, features, controls, architecture overview, build steps (Windows), how the assets were made (Blender MCP), credits, licence
- [ ] `docs/PROGRESS.md` final state, known issues listed honestly
- [ ] Tags `phase-0`…`phase-8`, `v0.1.0`, `v0.2.0`, `v0.3.0`, `v1.0.0` exist
- [ ] Clean clone builds (REL-10)
- [ ] Blender `.blend` sources and scripts committed

## 6. Demo / submission package
APK · short demo video (60–90 s screen recording) · screenshots · architecture diagram (one image) · test report (copy of PROGRESS.md test section) · source zip or repo link · the YouTube video link.

## 7. Final sign-off questions
Would a stranger understand the controls in 10 seconds? · Is the first minute fun, not boring? · Is a death ever unfair? · Is there anything scary/unsuitable for kids? · Does it still run smoothly after 10 minutes?
