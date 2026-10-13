# Implementation Plan: Advanced Productivity Suite & Gamified Workflows

## Executive Summary
Following the competitive benchmark against leading productivity platforms (**Todoist**, **TickTick**, **Things 3**, and **Habitica**), this plan introduces five major power-user features into **Tasks**, structured under an intuitive bottom navigation system:
1. **Habit Tracking & Daily Routines with Streaks**
2. **Eisenhower Matrix 2×2 Decision Grid**
3. **Visual Kanban Board View**
4. **Smart Natural Language Quick-Add Parser**
5. **Gamified Achievement Badges & Productivity XP Levels**

All new workflows are integrated cleanly with local offline Room persistence, existing task lists, the Pomodoro focus timer, and Material 3 design principles.

---

## Architecture & Navigation Structure

### App Navigation Hub
Transition from single-screen modal navigation to a cohesive **Material 3 Navigation Suite / Bottom Navigation Bar** with 4 dedicated primary destinations:
1. **Tasks (`tab_tasks`)**: Current comprehensive task list, filters, due date grouping, and calendar view.
2. **Workflows (`tab_workflows`)**: Switchable views between **Eisenhower Matrix (2×2)** and **Kanban Board**.
3. **Habits (`tab_habits`)**: Daily recurring routines, streak counters, check-in controls, and weekly consistency rings.
4. **Achievements & Stats (`tab_stats`)**: Productivity XP level progress, unlockable achievement badges, Pomodoro focus history, and completion metrics.

---

## Phase 1: Smart Natural Language Quick-Add Parser (Todoist-Style)

### Objectives
Enable rapid task capture by parsing dates, times, priority levels, and lists directly from user typing without forcing manual picker clicks.

### Key Capabilities
- **Date & Time Token Detection**:
  - Keywords: `today`, `tomorrow`, `tonight`, `next monday`, `in 3 days`, `friday`.
  - Specific dates: `oct 24`, `10/24`, `24th`.
  - Times: `at 3pm`, `at 15:30`, `9am`, `morning` (9:00 AM), `evening` (6:00 PM).
- **Priority Syntax**:
  - `!high` or `!p1` -> High Priority (Red badge).
  - `!med` or `!p2` -> Medium Priority (Amber badge).
  - `!low` or `!p3` -> Low Priority (Blue badge).
- **List Target Syntax**:
  - `#Work`, `#Personal`, `#Shopping` matches existing lists or suggests list assignment.
- **Interactive Parsing Preview**:
  - As the user types in `AddTaskBottomSheet` or Quick Add, detected tokens are highlighted and converted into clickable chip pills beneath the input field.
  - Tapping an extracted chip allows quick manual override or removal.

---

## Phase 2: Habit Tracking with Daily Routines & Streaks (TickTick / Habitica Style)

### Objectives
Bridge task management with daily self-improvement and recurring routine tracking.

### Data Layer (`com.example.data`)
- **`Habit` Entity**:
  - `id: Long` (Auto-generated primary key)
  - `title: String`
  - `description: String?`
  - `frequency: HabitFrequency` (`DAILY`, `WEEKDAYS`, `WEEKENDS`, `CUSTOM_DAYS`)
  - `targetDaysOfWeek: List<Int>` (e.g., `[1, 2, 3, 4, 5]`)
  - `color: Long` (M3 color accent)
  - `icon: String` (Routine icon: fitness, water, reading, code, meditation, study)
  - `targetTimesPerDay: Int` (e.g., 1 for yes/no check, or 3 for 3x daily)
  - `createdAt: Long`
  - `isArchived: Boolean`
- **`HabitLog` Entity**:
  - `id: Long`
  - `habitId: Long` (Foreign key to Habit)
  - `dateEpochDay: Long` (Standardized epoch day)
  - `count: Int` (Times completed on this date)
  - `completedAt: Long`
- **`HabitDao`**:
  - Stream all active habits with their 7-day completion logs.
  - Increment habit check-in, undo check-in, calculate current streak and best streak.

### UI & UX Experience
- **Daily Routine Deck**:
  - Quick circular check-in toggle button with smooth spring scale animation and haptic pulse.
  - Current streak counter with flame indicator (e.g., `🔥 14 days`).
  - 7-day mini heatmap dots showing completion status for the past week.
- **Habit Creator Dialog**:
  - Frequency selection, reminder time, icon picker, and routine goal setting.

---

## Phase 3: Eisenhower Matrix 2×2 Decision Grid

### Objectives
Help users prioritize what to execute immediately versus what to schedule or eliminate, based on the classic urgency-importance framework.

