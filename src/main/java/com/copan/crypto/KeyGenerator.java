package com.copan.crypto;

import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.logging.Logger;

/**
 * Secure RSA key pair generator following Java 8 best practices.
 * Provides methods to generate cryptographically strong RSA key pairs
 * with configurable key sizes and secure random number generation.
 */
public class KeyGenerator {
    private static final Logger logger = Logger.getLogger(KeyGenerator.class.getName());
    
    // RSA algorithm constants
    private static final String ALGORITHM = "RSA";
    private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";
    
    // Recommended key sizes (in bits)
    public static final int KEY_SIZE_1024 = 1024;   // Deprecated, provided for compatibility
    public static final int KEY_SIZE_2048 = 2048;   // Current minimum recommended
    public static final int KEY_SIZE_3072 = 3072;   // Better security
    public static final int KEY_SIZE_4096 = 4096;   // High security
    
    // Default key size following current security recommendations
    private static final int DEFAULT_KEY_SIZE = KEY_SIZE_2048;
    
    // RSA public exponent (commonly used value)
    private static final java.math.BigInteger PUBLIC_EXPONENT = java.math.BigInteger.valueOf(65537);
    
    private final SecureRandom secureRandom;
    private final int keySize;
    
    /**
     * Creates a KeyGenerator with default key size (2048 bits) and secure random.
     * Uses the strongest available SecureRandom implementation.
     */
    public KeyGenerator() {
        this(DEFAULT_KEY_SIZE);
    }
    
    /**
     * Creates a KeyGenerator with specified key size and secure random.
     * 
     * @param keySize the RSA key size in bits (must be >= 1024 and multiple of 64)
     * @throws IllegalArgumentException if keySize is invalid
     */
    public KeyGenerator(int keySize) {
        validateKeySize(keySize);
        this.keySize = keySize;
        this.secureRandom = createSecureRandom();
        logger.info("Initialized KeyGenerator with " + keySize + "-bit RSA keys");
    }
    
    /**
     * Creates a KeyGenerator with specified key size and custom SecureRandom.
     * 
     * @param keySize the RSA key size in bits
     * @param secureRandom the SecureRandom instance to use
     * @throws IllegalArgumentException if keySize is invalid
     * @throws NullPointerException if secureRandom is null
     */
    public KeyGenerator(int keySize, SecureRandom secureRandom) {
        validateKeySize(keySize);
        if (secureRandom == null) {
            throw new NullPointerException("SecureRandom cannot be null");
        }
        this.keySize = keySize;
        this.secureRandom = secureRandom;
        logger.info("Initialized KeyGenerator with " + keySize + "-bit RSA keys and custom SecureRandom");
    }
    
    /**
     * Generates a new RSA key pair using secure random number generation.
     * The generated keys follow current cryptographic best practices.
     * 
     * @return a new RSA KeyPair
     * @throws KeyGenerationException if key generation fails
     */
    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            
            // Use RSAKeyGenParameterSpec for better control over key generation
            RSAKeyGenParameterSpec keyGenSpec = new RSAKeyGenParameterSpec(keySize, PUBLIC_EXPONENT);
            keyPairGenerator.initialize(keyGenSpec, secureRandom);
            
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            logger.fine("Generated new " + keySize + "-bit RSA key pair");
            return keyPair;
            
        } catch (NoSuchAlgorithmException e) {
            logger.severe("RSA algorithm not available: " + e.getMessage());
            throw new KeyGenerationException("RSA algorithm not supported", e);
        } catch (InvalidAlgorithmParameterException e) {
            logger.severe("Invalid RSA key generation parameters: " + e.getMessage());
            throw new KeyGenerationException("Invalid key generation parameters", e);
        } catch (Exception e) {
            logger.severe("Unexpected error during key generation: " + e.getMessage());
            throw new KeyGenerationException("Key generation failed", e);
        }
    }
    
    /**
     * Generates multiple RSA key pairs.
     * 
     * @param count the number of key pairs to generate (must be > 0)
     * @return array of KeyPair objects
     * @throws IllegalArgumentException if count <= 0
     * @throws KeyGenerationException if key generation fails
     */
    public KeyPair[] generateKeyPairs(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        
        KeyPair[] keyPairs = new KeyPair[count];
        for (int i = 0; i < count; i++) {
            keyPairs[i] = generateKeyPair();
        }
        
        logger.info("Generated " + count + " RSA key pairs");
        return keyPairs;
    }
    
    /**
     * Gets the configured key size.
     * 
     * @return the key size in bits
     */
    public int getKeySize() {
        return keySize;
    }
    
    /**
     * Gets the algorithm used for key generation.
     * 
     * @return the algorithm name ("RSA")
     */
    public String getAlgorithm() {
        return ALGORITHM;
    }
    
    /**
     * Gets the public exponent used in RSA key generation.
     * 
     * @return the public exponent (typically 65537)
     */
    public java.math.BigInteger getPublicExponent() {
        return PUBLIC_EXPONENT;
    }
    
    /**
     * Validates that the key size meets security requirements.
     * 
     * @param keySize the key size to validate
     * @throws IllegalArgumentException if keySize is invalid
     */
    private void validateKeySize(int keySize) {
        if (keySize < KEY_SIZE_1024) {
            throw new IllegalArgumentException("Key size must be at least " + KEY_SIZE_1024 + " bits");
        }
        if (keySize % 64 != 0) {
            throw new IllegalArgumentException("Key size must be a multiple of 64 bits");
        }
        if (keySize < KEY_SIZE_2048) {
            logger.warning("Using key size " + keySize + " bits. " +
                          "Consider using at least " + KEY_SIZE_2048 + " bits for better security.");
        }
    }
    
    /**
     * Creates a secure random number generator using the best available algorithm.
     * 
     * @return a properly seeded SecureRandom instance
     */
    private SecureRandom createSecureRandom() {
        try {
            // Try to use the specified algorithm first
            SecureRandom sr = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM);
            // Explicitly seed the random number generator
            sr.nextBytes(new byte[1]); // Force seeding
            return sr;
        } catch (NoSuchAlgorithmException e) {
            logger.warning("Algorithm " + SECURE_RANDOM_ALGORITHM + " not available, using default SecureRandom");
            // Fall back to default SecureRandom
            SecureRandom sr = new SecureRandom();
            sr.nextBytes(new byte[1]); // Force seeding
            return sr;
        }
    }
    
    /**
     * Exception thrown when key generation fails.
     */
    public static class KeyGenerationException extends RuntimeException {
        public KeyGenerationException(String message) {
            super(message);
        }
        
        public KeyGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}