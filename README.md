# Pocket Guardian

> **"Your safety. Your people. One tap away."**

**Pocket Guardian** is a production-quality, AI-assisted personal safety and emergency alert application built for modern Android devices using Kotlin, Jetpack Compose, and Material 3 with a high-contrast Light UI theme, Room local database persistence, and Gemini AI crisis advising.

---

## Complete Feature Suite

### 1. Rapid SOS System (3-Second Hold Confirmation)
- **Accidental-Tap Prevention**: Requires a continuous 3-second hold with real-time radial visual feedback, haptic vibration ticks, and release-to-cancel safeguard.
- **State Machine**: `IDLE` → `HOLDING` → `COUNTDOWN` → `EMERGENCY_ACTIVE` → `ALERTING` → `ALERT_SENT` → `RESOLVED`.
- **Automatic Incident Creation**: Generates an encrypted local Room audit log recording timestamps, GPS coordinates, accuracy, and notification dispatches.
- **Emergency Overlay Mode**: High-visibility distress screen providing instant 1-tap call to 911 / emergency services, live location telemetry, and Google Maps link sharing.
- **Duress PIN Coercion Disarm**: Resolving with Safe PIN (`1234`) disarms normally. Resolving with Duress PIN (`9999`) visually closes the emergency while secretly alerting contacts that the user was forced to disarm.
- **Encrypted Audio Evidence Buffer**: Incident logs record an audio evidence presence flag.

### 2. Safety Check-in Timer
- **Standalone Timed Protection**: Perfect for solo running, inspecting an apartment, or meeting someone new.
- **Presets**: 15m Solo Run, 30m Meeting New Person, 45m Late Commute.
- **Controls**: Live countdown progress, `+10 Min` extension, and Safe Disarm button.
- **Automatic Escalation**: Automatically dispatches SOS distress if un-cancelled prior to timer expiration.

### 3. Safe Havens & Geo-Zones
- **Designated Safety Locations**: Campus North Quad, Downtown Police Precinct, Home, Hospital.
- **1-Tap Navigation**: Direct Android intent opening turn-by-turn navigation in Google Maps directly to the nearest verified safe haven.
- **Custom Zones**: Easily add new safe zones with category tags.

### 4. Stealth Calculator Disguise
- **Camouflage for Dangerous Situations**: Disguises the entire app as a fully functioning arithmetic calculator.
- **Covert Trigger**: Entering the safety PIN or pressing `=` three times silently activates emergency SOS and GPS telemetry in the background without alarms or visual distress.

### 5. Guardian Mode (Safety Journeys & Periodic Check-ins)
- **Destination & ETA**: Set destination (e.g. *KSRCT College Gate*) and estimated travel time.
- **Periodic Check-in Timer**: Configurable intervals (e.g., every 15, 20, or 30 minutes) prompting *"Are you safe?"*.
- **Multi-Tier Escalation**: 60-second grace window; if unacknowledged, triggers attention warnings and escalates alerts to trusted contacts.
- **Immediate Response**: Quick-action buttons for `"I'm Safe"` and `"Need Help"`.

### 6. Trusted Contacts Circle
- **Tiered Priorities**: Primary (immediate high-urgency SMS/call) and Secondary (notification updates).
- **Relationships**: Parent, Partner, Sibling, Friend, Roommate, Emergency Contact.
- **Notification Toggles**: Individually enable or disable alert broadcast participation.
- **Test Alert Simulator**: Verify contact connectivity and message delivery before entering high-risk areas.

### 7. Guardian AI (Gemini 3.5 Flash Integration)
- **Calm, Actionable Safety Advice**: Practical guidance for walking alone, suspecting surveillance, rideshare safety, or de-escalation.
- **AI Crisis Message Generator**: Translates raw situational descriptions into concise emergency broadcasts containing GPS coordinates and maps links.
- **AI Incident Summaries**: Analyzes verified audit log events to generate calm post-incident reports.
- **Journey Risk Indicator**: Transparent `LOW`, `ATTENTION`, `HIGH` score based exclusively on verified telemetry (missed check-ins, location availability, active SOS).
- **Resilient Offline Fallback**: Instant local safety heuristics engine guarantees guidance even without internet or API keys.

### 8. Official Crisis & Emergency Directory
- **Verified National Hotlines**:
  - 911 / 112 Emergency Dispatch (1-tap dial)
  - 988 Suicide & Crisis Lifeline (1-tap call & 1-tap text)
  - 741741 Crisis Text Line (Discreet text "HOME")
  - National Domestic Violence Hotline (1-800-799-7233)
  - RAINN Sexual Assault Hotline (1-800-656-4673)
  - Poison Control (1-800-222-1222)

### 9. Discreet Safety Tools
- **Fake Incoming Call**: Realistic ringing screen with audio and vibration to provide users a polite, natural excuse to leave uncomfortable situations.
- **Loud Acoustic Siren & Strobe**: High-visibility deterrent for immediate physical defense.
- **Demo Mode Simulator**: On-device test harness for switching simulated GPS locations, testing check-in prompts, and generating test incidents without calling real phone lines.
- **Local Data Sovereignty**: All logs and contact records stored on-device in SQLite/Room with 1-tap privacy wipe.

---

## Configuration & Environment Variables

### Gemini API Key
The application reads the Gemini API key from `BuildConfig.GEMINI_API_KEY` (configured via the AI Studio Secrets panel and `.env` / `.env.example`).

To set your key in development:
1. Open `.env` in the root directory.
2. Set: `GEMINI_API_KEY=your_actual_gemini_api_key`
3. AI Studio automatically injects this secret into `BuildConfig`.
