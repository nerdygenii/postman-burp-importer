package burp.utils;

import burp.models.*;
import burp.parser.*;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import com.google.gson.*;
import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;

public class RequestBuilder {
    private final MontoyaApi api;
    private final VariableResolver resolver;
    private final boolean debugMode = true; // Set to true to enable debug logging for GraphQL debugging
    
    public RequestBuilder(MontoyaApi api, VariableResolver resolver) {
        this.api = api;
        this.resolver = resolver;
    }
    
    public byte[] buildRequest(PostmanCollection.Request request) throws Exception {
        List<String> headers = new ArrayList<>();
        
        // Resolve URL once to ensure consistency between host and path parsing
        String resolvedUrl = getResolvedUrl(request.url);
        
        // Build request line
        String method = request.method != null ? request.method : "GET";
        String path = buildPath(request.url, resolvedUrl);
        headers.add(method + " " + path + " HTTP/1.1");
        
        // Add host header
        String host = buildHost(request.url, resolvedUrl);
        headers.add("Host: " + host);
        if (debugMode) {
            System.out.println("DEBUG: Auto-generated Host header: " + host);
        }
        
        // Add custom headers (but skip Host header since we build it automatically)
        if (request.header != null) {
            for (PostmanCollection.Header header : request.header) {
                if (!header.disabled && header.key != null && header.value != null) {
                    String key = resolver.resolve(header.key);
                    String value = resolver.resolve(header.value);
                    
                    if (debugMode) {
                        api.logging().logToOutput("DEBUG: Processing custom header: " + key + ": " + value);
                    }
                    
                    // Skip Host header - we build it automatically from the URL
                    if (!"Host".equalsIgnoreCase(key)) {
                        headers.add(key + ": " + value);
                        if (debugMode) {
                            api.logging().logToOutput("DEBUG: Added custom header: " + key + ": " + value);
                        }
                    } else {
                        if (debugMode) {
                            api.logging().logToOutput("DEBUG: Skipped Host header: " + key + ": " + value);
                        }
                    }
                }
            }
        }
        
        // Handle authentication
        applyAuthentication(headers, request.auth);
        
        // Build body
        byte[] body = buildBody(request.body, headers);
        
        // Build HTTP message using Montoya API
        String httpMessage = String.join("\r\n", headers) + "\r\n\r\n" + new String(body, StandardCharsets.UTF_8);
        return httpMessage.getBytes(StandardCharsets.UTF_8);
    }
    
    private String getResolvedUrl(Object urlData) {
        if (urlData == null) return null;
        
        // Handle string URL format
        if (urlData instanceof String) {
            return resolver.resolve((String) urlData);
        }
        
        // Handle Url object format - extract raw URL and resolve it
        PostmanCollection.Url url = parseUrlObject(urlData);
        if (url != null && url.raw != null) {
            return resolver.resolve(url.raw);
        }
        
        return null;
    }
    
    private String buildPath(Object urlData, String resolvedUrl) throws UnsupportedEncodingException {
        if (urlData == null) return "/";
        
        // If we have a resolved URL, use it directly for path extraction
        if (resolvedUrl != null) {
            return extractPathFromUrl(resolvedUrl);
        }
        
        // Handle string URL format (fallback)
        if (urlData instanceof String) {
            String urlString = resolver.resolve((String) urlData);
            return extractPathFromUrl(urlString);
        }
        
        // Handle Url object format
        PostmanCollection.Url url = parseUrlObject(urlData);
        if (url == null) return "/";
        
        StringBuilder path = new StringBuilder();
        
        // Build path from segments
        if (url.path != null && !url.path.isEmpty()) {
            path.append("/");
            List<String> resolvedPaths = new ArrayList<>();
            for (String segment : url.path) {
                resolvedPaths.add(resolver.resolve(segment));
            }
            path.append(String.join("/", resolvedPaths));
        } else if (url.raw != null) {
            // Extract path from raw URL
            String resolved = resolver.resolve(url.raw);
            return extractPathFromUrl(resolved);
        } else {
            path.append("/");
        }
        
        // Add query parameters
        if (url.query != null && !url.query.isEmpty()) {
            List<String> queryParts = new ArrayList<>();
            for (PostmanCollection.Query query : url.query) {
                if (!query.disabled && query.key != null) {
                    String key = URLEncoder.encode(resolver.resolve(query.key), "UTF-8");
                    String value = query.value != null ? 
                        URLEncoder.encode(resolver.resolve(query.value), "UTF-8") : "";
                    queryParts.add(key + "=" + value);
                }
            }
            if (!queryParts.isEmpty()) {
                // Check if path already has query string
                String pathStr = path.toString();
                if (pathStr.contains("?")) {
                    path.append("&");
                } else {
                    path.append("?");
                }
                path.append(String.join("&", queryParts));
            }
        }
        
        return path.toString();
    }
    
