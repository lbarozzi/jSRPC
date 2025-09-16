# jSRPC - Multi-Threaded TCP Client/Server with RSA Encryption

A comprehensive Java-based TCP communication framework with advanced cryptographic security, providing both basic TCP networking and secure encrypted communications using RSA-2048 keys and digital signatures.

## 🚀 Features

### Core TCP Framework
- **Multi-threaded TCP Server**: Concurrent client handling with configurable thread pools
- **Robust TCP Client**: Connection management with automatic retry and error handling
- **Built-in Protocol Commands**: `ping`, `time`, `help` for connectivity testing
- **Connection Pooling**: Efficient resource management and graceful shutdowns

### Cryptographic Security Extension
- **RSA-2048 Encryption**: End-to-end message encryption using strong RSA keys
- **Digital Signatures**: SHA256withRSA signatures for message authentication and integrity
- **GPG-like Protocol**: Secure handshake and key exchange mechanism
- **Automatic Key Management**: Generation, storage, and loading of RSA key pairs in PEM format
- **Message Integrity**: Cryptographic verification preventing tampering and replay attacks

### Advanced Key Generation
- **Secure Key Generator**: Cryptographically strong RSA key pair generation
- **Multiple Key Sizes**: Support for 1024, 2048, 3072, and 4096-bit keys
- **Custom SecureRandom**: Configurable entropy sources for enhanced security
- **Batch Key Generation**: Efficient generation of multiple key pairs

### Testing and Quality
- **Comprehensive Test Suite**: 62+ JUnit tests covering all functionality
- **Integration Tests**: End-to-end testing of client-server interactions
- **Performance Tests**: Key generation and encryption performance validation
- **Security Tests**: Cryptographic property verification and randomness testing
## 🏗️ Architecture

### Project Structure

```
jSRPC/
├── src/
│   ├── main/java/com/copan/
│   │   ├── tcp/                 # Basic TCP networking
│   │   │   ├── TCPServer.java
│   │   │   ├── TCPClient.java
│   │   │   ├── ClientHandler.java
│   │   │   └── TCPDemo.java
│   │   ├── crypto/              # Cryptographic extensions
│   │   │   ├── CriptoServer.java
│   │   │   ├── CriptoClient.java
│   │   │   ├── CriptoClientHandler.java
│   │   │   ├── KeyManager.java
│   │   │   ├── CryptoUtils.java
│   │   │   ├── SecureMessage.java
│   │   │   ├── KeyGenerator.java
│   │   │   ├── CryptoDemo.java
│   │   │   └── KeyGeneratorDemo.java
│   │   └── App.java             # Main application entry point
│   └── test/java/com/copan/     # Comprehensive test suite
└── pom.xml                      # Maven configuration
```

### Class Hierarchy

```
TCPServer
└── CriptoServer (adds RSA encryption)

TCPClient  
└── CriptoClient (adds RSA encryption)

ClientHandler
└── CriptoClientHandler (adds secure message handling)
```

## 🔐 Security Architecture

### Encryption Specifications
- **Algorithm**: RSA with PKCS1 padding (Java 8 compatible)
- **Key Size**: 2048-bit default (configurable up to 4096-bit)
- **Signature**: SHA256withRSA digital signatures
- **Message Format**: JSON-serialized SecureMessage containers
- **Key Storage**: PEM format for OpenSSL compatibility

### Security Workflow
1. **Key Generation**: Automatic RSA key pair creation if not present
2. **Handshake**: Secure exchange of public keys between client and server
3. **Message Encryption**: All messages encrypted with recipient's public key
4. **Digital Signing**: Messages signed with sender's private key
5. **Verification**: Signature validation ensures message authenticity and integrity

## 📦 Quick Start

### Prerequisites
- Java 8 or higher
- Maven 3.6 or higher

### Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd jSRPC
   ```

2. **Build the project**:
   ```bash
   mvn clean compile
   ```

3. **Run tests**:
   ```bash
   mvn test
   ```

4. **Create JAR files**:
   ```bash
   mvn package
   ```
   This creates:
   - `target/jSRPC-1.0-SNAPSHOT.jar` (regular JAR)
   - `target/jSRPC-1.0-SNAPSHOT-fat.jar` (fat JAR with dependencies)

4. **Create JAR files**:
   ```bash
   mvn package
   ```
   This creates:
   - `target/jSRPC-1.0-SNAPSHOT.jar` (regular JAR)
   - `target/jSRPC-1.0-SNAPSHOT-fat.jar` (fat JAR with dependencies)

### Basic TCP Usage

#### Starting a TCP Server
```java
import com.copan.tcp.TCPServer;

// Create and start server
TCPServer server = new TCPServer(8080, 10); // port, max threads
server.start();

