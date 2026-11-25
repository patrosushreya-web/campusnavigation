import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * CampusMapGUI - Graphical User Interface for Campus Map Navigator
 *
 * Implements a modern Swing UI with:
 * - Interactive 2D Graph Canvas rendering nodes, edges, distances, and active paths
 * - Anti-aliased high-DPI graphics with glowing route highlights
 * - Controls for Dijkstra's Shortest Path and BFS Fewest Stops
 * - Quick-run buttons for Test Cases 1, 2, 3, 4a, and 4b
 * - Dynamic Add Location and Add Road dialogs
 * - Live formatted output log
 */
public class CampusMapGUI extends JFrame {

    private final CampusMapApp app;
    private final Map<String, Point> nodeCoordinates;
    private List<String> currentPath;
    private String currentAlgo;
    private int currentDistance;
    private int currentStops;

    // UI Components
    private JComboBox<String> fromComboBox;
    private JComboBox<String> toComboBox;
    private JTextArea logArea;
    private GraphPanel graphPanel;

    // Commuter Animation Engine
    private javax.swing.Timer commuterTimer;
    private boolean commuterActive = false;
    private int commuterSegment = 0;
    private double commuterProgress = 0.0;
    private Point2D.Double commuterPos = new Point2D.Double(120, 90);
    private final java.util.List<Point2D.Double> commuterTrail = new ArrayList<>();

    public CampusMapGUI() {
        this.app = new CampusMapApp();
        this.nodeCoordinates = new HashMap<>();
        this.currentPath = new ArrayList<>();
        this.currentAlgo = "None";
        this.currentDistance = 0;
        this.currentStops = 0;

        initCoordinates();
        initUI();
        initCommuterTimer();
    }

    private void initCommuterTimer() {
        commuterTimer = new javax.swing.Timer(25, e -> {
            if (!commuterActive || currentPath == null || currentPath.size() <= 1) return;

            if (commuterSegment >= currentPath.size() - 1) {
                commuterActive = false;
                appendLog("Commuter arrived at destination: " + currentPath.get(currentPath.size() - 1));
                graphPanel.repaint();
                return;
            }

            String u = currentPath.get(commuterSegment);
            String v = currentPath.get(commuterSegment + 1);
            Point p1 = nodeCoordinates.get(u);
            Point p2 = nodeCoordinates.get(v);

            if (p1 != null && p2 != null) {
                commuterPos.x = p1.x + (p2.x - p1.x) * commuterProgress;
                commuterPos.y = p1.y + (p2.y - p1.y) * commuterProgress;

                commuterTrail.add(new Point2D.Double(commuterPos.x, commuterPos.y));
                if (commuterTrail.size() > 16) commuterTrail.remove(0);

                commuterProgress += 0.025;
                if (commuterProgress >= 1.0) {
                    commuterProgress = 0.0;
                    commuterSegment++;
                }
                graphPanel.repaint();
            }
        });
        commuterTimer.start();
    }

    private void startCommuterAnimation() {
        if (currentPath == null || currentPath.size() <= 1) return;
        commuterActive = true;
        commuterSegment = 0;
        commuterProgress = 0.0;
        commuterTrail.clear();
        Point startP = nodeCoordinates.get(currentPath.get(0));
        if (startP != null) {
            commuterPos.x = startP.x;
            commuterPos.y = startP.y;
        }
        appendLog("Commuter walking from " + currentPath.get(0) + " along route...");
        graphPanel.repaint();
    }

        private void initCoordinates() {
        nodeCoordinates.clear();
        nodeCoordinates.put("Gate", new Point(350, 360));
        nodeCoordinates.put("Admin", new Point(520, 220));
        nodeCoordinates.put("Library", new Point(350, 130));
        nodeCoordinates.put("Canteen", new Point(180, 220));
    }

    private void updateDynamicCoordinates() {
        List<String> locations = app.getLocations();
        int total = locations.size();
        int cx = 300, cy = 180, rx = 200, ry = 110;

        for (int i = 0; i < total; i++) {
            String name = locations.get(i);
            if (!nodeCoordinates.containsKey(name)) {
                double angle = (2 * Math.PI * i) / total - Math.PI / 2;
                int x = (int) (cx + rx * Math.cos(angle));
                int y = (int) (cy + ry * Math.sin(angle));
                nodeCoordinates.put(name, new Point(x, y));
            }
        }
    }

