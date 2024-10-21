# Android-PWA-Wrapper

An Android Wrapper application to create native Android Apps from an offline-capable Progressive Web App.

## What has been done
- ✅ Get Android Studio 3.4+
- ✅ Clone/fork repository
- ✅ Put your Web App's URL in _WEBAPP_URL_ in `Constants.java`
- ✅ Replace *app_name* in `strings.xml` with the name of your App
- ✅ Add your own primary colors to `colors.xml` (*colorPrimary, colorPrimaryDark, colorPrimaryLight*)
- ✅ Put your own icons in place:
  - ✅ Add your own _ic_launcher.png_ and _ic_launcher_round.png_ in the `mipmap-...` folders
  - ✅ Add your own _ic_appbar.png_ in the `drawables-...` folders. This is displayed in Android's _Recent Apps_ View on your app bar, so it should look nicely when placed on top of your primary color.
  - ✅ I recommend using [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio) to get the icons ready in no time
- ✅ Change the package name in `app/build.gradle`, *applicationId*
- ✅ Change `AndroidManifest.xml` -> `aplication` -> `activity` -> `intent-filter` to your own URLs/schemes/patterns/etc. or remove the `intent-filter` for `android.intent.action.VIEW` altogether
- ✅ Check `Constants.java` for more options
- ✅ Build App in Android Studio
- ✅ Updated gradle and AndroidX

## License
[GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html) - if you use it, we wanna see it!
