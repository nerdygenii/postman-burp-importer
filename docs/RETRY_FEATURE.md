# Smart Retry System

The Smart Retry System provides intelligent handling of failed requests during import, allowing you to quickly retry failed operations without re-processing successful ones.

## 🚀 Features

### Intelligent Retry Logic
- ✅ **Selective Processing**: Only retries requests that actually failed
- ✅ **Preserved Success**: Keeps successful requests from original import
- ✅ **Merged Results**: Combines original and retry results seamlessly
- ✅ **One-Click Operation**: Simple button press to retry all failures
- ✅ **Real-Time Feedback**: Live progress tracking during retry

### Smart Detection
- **Network Issues**: Automatically detects connection timeouts and failures
- **VPN Problems**: Identifies connectivity issues requiring retry
- **Rate Limiting**: Recognizes temporary rate limit responses
- **Service Outages**: Handles temporary service unavailability

## 🔧 How It Works

### Automatic Failure Tracking
When requests fail during import, the system automatically:
1. **Records Failure Details**: Saves request name, path, and error message
2. **Preserves Request Data**: Keeps original request configuration for retry
3. **Updates UI**: Shows "Retry Failed Requests" button when failures occur
4. **Provides Summary**: Displays detailed failure information in logs

### Retry Process
When you click "Retry Failed Requests":
1. **Analyzes Failures**: Reviews failed requests from last import
2. **Attempts Retry**: Re-processes only the failed requests
3. **Merges Results**: Combines new successes with original results
4. **Updates Summary**: Shows final combined statistics

## 📋 Common Retry Scenarios

### Network Connectivity Issues
**Scenario**: VPN disconnection during import
```
Import collection → 15/20 requests succeed → 5 fail (network timeout)
→ Connect VPN → Click "Retry Failed Requests" → 5/5 succeed
→ Final result: 20/20 successful
```

### Rate Limiting
**Scenario**: API rate limits exceeded
```
Import collection → 18/25 requests succeed → 7 fail (rate limited)
→ Wait for rate limit reset → Click "Retry Failed Requests" → 7/7 succeed
→ Final result: 25/25 successful
```

### Service Outages
**Scenario**: Temporary service unavailability
```
Import collection → 12/30 requests succeed → 18 fail (service unavailable)
→ Service restored → Click "Retry Failed Requests" → 18/18 succeed
→ Final result: 30/30 successful
```

## 🎯 Usage Guide

### Basic Retry Workflow
1. **Perform Initial Import**: Import your collection normally
2. **Check Results**: Review import summary for failures
3. **Fix Root Cause**: Address network, VPN, or service issues
4. **Click Retry Button**: Use "Retry Failed Requests" button
5. **Verify Success**: Check final combined results

### Retry Button States
- **Hidden**: No failures detected, all requests successful
- **Enabled**: Failures detected and ready for retry
- **Disabled**: Retry in progress or no collection loaded

## 🔍 Troubleshooting

### Retry Button Not Appearing
**Problem**: No retry button visible after import failures

**Solutions**:
- Check that import actually had failures (not just warnings)
- Verify import completed (wasn't cancelled mid-process)
- Look for error messages in import log
- Try importing a smaller collection to test

### Retry Still Failing
**Problem**: Requests continue to fail even after retry

**Solutions**:
- **Check Network**: Verify internet connectivity and VPN status
- **Test Authentication**: Ensure tokens/credentials are still valid
- **Verify Endpoints**: Check if service URLs are accessible
- **Review Rate Limits**: Wait longer between retry attempts
- **Check Logs**: Look for specific error messages in Burp Suite logs

### Partial Retry Success
**Problem**: Some requests succeed in retry, others still fail

**Solutions**:
- **Repeat Process**: Multiple retry attempts may be needed
- **Check Individual Failures**: Review specific error messages
- **Split Collection**: Try importing problematic requests separately
- **Update Variables**: Ensure all variables are properly resolved

## 📊 Integration with Destinations

### Repeater Mode
- Failed requests don't create tabs
- Retry creates tabs only for newly successful requests
- Original successful tabs remain unchanged
- Clean separation between original and retry results

### Sitemap Mode
- Failed requests don't populate Sitemap
- Retry makes live HTTP requests for failed operations only
- Original successful requests remain in Sitemap
- Comprehensive attack surface after successful retry

### Both Mode
- Combines benefits of both retry approaches
- Failed requests don't appear in either Repeater or Sitemap
- Retry populates both destinations for newly successful requests
- Complete coverage after successful retry

## 🛡️ Security Considerations

### Rate Limiting Respect
- Retry includes same rate limiting as original import (200ms delays)
- Doesn't overwhelm target services with rapid retry attempts
- Maintains responsible testing practices

### Authentication Handling
- Preserves original authentication configurations
- Retries use same credentials as original requests
- Handles token expiration gracefully

### Error Information
- Detailed error logging for security analysis
- Helps identify potential security controls (rate limiting, IP blocking)
- Assists in understanding target service behavior

## 📈 Performance Benefits

### Efficiency Gains
- **Time Saving**: No need to re-import entire collection
- **Bandwidth Conservation**: Only retry failed requests
- **Resource Optimization**: Preserves successful work
- **User Experience**: Seamless recovery from failures

### Workflow Integration
- **Minimal Disruption**: Continue testing with successful requests while addressing failures
- **Incremental Progress**: Build complete request coverage over multiple retry cycles
- **Flexible Timing**: Retry when convenient or when issues are resolved

## 💡 Best Practices

### Proactive Preparation
1. **Check Connectivity**: Verify network and VPN before large imports
2. **Test Authentication**: Validate credentials with small collection first
3. **Review Rate Limits**: Understand target service limitations
4. **Plan for Failures**: Expect some failures in large collections

### Effective Retry Strategy
1. **Identify Root Cause**: Don't retry without addressing underlying issue
2. **Wait Appropriately**: Allow time for rate limits or service recovery
3. **Monitor Progress**: Watch retry progress and logs for patterns
4. **Multiple Attempts**: Some issues may require several retry cycles

### Troubleshooting Approach
1. **Start Small**: Test retry with small number of failed requests
2. **Check Logs**: Review detailed error messages for insights
3. **Isolate Issues**: Identify if failures are systematic or random
4. **Escalate Gradually**: Move from simple to complex troubleshooting

---

For more information about other features, see our [main documentation](../README.md).
