# Creating a Production Keystore

## Why Create a New Keystore?

Your current keystore has:
- ❌ Weak password (`android123`)
- ❌ Generic development information
- ❌ Was exposed in git history (compromised)

For production releases, you need a secure keystore with strong credentials.

## Step-by-Step Guide

### 1. Prepare Your Information

Before running the command, gather:
- **Your name or company name**
- **Organizational unit** (e.g., "Mobile Team", "Development")
- **Organization** (e.g., "TiarkaErell", "Personal Project")
- **City**
- **State/Province**
- **Country code** (2 letters, e.g., "FR" for France, "US" for USA)
- **Strong password** (at least 12 characters, mix of letters/numbers/symbols)

### 2. Generate the Keystore

Run this command and answer the prompts:

```bash
keytool -genkey -v \
  -keystore app/ibs-tracker-production.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias ibs-tracker-release
```

**Example answers:**
```
Enter keystore password: [Use a STRONG password - save it in a password manager!]
Re-enter new password: [Same password]
What is your first and last name?
  [Unknown]:  Vincent Leboeuf (or your name/company)
What is the name of your organizational unit?
  [Unknown]:  Mobile Development
What is the name of your organization?
  [Unknown]:  TiarkaErell
What is the name of your City or Locality?
  [Unknown]:  Paris
What is the name of your State or Province?
  [Unknown]:  Ile-de-France
What is the two-letter country code for this unit?
  [Unknown]:  FR
Is CN=Vincent Leboeuf, OU=Mobile Development, O=TiarkaErell, L=Paris, ST=Ile-de-France, C=FR correct?
  [no]:  yes

Enter key password for <ibs-tracker-release>
	(RETURN if same as keystore password):  [Press RETURN to use same password]
```

### 3. Update keystore.properties

Edit your `keystore.properties`:

```properties
# Production Keystore Configuration
storeFile=app/ibs-tracker-production.jks
storePassword=YOUR_STRONG_PASSWORD_HERE
keyAlias=ibs-tracker-release
keyPassword=YOUR_STRONG_PASSWORD_HERE
```

### 4. Secure the Keystore

**CRITICAL: Back up your keystore securely!**

```bash
# 1. Copy to a secure location outside the project
cp app/ibs-tracker-production.jks ~/Documents/Secure/ibs-tracker-production.jks.MASTER

# 2. Store in a password manager (recommended)
# - Upload the .jks file to 1Password/LastPass/etc.
# - Store the passwords in the same entry
# - Add notes: "IBS Tracker Production Signing Key"

# 3. Create a backup on secure cloud storage (optional)
# - Google Drive (private folder)
# - Dropbox (encrypted folder)
# - DO NOT commit to git!
```

### 5. Get Your New SHA-1 Fingerprint

```bash
keytool -list -v \
  -keystore app/ibs-tracker-production.jks \
  -alias ibs-tracker-release \
  -storepass YOUR_PASSWORD
```

Copy the SHA-1 fingerprint (looks like `XX:XX:XX:XX:...`)

### 6. Register with Google Cloud Console

1. Go to: https://console.cloud.google.com/apis/credentials
2. Find your OAuth 2.0 Client ID: `896856261645-5urcjd1287rrctptp9lri9ju9ieje2r5`
3. Click to edit
4. **Add** your new SHA-1 fingerprint (don't remove the old one yet!)
5. Save
6. Wait 5-10 minutes for propagation

### 7. Build Production APK

```bash
./gradlew assembleRelease
```

The signed APK will be in: `app/build/outputs/apk/release/app-release.apk`

### 8. Test on Your Device

```bash
# Install the production APK
adb -s 4B271FDAP008DU install -r app/build/outputs/apk/release/app-release.apk

# Test Google Sign-In with the production APK
```

### 9. Once Verified, Remove Old SHA-1

After confirming the new keystore works:
1. Go back to Google Cloud Console
2. Remove the old SHA-1: `DD:DA:AC:4F:AB:46:73:AE:56:C9:FB:CD:68:8E:EF:90:15:21:B4:79`
3. Keep only your new production SHA-1

## ⚠️ CRITICAL Security Checklist

- [ ] Used a strong password (12+ characters, mixed case, numbers, symbols)
- [ ] Saved password in a password manager
- [ ] Backed up keystore to secure location
- [ ] **NEVER** commit the keystore to git
- [ ] **NEVER** share the keystore file publicly
- [ ] Documented where the keystore is stored (for team access)
- [ ] Tested the production build before removing old SHA-1

## 📱 For Google Play Publishing

If you plan to publish on Google Play:

1. **Keep this keystore forever** - Google Play requires the same keystore for all updates
2. **Use Google Play App Signing** (recommended):
   - Upload your keystore to Google Play
   - Google manages the production key
   - You sign with an "upload key"
   - More info: https://developer.android.com/studio/publish/app-signing

## 🔄 Migration Path

**Option 1: Fresh Start (Recommended)**
- Create new production keystore
- Use for all future releases
- Keep old keystore for existing installs only

**Option 2: Continue with Current Keystore**
- Keep using the existing keystore
- Change passwords (not possible with keytool, need to create new)
- Accept the security risk

## Need Help?

If you have questions or need assistance, refer to:
- [Android Official Guide](https://developer.android.com/studio/publish/app-signing)
- [Google Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756)

---

**Remember:** Losing your keystore = unable to update your app on Google Play!
Store it securely in multiple locations.
