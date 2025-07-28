package burp.ui;

import burp.models.RequestPreview;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class RequestPreviewTableModel extends AbstractTableModel {
    private final String[] columnNames = {
        "Selected", "Method", "Name", "URL", "Path", "Variables", "Auth", "Headers", "Body"
    };
    
    private final Class<?>[] columnTypes = {
        Boolean.class, String.class, String.class, String.class, 
        String.class, String.class, String.class, String.class, String.class
    };
    
    private final List<RequestPreview> previews;
    private Runnable selectionChangeCallback; // Added callback for selection changes
    
    public RequestPreviewTableModel(List<RequestPreview> previews) {
        this.previews = previews;
    }
    
    public void setSelectionChangeCallback(Runnable callback) {
        this.selectionChangeCallback = callback;
    }
    
    @Override
    public int getRowCount() {
        return previews.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0; // Only the "Selected" column is editable
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        RequestPreview preview = previews.get(rowIndex);
        
        switch (columnIndex) {
            case 0: return preview.isSelected();
            case 1: return preview.getMethod();
            case 2: return preview.getName();
            case 3: return truncateUrl(preview.getUrl(), 50);
            case 4: return preview.getPath();
            case 5: return preview.getVariableStatus();
            case 6: return preview.hasAuth() ? "✓" : "";
            case 7: return preview.hasHeaders() ? "✓" : "";
            case 8: return preview.hasBody() ? "✓" : "";
            default: return "";
        }
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 0 && value instanceof Boolean) {
            previews.get(rowIndex).setSelected((Boolean) value);
            fireTableCellUpdated(rowIndex, columnIndex);
            
            // Notify about selection change
            if (selectionChangeCallback != null) {
                selectionChangeCallback.run();
            }
        }
    }
    
    private String truncateUrl(String url, int maxLength) {
        if (url == null) return "";
        if (url.length() <= maxLength) return url;
        return url.substring(0, maxLength - 3) + "...";
    }
    
    public RequestPreview getPreviewAt(int rowIndex) {
        return previews.get(rowIndex);
    }
}