    private void initUI() {
        setTitle("Rungta International Skills University (RISU) - Campus Map Navigator");
        setSize(1020, 700);
        setMinimumSize(new Dimension(880, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Use system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // --- TOP PANEL: Navigation Controls & Quick Test Cases ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBorder(new EmptyBorder(8, 12, 4, 12));
        topContainer.setBackground(new Color(245, 247, 250));

        // Row 1: Source & Destination + Actions
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        navBar.setOpaque(false);

        // Official Rungta University Logo badge
        JLabel logoLabel = new JLabel();
        try {
            java.io.File logoFile = new java.io.File("rungta_logo.png");
            if (logoFile.exists()) {
                ImageIcon orig = new ImageIcon("rungta_logo.png");
                Image scaled = orig.getImage().getScaledInstance(34, 34, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
                logoLabel.setToolTipText("Rungta International Skills University");
            } else {
                logoLabel.setText("🎓 ");
                logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            }
        } catch (Exception ex) {
            logoLabel.setText("🎓 ");
        }
        navBar.add(logoLabel);

        JLabel lblFrom = new JLabel("From:");
        lblFrom.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fromComboBox = new JComboBox<>();
        fromComboBox.setPreferredSize(new Dimension(130, 28));

        JLabel lblTo = new JLabel("To:");
        lblTo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        toComboBox = new JComboBox<>();
        toComboBox.setPreferredSize(new Dimension(130, 28));

        JButton btnDijkstra = new JButton("⚡ Shortest Dist (Dijkstra)");
        btnDijkstra.setBackground(new Color(37, 99, 235));
        btnDijkstra.setForeground(Color.WHITE);
        btnDijkstra.setFocusPainted(false);
        btnDijkstra.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDijkstra.addActionListener(e -> {
            executeDijkstra();
            startCommuterAnimation();
        });

        JButton btnBFS = new JButton("🛑 Fewest Stops (BFS)");
        btnBFS.setBackground(new Color(147, 51, 234));
        btnBFS.setForeground(Color.WHITE);
        btnBFS.setFocusPainted(false);
        btnBFS.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBFS.addActionListener(e -> {
            executeBFS();
            startCommuterAnimation();
        });

        JButton btnWalk = new JButton("🚶 Walk Path");
        btnWalk.setBackground(new Color(16, 185, 129));
        btnWalk.setForeground(Color.WHITE);
        btnWalk.setFocusPainted(false);
        btnWalk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnWalk.addActionListener(e -> startCommuterAnimation());

        JButton btnReset = new JButton("↺ Reset Map");
        btnReset.setFocusPainted(false);
        btnReset.addActionListener(e -> resetMap());

        navBar.add(lblFrom);
        navBar.add(fromComboBox);
        navBar.add(lblTo);
        navBar.add(toComboBox);
        navBar.add(btnDijkstra);
        navBar.add(btnBFS);
        navBar.add(btnWalk);
        navBar.add(btnReset);

        // Row 2: Test Case Buttons
        JPanel testBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        testBar.setOpaque(false);

        JLabel lblTest = new JLabel("Quick Tests:");
        lblTest.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTest.setForeground(new Color(100, 116, 139));
        testBar.add(lblTest);

        addTestButton(testBar, "TC1: Gate→Canteen (Dijkstra)", () -> runTestCase1());
        addTestButton(testBar, "TC2: Gate→Gate (Boundary)", () -> runTestCase2());
        addTestButton(testBar, "TC3: Gate→Hostel (Invalid)", () -> runTestCase3());
        addTestButton(testBar, "TC4a: Admin→Canteen (BFS)", () -> runTestCase4a());
        addTestButton(testBar, "TC4b: Admin→Canteen (Dijkstra)", () -> runTestCase4b());

        topContainer.add(navBar);
        topContainer.add(testBar);
        add(topContainer, BorderLayout.NORTH);

        // --- CENTER: Graph Canvas Panel ---
        graphPanel = new GraphPanel();
        add(graphPanel, BorderLayout.CENTER);

        // --- BOTTOM: Result Log and Management Actions ---
        JPanel bottomContainer = new JPanel(new BorderLayout(8, 8));
        bottomContainer.setBorder(new EmptyBorder(4, 12, 10, 12));
        bottomContainer.setPreferredSize(new Dimension(getWidth(), 180));

        // Actions toolbar
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton btnAddLocation = new JButton("➕ Add Location");
        btnAddLocation.addActionListener(e -> showAddLocationDialog());

        JButton btnAddRoad = new JButton("🔗 Add Road (Two-way)");
        btnAddRoad.addActionListener(e -> showAddRoadDialog());

        JButton btnRunAllTests = new JButton("🧪 Run All 4 Tests in Log");
        btnRunAllTests.addActionListener(e -> runAllTestsInLog());

        JButton btnClearLog = new JButton("Clear Log");
        btnClearLog.addActionListener(e -> logArea.setText(""));

        actionToolbar.add(btnAddLocation);
        actionToolbar.add(btnAddRoad);
        actionToolbar.add(btnRunAllTests);
        actionToolbar.add(btnClearLog);
        bottomContainer.add(actionToolbar, BorderLayout.NORTH);

        // Log Console
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(15, 23, 42));
        logArea.setForeground(new Color(241, 245, 249));
        logArea.setCaretColor(Color.WHITE);
        logArea.setMargin(new Insets(6, 8, 6, 8));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        bottomContainer.add(scrollPane, BorderLayout.CENTER);

        add(bottomContainer, BorderLayout.SOUTH);

        // Populate dropdowns & trigger initial state
        refreshDropdowns();
        appendLog("Welcome to Campus Map Navigator UI.");
        appendLog("Initial map loaded: 4 locations, 5 bidirectional paths.");
        executeDijkstra();
    }

    private void addTestButton(JPanel parent, String title, Runnable action) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setMargin(new Insets(2, 6, 2, 6));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> action.run());
        parent.add(btn);
    }

