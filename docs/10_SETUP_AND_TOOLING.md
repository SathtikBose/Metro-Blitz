# 10 — Setup and Tooling (Windows) + Beginner Glossary

You do NOT need to know Android or game development. Install the tools below once, then the agent does the rest.

## 1. Install list
| Tool | Why | Check command |
|------|-----|---------------|
| **Android Studio** (latest stable) | Android SDK, emulator, JDK, project wizard | open it once, finish the setup wizard |
| Android SDK Platform (latest stable) + Platform-Tools + Build-Tools | build + `adb` | SDK Manager |
| **Git** | version control | `git --version` |
| **Node.js LTS** | runs `npx` based MCP servers | `node -v` |
| **uv** (Python tool runner) | runs the Blender MCP via `uvx` | `uv --version` |
| **Blender** (latest stable 4.x) | 3D assets | `blender --version` or open the app |
| **Antigravity 2.0** | the AI agent | open the app and sign in |

## 2. Environment variables (Windows → "Edit environment variables")
- `ANDROID_HOME` = `C:\Users\<you>\AppData\Local\Android\Sdk`
- `JAVA_HOME` = Android Studio's bundled JDK, usually `C:\Program Files\Android\Android Studio\jbr`
- Add to `Path`: `%ANDROID_HOME%\platform-tools`
- Restart the terminal / Antigravity, then check: `java -version`, `adb version`.

## 3. Create the project (one time, 2 minutes)
Android Studio → New Project → **Empty Activity** (Compose) → Name **Metro Blitz**, Package **com.buildstack.metroblitz**, Language Kotlin, Minimum SDK **API 26**, Build config language Kotlin DSL. Save in a folder, e.g. `D:\Projects\MetroBlitz`. Let Gradle sync finish. Then create the folder `docs` inside the project and copy all `.md` files from the pack into it; copy `AGENTS.md` and the `.agents` folder to the project root. Open that project folder in Antigravity.
Why manual: the Gradle wrapper files are binary and are created reliably by the Android Studio wizard.

## 4. Run on a real phone (recommended, best performance and best for video)
1. Phone: Settings → About phone → tap **Build number** 7 times → Developer options → enable **USB debugging**.
2. Connect with a USB cable, accept the "Allow USB debugging" prompt.
3. Terminal: `adb devices` → should show your device as `device` (not `unauthorized`).
4. Optional wireless: Developer options → Wireless debugging.
For video screencast: install **scrcpy** (`winget install Genymobile.scrcpy`) and run `scrcpy` to mirror the phone on your PC.

## 5. Run on the emulator (backup)
Android Studio → Device Manager → Create device → Pixel 8 → system image (latest stable, x86_64) → Graphics: **Hardware**. Needs virtualization enabled in BIOS. 3D can be slower than a real phone; do not judge performance on it.

## 6. MCP servers for Antigravity
Open Antigravity → Agent pane → "…" menu → **MCP Servers → Manage MCP Servers → View raw config** (file `~/.gemini/antigravity/mcp_config.json`). Add (Windows form with `cmd /c`):
```json
{
  "mcpServers": {
    "blender": { "command": "cmd", "args": ["/c", "uvx", "blender-mcp"] },
    "context7": { "command": "cmd", "args": ["/c", "npx", "-y", "@upstash/context7-mcp@latest"] },
    "mobile-mcp": { "command": "cmd", "args": ["/c", "npx", "-y", "@mobilenext/mobile-mcp@latest"] }
  }
}
```
Save, reload the MCP panel, check each server shows its tools.
- **blender**: if the package name fails, try the renamed package `mcp-for-blender`. Verify the repo README for the current name.
- **context7**: gives the agent up-to-date library docs (stops it guessing SceneView/Compose APIs).
- **mobile-mcp**: lets the agent take screenshots and tap/swipe on the emulator or phone.
- Optional: `sceneview-mcp` (`npx sceneview-mcp`) for SceneView-specific help.

### Blender side
1. Download the MCP addon file `addon.py` from the Blender MCP repository you chose.
2. Blender → Edit → Preferences → Add-ons → Install from disk → pick `addon.py` → tick to enable.
3. In the 3D viewport press **N** → tab **BlenderMCP** → **Start server** (leave Blender open).
4. Leave the PolyHaven / Sketchfab / Hyper3D / Hunyuan options **off** (not used in this project).

## 7. Antigravity settings worth checking
- Model: Gemini 3.8 Flash with high thinking (or the strongest model your plan has).
- Terminal permission: allow the agent to run terminal commands automatically for `gradlew`, `adb`, `git`; keep confirmation for deleting files outside the project.
- Rules: `AGENTS.md` at the project root is read as a workspace rule (rule files have a 12 000-character limit; ours is shorter). Skills in `.agents/skills/` and the workflow `/phase-gate` in `.agents/workflows/` are included in the pack.
- Open the **docs** folder in the workspace so `@docs/...` mentions work.

## 8. Troubleshooting
| Symptom | Fix |
|---------|-----|
| `adb` not recognised | Add platform-tools to Path; restart terminal |
| `adb devices` shows `unauthorized` | Unplug, revoke USB debugging authorizations, replug, accept prompt |
| Gradle: "JAVA_HOME is not set" / wrong Java | Point `JAVA_HOME` to Android Studio `jbr` |
| Gradle sync fails offline | Connect to internet for first sync; dependencies download once |
| App closes immediately on emulator | Use a real phone or set emulator graphics to Hardware; check `adb logcat` |
| Black 3D screen | Device lacks OpenGL ES 3.0, or model path wrong; check logcat tag `MetroBlitz` |
| Blender MCP "connection refused" | In Blender press Start server; check port 9876 not blocked |
| `uvx`/`npx` not found in Antigravity | Restart Antigravity after installing; use full path in config |
| Antigravity stops mid-phase | Say: "Continue from docs/PROGRESS.md" |
| Agent changes too much | Remind: "Follow AGENTS.md. Only this phase." |
| Model stuck in a loop re-reading files | Start a new conversation and use the "resume" prompt from `ANTIGRAVITY_PROMPT.md` |

## 9. Beginner glossary
**Android:** *Activity* a screen host; *Composable* a UI building block in Compose; *Gradle* the build tool; *APK* the installable app file; *AAB* Play Store bundle; *SDK* Android developer kit; *ADB* command line bridge to a phone; *Manifest* app settings file; *ViewModel* holds screen state; *StateFlow* observable state; *DataStore* small saved settings; *R8* shrinker for release builds.
**Game dev:** *Game loop* code that runs every frame; *delta time (dt)* seconds since the last frame; *spawn* create an object in the world; *object pool* reuse objects instead of creating new ones; *hit box* invisible box used for collisions; *AABB* axis-aligned box; *lane* one of 3 tracks; *tier* difficulty level; *seed* number that makes random results repeatable; *FPS* frames per second; *jank* stutter; *pop-in* objects suddenly appearing; *fog* distance fade that hides pop-in.
**3D:** *Mesh* shape; *triangle count* complexity; *material* colour/surface; *rig/armature* bone skeleton; *skinning* binding mesh to bones; *keyframe* saved pose in time; *clip/action* one animation; *glTF / GLB* standard 3D file format; *pivot* the origin point of a model; *draw call* one batch sent to the GPU (fewer is faster).
