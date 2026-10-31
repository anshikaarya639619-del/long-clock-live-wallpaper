# DepthPix Pro — original Android MVP

Offline-first photo wallpaper editor. No proprietary DepthPix code, assets, or branding are copied.

## Implemented
- Import a photo using Android's system photo picker.
- Touch-driven 2.5D parallax preview with adjustable intensity.
- Clock/date overlay with four color styles.
- Optional custom text overlay.
- Export composed wallpaper as PNG using Android save dialog.
- No paid API, account, subscription, or API key.

## Limitations
Parallax is whole-image touch movement, not AI depth segmentation or separate foreground/background layers. This MVP exports static PNG; live wallpaper service, online catalog, favorites/history, rain/snow effects, and true depth masking are not yet implemented. APK is a debug build, not a Play Store-signed release.

## APK
Run GitHub Actions workflow Build DepthPix Pro APK via workflow_dispatch. Download artifact depthpix-pro-debug-apk, extract app-debug.apk, and install on Android.
