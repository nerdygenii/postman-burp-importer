package burp.utils;

import burp.models.*;
import burp.parser.VariableResolver;
import burp.api.montoya.MontoyaApi;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class VariableDetector {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    private final VariableResolver resolver;
    private final MontoyaApi api;
    
    public VariableDetector(VariableResolver resolver) {
        this.resolver = resolver;
        this.api = null; // For backward compatibility
    }
    
    public VariableDetector(VariableResolver resolver, MontoyaApi api) {
        this.resolver = resolver;
        this.api = api;
    }
    
    public VariableAnalysis analyzeCollection(PostmanCollection collection) {
        Set<String> unresolvedVariables = new HashSet<>();
        int totalRequests = 0;
        int requestsWithVariables = 0;
        
        if (api != null) {
            api.logging().logToOutput("🔍🔍🔍 NEW VARIABLE DETECTOR v1.0.4 IS RUNNING! 🔍🔍🔍");
            api.logging().logToOutput("DEBUG VariableDetector: Starting collection analysis");
        }
        
        List<RequestItem> requests = flattenRequests(collection.item, "");
        
        for (RequestItem item : requests) {
            totalRequests++;
            Set<String> requestVariables = findVariablesInRequest(item.request);
            
            if (api != null) {
                api.logging().logToOutput("DEBUG VariableDetector: Request '" + item.name + "' found variables=" + requestVariables);
            }
            
            if (!requestVariables.isEmpty()) {
                requestsWithVariables++;
                
                // Check which variables are unresolved
                for (String variable : requestVariables) {
                    String testValue = "{{" + variable + "}}";
                    String resolved = resolver.resolve(testValue);
                    
                    if (api != null) {
                        api.logging().logToOutput("DEBUG VariableDetector: Variable '" + variable + "' testValue='" + testValue + "' resolved='" + resolved + "'");
                    }
                    
                    // Variable is unresolved if:
                    // 1. Still contains the original variable syntax
                    // 2. Resolves to empty string or whitespace (indicates missing variable)
                    boolean isUnresolved = testValue.equals(resolved) || 
                                           resolved == null || 
                                           resolved.trim().isEmpty();
                    
                    if (isUnresolved) {
                        unresolvedVariables.add(variable);
                        if (api != null) {
                            api.logging().logToOutput("DEBUG VariableDetector: Variable '" + variable + "' is UNRESOLVED (resolved to: '" + resolved + "')");
                        }
                    } else {
                        if (api != null) {
                            api.logging().logToOutput("DEBUG VariableDetector: Variable '" + variable + "' WAS RESOLVED to: '" + resolved + "'");
                        }
                    }
                }
            }
        }
        
        if (api != null) {
            api.logging().logToOutput("DEBUG VariableDetector: Analysis complete - " + 
                "totalRequests=" + totalRequests + 
                ", requestsWithVariables=" + requestsWithVariables + 
                ", unresolvedVariables=" + unresolvedVariables);
        }
        
        return new VariableAnalysis(unresolvedVariables, totalRequests, requestsWithVariables);
    }
    
    public Set<String> findVariablesInRequest(PostmanCollection.Request request) {
        Set<String> variables = new HashSet<>();
        
        // Check URL
        String rawUrl = extractRawUrl(request.url);
        if (rawUrl != null) {
            Set<String> urlVars = extractVariables(rawUrl);
            variables.addAll(urlVars);
            if (api != null) {
                api.logging().logToOutput("DEBUG VariableDetector: URL variables=" + urlVars + " from rawUrl=" + rawUrl);
            }
        } else {
            if (api != null) {
                api.logging().logToOutput("DEBUG VariableDetector: Could not extract raw URL from=" + request.url);
            }
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
        if (request.body != null) {
            if (request.body.raw != null) {
                Set<String> bodyVars = extractVariables(request.body.raw);
                variables.addAll(bodyVars);
                if (api != null && !bodyVars.isEmpty()) {
                    api.logging().logToOutput("DEBUG VariableDetector: Raw body variables=" + bodyVars);
                }
            }
            
            // Check GraphQL body mode
            if ("graphql".equals(request.body.mode) && request.body.graphql != null) {
                Set<String> graphqlVars = findVariablesInGraphQL(request.body.graphql);
                variables.addAll(graphqlVars);
                if (api != null) {
                    api.logging().logToOutput("DEBUG VariableDetector: GraphQL body detected - found variables=" + graphqlVars);
                }
            }
        }
        
        // Check auth
        if (request.auth != null) {
            variables.addAll(findVariablesInAuth(request.auth));
        }
        
        if (api != null) {
            api.logging().logToOutput("DEBUG VariableDetector: Total variables found in request=" + variables);
        }
        return variables;
    }
    
    public Set<String> findVariablesInGraphQL(PostmanCollection.GraphQL graphql) {
        Set<String> variables = new HashSet<>();
        
        // Check GraphQL query for variables
        if (graphql.query != null) {
            Set<String> queryVars = extractVariables(graphql.query);
            variables.addAll(queryVars);
            if (api != null) {
                api.logging().logToOutput("DEBUG VariableDetector: GraphQL query variables=" + queryVars);
            }
        }
        
        // Check GraphQL variables JSON for Postman variables
        if (graphql.variables != null) {
            Set<String> variableVars = extractVariables(graphql.variables);
            variables.addAll(variableVars);
            if (api != null) {
                api.logging().logToOutput("DEBUG VariableDetector: GraphQL variables field variables=" + variableVars);
            }
        }
        
        if (api != null) {
            api.logging().logToOutput("DEBUG VariableDetector: Total GraphQL variables found=" + variables);
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
        
        // Handle string URL format
        if (urlData instanceof String) {
            return (String) urlData;
        }
        
        // Handle Url object format - use proper JSON parsing like PostmanImporter
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.JsonElement element = gson.toJsonTree(urlData);
            if (element.isJsonObject()) {
                com.google.gson.JsonObject urlObject = element.getAsJsonObject();
                if (urlObject.has("raw")) {
                    String rawUrl = urlObject.get("raw").getAsString();
                    if (api != null) {
                        api.logging().logToOutput("DEBUG VariableDetector: Successfully extracted raw URL=" + rawUrl);
                    }
                    return rawUrl;
                }
            }
        } catch (Exception e) {
            if (api != null) {
                api.logging().logToOutput("DEBUG VariableDetector: Failed to parse URL object, error=" + e.getMessage());
            }
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
