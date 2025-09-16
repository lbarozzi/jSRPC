package com.copan.crypto;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;

/**
 * Integration tests between KeyGenerator and existing crypto infrastructure.
 */
public class KeyGeneratorIntegrationTest {
    
    private static final String TEST_KEYS_DIR = "test_integration_keys";
    private KeyGenerator keyGenerator;
    private KeyManager keyManager;
    
    @Before
    public void setUp() throws IOException {
        cleanupTestKeys();
        keyGenerator = new KeyGenerator();
        keyManager = new KeyManager(TEST_KEYS_DIR);
    }
    
    @After
    public void tearDown() {
        cleanupTestKeys();
    }
    
    private void cleanupTestKeys() {
        try {
            if (Files.exists(Paths.get(TEST_KEYS_DIR))) {
                Files.walk(Paths.get(TEST_KEYS_DIR))
                     .map(path -> path.toFile())
                     .forEach(file -> file.delete());
                Files.deleteIfExists(Paths.get(TEST_KEYS_DIR));
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    @Test
    public void testKeyGeneratorWithKeyManager() {
        // Generate a key pair using KeyGenerator
        KeyPair generatedKeyPair = keyGenerator.generateKeyPair();
        
        // Save it using KeyManager
        String keyName = "integration_test";
        keyManager.saveKeyPair(keyName, generatedKeyPair);
        
        // Load it back using KeyManager
        KeyPair loadedKeyPair = new KeyPair(
            keyManager.loadPublicKey(keyName),
            keyManager.loadPrivateKey(keyName)
        );
        
        // Verify the keys are equivalent
        assertArrayEquals("Public keys should be equivalent",
                         generatedKeyPair.getPublic().getEncoded(),
                         loadedKeyPair.getPublic().getEncoded());
        assertArrayEquals("Private keys should be equivalent",
                         generatedKeyPair.getPrivate().getEncoded(),
                         loadedKeyPair.getPrivate().getEncoded());
    }
    
    @Test
    public void testKeyGeneratorWithCryptoUtils() {
        // Generate a key pair using KeyGenerator
        KeyPair keyPair = keyGenerator.generateKeyPair();
        
        String originalMessage = "Hello from KeyGenerator integration test!";
        
        // Test encryption/decryption with CryptoUtils
        String encryptedMessage = CryptoUtils.encrypt(originalMessage, keyPair.getPublic());
        String decryptedMessage = CryptoUtils.decrypt(encryptedMessage, keyPair.getPrivate());
        
        assertEquals("Messages should match after encryption/decryption", 
                    originalMessage, decryptedMessage);
        
        // Test digital signature with CryptoUtils
        String signature = CryptoUtils.sign(originalMessage, keyPair.getPrivate());
        boolean isValid = CryptoUtils.verify(originalMessage, signature, keyPair.getPublic());
        
        assertTrue("Signature should be valid", isValid);
    }
    
    @Test
    public void testKeyGeneratorWithSecureMessage() {
        // Generate two key pairs for sender and recipient
        KeyPair senderKeyPair = keyGenerator.generateKeyPair();
        KeyPair recipientKeyPair = keyGenerator.generateKeyPair();
        
        String originalMessage = "Secure message using KeyGenerator keys";
        
        // Create secure message using CryptoUtils
        SecureMessage secureMessage = CryptoUtils.createSecureMessage(
            originalMessage,
            senderKeyPair.getPrivate(),
            recipientKeyPair.getPublic()
        );
        
        // Verify and decrypt the secure message
        String decryptedMessage = CryptoUtils.verifyAndDecrypt(
            secureMessage,
            recipientKeyPair.getPrivate(),
            senderKeyPair.getPublic()
        );
        
        assertEquals("Messages should match after secure messaging", 
                    originalMessage, decryptedMessage);
    }
    
    @Test
    public void testKeyGeneratorReplacesKeyManagerGeneration() {
        // Generate keys using KeyGenerator
        KeyPair keyGeneratorPair = keyGenerator.generateKeyPair();
        
        // Generate keys using KeyManager (existing functionality)
        KeyPair keyManagerPair = keyManager.generateKeyPair();
        
        // Both should produce valid, different RSA key pairs
        assertNotNull("KeyGenerator pair should not be null", keyGeneratorPair);
        assertNotNull("KeyManager pair should not be null", keyManagerPair);
        
        // Keys should be different
        assertFalse("Generated keys should be different",
                   keyGeneratorPair.getPublic().equals(keyManagerPair.getPublic()));
        
        // Both should work with CryptoUtils
        String testMessage = "Testing both key generators";
        
        // Test KeyGenerator keys
        String encryptedWithKG = CryptoUtils.encrypt(testMessage, keyGeneratorPair.getPublic());
        String decryptedWithKG = CryptoUtils.decrypt(encryptedWithKG, keyGeneratorPair.getPrivate());
        assertEquals("KeyGenerator keys should work", testMessage, decryptedWithKG);
        
        // Test KeyManager keys
        String encryptedWithKM = CryptoUtils.encrypt(testMessage, keyManagerPair.getPublic());
        String decryptedWithKM = CryptoUtils.decrypt(encryptedWithKM, keyManagerPair.getPrivate());
        assertEquals("KeyManager keys should work", testMessage, decryptedWithKM);
    }
}