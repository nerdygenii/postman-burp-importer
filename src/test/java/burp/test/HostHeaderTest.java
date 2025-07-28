package burp.test;

import burp.utils.HttpUtils;

public class HostHeaderTest {
    public static void main(String[] args) {
        // Test URL parsing
        String testUrl1 = "https://hello.com";
        String testUrl2 = "http://api.example.com:8080";
        String testUrl3 = "hello.com";
        
        System.out.println("Testing URL parsing:");
        System.out.println("Input: " + testUrl1);
        HttpUtils.HostInfo host1 = HttpUtils.parseUrl(testUrl1);
        System.out.println("Host: " + host1.host);
        System.out.println();
        
        System.out.println("Input: " + testUrl2);
        HttpUtils.HostInfo host2 = HttpUtils.parseUrl(testUrl2);
        System.out.println("Host: " + host2.host);
        System.out.println();
        
        System.out.println("Input: " + testUrl3);
        HttpUtils.HostInfo host3 = HttpUtils.parseUrl(testUrl3);
        System.out.println("Host: " + host3.host);
    }
}
