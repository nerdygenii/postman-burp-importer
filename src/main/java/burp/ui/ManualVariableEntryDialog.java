package burp.ui;

import burp.utils.VariableDetector;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ManualVariableEntryDialog extends JDialog {
    private final Set<String> variables;
    private final VariableDetector detector;
    private final Map<String, JTextField> fieldMap = new HashMap<>();
    private boolean confirmed = false;
    
    public ManualVariableEntryDialog(Component parent, Set<String> variables, VariableDetector detector) {
        super(SwingUtilities.getWindowAncestor(parent), "Set Variables Manually", 
              ModalityType.APPLICATION_MODAL);
        this.variables = variables;
        this.detector = detector;
        
        initializeUI();
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, Math.min(600, variables.size() * 50 + 200));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Variables entry panel
        JPanel entryPanel = createEntryPanel();
        JScrollPane scrollPane = new JScrollPane(entryPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("✏️ Set Variable Values");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        
        JLabel descLabel = new JLabel("Enter values for the variables below. Suggested values are provided where possible.");
        descLabel.setFont(descLabel.getFont().deriveFont(12f));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(5), BorderLayout.CENTER);
        panel.add(descLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createEntryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        Map<String, String> suggestions = detector.generateVariableSuggestions(variables);
        
        for (String variable : variables) {
            JPanel fieldPanel = createVariableFieldPanel(variable, suggestions.get(variable));
            panel.add(fieldPanel);
            panel.add(Box.createVerticalStrut(8));
        }
        
        return panel;
    }
    
    private JPanel createVariableFieldPanel(String variable, String suggestion) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        // Variable name label
        JLabel nameLabel = new JLabel("{{" + variable + "}}");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
        nameLabel.setPreferredSize(new Dimension(150, 25));
        
        // Input field
        JTextField valueField = new JTextField();
        valueField.setPreferredSize(new Dimension(200, 25));
        
        // Set suggestion if available
        if (suggestion != null) {
            valueField.setText(suggestion);
            valueField.setForeground(Color.GRAY);
            
            // Clear suggestion when user starts typing
            valueField.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent evt) {
                    if (valueField.getForeground() == Color.GRAY) {
                        valueField.setText("");
                        valueField.setForeground(Color.BLACK);
                    }
                }
            });
        }
        
        fieldMap.put(variable, valueField);
        
        // Description/hint - use manual formatting instead of HTML
        JLabel hintLabel = new JLabel();
        if (suggestion != null) {
            hintLabel.setText("Suggested value (click to edit)");
        } else {
            hintLabel.setText("Enter value for this variable");
        }
        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.ITALIC, 10f));
        hintLabel.setForeground(Color.GRAY);
        
        // Layout
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(nameLabel, BorderLayout.NORTH);
        leftPanel.add(hintLabel, BorderLayout.SOUTH);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(valueField, BorderLayout.CENTER);
        
        // Add category icon based on variable type
        String icon = getVariableIcon(variable);
        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            panel.add(iconLabel, BorderLayout.EAST);
        }
        
        return panel;
    }
    
    private String getVariableIcon(String variable) {
        String lowerVar = variable.toLowerCase();
        
        if (lowerVar.contains("url") || lowerVar.contains("host")) {
            return "🌐";
        }
        if (lowerVar.contains("token") || lowerVar.contains("key") || lowerVar.contains("auth")) {
            return "🔑";
        }
        if (lowerVar.contains("id")) {
            return "🆔";
        }
        if (lowerVar.contains("env")) {
            return "⚙️";
        }
        
        return null;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        
        // Helper buttons
        JPanel helperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton clearAllBtn = new JButton("Clear All");
        clearAllBtn.addActionListener(e -> clearAllFields());
        
        JButton fillSuggestionsBtn = new JButton("Use All Suggestions");
        fillSuggestionsBtn.addActionListener(e -> fillAllSuggestions());
        
        helperPanel.add(clearAllBtn);
        helperPanel.add(fillSuggestionsBtn);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        JButton okBtn = new JButton("Set Variables");
        okBtn.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });
        
        actionPanel.add(cancelBtn);
        actionPanel.add(okBtn);
        
        panel.add(helperPanel, BorderLayout.WEST);
        panel.add(actionPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void clearAllFields() {
        for (JTextField field : fieldMap.values()) {
            field.setText("");
            field.setForeground(Color.BLACK);
        }
    }
    
    private void fillAllSuggestions() {
        Map<String, String> suggestions = detector.generateVariableSuggestions(variables);
        
        for (String variable : variables) {
            String suggestion = suggestions.get(variable);
            if (suggestion != null) {
                JTextField field = fieldMap.get(variable);
                field.setText(suggestion);
                field.setForeground(Color.BLACK);
            }
        }
    }
    
    private boolean validateInput() {
        // Check if at least one field has a value
        boolean hasValue = false;
        for (Map.Entry<String, JTextField> entry : fieldMap.entrySet()) {
            String value = entry.getValue().getText().trim();
            if (!value.isEmpty() && !Color.GRAY.equals(entry.getValue().getForeground())) {
                hasValue = true;
                break;
            }
        }
        
        if (!hasValue) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter at least one variable value.",
                "No Values Entered",
                JOptionPane.WARNING_MESSAGE
            );
            return false;
        }
        
        // Check for common mistakes
        for (Map.Entry<String, JTextField> entry : fieldMap.entrySet()) {
            String variable = entry.getKey();
            String value = entry.getValue().getText().trim();
            
            if (!value.isEmpty() && !Color.GRAY.equals(entry.getValue().getForeground())) {
                // Check if user accidentally included the {{ }} syntax
                if (value.startsWith("{{") && value.endsWith("}}")) {
                    int result = JOptionPane.showConfirmDialog(
                        this,
                        "Variable '" + variable + "' contains {{ }} syntax.\n" +
                        "Did you mean to enter just the value without {{ }}?\n\n" +
                        "Current: " + value + "\n" +
                        "Suggested: " + value.substring(2, value.length() - 2),
                        "Check Variable Value",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    
                    if (result == JOptionPane.YES_OPTION) {
                        entry.getValue().setText(value.substring(2, value.length() - 2));
                    }
                }
            }
        }
        
        return true;
    }
    
    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }
    
    public Map<String, String> getVariables() {
        Map<String, String> result = new HashMap<>();
        
        for (Map.Entry<String, JTextField> entry : fieldMap.entrySet()) {
            String value = entry.getValue().getText().trim();
            
            // Only include non-empty values that aren't suggestions (gray text)
            if (!value.isEmpty() && !Color.GRAY.equals(entry.getValue().getForeground())) {
                result.put(entry.getKey(), value);
            }
        }
        
        return result;
    }
}
