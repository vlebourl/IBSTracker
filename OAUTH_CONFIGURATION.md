# OAuth Configuration - IBS Tracker

## ✅ Current OAuth Setup (Correct Configuration)

### Google Cloud Project
- **Project ID**: `ibs-tracker-475708`
- **Project Number**: `896856261645`

### OAuth 2.0 Client IDs (You Have 2)

#### 1. Android OAuth Client
- **Type**: Android
- **Client ID**: `896856261645-5urcjd1287rrctptp9lri9ju9ieje2r5.apps.googleusercontent.com`
- **Package Name**: `com.tiarkaerell.ibstracker`
- **SHA-1 Certificate Fingerprint**: `C8:BB:E7:2D:CA:F7:F6:91:0D:6A:9E:74:B2:1E:41:CE:2D:35:60:AF`
- **Usage**: Automatically used by Google Play Services in the app

#### 2. Web OAuth Client
- **Type**: Web application
- **Client ID**: `896856261645-e2g6l6k1khelsl3kjji7d011oqdh5c34.apps.googleusercontent.com`
- **Client Secret**: ⚠️ **STORED SECURELY - NOT IN REPO**
- **Usage**: Used in `strings.xml` as `default_web_client_id` for server-side operations

---

## 📱 App Configuration Files

### strings.xml
```xml
<string name="default_web_client_id">896856261645-e2g6l6k1khelsl3kjji7d011oqdh5c34.apps.googleusercontent.com</string>
```
✅ **Status**: Updated with Web client ID

### google-services.json
```json
{
  "oauth_client": [
    {
      "client_id": "896856261645-5urcjd1287rrctptp9lri9ju9ieje2r5.apps.googleusercontent.com",
      "client_type": 1,
      "android_info": {
        "package_name": "com.tiarkaerell.ibstracker",
        "certificate_hash": "c8bbe72dcaf7f6910d6a9e74b21e41ce2d3560af"
      }
    },
    {
      "client_id": "896856261645-e2g6l6k1khelsl3kjji7d011oqdh5c34.apps.googleusercontent.com",
      "client_type": 3
    }
  ]
}
```
✅ **Status**: Updated with both Android (type 1) and Web (type 3) clients

---

## 🔐 Security Notes

### ⚠️ NEVER Commit These Files
- ❌ `client_secret_*.json` (contains client_secret)
- ❌ Any JSON downloaded from Google Cloud Console
- ✅ These are already in `.gitignore`

### ✅ Safe to Commit
- ✅ `strings.xml` - Client IDs are public by design
- ✅ `google-services.json` - Protected by package name + SHA-1
- ✅ OAuth Client IDs are NOT secrets (Client Secret is)

### 🔒 Keep Secret
- **Client Secret**: In downloaded JSON from Google Cloud Console (NOT in repo)
- **Keystore Password**: In keystore.properties (gitignored)
- **Keystore File**: `app/ibs-tracker-production.jks` (gitignored)

---

## 📋 Verification Checklist

Before testing Google Sign-In:

- [x] Android OAuth client created with package name `com.tiarkaerell.ibstracker`
- [x] Android OAuth client has production SHA-1 fingerprint registered
- [x] Web OAuth client created
- [x] `strings.xml` updated with Web client ID
- [x] `google-services.json` updated with both clients
- [ ] **Wait 5-10 minutes** for Google OAuth changes to propagate
- [ ] Rebuild the app
- [ ] Test Google Sign-In

---

## 🔄 How Google Sign-In Works

1. User taps "Sign in with Google" in your app
2. App uses **Android OAuth client** (automatically via google-services.json)
3. Google verifies:
   - Package name matches: `com.tiarkaerell.ibstracker` ✅
   - SHA-1 matches: `C8:BB:E7:2D:CA:F7:F6:91:0D:6A:9E:74:B2:1E:41:CE:2D:35:60:AF` ✅
4. User selects Google account and grants permissions
5. App receives ID token for the **Web OAuth client** (from strings.xml)
6. App can now make server-side requests using the Web client credentials

---

## 🆘 Troubleshooting

### Error: "You must use a Web client as the server client ID"
**Cause**: Web OAuth client missing or `strings.xml` has Android client ID
**Solution**: ✅ FIXED - Web client created and strings.xml updated

### Error: "10: Developer Error"
**Cause**: SHA-1 not registered or not yet propagated
**Solution**:
1. Go to Google Cloud Console → OAuth 2.0 Client IDs
2. Edit the **Android** client
3. Ensure SHA-1 `C8:BB:E7:2D:CA:F7:F6:91:0D:6A:9E:74:B2:1E:41:CE:2D:35:60:AF` is added
4. Wait 5-10 minutes for propagation

### Error: "Invalid audience"
**Cause**: Client ID mismatch
**Solution**: Verify `strings.xml` and `google-services.json` have correct client IDs

---

## 📞 Quick Reference

**Google Cloud Console**: https://console.cloud.google.com/apis/credentials?project=ibs-tracker-475708

**Android Client**: Edit this to add/update SHA-1 fingerprints
**Web Client**: This client ID goes in `strings.xml`

---

## 🎯 Current Status

✅ **OAuth Configuration**: Complete
⏳ **SHA-1 Registration**: Needs verification in Google Cloud Console
⏳ **Testing**: Ready to test after 5-10 minute propagation period

**Next Steps**:
1. Verify SHA-1 is registered in Android OAuth client
2. Wait 5-10 minutes
3. Rebuild and test Google Sign-In
