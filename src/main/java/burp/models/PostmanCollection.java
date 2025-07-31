package burp.models;

import java.util.List;
import java.util.Map;

public class PostmanCollection {
    public Info info;
    public List<Item> item;
    public List<Variable> variable;
    public Auth auth;
    
    public static class Info {
        public String name;
        public String _postman_id;
        public String description;
        public String schema;  // Changed from Schema object to String
    }
    
    public static class Item {
        public String name;
        public Request request;
        public List<Item> item;
        public String description;
        public List<Event> event;  // Fixed: Changed to List<Event> to match Postman format
    }
    
    public static class Event {
        public String listen;
        public Script script;
    }
    
    public static class Script {
        public String type;
        public List<String> exec;
    }
    
    public static class Request {
        public String method;
        public List<Header> header;
        public Body body;
        public Object url;  // Changed to Object to handle both string and Url object formats
        public Auth auth;
        public String description;
    }
    
    public static class Header {
        public String key;
        public String value;
        public String type;
        public boolean disabled;
        public String description;
    }
    
    public static class Body {
        public String mode;
        public String raw;
        public List<FormData> formdata;
        public List<UrlEncoded> urlencoded;
        public Options options;
        public File file;  // Added for file uploads
        public GraphQL graphql;  // Added for GraphQL support
    }
    
    public static class GraphQL {
        public String query;
        public String variables;
    }
    
    public static class File {
        public String src;
    }
    
    public static class FormData {
        public String key;
        public String value;
        public String type;
        public Object src;  // Can be String or Array
        public boolean disabled;
        public String description;
        
        // Helper method to get src as string
        public String getSrcAsString() {
            if (src == null) return null;
            if (src instanceof String) {
                return (String) src;
            } else if (src instanceof java.util.List) {
                java.util.List<?> srcList = (java.util.List<?>) src;
                if (!srcList.isEmpty()) {
                    return srcList.get(0).toString(); // Use first file
                }
            } else if (src.getClass().isArray()) {
                Object[] srcArray = (Object[]) src;
                if (srcArray.length > 0) {
                    return srcArray[0].toString(); // Use first file
                }
            }
            return src.toString(); // Fallback
        }
    }
    
    public static class UrlEncoded {
        public String key;
        public String value;
        public boolean disabled;
        public String description;
        public String type;
    }
    
    public static class Options {
        public Raw raw;
    }
    
    public static class Raw {
        public String language;
    }
    
    public static class Url {
        public String raw;
        public String protocol;
        public List<String> host;
        public List<String> path;
        public List<Query> query;
        public String port;
        public List<Variable> variable;  // Added for path variables
    }
    
    public static class Query {
        public String key;
        public String value;
        public boolean disabled;
        public String description;
    }
    
    public static class Auth {
        public String type;
        public Object bearer;    // Changed to Object to handle both array and object formats
        public Object basic;     // Changed to Object to handle both array and object formats  
        public Object apikey;    // Changed to Object to handle both array and object formats
        public Object oauth2;    // Changed to Object to handle both array and object formats
    }
    
    public static class AuthAttribute {
        public String key;
        public String value;
        public String type;
    }
    
    public static class Variable {
        public String key;
        public String value;
        public String type;
        public String description;
    }
}
