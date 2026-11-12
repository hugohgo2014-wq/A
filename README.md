# Ren'Py Studio Mobile
Native Android visual-novel authoring app with a C++17 story compiler and no WebView.

## Included
- Scene stage, event-sheet editor, asset library, playtest dialogue viewer and ZIP export.
- Visual blocks: dialogue, background, single sprite, three-PNG layered character, if/else/end-if, variable assignment, two-option choice, music, sound, transitions, wait, hide and end.
- Android document picker for importing image/audio assets.
- Exported `script.rpy`, `images/`, `audio/` and instructions for extraction into an existing Ren'Py `game/` directory.

## Build
GitHub Actions uses JDK 17, Gradle 8.9, Android platform 35, NDK 27.2.12479018 and CMake 3.22.1.
Run: `gradle --no-daemon :app:assembleDebug`
APK: `app/build/outputs/apk/debug/app-debug.apk`

## Scope
This is a functional first implementation of the editor foundation. Advanced visual screen-language authoring, nested event groups, full runtime emulation, and branch graph editing remain future work. The preview currently plays dialogue and choice text; it is not the Ren'Py runtime.
