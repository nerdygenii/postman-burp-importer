package burp.parser;

import burp.models.*;
import java.util.*;
import java.util.regex.*;

public class VariableResolver {
    private final Map<String, String> variables = new HashMap<>();
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}");
    
    public void addEnvironmentVariables(PostmanEnvironment environment) {
        if (environment.values != null) {
            for (PostmanEnvironment.Value value : environment.values) {
                if (value.enabled && value.value != null) {
                    // Only add if variable doesn't already exist (don't overwrite manual entries)
                    variables.putIfAbsent(value.key, value.value);
                }
            }
        }
    }
    
    public void addCollectionVariables(PostmanCollection collection) {
        if (collection.variable != null) {
            for (PostmanCollection.Variable var : collection.variable) {
                if (var.value != null) {
                    // Only add if variable doesn't already exist (don't overwrite manual entries)
                    variables.putIfAbsent(var.key, var.value);
                }
            }
        }
    }
    
    public void addCustomVariable(String key, String value) {
        // Custom variables should always overwrite existing ones
        variables.put(key, value);
    }
    
    public String resolve(String input) {
        if (input == null) return null;
        
        Matcher matcher = VARIABLE_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            String value = variables.getOrDefault(varName, "{{" + varName + "}}");
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    public Map<String, String> getVariables() {
        return new HashMap<>(variables);
    }
    
    public void clearAllVariables() {
        variables.clear();
    }
}
