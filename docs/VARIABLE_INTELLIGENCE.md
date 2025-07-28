# Variable Intelligence System

The Variable Intelligence System provides proactive detection, analysis, and resolution of Postman variables, eliminating common import failures and guiding users through the variable resolution process.

## 🧠 Features

### Intelligent Detection
- ✅ **Automatic Scanning**: Proactively detects unresolved variables in collections
- ✅ **Impact Analysis**: Shows how many requests are affected by each variable
- ✅ **Severity Assessment**: Prioritizes variables by their impact on import success
- ✅ **Smart Suggestions**: Intelligent default values based on variable names
- ✅ **Visual Indicators**: Color-coded status in request previews

### Guided Resolution
- 🎯 **Four Resolution Paths**: Multiple options for handling unresolved variables
- 🔍 **Context-Aware Help**: Specific guidance based on variable types and usage
- 📊 **Progress Tracking**: Visual feedback during variable resolution process
- 🎨 **Status Visualization**: Clear indicators for resolved vs unresolved variables

## 🔧 How It Works

### Detection Process
1. **Collection Analysis**: Scans entire collection for variable patterns `{{variable_name}}`
2. **Environment Check**: Compares against provided environment file variables
3. **Impact Assessment**: Determines which requests would fail without resolution
4. **Suggestion Generation**: Creates intelligent default values based on context

### Resolution Options
When unresolved variables are detected, you can choose:

#### 1. Upload Environment File
- **Best for**: Complete variable coverage
- **Process**: Select additional Postman environment file
- **Benefits**: Automatic resolution of all defined variables
- **Use case**: When you have the matching environment file

#### 2. Manual Entry
- **Best for**: Quick resolution of missing variables
- **Process**: Enter values with intelligent suggestions
- **Benefits**: Complete control over variable values
- **Use case**: When variables are missing from environment or you need custom values

#### 3. Ignore Variables
- **Best for**: Testing with unresolved variables
- **Process**: Proceed with import using placeholder values
- **Benefits**: Quick import without variable resolution
- **Use case**: When testing request structure rather than functionality

#### 4. Skip Problematic Requests
- **Best for**: Partial import of working requests
- **Process**: Import only requests without variable issues
- **Benefits**: Successful import of resolvable requests
- **Use case**: When only some requests have variable problems

## 📊 Visual Indicators

### Request Preview Status
- ✅ **Green**: All variables resolved
- ⚠️ **Yellow**: Partially resolved (some variables missing)
- ❌ **Red**: Problematic (multiple unresolved variables)

### Variable Resolution Dialog
- 🔍 **Detection Summary**: "⚠️ Found 5 variables affecting 12 requests"
- 📈 **Impact Levels**: High/Medium/Low based on affected request count
- 🎯 **Resolution Progress**: Visual progress as variables are resolved

## 🎯 Smart Suggestions

### Smart Pattern-Based Recommendations
The system analyzes variable names and contexts to provide intelligent suggestions:

#### API Endpoints
- `{{base_url}}` → `https://api.example.com`
- `{{api_host}}` → `https://api.company.com`
- `{{server_url}}` → `https://staging.api.com`

#### Authentication
- `{{api_key}}` → `your-api-key-here`
- `{{bearer_token}}` → `Bearer eyJ0eXAiOiJKV1Q...`
- `{{access_token}}` → `abc123def456ghi789`

#### Common Values
- `{{user_id}}` → `12345`
- `{{tenant_id}}` → `tenant_123`
- `{{version}}` → `v1`
- `{{environment}}` → `staging`

#### Ports and Protocols
- `{{port}}` → `8080`
- `{{protocol}}` → `https`
- `{{ws_protocol}}` → `wss`

### Context-Aware Intelligence
- **URL Context**: Variables in URLs get endpoint suggestions
- **Header Context**: Variables in headers get appropriate header value suggestions
- **Body Context**: Variables in request bodies get data format suggestions
- **Query Context**: Variables in query parameters get common parameter value suggestions

## 🔍 Troubleshooting Variables

