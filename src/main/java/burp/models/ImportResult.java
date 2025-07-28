package burp.models;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {
    public String collectionName;
    public int totalRequests;
    public int successCount;
    public List<String> failedRequests = new ArrayList<>();
    public List<FailedRequestInfo> failedRequestDetails = new ArrayList<>();
    public String error;
    
    public static class FailedRequestInfo {
        public String name;
        public String path;
        public String errorMessage;
        public Object requestData; // Store the actual request for retry
        
        public FailedRequestInfo(String name, String path, String errorMessage, Object requestData) {
            this.name = name;
            this.path = path;
            this.errorMessage = errorMessage;
            this.requestData = requestData;
        }
    }
}
