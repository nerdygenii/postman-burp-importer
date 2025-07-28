# Postman Collection Importer for Burp Suite

![Burp Suite Extension](https://img.shields.io/badge/Burp%20Suite-Extension-orange)
![Java](https://img.shields.io/badge/Java-8+-blue)
![License](https://img.shields.io/badge/License-MIT-green)
![Version](https://img.shields.io/badge/Version-1.0.0-brightgreen)

A powerful Burp Suite extension that seamlessly imports Postman collections and environments, converting them into Burp Repeater tabs and/or Sitemap entries for comprehensive security testing.

## 🚀 Features

### Core Functionality
- ✅ **Postman Collection v2.0 & v2.1** support
- ✅ **Environment file** support with variable resolution
- ✅ **Smart variable detection** with guided resolution
- ✅ **Multiple destinations**: Repeater, Sitemap, or both
- ✅ **Request preview** and selective import
- ✅ **Authentication support**: Bearer, Basic, API Key, OAuth2
- ✅ **All request body types**: JSON, form-data, x-www-form-urlencoded
- ✅ **Progress tracking** and detailed logging

### Advanced Features
- 🎯 **GraphQL Support**: Automatic detection and operation extraction ([Guide](docs/GRAPHQL_SUPPORT.md))
- 🔄 **Smart Retry System**: Retry failed requests with one click ([Guide](docs/RETRY_FEATURE.md))
- 🧠 **Intelligent Variables**: Smart variable suggestions ([Guide](docs/VARIABLE_INTELLIGENCE.md))
- 📊 **Multi-selection**: Choose specific requests to import ([Guide](docs/MULTI_SELECTION.md))
- ⚡ **Rate Limiting**: Configurable delays for live requests (0-5000ms, default: 200ms)
- 🎨 **Visual Indicators**: Color-coded status and variable resolution

## 📋 Requirements

- **Burp Suite Professional or Community Edition**
- **Java 8+**
- **Maven** (for building from source)

## 🛠️ Installation

### Option 1: Download Release
1. Download the latest JAR from [Releases](https://github.com/nerdygenii/postman-burp-importer/releases/latest)
2. In Burp Suite: `Extensions` → `Add` → Select the JAR file
3. The "Postman Importer" tab will appear

### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/nerdygenii/postman-burp-importer.git
cd postman-burp-importer

# Build the extension
mvn clean package

# Load the JAR in Burp Suite
# File: target/postman-burp-importer-1.0.0-jar-with-dependencies.jar
```

## 📖 Quick Start

### Basic Import
1. **Load Collection**: Click "Browse" next to Collection and select your `.json` file
2. **Load Environment** (Optional): Select your Postman environment file
3. **Choose Destination**:
   - **Repeater**: For manual testing (no HTTP requests made)
   - **Sitemap**: For live requests and attack surface discovery
   - **Both**: Best of both worlds
4. **Configure Rate Limiting** (Optional): Adjust delay between requests (default: 200ms)
5. **Import**: Click "Import Collection"

### With Variable Resolution
1. Follow steps 1-3 above
2. If unresolved variables are detected, choose resolution method:
   - **Upload Environment**: Select additional environment file
   - **Manual Entry**: Enter values with smart suggestions
   - **Ignore Variables**: Proceed with unresolved variables
   - **Skip Problematic**: Import only requests without issues

## 🎯 Use Cases

### Security Testing Workflows
- **API Penetration Testing**: Import entire API collections for comprehensive testing
- **Vulnerability Assessment**: Convert Postman tests to Burp Scanner targets
- **Manual Testing**: Use Repeater tabs for detailed request manipulation
- **Automation Bridge**: Connect development tools with security testing

### Team Collaboration
- **DevSecOps Integration**: Import from CI/CD Postman collections
- **QA to Security**: Reuse existing API test suites for security testing
- **Documentation**: Convert API documentation to live security tests

## 📊 Destination Modes

### Repeater Mode (Default)
- 📝 Creates tabs in Burp Repeater
- 🔧 Perfect for manual testing and request modification
- 🚫 No actual HTTP requests made
- ⚡ Fast import process

### Sitemap Mode 
- 🌐 Makes actual HTTP requests
- 📈 Populates Burp's Sitemap with real responses
- 🔍 Enables automatic scanner discovery
- 📊 Shows real status codes and response data
- ⏱️ Includes rate limiting (200ms delays)

### Both Mode
- 🎯 Combines Repeater and Sitemap benefits
- 📋 Requests appear in both locations
- 🔄 Best for comprehensive testing workflows

## 📚 Documentation

- 📖 [GraphQL Support Guide](docs/GRAPHQL_SUPPORT.md)
- 🔄 [Smart Retry System](docs/RETRY_FEATURE.md)
- 🧠 [Variable Intelligence](docs/VARIABLE_INTELLIGENCE.md)
- 📊 [Sitemap Integration](docs/SITEMAP_USAGE.md)
- 🎯 [Request Preview & Selection](docs/PREVIEW_FEATURE.md)
- 🛠️ [Troubleshooting Guide](docs/TROUBLESHOOTING.md)

## 🐛 Troubleshooting

### Common Issues
- **Variables not resolving**: Use "Preview Requests" to check variable status
- **Import fails**: Check collection format (v2.0/v2.1 required)
- **Network errors in Sitemap mode**: Use "Retry Failed Requests" button
- **Missing requests**: Verify folder structure and disabled requests

For detailed troubleshooting, see our [Troubleshooting Guide](docs/TROUBLESHOOTING.md).

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Abdulrahman Oyekunle**
- GitHub: [@nerdygenii](https://github.com/nerdygenii)
- LinkedIn: [Abdulrahman Oyekunle](https://linkedin.com/in/abdulrahman-oyekunle-3a7906179)
- Twitter: [@nerdy_genii](https://twitter.com/nerdy_genii)

## 🙏 Acknowledgments

- Burp Suite team for the excellent extensibility API
- Postman team for the comprehensive collection format
- **Badmus** ([@Commando-X](https://github.com/Commando-X)) for insightful input and testing
- **Guardians with Attitude** team (khalifa Mohammed,Abidakun Samuel) for their valuable feedback and suggestions
- Open source community for inspiration and feedback

## ⭐ Support

If this extension helps your security testing workflow, please consider:
- ⭐ Starring this repository
- 🐛 Reporting bugs and issues
- 💡 Suggesting new features
- 🤝 Contributing code improvements

## 

---

*Made with ❤️ for the cybersecurity community*
