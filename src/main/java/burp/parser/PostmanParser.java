package burp.parser;

import burp.models.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;

public class PostmanParser {
    private final Gson gson;
    
    public PostmanParser() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    }
    
    public PostmanCollection parseCollection(File file) throws Exception {
        try (FileReader reader = new FileReader(file)) {
            // First, try to parse as standard Postman collection
            JsonObject jsonObject = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
            
            PostmanCollection collection;
            
            // Check if it's wrapped in a "collection" key (like crAPI format)
            if (jsonObject.has("collection") && jsonObject.get("collection").isJsonObject()) {
                // Extract the actual collection from the wrapper
                JsonObject actualCollection = jsonObject.getAsJsonObject("collection");
                collection = gson.fromJson(actualCollection, PostmanCollection.class);
            } else {
                // Standard format - parse directly
                collection = gson.fromJson(jsonObject, PostmanCollection.class);
            }
            
            // Validate and fix null fields
            if (collection != null) {
                if (collection.item == null) {
                    collection.item = new java.util.ArrayList<>();
                }
                if (collection.variable == null) {
                    collection.variable = new java.util.ArrayList<>();
                }
                if (collection.info == null) {
                    collection.info = new PostmanCollection.Info();
                    collection.info.name = "Unnamed Collection";
                }
            }
            
            return collection;
        }
    }
    
    public PostmanEnvironment parseEnvironment(File file) throws Exception {
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, PostmanEnvironment.class);
        }
    }
}
