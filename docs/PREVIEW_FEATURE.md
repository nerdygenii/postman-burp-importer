# Request Preview & Selection

The Request Preview feature allows you to analyze your Postman collection before import, providing detailed insights into requests, variables, and potential issues while enabling selective import of specific requests.

## 🔍 Overview

### What is Request Preview?
Request Preview analyzes your Postman collection and displays a comprehensive table showing all requests, their properties, variable status, and potential issues before you commit to importing them.

### Benefits
- ✅ **Pre-Import Analysis**: See exactly what will be imported before processing
- ✅ **Selective Import**: Choose specific requests instead of importing everything
- ✅ **Variable Validation**: Identify variable issues before import failures
- ✅ **Request Inspection**: Review request details, methods, and URLs
- ✅ **Quality Assurance**: Catch problems early in the process

## 🚀 Features

### Comprehensive Request Analysis
- **Request Details**: Method, URL, description, and folder structure
- **Variable Status**: Visual indicators for resolved vs unresolved variables
- **Authentication Info**: Shows authentication method and configuration
- **Body Type Detection**: Identifies JSON, form-data, and other body types
- **GraphQL Recognition**: Special indicators for GraphQL operations

### Interactive Selection
- **Checkbox Interface**: Select/deselect individual requests
- **Bulk Operations**: Select all, clear all, and filter-based selection
- **Smart Filtering**: Filter by status, method, or variable resolution
- **Search Functionality**: Find specific requests by name or URL
- **Folder Organization**: Maintain Postman folder structure in preview

### Visual Status Indicators
- ✅ **Green**: Request is ready for import (all variables resolved)
- ⚠️ **Yellow**: Warning state (some variables unresolved)
- ❌ **Red**: Error state (critical issues or multiple unresolved variables)
- 🔵 **Blue**: Special indicators for GraphQL, authentication, etc.

## 📊 Preview Table Columns

### Core Information
- **☑️ Select**: Checkbox for including request in import
- **🏷️ Name**: Request name from Postman collection
- **🔧 Method**: HTTP method (GET, POST, PUT, DELETE, etc.)
- **🌐 URL**: Full request URL with variable resolution status
- **📁 Path**: Folder hierarchy from Postman collection

### Advanced Details
- **🔐 Auth**: Authentication method indicator
- **📝 Body**: Request body type (JSON, form-data, none, etc.)
- **⚠️ Variables**: Variable resolution status with color coding
- **📊 Status**: Overall request status and readiness

### Special Indicators
- **🎯 GraphQL**: Shows operation type for GraphQL requests
- **🔄 Retry**: Indicates if request was part of previous retry
- **⭐ Featured**: Highlights important or complex requests

## 🎯 Usage Workflows

### Basic Preview Workflow
1. **Load Collection**: Select your Postman collection file
2. **Optional Environment**: Add environment file if available
3. **Click Preview**: Click "Preview Requests" button
4. **Review Analysis**: Examine request table and status indicators
5. **Make Selections**: Choose which requests to import
6. **Import Selected**: Proceed with import of selected requests

### Variable Resolution Workflow
1. **Identify Issues**: Look for yellow/red status indicators
2. **Resolve Variables**: Use Variable Intelligence to fix issues
3. **Re-analyze**: Preview updates automatically after variable resolution
4. **Verify Status**: Confirm all selected requests show green status
5. **Proceed with Import**: Import with confidence

### Selective Import Workflow
1. **Analyze Collection**: Review all available requests
2. **Filter by Criteria**: Focus on specific request types or folders
3. **Select Strategically**: Choose requests based on testing needs
4. **Exclude Problematic**: Skip requests with unresolved issues
5. **Import Subset**: Process only selected requests

## 🔧 Advanced Features

### Multi-Selection Options

#### Select All/None
- **Select All**: Quickly select every request in the collection
- **Clear All**: Deselect all requests for fresh selection
- **Invert Selection**: Flip selection state of all requests

#### Conditional Selection
- **Select by Status**: Only select requests with specific status (green, yellow, red)
- **Select by Method**: Choose all GET, POST, or other method types
- **Select by Folder**: Select entire folders from Postman structure
- **Select by Variables**: Choose requests with resolved/unresolved variables

### Filtering Capabilities