    private String extractPathFromUrl(String urlString) {
        if (urlString == null || urlString.isEmpty()) return "/";
        
        try {
            int protocolEnd = urlString.indexOf("://");
            if (protocolEnd != -1) {
                int pathStart = urlString.indexOf('/', protocolEnd + 3);
                if (pathStart != -1) {
                    return urlString.substring(pathStart);
                } else {
                    return "/";
                }
            } else {
                // No protocol, assume it's just a path
                return urlString.startsWith("/") ? urlString : "/" + urlString;
            }
        } catch (Exception e) {
            return "/";
        }
    }
    
    private String buildHost(Object urlData, String resolvedUrl) {
        if (urlData == null) return "localhost";
        
        // Temporarily disable noisy debug messages to avoid buffer overflow
        // if (debugMode) {
        //     api.logging().logToOutput("DEBUG buildHost: urlData=" + urlData);
        //     api.logging().logToOutput("DEBUG buildHost: resolvedUrl=" + resolvedUrl);
        // }
        
        // Check if resolved URL still contains unresolved variables OR is empty
        boolean hasUnresolvedVariables = (resolvedUrl != null && resolvedUrl.matches(".*\\{\\{[^}]+\\}\\}.*")) || 
                                        (resolvedUrl != null && resolvedUrl.trim().isEmpty());
        
        // Temporarily disable noisy debug messages
        // if (debugMode) {
        //     api.logging().logToOutput("DEBUG buildHost: hasUnresolvedVariables=" + hasUnresolvedVariables);
        // }
        
        // If we have a fully resolved URL (no variables and not empty), use it for host extraction
        if (resolvedUrl != null && !hasUnresolvedVariables && !resolvedUrl.trim().isEmpty()) {
            HttpUtils.HostInfo hostInfo = HttpUtils.parseUrl(resolvedUrl);
            return buildHostWithPort(hostInfo.host, hostInfo.port, hostInfo.useHttps);
        }
        
        // Handle Url object format FIRST (for GraphQL cases)
        PostmanCollection.Url url = parseUrlObject(urlData);
        if (url != null) {
            // Temporarily disable noisy debug messages
            // if (debugMode) {
            //     api.logging().logToOutput("DEBUG buildHost: Parsed URL object host=" + url.host);
            //     api.logging().logToOutput("DEBUG buildHost: Parsed URL object raw=" + url.raw);
            // }
            
            if (url.host != null && !url.host.isEmpty()) {
                // For GraphQL endpoints like ["{{GRAPHQL_ENDPOINT}}"], preserve variable format
                String host = String.join(".", url.host);
                if (url.port != null && !url.port.isEmpty()) {
                    host += ":" + url.port;
                }
                if (debugMode) {
                    api.logging().logToOutput("DEBUG buildHost: Final host from object=" + host);
                }
                // Don't resolve variables here - preserve them as-is for unresolved variables
                return host;
            } else if (url.raw != null) {
                String originalUrl = url.raw; // Use original unresolved URL
                HttpUtils.HostInfo hostInfo = HttpUtils.parseUrl(originalUrl);
                if (debugMode) {
                    api.logging().logToOutput("DEBUG buildHost: Raw URL hostInfo.host=" + hostInfo.host);
                }
                return buildHostWithPort(hostInfo.host, hostInfo.port, hostInfo.useHttps);
            }
        }
        
        // Handle string URL format with variables (fallback)
        if (urlData instanceof String) {
            String urlString = (String) urlData; // Use original unresolved URL
            HttpUtils.HostInfo hostInfo = HttpUtils.parseUrl(urlString);
            // Temporarily disable noisy debug message
            // if (debugMode) {
            //     api.logging().logToOutput("DEBUG buildHost: String URL hostInfo.host=" + hostInfo.host);
            // }
            return buildHostWithPort(hostInfo.host, hostInfo.port, hostInfo.useHttps);
        }
        
        return "localhost";
    }
    
