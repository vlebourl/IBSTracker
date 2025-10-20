# IBS Tracker - Development Setup Guide

This guide will help you set up the IBS Tracker Android application for local development.

## Prerequisites

- Android Studio (Arctic Fox or later)
- JDK 21
- Android SDK with API level 34
- Git

## Initial Setup

### 1. Clone the Repository

```bash
git clone git@github.com:vlebourl/IBSTracker.git
cd IBSTracker
```

### 2. Configure Signing Keys

The app requires a signing key for both debug and release builds. Follow these steps:

#### Option A: Use Your Own Keystore (Recommended for Contributors)

1. Generate a new keystore:
```bash
keytool -genkey -v -keystore app/release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release-key
```

2. Create `keystore.properties` in the project root:
```bash
cp keystore.properties.example keystore.properties
```

3. Edit `keystore.properties` with your values:
```properties
storeFile=app/release-keystore.jks
storePassword=YOUR_PASSWORD
keyAlias=release-key
keyPassword=YOUR_KEY_PASSWORD
```

#### Option B: Request Production Keystore (For Official Builds)

If you need to create official release builds, contact the project maintainer for:
- The production `release-keystore.jks` file
- The keystore passwords

**⚠️ NEVER commit the keystore file or passwords to git!**

### 3. Configure Google Services (Optional)

The app uses Google Sign-In for authentication. If you want to test this feature:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or use an existing one
3. Enable Google Sign-In API
4. Create OAuth 2.0 credentials:
   - Type: Android
   - Package name: `com.tiarkaerell.ibstracker`
   - SHA-1: Get from your keystore (see below)

To get your SHA-1 fingerprint:
```bash
keytool -list -v -keystore app/release-keystore.jks -alias release-key -storepass YOUR_PASSWORD -keypass YOUR_KEY_PASSWORD
```

5. Update `app/src/main/res/values/strings.xml`:
```xml
<string name="default_web_client_id">YOUR_CLIENT_ID.apps.googleusercontent.com</string>
```

6. Update `app/google-services.json` with your project details (optional, the app will work without Firebase)

## Building the App

### Debug Build

```bash
./gradlew assembleDebug
```

The APK will be in `app/build/outputs/apk/debug/app-debug.apk`

### Release Build

```bash
./gradlew assembleRelease
```

The APK will be in `app/build/outputs/apk/release/app-release.apk`

## Running on Device/Emulator

### Via Android Studio

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Click Run ▶️ or press `Shift + F10`

### Via Command Line

```bash
# Install debug build
./gradlew installDebug

# Install release build
./gradlew installRelease
```

## CI/CD Setup (GitHub Actions Example)

For automated builds, you can use environment variables instead of `keystore.properties`:

```yaml
env:
  KEYSTORE_KEY_ALIAS: ${{ secrets.KEYSTORE_KEY_ALIAS }}
  KEYSTORE_KEY_PASSWORD: ${{ secrets.KEYSTORE_KEY_PASSWORD }}
  KEYSTORE_STORE_PASSWORD: ${{ secrets.KEYSTORE_STORE_PASSWORD }}
```

Store the keystore file as a base64-encoded secret:
```bash
# Encode
base64 -i app/release-keystore.jks -o keystore.b64

# Decode in CI
echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > app/release-keystore.jks
```

## Security Notes

### Files That Should NEVER Be Committed

- ❌ `*.jks`, `*.keystore` - Signing keys
- ❌ `keystore.properties` - Contains passwords
- ❌ `client_secret*.json` - OAuth secrets
- ❌ `local.properties` - Local SDK paths and secrets

### Files Safe to Commit

- ✅ `google-services.json` - Protected by package name + SHA-1
- ✅ `keystore.properties.example` - Template without real values
- ✅ OAuth Client IDs in code - Public by design

## Troubleshooting

### Build Fails with "keystore.properties not found"

Create the file following Step 2 above, or set environment variables.

### Google Sign-In Fails

1. Verify your SHA-1 fingerprint matches what's registered in Google Cloud Console
2. Ensure your Google account is added as a test user in OAuth consent screen
3. Wait 5-10 minutes after making changes in Google Cloud Console

### App Signature Mismatch

If you're using a different keystore than production, you'll need to:
1. Uninstall the existing app
2. Install with your keystore

## Contributing

Before submitting a pull request:

1. Ensure no secrets are committed
2. Run tests: `./gradlew test`
3. Check code style: `./gradlew ktlintCheck`
4. Build successfully: `./gradlew build`

## Support

For issues or questions:
- Open an issue on GitHub
- Contact: [Your Contact Info]

---

**⚠️ Security Reminder**: Never share your keystore files, passwords, or OAuth client secrets publicly!