// Server automatically handles: ping, time, help commands
// Custom message handling available through ClientHandler extension
```

#### Connecting with TCP Client
```java
import com.copan.tcp.TCPClient;

// Create and connect client
TCPClient client = new TCPClient("localhost", 8080);
client.connect();

// Test connectivity
String response = client.sendAndReceive("ping");
System.out.println(response); // "pong"

// Get server time
String time = client.sendAndReceive("time");
System.out.println(time); // Current server timestamp

// Send custom message
String echo = client.sendAndReceive("Hello Server!");
System.out.println(echo); // "Echo: Hello Server!"

client.disconnect();
```

### Secure Crypto Usage

#### Starting a Cryptographic Server
```java
import com.copan.crypto.CriptoServer;
import com.copan.crypto.KeyManager;

// Initialize key management
KeyManager keyManager = new KeyManager("server_keys");

// Create and start secure server
CriptoServer server = new CriptoServer(8080, 10, keyManager, "server");
server.start();

// Server automatically:
// - Generates RSA keys if not present
// - Handles cryptographic handshakes
// - Encrypts/decrypts all messages
```

#### Connecting with Cryptographic Client
```java
import com.copan.crypto.CriptoClient;
import com.copan.crypto.KeyManager;

// Initialize client key management
KeyManager keyManager = new KeyManager("client_keys");

// Create and connect secure client
CriptoClient client = new CriptoClient("localhost", 8080, keyManager, "client");
client.connect();

// All messages are automatically encrypted and signed
String response = client.sendAndReceive("Secret message!");
System.out.println(response); // Decrypted response

// Check encryption status
boolean secure = client.isCryptoHandshakeComplete();
System.out.println("Secure connection: " + secure);

client.disconnect();
```

## 🔑 Key Management

### Automatic Key Generation
```java
import com.copan.crypto.KeyManager;

// KeyManager automatically generates keys if they don't exist
KeyManager keyManager = new KeyManager("my_keys");

// Load or generate key pair for "client1"
keyManager.loadOrGenerateKeyPair("client1");

// Check if keys exist
boolean hasKeys = keyManager.keyPairExists("client1");
```

### Manual Key Generation
```java
import com.copan.crypto.KeyGenerator;

// Create key generator (default 2048-bit)
KeyGenerator generator = new KeyGenerator();

// Generate single key pair
KeyPair keyPair = generator.generateKeyPair();

// Generate multiple key pairs
KeyPair[] keyPairs = generator.generateKeyPairs(5);

// High-security 4096-bit keys
KeyGenerator secureGen = new KeyGenerator(KeyGenerator.KEY_SIZE_4096);
KeyPair secureKeys = secureGen.generateKeyPair();
```

### Key Storage and Loading
```java
import com.copan.crypto.KeyManager;

KeyManager keyManager = new KeyManager("keys_directory");

// Save key pair
keyManager.saveKeyPair("user1", keyPair);

// Load keys
PublicKey publicKey = keyManager.loadPublicKey("user1");
PrivateKey privateKey = keyManager.loadPrivateKey("user1");

// Delete keys
keyManager.deleteKeyPair("user1");
```

## 🔧 Advanced Usage

### Custom Message Handlers

#### Extending TCP Server
```java
import com.copan.tcp.ClientHandler;

public class CustomClientHandler extends ClientHandler {
    public CustomClientHandler(Socket clientSocket) {
        super(clientSocket);
    }
    
    @Override
    protected String processCustomMessage(String message) {
        if (message.startsWith("calculate:")) {
            // Custom calculation logic
            return performCalculation(message.substring(10));
        }
        return super.processCustomMessage(message);
    }
}

// Use custom handler in server
public class CustomTCPServer extends TCPServer {
    @Override
    protected void handleClient(Socket clientSocket) {
        CustomClientHandler handler = new CustomClientHandler(clientSocket);
        handler.handle();
    }
}
```

### Cryptographic Utilities

#### Direct Encryption/Decryption
```java
import com.copan.crypto.CryptoUtils;

// Encrypt message with recipient's public key
String encrypted = CryptoUtils.encrypt("Secret message", recipientPublicKey);

// Decrypt with your private key
String decrypted = CryptoUtils.decrypt(encrypted, yourPrivateKey);

// Digital signature
String signature = CryptoUtils.sign("Message to sign", yourPrivateKey);

// Verify signature
boolean valid = CryptoUtils.verify("Message to sign", signature, senderPublicKey);
```

#### Secure Message Container
```java
import com.copan.crypto.SecureMessage;

// Create secure message (encrypted + signed)
SecureMessage secureMsg = CryptoUtils.createSecureMessage(
    "Confidential data",
    senderPrivateKey,
    recipientPublicKey
);

