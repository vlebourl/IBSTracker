# Secure Password Storage Options

You asked a great security question! Here are your options for storing keystore passwords securely, ranked from most to least secure.

## ✅ Option 1: Environment Variables (Recommended)

**Pros:** ✅ No passwords in files, ✅ Works with CI/CD, ✅ Easy to rotate
**Cons:** ⚠️ Visible in process list, ⚠️ Need to set up on each machine

### Setup for macOS/Linux:

```bash
# Edit your shell profile
nano ~/.zshrc  # or ~/.bash_profile for bash

# Add these lines at the end:
export IBS_KEYSTORE_PASSWORD="your-strong-password-here"
export IBS_KEYSTORE_ALIAS="ibs-tracker-release"
export IBS_KEYSTORE_FILE="app/ibs-tracker-production.jks"

# Save and reload
source ~/.zshrc  # or source ~/.bash_profile
```

**Build the app:**
```bash
./gradlew assembleRelease
# Password is read from environment variable automatically!
```

### Setup for Windows (PowerShell):

```powershell
# Add to your PowerShell profile
notepad $PROFILE

# Add these lines:
$env:IBS_KEYSTORE_PASSWORD="your-strong-password-here"
$env:IBS_KEYSTORE_ALIAS="ibs-tracker-release"
$env:IBS_KEYSTORE_FILE="app\ibs-tracker-production.jks"
```

---

## ✅ Option 2: macOS Keychain + Script (Most Secure for Mac)

**Pros:** ✅✅ Most secure, ✅ OS-level encryption, ✅ No passwords in files/env
**Cons:** ⚠️ macOS only, ⚠️ Requires initial setup

### Setup:

```bash
# 1. Store password in macOS Keychain
security add-generic-password \
  -a "$USER" \
  -s "ibs-tracker-keystore" \
  -w "your-strong-password-here"

# 2. Create a build script
cat > build-release.sh << 'EOF'
#!/bin/bash
# Retrieve password from Keychain
export IBS_KEYSTORE_PASSWORD=$(security find-generic-password \
  -a "$USER" \
  -s "ibs-tracker-keystore" \
  -w)
export IBS_KEYSTORE_ALIAS="ibs-tracker-release"
export IBS_KEYSTORE_FILE="app/ibs-tracker-production.jks"

# Build
./gradlew assembleRelease
EOF

# 3. Make it executable
chmod +x build-release.sh

# 4. Use it to build
./build-release.sh
```

---

## ⚠️ Option 3: Gradle Properties with Restricted Permissions (Moderate)

**Pros:** ✅ Simple, ✅ Works with Android Studio
**Cons:** ⚠️ Password in file (though protected)

### Setup:

```bash
# Create gradle.properties in your home directory
nano ~/.gradle/gradle.properties

# Add these (they'll be available to all your projects):
IBS_KEYSTORE_PASSWORD=your-strong-password-here
IBS_KEYSTORE_ALIAS=ibs-tracker-release
IBS_KEYSTORE_FILE=app/ibs-tracker-production.jks

# Secure the file
chmod 600 ~/.gradle/gradle.properties
```

Then update `build.gradle.kts` to read from gradle.properties:
```kotlin
keyPassword = System.getenv("IBS_KEYSTORE_PASSWORD")
    ?: project.findProperty("IBS_KEYSTORE_PASSWORD")?.toString()
    ?: propKeyPassword
    ?: ""
```

---

## ⚠️ Option 4: Project-Level keystore.properties (Least Secure)

**Pros:** ✅ Simple, ✅ No setup needed
**Cons:** ❌ Password in plain text file, ❌ Risk of accidental commit

This is what we had originally. If you choose this:

```properties
# keystore.properties
storeFile=app/ibs-tracker-production.jks
storePassword=your-password-here
keyAlias=ibs-tracker-release
keyPassword=your-password-here
```

**CRITICAL:** Ensure it's in `.gitignore` (already done) and restrict permissions:
```bash
chmod 600 keystore.properties
```

---

## 🔐 Option 5: Build-Time Password Prompt (Most Paranoid)

**Pros:** ✅✅✅ No password stored anywhere
**Cons:** ❌ Must enter manually each build, ❌ Breaks CI/CD

Create a build script that prompts for password:

```bash
#!/bin/bash
# build-with-prompt.sh

read -sp "Enter keystore password: " PASSWORD
echo

export IBS_KEYSTORE_PASSWORD="$PASSWORD"
export IBS_KEYSTORE_ALIAS="ibs-tracker-release"
export IBS_KEYSTORE_FILE="app/ibs-tracker-production.jks"

./gradlew assembleRelease
```

---

## 🏆 My Recommendation: Hybrid Approach

**For local development:**
- Use **Option 1 (Environment Variables)** in your `~/.zshrc`
- Or **Option 2 (macOS Keychain)** if you're on Mac and want max security

**For CI/CD (GitHub Actions, etc.):**
- Use **GitHub Secrets** with environment variables
- Store keystore as base64-encoded secret

**For the keystore.properties file:**
- Keep it with **blank passwords** (as it is now)
- It only stores the file path and alias (non-sensitive)

---

## 🚀 Quick Setup (Environment Variables)

**Run this now to get started:**

```bash
# Add to your shell profile
echo 'export IBS_KEYSTORE_PASSWORD="YOUR_PASSWORD_HERE"' >> ~/.zshrc
source ~/.zshrc

# Test it works
echo $IBS_KEYSTORE_PASSWORD

# Build!
./gradlew assembleRelease
```

**Don't forget to:**
1. Replace `YOUR_PASSWORD_HERE` with your actual password
2. Save your password in a **password manager** (1Password, LastPass, etc.)
3. **Never** commit passwords to git

---

## Need Help Choosing?

**If you:**
- ✅ Want simplicity → **Option 1 (Environment Variables)**
- ✅ Want maximum security on Mac → **Option 2 (Keychain)**
- ✅ Want convenience → **Option 4 (File with chmod 600)**
- ✅ Building in CI/CD → **Option 1 (with GitHub Secrets)**

Let me know which option you prefer and I'll help you set it up!
