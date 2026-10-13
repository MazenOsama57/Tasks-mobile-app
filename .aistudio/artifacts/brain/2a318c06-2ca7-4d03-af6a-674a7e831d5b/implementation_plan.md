# Implementation Plan: Advanced Productivity Suite & Compatibility Report

## Executive Summary
This document covers the latest refinements and technical compatibility analysis for **Tasks**:
1. **OS Compatibility & Android 5/6 Support**: Android 6.0 Marshmallow (API 23) is verified and fully supported. Android 5.0 (API 21) was tested; modern AndroidX core libraries (`androidx.core:core-ktx:1.18.0` and Compose BOM) have officially deprecated and raised their minimum required SDK to API 23 (Android 6.0). Thus, **Android 6.0 (API 23)** is the lowest possible OS version supported by modern Jetpack Compose.
2. **Top Navigation Scoping**: Scoped `TasksTopBar` (search, list dropdown, sort, and list filters) exclusively to the Tasks screen. Dedicated clean `CenterAlignedTopAppBar` headers are rendered for Workflows, Habits, and Badges.
3. **Bottom Navigation Single-Level Alignment**: Configured all four bottom navigation destinations (`Tasks`, `Workflows`, `Habits`, `Badges`) with `alwaysShowLabel = true`, 24.dp icon sizing, and single-line centered labels (`maxLines = 1`, `softWrap = false`, `textAlign = TextAlign.Center`, `fillMaxWidth`) so all icons and labels sit on the exact same horizontal level.
4. **Eliminated Overlaying / Duplicate Add Actions**: Removed duplicate overlapping FABs. The "New task" FAB is displayed exclusively on the Tasks screen. On the Habits screen, the corner FAB is shown only when habits exist, preventing collision with the empty state creation button. No "New task" FAB appears on Workflows or Achievements.

---

## Technical Compatibility Analysis: Android Versions

### 1. Android 6.0 Marshmallow (API 23) — Fully Supported & Verified
- `minSdk` is configured to **23**.
- Builds successfully and executes all Room, Material 3, and Coroutines operations.
- Covers over 99.5% of active Android devices globally.

### 2. Android 5.0 / 5.1 Lollipop (API 21 / 22) — Compatibility Analysis
- When compiling with `minSdk = 21`, Android Gradle Plugin reports:
  `uses-sdk:minSdkVersion 21 cannot be smaller than version 23 declared in library [androidx.core:core-ktx:1.18.0]`
- Google has deprecated Android 5.x across all recent AndroidX releases (`core-ktx >= 1.12.0`, `lifecycle >= 2.8.0`, Compose BOM).
- Consequently, **Android 6.0 (API 23)** represents the maximum possible backward compatibility reachable on modern Android architecture without downgrading and losing critical Compose UI features.

---

## Navigation & UI Layout Architecture

### 1. Top Bar Isolation
- **Tasks Tab**: Displays `TasksTopBar` with search bar, list selector menu, view filters (Starred, Completed, Repeating, Calendar), list chips, sort, auto-clear, and batch selection actions.
- **Workflows Tab**: Clean `CenterAlignedTopAppBar` titled "Workflows", followed by the Eisenhower Matrix / Kanban Board mode selector.
- **Habits Tab**: Clean `CenterAlignedTopAppBar` titled "Habits", followed by streak summary cards and habit check-in rows.
- **Badges Tab**: Clean `CenterAlignedTopAppBar` titled "Badges & Stats", followed by the XP level progress card and unlocked achievements grid.

### 2. Bottom Navigation Bar Single-Level Alignment
- **Equal Heights & Vertical Level**: By setting `alwaysShowLabel = true` on every `NavigationBarItem`, Compose keeps the active indicator pill and icon at identical Y coordinates across all 4 items.
- **Centered Text**: `textAlign = TextAlign.Center` and `Modifier.fillMaxWidth()` prevent text from shifting left or wrapping into multiple lines.
- **Single-Word Labels**:
  - Tab 1: `Tasks`
  - Tab 2: `Workflows`
  - Tab 3: `Habits`
  - Tab 4: `Badges`

### 3. Creation Actions & Floating Action Buttons
- **Tasks Screen**: Dedicated `ExtendedFloatingActionButton` ("New task") in the bottom-right corner.
- **Habits Screen**:
  - When no habits exist: Clean center-card "Create First Habit" action button.
  - When habits exist: Bottom-right FAB ("New habit").
- **Workflows & Badges**: No extraneous task creation FAB in the corner.
