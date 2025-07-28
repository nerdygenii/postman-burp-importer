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

import javax.swing.*;
import java.awt.Component;  // Added missing import

public class BurpExtender implements IBurpExtender, ITab {
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private PostmanImporter importer;
    private JPanel mainPanel;
    
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        
        callbacks.setExtensionName("Postman Collection Importer");
        
        // Print extension info and author details
        callbacks.printOutput("=================================================");
        callbacks.printOutput("Postman Collection Importer v1.0.0");
        callbacks.printOutput("Author: Abdulrahman Oyekunle");
        callbacks.printOutput("GitHub: https://github.com/nerdygenii/postman-burp-importer");
        callbacks.printOutput("=================================================");
        callbacks.printOutput("Extension loaded successfully!");
        callbacks.printOutput("Features: Variable resolution, GraphQL support, Smart retry, Multiple destination(sends to repeater or sitemap or both)");
        callbacks.printOutput("");
        
        SwingUtilities.invokeLater(() -> {
            importer = new PostmanImporter(callbacks, helpers);
            mainPanel = importer.getMainPanel();
            callbacks.addSuiteTab(this);
        });
    }
    
    @Override
    public String getTabCaption() {
        return "Postman Importer";
    }
    
    @Override
    public Component getUiComponent() {
        return mainPanel;
    }
}
