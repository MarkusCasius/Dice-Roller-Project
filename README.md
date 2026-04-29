# Dice Roller Project

Android application built with Kotlin for tabletop gamers and statisticians. This app features a deterministic 2D-simulated dice tray, custom die creation and a statistical simulator to analyze roll distributions.

## Key Features

*   **Deterministic 2D Dice Tray:** Visual representation of rolls that matches the underlying engine results.
*   **Custom Die Factory:** Create dice with non-numeric faces (e.g., "Fire", "Ice", "Fail") for categorical rolling.
*   **Rule Architect:** Bundle multiple dice types (standard and custom) with complex modifiers (Flat bonuses, Keep Highest/Lowest, Reroll ranges).
*   **Statistical Simulator:** Run up to 10,000+ trials to view histograms and analyze outcome likelihoods.
*   **Cloud Sync:** Connect with Firebase SQL Connect to sync your custom dice and rules across devices. (Still Work In Progress)
*   **Data Management:** Export/Import individual dice or entire rulesets as JSON files.
*   **Global Customization:** Dynamic theme engine allowing users to customize background, component, and text colors.

## Tech Stack

*   **Language:** Kotlin (2.3.21)
*   **UI Architecture:** Single Activity with Navigation Component.
*   **Database:** Jetpack DataStore (Local) & Firebase Data Connect / PostgreSQL (Cloud).
*   **Cloud Services:** Firebase Auth (Email/Password), App Check (Debug/Play Integrity), Firestore (Legacy/Backup).
*   **Serialization:** GSON & Kotlinx Serialization.
*   **Testing:** JUnit 4 (Unit) & Espresso (UI).

## Prerequisites

Before running this project, ensure you have the following installed:

1.  **Android Studio Ladybug (2024.2.1)** or newer.
2.  **Java Development Kit (JDK) 21**.
3.  **Firebase CLI** (optional, but recommended for SQL Connect deployment).
4.  An Android Emulator or physical device running **API 24** or higher.

## Installation & Setup

### 1. Clone the Repository
Open terminal or post the link into repository clone on android studio: bash git clone https://github.com/your-username/DiceRollerProject.git cd DiceRollerProject

### 2. Firebase Configuration
This project requires Firebase to access online features.
1.  Go to the [Firebase Console](https://console.firebase.google.com/).
2.  Create a new project named `dice-roller-5a41b`.
3.  Register your Android app with the package name `com.example.dicerollerproject`.
4.  Download the `google-services.json` and place it in the `app/` directory.
5.  **Enable Authentication:** Enable the Email/Password provider in the Firebase Console.
6.  **Setup Data Connect:**
    *   Initialize a Data Connect service linked to a Cloud SQL (PostgreSQL) instance.
    *   Set the region to `us-east4` (or update `dataconnect.yaml` to your preferred region).

### 3. Deploy Data Connect
Open your terminal and run: bash firebase deploy --only dataconnect

### 4. Build the Project
Open the project in Android Studio. Click **Sync Project with Gradle Files** (the elephant icon). This will generate the **Data Connect Kotlin SDK** required for the Cloud Sync feature.

## Testing

### Unit Tests
The core dice logic and modifiers are tested using JUnit.
*   Right-click `app/src/test/java` and select **Run 'Tests in com.example...'**

### UI (Espresso) Tests
The creation and rolling workflows are tested using Espresso.
*   Ensure an emulator is running.
*   Right-click `app/src/androidTest/java` and select **Run 'All Tests'**.

## Project Structure

*   `/domain`: Core logic including `DiceEngine.kt`, `Modifier.kt`, and `RollResult.kt`.
*   `/data`: Data persistence layer including `LocalStore.kt` (DataStore) and `RuleMapper.kt`.
*   `/ui`: Fragments and Adapters for the rolling, simulation, and settings screens.
*   `/dataconnect`: GraphQL schema (`schema.gql`) and operations (`operations.gql`) for the PostgreSQL backend.
