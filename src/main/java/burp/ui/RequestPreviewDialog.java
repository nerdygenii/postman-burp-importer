package burp.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RequestPreviewDialog extends JDialog {
    private boolean approved = false;
    
    public RequestPreviewDialog(Frame parent, List<RequestPreview> requests) {
        super(parent, "Preview Requests", true);
        initializeUI(requests);
    }
    
    private void initializeUI(List<RequestPreview> requests) {
        setLayout(new BorderLayout());
        
        // Create table
        String[] columnNames = {"Import", "Name", "Method", "URL"};
        Object[][] data = new Object[requests.size()][4];
        
        for (int i = 0; i < requests.size(); i++) {
            RequestPreview req = requests.get(i);
            data[i][0] = true;
            data[i][1] = req.name;
            data[i][2] = req.method;
            data[i][3] = req.url;
        }
        
        JTable table = new JTable(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
        
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(400);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton selectAllBtn = new JButton("Select All");
        selectAllBtn.addActionListener(e -> setAllSelected(table, true));
        
        JButton deselectAllBtn = new JButton("Deselect All");
        deselectAllBtn.addActionListener(e -> setAllSelected(table, false));
        
        JButton importBtn = new JButton("Import Selected");
        importBtn.addActionListener(e -> {
            approved = true;
            updateRequestSelection(table, requests);
            dispose();
        });
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(selectAllBtn);
        buttonPanel.add(deselectAllBtn);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(cancelBtn);
        buttonPanel.add(importBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(getParent());
    }
    
    private void setAllSelected(JTable table, boolean selected) {
        for (int i = 0; i < table.getRowCount(); i++) {
            table.setValueAt(selected, i, 0);
        }
    }
    
    private void updateRequestSelection(JTable table, List<RequestPreview> requests) {
        for (int i = 0; i < table.getRowCount(); i++) {
            requests.get(i).selected = (Boolean) table.getValueAt(i, 0);
        }
    }
    
    public boolean isApproved() {
        return approved;
    }
    
    public static class RequestPreview {
        public String name;
        public String method;
        public String url;
        public boolean selected = true;
        
        public RequestPreview(String name, String method, String url) {
            this.name = name;
            this.method = method;
            this.url = url;
        }
    }
}
