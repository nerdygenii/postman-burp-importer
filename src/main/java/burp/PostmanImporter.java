/*
 * Postman Collection Importer for Burp Suite
 * Main implementation class
 * 
 * Author: Abdulrahman Oyekunle
 * GitHub: https://github.com/nerdygenii/postman-burp-importer
 * 
 * This class handles the core functionality of importing Postman collections,
 * processing variables, and converting requests to Burp Suite format.
 */

package burp;

import burp.models.*;
import burp.parser.*;
import burp.ui.*;
import burp.utils.*;
import burp.api.montoya.MontoyaApi;
import com.google.gson.*;
import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PostmanImporter {
    private final MontoyaApi api;
    private final ImporterPanel ui;
    private final PostmanParser parser;
    private final VariableResolver variableResolver;
    private final RequestBuilder requestBuilder;
    private final VariableDetector variableDetector; // Added variable detector
    private final Set<String> existingTabs = ConcurrentHashMap.newKeySet();
    private final boolean debugMode = true; // Set to false to reduce logging
    private ImportResult lastImportResult; // Store last import result for retry functionality
    
    public PostmanImporter(MontoyaApi api) {
        this.api = api;
        this.parser = new PostmanParser();
        this.variableResolver = new VariableResolver();
        this.requestBuilder = new RequestBuilder(api, variableResolver);
        this.variableDetector = new VariableDetector(variableResolver); // Initialize variable detector
        this.ui = new ImporterPanel(this);
    }
    
    public JPanel getMainPanel() {
        return ui.getPanel();
    }
    
    public void retryFailedRequests(String destination) {
        if (lastImportResult == null || lastImportResult.failedRequestDetails.isEmpty()) {
            ui.appendLog("No failed requests to retry.");
            return;
        }
        
        SwingWorker<ImportResult, String> worker = new SwingWorker<ImportResult, String>() {
            @Override
            protected ImportResult doInBackground() throws Exception {
                ImportResult retryResult = new ImportResult();
                retryResult.collectionName = lastImportResult.collectionName + " (Retry)";
                retryResult.totalRequests = lastImportResult.failedRequestDetails.size();
                
                publish("Retrying " + retryResult.totalRequests + " failed requests...");
                
                for (int i = 0; i < lastImportResult.failedRequestDetails.size(); i++) {
                    if (isCancelled()) break;
                    
                    ImportResult.FailedRequestInfo failedInfo = lastImportResult.failedRequestDetails.get(i);
                    
                    try {
                        // Cast the stored request data back to RequestItem
                        if (failedInfo.requestData instanceof RequestItem) {
                            RequestItem item = (RequestItem) failedInfo.requestData;
                            processRequest(item, destination);
                            retryResult.successCount++;
                            publish("✓ Retry successful: " + failedInfo.name);
                        } else {
                            throw new Exception("Invalid request data stored for retry");
                        }
                    } catch (Exception e) {
                        retryResult.failedRequestDetails.add(new ImportResult.FailedRequestInfo(
                            failedInfo.name, failedInfo.path, e.getMessage(), failedInfo.requestData));
                        retryResult.failedRequests.add(failedInfo.name + ": " + e.getMessage());
                        publish("✗ Retry failed: " + failedInfo.name + " - " + e.getMessage());
                    }
                    
                    setProgress((i + 1) * 100 / retryResult.totalRequests);
                }
                
                return retryResult;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    ui.appendLog(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    ImportResult retryResult = get();
                    
                    // Merge retry results with original results
                    ImportResult mergedResult = mergeRetryResults(lastImportResult, retryResult);
                    lastImportResult = mergedResult; // Update for future retries
                    
                    ui.showImportSummary(mergedResult);
                    
                    if (retryResult.successCount > 0) {
                        ui.appendLog("\n🎉 Retry completed! " + retryResult.successCount + 
                                   " previously failed requests are now successful.");
                    }
                } catch (Exception e) {
                    ui.showError("Retry failed: " + e.getMessage());
                }
                ui.setImportComplete();
            }
        };
        
        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                ui.updateProgress((Integer) evt.getNewValue());
            }
        });
        
        ui.setImportInProgress();
        worker.execute();
    }
    
    private ImportResult mergeRetryResults(ImportResult original, ImportResult retry) {
        ImportResult merged = new ImportResult();
        merged.collectionName = original.collectionName;
        merged.totalRequests = original.totalRequests;
        merged.successCount = original.successCount + retry.successCount;
        
        // Only keep requests that failed in the retry
        merged.failedRequests.addAll(retry.failedRequests);
        merged.failedRequestDetails.addAll(retry.failedRequestDetails);
        
        return merged;
    }
    
    // New method for generating previews
    public void showPreview(File collectionFile, File environmentFile) {
        SwingWorker<List<RequestPreview>, String> worker = new SwingWorker<List<RequestPreview>, String>() {
            @Override
            protected List<RequestPreview> doInBackground() throws Exception {
                publish("Analyzing collection...");
                
                // Parse collection
                PostmanCollection collection = parser.parseCollection(collectionFile);
                
                // Parse environment if provided
                VariableResolver tempResolver = new VariableResolver();
                if (environmentFile != null) {
                    publish("Loading environment variables...");
                    PostmanEnvironment environment = parser.parseEnvironment(environmentFile);
                    tempResolver.addEnvironmentVariables(environment);
                }
                
                // Add collection variables
                tempResolver.addCollectionVariables(collection);
                
                // Analyze variables
                publish("Analyzing variables...");
                VariableDetector tempDetector = new VariableDetector(tempResolver);
                VariableAnalysis variableAnalysis = tempDetector.analyzeCollection(collection);
                
                // Generate previews with variable information
                publish("Generating request previews...");
                return generatePreviews(collection, tempResolver, tempDetector, variableAnalysis);
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    ui.appendLog(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<RequestPreview> previews = get();
                    ui.appendLog("Preview generated successfully. Found " + previews.size() + " requests.");
                    
                    // Check if we need to show variable resolution dialog first
                    checkAndHandleVariables(previews, collectionFile, environmentFile);
                } catch (Exception e) {
                    ui.showError("Preview failed: " + e.getMessage());
                    ui.appendLog("Preview error: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void checkAndHandleVariables(List<RequestPreview> previews, File collectionFile, File environmentFile) {
        // Check if there are any unresolved variables across all requests
        boolean hasUnresolvedVariables = previews.stream()
            .anyMatch(RequestPreview::hasUnresolvedVariables);
        
        if (hasUnresolvedVariables) {
            // Show variable resolution dialog regardless of environment file
            showVariableResolutionDialog(previews, collectionFile, environmentFile);
        } else {
            // Proceed directly to selection dialog
            showSelectionDialog(previews, collectionFile, environmentFile);
        }
    }
    
    private void showVariableResolutionDialog(List<RequestPreview> previews, File collectionFile, File environmentFile) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Re-analyze with current resolver state for accurate variable detection
                PostmanCollection collection = parser.parseCollection(collectionFile);
                VariableAnalysis analysis = variableDetector.analyzeCollection(collection);
                
                if (analysis.hasVariables()) {
                    VariableResolutionDialog dialog = new VariableResolutionDialog(
                        ui.getPanel(), analysis, variableDetector);
                    
                    if (dialog.showDialog()) {
                        handleVariableResolution(dialog, previews, collectionFile, environmentFile);
                    } else {
                        ui.appendLog("Variable resolution cancelled by user.");
                        // Don't proceed - user cancelled, so we're done
                        return;
                    }
                } else {
                    // No variables detected, proceed normally
                    showSelectionDialog(previews, collectionFile, environmentFile);
                }
            } catch (Exception e) {
                ui.showError("Variable analysis failed: " + e.getMessage());
            }
        });
    }
    
    private void handleVariableResolution(VariableResolutionDialog dialog, List<RequestPreview> previews, 
                                         File collectionFile, File environmentFile) {
        switch (dialog.getChoice()) {
            case UPLOAD_ENVIRONMENT:
                // Use the selected environment file
                File newEnvironmentFile = dialog.getSelectedEnvironmentFile();
                ui.appendLog("Environment file selected: " + newEnvironmentFile.getName());
                
                // Regenerate previews with new environment
                regeneratePreviewsWithEnvironment(collectionFile, newEnvironmentFile);
                break;
                
            case MANUAL_ENTRY:
                // Apply manual variables to resolver
                Map<String, String> manualVars = dialog.getManualVariables();
                for (Map.Entry<String, String> entry : manualVars.entrySet()) {
                    variableResolver.addCustomVariable(entry.getKey(), entry.getValue());
                }
                ui.appendLog("Applied " + manualVars.size() + " manual variables.");
                
                // Regenerate previews with new variables
                regeneratePreviewsWithCurrentResolver(collectionFile, environmentFile);
                break;
                
            case IGNORE_CONTINUE:
                ui.appendLog("Continuing with unresolved variables (requests may fail).");
                showSelectionDialog(previews, collectionFile, environmentFile);
                break;
                
            case SKIP_VARIABLE_REQUESTS:
                // Filter out requests with unresolved variables
                List<RequestPreview> filteredPreviews = previews.stream()
                    .filter(p -> !p.hasUnresolvedVariables())
                    .collect(java.util.stream.Collectors.toList());
                
                ui.appendLog("Filtered to " + filteredPreviews.size() + " requests without variables.");
                showSelectionDialog(filteredPreviews, collectionFile, environmentFile);
                break;
        }
    }
    
    private void regeneratePreviewsWithEnvironment(File collectionFile, File environmentFile) {
        // Restart the preview process with the new environment file
        showPreview(collectionFile, environmentFile);
    }
    
    private void regeneratePreviewsWithCurrentResolver(File collectionFile, File environmentFile) {
        // Regenerate previews with current resolver state
        showPreview(collectionFile, environmentFile);
    }
    
    private List<RequestPreview> generatePreviews(PostmanCollection collection, VariableResolver resolver, 
                                                 VariableDetector detector, VariableAnalysis analysis) {
        List<RequestPreview> previews = new ArrayList<>();
        generatePreviewsRecursive(collection.item, "", previews, resolver, detector);
        return previews;
    }
    
    private void generatePreviewsRecursive(List<PostmanCollection.Item> items, String path, 
                                         List<RequestPreview> previews, VariableResolver resolver, 
                                         VariableDetector detector) {
        for (PostmanCollection.Item item : items) {
            String currentPath = path.isEmpty() ? item.name : path + "/" + item.name;
            
            if (item.request != null) {
                RequestPreview preview = createRequestPreview(item, currentPath, resolver, detector);
                previews.add(preview);
            }
            
            if (item.item != null && !item.item.isEmpty()) {
                generatePreviewsRecursive(item.item, currentPath, previews, resolver, detector);
            }
        }
    }
    
    private RequestPreview createRequestPreview(PostmanCollection.Item item, String path, 
                                              VariableResolver resolver, VariableDetector detector) {
        PostmanCollection.Request request = item.request;
        
        // Resolve URL for preview
        String url = "Unknown URL";
        try {
            String rawUrl = extractRawUrl(request.url);
            if (rawUrl != null) {
                url = resolver.resolve(rawUrl);
            }
        } catch (Exception e) {
            url = "Error resolving URL: " + e.getMessage();
        }
        
        // Check for various features
        boolean hasAuth = request.auth != null;
        boolean hasHeaders = request.header != null && !request.header.isEmpty();
        boolean hasBody = request.body != null && request.body.raw != null && !request.body.raw.trim().isEmpty();
        
        String method = request.method != null ? request.method : "GET";
        String description = request.description != null ? request.description : "";
        
        // Detect unresolved variables in this specific request
        Set<String> requestVariables = detector.findVariablesInRequest(request);
        Set<String> unresolvedVariables = new HashSet<>();
        
        for (String variable : requestVariables) {
            String testValue = "{{" + variable + "}}";
            String resolved = resolver.resolve(testValue);
            
            // If it's still the same, it means the variable wasn't resolved
            if (testValue.equals(resolved)) {
                unresolvedVariables.add(variable);
            }
        }
        
        // Enhanced GraphQL detection and naming
        String displayName = item.name;
        if (isGraphQLRequest(request)) {
            String operation = extractGraphQLOperation(request.body.raw);
            if (operation != null) {
                displayName = item.name + " [GraphQL: " + operation + "]";
            } else {
                displayName = item.name + " [GraphQL]";
            }
        }
        
        return new RequestPreview(displayName, path, method, url, description, hasAuth, hasHeaders, hasBody, unresolvedVariables);
    }
    
    private void showSelectionDialog(List<RequestPreview> previews, File collectionFile, File environmentFile) {
        SwingUtilities.invokeLater(() -> {
            RequestSelectionDialog dialog = new RequestSelectionDialog(previews, this, ui.getPanel());
            
            if (dialog.showDialog()) {
                List<RequestPreview> selectedPreviews = dialog.getSelectedRequests();
                if (!selectedPreviews.isEmpty()) {
                    ui.appendLog("Starting import of " + selectedPreviews.size() + " selected requests...");
                    String destination = ui.getSelectedDestination();
                    importSelectedRequests(collectionFile, environmentFile, selectedPreviews, destination);
                } else {
                    ui.appendLog("No requests selected for import.");
                }
            } else {
                ui.appendLog("Import cancelled by user.");
            }
        });
    }
    
    public void importSelectedRequests(File collectionFile, File environmentFile, List<RequestPreview> selectedPreviews) {
        importSelectedRequests(collectionFile, environmentFile, selectedPreviews, "repeater");
    }
    
    public void importSelectedRequests(File collectionFile, File environmentFile, List<RequestPreview> selectedPreviews, String destination) {
        // Create a set of selected request paths for quick lookup
        Set<String> selectedPaths = new HashSet<>();
        for (RequestPreview preview : selectedPreviews) {
            selectedPaths.add(preview.getPath());
        }
        
        SwingWorker<ImportResult, String> worker = new SwingWorker<ImportResult, String>() {
            @Override
            protected ImportResult doInBackground() throws Exception {
                ImportResult result = new ImportResult();
                
                try {
                    // Parse collection
                    publish("Parsing collection file...");
                    PostmanCollection collection = parser.parseCollection(collectionFile);
                    result.collectionName = collection.info.name;
                    
                    // Parse environment if provided
                    if (environmentFile != null) {
                        publish("Parsing environment file...");
                        PostmanEnvironment environment = parser.parseEnvironment(environmentFile);
                        variableResolver.addEnvironmentVariables(environment);
                    }
                    
                    // Add collection variables
                    variableResolver.addCollectionVariables(collection);
                    
                    // Flatten all requests
                    List<RequestItem> requests = flattenRequests(collection.item, "");
                    
                    // Filter to only selected requests
                    List<RequestItem> selectedRequests = new ArrayList<>();
                    for (RequestItem item : requests) {
                        if (selectedPaths.contains(item.path)) {
                            selectedRequests.add(item);
                        }
                    }
                    
                    result.totalRequests = selectedRequests.size();
                    publish("Processing " + selectedRequests.size() + " selected requests...");
                    
                    // Process each selected request
                    for (int i = 0; i < selectedRequests.size(); i++) {
                        if (isCancelled()) break;
                        
                        RequestItem item = selectedRequests.get(i);
                        try {
                            processRequest(item, destination);
                            result.successCount++;
                            publish("✓ Imported: " + item.name);
                        } catch (Exception e) {
                            result.failedRequestDetails.add(new ImportResult.FailedRequestInfo(
                                item.name, item.path, e.getMessage(), item));
                            result.failedRequests.add(item.name + ": " + e.getMessage());
                            publish("✗ Failed: " + item.name + " - " + e.getMessage());
                        }
                        
                        setProgress((i + 1) * 100 / selectedRequests.size());
                    }
                    
                } catch (Exception e) {
                    result.error = e.getMessage();
                    publish("Fatal error: " + e.getMessage());
                }
                
                return result;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    ui.appendLog(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    ImportResult result = get();
                    lastImportResult = result; // Store for retry functionality
                    ui.showImportSummary(result);
                } catch (Exception e) {
                    ui.showError("Import failed: " + e.getMessage());
                }
                ui.setImportComplete();
            }
        };
        
        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                ui.updateProgress((Integer) evt.getNewValue());
            }
        });
        
        ui.setImportInProgress();
        worker.execute();
    }
    
    public void importCollection(File collectionFile, File environmentFile) {
        importCollection(collectionFile, environmentFile, "repeater");
    }
    
    public void importCollection(File collectionFile, File environmentFile, String destination) {
        // First check for variables, similar to showPreview
        SwingWorker<List<RequestPreview>, String> worker = new SwingWorker<List<RequestPreview>, String>() {
            @Override
            protected List<RequestPreview> doInBackground() throws Exception {
                publish("Analyzing collection...");
                
                // Parse collection
                PostmanCollection collection = parser.parseCollection(collectionFile);
                
                // Parse environment if provided
                VariableResolver tempResolver = new VariableResolver();
                if (environmentFile != null) {
                    publish("Loading environment variables...");
                    PostmanEnvironment environment = parser.parseEnvironment(environmentFile);
                    tempResolver.addEnvironmentVariables(environment);
                }
                
                // Add collection variables
                tempResolver.addCollectionVariables(collection);
                
                // Analyze variables
                publish("Analyzing variables...");
                VariableDetector tempDetector = new VariableDetector(tempResolver);
                VariableAnalysis variableAnalysis = tempDetector.analyzeCollection(collection);
                
                // Generate previews with variable information
                publish("Generating request previews...");
                return generatePreviews(collection, tempResolver, tempDetector, variableAnalysis);
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    ui.appendLog(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<RequestPreview> previews = get();
                    ui.appendLog("Analysis complete. Checking for variables...");
                    
                    // Check if we need to handle variables first
                    checkVariablesAndImport(previews, collectionFile, environmentFile, destination);
                } catch (Exception e) {
                    ui.showError("Import failed: " + e.getMessage());
                    ui.appendLog("Import error: " + e.getMessage());
                    ui.setImportComplete();
                }
            }
        };
        
        ui.setImportInProgress();
        worker.execute();
    }
    
    private void checkVariablesAndImport(List<RequestPreview> previews, File collectionFile, File environmentFile, String destination) {
        // Check if there are any unresolved variables across all requests
        boolean hasUnresolvedVariables = previews.stream()
            .anyMatch(RequestPreview::hasUnresolvedVariables);
        
        if (hasUnresolvedVariables) {
            // Show variable resolution dialog regardless of environment file
            showVariableResolutionDialogForImport(previews, collectionFile, environmentFile, destination);
        } else {
            // Proceed directly with import using all requests
            proceedWithDirectImport(collectionFile, environmentFile, destination);
        }
    }
    
    private void showVariableResolutionDialogForImport(List<RequestPreview> previews, File collectionFile, File environmentFile, String destination) {
        try {
            PostmanCollection collection = parser.parseCollection(collectionFile);
            VariableResolver tempResolver = new VariableResolver();
            if (environmentFile != null) {
                PostmanEnvironment environment = parser.parseEnvironment(environmentFile);
                tempResolver.addEnvironmentVariables(environment);
            }
            tempResolver.addCollectionVariables(collection);
            
            VariableDetector tempDetector = new VariableDetector(tempResolver);
            VariableAnalysis variableAnalysis = tempDetector.analyzeCollection(collection);
            
            if (!variableAnalysis.getUnresolvedVariables().isEmpty()) {
                VariableResolutionDialog dialog = new VariableResolutionDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(ui.getPanel()),
                    variableAnalysis,
                    tempDetector
                );
                
                dialog.setVisible(true);
                
                if (dialog.getChoice() != VariableResolutionDialog.ResolutionChoice.CANCEL) {
                    VariableResolutionDialog.ResolutionChoice choice = dialog.getChoice();
                    
                    switch (choice) {
                        case UPLOAD_ENVIRONMENT:
                            ui.appendLog("Please upload an environment file and try again.");
                            ui.setImportComplete();
                            return;
                            
                        case MANUAL_ENTRY:
                            ManualVariableEntryDialog entryDialog = new ManualVariableEntryDialog(
                                (JFrame) SwingUtilities.getWindowAncestor(ui.getPanel()),
                                variableAnalysis.getUnresolvedVariables(),
                                tempDetector
                            );
                            
                            if (entryDialog.showDialog()) {
                                // Add manually entered variables to resolver
                                Map<String, String> enteredValues = entryDialog.getVariables();
                                for (Map.Entry<String, String> entry : enteredValues.entrySet()) {
                                    variableResolver.addCustomVariable(entry.getKey(), entry.getValue());
                                }
                                proceedWithDirectImport(collectionFile, environmentFile, destination);
                            } else {
                                ui.appendLog("Import cancelled by user.");
                                ui.setImportComplete();
                            }
                            return;
                            
                        case IGNORE_CONTINUE:
                            ui.appendLog("Proceeding with import, ignoring unresolved variables...");
                            proceedWithDirectImport(collectionFile, environmentFile, destination);
                            return;
                            
                        case SKIP_VARIABLE_REQUESTS:
                            ui.appendLog("Feature not yet implemented. Please choose another option.");
                            ui.setImportComplete();
                            return;
                    }
                } else {
                    // User cancelled the dialog
                    ui.appendLog("Import cancelled by user.");
                    ui.setImportComplete();
                    return;
                }
            }
        } catch (Exception e) {
            ui.showError("Variable resolution failed: " + e.getMessage());
            ui.setImportComplete();
        }
    }
    
    private void proceedWithDirectImport(File collectionFile, File environmentFile, String destination) {
        // This is the original import logic
        SwingWorker<ImportResult, String> worker = new SwingWorker<ImportResult, String>() {
            @Override
            protected ImportResult doInBackground() throws Exception {
                ImportResult result = new ImportResult();
                
                try {
                    // Parse collection
                    publish("Parsing collection file...");
                    PostmanCollection collection = parser.parseCollection(collectionFile);
                    result.collectionName = collection.info.name;
                    
                    // Parse environment if provided
                    if (environmentFile != null) {
                        publish("Parsing environment file...");
                        PostmanEnvironment environment = parser.parseEnvironment(environmentFile);
                        variableResolver.addEnvironmentVariables(environment);
                    }
                    
                    // Add collection variables
                    variableResolver.addCollectionVariables(collection);
                    
                    // Flatten all requests
                    List<RequestItem> requests = flattenRequests(collection.item, "");
                    result.totalRequests = requests.size();
                    
                    // Process each request
                    for (int i = 0; i < requests.size(); i++) {
                        if (isCancelled()) break;
                        
                        RequestItem item = requests.get(i);
                        try {
                            processRequest(item, destination);
                            result.successCount++;
                            publish("✓ Imported: " + item.name);
                        } catch (Exception e) {
                            result.failedRequestDetails.add(new ImportResult.FailedRequestInfo(
                                item.name, item.path, e.getMessage(), item));
                            result.failedRequests.add(item.name + ": " + e.getMessage());
                            publish("✗ Failed: " + item.name + " - " + e.getMessage());
                        }
                        
                        setProgress((i + 1) * 100 / requests.size());
                    }
                    
                } catch (Exception e) {
                    result.error = e.getMessage();
                    publish("Fatal error: " + e.getMessage());
                }
                
                return result;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    ui.appendLog(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    ImportResult result = get();
                    lastImportResult = result; // Store for retry functionality
                    ui.showImportSummary(result);
                } catch (Exception e) {
                    ui.showError("Import failed: " + e.getMessage());
                }
                ui.setImportComplete();
            }
        };
        
        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                ui.updateProgress((Integer) evt.getNewValue());
            }
        });
        
        ui.setImportInProgress();
        worker.execute();
    }
    
    private List<RequestItem> flattenRequests(List<PostmanCollection.Item> items, String path) {
        List<RequestItem> requests = new ArrayList<>();
        
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
    
    private void processRequest(RequestItem item) throws Exception {
        processRequest(item, "repeater");
    }
    
    private void processRequest(RequestItem item, String destination) throws Exception {
        // Build the HTTP request
        byte[] request = requestBuilder.buildRequest(item.request);
        
        // Extract host and port from URL
        String rawUrl = extractRawUrl(item.request.url);
        if (rawUrl == null) {
            throw new Exception("Unable to extract URL from request");
        }
        String resolvedUrl = variableResolver.resolve(rawUrl);
        HttpUtils.HostInfo hostInfo = HttpUtils.parseUrl(resolvedUrl);
        
        // Generate unique tab name for Repeater
        String tabName = generateUniqueTabName(item.name);
        
        switch (destination.toLowerCase()) {
            case "repeater":
                sendToRepeater(hostInfo, request, tabName);
                break;
                
            case "sitemap":
                sendToSitemap(hostInfo, request, item.name);
                break;
                
            case "both":
                sendToRepeater(hostInfo, request, tabName);
                // Add configurable delay to avoid overwhelming the target
                int delayMs = ui.getDelayMs();
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
                sendToSitemap(hostInfo, request, item.name);
                break;
                
            default:
                sendToRepeater(hostInfo, request, tabName);
                break;
        }
        
        existingTabs.add(tabName);
    }
    
    private void sendToRepeater(HttpUtils.HostInfo hostInfo, byte[] request, String tabName) {
        // Create HTTP service
        burp.api.montoya.http.HttpService httpService = burp.api.montoya.http.HttpService.httpService(
            hostInfo.host,
            hostInfo.port,
            hostInfo.useHttps
        );
        
        // Create HTTP request from raw bytes
        burp.api.montoya.http.message.requests.HttpRequest httpRequest = 
            burp.api.montoya.http.message.requests.HttpRequest.httpRequest(httpService, 
                burp.api.montoya.core.ByteArray.byteArray(request));
        
        // Send to repeater with tab name
        api.repeater().sendToRepeater(httpRequest, tabName);
    }
    
    private void sendToSitemap(HttpUtils.HostInfo hostInfo, byte[] request, String requestName) throws Exception {
        // Create HTTP service
        burp.api.montoya.http.HttpService httpService = burp.api.montoya.http.HttpService.httpService(
            hostInfo.host,
            hostInfo.port,
            hostInfo.useHttps
        );
        
        // Debug logging
        if (debugMode) {
            api.logging().logToOutput("DEBUG: Creating sitemap request for " + requestName);
            api.logging().logToOutput("DEBUG: Host: " + hostInfo.host + ", Port: " + hostInfo.port + ", HTTPS: " + hostInfo.useHttps);
        }
        
        try {
            // Make actual HTTP request to populate sitemap
            if (debugMode) {
                api.logging().logToOutput("DEBUG: Making HTTP request to " + hostInfo.host);
            }
            burp.api.montoya.http.message.requests.HttpRequest httpRequest = 
                burp.api.montoya.http.message.requests.HttpRequest.httpRequest(httpService, 
                    burp.api.montoya.core.ByteArray.byteArray(request));
            burp.api.montoya.http.message.HttpRequestResponse response = api.http().sendRequest(httpRequest);
            
            if (response != null) {
                if (debugMode) {
                    api.logging().logToOutput("DEBUG: Received response for " + requestName);
                }
                
                if (response.response() != null) {
                    // Add to sitemap through HTTP history
                    api.siteMap().add(response);
                    short statusCode = response.response().statusCode();
                    api.logging().logToOutput("Sitemap: " + requestName + " -> HTTP " + statusCode);
                    
                    if (debugMode) {
                        api.logging().logToOutput("DEBUG: Added " + requestName + " to sitemap");
                        // Also log the URL for verification
                        String url = response.request().url();
                        api.logging().logToOutput("DEBUG: Sitemap URL: " + url);
                    }
                } else {
                    api.logging().logToOutput("DEBUG: Response was null for " + requestName);
                }
            } else {
                api.logging().logToOutput("DEBUG: No response received for " + requestName);
            }
            
            // Add configurable delay to be respectful to the target server
            int delayMs = ui.getDelayMs();
            if (delayMs > 0) {
                Thread.sleep(delayMs);
            }
            
        } catch (Exception e) {
            // Check for specific network error types and provide clean error messages
            String errorMsg;
            if (e.getCause() instanceof java.net.UnknownHostException || 
                e.getMessage().contains("UnknownHostException")) {
                // Extract hostname from the exception for cleaner error message
                String hostname = extractHostnameFromError(e.getMessage());
                if (hostname != null) {
                    errorMsg = "DNS resolution failed for hostname: " + hostname + " (VPN/internal network required?)";
                } else {
                    errorMsg = "Hostname not accessible - check network connectivity or VPN connection";
                }
                api.logging().logToError("Sitemap connectivity issue for " + requestName + ": " + errorMsg);
            } else if (e.getCause() instanceof java.net.ConnectException || 
                       e.getMessage().contains("ConnectException")) {
                errorMsg = "Connection refused or timeout - service may be down or firewalled";
                api.logging().logToError("Sitemap connection failed for " + requestName + ": " + errorMsg);
            } else {
                errorMsg = "Request failed: " + extractCleanErrorMessage(e);
                api.logging().logToError("Failed to send " + requestName + " to sitemap: " + errorMsg);
            }
            throw new Exception(errorMsg);
        }
    }
    
    private String generateUniqueTabName(String baseName) {
        String tabName = baseName;
        int counter = 1;
        
        while (existingTabs.contains(tabName)) {
            tabName = baseName + " (" + counter++ + ")";
        }
        
        return tabName;
    }
    
    private String extractRawUrl(Object urlData) {
        if (urlData == null) return null;
        
        // Handle string URL format
        if (urlData instanceof String) {
            return (String) urlData;
        }
        
        // Handle Url object format
        try {
            Gson gson = new Gson();
            JsonElement element = gson.toJsonTree(urlData);
            if (element.isJsonObject()) {
                JsonObject urlObject = element.getAsJsonObject();
                if (urlObject.has("raw")) {
                    return urlObject.get("raw").getAsString();
                }
            }
        } catch (Exception e) {
            // If parsing fails, return null
        }
        
        return null;
    }
    
    private String extractHostnameFromError(String errorMessage) {
        // Try to extract hostname from UnknownHostException message
        // Example: "java.lang.RuntimeException: java.net.UnknownHostException: hostname.example.com"
        if (errorMessage.contains("UnknownHostException")) {
            String[] parts = errorMessage.split("UnknownHostException:");
            if (parts.length > 1) {
                String hostname = parts[1].trim();
                // Remove any trailing text that might be part of the exception
                int spaceIndex = hostname.indexOf(' ');
                if (spaceIndex > 0) {
                    hostname = hostname.substring(0, spaceIndex);
                }
                return hostname;
            }
        }
        return null;
    }
    
    private String extractCleanErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return e.getClass().getSimpleName();
        }
        
        // Clean up common exception chain patterns
        if (message.startsWith("java.lang.RuntimeException:")) {
            message = message.substring("java.lang.RuntimeException:".length()).trim();
        }
        if (message.startsWith("java.net.")) {
            int colonIndex = message.indexOf(':');
            if (colonIndex > 0) {
                message = message.substring(colonIndex + 1).trim();
            }
        }
        
        return message.isEmpty() ? e.getClass().getSimpleName() : message;
    }
    
    private boolean isGraphQLRequest(PostmanCollection.Request request) {
        // Check if this is a GraphQL request
        if (request.body == null || request.body.raw == null) {
            return false;
        }
        
        // Check URL for /graphql endpoint
        String rawUrl = extractRawUrl(request.url);
        boolean hasGraphQLEndpoint = rawUrl != null && rawUrl.toLowerCase().contains("/graphql");
        
        // Check body for GraphQL query patterns
        String body = request.body.raw.toLowerCase().trim();
        boolean hasGraphQLQuery = body.contains("\"query\"") || 
                                 body.contains("\"mutation\"") || 
                                 body.contains("\"subscription\"") ||
                                 body.startsWith("query ") ||
                                 body.startsWith("mutation ") ||
                                 body.startsWith("subscription ");
        
        return hasGraphQLEndpoint || hasGraphQLQuery;
    }
    
    private String extractGraphQLOperation(String rawBody) {
        if (rawBody == null) return null;
        
        try {
            // Try to parse as JSON to extract operation name
            Gson gson = new Gson();
            JsonElement element = gson.fromJson(rawBody, JsonElement.class);
            
            if (element.isJsonObject()) {
                JsonObject queryObj = element.getAsJsonObject();
                if (queryObj.has("query")) {
                    String query = queryObj.get("query").getAsString();
                    return extractOperationFromQuery(query);
                }
            }
        } catch (Exception e) {
            // If JSON parsing fails, try text-based extraction
            return extractOperationFromQuery(rawBody);
        }
        
        return null;
    }
    
    private String extractOperationFromQuery(String query) {
        if (query == null) return null;
        
        // Look for operation name patterns like "query GetUser" or "mutation CreateUser"
        String[] patterns = {"query ", "mutation ", "subscription "};
        
        for (String pattern : patterns) {
            int index = query.toLowerCase().indexOf(pattern);
            if (index >= 0) {
                String afterPattern = query.substring(index + pattern.length()).trim();
                
                // Extract operation name (first word after operation type)
                String[] words = afterPattern.split("[\\s\\(\\{]");
                if (words.length > 0 && !words[0].trim().isEmpty()) {
                    String operationType = pattern.trim();
                    String operationName = words[0].trim();
                    return operationType + " " + operationName;
                }
                
                // If no name found, just return the operation type
                return pattern.trim();
            }
        }
        
        return null;
    }
    
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
}
