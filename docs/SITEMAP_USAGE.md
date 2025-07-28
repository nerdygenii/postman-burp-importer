# Sitemap Integration Guide

The Sitemap integration feature allows you to make live HTTP requests that populate Burp Suite's Sitemap, enabling comprehensive attack surface discovery and automatic scanner integration.

## 🌐 Overview

### What is Sitemap Mode?
Sitemap mode makes actual HTTP requests to your target servers and populates Burp's Sitemap with real responses, status codes, and content. This creates a complete picture of your application's attack surface.

### Benefits
- ✅ **Real Response Data**: See actual server responses, not just request templates
- ✅ **Scanner Integration**: Automatically feeds Burp Scanner for vulnerability discovery
- ✅ **Attack Surface Mapping**: Complete overview of accessible endpoints
- ✅ **Status Code Analysis**: Identify working vs broken endpoints
- ✅ **Content Discovery**: Find hidden functionality through response analysis

## 🚀 Features

### Live Request Processing
- **Actual HTTP Requests**: Makes real connections to target servers
- **Response Capture**: Records full HTTP responses with headers and body
- **Status Tracking**: Displays real status codes (200, 404, 500, etc.)
- **Performance Metrics**: Shows response times and content sizes
- **Error Handling**: Graceful handling of network timeouts and failures

### Intelligent Rate Limiting
- **Configurable Delays**: Default 200ms between requests
- **Respectful Testing**: Prevents overwhelming target servers
- **Customizable Timing**: Adjust delays based on target service requirements
- **Burst Protection**: Prevents accidental DDoS scenarios

### Scanner Integration
- **Automatic Discovery**: Burp Scanner automatically discovers imported endpoints
- **Real Target Data**: Scanner works with actual response data
- **Comprehensive Coverage**: All imported endpoints available for scanning
- **Vulnerability Detection**: Immediate security testing capability

## 🎯 Use Cases

### Security Assessment Workflows

#### API Security Testing
```
1. Import Postman API collection → Sitemap mode
2. Live requests populate Sitemap with real endpoints
3. Burp Scanner automatically discovers attack surface
4. Run targeted scans on discovered endpoints
5. Analyze vulnerabilities found in real responses
```

#### Attack Surface Discovery
```
1. Import comprehensive application collection
2. Sitemap populated with all accessible endpoints
3. Analyze response patterns and error conditions
4. Identify hidden functionality and debug endpoints
5. Discover security-relevant functionality
```

#### Vulnerability Assessment
```
1. Import known good requests → Sitemap mode
2. Establish baseline of working functionality
3. Use Scanner to test for common vulnerabilities
4. Compare scanner results with known good states
5. Identify security issues in real application data
```

## 🔧 Configuration Options

### Request Destinations
- **Sitemap Only**: Requests appear only in Sitemap (no Repeater tabs)
- **Both Mode**: Requests appear in both Sitemap and Repeater
- **Selective Import**: Choose specific requests for Sitemap population

### Rate Limiting Settings
- **Default**: 200ms delay between requests
- **Conservative**: 500ms+ for rate-limited APIs
- **Aggressive**: 100ms for internal testing environments
- **Custom**: User-defined delays based on requirements

### Authentication Handling
- **Bearer Tokens**: Automatic inclusion in Authorization headers
- **API Keys**: Support for header-based and query-based API keys
- **Basic Auth**: Username/password authentication
- **Custom Headers**: Any authentication scheme via custom headers

## 📊 Response Analysis

### Status Code Patterns
- **2xx Success**: Working endpoints ready for testing
- **3xx Redirects**: Follow redirect chains for complete coverage
- **4xx Client Errors**: Identify authentication and authorization issues
- **5xx Server Errors**: Discover potential error handling vulnerabilities

### Content Analysis
- **Response Size**: Identify unusually large or small responses
- **Content Types**: Discover API endpoints vs web pages vs files
- **Error Messages**: Analyze error responses for information disclosure
- **Headers**: Review security headers and server information

