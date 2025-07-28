# Troubleshooting Guide

This comprehensive guide helps you resolve common issues when using the Postman Collection Importer for Burp Suite.

## 🚨 Quick Diagnosis

### Import Completely Fails
- **Check Collection Format**: Ensure v2.0 or v2.1 Postman collection
- **Verify File Integrity**: Make sure JSON file is not corrupted
- **Review Error Messages**: Check Burp Suite output for specific errors
- **Test with Simple Collection**: Try with a basic collection first

### Some Requests Fail
- **Variable Issues**: Use "Preview Requests" to check variable status
- **Authentication Problems**: Verify tokens and credentials are valid
- **Network Connectivity**: Check VPN, proxy, and firewall settings
- **Rate Limiting**: Look for 429 status codes in failed requests

### Variables Not Resolving
- **Environment File**: Check if environment file matches collection needs
- **Variable Syntax**: Ensure correct `{{variable_name}}` format
- **Case Sensitivity**: Verify exact spelling and capitalization
- **Scope Issues**: Confirm variables are defined in accessible scope

## 🔧 Installation & Setup Issues

### Extension Not Loading

#### JAR File Problems
**Problem**: Extension fails to load in Burp Suite

**Solutions**:
- Verify JAR file is complete and not corrupted
- Check Java version compatibility (requires Java 8+)
- Try building from source: `mvn clean package`
- Ensure using the `-jar-with-dependencies.jar` file

#### Burp Suite Configuration
**Problem**: Extension loads but tab doesn't appear

**Solutions**:
- Restart Burp Suite after loading extension
- Check Extensions tab for error messages
- Verify Burp Suite version compatibility
- Look for Java console errors in Burp Suite

#### Permission Issues
**Problem**: Cannot load extension due to permissions

**Solutions**:
- Run Burp Suite with appropriate permissions
- Check file system permissions on JAR file
- Verify Burp Suite can access extension directory
- Try loading from different location

### Missing Dependencies

#### Maven Build Issues
**Problem**: Build fails with dependency errors

**Solutions**:
```bash
# Clear Maven cache and rebuild
mvn clean package
mvn dependency:resolve
mvn clean compile package
```

#### Java Version Problems
**Problem**: Compilation or runtime errors related to Java version

**Solutions**:
- Verify Java 8+ is installed: `java -version`
- Set JAVA_HOME environment variable
- Use correct Maven Java version: `mvn -version`
- Consider using Java 11 for better compatibility

## 📂 Collection Import Issues

### File Format Problems

#### Invalid Collection Format
**Problem**: "Invalid collection format" or parsing errors

**Solutions**:
- Export collection from Postman in v2.1 format
- Validate JSON syntax using online JSON validator
- Check for special characters or encoding issues
- Try exporting collection again from Postman

#### Large Collection Issues
**Problem**: Import fails or becomes very slow with large collections

**Solutions**:
- Split large collections into smaller files
- Use selective import with preview feature
- Increase Burp Suite memory allocation
- Import in smaller batches using preview selection

#### Nested Folder Problems
**Problem**: Deep folder structures cause import issues

**Solutions**:
- Flatten collection structure in Postman
- Test with simpler folder organization
- Check for circular references in folders
- Verify folder names don't contain special characters

### Variable Resolution Problems

#### Environment File Issues
**Problem**: Environment file not resolving variables correctly

**Solutions**:
- Verify environment file is valid JSON
- Check variable names match exactly (case-sensitive)
- Ensure environment file exported from correct Postman environment
- Try manual variable entry as alternative

#### Variable Syntax Problems
**Problem**: Variables not detected or resolved

**Solutions**:
- Use correct syntax: `{{variable_name}}` (double curly braces)
- Avoid spaces in variable names
- Check for typos in variable references
- Ensure variables are not commented out

#### Circular Variable References
**Problem**: Variables reference each other in loops

**Solutions**:
- Review variable definitions for circular dependencies
- Simplify variable structure
- Use base values instead of variable references
- Define variables in correct dependency order

## 🌐 Network & Connectivity Issues

### Request Failures

#### Connection Timeouts
**Problem**: Many requests fail with timeout errors

**Solutions**:
- Check network connectivity to target hosts
- Verify VPN connection if required
- Test target URLs manually in browser
- Increase timeout settings if possible

#### Authentication Failures
**Problem**: All requests return 401 or 403 errors

**Solutions**:
- Verify authentication tokens are current
- Check token format and expiration
- Test authentication manually in Repeater
- Ensure correct authentication method is used

#### Rate Limiting Issues
**Problem**: Requests fail with 429 (Too Many Requests) errors

**Solutions**:
- Increase delay between requests (try 500ms or 1000ms)
- Import smaller batches of requests
- Use "Retry Failed Requests" after rate limit reset
- Check API documentation for rate limit specifications

### Proxy & SSL Issues

#### Proxy Configuration Problems
**Problem**: Requests fail due to proxy settings

**Solutions**:
- Verify Burp Suite proxy settings
- Check upstream proxy configuration
- Test direct connection without proxy
- Ensure proxy authentication is configured

