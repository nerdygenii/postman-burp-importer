package burp.utils;

import burp.models.*;
import burp.parser.VariableResolver;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class VariableDetector {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    private final VariableResolver resolver;
    
    public VariableDetector(VariableResolver resolver) {
        this.resolver = resolver;
    }
    
    public VariableAnalysis analyzeCollection(PostmanCollection collection) {
        Set<String> unresolvedVariables = new HashSet<>();
        int totalRequests = 0;
        int requestsWithVariables = 0;
        
        List<RequestItem> requests = flattenRequests(collection.item, "");
        
        for (RequestItem item : requests) {
            totalRequests++;
            Set<String> requestVariables = findVariablesInRequest(item.request);
            
            if (!requestVariables.isEmpty()) {
                requestsWithVariables++;
                
                // Check which variables are unresolved
                for (String variable : requestVariables) {
                    String testValue = "{{" + variable + "}}";
                    String resolved = resolver.resolve(testValue);
                    
                    // If it's still the same, it means the variable wasn't resolved
                    if (testValue.equals(resolved)) {
                        unresolvedVariables.add(variable);
                    }
                }
            }
        }
        
        return new VariableAnalysis(unresolvedVariables, totalRequests, requestsWithVariables);
    }
    
    public Set<String> findVariablesInRequest(PostmanCollection.Request request) {
        Set<String> variables = new HashSet<>();
        
        // Check URL
        String rawUrl = extractRawUrl(request.url);
        if (rawUrl != null) {
            variables.addAll(extractVariables(rawUrl));
        }
        
        // Check headers
        if (request.header != null) {
            for (PostmanCollection.Header header : request.header) {
                if (header.key != null) {
                    variables.addAll(extractVariables(header.key));
                }
                if (header.value != null) {
                    variables.addAll(extractVariables(header.value));
                }
            }
        }
        
        // Check body
        if (request.body != null && request.body.raw != null) {
            variables.addAll(extractVariables(request.body.raw));
        }
        
        // Check auth
        if (request.auth != null) {
            variables.addAll(findVariablesInAuth(request.auth));
        }
        
        return variables;
    }
    
    public Set<String> findVariablesInAuth(PostmanCollection.Auth auth) {
        Set<String> variables = new HashSet<>();
        
        if (auth.bearer != null) {
            variables.addAll(extractVariablesFromAuthData(auth.bearer));
        }
        if (auth.basic != null) {
            variables.addAll(extractVariablesFromAuthData(auth.basic));
        }
        if (auth.apikey != null) {
            variables.addAll(extractVariablesFromAuthData(auth.apikey));
        }
        
        return variables;
    }
    
    private Set<String> extractVariablesFromAuthData(Object authData) {
        Set<String> variables = new HashSet<>();
        
        if (authData instanceof String) {
            variables.addAll(extractVariables((String) authData));
        } else if (authData instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) authData;
            for (Object value : map.values()) {
                if (value != null) {
                    variables.addAll(extractVariables(value.toString()));
                }
            }
        }
        
        return variables;
    }
    
    private Set<String> extractVariables(String text) {
        Set<String> variables = new HashSet<>();
        if (text == null) return variables;
        
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        
        return variables;
    }
    
    private String extractRawUrl(Object urlData) {
        if (urlData == null) return null;
        
        if (urlData instanceof String) {
            return (String) urlData;
        }
        
        // Handle URL object format - simplified version
        try {
            if (urlData.toString().contains("raw")) {
                // Extract raw URL from object representation
                String objStr = urlData.toString();
                int rawIndex = objStr.indexOf("raw=");
                if (rawIndex != -1) {
                    int start = rawIndex + 4;
                    int end = objStr.indexOf(",", start);
                    if (end == -1) end = objStr.indexOf("}", start);
                    if (end != -1) {
                        return objStr.substring(start, end).trim();
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to string representation
        }
        
        return urlData.toString();
    }
    
    private List<RequestItem> flattenRequests(List<PostmanCollection.Item> items, String path) {
        List<RequestItem> requests = new ArrayList<>();
        
        // Add null check to prevent NullPointerException
        if (items == null) {
            return requests;
        }
        
        for (PostmanCollection.Item item : items) {
            String currentPath = path.isEmpty() ? item.name : path + "/" + item.name;
            
            if (item.request != null) {
                requests.add(new RequestItem(item.name, currentPath, item.request));
            }
            
            if (item.item != null && !item.item.isEmpty()) {
                requests.addAll(flattenRequests(item.item, currentPath));
            }
        }
        
        return requests;
    }
    
    // Helper class for flattened requests
    private static class RequestItem {
        final String name;
        final String path;
        final PostmanCollection.Request request;
        
        RequestItem(String name, String path, PostmanCollection.Request request) {
            this.name = name;
            this.path = path;
            this.request = request;
        }
    }
    
    public Map<String, String> generateVariableSuggestions(Set<String> variables) {
        Map<String, String> suggestions = new HashMap<>();
        
        for (String variable : variables) {
            String suggestion = suggestValueForVariable(variable);
            if (suggestion != null) {
                suggestions.put(variable, suggestion);
            }
        }
        
        return suggestions;
    }
    
    private String suggestValueForVariable(String variable) {
        String lowerVar = variable.toLowerCase();
        
        // URL patterns
        if (lowerVar.contains("url") || lowerVar.contains("host") || lowerVar.contains("domain")) {
            return "https://api.example.com";
        }
        if (lowerVar.contains("base") && lowerVar.contains("url")) {
            return "https://api.example.com";
        }
        
        // Auth patterns  
        if (lowerVar.contains("token") || lowerVar.contains("access")) {
            return "your_access_token_here";
        }
        if (lowerVar.contains("key") && lowerVar.contains("api")) {
            return "your_api_key_here";
        }
        if (lowerVar.contains("bearer")) {
            return "your_bearer_token";
        }
        
        // ID patterns
        if (lowerVar.contains("id")) {
            return "12345";
        }
        
        // Environment patterns
        if (lowerVar.contains("env")) {
            return "production";
        }
        
        // Timeout patterns
        if (lowerVar.contains("timeout")) {
            return "5000";
        }
        
        return null; // No suggestion available
    }
}
