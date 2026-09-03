import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ExamPrioritizer extends JFrame {

    // =====================================================
    // COLORS
    // =====================================================

    Color NAVY = new Color(20, 27, 48);
    Color BLUE = new Color(45, 105, 220);
    Color PURPLE = new Color(105, 55, 230);
    Color GREEN = new Color(30, 175, 90);
    Color ORANGE = new Color(240, 140, 25);

    Color LIGHT_BG = new Color(246, 248, 252);
    Color WHITE = Color.WHITE;
    Color TEXT = new Color(25, 35, 60);
    Color MUTED = new Color(100, 110, 130);

    // =====================================================
    // DATA & STATE
    // =====================================================

    ArrayList<TopicData> topicList = new ArrayList<>();
    ArrayList<QuestionData> practiceQuestions = new ArrayList<>();

    int totalStudySeconds = 0;
    javax.swing.Timer studyTimer;
    int currentTopic = -1;

    int practiceAttempts = 0;
    int practiceCorrect = 0;
    int currentQuestionIndex = 0;

    String currentStudentName = "";

    // =====================================================
    // EXAM DATE
    // =====================================================

    LocalDate examDate = null;
    DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // =====================================================
    // LABELS
    // =====================================================

    JLabel totalTimeLabel;
    JLabel overallPreparationLabel;
    JLabel topicsCompletedLabel;
    JLabel accuracyLabel;
    JLabel daysLeftLabel;
    JLabel studentHeaderLabel;

    // =====================================================
    // CARD LAYOUT & PANELS
    // =====================================================

    JPanel mainContainer;
    CardLayout appLayout;

    JPanel contentPanel;
    CardLayout cardLayout;
    JPanel sidebarPanel;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ExamPrioritizer() {

        initializeData();
        initializeQuestions();

        setTitle("Exam Topic Prioritizer - Student Portal");
        setSize(1500, 950);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        appLayout = new CardLayout();
        mainContainer = new JPanel(appLayout);

        // Login Screen
        mainContainer.add(createLoginPanel(), "Login");

        // Main App Container Screen
        JPanel mainAppPanel = new JPanel(new BorderLayout());
        sidebarPanel = createSidebar();
        mainAppPanel.add(sidebarPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(LIGHT_BG);

        contentPanel.add(createDashboard(), "Dashboard");
        contentPanel.add(createTopicsPage(), "Topics");
        contentPanel.add(createPracticePage(), "Practice");
        contentPanel.add(createProgressPage(), "Progress");
        contentPanel.add(createSettingsPage(), "Settings");

        mainAppPanel.add(contentPanel, BorderLayout.CENTER);
        mainContainer.add(mainAppPanel, "MainApp");

        add(mainContainer);
        appLayout.show(mainContainer, "Login");

        // Timers
        javax.swing.Timer dateTimer = new javax.swing.Timer(60 * 1000, e -> updateDaysLeft());
        dateTimer.start();

        javax.swing.Timer greetingTimer = new javax.swing.Timer(60 * 1000, e -> updateGreeting());
        greetingTimer.start();
    }

    // =====================================================
    // LOGIN PANEL (FIXED BUTTON VISIBILITY & ALIGNMENT)
    // =====================================================

    private JPanel createLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(NAVY);

        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(420, 520));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 225, 240), 1, true),
                new EmptyBorder(30, 35, 30, 35)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("🎓");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Student Login");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Welcome back! Please enter your details.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(350, 40));
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(350, 40));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setForeground(TEXT);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLabel.setForeground(TEXT);
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn = new JButton("Login →");
        loginBtn.setMaximumSize(new Dimension(350, 45));
        loginBtn.setPreferredSize(new Dimension(350, 45));
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setBackground(PURPLE);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setOpaque(true);
        loginBtn.setContentAreaFilled(true);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Demo Creds: student / 123");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (username.equalsIgnoreCase("student") && password.equals("123")) {
                currentStudentName = "Student";
                refreshAllPages("Dashboard");
                appLayout.show(mainContainer, "MainApp");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials. Use 'student' and '123'.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        card.add(logo);
        card.add(Box.createVerticalStrut(10));
        card.add(title);
        card.add(Box.createVerticalStrut(5));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(25));
        card.add(userLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(15));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(25));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(15));
        card.add(hint);

        outer.add(card);
        return outer;
    }

    // =====================================================
    // INITIAL DATA
    // =====================================================

    private void initializeData() {
        topicList.add(new TopicData("Graphs", 25, "Hard", 20, "Data Structures", getGraphExplanationPages()));
        topicList.add(new TopicData("Trees", 20, "Hard", 30, "Data Structures", getTreeExplanationPages()));
        topicList.add(new TopicData("Linked List", 15, "Medium", 50, "Data Structures", getLinkedListExplanationPages()));
        topicList.add(new TopicData("Stack", 10, "Medium", 40, "Data Structures", getStackExplanationPages()));
        topicList.add(new TopicData("Queue", 10, "Easy", 60, "Data Structures", getQueueExplanationPages()));

        topicList.add(new TopicData("OOP Concepts", 20, "Hard", 35, "Java Programming", getOOPExplanationPages()));
        topicList.add(new TopicData("Inheritance", 15, "Medium", 45, "Java Programming", getInheritanceExplanationPages()));
        topicList.add(new TopicData("Exception Handling", 15, "Medium", 50, "Java Programming", getExceptionExplanationPages()));
        topicList.add(new TopicData("Collections", 20, "Hard", 25, "Java Programming", getCollectionsExplanationPages()));

        topicList.add(new TopicData("SQL", 20, "Hard", 30, "Database Management", getSQLExplanationPages()));
        topicList.add(new TopicData("Joins", 15, "Medium", 40, "Database Management", getJoinsExplanationPages()));
        topicList.add(new TopicData("Normalization", 15, "Hard", 35, "Database Management", getNormalizationExplanationPages()));
        topicList.add(new TopicData("Transactions", 10, "Medium", 55, "Database Management", getTransactionsExplanationPages()));
    }

    private void initializeQuestions() {
        practiceQuestions.add(new QuestionData("Which data structure is commonly used for Breadth First Search (BFS)?", new String[]{"Stack", "Queue", "Array", "Tree"}, 1));
        practiceQuestions.add(new QuestionData("Which data structure is commonly used for Depth First Search (DFS)?", new String[]{"Queue", "Stack", "Priority Queue", "Hash Map"}, 1));
        practiceQuestions.add(new QuestionData("What is the time complexity to access an element in an Array by index?", new String[]{"O(1)", "O(n)", "O(log n)", "O(n^2)"}, 0));
        practiceQuestions.add(new QuestionData("Which OOP principle allows a class to inherit properties from another class?", new String[]{"Encapsulation", "Polymorphism", "Inheritance", "Abstraction"}, 2));
        practiceQuestions.add(new QuestionData("Which collection in Java guarantees uniqueness of elements?", new String[]{"ArrayList", "LinkedList", "HashSet", "Vector"}, 2));
        practiceQuestions.add(new QuestionData("Which SQL clause is used to filter records after aggregation (GROUP BY)?", new String[]{"WHERE", "HAVING", "ORDER BY", "JOIN"}, 1));
        practiceQuestions.add(new QuestionData("Which normal form eliminates partial dependencies?", new String[]{"1NF", "2NF", "3NF", "BCNF"}, 1));
        practiceQuestions.add(new QuestionData("What does the 'A' in ACID transaction properties stand for?", new String[]{"Availability", "Atomicity", "Accuracy", "Authentication"}, 1));
    }

    private String[] getGraphExplanationPages() {
        return new String[]{
            "<html><h2>Graphs - Page 1: Fundamentals & Representations</h2>"
            + "<p>A Graph G = (V, E) consists of a set of Vertices V and Edges E connecting pairs of vertices.</p><br>"
            + "<b>Key Representations:</b><br>"
            + "• <b>Adjacency Matrix:</b> V x V boolean matrix where matrix[i][j] = true if edge exists. Query time: O(1), Space: O(V²).<br>"
            + "• <b>Adjacency List:</b> Array of lists where list[i] contains neighbors of vertex i. Query time: O(degree(V)), Space: O(V + E).<br><br>"
            + "<b>Graph Properties:</b><br>"
            + "• <i>Directed vs Undirected:</i> Edges have direction or are bidirectional.<br>"
            + "• <i>Degree:</i> Number of edges connected to a vertex (In-degree & Out-degree for directed).</html>",

            "<html><h2>Graphs - Page 2: Traversals & Algorithms</h2>"
            + "<p><b>Breadth-First Search (BFS):</b><br>"
            + "Level-order traversal using a <b>Queue</b>. Finds shortest path in unweighted graphs. Time: O(V + E), Space: O(V).<br><br>"
            + "<b>Depth-First Search (DFS):</b><br>"
            + "Explores path to maximum depth using a <b>Stack</b> or recursion. Used in cycle detection, topological sorting. Time: O(V + E).<br><br>"
            + "<b>Shortest Path Algorithms:</b><br>"
            + "• <b>Dijkstra's:</b> Single source shortest path with non-negative weights using PriorityQueue — O((V + E) log V).<br>"
            + "• <b>Bellman-Ford:</b> Handles negative edge weights — O(V x E).</html>",

            "<html><h2>Graphs - Page 3: Advanced Topics & Minimum Spanning Trees</h2>"
            + "<p><b>Minimum Spanning Tree (MST):</b> Subset of edges connecting all vertices with minimum total weight.</p><br>"
            + "• <b>Kruskal's Algorithm:</b> Sorts edges, uses Disjoint Set Union (DSU) — O(E log E).<br>"
            + "• <b>Prim's Algorithm:</b> Grows tree greedily from a starting node — O(E log V).<br><br>"
            + "<b>Topological Sort:</b> Linear ordering of vertices in Directed Acyclic Graphs (DAG). Implemented using DFS or Kahn's Algorithm (In-degree queue).</html>"
        };
    }

    private String[] getTreeExplanationPages() {
        return new String[]{
            "<html><h2>Trees - Page 1: Structure & Properties</h2><p>Connected acyclic graph with designated root node.</p></html>",
            "<html><h2>Trees - Page 2: BST & Balancing</h2><p>Search, Insertion, Deletion in O(log N). Self-balancing variants: AVL, Red-Black.</p></html>",
            "<html><h2>Trees - Page 3: Traversals</h2><p>Inorder (Sorted), Preorder (Serialization), Postorder (Evaluation).</p></html>"
        };
    }

    private String[] getLinkedListExplanationPages() {
        return new String[]{
            "<html><h2>Linked List - Page 1: Fundamentals</h2><p>Linear structure linked via pointers. Insert/Delete at Head in O(1).</p></html>"
        };
    }

    private String[] getStackExplanationPages() {
        return new String[]{"<html><h2>Stack - Page 1</h2><p>LIFO Structure. Methods: push, pop, peek.</p></html>"};
    }

    private String[] getQueueExplanationPages() {
        return new String[]{"<html><h2>Queue - Page 1</h2><p>FIFO Structure. Operations: enqueue, dequeue.</p></html>"};
    }

    private String[] getOOPExplanationPages() {
        return new String[]{"<html><h2>OOP - Page 1</h2><p>Encapsulation, Abstraction, Inheritance, Polymorphism.</p></html>"};
    }

    private String[] getInheritanceExplanationPages() {
        return new String[]{"<html><h2>Inheritance - Page 1</h2><p>Uses extends/implements. Promotes code reuse.</p></html>"};
    }

    private String[] getExceptionExplanationPages() {
        return new String[]{"<html><h2>Exceptions - Page 1</h2><p>Throwable &rarr; Checked vs Unchecked Exceptions.</p></html>"};
    }

    private String[] getCollectionsExplanationPages() {
        return new String[]{"<html><h2>Collections - Page 1</h2><p>List, Set, Queue, and Map data structures.</p></html>"};
    }

    private String[] getSQLExplanationPages() {
        return new String[]{"<html><h2>SQL - Page 1</h2><p>DDL, DML, DQL, DCL, and TCL commands.</p></html>"};
    }

    private String[] getJoinsExplanationPages() {
        return new String[]{"<html><h2>Joins - Page 1</h2><p>Inner, Left, Right, Full Outer, and Cross joins.</p></html>"};
    }

    private String[] getNormalizationExplanationPages() {
        return new String[]{"<html><h2>Normalization - Page 1</h2><p>Eliminate anomalies with 1NF, 2NF, 3NF, BCNF.</p></html>"};
    }

    private String[] getTransactionsExplanationPages() {
        return new String[]{"<html><h2>Transactions - Page 1</h2><p>ACID Properties and Isolation levels.</p></html>"};
    }

    private void refreshAllPages(String pageToShow) {
        contentPanel.removeAll();
        contentPanel.add(createDashboard(), "Dashboard");
        contentPanel.add(createTopicsPage(), "Topics");
        contentPanel.add(createPracticePage(), "Practice");
        contentPanel.add(createProgressPage(), "Progress");
        contentPanel.add(createSettingsPage(), "Settings");

        contentPanel.revalidate();
        contentPanel.repaint();
        cardLayout.show(contentPanel, pageToShow);
        updateStatistics();
        updateDaysLeft();
    }

    // =====================================================
    // SIDEBAR
    // =====================================================

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(270, 900));
        sidebar.setBackground(NAVY);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 20, 25, 20));

        JLabel logo = new JLabel("🎓");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title1 = new JLabel("EXAM");
        title1.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title1.setForeground(Color.WHITE);
        title1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title2 = new JLabel("PRIORITIZER");
        title2.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title2.setForeground(new Color(145, 90, 255));
        title2.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(title1);
        sidebar.add(title2);
        sidebar.add(Box.createVerticalStrut(25));

        addNavigationButton(sidebar, "⌂   Dashboard", "Dashboard");
        addNavigationButton(sidebar, "◎   Topics", "Topics");
        addNavigationButton(sidebar, "✎   Practice", "Practice");
        addNavigationButton(sidebar, "▥   Progress", "Progress");
        addNavigationButton(sidebar, "⚙   Settings", "Settings");

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = new JButton("🚪 Log Out");
        logoutBtn.setMaximumSize(new Dimension(230, 45));
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(200, 50, 50));
        logoutBtn.setOpaque(true);
        logoutBtn.setContentAreaFilled(true);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                appLayout.show(mainContainer, "Login");
            }
        });

        sidebar.add(logoutBtn);
        return sidebar;
    }

    private void addNavigationButton(JPanel sidebar, String text, String page) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(230, 52));
        button.setPreferredSize(new Dimension(230, 52));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(28, 38, 70));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(10, 20, 10, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> {
            cardLayout.show(contentPanel, page);
            for (Component component : sidebar.getComponents()) {
                if (component instanceof JButton) {
                    JButton b = (JButton) component;
                    if (!b.getText().contains("Log Out")) {
                        b.setBackground(new Color(28, 38, 70));
                    }
                }
            }
            button.setBackground(PURPLE);
        });

        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(8));
    }

    // =====================================================
    // DASHBOARD
    // =====================================================

    private JPanel createDashboard() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(LIGHT_BG);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(28, 30, 15, 30));

        JPanel greeting = new JPanel();
        greeting.setLayout(new BoxLayout(greeting, BoxLayout.Y_AXIS));
        greeting.setOpaque(false);

        studentHeaderLabel = new JLabel(getGreeting());
        studentHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        studentHeaderLabel.setForeground(TEXT);

        JLabel small = new JLabel("Stay focused and make today count.");
        small.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        small.setForeground(MUTED);

        greeting.add(studentHeaderLabel);
        greeting.add(Box.createVerticalStrut(5));
        greeting.add(small);

        top.add(greeting, BorderLayout.WEST);
        top.add(createExamDatePanel(), BorderLayout.EAST);
        main.add(top, BorderLayout.NORTH);

        JPanel dashboard = new JPanel();
        dashboard.setBackground(LIGHT_BG);
        dashboard.setLayout(new BoxLayout(dashboard, BoxLayout.Y_AXIS));
        dashboard.setBorder(new EmptyBorder(5, 30, 30, 30));

        JPanel stats = new JPanel(new GridLayout(1, 4, 18, 0));
        stats.setOpaque(false);

        int overall = getOverallPrepPercentage();

        stats.add(createStatCard("📅", "Days Left", getDaysLeftText(), BLUE));
        stats.add(createStatCard("🎯", "Overall Preparation", overall + "%", GREEN));
        stats.add(createStatCard("📋", "Topics Completed", getCompletedText(), PURPLE));
        stats.add(createStatCard("📈", "Practice Accuracy", (practiceAttempts == 0 ? "0%" : (practiceCorrect * 100 / practiceAttempts) + "%"), ORANGE));

        dashboard.add(stats);
        dashboard.add(Box.createVerticalStrut(20));

        JPanel middle = new JPanel(new GridLayout(1, 2, 20, 0));
        middle.setOpaque(false);
        middle.add(createStudyTimePanel());
        middle.add(createPreparationPanel());
        dashboard.add(middle);

        dashboard.add(Box.createVerticalStrut(20));

        JPanel bottom = new JPanel(new BorderLayout(20, 0));
        bottom.setOpaque(false);
        bottom.add(createPriorityPanel(), BorderLayout.CENTER);
        bottom.add(createInsightsPanel(), BorderLayout.EAST);
        dashboard.add(bottom);

        JScrollPane scroll = new JScrollPane(dashboard);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    private int getOverallPrepPercentage() {
        if (topicList.isEmpty()) return 0;
        int overall = 0;
        for (TopicData topic : topicList) {
            overall += topic.preparation;
        }
        return overall / topicList.size();
    }

    private String getGreeting() {
        int hour = java.time.LocalTime.now().getHour();
        String nameStr = currentStudentName.isEmpty() ? "Student" : currentStudentName;
        if (hour >= 5 && hour < 12) return "Good Morning, " + nameStr + "!";
        if (hour >= 12 && hour < 17) return "Good Afternoon, " + nameStr + "!";
        return "Good Evening, " + nameStr + "!";
    }

    private void updateGreeting() {
        if (studentHeaderLabel != null) {
            studentHeaderLabel.setText(getGreeting());
        }
    }

    private JPanel createExamDatePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(350, 80));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 225, 240), 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel icon = new JLabel("📅");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel t1 = new JLabel("Exam Date");
        t1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t1.setForeground(MUTED);

        JLabel t2 = new JLabel(examDate == null ? "Not Set" : examDate.format(displayFormatter));
        t2.setFont(new Font("Segoe UI", Font.BOLD, 19));
        t2.setForeground(TEXT);

        text.add(t1);
        text.add(t2);

        JButton edit = new JButton("✎");
        edit.setFocusPainted(false);
        edit.setBorderPainted(false);
        edit.setContentAreaFilled(false);
        edit.setFont(new Font("Segoe UI", Font.BOLD, 20));

        edit.addActionListener(e -> {
            String currentDate = examDate == null ? "" : examDate.format(displayFormatter);
            String date = JOptionPane.showInputDialog(this, "Enter exam date\nFormat: dd MMM yyyy", currentDate);

            if (date != null && !date.trim().isEmpty()) {
                try {
                    LocalDate newDate = LocalDate.parse(date.trim(), displayFormatter);
                    if (!newDate.isBefore(LocalDate.now())) {
                        examDate = newDate;
                        t2.setText(examDate.format(displayFormatter));
                        updateDaysLeft();
                    }
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Use dd MMM yyyy", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(icon, BorderLayout.WEST);
        panel.add(text, BorderLayout.CENTER);
        panel.add(edit, BorderLayout.EAST);
        return panel;
    }

    private String getDaysLeftText() {
        return examDate == null ? "—" : String.valueOf(calculateDaysLeft());
    }

    private int calculateDaysLeft() {
        if (examDate == null) return 0;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), examDate);
        return (int) Math.max(0, days);
    }

    private void updateDaysLeft() {
        if (daysLeftLabel != null) {
            daysLeftLabel.setText(getDaysLeftText());
            daysLeftLabel.repaint();
        }
    }

    private JPanel createStatCard(String iconText, String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(225, 230, 240), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel icon = new JLabel(iconText);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(color);

        if (title.equals("Days Left")) daysLeftLabel = valueLabel;
        if (title.equals("Overall Preparation")) overallPreparationLabel = valueLabel;
        if (title.equals("Topics Completed")) topicsCompletedLabel = valueLabel;
        if (title.equals("Practice Accuracy")) accuracyLabel = valueLabel;

        text.add(titleLabel);
        text.add(Box.createVerticalStrut(5));
        text.add(valueLabel);

        card.add(icon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel createStudyTimePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(225, 230, 240), 1, true),
                new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel title = new JLabel("Total Study Time");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT);

        totalTimeLabel = new JLabel(formatTime(totalStudySeconds));
        totalTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        totalTimeLabel.setForeground(TEXT);

        JLabel message = new JLabel("Keep tracking your time!");
        message.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        message.setForeground(MUTED);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        left.add(title);
        left.add(Box.createVerticalStrut(30));
        left.add(totalTimeLabel);
        left.add(Box.createVerticalStrut(8));
        left.add(message);

        JLabel image = new JLabel("⏰");
        image.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 90));

        panel.add(left, BorderLayout.CENTER);
        panel.add(image, BorderLayout.EAST);
        return panel;
    }

    private JPanel createPreparationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(225, 230, 240), 1, true),
                new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel title = new JLabel("Preparation Progress");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT);

        int prep = getOverallPrepPercentage();
        ProgressCircle circle = new ProgressCircle(prep);
        JPanel legend = new JPanel();
        legend.setOpaque(false);
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));

        JLabel prepared = new JLabel("🟩  Prepared        " + prep + "%");
        JLabel remaining = new JLabel("🟦  Remaining      " + (100 - prep) + "%");
        prepared.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        remaining.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        legend.add(prepared);
        legend.add(Box.createVerticalStrut(12));
        legend.add(remaining);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        center.setOpaque(false);
        center.add(circle);
        center.add(legend);

        panel.add(title, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPriorityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(225, 230, 240), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JLabel title = new JLabel("⭐  Priority Topics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);

        JButton all = new JButton("View All Topics →");
        all.setFocusPainted(false);
        all.addActionListener(e -> cardLayout.show(contentPanel, "Topics"));

        heading.add(title, BorderLayout.WEST);
        heading.add(all, BorderLayout.EAST);
        panel.add(heading, BorderLayout.NORTH);

        JPanel topics = new JPanel();
        topics.setOpaque(false);
        topics.setLayout(new BoxLayout(topics, BoxLayout.Y_AXIS));

        for (int i = 0; i < Math.min(5, topicList.size()); i++) {
            topics.add(createTopicRow(i));
            topics.add(Box.createVerticalStrut(8));
        }

        panel.add(topics, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTopicRow(int index) {
        TopicData topic = topicList.get(index);
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(new Color(250, 251, 255));
        row.setBorder(new CompoundBorder(
                new LineBorder(new Color(235, 238, 245), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel number = new JLabel(String.valueOf(index + 1), SwingConstants.CENTER);
        number.setPreferredSize(new Dimension(35, 35));
        number.setFont(new Font("Segoe UI", Font.BOLD, 16));
        number.setForeground(Color.WHITE);
        number.setBackground(index == 0 ? new Color(240, 60, 70) : (index == 1 ? ORANGE : GREEN));
        number.setOpaque(true);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(topic.name);
        name.setFont(new Font("Segoe UI", Font.BOLD, 16));
        name.setForeground(TEXT);

        JLabel details = new JLabel(
                topic.marks + " Marks   •   "
                + topic.difficulty + "   •   "
                + topic.preparation + "% Prepared"
        );
        details.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        details.setForeground(MUTED);

        info.add(name);
        info.add(Box.createVerticalStrut(4));
        info.add(details);

        JPanel btnActionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnActionGroup.setOpaque(false);

        JButton completeBtn = new JButton(topic.completed ? "✓ Completed" : "Mark Complete");
        completeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        completeBtn.setBackground(topic.completed ? GREEN : new Color(230, 235, 245));
        completeBtn.setForeground(topic.completed ? Color.WHITE : TEXT);
        completeBtn.setOpaque(true);
        completeBtn.setContentAreaFilled(true);
        completeBtn.setBorderPainted(false);
        completeBtn.setFocusPainted(false);

        completeBtn.addActionListener(e -> {
            topic.completed = !topic.completed;
            topic.preparation = topic.completed ? 100 : 0;
            refreshAllPages("Dashboard");
        });

        JButton study = new JButton("Study  →");
        study.setFocusPainted(false);
        study.addActionListener(e -> openDetailedStudyWindow(index));

        btnActionGroup.add(completeBtn);
        btnActionGroup.add(study);

        row.add(number, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        row.add(btnActionGroup, BorderLayout.EAST);
        return row;
    }

    // =====================================================
    // DETAILED STUDY WINDOW
    // =====================================================

    private void openDetailedStudyWindow(int topicIndex) {
        currentTopic = topicIndex;
        TopicData topic = topicList.get(topicIndex);

        JDialog studyDialog = new JDialog(this, "Studying: " + topic.name, true);
        studyDialog.setSize(900, 650);
        studyDialog.setLocationRelativeTo(this);
        studyDialog.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(NAVY);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel topicTitle = new JLabel("📖 " + topic.name + " (" + topic.subjectName + ")");
        topicTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topicTitle.setForeground(Color.WHITE);

        JLabel timerDisplay = new JLabel(formatTime(topic.seconds));
        timerDisplay.setFont(new Font("Segoe UI", Font.BOLD, 26));
        timerDisplay.setForeground(Color.CYAN);

        JButton startStopBtn = new JButton("▶ Start Study Timer");
        startStopBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        startStopBtn.addActionListener(e -> {
            if (studyTimer != null && studyTimer.isRunning()) {
                studyTimer.stop();
                startStopBtn.setText("▶ Start Study Timer");
            } else {
                studyTimer = new javax.swing.Timer(1000, ev -> {
                    topic.seconds++;
                    totalStudySeconds++;
                    timerDisplay.setText(formatTime(topic.seconds));
                    updateTotalTime();
                });
                studyTimer.start();
                startStopBtn.setText("⏸ Pause Timer");
            }
        });

        JPanel timerControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        timerControls.setOpaque(false);
        timerControls.add(timerDisplay);
        timerControls.add(startStopBtn);

        headerPanel.add(topicTitle, BorderLayout.WEST);
        headerPanel.add(timerControls, BorderLayout.EAST);

        final int[] currentPage = {0};
        String[] pages = topic.explanationPages;

        JLabel contentLabel = new JLabel(pages[0]);
        contentLabel.setVerticalAlignment(SwingConstants.TOP);
        contentLabel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JScrollPane scrollPane = new JScrollPane(contentLabel);
        scrollPane.setBorder(null);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(new EmptyBorder(12, 25, 12, 25));
        footerPanel.setBackground(WHITE);

        JLabel pageTracker = new JLabel("Page 1 of " + pages.length);
        pageTracker.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton prevBtn = new JButton("← Previous Page");
        JButton nextBtn = new JButton("Next Page →");
        prevBtn.setEnabled(false);

        prevBtn.addActionListener(e -> {
            if (currentPage[0] > 0) {
                currentPage[0]--;
                contentLabel.setText(pages[currentPage[0]]);
                pageTracker.setText("Page " + (currentPage[0] + 1) + " of " + pages.length);
                nextBtn.setEnabled(true);
                if (currentPage[0] == 0) prevBtn.setEnabled(false);
            }
        });

        nextBtn.addActionListener(e -> {
            if (currentPage[0] < pages.length - 1) {
                currentPage[0]++;
                contentLabel.setText(pages[currentPage[0]]);
                pageTracker.setText("Page " + (currentPage[0] + 1) + " of " + pages.length);
                prevBtn.setEnabled(true);
                if (currentPage[0] == pages.length - 1) nextBtn.setEnabled(false);
            }
        });

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navButtons.setOpaque(false);
        navButtons.add(prevBtn);
        navButtons.add(nextBtn);

        footerPanel.add(pageTracker, BorderLayout.WEST);
        footerPanel.add(navButtons, BorderLayout.EAST);

        studyDialog.add(headerPanel, BorderLayout.NORTH);
        studyDialog.add(scrollPane, BorderLayout.CENTER);
        studyDialog.add(footerPanel, BorderLayout.SOUTH);

        studyDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (studyTimer != null) studyTimer.stop();
            }
        });

        studyDialog.setVisible(true);
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private void updateTotalTime() {
        if (totalTimeLabel != null) {
            totalTimeLabel.setText(formatTime(totalStudySeconds));
        }
    }

    private String getCompletedText() {
        int count = 0;
        for (TopicData topic : topicList) {
            if (topic.completed) count++;
        }
        return count + " / " + topicList.size();
    }

    private void updateStatistics() {
        int completedCount = 0;
        int overall = 0;

        for (TopicData topic : topicList) {
            if (topic.completed) completedCount++;
            overall += topic.preparation;
        }

        if (!topicList.isEmpty()) overall /= topicList.size();

        if (overallPreparationLabel != null) overallPreparationLabel.setText(overall + "%");
        if (topicsCompletedLabel != null) topicsCompletedLabel.setText(completedCount + " / " + topicList.size());
        if (accuracyLabel != null) {
            accuracyLabel.setText(practiceAttempts == 0 ? "0%" : (practiceCorrect * 100 / practiceAttempts) + "%");
        }
    }

    private JPanel createInsightsPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(330, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(225, 230, 240), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("📊 Study Insights");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        addInsight(panel, "🎯", "Focus More", "Graphs needs more attention.");
        addInsight(panel, "⏰", "Time Management", "Track your study time daily.");
        addInsight(panel, "📋", "Practice Regularly", "Improve accuracy with practice.");
        return panel;
    }

    private void addInsight(JPanel parent, String icon, String title, String text) {
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 25));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(TEXT);

        JLabel d = new JLabel(text);
        d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        d.setForeground(MUTED);

        textPanel.add(t);
        textPanel.add(d);

        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setOpaque(false);
        item.add(iconLabel, BorderLayout.WEST);
        item.add(textPanel, BorderLayout.CENTER);

        parent.add(item);
        parent.add(Box.createVerticalStrut(15));
    }

    // =====================================================
    // TOPICS PAGE
    // =====================================================

    private JPanel createTopicsPage() {
        JPanel panel = createPagePanel("🎯 Topics", "All study topics across subjects");

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        for (int i = 0; i < topicList.size(); i++) {
            TopicData topic = topicList.get(i);

            JPanel row = new JPanel(new BorderLayout(15, 8));
            row.setBackground(Color.WHITE);
            row.setBorder(new CompoundBorder(
                    new LineBorder(new Color(225, 230, 240), 1, true),
                    new EmptyBorder(16, 18, 16, 18)
            ));

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

            JLabel name = new JLabel(topic.name + "  —  " + topic.subjectName);
            name.setFont(new Font("Segoe UI", Font.BOLD, 17));
            name.setForeground(TEXT);

            JLabel details = new JLabel(topic.marks + " Marks  •  " + topic.difficulty + "  •  " + topic.preparation + "% Prepared");
            details.setForeground(MUTED);

            info.add(name);
            info.add(Box.createVerticalStrut(4));
            info.add(details);

            JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            btnGroup.setOpaque(false);

            JButton completeBtn = new JButton(topic.completed ? "✓ Completed" : "Mark Complete");
            completeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            completeBtn.setBackground(topic.completed ? GREEN : new Color(230, 235, 245));
            completeBtn.setForeground(topic.completed ? Color.WHITE : TEXT);
            completeBtn.setOpaque(true);
            completeBtn.setContentAreaFilled(true);
            completeBtn.setBorderPainted(false);
            completeBtn.setFocusPainted(false);

            completeBtn.addActionListener(e -> {
                topic.completed = !topic.completed;
                topic.preparation = topic.completed ? 100 : 0;
                refreshAllPages("Topics");
            });

            JButton study = new JButton("Study →");
            study.setFocusPainted(false);
            final int index = i;
            study.addActionListener(e -> openDetailedStudyWindow(index));

            btnGroup.add(completeBtn);
            btnGroup.add(study);

            row.add(info, BorderLayout.CENTER);
            row.add(btnGroup, BorderLayout.EAST);

            list.add(row);
            list.add(Box.createVerticalStrut(10));
        }

        panel.add(new JScrollPane(list));
        return panel;
    }

    // =====================================================
    // PRACTICE PAGE
    // =====================================================

    private JPanel createPracticePage() {
        JPanel panel = createPagePanel("✎ Practice", "Test your knowledge and track real-time accuracy");

        QuestionData qData = practiceQuestions.get(currentQuestionIndex);

        JLabel questionNum = new JLabel("Question " + (currentQuestionIndex + 1) + " of " + practiceQuestions.size());
        questionNum.setFont(new Font("Segoe UI", Font.BOLD, 14));
        questionNum.setForeground(PURPLE);

        JLabel questionText = new JLabel("<html><h3>" + qData.question + "</h3></html>");

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        optionsPanel.setOpaque(false);

        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JButton[] optionBtns = new JButton[4];

        for (int i = 0; i < 4; i++) {
            optionBtns[i] = new JButton(qData.options[i]);
            optionBtns[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            optionBtns[i].setHorizontalAlignment(SwingConstants.LEFT);
            final int chosen = i;

            optionBtns[i].addActionListener(e -> {
                practiceAttempts++;
                if (chosen == qData.correctAnswerIndex) {
                    practiceCorrect++;
                    resultLabel.setText("✓ Correct!");
                    resultLabel.setForeground(GREEN);
                } else {
                    resultLabel.setText("✗ Incorrect! Correct: " + qData.options[qData.correctAnswerIndex]);
                    resultLabel.setForeground(Color.RED);
                }
                for (JButton btn : optionBtns) btn.setEnabled(false);
                updateStatistics();
            });

            optionsPanel.add(optionBtns[i]);
        }

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navPanel.setOpaque(false);

        JButton nextQ = new JButton("Next Question →");
        nextQ.addActionListener(e -> {
            currentQuestionIndex = (currentQuestionIndex + 1) % practiceQuestions.size();
            refreshAllPages("Practice");
        });
        navPanel.add(nextQ);

        panel.add(questionNum);
        panel.add(Box.createVerticalStrut(10));
        panel.add(questionText);
        panel.add(Box.createVerticalStrut(15));
        panel.add(optionsPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(resultLabel);
        panel.add(navPanel);

        return panel;
    }

    // =====================================================
    // PROGRESS PAGE
    // =====================================================

    private JPanel createProgressPage() {
        JPanel panel = createPagePanel("📊 Progress", "Track preparation performance and practice accuracy");

        int overall = getOverallPrepPercentage();

        JLabel overallLabel = new JLabel("Overall Preparation: " + overall + "%");
        overallLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JProgressBar overallBar = new JProgressBar(0, 100);
        overallBar.setValue(overall);
        overallBar.setStringPainted(true);
        overallBar.setPreferredSize(new Dimension(700, 30));

        int accuracy = (practiceAttempts == 0) ? 0 : (practiceCorrect * 100 / practiceAttempts);
        JLabel accLabel = new JLabel("Practice Accuracy: " + accuracy + "% (" + practiceCorrect + " / " + practiceAttempts + " Correct)");
        accLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        accLabel.setForeground(ORANGE);

        panel.add(overallLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(overallBar);
        panel.add(Box.createVerticalStrut(20));
        panel.add(accLabel);
        panel.add(Box.createVerticalStrut(25));

        JPanel topicProgressList = new JPanel();
        topicProgressList.setOpaque(false);
        topicProgressList.setLayout(new BoxLayout(topicProgressList, BoxLayout.Y_AXIS));

        for (TopicData topic : topicList) {
            JLabel topicLabel = new JLabel(topic.name + " (" + topic.subjectName + ") - " + topic.preparation + "%");
            topicLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue(topic.preparation);
            bar.setStringPainted(true);

            topicProgressList.add(topicLabel);
            topicProgressList.add(bar);
            topicProgressList.add(Box.createVerticalStrut(12));
        }

        panel.add(new JScrollPane(topicProgressList));
        return panel;
    }

    // =====================================================
    // SETTINGS PAGE (FIXED ALIGNMENT)
    // =====================================================

    private JPanel createSettingsPage() {
        JPanel panel = createPagePanel("⚙ Settings", "Customize your study preferences and learning strategy");

        JPanel settingsContainer = new JPanel();
        settingsContainer.setOpaque(false);
        settingsContainer.setLayout(new BoxLayout(settingsContainer, BoxLayout.Y_AXIS));

        // SECTION 1: STUDY & POMODORO PREFERENCES
        JPanel studyCard = createSettingsCard("⏱  Study & Pomodoro Strategy");

        JPanel comboRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        comboRow.setOpaque(false);
        comboRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sessionLabel = new JLabel("Focus Session Length:");
        sessionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JComboBox<String> sessionCombo = new JComboBox<>(new String[]{"25 mins (Standard Pomodoro)", "45 mins (Deep Work)", "60 mins (Intense Study)"});

        JLabel goalLabel = new JLabel("Daily Target Study:");
        goalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JSpinner goalSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 12, 1));
        JLabel hoursLabel = new JLabel("Hours/day");

        comboRow.add(sessionLabel);
        comboRow.add(sessionCombo);
        comboRow.add(Box.createHorizontalStrut(20));
        comboRow.add(goalLabel);
        comboRow.add(goalSpinner);
        comboRow.add(hoursLabel);

        studyCard.add(comboRow);
        settingsContainer.add(studyCard);
        settingsContainer.add(Box.createVerticalStrut(15));

        // SECTION 2: PRIORITIZATION ALGORITHM
        JPanel algoCard = createSettingsCard("🎯  Topic Priority Algorithm");

        JRadioButton algo1 = new JRadioButton("Balanced (Mix of High Marks & Hard Topics)", true);
        JRadioButton algo2 = new JRadioButton("Aggressive (Focus Heavily on Hard / Weak Topics First)");
        JRadioButton algo3 = new JRadioButton("Exam Weightage First (Focus purely on High Marks Topics)");

        ButtonGroup algoGroup = new ButtonGroup();
        algoGroup.add(algo1);
        algoGroup.add(algo2);
        algoGroup.add(algo3);

        algo1.setOpaque(false);
        algo2.setOpaque(false);
        algo3.setOpaque(false);

        algo1.setAlignmentX(Component.LEFT_ALIGNMENT);
        algo2.setAlignmentX(Component.LEFT_ALIGNMENT);
        algo3.setAlignmentX(Component.LEFT_ALIGNMENT);

        algoCard.add(algo1);
        algoCard.add(Box.createVerticalStrut(5));
        algoCard.add(algo2);
        algoCard.add(Box.createVerticalStrut(5));
        algoCard.add(algo3);

        settingsContainer.add(algoCard);
        settingsContainer.add(Box.createVerticalStrut(15));

        // SECTION 3: NOTIFICATIONS & APPEARANCE
        JPanel generalCard = createSettingsCard("🔔  Notifications & Display");

        JCheckBox reminder = new JCheckBox("Enable study break reminders");
        JCheckBox sound = new JCheckBox("Play sound when study session ends");
        JCheckBox dark = new JCheckBox("Enable Dark Mode interface");

        reminder.setOpaque(false);
        sound.setOpaque(false);
        dark.setOpaque(false);

        reminder.setAlignmentX(Component.LEFT_ALIGNMENT);
        sound.setAlignmentX(Component.LEFT_ALIGNMENT);
        dark.setAlignmentX(Component.LEFT_ALIGNMENT);

        generalCard.add(reminder);
        generalCard.add(Box.createVerticalStrut(5));
        generalCard.add(sound);
        generalCard.add(Box.createVerticalStrut(5));
        generalCard.add(dark);

        settingsContainer.add(generalCard);
        settingsContainer.add(Box.createVerticalStrut(15));

        // SECTION 4: DATA & RESET OPTIONS
        JPanel dataCard = createSettingsCard("🗑  Data & Progress Reset");

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn = new JButton("💾 Save All Preferences");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setBackground(PURPLE);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setOpaque(true);
        saveBtn.setContentAreaFilled(true);
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "All settings updated successfully!"));

        JButton resetPracticeBtn = new JButton("Reset Practice History");
        resetPracticeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resetPracticeBtn.setFocusPainted(false);
        resetPracticeBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to reset all practice test statistics?", "Reset Practice", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                practiceAttempts = 0;
                practiceCorrect = 0;
                updateStatistics();
                JOptionPane.showMessageDialog(this, "Practice history reset successfully!");
            }
        });

        btnRow.add(saveBtn);
        btnRow.add(resetPracticeBtn);

        dataCard.add(btnRow);
        settingsContainer.add(dataCard);

        JScrollPane scroll = new JScrollPane(settingsContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scroll);
        return panel;
    }

    private JPanel createSettingsCard(String cardTitle) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(225, 230, 240), 1, true),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JLabel title = new JLabel(cardTitle);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(12));
        return card;
    }

    private JPanel createPagePanel(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setBackground(LIGHT_BG);
        panel.setBorder(new EmptyBorder(35, 40, 35, 40));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 32));
        t.setForeground(TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel s = new JLabel(subtitle);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        s.setForeground(MUTED);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(t);
        panel.add(Box.createVerticalStrut(5));
        panel.add(s);
        panel.add(Box.createVerticalStrut(25));

        return panel;
    }

    // =====================================================
    // PROGRESS CIRCLE
    // =====================================================

    class ProgressCircle extends JPanel {
        private int percentage;

        ProgressCircle(int percentage) {
            this.percentage = percentage;
            setPreferredSize(new Dimension(170, 170));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = 125;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setStroke(new BasicStroke(16, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.setColor(new Color(220, 225, 240));
            g2.drawArc(x, y, size, size, 0, 360);

            int angle = (int) (360.0 * percentage / 100.0);
            g2.setColor(GREEN);
            g2.drawArc(x, y, size, size, 90, -angle);

            g2.setColor(TEXT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 25));
            String text = percentage + "%";
            FontMetrics fm = g2.getFontMetrics();
            int tx = getWidth() / 2 - fm.stringWidth(text) / 2;
            int ty = getHeight() / 2 + fm.getAscent() / 2;

            g2.drawString(text, tx, ty);
            g2.dispose();
        }
    }

    // =====================================================
    // DATA CLASSES
    // =====================================================

    static class TopicData {
        String name;
        int marks;
        String difficulty;
        int preparation;
        boolean completed;
        int seconds;
        String subjectName;
        String[] explanationPages;

        TopicData(String name, int marks, String difficulty, int preparation, String subjectName, String[] explanationPages) {
            this.name = name;
            this.marks = marks;
            this.difficulty = difficulty;
            this.preparation = preparation;
            this.completed = preparation >= 100;
            this.seconds = 0;
            this.subjectName = subjectName;
            this.explanationPages = explanationPages;
        }
    }

    static class QuestionData {
        String question;
        String[] options;
        int correctAnswerIndex;

        QuestionData(String question, String[] options, int correctAnswerIndex) {
            this.question = question;
            this.options = options;
            this.correctAnswerIndex = correctAnswerIndex;
        }
    }

    // =====================================================
    // MAIN
    // =====================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}
            new ExamPrioritizer().setVisible(true);
        });
    }
}