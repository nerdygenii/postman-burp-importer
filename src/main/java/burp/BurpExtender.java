/*
 * Postman Collection Importer for Burp Suite
 * 
 * A powerful Burp Suite extension that imports Postman collections and converts them
 * into Burp Repeater tabs and/or Sitemap entries for comprehensive security testing.
 * 
 * Author: Abdulrahman Oyekunle
 * GitHub: https://github.com/nerdygenii/postman-burp-importer
 * LinkedIn: https://linkedin.com/in/abdulrahman-oyekunle-3a7906179
 * 
 * Features:
 * - Postman Collection v2.0 & v2.1 support
 * - Environment file support with variable resolution
 * - Smart variable detection and guided resolution
 * - Multiple destinations: Repeater, Sitemap, or both
 * - GraphQL support with operation extraction
 * - Smart retry system for failed requests
 * - Intelligent variable suggestions
 * 
 * License: MIT
 * Version: 1.0.0
 */

package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import javax.swing.*;
import java.awt.Component;

public class BurpExtender implements BurpExtension {
    private MontoyaApi api;
    private PostmanImporter importer;
    private JPanel mainPanel;
    
    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        
        api.extension().setName("Postman Collection Importer");
        
        // Print extension info and author details
        api.logging().logToOutput("=================================================");
        api.logging().logToOutput("Postman Collection Importer v1.0.1");
        api.logging().logToOutput("Author: Abdulrahman Oyekunle");
        api.logging().logToOutput("GitHub: https://github.com/nerdygenii/postman-burp-importer");
        api.logging().logToOutput("=================================================");
        api.logging().logToOutput("Extension loaded successfully!");
        api.logging().logToOutput("Features: Variable resolution, GraphQL support, Smart retry, Multiple destination(sends to repeater or sitemap or both)");
        api.logging().logToOutput("");
        
        SwingUtilities.invokeLater(() -> {
            importer = new PostmanImporter(api);
            mainPanel = importer.getMainPanel();
            api.userInterface().registerSuiteTab("Postman Importer", mainPanel);
        });
    }
}
