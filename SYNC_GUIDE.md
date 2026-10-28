# Dead Sleep - Auto Git Sync

This repository is connected to [https://github.com/tanjeem180hz/Dead-Sleep](https://github.com/tanjeem180hz/Dead-Sleep) on branch `main`.

## 🔄 Automatic Synchronization Setup

Whenever you work on this project, changes can be automatically synced and pushed to GitHub:

### 1. Continuous Auto-Push Monitor
- **Double-click `auto-push.bat`**: Opens a lightweight monitor window that checks for changes every 15 seconds. Whenever you create, modify, or delete any file, it automatically stages, commits with a timestamp, and pushes to GitHub.
- **Silent Background Run (`auto-push-background.vbs`)**: Runs the auto-push watcher completely hidden in the background without opening a terminal window.
- **Stop Monitor (`stop-auto-push.bat`)**: Immediately terminates any running auto-push background monitor.

### 2. Instant 1-Click Sync
- **Double-click `sync-now.bat`**: Immediately stages all files, commits, and pushes them to GitHub in one click.

### 3. Automatic Git Hook
- A `post-commit` hook is installed in `.git/hooks/post-commit`. Any manual `git commit` you make in any terminal or IDE (Android Studio, VS Code, Git CLI) will automatically push to GitHub immediately.

---
*Status: Repository connected, up-to-date, and automated.*
