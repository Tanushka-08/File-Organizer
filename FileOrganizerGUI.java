import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Map;

public class FileOrganizerGUI extends JFrame {

    private JTextField folderPathField;
    private JTextArea outputArea;
    private JLabel totalFilesLabel, imagesLabel, documentsLabel, videosLabel,
            musicLabel, archivesLabel, programsLabel, codeLabel, othersLabel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private boolean darkMode = false;

    private Color bgMain = new Color(245, 247, 250);
    private Color bgCard = Color.WHITE;
    private Color textPrimary = new Color(35, 35, 35);
    private Color textSecondary = new Color(90, 90, 90);
    private Color headerColor = new Color(35, 49, 82);

    private JButton darkModeButton;

    public FileOrganizerGUI() {
        setTitle("File Organizer Script");
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        buildUI();

        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            playStartupAnimation();
        });
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(18, 18));
        mainPanel.setBackground(bgMain);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);

        centerPanel.add(createTopControlPanel(), BorderLayout.NORTH);
        centerPanel.add(createMainContentPanel(), BorderLayout.CENTER);
        centerPanel.add(createBottomStatusPanel(), BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        revalidate();
        repaint();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(headerColor);
        headerPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(0, 0, 0, 30), 1, true),
                new EmptyBorder(22, 25, 22, 25)
        ));

        JLabel titleLabel = new JLabel("File Organizer Script");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));

        JLabel subtitleLabel = new JLabel("Smart Desktop File Management using Java");
        subtitleLabel.setForeground(new Color(220, 220, 220));
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitleLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        darkModeButton = createStyledButton("Dark Mode", new Color(75, 85, 99));
        darkModeButton.setPreferredSize(new Dimension(150, 42));
        darkModeButton.addActionListener(e -> toggleDarkMode());

        rightPanel.add(darkModeButton);

        headerPanel.add(textPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createTopControlPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        JPanel folderPanel = new JPanel();
        folderPanel.setLayout(new BoxLayout(folderPanel, BoxLayout.Y_AXIS));
        folderPanel.setBackground(bgCard);
        folderPanel.setBorder(createCardBorder());

        JLabel folderLabel = new JLabel("Selected Folder");
        folderLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        folderLabel.setForeground(textPrimary);
        folderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        folderPathField = new JTextField("No folder selected");
        folderPathField.setEditable(false);
        folderPathField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        folderPathField.setPreferredSize(new Dimension(760, 44));
        folderPathField.setBackground(darkMode ? new Color(50, 55, 65) : new Color(250, 250, 250));
        folderPathField.setForeground(textPrimary);
        folderPathField.setCaretColor(textPrimary);

        JButton browseButton = createStyledButton("Browse Folder", new Color(52, 152, 219));
        browseButton.setPreferredSize(new Dimension(170, 44));
        browseButton.addActionListener(e -> chooseFolder());

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        inputRow.setOpaque(false);
        inputRow.add(folderPathField);
        inputRow.add(browseButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 12));
        buttonPanel.setOpaque(false);

        JButton previewButton = createStyledButton("Preview Files", new Color(46, 204, 113));
        JButton organizeButton = createStyledButton("Organize Files", new Color(155, 89, 182));
        JButton undoButton = createStyledButton("Undo Last", new Color(241, 196, 15));
        JButton customCategoryButton = createStyledButton("Add Category", new Color(230, 126, 34));
        JButton clearButton = createStyledButton("Clear Output", new Color(231, 76, 60));

        previewButton.addActionListener(e -> previewFiles());
        organizeButton.addActionListener(e -> organizeFiles());
        undoButton.addActionListener(e -> undoLast());
        customCategoryButton.addActionListener(e -> addCustomCategory());
        clearButton.addActionListener(e -> clearOutput());

        buttonPanel.add(previewButton);
        buttonPanel.add(organizeButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(customCategoryButton);
        buttonPanel.add(clearButton);

        folderPanel.add(folderLabel);
        folderPanel.add(Box.createVerticalStrut(12));
        folderPanel.add(inputRow);
        folderPanel.add(Box.createVerticalStrut(12));
        folderPanel.add(buttonPanel);

        wrapper.add(folderPanel);

        return wrapper;
    }

    private JPanel createMainContentPanel() {
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        contentPanel.setOpaque(false);
        contentPanel.add(createStatsPanel());
        contentPanel.add(createOutputPanel());
        return contentPanel;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(bgCard);
        statsPanel.setBorder(createCardBorder());

        JLabel title = new JLabel("File Statistics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(textPrimary);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalFilesLabel = createStatCard("Total Files: 0");
        imagesLabel = createStatCard("Images: 0");
        documentsLabel = createStatCard("Documents: 0");
        videosLabel = createStatCard("Videos: 0");
        musicLabel = createStatCard("Music: 0");
        archivesLabel = createStatCard("Archives: 0");
        programsLabel = createStatCard("Programs: 0");
        codeLabel = createStatCard("Code: 0");
        othersLabel = createStatCard("Others: 0");

        statsPanel.add(title);
        statsPanel.add(Box.createVerticalStrut(20));
        statsPanel.add(totalFilesLabel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(imagesLabel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(documentsLabel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(videosLabel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(musicLabel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(archivesLabel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(programsLabel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(codeLabel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(othersLabel);

        return statsPanel;
    }

    private JPanel createOutputPanel() {
        JPanel outputPanel = new JPanel(new BorderLayout(10, 10));
        outputPanel.setBackground(bgCard);
        outputPanel.setBorder(createCardBorder());

        JLabel title = new JLabel("System Output");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(textPrimary);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBackground(darkMode ? new Color(34, 39, 48) : new Color(250, 250, 250));
        outputArea.setForeground(textPrimary);
        outputArea.setMargin(new Insets(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));

        outputPanel.add(title, BorderLayout.NORTH);
        outputPanel.add(scrollPane, BorderLayout.CENTER);

        return outputPanel;
    }

    private JPanel createBottomStatusPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(bgCard);
        bottomPanel.setBorder(createCardBorder());

        progressBar = new JProgressBar();
        progressBar.setForeground(new Color(52, 152, 219));
        progressBar.setBackground(darkMode ? new Color(55, 60, 70) : new Color(230, 230, 230));
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

        statusLabel = new JLabel("Status: Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(textPrimary);

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(progressBar, BorderLayout.CENTER);

        return bottomPanel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);

        button.setBorder(new CompoundBorder(
                new LineBorder(bgColor.darker(), 1, true),
                new EmptyBorder(12, 20, 12, 20)
        ));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(brighten(bgColor, 18));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(darken(bgColor, 18));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(brighten(bgColor, 10));
            }
        });

        return button;
    }

    private JLabel createStatCard(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setOpaque(true);
        label.setBackground(darkMode ? new Color(50, 55, 65) : new Color(248, 249, 250));
        label.setForeground(textPrimary);
        label.setBorder(new CompoundBorder(
                new LineBorder(darkMode ? new Color(85, 90, 100) : new Color(230, 230, 230), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private Border createCardBorder() {
        return new CompoundBorder(
                new LineBorder(darkMode ? new Color(75, 80, 90) : new Color(220, 220, 220), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        );
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File folder = chooser.getSelectedFile();
            folderPathField.setText(folder.getAbsolutePath());
            typeWriterOutput("Selected Folder:\n" + folder.getAbsolutePath() + "\n\nReady for preview or organization.\n");
            animateStatus("Status: Folder selected");
            animateProgress(0, 25);
            resetStats();
        }
    }

    private void previewFiles() {
        String path = folderPathField.getText().trim();

        if (path.isEmpty() || path.equals("No folder selected")) {
            JOptionPane.showMessageDialog(this, "Please select a folder first.");
            return;
        }

        animateStatus("Status: Previewing files...");
        animateProgress(progressBar.getValue(), 40);

        FileOrganizer organizer = new FileOrganizer(path);
        organizer.preview();

        outputArea.setText(organizer.getOutputLog());
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
        updateStats(organizer);

        animateProgress(40, 100);
        animateStatus("Status: Preview completed");
    }

    private void organizeFiles() {
        String path = folderPathField.getText().trim();

        if (path.isEmpty() || path.equals("No folder selected")) {
            JOptionPane.showMessageDialog(this, "Please select a folder first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to organize files in this folder?",
                "Confirm Organization",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            animateStatus("Status: Organizing files...");
            animateProgress(progressBar.getValue(), 55);

            FileOrganizer organizer = new FileOrganizer(path);
            organizer.organize();

            outputArea.setText(organizer.getOutputLog());
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
            updateStats(organizer);

            animateProgress(55, 100);
            animateStatus("Status: Organization completed");
        }
    }

    private void undoLast() {
        String path = folderPathField.getText().trim();

        if (path.isEmpty() || path.equals("No folder selected")) {
            JOptionPane.showMessageDialog(this, "Please select a folder first.");
            return;
        }

        animateStatus("Status: Undoing last operation...");
        animateProgress(progressBar.getValue(), 50);

        FileOrganizer organizer = new FileOrganizer(path);
        organizer.undoLastOrganization();

        outputArea.setText(organizer.getOutputLog());
        outputArea.setCaretPosition(outputArea.getDocument().getLength());

        animateProgress(50, 100);
        animateStatus("Status: Undo completed");
    }

    private void addCustomCategory() {
        String path = folderPathField.getText().trim();

        if (path.isEmpty() || path.equals("No folder selected")) {
            JOptionPane.showMessageDialog(this, "Please select a folder first.");
            return;
        }

        String categoryName = JOptionPane.showInputDialog(this, "Enter category name:");
        if (categoryName == null || categoryName.trim().isEmpty()) return;

        String extensions = JOptionPane.showInputDialog(this,
                "Enter extensions separated by commas (example: .psd,.ai,.fig):");
        if (extensions == null || extensions.trim().isEmpty()) return;

        FileOrganizer organizer = new FileOrganizer(path);
        organizer.addCustomCategory(categoryName.trim(), extensions.trim());

        outputArea.setText(organizer.getOutputLog());
        outputArea.setCaretPosition(outputArea.getDocument().getLength());

        animateStatus("Status: Custom category added");
        animateProgress(progressBar.getValue(), 100);
    }

    private void clearOutput() {
        outputArea.setText("");
        folderPathField.setText("No folder selected");
        animateProgress(progressBar.getValue(), 0);
        animateStatus("Status: Ready");
        resetStats();
    }

    private void updateStats(FileOrganizer organizer) {
        Map<String, Integer> stats = organizer.getCategoryStats();

        totalFilesLabel.setText("Total Files: " + organizer.getTotalFilesProcessed());
        imagesLabel.setText("Images: " + stats.getOrDefault("Images", 0));
        documentsLabel.setText("Documents: " + stats.getOrDefault("Documents", 0));
        videosLabel.setText("Videos: " + stats.getOrDefault("Videos", 0));
        musicLabel.setText("Music: " + stats.getOrDefault("Music", 0));
        archivesLabel.setText("Archives: " + stats.getOrDefault("Archives", 0));
        programsLabel.setText("Programs: " + stats.getOrDefault("Programs", 0));
        codeLabel.setText("Code: " + stats.getOrDefault("Code", 0));
        othersLabel.setText("Others: " + stats.getOrDefault("Others", 0));
    }

    private void resetStats() {
        totalFilesLabel.setText("Total Files: 0");
        imagesLabel.setText("Images: 0");
        documentsLabel.setText("Documents: 0");
        videosLabel.setText("Videos: 0");
        musicLabel.setText("Music: 0");
        archivesLabel.setText("Archives: 0");
        programsLabel.setText("Programs: 0");
        codeLabel.setText("Code: 0");
        othersLabel.setText("Others: 0");
    }

    private void toggleDarkMode() {
        if (!darkMode) {
            bgMain = new Color(28, 30, 36);
            bgCard = new Color(40, 44, 52);
            textPrimary = Color.WHITE;
            textSecondary = new Color(200, 200, 200);
            headerColor = new Color(18, 24, 38);
            darkModeButton.setText("Light Mode");
        } else {
            bgMain = new Color(245, 247, 250);
            bgCard = Color.WHITE;
            textPrimary = new Color(35, 35, 35);
            textSecondary = new Color(90, 90, 90);
            headerColor = new Color(35, 49, 82);
            darkModeButton.setText("Dark Mode");
        }

        darkMode = !darkMode;
        buildUI();
        setVisible(true);
    }

    // ---------- ANIMATION METHODS ----------

    private void animateProgress(int start, int end) {
        Timer timer = new Timer(12, null);
        final int[] value = {start};

        timer.addActionListener(e -> {
            if (value[0] < end) {
                value[0]++;
                progressBar.setValue(value[0]);
            } else if (value[0] > end) {
                value[0]--;
                progressBar.setValue(value[0]);
            } else {
                timer.stop();
            }
        });

        timer.start();
    }

    private void animateStatus(String text) {
        Timer blinkTimer = new Timer(120, null);
        final int[] count = {0};

        blinkTimer.addActionListener(e -> {
            if (count[0] % 2 == 0) {
                statusLabel.setForeground(new Color(52, 152, 219));
            } else {
                statusLabel.setForeground(textPrimary);
            }

            count[0]++;

            if (count[0] >= 6) {
                statusLabel.setText(text);
                statusLabel.setForeground(textPrimary);
                blinkTimer.stop();
            }
        });

        blinkTimer.start();
    }

    private void typeWriterOutput(String text) {
        outputArea.setText("");
        Timer timer = new Timer(12, null);
        final int[] index = {0};

        timer.addActionListener(e -> {
            if (index[0] < text.length()) {
                outputArea.append(String.valueOf(text.charAt(index[0])));
                outputArea.setCaretPosition(outputArea.getDocument().getLength());
                index[0]++;
            } else {
                timer.stop();
            }
        });

        timer.start();
    }

    private void playStartupAnimation() {
        typeWriterOutput("Welcome to File Organizer Script\n\n" +
                "This application helps you:\n" +
                "- Preview files\n" +
                "- Organize them by category\n" +
                "- Undo last organization\n" +
                "- Add custom categories\n\n" +
                "Select a folder to begin.\n");

        animateStatus("Status: Application Ready");
        animateProgress(0, 15);
    }

    private Color brighten(Color color, int amount) {
        int r = Math.min(255, color.getRed() + amount);
        int g = Math.min(255, color.getGreen() + amount);
        int b = Math.min(255, color.getBlue() + amount);
        return new Color(r, g, b);
    }

    private Color darken(Color color, int amount) {
        int r = Math.max(0, color.getRed() - amount);
        int g = Math.max(0, color.getGreen() - amount);
        int b = Math.max(0, color.getBlue() - amount);
        return new Color(r, g, b);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileOrganizerGUI::new);
    }
}