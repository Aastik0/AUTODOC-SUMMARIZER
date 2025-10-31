package com.autodoc;

import com.autodoc.db.DatabaseManager;
import com.autodoc.db.GlossaryDao;
import com.autodoc.service.NlpService;
import com.autodoc.service.OcrService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

// NEW IMPORTS for HTTP Client
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MainApp {

    // --- CRITICAL N8N CONFIGURATION ---
    // !!! REPLACE THIS WITH YOUR OWN N8N WEBHOOK URL !!!
    private static final String N8N_WEBHOOK_URL = "https://adikdm9.app.n8n.cloud/webhook/f814ea7c-984d-4230-aeae-550bd6eaf407";


    // --- Backend Services ---
    private final OcrService ocrService;
    private final NlpService nlpService;
    private String extractedTextContent = "";
    private String summaryTextContent = "";

    // --- UI Components ---
    private JFrame frame;
    private JTextArea originalContentArea;
    private JTextArea summaryArea;
    private JTextArea termsArea; 
    private JButton uploadButton, simplifyButton, exportButton;
    private JPanel summaryCard; 

    // --- NEW UI COMPONENTS ---
    private JTextField emailField;
    private JButton emailButton;
    private HttpClient httpClient; // HTTP client for n8n

    // --- EXECUTIVE DARK MODE PALETTE (REVISED V2) ---
    private static final Color COLOR_PRIMARY_BG = new Color(18, 18, 18); // #121212
    private static final Color COLOR_CONSOLE_BG = new Color(30, 41, 59); // #1e293b
    private static final Color COLOR_MODULE_BG = new Color(32, 36, 42);  // #20242a
    private static final Color COLOR_ACCENT_CYAN = new Color(0, 191, 255); // Electric Cyan
    private static final Color COLOR_ACCENT_BLUE_DATA = new Color(59, 130, 246);  // NEW: Decent Data Blue
    private static final Color COLOR_ACCENT_GREEN_VALIDATION = new Color(16, 185, 129); // NEW: Decent Validation Green
    private static final Color COLOR_TEXT_PRIMARY = new Color(225, 225, 225);
    private static final Color COLOR_TEXT_SECONDARY = new Color(150, 150, 150);
    private static final Color COLOR_TEXT_DARK = Color.BLACK;

    // --- Fonts ---
    private static final Font FONT_LOGO = new Font("Segoe UI", Font.BOLD, 42);
    private static final Font FONT_SLOGAN = new Font("Segoe UI Light", Font.PLAIN, 16);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_MONOSPACE = new Font("Consolas", Font.PLAIN, 14);
    private static final Font FONT_TEXT = new Font("Segoe UI", Font.PLAIN, 14);


    public MainApp() {
        this.ocrService = new OcrService();
        GlossaryDao glossaryDao = new GlossaryDao();
        this.nlpService = new NlpService(glossaryDao);
        this.httpClient = HttpClient.newHttpClient(); // Initialize the HTTP client
        createAndShowGUI();
    }
    
    private void createAndShowGUI() {
        frame = new JFrame("AutoDoc AI Command Center");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1400, 800));
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(COLOR_PRIMARY_BG);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(COLOR_PRIMARY_BG);
        GridBagConstraints gbc = new GridBagConstraints();

        // --- Left Fixed Console ---
        JPanel consolePanel = createConsolePanel();
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(consolePanel, gbc);

        // --- Right Operational Dashboard ---
        JPanel dashboardPanel = createDashboardPanel();
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        mainPanel.add(dashboardPanel, gbc);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    // --- THIS METHOD IS UPDATED ---
    private JPanel createConsolePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_CONSOLE_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; // Make components fill horizontally

        // --- 1. Email Panel (Stays at the Top) ---
        JPanel emailPanel = new JPanel(new GridBagLayout());
        emailPanel.setOpaque(false); // Make it transparent
        GridBagConstraints emailGbc = new GridBagConstraints();
        emailGbc.fill = GridBagConstraints.HORIZONTAL;
        emailGbc.insets = new Insets(0, 30, 0, 30); // Horizontal padding

        JLabel emailLabel = new JLabel("Enter Email / Phone for Delivery");
        emailLabel.setFont(FONT_BUTTON);
        emailLabel.setForeground(COLOR_TEXT_SECONDARY);
        emailGbc.gridy = 0;
        emailGbc.insets = new Insets(20, 30, 10, 30);
        emailPanel.add(emailLabel, emailGbc);

        emailField = new JTextField();
        emailField.setBackground(COLOR_MODULE_BG);
        emailField.setForeground(COLOR_TEXT_PRIMARY);
        emailField.setCaretColor(COLOR_ACCENT_CYAN);
        emailField.setFont(FONT_TEXT);
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_ACCENT_CYAN, 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        emailGbc.gridy = 1;
        emailGbc.insets = new Insets(0, 30, 0, 30);
        emailPanel.add(emailField, emailGbc);
        
        // Add emailPanel to the main console panel at the top
        gbc.gridy = 0;
        gbc.weighty = 0; // Does not take vertical space
        gbc.anchor = GridBagConstraints.NORTH; // Stick to the top
        panel.add(emailPanel, gbc);

        // --- 2. Logo Panel (Centers itself) ---
        JPanel logoPanel = new JPanel(new GridBagLayout());
        logoPanel.setOpaque(false); // Make it transparent
        GridBagConstraints logoGbc = new GridBagConstraints();

        JLabel logoLabel = new JLabel("<html>AUTODOC <font color='#00BFFF'>AI</font></html>");
        logoLabel.setFont(FONT_LOGO);
        logoLabel.setForeground(COLOR_TEXT_PRIMARY);
        logoGbc.gridy = 0;
        logoGbc.insets = new Insets(0, 0, 10, 0);
        logoPanel.add(logoLabel, logoGbc);

        JLabel sloganLabel = new JLabel("Precision Analytics. Zero Latency.");
        sloganLabel.setFont(FONT_SLOGAN);
        sloganLabel.setForeground(COLOR_TEXT_SECONDARY);
        logoGbc.gridy = 1;
        logoPanel.add(sloganLabel, logoGbc);

        // Add logoPanel to the main console panel.
        // It takes up all the remaining vertical space (weighty 1.0)
        // and anchors itself in the center.
        gbc.gridy = 1;
        gbc.weighty = 1.0; // Takes all remaining vertical space
        gbc.anchor = GridBagConstraints.CENTER; // Centers itself
        panel.add(logoPanel, gbc);

        return panel;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_PRIMARY_BG);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        // --- Top Action Buttons ---
        JPanel buttonPanel = createHeaderButtonPanel();
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 25, 0);
        panel.add(buttonPanel, gbc);

        // --- Data Modules ---
        JScrollPane scrollPane = createScrollableModulesPanel();
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(scrollPane, gbc);

        return panel;
    }
    
    private JPanel createHeaderButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setOpaque(false);

        uploadButton = createStyledButton("Upload Report");
        simplifyButton = createStyledButton("Simplify Report");
        exportButton = createStyledButton("Export Summary");
        
        // --- NEW EMAIL BUTTON ---
        emailButton = createStyledButton("Email Summary");

        simplifyButton.setEnabled(false);
        exportButton.setEnabled(false);
        emailButton.setEnabled(false); // Disabled by default

        uploadButton.addActionListener(e -> uploadFile());
        simplifyButton.addActionListener(e -> simplifyReport());
        exportButton.addActionListener(e -> exportSummary());
        emailButton.addActionListener(e -> sendSummaryViaN8N()); // Add action listener

        panel.add(uploadButton);
        panel.add(simplifyButton);
        panel.add(exportButton);
        panel.add(emailButton); // Add new button to panel
        
        return panel;
    }
    
    private JScrollPane createScrollableModulesPanel() {
        JPanel modulesPanel = new JPanel(new GridBagLayout());
        modulesPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 20, 0);

        // --- Raw OCR Extract Module ---
        originalContentArea = createStyledTextArea("Awaiting document ingestion...", FONT_MONOSPACE);
        JPanel originalCard = createDataModule("RAW OCR EXTRACT", COLOR_ACCENT_CYAN, originalContentArea);
        gbc.gridy = 0;
        gbc.weighty = 0; 
        gbc.anchor = GridBagConstraints.NORTH;
        modulesPanel.add(originalCard, gbc);

        // --- Simplified Summary Module ---
        summaryArea = createStyledTextArea("NLP analysis results will appear here...", FONT_TEXT);
        summaryCard = createDataModule("SIMPLIFIED SUMMARY", COLOR_ACCENT_BLUE_DATA, summaryArea); 
        gbc.gridy = 1;
        gbc.weighty = 0; 
        modulesPanel.add(summaryCard, gbc);

        // --- Identified Terms Module (now with JTextArea) ---
        termsArea = createStyledTextArea("Validated medical term definitions will appear here...", FONT_TEXT);
        JPanel termsCard = createDataModule("IDENTIFIED MEDICAL TERMS", COLOR_ACCENT_GREEN_VALIDATION, termsArea);
        gbc.gridy = 2;
        gbc.weighty = 0; 
        modulesPanel.add(termsCard, gbc);

        // --- Filler Panel to absorb extra space ---
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        gbc.gridy = 3;
        gbc.weighty = 1.0; 
        modulesPanel.add(filler, gbc);

        JScrollPane scrollPane = new JScrollPane(modulesPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(COLOR_PRIMARY_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                int scrollAmount = e.getWheelRotation() * verticalScrollBar.getUnitIncrement();
                verticalScrollBar.setValue(verticalScrollBar.getValue() + scrollAmount);
            }
        });

        return scrollPane;
    }

    private JPanel createDataModule(String title, Color headerColor, Component content) {
        JPanel module = new JPanel(new BorderLayout());
        module.setBackground(COLOR_MODULE_BG);
        module.setBorder(new EmptyBorder(3, 3, 3, 3)); 
        module.setPreferredSize(new Dimension(0, 250)); 

        JPanel innerPanel = new JPanel(new BorderLayout(0, 10));
        innerPanel.setBackground(COLOR_MODULE_BG);
        innerPanel.setBorder(BorderFactory.createLineBorder(COLOR_CONSOLE_BG));
        
        JPanel headerStripe = new JPanel(new BorderLayout());
        headerStripe.setBackground(headerColor);
        JLabel titleLabel = new JLabel(" " + title);
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_TEXT_DARK);
        headerStripe.add(titleLabel, BorderLayout.CENTER);
        
        innerPanel.add(headerStripe, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.getViewport().setBackground(COLOR_MODULE_BG);
        scrollPane.setBorder(null);
        innerPanel.add(scrollPane, BorderLayout.CENTER);
        
        module.add(innerPanel, BorderLayout.CENTER);
        return module;
    }
    
    private void updateTermsPanel(String definitions) {
        if (definitions.isEmpty() || definitions.startsWith("No specific")) {
            termsArea.setForeground(COLOR_TEXT_SECONDARY);
            termsArea.setText("No validated terms identified from glossary.");
        } else {
            termsArea.setForeground(COLOR_TEXT_PRIMARY);
            termsArea.setText(definitions);
        }
        termsArea.setCaretPosition(0);
    }
    
    private JTextArea createStyledTextArea(String placeholder, Font font) {
        JTextArea textArea = new JTextArea(placeholder);
        textArea.setFont(font);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(COLOR_MODULE_BG);
        textArea.setForeground(COLOR_TEXT_PRIMARY);
        textArea.setCaretColor(COLOR_ACCENT_CYAN);
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        return textArea;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setBackground(COLOR_ACCENT_CYAN);
        button.setForeground(COLOR_TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 25, 12, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(COLOR_ACCENT_CYAN.brighter());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(COLOR_ACCENT_CYAN);
            }
        });
        return button;
    }

    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            originalContentArea.setText("INGESTING DOCUMENT: " + selectedFile.getName() + "...");
            originalContentArea.setForeground(COLOR_TEXT_PRIMARY);

            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    return ocrService.extractText(selectedFile);
                }
                @Override
                protected void done() {
                    try {
                        extractedTextContent = get();
                        originalContentArea.setText(extractedTextContent);
                        originalContentArea.setCaretPosition(0);
                        simplifyButton.setEnabled(true);
                        exportButton.setEnabled(false);
                        emailButton.setEnabled(false); // Disable email button until simplified
                        summaryArea.setForeground(COLOR_TEXT_SECONDARY);
                        summaryArea.setText("Ingestion complete. Ready for NLP analysis.");
                        updateTermsPanel("");
                    } catch (Exception e) {
                        extractedTextContent = "";
                        simplifyButton.setEnabled(false);
                        String errorMessage = "INGESTION FAILED: " + e.getCause().getMessage();
                        originalContentArea.setForeground(Color.RED);
                        originalContentArea.setText(errorMessage);
                    }
                }
            };
            worker.execute();
        }
    }

    private void simplifyReport() {
        if (extractedTextContent.isEmpty()) return;

        summaryTextContent = nlpService.summarize(extractedTextContent);
        String definitions = nlpService.defineTerms(extractedTextContent);

        summaryArea.setForeground(COLOR_TEXT_PRIMARY);
        summaryArea.setText(summaryTextContent);
        summaryArea.setCaretPosition(0);
        updateTermsPanel(definitions);
        exportButton.setEnabled(true);
        emailButton.setEnabled(true); // Enable email button now

        // Auto-scroll to the summary panel
        SwingUtilities.invokeLater(() -> {
            summaryCard.scrollRectToVisible(summaryCard.getBounds());
        });
    }
    
    // --- THIS METHOD IS UPDATED ---
    private void sendSummaryViaN8N() {
        String email = emailField.getText();
        String summary = summaryArea.getText();
        String terms = termsArea.getText();
        
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter an email address or phone number in the left panel.", "Email/Phone Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (summary.isEmpty() || summary.startsWith("NLP analysis")) {
            JOptionPane.showMessageDialog(frame, "Please simplify a report before emailing.", "No Summary", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show a loading message
        JOptionPane.showMessageDialog(frame, "Sending summary to " + email + "...", "Sending...", JOptionPane.INFORMATION_MESSAGE);

        // Build the JSON payload
        String jsonPayload = "{\"email\": \"" + escapeJson(email) + "\", " +
                             "\"summary\": \"" + escapeJson(summary) + "\", " +
                             "\"terms\": \"" + escapeJson(terms) + "\"}";

        // Run the network request in a separate thread
        SwingWorker<HttpResponse<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected HttpResponse<String> doInBackground() throws Exception {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(N8N_WEBHOOK_URL)) // Debug code removed
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();
                return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            }

            @Override
            protected void done() {
                try {
                    HttpResponse<String> response = get();
                    if (response.statusCode() == 200) {
                        JOptionPane.showMessageDialog(frame, "Summary sent successfully to " + email, "Email Sent", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Error sending email. n8n workflow returned status: " + response.statusCode(), "Send Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(frame, "Network Error: " + e.getMessage(), "Send Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    // Helper method to escape strings for JSON
    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void exportSummary() {
        if (summaryTextContent.isEmpty()) return;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("AutoDoc_Artifact.txt"));
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write("--- NLP ANALYSIS SUMMARY ---\n\n" + summaryTextContent);
                writer.write("\n\n--- VALIDATED MEDICAL TERMS ---\n\n"); 
                writer.write(termsArea.getText());
                JOptionPane.showMessageDialog(frame, "Artifact exported successfully.", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error exporting artifact: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        // Set properties for a cleaner look on some systems
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        DatabaseManager.initializeDatabase();
        SwingUtilities.invokeLater(MainApp::new);
    }
}

