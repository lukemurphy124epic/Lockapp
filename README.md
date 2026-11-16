# Dumbphone Lock — easy-override prototype

Android Studio Kotlin project. No root, factory reset, Device Owner, or Device Administrator required.

## Build and install

1. Open this folder in a recent Android Studio with Android SDK 35 installed.
2. Let Gradle sync. If prompted, install SDK/build tools.
3. Connect your Samsung Android phone via USB with USB debugging enabled, or use an emulator.
4. Run the `app` configuration (or Build > Build APK(s), then install the APK).
5. Open Dumbphone Lock, select start/end times and days, optionally allow messaging apps, tick Enable, and tap Save.
6. Tap Open Accessibility settings, find 'Dumbphone Lock app blocking' under Installed apps, and enable it. Android may ask you to explicitly allow restricted settings for a sideloaded app via Settings > Apps > Dumbphone Lock > menu > Allow restricted settings. Only do this if you trust this locally built code.

## How it works

An AccessibilityService watches the package name of the active window (not the screen content). During chosen hours, a non-allowlisted app is sent back to Home. It also checks every five seconds to catch a schedule starting while an app is already open. It does not read text, messages, passwords or window content.

Default allowed: launcher, System UI, Settings, this app, role-holding dialer/SMS apps, Android phone service and common dialers. Other messaging apps must be selected in the app list. Some OEM-specific calling, emergency, or messaging components may need to be added to the allowlist if the prototype interferes with them; test calls and messages before relying on it. Settings remains accessible for safe recovery.

Overnight schedules work: Monday 22:00–Tuesday 07:00 is selected by checking Monday. Start and end at the same time means disabled.

## How to override

Tap Pause for 15 minutes, uncheck Enable and save, or go to Android Settings > Accessibility > Installed apps > Dumbphone Lock app blocking and turn it off. You can uninstall the app normally.

## Known limitations

- This is an experimental prototype, not an APK; compilation and on-device behavior have not been verified here.
- It redirects apps to Home; it does not suspend or uninstall apps, and a determined user can bypass it.
- The app list only shows apps exposing a launcher activity. Some system apps and deep links may behave differently.
- Android/Samsung may interrupt accessibility services or change system package names. Verify functionality after system updates.
- A brief flash of a blocked app may appear before redirecting.
- No automatic alarm, background daemon, or Device Owner privileges are used.

## Build entirely on your phone using GitHub Actions

1. Sign in to github.com in your phone browser, and create a new **private** repository (for example `DumbphoneLock`). Do not add a README during repository creation.
2. Upload the **contents** of this `DumbphoneLock` folder to the repository root, preserving `.github/workflows/build-apk.yml`. GitHub's mobile web uploader can be awkward for folders/hidden files; use desktop-site mode or upload files through a mobile Git client if needed. **Do not upload the ZIP alone**: Actions needs the actual project files.
3. Open the repository's **Actions** tab, choose **Build Android APK**, and tap **Run workflow** (or push to `main` to trigger automatically).
4. When the run completes, open the run, scroll to **Artifacts**, and download `DumbphoneLock-debug-apk` (a ZIP containing `app-debug.apk`). Extract it on the phone.
5. Tap `app-debug.apk` to install. Android may require allowing installation from your browser/file manager. Then open the app, set hours/days, save, and enable the Accessibility service.

This workflow builds a **debug-signed** APK for personal testing, not a Play Store release. GitHub Actions runs may fail due to changes in Android build tooling; check the build log if so. Never publish a private signing key to GitHub.
