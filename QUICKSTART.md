# Quick Start Guide

This is a quick reference for getting the app building on a new machine.

## TL;DR - Minimal Setup

```bash
# 1. Clone the repo
git clone git@github.com:vlebourl/IBSTracker.git
cd IBSTracker

# 2. Create keystore.properties
cp keystore.properties.example keystore.properties

# 3. Edit keystore.properties with your credentials
# (See options below)

# 4. Build!
./gradlew assembleDebug
```

## Keystore Options

### Option 1: Generate Your Own (For Testing/Development)

```bash
# Generate a new keystore
keytool -genkey -v -keystore app/release-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias release-key

# Update keystore.properties with YOUR passwords
```

Your `keystore.properties` should look like:
```properties
storeFile=app/release-keystore.jks
storePassword=YOUR_PASSWORD_HERE
keyAlias=release-key
keyPassword=YOUR_KEY_PASSWORD_HERE
```

### Option 2: Get Production Keystore (For Official Releases)

**Request from project maintainer:**
1. The `release-keystore.jks` file (store securely!)
2. The passwords for `storePassword` and `keyPassword`
3. Put the keystore in `app/release-keystore.jks`
4. Fill in `keystore.properties` with the provided passwords

**🔐 Security:** Keep the production keystore file and passwords in a secure password manager (1Password, LastPass, etc.). Never commit them to git!

## Without Keystore (Debug Only)

If you just want to build debug APKs without signing, you can:

1. Comment out the signing config in `app/build.gradle.kts`:
```kotlin
// debug {
//     signingConfig = signingConfigs.getByName("release")
// }
```

2. Build debug:
```bash
./gradlew assembleDebug
```

## Google Sign-In Setup (Optional)

The app will build and run without Google Sign-In configured, but if you want that feature:

1. Get your keystore SHA-1:
```bash
keytool -list -v -keystore app/release-keystore.jks \
  -alias release-key -storepass YOUR_PASSWORD
```

2. Register it in [Google Cloud Console](https://console.cloud.google.com/)
   - Project: `ibs-tracker-475708`
   - Add your SHA-1 to the OAuth Android client
   - Add your test account in OAuth consent screen

3. Wait 5-10 minutes for Google to propagate the changes

## CI/CD Setup

For GitHub Actions or other CI systems, use environment variables instead of `keystore.properties`:

```yaml
env:
  KEYSTORE_KEY_ALIAS: ${{ secrets.KEYSTORE_KEY_ALIAS }}
  KEYSTORE_KEY_PASSWORD: ${{ secrets.KEYSTORE_KEY_PASSWORD }}
  KEYSTORE_STORE_PASSWORD: ${{ secrets.KEYSTORE_STORE_PASSWORD }}
  KEYSTORE_FILE: ${{ secrets.KEYSTORE_FILE }}
```

The `build.gradle.kts` automatically falls back to environment variables if `keystore.properties` doesn't exist.

## Troubleshooting

**"keystore.properties not found"**
→ Create it from the example: `cp keystore.properties.example keystore.properties`

**"Keystore was tampered with, or password was incorrect"**
→ Check your passwords in `keystore.properties`

**Google Sign-In fails with error 10**
→ Your SHA-1 doesn't match what's in Google Cloud Console

---

For detailed setup instructions, see [SETUP.md](SETUP.md)
