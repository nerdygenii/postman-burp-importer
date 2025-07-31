package burp.utils;

import java.net.URL;
import java.util.regex.Pattern;

public class HttpUtils {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}");
    
    public static class HostInfo {
        public final String host;
        public final int port;
        public final boolean useHttps;
        
        public HostInfo(String host, int port, boolean useHttps) {
            this.host = host;
            this.port = port;
            this.useHttps = useHttps;
        }
    }
    
    public static HostInfo parseUrl(String urlString) {
        if (urlString == null || urlString.isEmpty()) {
            return new HostInfo("localhost", 80, false);
        }
        
        // Check if URL contains unresolved variables - preserve them as-is
        if (VARIABLE_PATTERN.matcher(urlString).find()) {
            return parseUrlWithVariables(urlString);
        }
        
        try {
            URL url = new URL(urlString);
            String host = url.getHost();
            int port = url.getPort();
            boolean useHttps = "https".equalsIgnoreCase(url.getProtocol());
            
            if (port == -1) {
                port = useHttps ? 443 : 80;
            }
            
            return new HostInfo(host, port, useHttps);
        } catch (Exception e) {
            // Fallback for malformed URLs - still check for variables
            return parseUrlWithVariables(urlString);
        }
    }
    
    private static HostInfo parseUrlWithVariables(String urlString) {
        // Extract host portion from URLs containing variables
        // Examples: 
        // "{{url}}/path" -> "{{url}}"
        // "https://{{baseurl}}/api" -> "{{baseurl}}"
        // "{{protocol}}://{{host}}:{{port}}/path" -> "{{host}}"
        
        String host = "localhost"; // Default fallback
        
        try {
            // Remove protocol if present
            String withoutProtocol = urlString;
            if (urlString.contains("://")) {
                withoutProtocol = urlString.substring(urlString.indexOf("://") + 3);
            }
            
            // Extract host part (everything before first slash or colon)
            int slashIndex = withoutProtocol.indexOf('/');
            int colonIndex = withoutProtocol.indexOf(':');
            
            int endIndex = withoutProtocol.length();
            if (slashIndex != -1 && colonIndex != -1) {
                endIndex = Math.min(slashIndex, colonIndex);
            } else if (slashIndex != -1) {
                endIndex = slashIndex;
            } else if (colonIndex != -1) {
                endIndex = colonIndex;
            }
            
            String hostPart = withoutProtocol.substring(0, endIndex);
            
            // If the host part contains variables, preserve it
            if (VARIABLE_PATTERN.matcher(hostPart).find()) {
                host = hostPart;
            } else if (!hostPart.isEmpty()) {
                host = hostPart;
            }
            
        } catch (Exception e) {
            // Keep default localhost if parsing fails completely
        }
        
        return new HostInfo(host, 80, false);
    }
}
