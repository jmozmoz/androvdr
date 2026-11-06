# AndroVDR

AndroVDR is an Android remote control for the Linux-based [VDR](https://tvdr.de/).

This project is based on [grmpl/androvdr](https://github.com/grmpl/androvdr),
a fork of the original AndroVDR project by androvdr, released under the
GNU General Public License v2 (GPL-2.0).

## Build

The project has been migrated from the discontinued Eclipse ADT/Ant build to Gradle.  ChatGPT was heavily used for the update.
Open this directory in Android Studio (or build it with Gradle) using:

- JDK 17
- Android SDK Platform 36 and Build Tools installed by Android Studio
- Android Gradle Plugin 8.10.1

Run `gradle assembleDebug` to build the debug APK. The source tree remains in `src/` and resources in `res/` to minimize churn.

The app is compiled against Android 16 (API 36), targets Android 14 (API 34), and keeps a minimum SDK of 14 (Android 4.0) where the legacy support-v4 UI components still allow it. Targeting API 34 deliberately avoids Android 15+ forced edge-to-edge layout changes in the existing pre-edge-to-edge screens; the app still runs on Android 16 using platform compatibility behavior. Raise the target SDK only after adding and testing proper system-bar insets throughout the legacy activity and preference screens. Older Android releases below API 14 are not supported by this build configuration.

## Android compatibility changes

- Added a modern Gradle build and explicit dependencies for the libraries that were previously expected from an Eclipse workspace or local JARs.
- Updated notification creation for notification channels (Android 8+) and the runtime notification permission (Android 13+); PendingIntents are immutable on Android 6+.
- Removed the obsolete ACRA 4 crash-report dialog integration. It was tied to an obsolete reporting API and is not safe to carry forward unchanged.
- Moved app-owned files to the app-specific external-files directory. This works with scoped storage on Android 10+ and does not require broad storage access. Existing files in the old shared `/AndroVDR` directory may need to be copied into `Android/data/de.androvdr/files/AndroVDR` with a file manager. SSH key import currently scans the app-specific external-files root.
- Declared `android:exported` for the launcher and search activities as required when targeting Android 12+. Kept cleartext traffic enabled because VDR streamdev and VDRAdmin endpoints are commonly plain HTTP on the LAN.

## Features

- Remote control and views for channels, timers, recordings and EPG
- EPG search (requires the epgsearch plugin in VDR)
- Channel logos, adjustable text size and configurable layouts
- Gesture control and hardware-key volume control
- Multiple VDR devices and SSH-tunneled connections
- Wake-on-LAN and Live TV (requires streamdev and an external player)

## Compatibility notes

This is a modernization of an old application, not a complete UI rewrite. The legacy Android Support Library and platform `PreferenceActivity` are retained for the existing screens. The app should be exercised on real Android 14 and Android 16 devices, especially the remote screen layouts, SSH import, external player launch, and background SSH notification. The old shared-storage files are not automatically migrated because current Android versions restrict unprompted access to arbitrary shared-storage paths.

### Build compatibility notes

The project keeps legacy Java `switch` statements over `R.id` by setting `android.nonFinalResIds=false`. The `cm_remote` and `rec_remote` IDs are also declared centrally in `res/values/ids.xml`, because the Android 14+ menu variants omit those menu entries. For builds, select JDK 17 in Android Studio (Settings > Build Tools > Gradle > Gradle JDK); Java 25 may emit source/target 8 deprecation warnings.

### Android 14/16 startup crash fix

The old `com.mcxiaoke.viewpagerindicator:library:2.4.1` binary was removed. Its compiled styleable attribute indices are not reliable with current Android Gradle resource processing and caused `LinePageIndicator` to read a dimension as a color during layout inflation. AndroVDR now uses a small in-project `de.androvdr.widget.LinePageIndicator` implementation with the same basic page-indicator behavior (page display, selection updates, and tap-to-select).

The indicator also needs a bounded height in `res/layout/remote_pager.xml`. Using `wrap_content` with the default `View.onMeasure()` can make it consume the available vertical space, leaving the `ViewPager` with zero height. Setting an explicit height avoids this layout problem.