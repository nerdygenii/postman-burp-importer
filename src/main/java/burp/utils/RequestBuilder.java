package burp.utils;

import burp.*;
import burp.models.*;
import burp.parser.*;
import com.google.gson.*;
import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;

public class RequestBuilder {
    private final IExtensionHelpers helpers;
    private final VariableResolver resolver;
    private final boolean debugMode = false; // Set to true to enable debug logging
    
    public RequestBuilder(IExtensionHelpers helpers, VariableResolver resolver) {
        this.helpers = helpers;
        this.resolver = resolver;
    }
    
    public byte[] buildRequest(PostmanCollection.Request request) throws Exception {
        List<String> headers = new ArrayList<>();
        
        // Build request line
        String method = request.method != null ? request.method : "GET";
        String path = buildPath(request.url);
        headers.add(method + " " + path + " HTTP/1.1");
        
        // Add host header
        String host = buildHost(request.url);
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
                        System.out.println("DEBUG: Processing custom header: " + key + ": " + value);
                    }
                    
                    // Skip Host header - we build it automatically from the URL
                    if (!"Host".equalsIgnoreCase(key)) {
                        headers.add(key + ": " + value);
                        if (debugMode) {
                            System.out.println("DEBUG: Added custom header: " + key + ": " + value);
                        }
                    } else {
                        if (debugMode) {
                            System.out.println("DEBUG: Skipped Host header: " + key + ": " + value);
                        }
                    }
                }
            }
        }
        
        // Handle authentication
        applyAuthentication(headers, request.auth);
        
        // Build body
        byte[] body = buildBody(request.body, headers);
        
        return helpers.buildHttpMessage(headers, body);
    }
    
    private String buildPath(Object urlData) throws UnsupportedEncodingException {
        if (urlData == null) return "/";
        
        // Handle string URL format
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
    
    private String buildHost(Object urlData) {
        if (urlData == null) return "localhost";
        
        // Handle string URL format
        if (urlData instanceof String) {
            String urlString = resolver.resolve((String) urlData);
            HttpUtils.HostInfo hostInfo = HttpUtils.parseUrl(urlString);
            return buildHostWithPort(hostInfo.host, hostInfo.port, hostInfo.useHttps);
        }
        
        // Handle Url object format
        PostmanCollection.Url url = parseUrlObject(urlData);
        if (url == null) return "localhost";
        
        if (url.host != null && !url.host.isEmpty()) {
            String host = String.join(".", url.host);
            if (url.port != null && !url.port.isEmpty()) {
                host += ":" + url.port;
            }
            return resolver.resolve(host);
        } else if (url.raw != null) {
            String resolved = resolver.resolve(url.raw);
            HttpUtils.HostInfo hostInfo = HttpUtils.parseUrl(resolved);
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