// Serialize to JSON for transmission
String json = secureMsg.toJson();

// Deserialize from JSON
SecureMessage received = SecureMessage.fromJson(json);

// Verify and decrypt
String plaintext = CryptoUtils.verifyAndDecrypt(
    received,
    recipientPrivateKey,
    senderPublicKey
);
```

## 🎯 Demo Applications

### TCP Demo
Run the basic TCP demonstration:
```bash
# Terminal 1 - Start server
java -cp target/jSRPC-1.0-SNAPSHOT-fat.jar com.copan.tcp.TCPDemo server -p 8080

# Terminal 2 - Run client
java -cp target/jSRPC-1.0-SNAPSHOT-fat.jar com.copan.tcp.TCPDemo client -h localhost -p 8080
```

### Crypto Demo
Run the secure cryptographic demonstration:
```bash
java -cp target/jSRPC-1.0-SNAPSHOT-fat.jar com.copan.crypto.CryptoDemo
```

### Key Generator Demo
Explore key generation capabilities:
```bash
java -cp target/jSRPC-1.0-SNAPSHOT-fat.jar com.copan.crypto.KeyGeneratorDemo
```

## 🧪 Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TCPIntegrationTest

# Run with detailed output
mvn test -Dtest=CryptoTest -DforkMode=never -Dmaven.surefire.debug
```

### Test Coverage
The project includes comprehensive testing across multiple categories:

#### TCP Framework Tests
- **TCPServerTest**: Server lifecycle, connection handling, thread management
- **TCPClientTest**: Connection establishment, message sending, error handling
- **TCPIntegrationTest**: End-to-end client-server interaction scenarios

#### Cryptographic Tests
- **CryptoTest**: Secure server/client communication, encryption verification
- **KeyGeneratorTest**: Key generation security properties, randomness validation
- **KeyGeneratorIntegrationTest**: Integration with existing crypto infrastructure

#### Test Scenarios
- ✅ Single client connections
- ✅ Multiple concurrent clients (up to 50)
- ✅ Connection retry and error recovery
- ✅ Long-running sessions
- ✅ Cryptographic handshake verification
- ✅ Message encryption/decryption accuracy
- ✅ Digital signature validation
- ✅ Key generation performance
- ✅ Thread pool management
- ✅ Graceful shutdown procedures

### Test Statistics
- **Total Tests**: 62+
- **Coverage Areas**: TCP networking, cryptography, key management, integration scenarios
- **Test Types**: Unit tests, integration tests, performance tests, security validation

## 🛠️ Build Configuration

### Maven Configuration
The project uses Maven with the following key configurations:

#### Dependencies
```xml
<dependencies>
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.11</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

#### Build Plugins
- **Maven Compiler Plugin**: Java 8 target compatibility
- **Maven Shade Plugin**: Creates fat JAR with all dependencies
- **Maven JAR Plugin**: Configures main class (`com.copan.App`)

#### Available Build Commands
```bash
# Compile project
mvn compile

# Run tests
mvn test

# Create JAR files
mvn package

# Install to local repository  
mvn install