    private void refreshDropdowns() {
        String currentFrom = (String) fromComboBox.getSelectedItem();
        String currentTo = (String) toComboBox.getSelectedItem();

        fromComboBox.removeAllItems();
        toComboBox.removeAllItems();

        List<String> locations = app.getLocations();
        for (String loc : locations) {
            fromComboBox.addItem(loc);
            toComboBox.addItem(loc);
        }

        if (currentFrom != null && locations.contains(currentFrom)) {
            fromComboBox.setSelectedItem(currentFrom);
        } else if (!locations.isEmpty()) {
            fromComboBox.setSelectedIndex(0);
        }

        if (currentTo != null && locations.contains(currentTo)) {
            toComboBox.setSelectedItem(currentTo);
        } else if (locations.size() > 1) {
            toComboBox.setSelectedIndex(locations.size() - 1);
        }
    }

    private void appendLog(String text) {
        logArea.append(text + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void executeDijkstra() {
        String from = (String) fromComboBox.getSelectedItem();
        String to = (String) toComboBox.getSelectedItem();
        if (from == null || to == null) return;

        CampusMapApp.PathResult result = app.findShortestDistance(from, to);
        currentAlgo = "Dijkstra (Shortest Distance)";

        if (result.success) {
            currentPath = result.path;
            currentDistance = result.totalDistance;
            currentStops = result.stops;
            appendLog("\n[Option 3 - Dijkstra Shortest Distance]");
            appendLog("From: " + from + ", To: " + to);
            appendLog("Path: " + result.getPathString());
            appendLog("Distance: " + result.totalDistance + " m");
        } else {
            currentPath.clear();
            currentDistance = 0;
            currentStops = 0;
            appendLog("\n[Option 3 - Error]");
            appendLog(result.errorMessage);
        }
        graphPanel.repaint();
    }

    private void executeBFS() {
        String from = (String) fromComboBox.getSelectedItem();
        String to = (String) toComboBox.getSelectedItem();
        if (from == null || to == null) return;

        CampusMapApp.PathResult result = app.findFewestStops(from, to);
        currentAlgo = "BFS (Fewest Stops)";

        if (result.success) {
            currentPath = result.path;
            currentDistance = result.totalDistance;
            currentStops = result.stops;
            appendLog("\n[Option 4 - BFS Fewest Stops]");
            appendLog("From: " + from + ", To: " + to);
            appendLog("Path: " + result.getPathString() + " | Stops: " + result.stops + " | Distance: " + result.totalDistance + " m");
        } else {
            currentPath.clear();
            currentDistance = 0;
            currentStops = 0;
            appendLog("\n[Option 4 - Error]");
            appendLog(result.errorMessage);
        }
        graphPanel.repaint();
    }

    // --- QUICK TEST CASE EXECUTIONS ---
    private void runTestCase1() {
        fromComboBox.setSelectedItem("Gate");
        toComboBox.setSelectedItem("Canteen");
        executeDijkstra();
    }

    private void runTestCase2() {
        fromComboBox.setSelectedItem("Gate");
        toComboBox.setSelectedItem("Gate");
        executeDijkstra();
    }

    private void runTestCase3() {
        appendLog("\n[Test Case 3: Invalid Input]");
        appendLog("Option 3 (Shortest distance)");
        appendLog("From: Gate, To: Hostel");
        CampusMapApp.PathResult res = app.findShortestDistance("Gate", "Hostel");
        appendLog(res.errorMessage);
        currentPath.clear();
        currentAlgo = "Invalid (Hostel not found)";
        graphPanel.repaint();
    }

    private void runTestCase4a() {
        fromComboBox.setSelectedItem("Admin");
        toComboBox.setSelectedItem("Canteen");
        executeBFS();
    }

    private void runTestCase4b() {
        fromComboBox.setSelectedItem("Admin");
        toComboBox.setSelectedItem("Canteen");
        executeDijkstra();
    }

    private void runAllTestsInLog() {
        appendLog("\n=======================================================");
        appendLog("           RUNNING ALL 4 AUTOMATED TEST CASES          ");
        appendLog("=======================================================");

        // TC 1
        CampusMapApp.PathResult tc1 = app.findShortestDistance("Gate", "Canteen");
        appendLog("\nTest Case 1: Gate -> Canteen (Shortest distance)");
        appendLog("Path: " + tc1.getPathString());
        appendLog("Distance: " + tc1.totalDistance + " m");
        appendLog("Status: " + (tc1.getPathString().equals("Gate -> Admin -> Library -> Canteen") && tc1.totalDistance == 150 ? "PASSED [OK]" : "FAILED"));

        // TC 2
        CampusMapApp.PathResult tc2 = app.findShortestDistance("Gate", "Gate");
        appendLog("\nTest Case 2: Gate -> Gate (Boundary)");
        appendLog("Path: " + tc2.getPathString());
        appendLog("Distance: " + tc2.totalDistance + " m");
        appendLog("Status: " + (tc2.getPathString().equals("Gate") && tc2.totalDistance == 0 ? "PASSED [OK]" : "FAILED"));

        // TC 3
        CampusMapApp.PathResult tc3 = app.findShortestDistance("Gate", "Hostel");
        appendLog("\nTest Case 3: Gate -> Hostel (Invalid location)");
        appendLog(tc3.errorMessage);
        appendLog("Status: " + ("Location Hostel not found.".equals(tc3.errorMessage) ? "PASSED [OK]" : "FAILED"));

        // TC 4a
        CampusMapApp.PathResult tc4a = app.findFewestStops("Admin", "Canteen");
        appendLog("\nTest Case 4 (a): Admin -> Canteen (Fewest stops)");
        appendLog("Path: " + tc4a.getPathString() + " | Stops: " + tc4a.stops + " | Distance: " + tc4a.totalDistance + " m");
        appendLog("Status: " + (tc4a.getPathString().equals("Admin -> Canteen") && tc4a.stops == 1 && tc4a.totalDistance == 200 ? "PASSED [OK]" : "FAILED"));

        // TC 4b
        CampusMapApp.PathResult tc4b = app.findShortestDistance("Admin", "Canteen");
        appendLog("\nTest Case 4 (b): Admin -> Canteen (Shortest distance)");
        appendLog("Path: " + tc4b.getPathString() + " | Distance: " + tc4b.totalDistance + " m");
        appendLog("Status: " + (tc4b.getPathString().equals("Admin -> Library -> Canteen") && tc4b.totalDistance == 100 ? "PASSED [OK]" : "FAILED"));

        appendLog("=======================================================\n");
    }

    private void resetMap() {
        app.initializeDefaultMap();
        initCoordinates();
        refreshDropdowns();
        appendLog("\nCampus map successfully reset to default layout.");
        executeDijkstra();
    }

    private void showAddLocationDialog() {
        String name = JOptionPane.showInputDialog(this, "Enter name of new location:", "Add Campus Building", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            if (app.addLocation(name.trim())) {
                updateDynamicCoordinates();
                refreshDropdowns();
                fromComboBox.setSelectedItem(name.trim());
                appendLog("Added new location: " + name.trim());
                graphPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Location already exists or invalid name.", "Notice", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void showAddRoadDialog() {
        List<String> locs = app.getLocations();
        if (locs.size() < 2) {
            JOptionPane.showMessageDialog(this, "Need at least 2 locations to add a road.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<String> fromBox = new JComboBox<>(locs.toArray(new String[0]));
        JComboBox<String> toBox = new JComboBox<>(locs.toArray(new String[0]));
        if (locs.size() > 1) toBox.setSelectedIndex(1);

        JTextField distField = new JTextField("120");

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 8));
        panel.add(new JLabel("From Location:"));
        panel.add(fromBox);
        panel.add(new JLabel("To Location:"));
        panel.add(toBox);
        panel.add(new JLabel("Distance in metres:"));
        panel.add(distField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add Two-way Road", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String u = (String) fromBox.getSelectedItem();
            String v = (String) toBox.getSelectedItem();
            try {
                int dist = Integer.parseInt(distField.getText().trim());
                if (dist < 0) {
                    JOptionPane.showMessageDialog(this, "Distance cannot be negative (" + dist + " m). Negative distances are not allowed.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (dist == 0) {
                    JOptionPane.showMessageDialog(this, "Distance must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (u.equals(v)) {
                    JOptionPane.showMessageDialog(this, "Source and destination cannot be identical.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                app.addPath(u, v, dist);
                appendLog("Connected: " + u + " <-> " + v + " (" + dist + " m)");
                executeDijkstra();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid distance. Please enter an integer.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- CANVAS PANEL FOR DRAWING GRAPH ---
    private class GraphPanel extends JPanel {
        private String draggedBuilding = null;
        private Point dragOffset = new Point(0, 0);
        private double ripplePhase = 0.0;

        public GraphPanel() {
            setBackground(new Color(13, 27, 30));
            setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));

            // Add Mouse Drag Listeners for Interactive Landmark Repositioning
            java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    Point mp = e.getPoint();
                    for (String loc : app.getLocations()) {
                        Point p = nodeCoordinates.get(loc);
                        if (p != null && p.distance(mp) <= 30) {
                            draggedBuilding = loc;
                            dragOffset.setLocation(mp.x - p.x, mp.y - p.y);
                            break;
                        }
                    }
                }

                @Override
                public void mouseDragged(java.awt.event.MouseEvent e) {
                    if (draggedBuilding != null) {
                        Point mp = e.getPoint();
                        int nx = Math.max(45, Math.min(getWidth() - 45, mp.x - dragOffset.x));
                        int ny = Math.max(45, Math.min(getHeight() - 45, mp.y - dragOffset.y));
                        nodeCoordinates.put(draggedBuilding, new Point(nx, ny));
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    draggedBuilding = null;
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        private String getBuildingIcon(String name) {
            String lower = name.toLowerCase();
            if (lower.contains("gate")) return "🏛";
            if (lower.contains("admin")) return "🏢";
            if (lower.contains("library")) return "📚";
            if (lower.contains("canteen") || lower.contains("cafe")) return "☕";
            return "📍";
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. Campus Lawn Background (80-Acre Bhilai Campus Turf)
            g2.setColor(new Color(10, 25, 23));
            g2.fillRect(0, 0, w, h);

            // Subtle campus walkway grid
            g2.setColor(new Color(15, 38, 32));
            for (int x = 0; x < w; x += 50) g2.drawLine(x, 0, x, h);
            for (int y = 0; y < h; y += 50) g2.drawLine(0, y, w, y);

            // 2. RISU Cricket Turf & Sports Grounds (Center)
            int fcx = w / 2;
            int fcy = h / 2;
            g2.setColor(new Color(16, 185, 129, 25));
            g2.fillOval(fcx - 65, fcy - 38, 130, 76);
            g2.setColor(new Color(52, 211, 153, 70));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(fcx - 65, fcy - 38, 130, 76);

            // Pitch
            g2.setColor(new Color(217, 119, 6));
            g2.fillRect(fcx - 14, fcy - 4, 28, 8);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
            g2.setColor(new Color(255, 255, 255, 90));
            FontMetrics cfm = g2.getFontMetrics();
            String cText = "RISU SPORTS ARENA";
            g2.drawString(cText, fcx - cfm.stringWidth(cText) / 2, fcy + 22);

            // 2b. Hostel Zone (Top Perimeter - Demonstrating TC3)
            int hx = fcx;
            int hy = 42;
            g2.setColor(new Color(244, 63, 94, 20));
            g2.fillRoundRect(hx - 55, hy - 18, 110, 36, 8, 8);
            g2.setColor(new Color(244, 63, 94, 90));
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f, 4.0f}, 0.0f));
            g2.drawRoundRect(hx - 55, hy - 18, 110, 36, 8, 8);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
            g2.setColor(new Color(251, 113, 133));
            FontMetrics hfm = g2.getFontMetrics();
            String h1 = "🏢 HOSTEL BLOCK";
            String h2 = "(TC3: Not Connected)";
            g2.drawString(h1, hx - hfm.stringWidth(h1) / 2, hy - 2);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            g2.setColor(new Color(255, 255, 255, 120));
            FontMetrics hfm2 = g2.getFontMetrics();
            g2.drawString(h2, hx - hfm2.stringWidth(h2) / 2, hy + 10);

            // 3. Palm Trees along Boulevard
            drawTree(g2, 45, 35, 15, "palm");
            drawTree(g2, 175, 35, 16, "palm");
            drawTree(g2, 385, 35, 16, "palm");
            drawTree(g2, w - 45, 35, 15, "palm");
            drawTree(g2, 40, h / 2, 16, "palm");
            drawTree(g2, w - 40, h / 2, 16, "palm");
            drawTree(g2, 45, h - 45, 15, "palm");
            drawTree(g2, 185, h - 45, 16, "palm");
            drawTree(g2, 385, h - 45, 16, "palm");
            drawTree(g2, w - 45, h - 45, 15, "palm");

            List<String> locations = app.getLocations();
            updateDynamicCoordinates();

            // Set of active edges in current path
            Set<String> activeEdges = new HashSet<>();
            if (currentPath != null && currentPath.size() > 1) {
                for (int i = 0; i < currentPath.size() - 1; i++) {
                    String u = currentPath.get(i);
                    String v = currentPath.get(i + 1);
                    activeEdges.add(u + "--" + v);
                    activeEdges.add(v + "--" + u);
                }
            }

            // 4. Draw Cobblestone Roads
            Set<String> drawn = new HashSet<>();
            for (int i = 0; i < locations.size(); i++) {
                String u = locations.get(i);
                Point p1 = nodeCoordinates.get(u);
                if (p1 == null) continue;

                List<CampusMapApp.Edge> edges = app.getEdges(i);
                for (CampusMapApp.Edge edge : edges) {
                    String v = locations.get(edge.targetVertex);
                    Point p2 = nodeCoordinates.get(v);
                    if (p2 == null) continue;

                    String pairKey = (u.compareTo(v) < 0) ? u + "--" + v : v + "--" + u;
                    if (!drawn.contains(pairKey)) {
                        drawn.add(pairKey);
                        boolean isActive = activeEdges.contains(pairKey);

                        boolean isOuterBypass = (u.equalsIgnoreCase("Admin") && v.equalsIgnoreCase("Canteen")) ||
                                                (u.equalsIgnoreCase("Canteen") && v.equalsIgnoreCase("Admin"));

                        if (isOuterBypass) {
                            // Draw sweeping outer perimeter ring road (200m)
                            java.awt.geom.QuadCurve2D qc = new java.awt.geom.QuadCurve2D.Float(p1.x, p1.y, (p1.x + p2.x)/2, Math.max(p1.y, p2.y) + 90, p2.x, p2.y);
                            g2.setStroke(new BasicStroke(isActive ? 12.0f : 7.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            g2.setColor(new Color(51, 65, 85));
                            g2.draw(qc);

                            g2.setStroke(new BasicStroke(isActive ? 6.0f : 4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            g2.setColor(new Color(30, 41, 59));
                            g2.draw(qc);

                            if (isActive) {
                                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                                g2.setColor(new Color(192, 132, 252)); // BFS Purple
                                g2.draw(qc);
                            } else {
                                g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f, 5.0f}, 0.0f));
                                g2.setColor(new Color(168, 85, 247, 180));
                                g2.draw(qc);
                            }
                        } else {
                            // Standard straight road
                            g2.setStroke(new BasicStroke(isActive ? 13.0f : 9.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            g2.setColor(new Color(51, 65, 85));
                            g2.drawLine(p1.x, p1.y, p2.x, p2.y);

                            g2.setStroke(new BasicStroke(isActive ? 7.0f : 5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            g2.setColor(new Color(30, 41, 59));
                            g2.drawLine(p1.x, p1.y, p2.x, p2.y);

                            if (isActive) {
                                g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                                g2.setColor(currentAlgo.contains("BFS") ? new Color(192, 132, 252) : new Color(96, 165, 250));
                                g2.drawLine(p1.x, p1.y, p2.x, p2.y);

                                g2.setStroke(new BasicStroke(1.2f));
                                g2.setColor(Color.WHITE);
                                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                            } else {
                                g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f, 4.0f}, 0.0f));
                                g2.setColor(new Color(100, 116, 139));
                                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                            }
                        }

                        // Distance Pill Badge
                        int mx = (p1.x + p2.x) / 2;
                        int my = (p1.y + p2.y) / 2;
                        String distText = edge.weight + " m";
                        FontMetrics fm = g2.getFontMetrics(new Font("Segoe UI", Font.BOLD, 10));
                        int pw = fm.stringWidth(distText) + 14;
                        int ph = 18;

                        g2.setColor(isActive 
                            ? (currentAlgo.contains("BFS") ? new Color(88, 28, 135) : new Color(30, 58, 138)) 
                            : new Color(15, 23, 42));
                        g2.fillRoundRect(mx - pw / 2, my - ph / 2, pw, ph, 8, 8);

                        g2.setColor(isActive ? new Color(96, 165, 250) : new Color(71, 85, 105));
                        g2.setStroke(new BasicStroke(1.0f));
                        g2.drawRoundRect(mx - pw / 2, my - ph / 2, pw, ph, 8, 8);

                        g2.setColor(isActive ? Color.WHITE : new Color(203, 213, 225));
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        g2.drawString(distText, mx - fm.stringWidth(distText) / 2, my + 4);
                    }
                }
            }

            // 5. Draw Campus Buildings
            for (String loc : locations) {
                Point p = nodeCoordinates.get(loc);
                if (p == null) continue;

                boolean isSource = currentPath != null && !currentPath.isEmpty() && currentPath.get(0).equals(loc);
                boolean isDest = currentPath != null && !currentPath.isEmpty() && currentPath.get(currentPath.size() - 1).equals(loc);
                boolean isInPath = currentPath != null && currentPath.contains(loc);

                // Halo glow for route nodes
                if (isInPath) {
                    RadialGradientPaint rgp = new RadialGradientPaint(
                        new Point2D.Float(p.x, p.y), 40.0f,
                        new float[]{0.0f, 1.0f},
                        new Color[]{
                            isSource ? new Color(16, 185, 129, 130) : (isDest ? new Color(239, 68, 68, 130) : new Color(59, 130, 246, 130)),
                            new Color(0, 0, 0, 0)
                        }
                    );
                    g2.setPaint(rgp);
                    g2.fillOval(p.x - 40, p.y - 40, 80, 80);
                }

                // Drop shadow
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillOval(p.x - 22, p.y + 16, 44, 14);

                // Custom RISU Architecture Render
                if ("Admin".equalsIgnoreCase(loc)) {
                    drawAdminBlock(g2, p.x, p.y, isSource, isDest, isInPath);
                } else if ("Library".equalsIgnoreCase(loc)) {
                    drawLibraryBlock(g2, p.x, p.y, isSource, isDest, isInPath);
                } else if ("Canteen".equalsIgnoreCase(loc)) {
                    drawCanteenBlock(g2, p.x, p.y, isSource, isDest, isInPath);
                } else if ("Gate".equalsIgnoreCase(loc)) {
                    drawMainGateBlock(g2, p.x, p.y, isSource, isDest, isInPath);
                } else {
                    // Generic dynamic node
                    int radius = 22;
                    g2.setColor(isInPath ? new Color(37, 99, 235) : new Color(30, 41, 59));
                    g2.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
                    g2.setColor(isInPath ? Color.WHITE : new Color(100, 116, 139));
                    g2.setStroke(new BasicStroke(isInPath ? 2.5f : 1.2f));
                    g2.drawOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
                    String icon = getBuildingIcon(loc);
                    g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                    FontMetrics ifm = g2.getFontMetrics();
                    g2.setColor(Color.WHITE);
                    g2.drawString(icon, p.x - ifm.stringWidth(icon) / 2, p.y + 6);
                }

                // 360 Hotspot Pin (Circular Red Marker with walking person)
                int pinRadius = 9;
                g2.setColor(isSource ? new Color(16, 185, 129) : (isDest ? new Color(239, 68, 68) : new Color(192, 38, 45)));
                g2.fillOval(p.x - pinRadius, p.y - 32, pinRadius * 2, pinRadius * 2);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(p.x - pinRadius, p.y - 32, pinRadius * 2, pinRadius * 2);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
                FontMetrics pfm = g2.getFontMetrics();
                String pinText = isSource ? "S" : (isDest ? "D" : "📍");
                g2.drawString(pinText, p.x - pfm.stringWidth(pinText) / 2, p.y - 20);

                // Name Banner Ribbon
                int bannerY = p.y + 36;
                Font bFont = new Font("Segoe UI", Font.BOLD, 11);
                g2.setFont(bFont);
                FontMetrics bfm = g2.getFontMetrics();
                int bw = bfm.stringWidth(loc) + 16;
                int bh = 18;

                g2.setColor(new Color(15, 23, 42, 235));
                g2.fillRoundRect(p.x - bw / 2, bannerY - bh / 2, bw, bh, 8, 8);
                g2.setColor(isInPath ? (isSource ? new Color(16, 185, 129) : (isDest ? new Color(239, 68, 68) : new Color(96, 165, 250))) : new Color(71, 85, 105));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(p.x - bw / 2, bannerY - bh / 2, bw, bh, 8, 8);

                g2.setColor(Color.WHITE);
                g2.drawString(loc, p.x - bfm.stringWidth(loc) / 2, bannerY + 4);

                // RISU Subtitle
                String sub = "";
                if ("Gate".equalsIgnoreCase(loc)) sub = "Main Gate (Kohka Rd)";
                else if ("Admin".equalsIgnoreCase(loc)) sub = "Admin Block A";
                else if ("Library".equalsIgnoreCase(loc)) sub = "Central Library Hub";
                else if ("Canteen".equalsIgnoreCase(loc)) sub = "Food Court & Nescafe";
                else sub = loc + " Wing";

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                g2.setColor(new Color(148, 163, 184));
                FontMetrics sfm = g2.getFontMetrics();
                g2.drawString(sub, p.x - sfm.stringWidth(sub) / 2, bannerY + 16);

                // Role tag (START / GOAL)
                if (isSource || isDest) {
                    String tag = isSource ? "START" : "GOAL";
                    int ty = p.y - 30;
                    g2.setColor(isSource ? new Color(16, 185, 129) : new Color(239, 68, 68));
                    g2.fillRoundRect(p.x - 20, ty - 7, 40, 14, 4, 4);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    FontMetrics tfm = g2.getFontMetrics();
                    g2.drawString(tag, p.x - tfm.stringWidth(tag) / 2, ty + 4);
                }
            }

            // 6. Draw Commuter Dot with Trail
            if (commuterActive) {
                for (int i = 0; i < commuterTrail.size(); i++) {
                    Point2D.Double pt = commuterTrail.get(i);
                    float alpha = (float) (i + 1) / commuterTrail.size() * 0.45f;
                    g2.setColor(new Color(245, 158, 11, (int) (alpha * 255)));
                    g2.fillOval((int) pt.x - 3, (int) pt.y - 3, 6, 6);
                }

                // Commuter Avatar
                g2.setColor(new Color(217, 119, 6)); // Amber
                g2.fillOval((int) commuterPos.x - 10, (int) commuterPos.y - 10, 20, 20);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawOval((int) commuterPos.x - 10, (int) commuterPos.y - 10, 20, 20);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.setColor(Color.WHITE);
                g2.drawString("🚶", (int) commuterPos.x - 5, (int) commuterPos.y + 4);
            }

            // Metric Scale Bar (1m = 1.6px)
            int sbX = 35;
            int sbY = h - 35;
            int bar50 = (int)(50 * 1.6);  // 80px
            int bar100 = (int)(100 * 1.6); // 160px

            g2.setColor(new Color(15, 23, 42, 220));
            g2.fillRoundRect(sbX - 8, sbY - 16, bar100 + 16, 26, 6, 6);
            g2.setColor(new Color(255, 255, 255, 70));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(sbX - 8, sbY - 16, bar100 + 16, 26, 6, 6);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawLine(sbX, sbY, sbX + bar100, sbY);
            g2.drawLine(sbX, sbY - 4, sbX, sbY + 4);
            g2.drawLine(sbX + bar50, sbY - 4, sbX + bar50, sbY + 4);
            g2.drawLine(sbX + bar100, sbY - 4, sbX + bar100, sbY + 4);

            g2.setFont(new Font("Consolas", Font.BOLD, 8));
            g2.drawString("0m", sbX - 4, sbY - 6);
            g2.drawString("50m", sbX + bar50 - 8, sbY - 6);
            g2.drawString("100m (Scale 1:160)", sbX + bar100 - 30, sbY - 6);

            // Legend / Status Footer
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(new Color(203, 213, 225));
            String status = "RISU Bhilai Campus • Routing: " + currentAlgo + " | Distance: " + currentDistance + " m | Stops: " + currentStops + " | Drag buildings with mouse";
            g2.drawString(status, 15, h - 12);
        }

        
        private void drawAdminBlock(Graphics2D g2, int x, int y, boolean isSource, boolean isDest, boolean inPath) {
            // Drop Shadow
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRoundRect(x - 38, y - 26, 76, 52, 10, 10);

            // Crimson red building facade
            g2.setColor(new Color(140, 29, 36));
            g2.fillRoundRect(x - 40, y - 28, 80, 50, 8, 8);
            g2.setColor(inPath ? new Color(251, 191, 36) : new Color(90, 16, 21));
            g2.setStroke(new BasicStroke(inPath ? 2.5f : 1.2f));
            g2.drawRoundRect(x - 40, y - 28, 80, 50, 8, 8);

            // White window grid pattern (Signature RISU style)
            g2.setColor(Color.WHITE);
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 5; c++) {
                    g2.fillRect(x - 35 + c * 15, y - 24 + r * 11, 8, 6);
                }
            }

            // Blue & Gold Entrance Portico
            g2.setColor(new Color(29, 78, 216)); // Royal Blue
            g2.fillRect(x - 14, y - 6, 28, 28);
            g2.setColor(new Color(234, 179, 8)); // Golden Yellow Pillars
            g2.fillRect(x - 12, y + 6, 5, 16);
            g2.fillRect(x + 7, y + 6, 5, 16);

            // Golden Pagoda / Temple Finial (defining architectural crown of Block A1)
            g2.setColor(new Color(234, 179, 8));
            int[] px = {x - 16, x, x + 16};
            int[] py = {y - 28, y - 42, y - 28};
            g2.fillPolygon(px, py, 3);
            g2.setColor(new Color(250, 204, 21));
            g2.fillOval(x - 4, y - 46, 8, 8);
            g2.fillOval(x - 2, y - 50, 4, 4);

            // Sign banner
            g2.setColor(new Color(249, 115, 22));
            g2.fillRect(x - 18, y - 2, 36, 6);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 5));
            g2.drawString("RISU A1", x - 10, y + 3);
        }

        private void drawLibraryBlock(Graphics2D g2, int x, int y, boolean isSource, boolean isDest, boolean inPath) {
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRoundRect(x - 38, y - 22, 76, 44, 8, 8);

            // Glass Atrium Facade
            g2.setColor(new Color(2, 132, 199));
            g2.fillRoundRect(x - 40, y - 24, 80, 44, 8, 8);
            g2.setColor(inPath ? new Color(251, 191, 36) : Color.WHITE);
            g2.setStroke(new BasicStroke(inPath ? 2.5f : 1.2f));
            g2.drawRoundRect(x - 40, y - 24, 80, 44, 8, 8);

            // Wave Entrance Canopy
            g2.setColor(new Color(12, 74, 110));
            g2.fillRoundRect(x - 24, y + 6, 48, 14, 4, 4);

            // Text
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
            FontMetrics fm = g2.getFontMetrics();
            String t = "📚 CENTRAL LIBRARY";
            g2.drawString(t, x - fm.stringWidth(t) / 2, y - 4);
        }

        private void drawCanteenBlock(Graphics2D g2, int x, int y, boolean isSource, boolean isDest, boolean inPath) {
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRoundRect(x - 36, y - 20, 72, 40, 8, 8);

            // Rustic wood deck
            g2.setColor(new Color(120, 53, 15));
            g2.fillRoundRect(x - 38, y - 22, 76, 42, 8, 8);
            g2.setColor(inPath ? new Color(251, 191, 36) : new Color(245, 158, 11));
            g2.setStroke(new BasicStroke(inPath ? 2.5f : 1.2f));
            g2.drawRoundRect(x - 38, y - 22, 76, 42, 8, 8);

            // Sun umbrellas
            g2.setColor(new Color(239, 68, 68));
            g2.fillArc(x - 22, y - 10, 16, 16, 0, 180);
            g2.setColor(new Color(59, 130, 246));
            g2.fillArc(x + 6, y - 10, 16, 16, 0, 180);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
            FontMetrics fm = g2.getFontMetrics();
            String t = "☕ CHAI KA ADDA";
            g2.drawString(t, x - fm.stringWidth(t) / 2, y + 12);
        }

        private void drawMainGateBlock(Graphics2D g2, int x, int y, boolean isSource, boolean isDest, boolean inPath) {
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRect(x - 48, y - 14, 96, 32);

            // Grand Arch Blue Pillars
            g2.setColor(new Color(30, 58, 138));
            g2.fillRect(x - 46, y - 16, 14, 34);
            g2.fillRect(x + 32, y - 16, 14, 34);

            // Gold Header Truss Beam
            g2.setColor(new Color(234, 179, 8));
            g2.fillRect(x - 50, y - 24, 100, 8);

            // Overhead Signboard
            g2.setColor(new Color(192, 38, 45));
            g2.fillRect(x - 38, y - 14, 76, 8);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 6));
            FontMetrics fm = g2.getFontMetrics();
            String t = "RUNGTA UNIVERSITY";
            g2.drawString(t, x - fm.stringWidth(t) / 2, y - 8);

            // Boom Barrier (Red/White)
            g2.setColor(new Color(239, 68, 68));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawLine(x - 30, y + 10, x + 30, y + 10);
        }

        private void drawTree(Graphics2D g2, int x, int y, int r, String type) {
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillOval(x - r, y + r / 2, r * 2, r);

            if ("palm".equals(type)) {
                // Curved trunk
                g2.setColor(new Color(120, 53, 15));
                g2.setStroke(new BasicStroke(3.0f));
                g2.drawLine(x, y + r, x - 2, y - r);

                // Palm fronds
                g2.setColor(new Color(21, 128, 61));
                for (int i = 0; i < 5; i++) {
                    double ang = (i * Math.PI * 2) / 5;
                    int fx = (int) (x + Math.cos(ang) * 12);
                    int fy = (int) (y - r + Math.sin(ang) * 8);
                    g2.fillOval(fx - 4, fy - 3, 8, 6);
                }
                g2.setColor(new Color(34, 197, 94));
                g2.fillOval(x - 3, y - r - 3, 6, 6);
            } else if ("pine".equals(type)) {
                g2.setColor(new Color(30, 58, 38));
                int[] px = {x, x - r, x + r};
                int[] py = {y - (int)(r * 1.3), y + r, y + r};
                g2.fillPolygon(px, py, 3);
            } else if ("cherry".equals(type)) {
                g2.setColor(new Color(244, 114, 182));
                g2.fillOval(x - r, y - r, r * 2, r * 2);
            } else {
                g2.setColor(new Color(21, 128, 61));
                g2.fillOval(x - r, y - r, r * 2, r * 2);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CampusMapGUI gui = new CampusMapGUI();
            gui.setVisible(true);
        });
    }
}
