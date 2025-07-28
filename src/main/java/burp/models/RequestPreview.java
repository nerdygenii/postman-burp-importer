package burp.models;

import java.awt.Color;
import java.util.Set;

public class RequestPreview {
    private final String name;
    private final String path;
    private final String method;
    private final String url;
    private final String description;
    private final boolean hasAuth;
    private final boolean hasHeaders;
    private final boolean hasBody;
    private final Set<String> unresolvedVariables; // Added variable information
    private boolean selected;
    
    public RequestPreview(String name, String path, String method, String url, 
                         String description, boolean hasAuth, boolean hasHeaders, boolean hasBody,
                         Set<String> unresolvedVariables) {
        this.name = name;
        this.path = path;
        this.method = method;
        this.url = url;
        this.description = description;
        this.hasAuth = hasAuth;
        this.hasHeaders = hasHeaders;
        this.hasBody = hasBody;
        this.unresolvedVariables = unresolvedVariables;
        this.selected = true; // Default to selected
    }
    
    // Backward compatibility constructor
    public RequestPreview(String name, String path, String method, String url, 
                         String description, boolean hasAuth, boolean hasHeaders, boolean hasBody) {
        this(name, path, method, url, description, hasAuth, hasHeaders, hasBody, java.util.Collections.emptySet());
    }
    
    // Getters
    public String getName() { return name; }
    public String getPath() { return path; }
    public String getMethod() { return method; }
    public String getUrl() { return url; }
    public String getDescription() { return description; }
    public boolean hasAuth() { return hasAuth; }
    public boolean hasHeaders() { return hasHeaders; }
    public boolean hasBody() { return hasBody; }
    public boolean isSelected() { return selected; }
    public Set<String> getUnresolvedVariables() { return unresolvedVariables; }
    public boolean hasUnresolvedVariables() { return !unresolvedVariables.isEmpty(); }
    
    // Setter for selection
    public void setSelected(boolean selected) { this.selected = selected; }
    
    public String getVariableStatus() {
        if (unresolvedVariables.isEmpty()) {
            return "✅ All resolved";
        } else if (unresolvedVariables.size() == 1) {
            return "⚠️ " + unresolvedVariables.iterator().next();
        } else {
            return "❌ " + unresolvedVariables.size() + " variables";
        }
    }
    
    public Color getVariableStatusColor() {
        if (unresolvedVariables.isEmpty()) {
            return new java.awt.Color(0, 120, 0); // Green
        } else if (unresolvedVariables.size() <= 2) {
            return new java.awt.Color(255, 140, 0); // Orange
        } else {
            return java.awt.Color.RED;
        }
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s", method, name, url);
    }
}
