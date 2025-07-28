package burp.test;

import burp.models.RequestPreview;

/**
 * Simple test to verify RequestPreview functionality
 */
public class RequestPreviewTest {
    
    public static void main(String[] args) {
        System.out.println("Testing RequestPreview class...");
        
        // Create test request preview
        RequestPreview preview = new RequestPreview(
            "Login Request",
            "Authentication/Login Request", 
            "POST",
            "https://api.example.com/auth/login",
            "User login endpoint",
            true,  // has auth
            true,  // has headers  
            true   // has body
        );
        
        // Test getters
        assert preview.getName().equals("Login Request");
        assert preview.getPath().equals("Authentication/Login Request");
        assert preview.getMethod().equals("POST");
        assert preview.getUrl().equals("https://api.example.com/auth/login");
        assert preview.hasAuth() == true;
        assert preview.hasHeaders() == true;
        assert preview.hasBody() == true;
        assert preview.isSelected() == true; // Default selected
        
        // Test selection toggle
        preview.setSelected(false);
        assert preview.isSelected() == false;
        
        preview.setSelected(true);
        assert preview.isSelected() == true;
        
        // Test toString
        String expected = "[POST] Login Request - https://api.example.com/auth/login";
        assert preview.toString().equals(expected);
        
        System.out.println("✓ All tests passed!");
        System.out.println("Preview: " + preview.toString());
        System.out.println("Path: " + preview.getPath());
        System.out.println("Features: Auth=" + preview.hasAuth() + 
                          ", Headers=" + preview.hasHeaders() + 
                          ", Body=" + preview.hasBody());
    }
}
