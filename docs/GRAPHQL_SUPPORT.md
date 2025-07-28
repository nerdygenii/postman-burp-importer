# GraphQL Support Guide

The Postman Collection Importer provides comprehensive support for GraphQL APIs, making it easy to import and test GraphQL queries, mutations, and subscriptions in Burp Suite.

## 🚀 Features

### Automatic Detection
- ✅ **Query Recognition**: Automatically detects GraphQL queries, mutations, and subscriptions
- ✅ **Operation Extraction**: Parses operation names for better identification
- ✅ **Schema Introspection**: Supports GraphQL schema discovery queries
- ✅ **Variable Handling**: Seamless integration with Postman variables in GraphQL content

### Enhanced Preview
GraphQL requests are clearly identified in the preview with special formatting:
```
"Get User Profile [GraphQL: query GetUser]"
"Create User [GraphQL: mutation CreateUser]"
"Subscribe to Updates [GraphQL: subscription OnUpdate]"
```

## 📋 Supported GraphQL Features

### Query Types
- **Queries**: Data fetching operations
- **Mutations**: Data modification operations  
- **Subscriptions**: Real-time data streaming
- **Fragments**: Reusable query components
- **Inline Fragments**: Type-specific field selections

### Variable Support
- **Query Variables**: Dynamic values in GraphQL operations
- **Postman Variables**: `{{variable}}` resolution in GraphQL content
- **Environment Variables**: Full support for Postman environment files
- **Nested Variables**: Complex variable structures and arrays

## 🔧 Usage Examples

### Basic GraphQL Query
```graphql
query GetUser($id: ID!) {
  user(id: $id) {
    id
    name
    email
    profile {
      avatar
      bio
    }
  }
}
```

### With Postman Variables
```graphql
query GetUserPosts {
  user(id: "{{user_id}}") {
    posts(limit: {{posts_limit}}) {
      id
      title
      content
      createdAt
    }
  }
}
```

### GraphQL Mutation
```graphql
mutation CreatePost($input: PostInput!) {
  createPost(input: $input) {
    id
    title
    status
    author {
      name
    }
  }
}
```

## 🎯 Best Practices

### Collection Organization
1. **Group by Operation Type**: Separate queries, mutations, and subscriptions
2. **Use Descriptive Names**: Clear operation names for easy identification
3. **Include Variables**: Use Postman variables for dynamic testing
4. **Add Documentation**: Include descriptions for complex operations

### Variable Management
1. **Environment Files**: Use separate environments for different endpoints
2. **Variable Naming**: Use clear, descriptive variable names
3. **Default Values**: Provide sensible defaults in environment files
4. **Type Safety**: Ensure variables match GraphQL schema types

### Security Testing
1. **Authorization Testing**: Test different user roles and permissions
2. **Input Validation**: Test malformed queries and invalid inputs
3. **Rate Limiting**: Test query complexity and depth limits
4. **Introspection**: Check if schema introspection is properly disabled in production

## 🔍 Troubleshooting

### Common Issues

#### GraphQL Not Detected
**Problem**: GraphQL requests appear as regular HTTP requests

**Solutions**:
- Ensure request body contains valid GraphQL syntax
- Check that Content-Type is `application/json` or `application/graphql`
- Verify query structure starts with `query`, `mutation`, or `subscription`

#### Variables Not Resolving
**Problem**: Postman variables in GraphQL content not replaced

**Solutions**:
- Use "Preview Requests" to check variable resolution status
- Ensure environment file contains required variables
- Check variable syntax: `{{variable_name}}`
- Verify variable scope (environment vs collection vs global)

#### Malformed Queries
**Problem**: GraphQL queries cause import failures

**Solutions**:
- Validate GraphQL syntax using online validators
- Check for missing brackets, commas, or quotes
- Ensure variable definitions match usage
- Test queries in GraphQL playground first

#### Authentication Issues
**Problem**: GraphQL requests fail authentication

**Solutions**:
- Verify Bearer token or API key configuration
- Check authentication headers in Postman collection
- Ensure token variables are properly resolved
- Test authentication separately in Repeater

## 📊 Integration with Burp Suite

### Repeater Mode
- GraphQL requests appear as individual tabs
- Easy manual testing and query modification
- Syntax highlighting for JSON responses
- History tracking for query iterations

### Sitemap Mode
- Live GraphQL requests populate Sitemap
- Real response data for schema discovery
- Integration with Burp Scanner for security testing
- Rate limiting respects GraphQL endpoint constraints

### Both Mode
- Combines manual testing capabilities with automated discovery
- Perfect for comprehensive GraphQL security assessments
- Enables both exploratory and systematic testing approaches

## 🛡️ Security Considerations

### Testing Recommendations
1. **Depth Limiting**: Test query depth limits and nested object access
2. **Field Access**: Verify field-level authorization controls
3. **Introspection**: Check if schema introspection is appropriately restricted
4. **Rate Limiting**: Test query complexity scoring and rate limits
5. **Input Validation**: Test for injection attacks in GraphQL variables
6. **Error Handling**: Analyze error messages for information disclosure

### Common Vulnerabilities
- **Excessive Query Depth**: Deeply nested queries causing DoS
- **Field Authorization**: Insufficient access controls on sensitive fields
- **Information Disclosure**: Verbose error messages revealing schema details
- **Injection Attacks**: SQL/NoSQL injection through GraphQL variables
- **Batch Attacks**: Multiple operations in single request bypassing limits

## 📚 Additional Resources

- [GraphQL Security Best Practices](https://graphql.org/learn/security/)
- [OWASP GraphQL Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/GraphQL_Cheat_Sheet.html)
- [GraphQL Specification](https://spec.graphql.org/)
- [Postman GraphQL Documentation](https://learning.postman.com/docs/sending-requests/graphql/graphql/)

---

For more information about other features, see our [main documentation](../README.md).
