# KeyGenerator Class

The `KeyGenerator` class provides secure RSA key pair generation following Java 8 best practices and current cryptographic standards.

## Features

- **Secure Random Generation**: Uses cryptographically strong random number generators
- **Configurable Key Sizes**: Supports 1024, 2048, 3072, and 4096-bit RSA keys
- **Best Practices**: Implements current security recommendations
- **Integration**: Works seamlessly with existing crypto infrastructure
- **Thread-Safe**: Can be safely used in multi-threaded environments

## Quick Start

### Basic Usage

```java
// Create a default KeyGenerator (2048-bit keys)
KeyGenerator generator = new KeyGenerator();

// Generate a key pair
KeyPair keyPair = generator.generateKeyPair();

// Use the keys with CryptoUtils
String message = "Hello, secure world!";
String encrypted = CryptoUtils.encrypt(message, keyPair.getPublic());
String decrypted = CryptoUtils.decrypt(encrypted, keyPair.getPrivate());
```

### Custom Key Size

```java
// Create a KeyGenerator with 4096-bit keys for high security
KeyGenerator generator = new KeyGenerator(KeyGenerator.KEY_SIZE_4096);
KeyPair keyPair = generator.generateKeyPair();
```

### Multiple Key Generation

```java
KeyGenerator generator = new KeyGenerator();

// Generate 5 key pairs at once
KeyPair[] keyPairs = generator.generateKeyPairs(5);
```

### Custom SecureRandom

```java
SecureRandom customRandom = new SecureRandom();
KeyGenerator generator = new KeyGenerator(KeyGenerator.KEY_SIZE_2048, customRandom);
KeyPair keyPair = generator.generateKeyPair();
```

## Key Size Recommendations

- **1024 bits**: Deprecated, provided for compatibility only
- **2048 bits**: Current minimum recommended (default)
- **3072 bits**: Better security for sensitive applications
- **4096 bits**: High security for critical applications

## Security Features

1. **Strong Random Number Generation**: Uses SHA1PRNG or system default SecureRandom
2. **Proper Public Exponent**: Uses the standard value 65537
3. **Key Size Validation**: Prevents weak key sizes
4. **Explicit Seeding**: Forces proper random number generator initialization

## Integration with Existing Code

The KeyGenerator works seamlessly with the existing crypto infrastructure:

```java
// Generate keys with KeyGenerator
KeyGenerator generator = new KeyGenerator();
KeyPair keyPair = generator.generateKeyPair();

// Save with KeyManager
KeyManager keyManager = new KeyManager();
keyManager.saveKeyPair("mykey", keyPair);

// Use with CryptoUtils
String message = "Secure message";
String encrypted = CryptoUtils.encrypt(message, keyPair.getPublic());
String signature = CryptoUtils.sign(message, keyPair.getPrivate());
```

## Error Handling

The KeyGenerator throws `KeyGenerationException` for key generation failures:

```java
try {
    KeyPair keyPair = generator.generateKeyPair();
} catch (KeyGenerator.KeyGenerationException e) {
    System.err.println("Key generation failed: " + e.getMessage());
}
```

## Performance Considerations

Key generation times vary by key size:

- **1024 bits**: ~50ms
- **2048 bits**: ~200-400ms  
- **3072 bits**: ~200-300ms
- **4096 bits**: ~1000-1500ms

## Running the Demo

To see the KeyGenerator in action:

```bash
mvn compile exec:java -Dexec.mainClass="com.copan.crypto.KeyGeneratorDemo"
```

## API Reference

### Constructors

- `KeyGenerator()` - Creates generator with 2048-bit keys
- `KeyGenerator(int keySize)` - Creates generator with specified key size
- `KeyGenerator(int keySize, SecureRandom secureRandom)` - Creates generator with custom SecureRandom

### Methods

- `KeyPair generateKeyPair()` - Generates a single key pair
- `KeyPair[] generateKeyPairs(int count)` - Generates multiple key pairs
- `int getKeySize()` - Returns configured key size
- `String getAlgorithm()` - Returns "RSA"
- `BigInteger getPublicExponent()` - Returns 65537

### Constants

- `KEY_SIZE_1024` - 1024-bit key size
- `KEY_SIZE_2048` - 2048-bit key size (default)
- `KEY_SIZE_3072` - 3072-bit key size  
- `KEY_SIZE_4096` - 4096-bit key size