# Campus Map Navigator - Rungta International Skills University (RISU)

<p align="center">
  <img src="rungta_logo.png" alt="Rungta International Skills University Logo" width="160">
</p>

[![Java](https://img.shields.io/badge/Java-11%2B-orange.svg)](https://www.oracle.com/java/)
[![Campus](https://img.shields.io/badge/Campus-Rungta%20RISU%20Bhilai-gold.svg)](https://rungta.ac.in)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

A comprehensive Java console and graphical navigation application modeled with reference to the **80-acre integrated campus of Rungta International Skills University (RISU)** on Kohka-Kurud Road, Bhilai, Chhattisgarh.

---

## Project Overview

On an expansive university campus like **Rungta International Skills University (RISU)**, students, staff, and visitors must navigate between key administrative, academic, dining, and entrance facilities:
- **Gate (Main Entrance - Kohka-Kurud Rd):** Grand entrance archway and security checkpoint.
- **Admin (Administrative Block A):** Houses the Secretariat, Chancellor, Vice-Chancellor, and Central Admissions.
- **Library (Central Resource Hub):** Multi-story central library with reading halls and research databases.
- **Canteen (Food Court & Nescafe):** Central campus dining plaza and cafeteria lawn.
- **Hostel Block:** Residential zones (used in Test Case 3 to illustrate handling unlinked / non-existent network locations).
- **Sports Complex & Cricket Turf:** Landscaped recreation arenas integrated into the visual layout.

Depending on traveler priorities, commuters can find:
1. **Shortest Distance (Option 3 - Dijkstra):** Minimizes physical meters walked across campus.
2. **Fewest Stops (Option 4 - BFS):** Minimizes the number of intermediate building waypoints.

This application provides an interactive, robust, and extensible solution written entirely in standard Java without external dependencies.

---

## Campus Map Topology

The default campus graph contains **4 key locations** and **5 bidirectional roads** with specified distances in metres:

| Path Connection | Distance (m) | Path Type |
|---|---|---|
| **Gate <-> Library** | 100 m | Bidirectional (Two-way) |
| **Gate <-> Admin** | 50 m | Bidirectional (Two-way) |
| **Admin <-> Library** | 40 m | Bidirectional (Two-way) |
| **Library <-> Canteen** | 60 m | Bidirectional (Two-way) |
| **Admin <-> Canteen** | 200 m | Bidirectional (Two-way) |

### Graph Diagram
```
        [Gate]
       /      \
 50 m /        \ 100 m
     /          \
  [Admin]        [Library]
   |     \      /    |
   |      \40m /     |
   |       \  /      | 60 m
   | 200 m  \/       |
   |        /\       |
   +---[Canteen]-----+
```

---

## Key Concepts & Data Structures

| Concept | Implementation in Code | Purpose |
|---|---|---|
| **Graph Representation** | `List<List<Edge>> adjList` | Adjacency list storing outgoing weighted edges for each vertex. Space efficient for sparse campus maps ($O(V + E)$). |
| **Hashing** | `Map<String, Integer> nameToId` | Hash map providing $O(1)$ average time mapping from location names (case-insensitive) to vertex IDs. |
| **Reverse Mapping** | `List<String> idToName` | Fast $O(1)$ indexing from internal vertex ID back to canonical human-readable location name. |
| **Dijkstra's Algorithm** | `PriorityQueue<PQNode>` + `int[] dist` + `int[] parent` | Computes the single-source shortest path based on edge weights (metres) in $O((V + E) \log V)$ time. |
| **Breadth-First Search (BFS)** | `ArrayDeque<Integer>` + `boolean[] visited` + `int[] parent` | Computes the unweighted shortest path (fewest stops/hops) in $O(V + E)$ time. |
| **Path Reconstruction** | `parent[]` array backtrack | Traces back from destination vertex $v$ to source vertex $u$ using the parent pointer array. |

---

## Features

- **Interactive Console Menu:** Simple, user-friendly options for all operations.
- **Dynamic Location Management (Option 1):** Add custom buildings/locations at runtime.
- **Dynamic Road Creation (Option 2):** Add new bidirectional roads between existing locations with custom positive distances.
- **Dijkstra's Shortest Path (Option 3):** Computes exact shortest distance and prints step-by-step path.
- **BFS Fewest Stops (Option 4):** Computes minimal transition route, displaying stops count and cumulative distance.
- **Campus Map Visualizer (Option 5):** Prints all registered vertices and their connections with weights.
- **Automated Test Runner (Option 6):** Instantly runs and verifies all 4 benchmark test cases with pass/fail validation.
- **Negative Distance & Unknown Location Handling:** Rejects negative and zero weights; gracefully handles unregistered locations.

---

## Test Cases & Expected vs Actual Output

### Summary Verification Matrix

| Test Case | Type | Input Parameters | Expected Output | Actual Program Output | Status |
|---|---|---|---|---|---|
| **Test Case 1** | Normal Input | Option 3 (Shortest distance)<br>From: `Gate`, To: `Canteen` | Path: `Gate -> Admin -> Library -> Canteen`<br>Distance: `150 m` | Path: `Gate -> Admin -> Library -> Canteen`<br>Distance: `150 m` | **PASSED** |
| **Test Case 2** | Boundary / Minimum Input | Option 3 (Shortest distance)<br>From: `Gate`, To: `Gate` | Path: `Gate`<br>Distance: `0 m` | Path: `Gate`<br>Distance: `0 m` | **PASSED** |
| **Test Case 3** | Invalid Input | Option 3 (Shortest distance)<br>From: `Gate`, To: `Hostel` | `Location Hostel not found.` | `Location Hostel not found.` | **PASSED** |
| **Test Case 4 (a)** | Different Valid Scenario | Option 4 (Fewest stops)<br>From: `Admin`, To: `Canteen` | Path: `Admin -> Canteen`<br>Stops: `1`<br>Distance: `200 m` | Path: `Admin -> Canteen \| Stops: 1 \| Distance: 200 m` | **PASSED** |
| **Test Case 4 (b)** | Different Valid Scenario | Option 3 (Shortest distance)<br>From: `Admin`, To: `Canteen` | Path: `Admin -> Library -> Canteen`<br>Distance: `100 m` | Path: `Admin -> Library -> Canteen \| Distance: 100 m` | **PASSED** |

---

## How to Compile and Run

### Prerequisites
- Java Development Kit (JDK 8 or higher, JDK 11/17/21 recommended).
- Console / Terminal (Command Prompt, PowerShell, bash, or zsh).

### Option 1: Run with Batch / Shell Scripts
On **Windows**:
```cmd
gui.bat       # Compiles and launches Java Swing Graphical Desktop UI
run.bat       # Runs the interactive console app
test.bat      # Runs automated verification tests
```

On **Linux / macOS**:
```bash
chmod +x run.sh test.sh gui.sh
./gui.sh      # Compiles and launches Java Swing Graphical Desktop UI
./run.sh      # Runs the interactive console app
./test.sh     # Runs automated verification tests
```

### Option 2: Standard Command Line
Compile:
```bash
javac CampusMapApp.java CampusMapGUI.java
```

Run Desktop GUI:
```bash
java CampusMapGUI
# OR
java CampusMapApp --gui
```

Run Interactive Menu:
```bash
java CampusMapApp
```

Run Automated Test Suite directly:
```bash
java CampusMapApp --test
```

### Option 3: Browser Interactive UI
Double click `index.html` in the project folder to open the full interactive visual navigator in any web browser!

---

## Time & Space Complexity Analysis

| Operation / Algorithm | Time Complexity | Space Complexity | Explanation |
|---|---|---|---|
| **Location Lookup (Hashing)** | $O(1)$ average, $O(V)$ worst | $O(V)$ | Java's `HashMap` gives constant time retrieval for vertex IDs. |
| **Add Location / Add Road** | $O(1)$ amortized | $O(1)$ | Direct insertion into `HashMap` and `adjList`. |
| **Dijkstra's Algorithm** | $O((V + E) \log V)$ | $O(V)$ | Uses a binary min-heap (`PriorityQueue`) with at most $V$ extractions and $E$ relaxation operations. |
| **Breadth-First Search (BFS)** | $O(V + E)$ | $O(V)$ | Traverses each vertex and edge at most once using a FIFO queue. |
| **Path Reconstruction** | $O(L)$ where $L \le V$ | $O(L)$ | Backtracks through the `parent[]` array from target to source. |
| **Total Graph Storage** | - | $O(V + E)$ | Adjacency list storing only existing bidirectional edges. |

---

## Project Structure

```
CampusMapApp/
│
├── CampusMapApp.java       # Complete console Java application with graph logic
├── CampusMapGUI.java       # Native Java Swing Desktop GUI application
├── index.html              # Modern interactive HTML5 / Tailwind visualizer
├── README.md               # GitHub repository overview and documentation
├── PROJECT_REPORT.md       # Full formal academic / technical report
├── gui.bat                 # Windows launch script for Java Desktop GUI
├── run.bat                 # Windows launch script for Console App
├── test.bat                # Windows automated test execution script
├── gui.sh                  # Unix/Linux/macOS launch script for Desktop GUI
├── run.sh                  # Unix/Linux/macOS execution script
├── test.sh                 # Unix/Linux/macOS automated test script
└── .gitignore              # Git ignore rules for compiled binaries
```

---

## GitHub Upload Guide

To upload this project to your GitHub repository:

1. **Initialize Git Repository:**
   ```bash
   cd CampusMapApp
   git init
   ```

2. **Stage and Commit all Files:**
   ```bash
   git add .
   git commit -m "feat: complete campus map console app with Dijkstra, BFS, Hashing and test cases"
   ```

3. **Link to Remote GitHub Repository:**
   ```bash
   git branch -M main
   git remote add origin https://github.com/<your-username>/<your-repo-name>.git
   ```

4. **Push to GitHub:**
   ```bash
   git push -u origin main
   ```
