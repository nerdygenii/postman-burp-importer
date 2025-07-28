package burp.ui;

import burp.PostmanImporter;
import burp.models.RequestPreview;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class RequestSelectionDialog extends JDialog {
    private final List<RequestPreview> previews;
    private final PostmanImporter importer;
    private final JTable table;
    private final RequestPreviewTableModel tableModel;
    private boolean importConfirmed = false;
    private JLabel statusLabel; // Added to show selection count
    
    public RequestSelectionDialog(List<RequestPreview> previews, PostmanImporter importer, 
                                 Component parent) {
        super(SwingUtilities.getWindowAncestor(parent), "Select Requests to Import", 
              ModalityType.APPLICATION_MODAL);
        this.previews = previews;
        this.importer = importer;
        this.tableModel = new RequestPreviewTableModel(previews);
        this.table = new JTable(tableModel);
        
        // Set up selection change callback
        tableModel.setSelectionChangeCallback(this::updateSelectionCount);
        
        initializeUI();
        setupTable();
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(800, 600);
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Select Requests to Import");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Status panel showing total and selected count
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel totalLabel = new JLabel(String.format("Total: %d requests", previews.size()));
        statusLabel = new JLabel();
        updateSelectionCount();
        
        statusPanel.add(totalLabel);
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(statusLabel);
        
        headerPanel.add(statusPanel, BorderLayout.EAST);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Requests"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupTable() {
        // Allow row selection but don't interfere with checkbox editing
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setRowSelectionAllowed(false); // Disable row selection to avoid conflicts with checkbox
        table.setColumnSelectionAllowed(false);
        
        // Configure column widths
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(70);  // Selected (wider for checkbox)
        columnModel.getColumn(1).setPreferredWidth(80);  // Method
        columnModel.getColumn(2).setPreferredWidth(200); // Name
        columnModel.getColumn(3).setPreferredWidth(250); // URL (reduced to make room for Variables)
        columnModel.getColumn(4).setPreferredWidth(120); // Path
        columnModel.getColumn(5).setPreferredWidth(120); // Variables (new column)
        columnModel.getColumn(6).setPreferredWidth(60);  // Auth
        columnModel.getColumn(7).setPreferredWidth(60);  // Headers
        columnModel.getColumn(8).setPreferredWidth(60);  // Body
        
        // Add custom renderer for method column
        columnModel.getColumn(1).setCellRenderer(new MethodCellRenderer());
        
        // Add custom renderer for variables column
        columnModel.getColumn(5).setCellRenderer(new VariableCellRenderer());
        
        // Configure checkbox column properly
        TableColumn selectColumn = columnModel.getColumn(0);
        selectColumn.setCellRenderer(new CheckboxRenderer());
        selectColumn.setCellEditor(new CheckboxEditor());
        selectColumn.setMaxWidth(70);
        selectColumn.setMinWidth(70);
    }
    
    // Custom checkbox renderer
    private class CheckboxRenderer extends JCheckBox implements TableCellRenderer {
        public CheckboxRenderer() {
            setHorizontalAlignment(JCheckBox.CENTER);
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setSelected(value != null && (Boolean) value);
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            
            return this;
        }
    }
    
    // Custom checkbox editor
    private class CheckboxEditor extends DefaultCellEditor {
        public CheckboxEditor() {
            super(new JCheckBox());
            JCheckBox checkBox = (JCheckBox) getComponent();
            checkBox.setHorizontalAlignment(JCheckBox.CENTER);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            JCheckBox checkBox = (JCheckBox) getComponent();
            checkBox.setSelected(value != null && (Boolean) value);
            return checkBox;
        }
        
        @Override
        public Object getCellEditorValue() {
            return ((JCheckBox) getComponent()).isSelected();
        }
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Selection buttons
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton selectAllBtn = new JButton("Select All");
        selectAllBtn.addActionListener(e -> selectAll(true));
        
        JButton selectNoneBtn = new JButton("Select None");
        selectNoneBtn.addActionListener(e -> selectAll(false));
        
        JButton selectByMethodBtn = new JButton("Select by Method...");
        selectByMethodBtn.addActionListener(e -> selectByMethod());
        
        selectionPanel.add(selectAllBtn);
        selectionPanel.add(selectNoneBtn);
        selectionPanel.add(selectByMethodBtn);
        
        buttonPanel.add(selectionPanel, BorderLayout.WEST);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton previewBtn = new JButton("Preview Selected");
        previewBtn.addActionListener(e -> previewSelected());
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> {
            importConfirmed = false;
            dispose();
        });
        
        JButton importBtn = new JButton("Import Selected");
        importBtn.addActionListener(e -> {
            importConfirmed = true;
            dispose();
        });
        
        actionPanel.add(previewBtn);
        actionPanel.add(cancelBtn);
        actionPanel.add(importBtn);
        
        buttonPanel.add(actionPanel, BorderLayout.EAST);
        
        return buttonPanel;
    }
    
    private void selectAll(boolean selected) {
        for (RequestPreview preview : previews) {
            preview.setSelected(selected);
        }
        tableModel.fireTableDataChanged();
        updateSelectionCount();
    }
    
    private void updateSelectionCount() {
        int selectedCount = 0;
        for (RequestPreview preview : previews) {
            if (preview.isSelected()) {
                selectedCount++;
            }
        }
        
        if (statusLabel != null) {
            statusLabel.setText(String.format("Selected: %d", selectedCount));
            statusLabel.setForeground(selectedCount > 0 ? new Color(0, 120, 0) : Color.GRAY);
        }
    }
    
    private void selectByMethod() {
        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};
        String method = (String) JOptionPane.showInputDialog(
            this,
            "Select requests by HTTP method:",
            "Select by Method",
            JOptionPane.QUESTION_MESSAGE,
            null,
            methods,
            "GET"
        );
        
        if (method != null) {
            for (RequestPreview preview : previews) {
                preview.setSelected(method.equals(preview.getMethod()));
            }
            tableModel.fireTableDataChanged();
            updateSelectionCount();
        }
    }
    
    private void previewSelected() {
        List<RequestPreview> selected = getSelectedRequests();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No requests selected for preview.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        StringBuilder preview = new StringBuilder();
        preview.append(String.format("Selected %d requests for import:\n\n", selected.size()));
        
        for (RequestPreview req : selected) {
            preview.append(String.format("[%s] %s\n", req.getMethod(), req.getName()));
            preview.append(String.format("    URL: %s\n", req.getUrl()));
            preview.append(String.format("    Path: %s\n", req.getPath()));
            if (req.hasAuth()) preview.append("    • Has Authentication\n");
            if (req.hasHeaders()) preview.append("    • Has Custom Headers\n");
            if (req.hasBody()) preview.append("    • Has Request Body\n");
            preview.append("\n");
        }
        
        JTextArea textArea = new JTextArea(preview.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Import Preview", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
    
    public List<RequestPreview> getSelectedRequests() {
        List<RequestPreview> selected = new ArrayList<>();
        for (RequestPreview preview : previews) {
            if (preview.isSelected()) {
                selected.add(preview);
            }
        }
        return selected;
    }
    
    public boolean showDialog() {
        setVisible(true);
        return importConfirmed;
    }
    
    // Custom cell renderer for HTTP methods with colors
    private static class MethodCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String method = (String) value;
            if (!isSelected) {
                switch (method) {
                    case "GET":
                        setForeground(new Color(0, 120, 0));
                        break;
                    case "POST":
                        setForeground(new Color(255, 140, 0));
                        break;
                    case "PUT":
                        setForeground(new Color(0, 0, 200));
                        break;
                    case "DELETE":
                        setForeground(new Color(200, 0, 0));
                        break;
                    default:
                        setForeground(Color.BLACK);
                }
            }
            
            setHorizontalAlignment(CENTER);
            setFont(getFont().deriveFont(Font.BOLD));
            return this;
        }
    }
    
    // Custom cell renderer for variables with colors
    private static class VariableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String variableStatus = (String) value;
            if (!isSelected) {
                if (variableStatus.startsWith("✅")) {
                    setForeground(new Color(0, 120, 0)); // Green
                } else if (variableStatus.startsWith("⚠️")) {
                    setForeground(new Color(255, 140, 0)); // Orange
                } else if (variableStatus.startsWith("❌")) {
                    setForeground(Color.RED);
                } else {
                    setForeground(table.getForeground());
                }
            } else {
                setForeground(table.getSelectionForeground());
            }
            
            return this;
        }
    }
}
