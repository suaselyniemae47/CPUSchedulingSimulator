# CPU Scheduling Simulator

A Java application with both a **Graphical User Interface (Swing)** and a **Command-Line Interface (CLI)** for simulating and analyzing classic Operating System CPU scheduling algorithms.

## Supported Algorithms
- **FCFS** — First-Come, First-Served (Non-Preemptive)
- **SJF** — Shortest Job First (Non-Preemptive)
- **SRTF** — Shortest Remaining Time First (Preemptive)
- **Priority Scheduling** — Non-Preemptive (lower number = higher priority)
- **Round Robin (RR)** — Preemptive time-sliced scheduling (default Time Quantum = `3` in GUI, configurable in CLI)

## Features
- **Interactive Swing GUI (`CPUSchedulerGUI.java`):**
  - Dropdown selector for all 5 scheduling algorithms.
  - Input fields for 4 processes (`P1`–`P4`) with **Priority (`PT`)**, **Arrival Time (`AT`)**, and **Burst Time (`BT`)**.
  - Color-coded visual **Gantt Chart** (including `IDLE` CPU time blocks).
  - **Details Table** displaying `PT`, `AT`, `BT`, Completion Time (`CT`), Turnaround Time (`TAT`), Waiting Time (`WT`), and Response Time (`RT`).
  - Automatic calculation of **Average Waiting Time**, **Average Turnaround Time**, and **Average Response Time**.
- **Console Simulator (`CPUSchedulerSimulator.java`):**
  - Supports custom process counts and custom Round Robin time quanta via terminal prompts.
  - Prints formatted ASCII Gantt charts, per-process metrics, and overall averages.

## Prerequisites
- **Java Development Kit (JDK) 8** or higher

## How to Compile and Run

1. **Compile both source files:**
   ```bash
   javac CPUSchedulerSimulator.java CPUSchedulerGUI.java
   ```

2. **Run the GUI version (Recommended):**
   ```bash
   java CPUSchedulerGUI
   ```

3. **Or run the Console (CLI) version:**
   ```bash
   java CPUSchedulerSimulator
   ```

## Project Structure
```text
├── CPUSchedulerGUI.java        # Java Swing graphical interface & Gantt chart renderer
├── CPUSchedulerSimulator.java  # Core scheduling algorithms & CLI entry point
└── .gitignore                  # Git ignore rules
```

## Metrics Formulas Used
- **Completion Time (`CT`):** Time at which a process finishes execution
- **Turnaround Time (`TAT`):** `CT - AT`
- **Waiting Time (`WT`):** `TAT - BT`
- **Response Time (`RT`):** `First CPU Execution Time - AT`
