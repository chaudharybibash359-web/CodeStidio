# Developer Code Studio: Mobile IDE & AI App Builder

A full-fledged mobile development studio inspired by VS Code, enabling developers to build, edit, prompt-generate, preview, and export web apps, HTML5 games, and mobile prototypes with live in-app execution and game touch controls.

### User Review & Critical Decisions

> [!IMPORTANT]
> Based on your answers during Phase 1 clarification, the following core architecture decisions have been confirmed:

- **Confirmed Runtime & Stack**: Full web stack supporting HTML5, CSS3, modern JavaScript (ES6+), Canvas 2D/WebGL game APIs, and responsive mobile prototypes.
- **Confirmed AI Generation Engine**: Instant offline smart project blueprints and prompt synthesizer (covering interactive 2D games, utilities, dashboards, and canvas applications) with optional Gemini API extension.
- **Confirmed Live Sandbox Runner**: Dedicated fullscreen sandbox environment equipped with customizable virtual game touch controls (DPad, A/B action buttons, touch drag overlay), live JavaScript console logs, and instant APK/ZIP package builder.
- **File Management & Persistence**: Local Room SQLite storage for seamless multi-project workspaces, file tree manipulation, revision history, and instant hot-reloading.

---

### 1. Overview & Core Concept

- **What It Does**: Developer Code Studio provides a complete mobile IDE experience right in your pocket. Users can either type code directly in a syntax-highlighted code editor with a file explorer, or describe what they want in natural language to let the AI Studio generate complete, multi-file working applications and games. Users can instantly execute their apps in a live fullscreen sandbox with virtual game controls (DPad, Action buttons), inspect console logs, debug errors, and trigger a build pipeline to export ZIP packages, standalone HTML bundles, or simulated APK installers.
- **Target Audience / Persona**: Mobile developers, students, game creators, hobbyists, and prompt engineers who want to code, prototype, and test games or web apps on Android devices anywhere without needing a desktop computer.
- **Key Value**: Eliminates the barrier between code generation and execution on mobile. Provides true file-tree management, real syntax editing, instant zero-latency preview, and physical-like game controls for testing mobile web games.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Studio Dashboard & Project Creation**:
   - View recent projects with category tags (Web App, 2D Game, Utility, Portfolio).
   - Tap **"Prompt to App (AI Builder)"** to describe any desired app (e.g., *"Make a retro arcade space shooter with high scores and sound effects"* or *"Build an expense tracker with category filters"*). The generator synthesizes `index.html`, `style.css`, and `app.js` with functional logic.
   - Or tap **"New Blank Project"** / choose from built-in starter templates (Flappy Bird, Platformer Game, Weather App, Calculator, 2048 Puzzle).

2. **VS Code-Style Workspace & Code Editor**:
   - Left drawer / sidebar: VS Code file tree explorer (`index.html`, `style.css`, `main.js`, `assets/`, `manifest.json`), file creation, renaming, and deletion.
   - Editor tabs bar: Switch between open files smoothly with dirty indicator (unsaved changes dot).
   - Editor canvas: Dark-mode IDE theme (VS Code Dark+ palette) with monospace font, line numbers, syntax highlighting (HTML tags/attributes, CSS selectors/properties, JS keywords/strings/functions), undo/redo, search/replace, and quick code snippet accessories (brackets, quotes, semicolons, indent).

3. **Fullscreen Sandbox Runner & Game Touch Controls**:
   - One-tap **"Run / Preview"** button compiles the workspace into a sandbox DOM runtime.
   - Seamless floating overlay or sticky bottom pad offering:
     - **Virtual Game Controller**: Responsive 4-way DPad / Joystick + Action Buttons (A, B, Jump, Fire) that trigger standard keyboard events (`ArrowUp`, `ArrowDown`, `ArrowLeft`, `ArrowRight`, `Space`, `KeyZ`, `KeyX`) or virtual touch points in the game canvas.
     - **Developer Console**: Collapsible bottom drawer showing real-time `console.log`, `console.warn`, `console.error`, and runtime exception banners.
     - **Viewport Switcher**: Toggle between Phone Portrait, Phone Landscape, Tablet, and Desktop viewport sizes.

4. **Build, Test & Export / Install Simulation**:
   - Tap **"Build & Install"**: Runs an interactive compilation checklist (Asset Bundler -> Code Minification Check -> Manifest Verification -> APK Packaging).
   - Generates a downloadable ZIP bundle, standalone offline HTML file, and an Android App Package (APK) installer simulator with "Install on Device" preview flow.

#### Visual Identity & Theme
- **Aesthetic Direction**: Authentic modern IDE aesthetic inspired by VS Code Dark+ and Material Design 3. Sleek, high-density, purposeful, and distraction-free.
- **Color Palette**:
  - Background Base: `#1E1E1E` (VS Code Editor background)
  - Surface & Sidebar: `#252526` / `#333333`
  - Accent / Brand: `#007ACC` (VS Code Blue) & `#0E639C`
  - Syntax Colors: Keywords `#569CD6` (Blue), Strings `#CE9178` (Coral Orange), Functions `#DCDCAA` (Warm Yellow), Tags `#4EC9B0` (Teal), Comments `#6A9955` (Muted Green).
  - Status Accents: Error `#F44747`, Warning `#CCA700`, Success `#89D185`.