### Matrix Quadrants
1. **Q1: Do First (Urgent & Important)**: Tasks due today/overdue with High priority.
2. **Q2: Schedule (Important, Not Urgent)**: Tasks with High/Medium priority due later or with no immediate deadline.
3. **Q3: Delegate / Quick Win (Urgent, Not Important)**: Tasks due today/tomorrow with Normal/Low priority.
4. **Q4: Don't Do / Backlog (Neither)**: Tasks with Low priority and no urgent due date.

### Interaction & Customization
- **Quadrant Grid**:
  - Clean 4-card M3 grid layout with distinct themed container tones and quadrant counters.
  - Expand any quadrant to full view or collapse into a 2×2 split.
- **Drag or Move Action**:
  - Quick action menu on each task item to instantly assign to another quadrant (automatically re-calibrating its priority and due date).
- **Direct Add**:
  - Tapping `+` within any quadrant immediately creates a task pre-configured with that quadrant's urgency and importance values.

---

## Phase 4: Visual Kanban Board View

### Objectives
Provide an agile, workflow-oriented column view where tasks can be moved across progress stages.

### Workflow Stages
- **Default Board Columns**:
  - **To Do**: Open tasks pending start.
  - **In Progress**: Tasks actively being worked on (or linked to an ongoing Pomodoro session).
  - **Completed**: Tasks finished today or recently.
- **Alternative List-Based Board Mode**:
  - Switch board columns to represent user lists (e.g., *Personal*, *Work*, *Projects*) for side-by-side management.
- **Board UX**:
  - Horizontally swipeable column cards with column counters.
  - Quick move arrows (`-> Next Stage`, `<- Prev Stage`) and smooth status toggling.
  - Inline card action to start a Pomodoro timer directly on any Kanban card.

---

## Phase 5: Gamified Achievement Badges & Productivity Levels

### Objectives
Reward consistency and turn daily productivity into an engaging, gamified journey.

### Gamification Engine (`com.example.data.gamification`)
- **Experience Points (XP)**:
  - Completing a task: `+15 XP` (Bonus `+10 XP` for High Priority).
  - Completing a Subtask: `+5 XP`.
  - Finishing a 25m Pomodoro Focus Session: `+25 XP`.
  - Checking in a daily habit: `+20 XP`.
  - Maintaining a 3-day / 7-day streak: `+50 XP / +150 XP` bonus.
- **Productivity Levels**:
  - Level 1: *Novice Planner* (0 - 100 XP)
  - Level 2: *Focus Apprentice* (101 - 300 XP)
  - Level 3: *Task Strategist* (301 - 650 XP)
  - Level 4: *Momentum Master* (651 - 1,200 XP)
  - Level 5+: *Grandmaster of Flow* (1,201+ XP)
- **Badges & Trophies**:
  - **Early Bird**: Complete a task before 9:00 AM.
  - **Deep Diver**: Complete 4+ Pomodoro focus sessions in a day.
  - **Streak Igniter**: Maintain a 7-day streak in tasks or habits.
  - **Centurion**: Complete 100 total lifetime tasks.
  - **Zen Master**: Clear all pending tasks in your primary list.
  - **Multi-Task Finisher**: Complete all subtasks of a complex task.

### Achievements Showcase UI
- Level card with animated progress bar showing current XP and XP needed for next level.
- Grid of earned badges (colored, glowing) and locked badges (with progress requirements).
- Delightful celebration banner/toast when leveling up or unlocking a new badge.

---

## Phased Implementation Sequence

| Phase | Milestone | Description |
|---|---|---|
| **Phase 1** | **Navigation & Shell** | Add M3 Bottom Navigation Bar (`Tasks`, `Workflows`, `Habits`, `Stats & Badges`). |
| **Phase 2** | **Smart Parser** | Implement `NaturalLanguageTaskParser` with preview chips in task creation. |
| **Phase 3** | **Habit Engine** | Room entities (`Habit`, `HabitLog`), Habit DAO, and Habits Tab with streak rings. |
| **Phase 4** | **Workflows Tab** | 2×2 Eisenhower Matrix view and Kanban Board column view with switching tab. |
| **Phase 5** | **Gamification** | XP engine, levels, achievement badges, and Stats/Achievements screen. |

---

## User Review & Feedback Focus
Before starting implementation:
1. **Default Tab**: Would you prefer the app to always start on the **Tasks** tab, or remember the last opened tab?
2. **Workflows Selection**: In the new Workflows tab, is having a top segmented button to toggle between **Eisenhower Matrix** and **Kanban Board** your preferred UX?

*Click **Proceed** to start Phase 1.*
