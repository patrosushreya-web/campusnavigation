import java.util.*;

/**
 * CampusMapApp - Console Application for Campus Navigation
 *
 * Concepts implemented:
 * - Graph: Undirected, weighted graph represented as an Adjacency List.
 * - Hashing: HashMap mapping location names to integer vertex IDs (O(1) average lookup),
 *            and an ArrayList for reverse mapping (vertex ID to name).
 * - Dijkstra's Algorithm: PriorityQueue-based shortest path by distance with parent array reconstruction.
 * - Breadth-First Search (BFS): Queue-based path with fewest stops (hops) with parent array reconstruction.
 * - Dynamic graph manipulation: Add Location and Add Path with validation.
 * - Error Handling: Unknown location reporting, negative distance rejection, boundary conditions.
 */
public class CampusMapApp {

    // Representation of a weighted edge to a neighbor vertex
    static class Edge {
        int targetVertex;
        int weight; // distance in meters

        public Edge(int targetVertex, int weight) {
            this.targetVertex = targetVertex;
            this.weight = weight;
        }
    }

    // Node for Dijkstra's Priority Queue
    static class PQNode implements Comparable<PQNode> {
        int vertex;
        int distance;

        public PQNode(int vertex, int distance) {
            this.vertex = vertex;
            this.distance = distance;
        }

        @Override
        public int compareTo(PQNode other) {
            return Integer.compare(this.distance, other.distance);
        }
    }

    // Class to encapsulate the result of path calculations
    public static class PathResult {
        public boolean success;
        public String errorMessage;
        public List<String> path;
        public int totalDistance;
        public int stops; // number of edges / hops

        public PathResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.path = new ArrayList<>();
            this.totalDistance = 0;
            this.stops = 0;
        }

        public PathResult(List<String> path, int totalDistance, int stops) {
            this.success = true;
            this.errorMessage = null;
            this.path = path;
            this.totalDistance = totalDistance;
            this.stops = stops;
        }

