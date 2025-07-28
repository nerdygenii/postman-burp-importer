package burp.ui;

import burp.PostmanImporter;
import burp.models.ImportResult;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class ImporterPanel {
    private final PostmanImporter importer;
    private final JPanel mainPanel;
    private JTextArea logArea;  // Removed final
    private JProgressBar progressBar;  // Removed final
    private JButton importButton;  // Removed final
    private JButton previewButton;  // Added preview button field
    private JButton retryButton;   // Added retry button field
    private JButton cancelButton;  // Removed final
    private JTextField collectionField;  // Removed final
    private JTextField environmentField;  // Removed final
    private ButtonGroup destinationGroup;  // Added for destination selection
    private JRadioButton repeaterOption;
    private JRadioButton sitemapOption;
    private JRadioButton bothOption;
    private JSpinner delaySpinner;  // Added for rate limiting configuration
    private File selectedCollection;
    private File selectedEnvironment;
    
    public ImporterPanel(PostmanImporter importer) {
        this.importer = importer;
        this.mainPanel = createUI();
    }
    
    private JPanel createUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header with proper spacing
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("Postman Collection Importer");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel helpLabel = new JLabel("Import Postman collections to Repeater and/or Sitemap");
        helpLabel.setFont(helpLabel.getFont().deriveFont(Font.ITALIC, 12f));
        helpLabel.setForeground(Color.GRAY);
        helpLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5)); // Add 5px spacing
        headerPanel.add(helpLabel);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        
        // File selection panel
        JPanel filePanel = new JPanel(new GridBagLayout());
        filePanel.setBorder(BorderFactory.createTitledBorder("Select Files"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Collection file
        gbc.gridx = 0; gbc.gridy = 0;
        filePanel.add(new JLabel("Collection:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        collectionField = new JTextField();
        collectionField.setEditable(false);
        filePanel.add(collectionField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        JButton browseCollectionBtn = new JButton("Browse...");
        browseCollectionBtn.addActionListener(e -> selectCollectionFile());
        filePanel.add(browseCollectionBtn, gbc);
        
        // Environment file
        gbc.gridx = 0; gbc.gridy = 1;
        filePanel.add(new JLabel("Environment (optional):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        environmentField = new JTextField();
        environmentField.setEditable(false);
        filePanel.add(environmentField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        JButton browseEnvBtn = new JButton("Browse...");
        browseEnvBtn.addActionListener(e -> selectEnvironmentFile());
        filePanel.add(browseEnvBtn, gbc);
        
        // Destination selection
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        filePanel.add(Box.createVerticalStrut(10), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        filePanel.add(new JLabel("Send requests to:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel destinationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        
        destinationGroup = new ButtonGroup();
        repeaterOption = new JRadioButton("Repeater", true);
        sitemapOption = new JRadioButton("Sitemap (Live Requests)");
        bothOption = new JRadioButton("Both");
        
        destinationGroup.add(repeaterOption);
        destinationGroup.add(sitemapOption);
        destinationGroup.add(bothOption);
        
        destinationPanel.add(repeaterOption);
        destinationPanel.add(Box.createHorizontalStrut(15));
        destinationPanel.add(sitemapOption);
        destinationPanel.add(Box.createHorizontalStrut(15));
        destinationPanel.add(bothOption);
        
        filePanel.add(destinationPanel, gbc);
        
        // Rate limiting configuration
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        filePanel.add(new JLabel("Rate limit delay (ms):"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel delayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        delaySpinner = new JSpinner(new SpinnerNumberModel(200, 0, 5000, 50));
        delaySpinner.setPreferredSize(new Dimension(80, 25));
        delaySpinner.setToolTipText("Delay between requests in milliseconds (0-5000ms, default: 200ms)");
        delayPanel.add(delaySpinner);
        delayPanel.add(Box.createHorizontalStrut(5));
        delayPanel.add(new JLabel("(0 = no delay, default: 200ms)"));
        filePanel.add(delayPanel, gbc);
        
        contentPanel.add(filePanel, BorderLayout.NORTH);
        
        // Log area
        logArea = new JTextArea(15, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Import Log"));
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Progress panel
        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        previewButton = new JButton("Preview Requests");
        previewButton.addActionListener(e -> startPreview());
        previewButton.setEnabled(false);
        
        importButton = new JButton("Import Collection");
        importButton.addActionListener(e -> startImport());
        importButton.setEnabled(false);
        
        retryButton = new JButton("Retry Failed Requests");
        retryButton.addActionListener(e -> startRetry());
        retryButton.setEnabled(false);
        retryButton.setToolTipText("Retry requests that failed during the last import");
        
        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);
        
        JButton clearButton = new JButton("Clear Log");
        clearButton.addActionListener(e -> logArea.setText(""));
        
        buttonPanel.add(clearButton);
        buttonPanel.add(previewButton);
        buttonPanel.add(retryButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(importButton);
        
        progressPanel.add(buttonPanel, BorderLayout.EAST);
        contentPanel.add(progressPanel, BorderLayout.SOUTH);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        // Add footer with author information
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel authorLabel = new JLabel("Made by Abdulrahman Oyekunle");
        authorLabel.setFont(authorLabel.getFont().deriveFont(Font.ITALIC, 10f));
        authorLabel.setForeground(Color.GRAY);
        authorLabel.setHorizontalAlignment(SwingConstants.LEFT);
        
        JLabel githubLabel = new JLabel("github.com/nerdygenii");
        githubLabel.setFont(githubLabel.getFont().deriveFont(Font.ITALIC, 10f));
        githubLabel.setForeground(Color.GRAY);
        githubLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        footerPanel.add(authorLabel, BorderLayout.WEST);
        footerPanel.add(githubLabel, BorderLayout.EAST);
        
        panel.add(footerPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void selectCollectionFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
        chooser.setDialogTitle("Select Postman Collection");
        
        if (chooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            selectedCollection = chooser.getSelectedFile();
            collectionField.setText(selectedCollection.getName());
            previewButton.setEnabled(true);
            importButton.setEnabled(true);
        }
    }
    
    private void selectEnvironmentFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
        chooser.setDialogTitle("Select Postman Environment (Optional)");
        
        if (chooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            selectedEnvironment = chooser.getSelectedFile();
            environmentField.setText(selectedEnvironment.getName());
        }
    }
    
    private void startImport() {
        if (selectedCollection != null) {
            logArea.setText("");
            String destination = getSelectedDestination();
            importer.importCollection(selectedCollection, selectedEnvironment, destination);
        }
    }
    
    private void startPreview() {
        if (selectedCollection != null) {
            logArea.setText("");
            importer.showPreview(selectedCollection, selectedEnvironment);
        }
    }
    
    private void startRetry() {
        logArea.setText("");
        appendLog("Retrying failed requests...");
        String destination = getSelectedDestination();
        importer.retryFailedRequests(destination);
    }
    
    public String getSelectedDestination() {
        if (repeaterOption.isSelected()) {
            return "repeater";
        } else if (sitemapOption.isSelected()) {
            return "sitemap";
        } else if (bothOption.isSelected()) {
            return "both";
        }
        return "repeater"; // Default fallback
    }
    
    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public void updateProgress(int value) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(value));
    }
    
    public void setImportInProgress() {
        SwingUtilities.invokeLater(() -> {
            previewButton.setEnabled(false);
            importButton.setEnabled(false);
            retryButton.setEnabled(false);
            cancelButton.setEnabled(true);
            progressBar.setValue(0);
        });
    }
    
    public void setImportComplete() {
        SwingUtilities.invokeLater(() -> {
            previewButton.setEnabled(selectedCollection != null);
            importButton.setEnabled(selectedCollection != null);
            // retryButton will be enabled by showImportSummary if there are failed requests
            cancelButton.setEnabled(false);
            progressBar.setValue(100);
        });
    }
    
    public void showImportSummary(ImportResult result) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder summary = new StringBuilder();
            summary.append("\n========== IMPORT SUMMARY ==========\n");
            summary.append("Collection: ").append(result.collectionName).append("\n");
            summary.append("Total Requests: ").append(result.totalRequests).append("\n");
            summary.append("Successfully Imported: ").append(result.successCount).append("\n");
            summary.append("Failed: ").append(result.failedRequests.size()).append("\n");
            
            if (!result.failedRequests.isEmpty()) {
                summary.append("\nFailed Requests:\n");
                for (String failure : result.failedRequests) {
                    summary.append("  - ").append(failure).append("\n");
                }
                
                // Enable retry button if there are failed requests
                retryButton.setEnabled(true);
                summary.append("\n💡 TIP: Use 'Retry Failed Requests' button to retry failed requests after fixing network issues.\n");
            } else {
                retryButton.setEnabled(false);
            }
            
            if (result.error != null) {
                summary.append("\nError: ").append(result.error).append("\n");
            }
            
            summary.append("====================================\n");
            
            appendLog(summary.toString());
            
            // Show dialog
            String message = String.format(
                "Import completed!\n\n" +
                "Successfully imported: %d/%d requests\n" +
                "Failed: %d requests",
                result.successCount, result.totalRequests, result.failedRequests.size()
            );
            
            if (!result.failedRequests.isEmpty()) {
                message += "\n\n💡 You can retry failed requests using the 'Retry Failed Requests' button.";
            }
            
            JOptionPane.showMessageDialog(
                mainPanel,
                message,
                "Import Complete",
                result.failedRequests.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
            );
        });
    }
    
    public void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(mainPanel, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public JPanel getPanel() {
        return mainPanel;
    }
    
    public int getDelayMs() {
        return (Integer) delaySpinner.getValue();
    }
}