# Clean build artifacts
mvn clean
```

## 📊 Performance Characteristics

### Key Generation Performance
- **RSA-2048**: ~100-500ms per key pair
- **RSA-4096**: ~1-5 seconds per key pair
- **Batch Generation**: Optimized for multiple key creation

### Encryption Performance
- **Message Size Limit**: ~245 bytes per RSA-2048 block (PKCS1 padding)
- **Throughput**: Suitable for command/control messages, not bulk data transfer
- **Connection Overhead**: ~200-500ms for cryptographic handshake

### Server Capacity
- **Concurrent Connections**: Configurable (default 10 threads)
- **Memory Usage**: ~1-2MB per active client connection
- **Scalability**: Thread pool model supports moderate concurrent load

## 🔒 Security Considerations

### Cryptographic Strengths
- **RSA-2048**: Currently secure against classical attacks
- **SHA256**: Cryptographically secure hash function
- **SecureRandom**: Cryptographically strong entropy source
- **PEM Format**: Industry-standard key storage format

### Java 8 Compatibility
The implementation is optimized for Java 8 compatibility:
- **PKCS1 Padding**: Used instead of OAEP (not available in Java 8)
- **SHA256withRSA**: Used instead of PSS signatures
- **Standard Libraries**: No external cryptographic dependencies

### Security Limitations
- **Message Size**: RSA encryption limited to ~245 bytes per block
- **Key Storage**: Keys stored unencrypted on disk (consider adding password protection)
- **Network Transport**: No TLS wrapper (plain TCP with application-layer encryption)
- **Key Distribution**: Manual key exchange (no PKI infrastructure)

### Recommended Security Practices
1. **Key Rotation**: Periodically generate new key pairs
2. **Secure Key Storage**: Protect key files with appropriate file system permissions
3. **Network Security**: Consider additional network-layer protection for production use
4. **Key Size**: Use RSA-3072 or RSA-4096 for high-security applications

## 🐛 Troubleshooting

### Common Issues

#### Connection Refused
```
Error: Connection refused
Solution: Ensure server is running and firewall allows connections
```

#### Key Generation Errors
```
Error: Unable to generate RSA key pair
Solution: Check Java cryptography policy, ensure sufficient entropy
```

#### Encryption Failures
```
Error: Message too long for RSA encryption
Solution: RSA encrypts max ~245 bytes, split larger messages or use hybrid encryption
```

#### Thread Pool Exhaustion
```
Error: RejectedExecutionException
Solution: Increase server thread pool size or implement connection limiting
```

### Debug Mode
Enable detailed logging for troubleshooting:
```java
Logger.getLogger("com.copan").setLevel(Level.FINE);
```

## 📚 API Reference

### Core Classes

#### TCPServer
```java
public class TCPServer {
    public TCPServer(int port, int maxThreads)
    public void start() throws IOException
    public void stop()
    public boolean isRunning()
    public int getPort()
    public int getActiveConnections()
}
```

#### TCPClient  
```java
public class TCPClient {
    public TCPClient(String host, int port)
    public void connect() throws IOException
    public void disconnect()
    public boolean isConnected()
    public String sendAndReceive(String message) throws IOException
    public void sendMessage(String message) throws IOException
}
```

#### CriptoServer
```java
public class CriptoServer extends TCPServer {
    public CriptoServer(int port, int maxThreads, KeyManager keyManager, String keyName)
    public KeyManager getKeyManager()
    public boolean hasValidKeys()
}
```

#### CriptoClient
```java
public class CriptoClient extends TCPClient {
    public CriptoClient(String host, int port, KeyManager keyManager, String keyName)
    public boolean isCryptoHandshakeComplete()
    // Inherits all TCPClient methods with automatic encryption
}
```

#### KeyGenerator
```java
public class KeyGenerator {
    public static final int KEY_SIZE_1024 = 1024;
    public static final int KEY_SIZE_2048 = 2048;
    public static final int KEY_SIZE_3072 = 3072;
    public static final int KEY_SIZE_4096 = 4096;
    
    public KeyGenerator()
    public KeyGenerator(int keySize)
    public KeyPair generateKeyPair()
    public KeyPair[] generateKeyPairs(int count)
}
```

#### KeyManager
```java
public class KeyManager {
    public KeyManager(String keysDirectory)
    public void saveKeyPair(String name, KeyPair keyPair)
    public PublicKey loadPublicKey(String name)
    public PrivateKey loadPrivateKey(String name)
    public boolean keyPairExists(String name)
    public void deleteKeyPair(String name)
}
```

### Utility Classes

#### CryptoUtils
```java
public class CryptoUtils {
    public static String encrypt(String plainText, PublicKey publicKey)
    public static String decrypt(String cipherText, PrivateKey privateKey)
    public static String sign(String message, PrivateKey privateKey)
    public static boolean verify(String message, String signature, PublicKey publicKey)
    public static SecureMessage createSecureMessage(String message, PrivateKey senderKey, PublicKey recipientKey)
    public static String verifyAndDecrypt(SecureMessage secureMsg, PrivateKey recipientKey, PublicKey senderKey)
}
```

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make changes and add tests
4. Run the full test suite: `mvn test`
5. Submit a pull request

### Code Style
- Follow Java naming conventions
- Add comprehensive Javadoc comments
- Include unit tests for new functionality
- Maintain Java 8 compatibility

### Testing Requirements
- All new features must include unit tests
- Integration tests for client-server interactions
- Security tests for cryptographic functionality
- Performance tests for key generation

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 Related Projects

- **OpenSSL**: For PEM key format compatibility
- **Bouncy Castle**: Alternative cryptographic provider
- **Apache MINA**: Advanced networking framework
- **Netty**: High-performance network application framework

## 📖 Additional Documentation

- [CRYPTO_README.md](CRYPTO_README.md) - Detailed cryptographic implementation guide
- [KEYGEN_README.md](KEYGEN_README.md) - Key generation class documentation
- [JavaDoc](target/site/apidocs/) - Generated API documentation (after `mvn javadoc:javadoc`)

---

**jSRPC** - Secure tunnel for XML/RPC communications with enterprise-grade cryptographic protection.

*For questions, issues, or contributions, please use the GitHub issue tracker.* 