- **Typography**:
  - Code Editor: Monospace font with clear character distinction (`0` vs `O`, `1` vs `l`).
  - Studio UI: Clean Roboto / Sans-Serif with sharp typographic scale (Labels 11-12sp, Body 14sp, Titles 16-18sp).
- **Interactive Feedback**:
  - Visual ripples on buttons and toolbar actions.
  - Active tab underline in brand blue (`#007ACC`).
  - Haptic touch feedback when pressing virtual game controller buttons.
  - Slide-in animations for file explorer and console drawer.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Local Offline-First AI & Blueprint Engine**:
  - *Chosen Approach*: Implement a rich offline prompt-to-app generator that parses user requests, detects intent/keywords (game type, app genre, color themes, features), and dynamically generates complete multi-file projects instantly, while also providing a pluggable Gemini API bridge if secrets are provided.
  - *Why*: Instant generation with 0s latency, works 100% offline without requiring users to wait or fail if cloud quotas are exhausted.
- **Decision 2: Sandboxed WebView Execution with Injected Game Controls Bridge**:
  - *Chosen Approach*: Use an Android `WebView` running isolated HTML5/JS with injected JavaScript bridges to intercept `console.log` messages and dispatch simulated keyboard/touch events from the Compose virtual gamepad.
  - *Why*: Runs full web apps, HTML5 games (Phaser, Canvas2D, Three.js, pure JS), CSS animations, and DOM apps at 60 FPS natively on Android.
- **Decision 3: Room Database for Workspace State**:
  - *Chosen Approach*: Store projects, file trees, file contents, and settings in Room SQLite.
  - *Why*: Reliable atomic saves, instant search, fast file switching, and zero risk of data loss when multitasking.

---

### 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│                      DEVELOPER CODE STUDIO                      │
├─────────────────┬─────────────────────────────┬─────────────────┤
│  File Explorer  │         Code Editor         │  Runner Toolbar │
│  - Project tree │  - Line numbers & syntax   │  - Run / Pause  │
│  - Multi-files  │  - Multi-tab management     │  - Build & APK  │
│  - New / Delete │  - Quick accessory bar      │  - AI Prompt    │
└────────┬────────┴──────────────┬──────────────┴────────┬────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                   StudioViewModel & StateFlow                   │
│   - ActiveProject / CurrentFileContent / OpenTabs / Logs        │
└────────┬───────────────────────┬───────────────────────┬────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  AI Generator   │    │  Room Database  │    │ Sandbox Runner  │
│ - Blueprint Lib │    │ - ProjectEntity │    │ - WebView DOM   │
│ - Prompt Parser │    │ - FileEntity    │    │ - JS Console    │
│ - Gemini Bridge │    │ - ProjectDao    │    │ - Game Controller│
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

#### Core Entities & State Model
- `ProjectEntity`: `id`, `name`, `description`, `type` (WEB_APP, GAME, UTILITY), `createdAt`, `updatedAt`, `icon`.
- `ProjectFile`: `id`, `projectId`, `filePath` (e.g. `index.html`, `js/game.js`), `fileType` (HTML, CSS, JS, JSON), `content`, `updatedAt`.
- `EditorUiState`: `activeProjectId`, `activeFileId`, `openFileIds`, `isModified`, `cursorPosition`, `searchQuery`.
- `ConsoleMessage`: `timestamp`, `level` (LOG, WARN, ERROR, INFO), `text`.
- `GameControlState`: `dpadDirection`, `isButtonAPressed`, `isButtonBPressed`, `isJumpPressed`.

#### Interactive Component & State Mapping
- **Prompt-to-App Dialog**: Modal accepting user description with genre tags (Platformer, Shooter, Puzzle, Calculator, Todo). Triggers generator to create multi-file project and auto-opens in editor.
- **Editor Screen**: Line-numbered text area with custom syntax visual transformation, quick symbol keyboard (`{`, `}`, `(`, `)`, `;`, `=`, `<`, `>`, `"`), and real-time tab switching.
- **Sandbox Preview Screen**:
  - Fullscreen WebView rendering bundled HTML+CSS+JS.
  - Virtual Gamepad overlay (D-pad left, right, up, down; action buttons A, B). Injects `window.dispatchEvent(new KeyboardEvent('keydown', { key: ... }))` into the running game.
  - Live Collapsible Console: Captures `console.log` from webview and displays scrollable logs with stack traces.
- **Build & Export Screen**: Builds offline single-page HTML, exports ZIP archive to Android storage, and displays an APK Build Progress flow with simulated package verification and installation preview.
