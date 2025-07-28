# Multi-Selection Guide

The Multi-Selection feature provides flexible control over which requests from your Postman collection are imported, allowing you to choose specific requests, folders, or subsets based on your testing needs.

## ✅ Overview

### What is Multi-Selection?
Multi-Selection allows you to selectively import specific requests from your Postman collection instead of importing everything. This provides precise control over your security testing scope and helps manage large collections efficiently.

### Benefits
- ✅ **Precise Control**: Choose exactly which requests to import
- ✅ **Efficient Testing**: Focus on specific functionality or endpoints
- ✅ **Resource Management**: Import only what you need, reducing overhead
- ✅ **Flexible Workflows**: Support different testing scenarios and scopes
- ✅ **Quality Control**: Exclude problematic requests while importing good ones

## 🎯 Selection Methods

### Individual Request Selection
- **Checkbox Interface**: Click individual checkboxes next to each request
- **Visual Feedback**: Selected requests are clearly highlighted
- **Status Awareness**: Selection respects request status (ready/warning/error)
- **Real-time Updates**: Selection count updates as you choose requests

### Bulk Selection Operations
- **Select All**: Choose every request in the collection
- **Clear All**: Deselect all currently selected requests
- **Invert Selection**: Flip the selection state of all requests
- **Conditional Selection**: Select based on specific criteria

### Filter-Based Selection
- **Status Filtering**: Select only ready, warning, or error requests
- **Method Filtering**: Choose requests by HTTP method (GET, POST, etc.)
- **Folder Filtering**: Select entire folders from Postman hierarchy
- **Search-Based**: Select requests matching search criteria

## 🔧 Selection Workflows

### Testing-Focused Selection

#### API Endpoint Testing
```
1. Load collection → Preview requests
2. Filter by specific API version or service
3. Select all GET requests for read testing
4. Add specific POST/PUT requests for write testing
5. Import selected subset for focused testing
```

#### Authentication Testing
```
1. Preview collection to see all requests
2. Select requests with different authentication methods
3. Include both authenticated and unauthenticated endpoints
4. Focus on authorization boundary testing
5. Import auth-focused request subset
```

#### GraphQL-Specific Testing
```
1. Preview collection with GraphQL detection
2. Filter to show only GraphQL operations
3. Select specific query/mutation/subscription types
4. Exclude introspection queries if desired
5. Import GraphQL-focused subset
```

### Scope-Based Selection

#### Service-Specific Testing
```
1. Preview large microservice collection
2. Filter by service hostname or path prefix
3. Select all requests for specific service
4. Exclude dependencies or external services
5. Import service-focused scope
```

#### Environment-Specific Testing
```
1. Analyze collection for environment differences
2. Select requests appropriate for test environment
3. Exclude production-only or admin endpoints
4. Include monitoring and health check endpoints
5. Import environment-appropriate subset
```

## 📊 Selection Interface

### Visual Indicators

#### Selection Status
- ☑️ **Selected**: Request will be included in import
- ☐ **Unselected**: Request will be skipped
- 🔒 **Disabled**: Request cannot be selected due to issues
- ⚠️ **Warning**: Request has issues but can still be selected

#### Request Status Integration
- ✅ **Ready + Selected**: Optimal state for import
- ⚠️ **Warning + Selected**: Will attempt import with potential issues
- ❌ **Error + Selected**: May fail during import
- ⚠️ **Ready + Unselected**: Available but not chosen for import

### Selection Counters
- **Total Requests**: Shows complete collection size
- **Selected Count**: Number of currently selected requests
- **Ready Count**: How many selected requests are ready for import
- **Issue Count**: How many selected requests have potential problems

### Preview Integration
- **Real-time Updates**: Selection changes update counters immediately
- **Status Preservation**: Selection state maintained during variable resolution
- **Filter Persistence**: Selection survives filtering and search operations
- **Import Preview**: Clear indication of what will be imported

## 🎯 Advanced Selection Strategies

### Progressive Selection

#### Start Small, Expand Gradually
```
1. Begin with 5-10 critical requests
2. Import and verify basic functionality
3. Add more requests in subsequent imports
4. Build comprehensive coverage incrementally
```

