package burp.utils;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for creating properly formatted UI components.
 * This handles the fact that HTML rendering may not work properly in all Burp Suite environments.
 */
public class UIHelpers {
    
    /**
     * Creates a formatted label with manual styling instead of HTML.
     * Use this when you need italics, colors, or specific font sizes.
     */
    public static JLabel createFormattedLabel(String text, boolean italic, boolean bold, float fontSize, Color color) {
        JLabel label = new JLabel(text);
        
        // Build font style
        int style = Font.PLAIN;
        if (italic) style |= Font.ITALIC;
        if (bold) style |= Font.BOLD;
        
        label.setFont(label.getFont().deriveFont(style, fontSize));
        
        if (color != null) {
            label.setForeground(color);
        }
        
        return label;
    }
    
    /**
     * Creates an italic gray label - commonly used for hints and descriptions
     */
    public static JLabel createHintLabel(String text, float fontSize) {
        return createFormattedLabel(text, true, false, fontSize, Color.GRAY);
    }
    
    /**
     * Creates a bold title label
     */
    public static JLabel createTitleLabel(String text, float fontSize) {
        return createFormattedLabel(text, false, true, fontSize, null);
    }
    
    /**
     * Creates a multi-line text area that looks like a label but supports word wrapping.
     * Use this for longer descriptive text.
     */
    public static JTextArea createMultiLineLabel(String text, float fontSize) {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setFont(textArea.getFont().deriveFont(fontSize));
        textArea.setForeground(UIManager.getColor("Label.foreground"));
        textArea.setBorder(null);
        return textArea;
    }
    
    /**
     * If HTML rendering is needed in the future, this method attempts to create
     * a component that can properly render HTML content.
     */
    public static JComponent createHTMLLabel(String htmlText) {
        // Try JLabel first (simplest)
        JLabel label = new JLabel(htmlText);
        
        // If we detect that HTML isn't rendering (text starts with <html> but looks raw),
        // we could fall back to JEditorPane here in the future
        
        return label;
    }
}