    private String buildHostWithPort(String host, int port, boolean useHttps) {
        // Include port only if it's not the default port
        boolean isDefaultPort = (useHttps && port == 443) || (!useHttps && port == 80);
        
        if (isDefaultPort) {
            return host;
        } else {
            return host + ":" + port;
        }
    }
    
    private void applyAuthentication(List<String> headers, PostmanCollection.Auth auth) {
        if (auth == null || auth.type == null) return;
        
        switch (auth.type.toLowerCase()) {
            case "bearer":
                String token = extractAuthValue(auth.bearer, "token");
                if (token != null) {
                    headers.add("Authorization: Bearer " + resolver.resolve(token));
                }
                break;
                
            case "basic":
                String username = extractAuthValue(auth.basic, "username");
                String password = extractAuthValue(auth.basic, "password");
                if (username != null || password != null) {
                    String credentials = (username != null ? resolver.resolve(username) : "") + ":" + 
                                       (password != null ? resolver.resolve(password) : "");
                    String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
                    headers.add("Authorization: Basic " + encoded);
                }
                break;
                
            case "apikey":
                String keyName = extractAuthValue(auth.apikey, "key");
                String keyValue = extractAuthValue(auth.apikey, "value");
                if (keyName != null && keyValue != null) {
                    headers.add(resolver.resolve(keyName) + ": " + resolver.resolve(keyValue));
                }
                break;
        }
    }
    
    private String extractAuthValue(Object authData, String key) {
        if (authData == null) return null;
        
        Gson gson = new Gson();
        
        try {
            // Try to handle as JsonElement first
            JsonElement element = gson.toJsonTree(authData);
            
            if (element.isJsonArray()) {
                // Handle array format: [{"key": "token", "value": "abc", "type": "string"}]
                JsonArray array = element.getAsJsonArray();
                for (JsonElement item : array) {
                    if (item.isJsonObject()) {
                        JsonObject obj = item.getAsJsonObject();
                        if (obj.has("key") && obj.get("key").getAsString().equals(key)) {
                            return obj.has("value") ? obj.get("value").getAsString() : null;
                        }
                    }
                }
            } else if (element.isJsonObject()) {
                // Handle object format: {"token": "abc"}
                JsonObject obj = element.getAsJsonObject();
                if (obj.has(key)) {
                    return obj.get(key).getAsString();
                }
            }
        } catch (Exception e) {
            // If JSON parsing fails, try direct property access for simple cases
            if (authData instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) authData;
                Object value = map.get(key);
                return value != null ? value.toString() : null;
            }
        }
        
