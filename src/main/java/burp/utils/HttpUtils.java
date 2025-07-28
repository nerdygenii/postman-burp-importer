package burp.utils;

import java.net.URL;

public class HttpUtils {
    
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
            // Fallback for malformed URLs
            return new HostInfo("localhost", 80, false);
        }
    }
}
