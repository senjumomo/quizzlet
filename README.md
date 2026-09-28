# ⚡ QuizPulse — Live Google Sheet Trivia Engine

An interactive, responsive trivia web application that reads questions and answers in real-time from a Google Sheet database. Zero server configuration or API keys required!

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Platform](https://img.shields.io/badge/platform-GitHub%20Pages-brightgreen)
![Status](https://img.shields.io/badge/status-active-success)

🎮 **Live Web App**: [https://senjumomo.github.io/quizzlet/](https://senjumomo.github.io/quizzlet/)

---

## ✨ Features

- **📊 Live Google Sheets Database**: Fetches questions directly from your Google Sheet using Google's Visualization API (JSONP). When you update your sheet, the quiz updates automatically.
- **🏷️ Dynamic Discovery**: Auto-detects all categories and subcategories present in your spreadsheet and populates the filter menus.
- **🔊 Built-in Web Audio SFX**: Synthesized sound effects generated natively using the browser's Web Audio API (no external sound files to download).
- **🎉 Interactive Celebrations**: Real-time canvas confetti, streak counters, and performance scorecards.
- **⌨️ Keyboard Accessible**: Play using `A`/`B`/`C`/`D` or `1`/`2`/`3`/`4` keys, and advance with `Enter` or `Space`.
- **📱 Fully Responsive**: Glassmorphic, modern dark-mode design optimized for mobile, tablet, and desktop screens.

---

## 📋 Google Sheet Setup & Schema

The application connects to Google Sheets via its public spreadsheet ID.

- **Current Live Sheet**: [Open Google Sheet Database](https://docs.google.com/spreadsheets/d/1o3kNHih-35T5-ssvNaJ9i4TAs38zSQHKyhBqTZ9AYX4/edit?usp=sharing)

### Spreadsheet Columns

Ensure your Google Sheet headers follow this exact column order:

| Col | Header | Description | Example |
|---|---|---|---|
| A | **Category** | Topic name | `Mythology` |
| B | **Subcategory** | Subtopic or specific lore | `Greek` |
| C | **Difficulty** | Difficulty level | `Easy`, `Medium`, or `Hard` |
| D | **Question** | Trivia question prompt | `Who is the king of the Olympian gods?` |
| E | **Correct Answer** | The correct answer | `Zeus` |
| F | **Incorrect 1** | Distractor option 1 | `Poseidon` |
| G | **Incorrect 2** | Distractor option 2 | `Hades` |
| H | **Incorrect 3** | Distractor option 3 | `Apollo` |

> **Important**: In Google Sheets, make sure your sheet is shared with:  
> **"Anyone with the link can view"** (`Share` -> `General access` -> `Anyone with the link: Viewer`).

---

## 🚀 How to Deploy to GitHub Pages

You can host this project on GitHub Pages completely free so anyone can use it.

### 1. Create a New Repository on GitHub
1. Go to [github.com/new](https://github.com/new).
2. Choose a repository name (e.g. `quiz-app` or `quizpulse`).
3. Set the repository visibility to **Public**.
4. Leave "Add a README file" **unchecked** (we already have one).
5. Click **Create repository**.

### 2. Push Your Local Code
Open PowerShell or your terminal in this project folder and run:

```bash
# Initialize git if not already initialized
git init -b main

# Add and commit your files
git add .
git commit -m "Initial commit: QuizPulse Google Sheet Trivia Engine"

# Link your remote repository
git remote add origin https://github.com/senjumomo/quizzlet.git

# Push to GitHub
git push -u origin main
```

### 3. Turn on GitHub Pages
1. Go to your repository at [github.com/senjumomo/quizzlet](https://github.com/senjumomo/quizzlet).
2. Click **Settings** (top navigation tab).
3. In the left sidebar, click **Pages** (under the "Code and automation" section).
4. Under **Build and deployment**:
   - **Source**: Select `Deploy from a branch`.
   - **Branch**: Select `main` and folder `/ (root)`.
5. Click **Save**.

After 1–2 minutes, GitHub will publish your site at:
```
https://senjumomo.github.io/quizzlet/
```

---

## ⚙️ Using Your Own Google Sheet

If you ever want to point the quiz to a different Google Sheet:

1. Open `index.html`.
2. Find the constant on line ~670:
   ```javascript
   const SPREADSHEET_ID = 'YOUR_NEW_SPREADSHEET_ID_HERE';
   ```
3. Update the ID extracted from your sheet URL:
   `https://docs.google.com/spreadsheets/d/`**`<SPREADSHEET_ID>`**`/edit`
4. Commit and push your change.

---

## 🛠️ Local Testing

Simply double-click `index.html` to open it in any web browser, or use a local server:

```bash
npx serve .
```

---

## 📄 License
This project is open-source and free to use under the [MIT License](LICENSE).