### Performance Metrics
- **Response Times**: Identify slow endpoints that might indicate processing issues
- **Timeout Patterns**: Discover endpoints with unusual timing characteristics
- **Size Variations**: Find endpoints with unexpected response sizes

## 🛡️ Security Considerations

### Responsible Testing
- **Rate Limiting**: Always respect target server performance
- **Authentication**: Use appropriate test credentials, not production
- **Scope Management**: Only test endpoints within authorized scope
- **Data Sensitivity**: Be aware of test data in requests and responses

### Network Security
- **Proxy Awareness**: All requests go through Burp's proxy
- **SSL/TLS**: Proper handling of HTTPS endpoints
- **Certificate Validation**: Respects Burp's certificate handling settings
- **Network Isolation**: Consider testing in isolated network environments

### Data Protection
- **Sensitive Information**: Be careful with API keys and tokens in live requests
- **Response Data**: Review response content for sensitive information
- **Logging**: Understand that all requests/responses are logged in Burp
- **Cleanup**: Clear sensitive data from Burp project after testing

## 🔍 Troubleshooting

### Common Issues

#### Requests Not Appearing in Sitemap
**Problem**: Live requests succeed but don't populate Sitemap

**Solutions**:
- Verify "Sitemap" or "Both" mode is selected
- Check Burp's Target scope settings
- Ensure requests are in-scope for current project
- Review Burp's display filters in Sitemap

#### Authentication Failures
**Problem**: All requests return 401/403 errors

**Solutions**:
- Verify authentication tokens are current and valid
- Check token format and header configuration
- Ensure variables are properly resolved
- Test authentication manually in Repeater first

#### Rate Limiting Issues
**Problem**: Many requests fail with 429 (Too Many Requests)

**Solutions**:
- Increase delay between requests (try 500ms or 1000ms)
- Import smaller batches of requests
- Check API documentation for rate limit specifications
- Use "Retry Failed Requests" after rate limit reset

#### Network Connectivity Problems
**Problem**: Requests fail with connection timeouts

**Solutions**:
- Verify network connectivity to target servers
- Check VPN or proxy configuration
- Ensure target URLs are accessible from current network
- Test connectivity outside Burp Suite first

### Performance Optimization

#### Large Collections
- **Batch Processing**: Import large collections in smaller chunks
- **Selective Import**: Use preview to select only necessary requests
- **Increased Delays**: Use longer delays for large imports
- **Monitoring**: Watch system resources during large imports

#### Memory Management
- **Response Size**: Be aware of large response sizes affecting memory
- **Collection Cleanup**: Remove unnecessary requests before import
- **Burp Settings**: Adjust Burp's memory settings for large projects
- **Progress Monitoring**: Watch import progress for stuck requests

## 📈 Best Practices

### Planning Your Import
1. **Scope Definition**: Clearly define what endpoints should be tested
2. **Authentication Preparation**: Ensure valid credentials are available
3. **Rate Limit Research**: Understand target API rate limits
4. **Network Planning**: Verify connectivity and proxy settings

### Execution Strategy
1. **Start Small**: Test with a few requests first
2. **Monitor Progress**: Watch import progress and error rates
3. **Adjust Settings**: Modify delays based on observed behavior
4. **Error Analysis**: Review failed requests for patterns

### Post-Import Analysis
1. **Response Review**: Analyze response patterns and status codes
2. **Error Investigation**: Investigate failed requests for security insights
3. **Scanner Preparation**: Verify endpoints are ready for scanner
4. **Documentation**: Document findings and configuration for future use

### Integration with Security Testing
1. **Baseline Establishment**: Use Sitemap data as security testing baseline
2. **Scanner Configuration**: Configure Burp Scanner based on discovered endpoints
3. **Manual Testing**: Use Repeater tabs for detailed manual testing
4. **Reporting**: Include Sitemap analysis in security assessment reports

---

For more information about other features, see our [main documentation](../README.md).