        public String getPathString() {
            return String.join(" -> ", path);
        }
    }

    // Graph Data Structures
    // HashMap for mapping Location Name (case-insensitive key) to Vertex ID
    private final Map<String, Integer> nameToId;
    // List for reverse mapping Vertex ID to canonical Location Name
    private final List<String> idToName;
    // Adjacency list: adjList.get(u) contains all outgoing edges from vertex u
    private final List<List<Edge>> adjList;

    /**
     * Constructor initializing empty graph structures and loading default campus map.
     */
    public CampusMapApp() {
        this.nameToId = new HashMap<>();
        this.idToName = new ArrayList<>();
        this.adjList = new ArrayList<>();
        initializeDefaultMap();
    }

    /**
     * Initializes default locations and two-way roads as per the problem specification:
     * Gate-Library: 100m, Gate-Admin: 50m, Admin-Library: 40m, Library-Canteen: 60m, Admin-Canteen: 200m
     */
    public final void initializeDefaultMap() {
        // Clear existing data if any
        nameToId.clear();
        idToName.clear();
        adjList.clear();

        // Add standard locations
        addLocation("Gate");
        addLocation("Admin");
        addLocation("Library");
        addLocation("Canteen");

        // Add bidirectional paths with distances in metres
        addPathInternal("Gate", "Library", 100);
        addPathInternal("Gate", "Admin", 50);
        addPathInternal("Admin", "Library", 40);
        addPathInternal("Library", "Canteen", 60);
        addPathInternal("Admin", "Canteen", 200);
    }

    /**
     * Adds a new location to the campus map.
     *
     * @param locationName the name of the campus location
     * @return true if added, false if already exists or invalid
     */
    public boolean addLocation(String locationName) {
        if (locationName == null || locationName.trim().isEmpty()) {
            System.out.println("Error: Location name cannot be empty.");
            return false;
        }
        String trimmed = locationName.trim();
        String lookupKey = trimmed.toLowerCase();

        if (nameToId.containsKey(lookupKey)) {
            System.out.println("Notice: Location '" + trimmed + "' already exists.");
            return false;
        }

        int newId = idToName.size();
        nameToId.put(lookupKey, newId);
        idToName.add(trimmed);
        adjList.add(new ArrayList<>());
        return true;
    }

    /**
     * Helper to add path internally without duplicate user validation prints.
     */
    private boolean addPathInternal(String from, String to, int distance) {
        String keyFrom = from.trim().toLowerCase();
        String keyTo = to.trim().toLowerCase();

        if (!nameToId.containsKey(keyFrom) || !nameToId.containsKey(keyTo)) {
            return false;
        }

        int u = nameToId.get(keyFrom);
        int v = nameToId.get(keyTo);

        // Undirected graph: two-way road
        adjList.get(u).add(new Edge(v, distance));
        adjList.get(v).add(new Edge(u, distance));
        return true;
    }

    /**
     * Public method to add a path between two locations with validation.
     * Validates positive distance and checks whether both locations exist.
     *
     * @param from     starting location name
     * @param to       destination location name
     * @param distance distance in metres
     * @return true if successfully added, false otherwise
     */
    public boolean addPath(String from, String to, int distance) {
        if (distance < 0) {
            System.out.println("Error: Distance cannot be negative (" + distance + " m). Negative distances are not allowed.");
            return false;
        }
        if (distance == 0) {
            System.out.println("Error: Distance between two distinct locations must be greater than 0.");
            return false;
        }

        String keyFrom = (from != null) ? from.trim().toLowerCase() : "";
        String keyTo = (to != null) ? to.trim().toLowerCase() : "";

        if (!nameToId.containsKey(keyFrom)) {
            System.out.println("Location " + from + " not found.");
            return false;
        }
        if (!nameToId.containsKey(keyTo)) {
            System.out.println("Location " + to + " not found.");
            return false;
        }

        int u = nameToId.get(keyFrom);
        int v = nameToId.get(keyTo);

        if (u == v) {
            System.out.println("Error: Source and destination are the same location.");
            return false;
        }

        // Check if edge already exists, if so update weight or add parallel
        for (Edge e : adjList.get(u)) {
            if (e.targetVertex == v) {
                System.out.println("Notice: Updating existing path between " + idToName.get(u) + " and " + idToName.get(v) + " to " + distance + " m.");
                e.weight = distance;
                for (Edge reverseEdge : adjList.get(v)) {
                    if (reverseEdge.targetVertex == u) {
                        reverseEdge.weight = distance;
                        break;
                    }
                }
                return true;
            }
        }

        adjList.get(u).add(new Edge(v, distance));
        adjList.get(v).add(new Edge(u, distance));
        System.out.println("Path added successfully: " + idToName.get(u) + " <-> " + idToName.get(v) + " (" + distance + " m)");
        return true;
    }

    /**
     * Finds the shortest path by distance using Dijkstra's Algorithm.
     * Reconstructs the full path using a parent array.
     *
     * @param fromName source location name
     * @param toName   destination location name
     * @return PathResult containing path, distance, and status
     */
    public PathResult findShortestDistance(String fromName, String toName) {
        // Validate inputs
        if (fromName == null || fromName.trim().isEmpty()) {
            return new PathResult(false, "Source location cannot be empty.");
        }
        if (toName == null || toName.trim().isEmpty()) {
            return new PathResult(false, "Destination location cannot be empty.");
        }

        String keyFrom = fromName.trim().toLowerCase();
        String keyTo = toName.trim().toLowerCase();

        if (!nameToId.containsKey(keyFrom)) {
            return new PathResult(false, "Location " + fromName.trim() + " not found.");
        }
        if (!nameToId.containsKey(keyTo)) {
            return new PathResult(false, "Location " + toName.trim() + " not found.");
        }

        int startId = nameToId.get(keyFrom);
        int destId = nameToId.get(keyTo);

        // Boundary case: Source and Destination are identical (0 meters)
        if (startId == destId) {
            List<String> singlePath = new ArrayList<>();
            singlePath.add(idToName.get(startId));
            return new PathResult(singlePath, 0, 0);
        }

        int n = idToName.size();
        int[] dist = new int[n];
        int[] parent = new int[n];
        boolean[] visited = new boolean[n];

        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);

        PriorityQueue<PQNode> pq = new PriorityQueue<>();
        dist[startId] = 0;
        pq.offer(new PQNode(startId, 0));

        while (!pq.isEmpty()) {
            PQNode current = pq.poll();
            int u = current.vertex;

            if (visited[u]) continue;
            visited[u] = true;

            // Early exit if destination reached
            if (u == destId) break;

            for (Edge edge : adjList.get(u)) {
                int v = edge.targetVertex;
                int weight = edge.weight;

                if (!visited[v] && dist[u] != Integer.MAX_VALUE && dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    parent[v] = u;
                    pq.offer(new PQNode(v, dist[v]));
                }
            }
        }

        // Check if destination was reachable
        if (dist[destId] == Integer.MAX_VALUE) {
            return new PathResult(false, "No path exists between " + idToName.get(startId) + " and " + idToName.get(destId) + ".");
        }

        // Reconstruct path using parent array
        List<String> path = reconstructPath(parent, startId, destId);
        int stops = path.size() - 1;
        return new PathResult(path, dist[destId], stops);
    }

    /**
     * Finds the path with the fewest stops (hops) using Breadth-First Search (BFS).
     * Reconstructs the full path using a parent array and computes the cumulative distance.
     *
     * @param fromName source location name
     * @param toName   destination location name
     * @return PathResult containing path, stops, distance, and status
     */
    public PathResult findFewestStops(String fromName, String toName) {
        // Validate inputs
        if (fromName == null || fromName.trim().isEmpty()) {
            return new PathResult(false, "Source location cannot be empty.");
        }
        if (toName == null || toName.trim().isEmpty()) {
            return new PathResult(false, "Destination location cannot be empty.");
        }

        String keyFrom = fromName.trim().toLowerCase();
        String keyTo = toName.trim().toLowerCase();

        if (!nameToId.containsKey(keyFrom)) {
            return new PathResult(false, "Location " + fromName.trim() + " not found.");
        }
        if (!nameToId.containsKey(keyTo)) {
            return new PathResult(false, "Location " + toName.trim() + " not found.");
        }

        int startId = nameToId.get(keyFrom);
        int destId = nameToId.get(keyTo);

        // Boundary case: Source and Destination are identical
        if (startId == destId) {
            List<String> singlePath = new ArrayList<>();
            singlePath.add(idToName.get(startId));
            return new PathResult(singlePath, 0, 0);
        }

        int n = idToName.size();
        boolean[] visited = new boolean[n];
        int[] parent = new int[n];
        int[] stops = new int[n];

        Arrays.fill(parent, -1);
        Arrays.fill(stops, 0);

        Queue<Integer> queue = new ArrayDeque<>();
        visited[startId] = true;
        queue.offer(startId);

        boolean reached = false;
        while (!queue.isEmpty()) {
            int u = queue.poll();

            if (u == destId) {
                reached = true;
                break;
            }

            for (Edge edge : adjList.get(u)) {
                int v = edge.targetVertex;
                if (!visited[v]) {
                    visited[v] = true;
                    parent[v] = u;
                    stops[v] = stops[u] + 1;
                    queue.offer(v);

                    if (v == destId) {
                        reached = true;
                        break;
                    }
                }
            }
            if (reached) break;
        }

        if (!visited[destId]) {
            return new PathResult(false, "No path exists between " + idToName.get(startId) + " and " + idToName.get(destId) + ".");
        }

        // Reconstruct path using parent array
        List<String> path = reconstructPath(parent, startId, destId);

        // Compute total distance along the BFS path
        int totalDistance = calculatePathDistance(path);
        int stopCount = stops[destId];

        return new PathResult(path, totalDistance, stopCount);
    }

    /**
     * Returns an unmodifiable list of all canonical location names.
     */
    public List<String> getLocations() {
        return Collections.unmodifiableList(idToName);
    }

    /**
     * Returns an unmodifiable view of outgoing edges for vertex u.
     */
    public List<Edge> getEdges(int vertexId) {
        if (vertexId < 0 || vertexId >= adjList.size()) return Collections.emptyList();
        return Collections.unmodifiableList(adjList.get(vertexId));
    }

    /**
     * Returns vertex ID for a given name or -1 if not found.
     */
    public int getVertexId(String name) {
        if (name == null) return -1;
        Integer id = nameToId.get(name.trim().toLowerCase());
        return (id != null) ? id : -1;
    }

    /**
     * Reconstructs the path from destination to source using the parent array.
     */
    private List<String> reconstructPath(int[] parent, int startId, int destId) {
        LinkedList<String> path = new LinkedList<>();
        int curr = destId;
        while (curr != -1) {
            path.addFirst(idToName.get(curr));
            if (curr == startId) break;
            curr = parent[curr];
        }
        return path;
    }

    /**
     * Calculates the cumulative distance of a reconstructed path.
     */
    private int calculatePathDistance(List<String> path) {
        int dist = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int u = nameToId.get(path.get(i).toLowerCase());
            int v = nameToId.get(path.get(i + 1).toLowerCase());
            // Find edge distance between u and v
            for (Edge edge : adjList.get(u)) {
                if (edge.targetVertex == v) {
                    dist += edge.weight;
                    break;
                }
            }
        }
        return dist;
    }

    /**
     * Displays all current locations and adjacency list representations.
     */
    public void displayMap() {
        System.out.println("\n----------------- CAMPUS MAP OVERVIEW -----------------");
        System.out.println("Total Locations: " + idToName.size());
        for (int i = 0; i < idToName.size(); i++) {
            System.out.print("[" + i + "] " + idToName.get(i) + " connects to: ");
            List<Edge> edges = adjList.get(i);
            if (edges.isEmpty()) {
                System.out.println("(No connected paths)");
            } else {
                List<String> connections = new ArrayList<>();
                for (Edge e : edges) {
                    connections.add(idToName.get(e.targetVertex) + " (" + e.weight + " m)");
                }
                System.out.println(String.join(", ", connections));
            }
        }
        System.out.println("------------------------------------------------------\n");
    }

    /**
     * Runs automated verification of all 4 test cases specified in the requirements.
     */
    public static void runAutomatedTests() {
        CampusMapApp map = new CampusMapApp();
        System.out.println("\n=======================================================");
        System.out.println("      RUNNING AUTOMATED TEST CASES (1 to 4)");
        System.out.println("=======================================================");

        // Test Case 1: Normal Input
        System.out.println("\n--- [TEST CASE 1] Normal Input ---");
        System.out.println("Option 3 (Shortest distance)");
        System.out.println("From: Gate, To: Canteen");
        PathResult tc1 = map.findShortestDistance("Gate", "Canteen");
        if (tc1.success) {
            System.out.println("Path: " + tc1.getPathString());
            System.out.println("Distance: " + tc1.totalDistance + " m");
            boolean pass = tc1.getPathString().equals("Gate -> Admin -> Library -> Canteen") && tc1.totalDistance == 150;
            System.out.println("Status: " + (pass ? "PASSED [OK]" : "FAILED"));
        } else {
            System.out.println(tc1.errorMessage);
            System.out.println("Status: FAILED");
        }

        // Test Case 2: Boundary / Minimum Input
        System.out.println("\n--- [TEST CASE 2] Boundary / Minimum Input ---");
        System.out.println("Option 3 (Shortest distance)");
        System.out.println("From: Gate, To: Gate");
        PathResult tc2 = map.findShortestDistance("Gate", "Gate");
        if (tc2.success) {
            System.out.println("Path: " + tc2.getPathString());
            System.out.println("Distance: " + tc2.totalDistance + " m");
            boolean pass = tc2.getPathString().equals("Gate") && tc2.totalDistance == 0;
            System.out.println("Status: " + (pass ? "PASSED [OK]" : "FAILED"));
        } else {
            System.out.println(tc2.errorMessage);
            System.out.println("Status: FAILED");
        }

        // Test Case 3: Invalid Input
        System.out.println("\n--- [TEST CASE 3] Invalid Input ---");
        System.out.println("Option 3 (Shortest distance)");
        System.out.println("From: Gate, To: Hostel");
        PathResult tc3 = map.findShortestDistance("Gate", "Hostel");
        if (!tc3.success) {
            System.out.println(tc3.errorMessage);
            boolean pass = tc3.errorMessage.equals("Location Hostel not found.");
            System.out.println("Status: " + (pass ? "PASSED [OK]" : "FAILED"));
        } else {
            System.out.println("Path: " + tc3.getPathString());
            System.out.println("Status: FAILED (Expected error)");
        }

        // Test Case 4: Different Valid Scenario
        System.out.println("\n--- [TEST CASE 4] Different Valid Scenario ---");
        System.out.println("(a) Option 4 (Fewest stops): Admin to Canteen");
        PathResult tc4a = map.findFewestStops("Admin", "Canteen");
        if (tc4a.success) {
            System.out.println("(a) Path: " + tc4a.getPathString() + " | Stops: " + tc4a.stops + " | Distance: " + tc4a.totalDistance + " m");
            boolean passA = tc4a.getPathString().equals("Admin -> Canteen") && tc4a.stops == 1 && tc4a.totalDistance == 200;
            System.out.println("    Status: " + (passA ? "PASSED [OK]" : "FAILED"));
        } else {
            System.out.println(tc4a.errorMessage);
            System.out.println("    Status: FAILED");
        }

        System.out.println("(b) Option 3 (Shortest distance): Admin to Canteen");
        PathResult tc4b = map.findShortestDistance("Admin", "Canteen");
        if (tc4b.success) {
            System.out.println("(b) Path: " + tc4b.getPathString() + " | Distance: " + tc4b.totalDistance + " m");
            boolean passB = tc4b.getPathString().equals("Admin -> Library -> Canteen") && tc4b.totalDistance == 100;
            System.out.println("    Status: " + (passB ? "PASSED [OK]" : "FAILED"));
        } else {
            System.out.println(tc4b.errorMessage);
            System.out.println("    Status: FAILED");
        }

        System.out.println("\n=======================================================");
        System.out.println("              ALL TEST CASES COMPLETED");
        System.out.println("=======================================================\n");
    }

    /**
     * Interactive console menu.
     */
    public static void main(String[] args) {
        // If arguments contain --test or -t, run automated tests and return
        if (args.length > 0 && (args[0].equalsIgnoreCase("--test") || args[0].equalsIgnoreCase("-t") || args[0].equalsIgnoreCase("test"))) {
            runAutomatedTests();
            return;
        }

        // If arguments contain --gui or -g, launch graphical desktop UI
        if (args.length > 0 && (args[0].equalsIgnoreCase("--gui") || args[0].equalsIgnoreCase("-g") || args[0].equalsIgnoreCase("gui"))) {
            CampusMapGUI.main(args);
            return;
        }

        CampusMapApp app = new CampusMapApp();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("==================================================");
        System.out.println("  RUNGTA INTERNATIONAL SKILLS UNIVERSITY (RISU)  ");
        System.out.println("      Campus Map Navigator (Bhilai Campus)        ");
        System.out.println("==================================================");

        while (running) {
            System.out.println("\n-------------------- MAIN MENU -------------------");
            System.out.println("1. Add Location");
            System.out.println("2. Add Path (Two-way Road)");
            System.out.println("3. Find Shortest Path by Distance (Dijkstra)");
            System.out.println("4. Find Path with Fewest Stops (BFS)");
            System.out.println("5. Display Campus Map");
            System.out.println("6. Run Automated Test Cases (1 to 4)");
            System.out.println("7. Reset to Default Campus Map");
            System.out.println("8. Launch Graphical User Interface (GUI Window)");
            System.out.println("9. Exit");
            System.out.println("--------------------------------------------------");
            System.out.print("Enter your option (1-9): ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1": {
                    System.out.print("Enter new location name: ");
                    String name = scanner.nextLine().trim();
                    if (app.addLocation(name)) {
                        System.out.println("Success: Location '" + name + "' added successfully.");
                    }
                    break;
                }
                case "2": {
                    System.out.print("Enter from location: ");
                    String from = scanner.nextLine().trim();
                    System.out.print("Enter to location: ");
                    String to = scanner.nextLine().trim();
                    System.out.print("Enter distance in metres: ");
                    String distStr = scanner.nextLine().trim();
                    try {
                        int dist = Integer.parseInt(distStr);
                        app.addPath(from, to, dist);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Invalid distance format. Please enter an integer value.");
                    }
                    break;
                }
                case "3": { // Shortest distance (Dijkstra)
                    System.out.print("From: ");
                    String from = scanner.nextLine().trim();
                    System.out.print("To: ");
                    String to = scanner.nextLine().trim();

                    PathResult res = app.findShortestDistance(from, to);
                    if (res.success) {
                        System.out.println("Path: " + res.getPathString());
                        System.out.println("Distance: " + res.totalDistance + " m");
                    } else {
                        System.out.println(res.errorMessage);
                    }
                    break;
                }
                case "4": { // Fewest stops (BFS)
                    System.out.print("From: ");
                    String from = scanner.nextLine().trim();
                    System.out.print("To: ");
                    String to = scanner.nextLine().trim();

                    PathResult res = app.findFewestStops(from, to);
                    if (res.success) {
                        System.out.println("Path: " + res.getPathString() + " | Stops: " + res.stops + " | Distance: " + res.totalDistance + " m");
                    } else {
                        System.out.println(res.errorMessage);
                    }
                    break;
                }
                case "5": {
                    app.displayMap();
                    break;
                }
                case "6": {
                    runAutomatedTests();
                    break;
                }
                case "7": {
                    app.initializeDefaultMap();
                    System.out.println("Campus map reset to default configuration successfully.");
                    break;
                }
                case "8": {
                    System.out.println("Opening Campus Map GUI window...");
                    CampusMapGUI.main(new String[0]);
                    break;
                }
                case "9": {
                    System.out.println("Exiting Campus Map Navigator. Goodbye!");
                    running = false;
                    break;
                }
                default:
                    System.out.println("Invalid option. Please enter a number from 1 to 9.");
            }
        }
        scanner.close();
    }
}
