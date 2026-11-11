# Assistant Revival

Android assistant project using Gemma 3 1B IT locally, llama.cpp, Java, C++/JNI, and selectable Java/OpenGL ES 3.0 rendering.

The GitHub Actions workflow downloads and verifies the GGUF model, packages it into the APK, and uploads the large ARM64 debug APK as an artifact.

Source project: `assistant-revival-source.zip`.


## One-time source upload

Upload the provided `assistant-revival-source.zip` file to the repository root (same level as this README). The workflow already downloads the Gemma GGUF, verifies its SHA-256, places it in `assistant-revival/models/`, packages it into the APK assets, and builds the APK. After the source ZIP is present, open **Actions → Build Assistant Revival APK → Run workflow**.
