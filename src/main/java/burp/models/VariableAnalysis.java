package burp.models;

import java.util.Set;

public class VariableAnalysis {
    private final Set<String> unresolvedVariables;
    private final int totalRequests;
    private final int requestsWithVariables;
    private final VariableImpact impact;
    
    public enum VariableImpact {
        NONE,       // No variables detected
        LOW,        // <25% of requests affected
        MEDIUM,     // 25-75% of requests affected  
        HIGH        // >75% of requests affected
    }
    
    public VariableAnalysis(Set<String> unresolvedVariables, int totalRequests, int requestsWithVariables) {
        this.unresolvedVariables = unresolvedVariables;
        this.totalRequests = totalRequests;
        this.requestsWithVariables = requestsWithVariables;
        this.impact = calculateImpact();
    }
    
    private VariableImpact calculateImpact() {
        if (unresolvedVariables.isEmpty()) {
            return VariableImpact.NONE;
        }
        
        double percentage = (double) requestsWithVariables / totalRequests;
        if (percentage < 0.25) {
            return VariableImpact.LOW;
        } else if (percentage < 0.75) {
            return VariableImpact.MEDIUM;
        } else {
            return VariableImpact.HIGH;
        }
    }
    
    public Set<String> getUnresolvedVariables() { return unresolvedVariables; }
    public int getTotalRequests() { return totalRequests; }
    public int getRequestsWithVariables() { return requestsWithVariables; }
    public VariableImpact getImpact() { return impact; }
    public boolean hasVariables() { return !unresolvedVariables.isEmpty(); }
    
    public String getImpactDescription() {
        switch (impact) {
            case NONE: return "No variables detected";
            case LOW: return "Few requests affected (" + requestsWithVariables + "/" + totalRequests + ")";
            case MEDIUM: return "Some requests affected (" + requestsWithVariables + "/" + totalRequests + ")";
            case HIGH: return "Most requests affected (" + requestsWithVariables + "/" + totalRequests + ")";
            default: return "Unknown impact";
        }
    }
}
