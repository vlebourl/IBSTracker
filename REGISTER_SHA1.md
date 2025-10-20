# Register Production SHA-1 with Google Cloud Console

## Your New Production SHA-1 Fingerprint

```
C8:BB:E7:2D:CA:F7:F6:91:0D:6A:9E:74:B2:1E:41:CE:2D:35:60:AF
```

**Previous (old keystore) SHA-1**: `DD:DA:AC:4F:AB:46:73:AE:56:C9:FB:CD:68:8E:EF:90:15:21:B4:79`

---

## Step-by-Step Registration

### 1. Go to Google Cloud Console

Open: https://console.cloud.google.com/apis/credentials?project=ibs-tracker-475708

### 2. Find Your Android OAuth 2.0 Client ID

Look for the Android client with ID: `896856261645-5urcjd1287rrctptp9lri9ju9ieje2r5.apps.googleusercontent.com`

**Note**: You also have a Web client: `896856261645-e2g6l6k1khelsl3kjji7d011oqdh5c34.apps.googleusercontent.com` (used in strings.xml)

### 3. Edit the Client ID

Click on the pencil icon (Edit) next to the Android client ID

### 4. Add the New SHA-1 Fingerprint

In the "SHA-1 certificate fingerprint" section:
- **Keep the old SHA-1** (for now, so existing installs still work)
- Click "Add fingerprint"
- Paste the new SHA-1: `C8:BB:E7:2D:CA:F7:F6:91:0D:6A:9E:74:B2:1E:41:CE:2D:35:60:AF`

You should now have **both** SHA-1 fingerprints registered:
- Old: `DD:DA:AC:4F:AB:46:73:AE:56:C9:FB:CD:68:8E:EF:90:15:21:B4:79`
- New: `C8:BB:E7:2D:CA:F7:F6:91:0D:6A:9E:74:B2:1E:41:CE:2D:35:60:AF`

### 5. Save Changes

Click "Save" at the bottom

### 6. Wait for Propagation

**IMPORTANT**: Google OAuth changes can take 5-10 minutes to propagate globally. Wait at least 5 minutes before testing.

---

## After Registration

### Build the Production APK

```bash
./gradlew assembleRelease
```

The signed APK will be in: `app/build/outputs/apk/release/app-release.apk`

### Install on Your Pixel 9 Pro

```bash
adb -s 4B271FDAP008DU install -r app/build/outputs/apk/release/app-release.apk
```

### Test Google Sign-In

1. Open the app on your device
2. Try to sign in with Google
3. It should work with the new production keystore!

---

## Once Verified Working

After confirming the new keystore works perfectly:

1. Go back to Google Cloud Console
2. Edit the OAuth 2.0 Client ID
3. **Remove** the old SHA-1: `DD:DA:AC:4F:AB:46:73:AE:56:C9:FB:CD:68:8E:EF:90:15:21:B4:79`
4. Keep only the new production SHA-1
5. Save changes

This ensures only your secure production keystore can authenticate users.

---

## What Changed

✅ **Updated Files**:
- `app/google-services.json` - Updated certificate hash to new SHA-1
- `app/ibs-tracker-production.jks` - Your new secure production keystore
- `keystore.properties` - Now points to production keystore

✅ **Keystore Details**:
- **Owner**: CN=Vincent Le Bourlot, OU=Development, O=Ti ar Kaerell, C=FR
- **Alias**: ibs-tracker-release
- **Algorithm**: 2048-bit RSA
- **Valid until**: 2053

---

## Troubleshooting

**If sign-in still fails after registration:**

1. **Check wait time**: Did you wait 5-10 minutes after saving in Google Cloud Console?
2. **Verify SHA-1**: Run `keytool -list -v -keystore app/ibs-tracker-production.jks -alias ibs-tracker-release` and confirm the SHA-1 matches
3. **Check logcat**: Run `adb -s 4B271FDAP008DU logcat -d | grep -i "google\|auth\|sign"` to see error details
4. **Verify package name**: Ensure it's `com.tiarkaerell.ibstracker` in both the app and Google Cloud Console
5. **Clear app data**: Settings → Apps → IBS Tracker → Storage → Clear Data (this will sign you out)

**If you see "Invalid audience" error:**
- The google-services.json file still has the wrong client ID or certificate hash
- Rebuild the app after fixing

**If you see error code 10:**
- SHA-1 not registered or not propagated yet
- Wait 10 more minutes and try again

---

## Next Steps

1. ✅ Register SHA-1 in Google Cloud Console (you do this)
2. ⏳ Wait 5-10 minutes for propagation
3. 🔨 Build production APK: `./gradlew assembleRelease`
4. 📱 Install on device: `adb -s 4B271FDAP008DU install -r app/build/outputs/apk/release/app-release.apk`
5. ✅ Test Google Sign-In
6. 🧹 Remove old SHA-1 once verified working
