# Claim Saathi (Android)

Android app for Claim Saathi, a health-insurance companion. Customers sign in with a mobile number, link a policy, start a cashless or reimbursement claim, upload documents, track settlement, and ask Saathi about their cover.

The app talks to the Claim Saathi API. It does not store claims locally. The server is the source of truth.

## Requirements

- Android Studio (current stable) with Android SDK 34
- JDK 17
- A device or emulator running Android 8.0 (API 26) or newer
- The Claim Saathi server reachable from that device

| Setting | Value |
|---|---|
| Application id | `com.claimsaathi.app` |
| Min / target / compile SDK | 26 / 34 / 34 |
| Kotlin | 2.0.21 |
| Android Gradle Plugin | 8.7.3 |
| Gradle | 8.11.1 |
| Version | 1.0 (`versionCode` 1) |

`local.properties` (the Android SDK path) is gitignored. Android Studio creates it on first open.

## Run

1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Set the API base URL (see below) if the tunnel or host has changed.
4. Run the `app` configuration on an emulator or a USB device.

From the command line:

```bash
./gradlew :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/`.

Release builds are not minified. There is no signing config in the repo, so a Play upload needs a keystore added locally.

## API base URL

The app reads `BuildConfig.API_BASE_URL` from `app/build.gradle.kts`. It must end with `/api/`.

```kotlin
buildConfigField(
    "String",
    "API_BASE_URL",
    "\"https://your-host.example/api/\""
)
```

Rebuild after changing it. The current value is a Cloudflare tunnel and will stop working when that tunnel is restarted.

## Sign-in

1. Enter a 10-digit phone number. The demo server does not send an SMS.
2. Enter the OTP. The demo code is always `111000`.
3. New users complete name, email, date of birth (`yyyy-MM-dd`), gender, and city.
4. The JWT is stored in encrypted shared preferences (`claim_saathi_auth`). If the keystore is unavailable, it falls back to a private plain preferences file.
5. On the next launch, a saved token calls `GET me`. A `401` clears the token and returns the user to login.

## Tabs and screens

The bottom bar has five tabs. Other screens are pushed on the navigation stack.

| Tab or screen | What it does |
|---|---|
| Home | Greeting, paid-out and open-claim counts, active policy, current claim, pending actions, amount warnings, and shortcuts to start a claim, link a policy, or open the bank |
| Claims | Claim list and status filters |
| Chat | Saathi chat with suggestion chips, follow-ups, and finance cards |
| Alerts | Notifications, with an unread badge |
| Profile | Profile details, bank shortcut, logout |
| Link a policy | Manual policy fields, or a PDF upload. Empty fields are filled by the server demo cover. Room rent defaults to ₹4,000/day |
| Policy reader | Coverage summary after a policy is saved or analyzed |
| Start claim | Sample-data start, live amount preview and warnings, then create the claim |
| Checklist | Required documents, camera / gallery / file upload, and an n/7 progress bar |
| Validation | Result of the last upload |
| Claim tracking | Status stepper, timeline, queries, and settlement entry |
| Query reply | Explain an open query and reply with text and an optional file |
| Settlement | Approved amount, deductions, and payout details |
| Bank | Masked payout account |

Ask Saathi on a claim opens Chat with that claim id attached, so answers stay on that case.

## Uploads

Camera, gallery, and file picks are supported for claim documents, query replies, and policy PDFs.

- Camera use requires the `CAMERA` permission. The manifest also declares `INTERNET`.
- Photos are downscaled to a 1600px long edge and compressed as JPEG at quality 70. The original base name is kept so the server can detect the document type.
- Camera files go through a `FileProvider` (`com.claimsaathi.app.files`).

## Network

`data/Network.kt` builds one Retrofit client:

- Connect timeout 15 seconds
- Read timeout 90 seconds (uploads and chat)
- Write timeout 60 seconds
- `Authorization: Bearer <jwt>` when a token exists
- JSON via kotlinx.serialization, ignoring unknown keys
- HTTP errors surface the server `error.message` when the body matches that shape

Endpoints live in `data/ClaimSaathiApi.kt` and are relative to the base URL:

| Area | Calls |
|---|---|
| Auth | `POST auth/otp/send`, `POST auth/otp/verify` |
| Profile and home | `GET me`, `PUT`/`PATCH me/profile`, `GET me/home`, `GET me/finance` |
| Policies | `GET me/policies`, `GET me/policies/{id}`, `POST me/policies` (JSON or PDF), `POST me/policies/{id}/analyze` |
| Bank | `GET me/bank`, `POST me/bank` |
| Claims | `POST claims/check-coverage`, `POST claims/preview`, `POST claims`, `GET claims`, `GET claims/{id}`, `POST claims/{id}/preauth`, checklist, document upload, timeline, settlement |
| Queries | `GET queries`, `GET queries/{id}/explain`, `POST queries/{id}/respond` |
| Assistant | `GET ai/suggestions`, `POST ai/chat`, `GET demo/templates` |
| Notifications | `GET notifications`, mark one or all read |

## Project layout

```
app/src/main/java/com/claimsaathi/app/
  ClaimSaathiApp.kt          application; starts the token store
  MainActivity.kt            navigation graph and bottom bar
  data/
    ClaimSaathiApi.kt        Retrofit interface
    Network.kt               client, auth header, token store
    Models.kt                request and response models
  ui/
    AppVm.kt                 session and shared screen state
    LiveScreens.kt           login through settlement screens
    Features.kt              start claim, chat, finance, upload sources
    Common.kt                buttons, cards, brand mark, image compression
    Polish.kt                skeletons, empty states, tips, chat chrome
    Colors.kt / Theme.kt     Paytm-style palette
app/src/main/res/            launcher icon, colors, file-provider paths
```

The launcher icon is a cyan shield with a navy check (`#00BAF2` background). The same image is `drawable/ic_brand.png`, used in the in-app header. Adaptive icons are in `mipmap-anydpi-v26`.

## Look

Background `#F5F7FA`, primary `#00BAF2`, navy `#002E6E`. Cards are 16dp. Loading uses skeleton cards. Empty lists use an icon and a short message.

## Related repos

- `Claim-saathi-server` — API this app calls
- `Claim-saathi-iOS` — the same product on iOS
- `Claim-saathi-dashboard` — operator dashboard, including the demo-data switch
