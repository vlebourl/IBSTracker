# Fix Google OAuth Configuration

## 🔴 Current Error

```
[GetTokenResponseHandler] Server returned error: You must use a Web client as the server client ID.
```

**Root Cause**: Google Sign-In for Android requires BOTH an Android OAuth client AND a Web OAuth client properly configured.

---

## ✅ Step-by-Step Fix

### 1. Go to Google Cloud Console

https://console.cloud.google.com/apis/credentials?project=ibs-tracker-475708

### 2. Check Your Existing OAuth 2.0 Client IDs

You should see at least two client IDs:

**Client ID currently in your app**:
```
896856261645-5urcjd1287rrctptp9lri9ju9ieje2r5.apps.googleusercontent.com
```

**Question**: Is this client ID an **Android** type or **Web application** type?

---

## 🛠️ Configuration Setup

### Option A: If You Have Only a Web Client

**You need to create an Android OAuth client:**

1. Click **"+ CREATE CREDENTIALS"** → **"OAuth client ID"**
2. **Application type**: **Android**
3. **Name**: `IBS Tracker Android`
4. **Package name**: `com.tiarkaerell.ibstracker`
5. **SHA-1 certificate fingerprint**:
   ```
   C8:BB:E7:2D:CA:F7:F6:91:0D:6A:9E:74:B2:1E:41:CE:2D:35:60:AF
   ```
6. Click **"CREATE"**

After creation, you'll get an Android Client ID (e.g., `896856261645-XXXXX.apps.googleusercontent.com`)

**Then**, ensure your Web client has this Android package linked:
- Edit your Web OAuth client
- Under "Authorized redirect URIs", you may need to add Android-specific configs

### Option B: If You Have Only an Android Client

**You need to create a Web Application OAuth client:**

1. Click **"+ CREATE CREDENTIALS"** → **"OAuth client ID"**
2. **Application type**: **Web application**
3. **Name**: `IBS Tracker Web Client`
4. **Authorized redirect URIs**: Add if needed for backend
5. Click **"CREATE"**

Copy the new Web Client ID and update `strings.xml`.

### Option C: If You Have Both (Most Likely)

**Check your Android OAuth client:**

1. Find the OAuth client of type **"Android"**
2. Click to edit it
3. Verify:
   - **Package name**: `com.tiarkaerell.ibstracker` ✅
   - **SHA-1 fingerprint**: `C8:BB:E7:2D:CA:F7:F6:91:0D:6A:9E:74:B2:1E:41:CE:2D:35:60:AF` ✅

**Check your Web OAuth client:**

1. Find the OAuth client of type **"Web application"**
2. This is the ID that should be in your `strings.xml`
3. Ensure it's properly configured

---

## 📱 Update Your App Configuration

After verifying both OAuth clients exist:

### 1. Verify strings.xml

The `default_web_client_id` should be your **Web application** Client ID:

```xml
<string name="default_web_client_id">896856261645-XXXXX.apps.googleusercontent.com</string>
```

**IMPORTANT**: This MUST be the Web client ID, NOT the Android client ID!

### 2. Verify google-services.json

Your `google-services.json` should list the **Android** client ID under `oauth_client`:

```json
{
  "oauth_client": [
    {
      "client_id": "896856261645-XXXXX.apps.googleusercontent.com",
      "client_type": 1,
      "android_info": {
        "package_name": "com.tiarkaerell.ibstracker",
        "certificate_hash": "c8bbe72dcaf7f6910d6a9e74b21e41ce2d3560af"
      }
    },
    {
      "client_id": "896856261645-XXXXX.apps.googleusercontent.com",
      "client_type": 3
    }
  ]
}
```

**client_type 1** = Android client
**client_type 3** = Web client

---

## 🔍 How to Identify Client Types in Google Cloud Console

When you look at your OAuth 2.0 Client IDs list:

| Type | Looks Like |
|------|------------|
| **Android** | Shows package name (`com.tiarkaerell.ibstracker`) and SHA-1 |
| **Web application** | Shows "Authorized redirect URIs" or "Authorized JavaScript origins" |
| **iOS** | Shows bundle ID |

---

## 🎯 What You Need to Do Right Now

### Step 1: Check Google Cloud Console

Go to https://console.cloud.google.com/apis/credentials?project=ibs-tracker-475708

**Answer these questions:**

1. How many OAuth 2.0 Client IDs do you see?
2. What are their types? (Android / Web application / iOS / etc.)
3. What are their Client IDs?

### Step 2: Identify Which ID Goes Where

- **Android OAuth client** → Used by Google Play Services automatically
- **Web OAuth client** → Must be in `strings.xml` as `default_web_client_id`

### Step 3: Update Configuration

Based on what you find:

**If you have both Android and Web clients:**
- Ensure Android client has the new SHA-1: `C8:BB:E7:2D:CA:F7:F6:91:0D:6A:9E:74:B2:1E:41:CE:2D:35:60:AF`
- Ensure `strings.xml` has the Web client ID

**If you only have one:**
- Create the missing one (Android or Web)
- Update app configuration accordingly

---

## 🧪 Testing After Configuration

1. **Wait 5-10 minutes** after making changes in Google Cloud Console
2. Rebuild the app: `./gradlew assembleRelease`
3. Reinstall: `adb -s 4B271FDAP008DU install -r app/build/outputs/apk/release/app-release.apk`
4. Test Google Sign-In

---

## 📋 Quick Reference

**Your current setup:**
- **Package name**: `com.tiarkaerell.ibstracker`
- **Production SHA-1**: `C8:BB:E7:2D:CA:F7:F6:91:0D:6A:9E:74:B2:1E:41:CE:2D:35:60:AF`
- **Project ID**: `ibs-tracker-475708`
- **Project number**: `896856261645`
- **Client ID in strings.xml**: `896856261645-5urcjd1287rrctptp9lri9ju9ieje2r5.apps.googleusercontent.com`

**Key question**: Is `896856261645-5urcjd1287rrctptp9lri9ju9ieje2r5` an Android or Web client?

---

## 🆘 Common Issues

### "10: Developer Error"
- SHA-1 not registered or propagating
- Android OAuth client missing

### "You must use a Web client as the server client ID"
- Web OAuth client missing or misconfigured
- Wrong client ID in `strings.xml`
- `default_web_client_id` is pointing to Android client instead of Web client

### "Invalid audience"
- Client ID mismatch between app and Google Cloud Console
- Package name mismatch

---

## Next Steps

**Tell me:**
1. How many OAuth clients you see in Google Cloud Console
2. Their types (Android / Web)
3. Their Client IDs

Then I can tell you exactly what to update!