#### Quality-First Approach
```
1. Select only green (ready) status requests initially
2. Import and test successful requests
3. Resolve variable issues for warning requests
4. Add resolved requests in second import pass
```

### Strategic Exclusions

#### Problematic Request Management
- **Skip Known Issues**: Exclude requests with unresolved variables
- **Environment Restrictions**: Skip requests requiring unavailable services
- **Rate Limit Management**: Exclude high-frequency requests for initial testing
- **Scope Boundaries**: Skip out-of-scope or external service requests

#### Resource Optimization
- **Large Payload Exclusion**: Skip requests with very large bodies
- **Slow Endpoint Avoidance**: Exclude requests to known slow endpoints
- **Authentication Complexity**: Skip complex multi-step auth flows initially
- **Dependency Management**: Exclude requests requiring specific test data

## 🔍 Selection Validation

### Pre-Import Checks

#### Selection Completeness
- **Coverage Analysis**: Ensure selected requests cover intended test scope
- **Dependency Verification**: Check that dependent requests are included
- **Flow Validation**: Verify request sequences make sense together
- **Authentication Coverage**: Ensure auth requests support data requests

#### Quality Assurance
- **Variable Resolution**: Verify all selected requests have resolved variables
- **Status Review**: Check mix of ready vs warning status in selection
- **Method Balance**: Ensure appropriate mix of HTTP methods
- **Error Risk Assessment**: Understand potential failure points

### Selection Optimization

#### Performance Considerations
- **Batch Size**: Select appropriate number of requests for system resources
- **Network Impact**: Consider total network load of selected requests
- **Rate Limiting**: Balance selection size with target service limits
- **Processing Time**: Estimate import duration based on selection

#### Testing Effectiveness
- **Scope Coverage**: Ensure selection covers intended testing scope
- **Edge Case Inclusion**: Include boundary and error condition requests
- **Positive/Negative Balance**: Mix successful and failure scenario requests
- **Complexity Gradation**: Include simple to complex request types

## 🛡️ Best Practices

### Planning Your Selection

#### Define Testing Objectives
1. **Scope Definition**: Clearly define what you want to test
2. **Priority Ranking**: Identify must-have vs nice-to-have requests
3. **Resource Planning**: Consider available time and system resources
4. **Risk Assessment**: Understand potential impact of selected requests

#### Analyze Before Selecting
1. **Preview First**: Always use preview to understand collection structure
2. **Status Review**: Check request status indicators before selection
3. **Variable Analysis**: Understand variable resolution requirements
4. **Dependency Mapping**: Identify request dependencies and relationships

### Execution Strategy

#### Iterative Approach
1. **Start Conservative**: Begin with smaller, safer selections
2. **Validate Results**: Verify import success before expanding
3. **Add Incrementally**: Gradually increase scope and complexity
4. **Learn and Adapt**: Use results to inform future selections

#### Quality Control
1. **Status Awareness**: Prioritize green status requests in selection
2. **Variable Resolution**: Resolve variables before import when possible
3. **Error Handling**: Have plan for handling failed requests
4. **Documentation**: Record selection criteria for reproducibility

### Common Selection Patterns

#### Security Testing Focused
- **Authentication Endpoints**: Login, logout, token refresh
- **Authorization Boundaries**: Admin vs user vs anonymous access
- **Data Access Patterns**: CRUD operations on sensitive data
- **Error Conditions**: Invalid inputs and edge cases

#### API Coverage Testing
- **Core Functionality**: Primary business logic endpoints
- **CRUD Operations**: Complete create, read, update, delete cycles
- **Search and Filtering**: Query endpoints with various parameters
- **Bulk Operations**: Batch processing and bulk data endpoints

#### Performance Testing Preparation
- **High-Frequency Endpoints**: Most commonly used requests
- **Resource-Intensive Operations**: Complex queries and calculations
- **Data Volume Scenarios**: Large request and response payloads
- **Concurrent Access Points**: Endpoints supporting multiple simultaneous users

---

For more information about other features, see our [main documentation](../README.md).