### Common Variable Issues

#### Variables Not Detected
**Problem**: System doesn't find variables that exist in collection

**Solutions**:
- Ensure variables use correct syntax: `{{variable_name}}`
- Check for typos in variable names
- Verify variables aren't commented out
- Look for variables in nested objects or arrays

#### Suggestions Not Helpful
**Problem**: Suggestions don't match your needs

**Solutions**:
- **Ignore Suggestions**: Type your own values
- **Use Patterns**: Build on suggested patterns with your data
- **Multiple Variables**: Use similar values for related variables
- **Environment File**: Upload environment file for accurate values

#### Partial Resolution
**Problem**: Some variables resolve, others don't

**Solutions**:
- **Check Scope**: Ensure all variables are in the same scope
- **Review Names**: Look for case sensitivity issues
- **Multiple Sources**: Combine environment file with manual entry
- **Iterative Process**: Resolve variables in multiple passes

### Resolution Workflow Issues

#### Dialog Doesn't Appear
**Problem**: Variable resolution dialog isn't shown

**Solutions**:
- Ensure collection actually contains unresolved variables
- Check that environment file doesn't already resolve all variables
- Verify collection is valid Postman format
- Try "Preview Requests" to trigger variable analysis

#### Can't Enter Values
**Problem**: Manual entry fields are disabled or non-responsive

**Solutions**:
- Ensure "Manual Entry" option is selected
- Check that variables were properly detected
- Try refreshing by re-analyzing collection
- Verify Java/Swing UI is responsive in Burp Suite

## 📈 Advanced Usage

### Complex Variable Scenarios

#### Nested Variables
```json
{
  "url": "{{base_url}}/{{api_version}}/users/{{user_id}}",
  "headers": {
    "Authorization": "{{auth_scheme}} {{token}}",
    "X-API-Version": "{{api_version}}"
  }
}
```

#### Conditional Variables
- **Environment-Specific**: Different values per environment (dev/staging/prod)
- **User-Specific**: Variables that change per test user
- **Dynamic Values**: Variables that require calculation or generation

### Best Practices

#### Variable Organization
1. **Consistent Naming**: Use clear, descriptive variable names
2. **Logical Grouping**: Group related variables together
3. **Environment Separation**: Use environment files for environment-specific values
4. **Documentation**: Include descriptions for complex variables

#### Resolution Strategy
1. **Start with Environment**: Use environment files for bulk resolution
2. **Manual Override**: Use manual entry for missing or custom values
3. **Test Incrementally**: Resolve variables in small batches for testing
4. **Validate Results**: Use preview to verify variable resolution

#### Security Considerations
1. **Sensitive Data**: Be careful with API keys and tokens in manual entry
2. **Environment Safety**: Use appropriate environment files for testing vs production
3. **Variable Scope**: Understand which variables are shared vs private
4. **Cleanup**: Remove or replace sensitive values after testing

## 🛡️ Security Features

### Safe Defaults
- **No Sensitive Data**: Default suggestions avoid exposing real credentials
- **Example Format**: Suggestions show format without real values
- **Placeholder Values**: Safe placeholders for sensitive variables
- **Clear Labeling**: Obvious indicators for example vs real values

### Variable Validation
- **Format Checking**: Basic validation for URL, email, and other formats
- **Length Limits**: Reasonable limits on variable value lengths
- **Character Safety**: Prevention of injection-prone characters where appropriate
- **Encoding Support**: Proper handling of special characters and encoding

## 📚 Integration Examples

### DevSecOps Workflow
```
1. Developer creates Postman collection
2. Security team imports to Burp Suite
3. Variable Intelligence detects missing environment variables
4. Team uploads staging environment file
5. Manual entry for security-specific test values
6. Complete import ready for security testing
```

### Team Collaboration
```
1. QA team shares Postman collection
2. Security engineer imports collection
3. Smart suggestions provide quick defaults
4. Manual entry customizes for security scenarios
5. Successful import enables immediate testing
```

---

For more information about other features, see our [main documentation](../README.md).
