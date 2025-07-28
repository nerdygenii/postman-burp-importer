package burp.ui;

import burp.models.VariableAnalysis;
import burp.utils.VariableDetector;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class VariableResolutionDialog extends JDialog {
    private final VariableAnalysis analysis;
    private final VariableDetector detector;
    private final Component parent;
    
    public enum ResolutionChoice {
        UPLOAD_ENVIRONMENT,
        MANUAL_ENTRY, 
        IGNORE_CONTINUE,
        SKIP_VARIABLE_REQUESTS,
        CANCEL
    }
    
    private ResolutionChoice choice = ResolutionChoice.CANCEL;
    private File selectedEnvironmentFile;
    private Map<String, String> manualVariables = new HashMap<>();
    
    public VariableResolutionDialog(Component parent, VariableAnalysis analysis, VariableDetector detector) {
        super(SwingUtilities.getWindowAncestor(parent), "Unresolved Variables Detected", 
              ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.analysis = analysis;
        this.detector = detector;
        
        initializeUI();
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 500);
        
        // Header with warning icon and message
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Variable analysis panel
        JPanel analysisPanel = createAnalysisPanel();
        add(analysisPanel, BorderLayout.CENTER);
        
        // Options panel
        JPanel optionsPanel = createOptionsPanel();
        add(optionsPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        
        // Icon and title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(iconLabel.getFont().deriveFont(24f));
        
        JLabel titleLabel = new JLabel("Unresolved Variables Detected");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        
        titlePanel.add(iconLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(titleLabel);
        
        panel.add(titlePanel, BorderLayout.WEST);
        
        // Impact indicator
        JLabel impactLabel = new JLabel(analysis.getImpactDescription());
        impactLabel.setForeground(getImpactColor());
        panel.add(impactLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private Color getImpactColor() {
        switch (analysis.getImpact()) {
            case LOW: return new Color(255, 140, 0); // Orange
            case MEDIUM: return new Color(255, 69, 0); // Red-Orange  
            case HIGH: return Color.RED;
            default: return Color.GRAY;
        }
    }
    
    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        
        // Description - use manual formatting instead of HTML
        JLabel descLabel = new JLabel("Your Postman collection contains variables that need to be resolved. Choose how you'd like to handle them:");
        descLabel.setFont(descLabel.getFont().deriveFont(12f));
        panel.add(descLabel, BorderLayout.NORTH);
        
        // Variables list
        JPanel variablesPanel = new JPanel(new BorderLayout());
        variablesPanel.setBorder(BorderFactory.createTitledBorder("Unresolved Variables"));
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        Map<String, String> suggestions = detector.generateVariableSuggestions(analysis.getUnresolvedVariables());
        
        for (String variable : analysis.getUnresolvedVariables()) {
            String suggestion = suggestions.get(variable);
            String display = suggestion != null ? 
                "{{" + variable + "}} → suggested: " + suggestion :
                "{{" + variable + "}}";
            listModel.addElement(display);
        }
        
        JList<String> variablesList = new JList<>(listModel);
        variablesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        variablesList.setVisibleRowCount(6);
        
        JScrollPane scrollPane = new JScrollPane(variablesList);
        variablesPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(variablesPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        
        // Options buttons
        JPanel optionsGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        
        // Option 1: Upload Environment File
        JButton uploadBtn = createOptionButton(
            "📁 Upload Environment File",
            "Browse for your Postman environment file (.json)",
            "Recommended for production use",
            () -> handleUploadEnvironment()
        );
        
        // Option 2: Manual Entry
        JButton manualBtn = createOptionButton(
            "✏️ Set Variables Manually", 
            "Enter variable values manually",
            "Perfect for quick testing or demo",
            () -> handleManualEntry()
        );
        
        // Option 3: Ignore and Continue
        JButton ignoreBtn = createOptionButton(
            "⚠️ Ignore and Continue",
            "Continue with unresolved variables",
            "Requests will likely fail",
            () -> handleIgnoreAndContinue()
        );
        
        // Option 4: Skip Variable Requests
        JButton skipBtn = createOptionButton(
            "🎯 Skip Variable Requests",
            "Import only requests without variables",
            "Import " + (analysis.getTotalRequests() - analysis.getRequestsWithVariables()) + " variable-free requests",
            () -> handleSkipVariableRequests()
        );
        
        optionsGrid.add(uploadBtn);
        optionsGrid.add(manualBtn);
        optionsGrid.add(ignoreBtn);
        optionsGrid.add(skipBtn);
        
        panel.add(optionsGrid, BorderLayout.CENTER);
        
        // Cancel button
        JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> {
            choice = ResolutionChoice.CANCEL;
            dispose();
        });
        cancelPanel.add(cancelBtn);
        
        panel.add(cancelPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JButton createOptionButton(String title, String description, String detail, Runnable action) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setPreferredSize(new Dimension(250, 80));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));
        
        // Description - use manual formatting instead of HTML 
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(descLabel.getFont().deriveFont(10f));
        
        // Detail - use manual formatting instead of HTML
        JLabel detailLabel = new JLabel(detail);
        detailLabel.setFont(detailLabel.getFont().deriveFont(Font.ITALIC, 9f));
        detailLabel.setForeground(Color.GRAY);
        
        JPanel contentPanel = new JPanel(new BorderLayout(5, 2));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(descLabel, BorderLayout.CENTER);
        contentPanel.add(detailLabel, BorderLayout.SOUTH);
        
        button.add(contentPanel, BorderLayout.CENTER);
        button.addActionListener(e -> action.run());
        
        return button;
    }
    
    private void handleUploadEnvironment() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
        chooser.setDialogTitle("Select Postman Environment File");
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedEnvironmentFile = chooser.getSelectedFile();
            choice = ResolutionChoice.UPLOAD_ENVIRONMENT;
            dispose();
        }
    }
    
    private void handleManualEntry() {
        ManualVariableEntryDialog entryDialog = new ManualVariableEntryDialog(
            this, analysis.getUnresolvedVariables(), detector);
        
        if (entryDialog.showDialog()) {
            manualVariables = entryDialog.getVariables();
            choice = ResolutionChoice.MANUAL_ENTRY;
            dispose();
        }
    }
    
    private void handleIgnoreAndContinue() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "⚠️ Warning: Continuing with unresolved variables will likely cause import failures.\n\n" +
            "URLs like 'https://{{api_base_url}}/users' will remain unresolved.\n" +
            "Are you sure you want to continue?",
            "Confirm: Ignore Variables",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            choice = ResolutionChoice.IGNORE_CONTINUE;
            dispose();
        }
    }
    
    private void handleSkipVariableRequests() {
        int variableFreeRequests = analysis.getTotalRequests() - analysis.getRequestsWithVariables();
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "This will import only requests without unresolved variables.\n\n" +
            "Requests to import: " + variableFreeRequests + "/" + analysis.getTotalRequests() + "\n" +
            "Requests to skip: " + analysis.getRequestsWithVariables() + "\n\n" +
            "Continue with selective import?",
            "Confirm: Skip Variable Requests",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            choice = ResolutionChoice.SKIP_VARIABLE_REQUESTS;
            dispose();
        }
    }
    
    public boolean showDialog() {
        setVisible(true);
        return choice != ResolutionChoice.CANCEL;
    }
    
    // Getters
    public ResolutionChoice getChoice() { return choice; }
    public File getSelectedEnvironmentFile() { return selectedEnvironmentFile; }
    public Map<String, String> getManualVariables() { return manualVariables; }
}
