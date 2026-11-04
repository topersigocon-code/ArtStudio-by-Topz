# ArtStudio by Topz

A native, offline-first Android digital drawing studio foundation.

## Current milestone
- Native Kotlin Android application
- Android 10+ (minSdk 29)
- Artwork gallery
- 1600 x 1600 transparent drawing canvas
- Brush and eraser
- Basic color swatches
- Undo/redo history
- PNG save and reopen
- Custom navy-and-gold launcher icon

## Build
Use JDK 17 and Android SDK platform 35. From the project root:

```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Notes
This is the first permanent application foundation, not a throwaway prototype. The initial drawing engine is intentionally a functional baseline. Advanced pressure dynamics, layers, PSD, animation, GPU/tile rendering, and full project serialization are planned as subsequent production milestones.
