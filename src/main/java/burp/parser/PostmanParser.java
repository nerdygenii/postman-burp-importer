package burp.parser;

import burp.models.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
            return gson.fromJson(reader, PostmanCollection.class);
        }
    }
    
    public PostmanEnvironment parseEnvironment(File file) throws Exception {
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, PostmanEnvironment.class);
        }
    }
}