#### SSL Certificate Issues
**Problem**: HTTPS requests fail with certificate errors

**Solutions**:
- Install Burp Suite CA certificate
- Check target certificate validity
- Verify SSL/TLS settings in Burp Suite
- Test with HTTP endpoints first

## 🎯 Feature-Specific Issues

### GraphQL Problems

#### GraphQL Not Detected
**Problem**: GraphQL requests appear as regular HTTP requests

**Solutions**:
- Ensure request body contains valid GraphQL syntax
- Check Content-Type is `application/json`
- Verify query starts with `query`, `mutation`, or `subscription`
- Test with simpler GraphQL query first

#### GraphQL Variable Issues
**Problem**: GraphQL variables not resolving correctly

**Solutions**:
- Check both query variables and Postman variables
- Ensure proper JSON structure in variables section
- Verify variable types match GraphQL schema
- Test variables individually

### Sitemap Integration Issues

#### Requests Not Appearing in Sitemap
**Problem**: Live requests don't populate Sitemap

**Solutions**:
- Verify "Sitemap" or "Both" mode is selected
- Check Burp's Target scope settings
- Ensure requests are in-scope for current project
- Review Burp's display filters in Sitemap

#### Sitemap Performance Issues
**Problem**: Sitemap mode is very slow or resource-intensive

**Solutions**:
- Reduce number of requests being imported
- Increase delays between requests
- Close unnecessary Burp Suite tools
- Monitor system resources during import

### Variable Intelligence Issues

#### Variable Detection Not Working
**Problem**: System doesn't detect unresolved variables

**Solutions**:
- Ensure variables use correct `{{}}` syntax
- Check for typos in variable names
- Verify variables aren't commented out
- Try manual variable entry as backup

#### Suggestions Not Helpful
**Problem**: AI suggestions don't match your needs

**Solutions**:
- Ignore suggestions and enter custom values
- Use suggested patterns as starting point
- Try multiple variable resolution approaches
- Use environment file for better accuracy

## 🔄 Retry & Recovery Issues

### Retry Feature Problems

#### Retry Button Not Appearing
**Problem**: No retry option after import failures

**Solutions**:
- Ensure import actually had failures (not just warnings)
- Verify import completed (wasn't cancelled)
- Check error messages in import log
- Try importing smaller collection to test

#### Retry Still Failing
**Problem**: Requests continue to fail after retry

**Solutions**:
- Check network connectivity and VPN status
- Verify authentication tokens are still valid
- Review specific error messages
- Wait longer between retry attempts

### Performance & Memory Issues

#### Out of Memory Errors
**Problem**: Java heap space or memory errors

**Solutions**:
- Increase Burp Suite memory allocation
- Close unnecessary applications
- Process smaller collections
- Restart Burp Suite to clear memory

#### Slow Performance
**Problem**: Import or preview very slow

**Solutions**:
- Reduce collection size
- Use selective import instead of full collection
- Close other resource-intensive tools
- Check system resources (CPU, memory, disk)

## 🛠️ Advanced Troubleshooting

### Debug Mode

#### Enabling Debug Logging
For advanced troubleshooting, enable debug mode:

1. Open `RequestBuilder.java`
2. Change `debugMode = false` to `debugMode = true`
3. Rebuild: `mvn clean package`
4. Reload extension in Burp Suite
5. Check Burp Suite output for detailed logs

#### Interpreting Debug Output
Debug logs show:
- Variable resolution steps
- Host header construction
- Authentication processing
- Request building details
- Error stack traces

### Log Analysis

#### Burp Suite Console
Check Extensions → Postman Importer → Output for:
- Extension loading messages
- Import progress information
- Error details and stack traces
- Variable resolution information

#### System Logs
For system-level issues:
- Check Java console in Burp Suite
- Review system memory and CPU usage
- Monitor network connectivity
- Check firewall and antivirus logs

### Recovery Strategies

#### Partial Recovery
When imports partially fail:
1. Note which requests succeeded
2. Identify patterns in failures
3. Address root causes (network, auth, variables)
4. Use retry feature for failed requests
5. Consider splitting problematic requests

#### Complete Reset
For persistent issues:
1. Restart Burp Suite completely
2. Clear Burp Suite project data
3. Test with minimal collection
4. Rebuild extension from source
5. Check for extension updates

## 📞 Getting Help

### Before Reporting Issues
1. **Test with Simple Collection**: Verify basic functionality works
2. **Check Documentation**: Review relevant feature guides
3. **Enable Debug Mode**: Gather detailed error information
4. **Document Steps**: Record exact steps to reproduce issue
5. **Collect Logs**: Save error messages and debug output

### Reporting Problems
When reporting issues, include:
- Postman collection version (v2.0/v2.1)
- Burp Suite version and edition
- Java version information
- Exact error messages
- Steps to reproduce
- Debug logs if available

### Community Resources
- GitHub Issues: Report bugs and request features
- Documentation: Comprehensive guides for all features
- Examples: Sample collections for testing
- Updates: Check for new releases and fixes

---

For more information about specific features, see our [main documentation](../README.md).