#### Status Filtering
- **Show All**: Display every request regardless of status
- **Ready Only**: Show only requests ready for import (green status)
- **Issues Only**: Display only requests with variable problems
- **GraphQL Only**: Filter to show only GraphQL operations

#### Search and Filter
- **Name Search**: Find requests by name or partial name matching
- **URL Search**: Locate requests by URL pattern or domain
- **Method Filter**: Show only specific HTTP methods
- **Folder Filter**: Focus on specific collection folders

### Variable Analysis Integration

#### Variable Status Display
- **Resolved Count**: Shows how many variables are resolved per request
- **Unresolved List**: Displays specific variables that need resolution
- **Impact Assessment**: Indicates how variable issues affect request functionality
- **Suggestions Available**: Shows when AI suggestions are available

#### Resolution Integration
- **Direct Resolution**: Click on variable issues to resolve them immediately
- **Batch Resolution**: Resolve variables affecting multiple selected requests
- **Preview Updates**: See real-time updates as variables are resolved
- **Status Changes**: Watch status indicators update with resolution progress

## 🛡️ Quality Assurance

### Pre-Import Validation

#### Request Completeness
- **URL Validation**: Ensures all URLs are properly formed
- **Method Verification**: Confirms HTTP methods are valid
- **Header Analysis**: Reviews header completeness and format
- **Body Structure**: Validates request body syntax and structure

#### Variable Completeness
- **Resolution Check**: Verifies all variables can be resolved
- **Type Validation**: Ensures variable values match expected types
- **Scope Verification**: Confirms variables are available in current scope
- **Circular Reference Detection**: Identifies problematic variable dependencies

#### Authentication Validation
- **Credential Check**: Verifies authentication information is complete
- **Method Compatibility**: Ensures auth method matches request requirements
- **Token Format**: Validates token formats and expiration
- **Scope Authorization**: Checks if credentials have appropriate scope

### Error Prevention

#### Common Issue Detection
- **Malformed URLs**: Identifies URLs that will cause import failures
- **Missing Variables**: Highlights variables that must be resolved
- **Invalid Methods**: Catches unsupported or malformed HTTP methods
- **Authentication Problems**: Detects incomplete or invalid auth configurations

#### Proactive Warnings
- **Rate Limit Risks**: Warns about potential rate limiting with large imports
- **Network Dependencies**: Identifies requests requiring specific network access
- **Variable Dependencies**: Shows complex variable resolution requirements
- **Performance Impacts**: Estimates import time and resource usage

## 🔍 Troubleshooting

### Preview Not Loading

#### Collection Issues
**Problem**: Preview fails to generate or shows errors

**Solutions**:
- Verify collection file is valid JSON format
- Check that collection uses v2.0 or v2.1 format
- Ensure file is not corrupted or truncated
- Try with a smaller or simpler collection first

#### Environment Problems
**Problem**: Variables not resolving despite environment file

**Solutions**:
- Verify environment file matches collection requirements
- Check variable name spelling and case sensitivity
- Ensure environment file is valid JSON
- Try manual variable entry as alternative

### Selection Issues

#### Checkboxes Not Working
**Problem**: Cannot select or deselect requests

**Solutions**:
- Ensure preview has finished loading completely
- Try refreshing preview by re-clicking "Preview Requests"
- Check that collection analysis completed successfully
- Verify UI is responsive (not frozen or busy)

#### Bulk Operations Failing
**Problem**: Select All or Clear All buttons not working

**Solutions**:
- Wait for preview to complete loading
- Try individual selections first to test functionality
- Check for JavaScript errors in Burp Suite console
- Restart Burp Suite if UI becomes unresponsive

### Performance Issues

#### Slow Preview Generation
**Problem**: Preview takes very long to generate

**Solutions**:
- Try with smaller collection to test functionality
- Close other resource-intensive applications
- Increase Burp Suite memory allocation
- Consider splitting large collections into smaller files

#### Memory Problems
**Problem**: Out of memory errors during preview

**Solutions**:
- Increase Java heap size for Burp Suite
- Close unnecessary tabs and tools in Burp Suite
- Use selective import instead of full collection
- Process collection in smaller batches

---

For more information about other features, see our [main documentation](../README.md).