        return null;
    }
    
    private byte[] buildBody(PostmanCollection.Body body, List<String> headers) throws UnsupportedEncodingException {
        if (body == null) return new byte[0];
        
        switch (body.mode) {
            case "raw":
                if (body.raw != null) {
                    String resolved = resolver.resolve(body.raw);
                    if (!hasContentType(headers)) {
                        String contentType = guessContentType(body);
                        headers.add("Content-Type: " + contentType);
                    }
                    return resolved.getBytes(StandardCharsets.UTF_8);
                }
                break;
                
            case "graphql":
                if (body.graphql != null) {
                    return buildGraphQLBody(body.graphql, headers);
                }
                break;
                
            case "urlencoded":
                if (body.urlencoded != null) {
                    List<String> params = new ArrayList<>();
                    for (PostmanCollection.UrlEncoded param : body.urlencoded) {
                        if (!param.disabled && param.key != null) {
                            String key = URLEncoder.encode(resolver.resolve(param.key), "UTF-8");
                            String value = param.value != null ? 
                                URLEncoder.encode(resolver.resolve(param.value), "UTF-8") : "";
                            params.add(key + "=" + value);
                        }
                    }
                    if (!hasContentType(headers)) {
                        headers.add("Content-Type: application/x-www-form-urlencoded");
                    }
                    return String.join("&", params).getBytes(StandardCharsets.UTF_8);
                }
                break;
                
            case "formdata":
                if (body.formdata != null) {
                    String boundary = "----WebKitFormBoundary" + generateBoundary();
                    if (!hasContentType(headers)) {
                        headers.add("Content-Type: multipart/form-data; boundary=" + boundary);
                    }
                    return buildMultipartBody(body.formdata, boundary);
                }
                break;
        }
        
        return new byte[0];
    }
    
    private byte[] buildGraphQLBody(PostmanCollection.GraphQL graphql, List<String> headers) {
        if (graphql == null) return new byte[0];
        
        // Keep minimal GraphQL logging to avoid buffer overflow
        if (debugMode) {
            api.logging().logToOutput("DEBUG GraphQL: Building GraphQL body");
            // api.logging().logToOutput("DEBUG GraphQL: Query=" + graphql.query);
            // api.logging().logToOutput("DEBUG GraphQL: Variables raw=" + graphql.variables);
        }
        
        try {
            Gson gson = new GsonBuilder()
                .serializeNulls()  // Preserve null values in JSON
                .create();
            JsonObject body = new JsonObject();
            
            // Add query (resolve variables in the query string)
            // Supports all GraphQL operations: query, mutation, subscription
            if (graphql.query != null) {
                String resolvedQuery = resolver.resolve(graphql.query);
                body.addProperty("query", resolvedQuery);
                // Reduce verbose logging
                // if (debugMode) {
                //     api.logging().logToOutput("DEBUG GraphQL: Resolved query=" + resolvedQuery);
                // }
            }
            
            // Add variables (preserve the actual GraphQL variables structure)
            if (graphql.variables != null && !graphql.variables.trim().isEmpty()) {
                try {
                    // Clean up the variables string (remove extra whitespace and newlines)
                    String variablesString = graphql.variables.trim();
                    if (debugMode) {
                        api.logging().logToOutput("DEBUG GraphQL: Cleaned variables string=" + variablesString);
                    }
                    
                    // Parse the JSON to get the actual structure first
                    JsonElement variablesElement = gson.fromJson(variablesString, JsonElement.class);
                    if (debugMode) {
                        api.logging().logToOutput("DEBUG GraphQL: Parsed variables element=" + variablesElement);
                    }
                    
                    // Only resolve Postman variables, not GraphQL nulls
                    // Check if the original string contains Postman variables
                    if (variablesString.contains("{{") && variablesString.contains("}}")) {
                        // Convert back to string, then resolve Postman variables, then parse again
                        String variablesJson = gson.toJson(variablesElement);
                        String resolvedVariablesJson = resolver.resolve(variablesJson);
                        JsonElement finalVariables = gson.fromJson(resolvedVariablesJson, JsonElement.class);
                        body.add("variables", finalVariables);
                        
                        if (debugMode) {
                            api.logging().logToOutput("DEBUG GraphQL: Variables had Postman vars, resolved to=" + finalVariables);
                        }
                    } else {
                        // No Postman variables, use original parsed structure
                        body.add("variables", variablesElement);
                        
                        if (debugMode) {
                            api.logging().logToOutput("DEBUG GraphQL: No Postman vars, using original=" + variablesElement);
                        }
                    }
                } catch (Exception e) {
                    if (debugMode) {
                        api.logging().logToOutput("DEBUG GraphQL: Variables parsing failed, trying fallback: " + e.getMessage());
                    }
                    // If first approach fails, try simpler direct parsing
                    try {
                        String cleanVariables = graphql.variables.replaceAll("\\s+", " ").trim();
                        String resolvedVariables = resolver.resolve(cleanVariables);
                        JsonElement variablesElement = gson.fromJson(resolvedVariables, JsonElement.class);
                        body.add("variables", variablesElement);
                    } catch (Exception ex) {
                        if (debugMode) {
                            api.logging().logToOutput("DEBUG GraphQL: All variables parsing failed, using empty object: " + ex.getMessage());
                        }
                        // Final fallback to empty object
                        body.add("variables", new JsonObject());
                    }
                }
            } else {
                body.add("variables", new JsonObject());
            }
            
            // Set Content-Type header if not already present
            if (!hasContentType(headers)) {
                headers.add("Content-Type: application/json");
            }
            
            String finalBody = gson.toJson(body);
            if (debugMode) {
                api.logging().logToOutput("DEBUG GraphQL: Final JSON body=" + finalBody);
            }
            
            return finalBody.getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            if (debugMode) {
                api.logging().logToOutput("DEBUG GraphQL: Complete failure in buildGraphQLBody: " + e.getMessage());
            }
            // Fallback to empty body if GraphQL processing fails
            return new byte[0];
        }
    }
    
    private byte[] buildMultipartBody(List<PostmanCollection.FormData> formData, String boundary) {
        StringBuilder body = new StringBuilder();
        
        for (PostmanCollection.FormData field : formData) {
            if (!field.disabled && field.key != null) {
                body.append("--").append(boundary).append("\r\n");
                
                if ("file".equals(field.type)) {
                    body.append("Content-Disposition: form-data; name=\"")
                        .append(resolver.resolve(field.key))
                        .append("\"; filename=\"")
                        .append(field.getSrcAsString() != null ? field.getSrcAsString() : "file.txt")
                        .append("\"\r\n");
                    body.append("Content-Type: application/octet-stream\r\n\r\n");
                    body.append("[File content placeholder]");
                } else {
                    body.append("Content-Disposition: form-data; name=\"")
                        .append(resolver.resolve(field.key))
                        .append("\"\r\n\r\n");
                    body.append(field.value != null ? resolver.resolve(field.value) : "");
                }
                body.append("\r\n");
            }
        }
        
        body.append("--").append(boundary).append("--\r\n");
        return body.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    private boolean hasContentType(List<String> headers) {
        return headers.stream().anyMatch(h -> h.toLowerCase().startsWith("content-type:"));
    }
    
    private String guessContentType(PostmanCollection.Body body) {
        if (body.options != null && body.options.raw != null && body.options.raw.language != null) {
            switch (body.options.raw.language) {
                case "json": return "application/json";
                case "xml": return "application/xml";
                case "html": return "text/html";
                case "javascript": return "application/javascript";
                default: return "text/plain";
            }
        }
        
        // Enhanced GraphQL detection - check if raw body contains GraphQL query
        if (body.raw != null && isGraphQLQuery(body.raw)) {
            return "application/json";
        }
        
        return "text/plain";
    }
    
    private boolean isGraphQLQuery(String body) {
        if (body == null) return false;
        
        // Check for common GraphQL patterns
        String lowerBody = body.toLowerCase().trim();
        return lowerBody.contains("\"query\"") || 
               lowerBody.contains("\"mutation\"") || 
               lowerBody.contains("\"subscription\"") ||
               lowerBody.startsWith("query ") ||
               lowerBody.startsWith("mutation ") ||
               lowerBody.startsWith("subscription ");
    }
    
    private String generateBoundary() {
        return Long.toHexString(System.currentTimeMillis());
    }
    
    private PostmanCollection.Url parseUrlObject(Object urlData) {
        if (urlData == null) return null;
        
        try {
            Gson gson = new Gson();
            JsonElement element = gson.toJsonTree(urlData);
            if (element.isJsonObject()) {
                return gson.fromJson(element, PostmanCollection.Url.class);
            }
        } catch (Exception e) {
            // If parsing fails, return null and let the caller handle string format
        }
        return null;
    }
}
