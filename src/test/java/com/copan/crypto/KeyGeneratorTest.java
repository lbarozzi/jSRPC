package com.copan.crypto;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Comprehensive tests for the KeyGenerator class.
 * Tests security properties, key validity, and proper error handling.
 */
public class KeyGeneratorTest {
    
    private KeyGenerator defaultGenerator;
    
    @Before
    public void setUp() {
        defaultGenerator = new KeyGenerator();
    }
    
    @Test
    public void testDefaultConstructor() {
        assertNotNull("Default generator should not be null", defaultGenerator);
        assertEquals("Default key size should be 2048", KeyGenerator.KEY_SIZE_2048, defaultGenerator.getKeySize());
        assertEquals("Algorithm should be RSA", "RSA", defaultGenerator.getAlgorithm());
        assertEquals("Public exponent should be 65537", 
                    java.math.BigInteger.valueOf(65537), defaultGenerator.getPublicExponent());
    }
    
    @Test
    public void testConstructorWithKeySize() {
        KeyGenerator generator = new KeyGenerator(KeyGenerator.KEY_SIZE_4096);
        assertEquals("Key size should be 4096", KeyGenerator.KEY_SIZE_4096, generator.getKeySize());
    }
    
    @Test
    public void testConstructorWithCustomSecureRandom() {
        SecureRandom customRandom = new SecureRandom();
        KeyGenerator generator = new KeyGenerator(KeyGenerator.KEY_SIZE_2048, customRandom);
        assertEquals("Key size should be 2048", KeyGenerator.KEY_SIZE_2048, generator.getKeySize());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInvalidKeySize() {
        new KeyGenerator(512); // Too small
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNonMultipleOf64KeySize() {
        new KeyGenerator(2049); // Not multiple of 64
    }
    
    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullSecureRandom() {
        new KeyGenerator(KeyGenerator.KEY_SIZE_2048, null);
    }
    
    @Test
    public void testGenerateKeyPair() {
        KeyPair keyPair = defaultGenerator.generateKeyPair();
        
        assertNotNull("Key pair should not be null", keyPair);
        assertNotNull("Public key should not be null", keyPair.getPublic());
        assertNotNull("Private key should not be null", keyPair.getPrivate());
        
        // Verify key types
        assertTrue("Public key should be RSA", keyPair.getPublic() instanceof RSAPublicKey);
        assertTrue("Private key should be RSA", keyPair.getPrivate() instanceof RSAPrivateKey);
        
        // Verify key sizes
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        
        assertEquals("Public key size should match", 
                    KeyGenerator.KEY_SIZE_2048, publicKey.getModulus().bitLength());
        assertEquals("Private key size should match", 
                    KeyGenerator.KEY_SIZE_2048, privateKey.getModulus().bitLength());
        
        // Verify public exponent
        assertEquals("Public exponent should be 65537", 
                    java.math.BigInteger.valueOf(65537), publicKey.getPublicExponent());
    }
    
    @Test
    public void testGenerateKeyPairDifferentSizes() {
        int[] keySizes = {KeyGenerator.KEY_SIZE_1024, KeyGenerator.KEY_SIZE_2048, 
                         KeyGenerator.KEY_SIZE_3072, KeyGenerator.KEY_SIZE_4096};
        
        for (int keySize : keySizes) {
            KeyGenerator generator = new KeyGenerator(keySize);
            KeyPair keyPair = generator.generateKeyPair();
            
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            assertEquals("Key size should match for " + keySize + " bits", 
                        keySize, publicKey.getModulus().bitLength());
        }
    }
    
    @Test
    public void testGenerateMultipleKeyPairs() {
        int count = 5;
        KeyPair[] keyPairs = defaultGenerator.generateKeyPairs(count);
        
        assertNotNull("Key pairs array should not be null", keyPairs);
        assertEquals("Should generate correct number of key pairs", count, keyPairs.length);
        
        // Verify all key pairs are unique and valid
        for (int i = 0; i < count; i++) {
            assertNotNull("Key pair " + i + " should not be null", keyPairs[i]);
            assertNotNull("Public key " + i + " should not be null", keyPairs[i].getPublic());
            assertNotNull("Private key " + i + " should not be null", keyPairs[i].getPrivate());
            
            // Verify uniqueness by comparing with other key pairs
            for (int j = i + 1; j < count; j++) {
                assertFalse("Key pairs should be unique",
                          keyPairs[i].getPublic().equals(keyPairs[j].getPublic()));
                assertFalse("Private keys should be unique",
                          keyPairs[i].getPrivate().equals(keyPairs[j].getPrivate()));
            }
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGenerateKeyPairsWithZeroCount() {
        defaultGenerator.generateKeyPairs(0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGenerateKeyPairsWithNegativeCount() {
        defaultGenerator.generateKeyPairs(-1);
    }
    
    @Test
    public void testKeyPairRandomness() {
        // Generate multiple key pairs and verify they are different
        KeyPair keyPair1 = defaultGenerator.generateKeyPair();
        KeyPair keyPair2 = defaultGenerator.generateKeyPair();
        
        assertFalse("Public keys should be different", 
                   keyPair1.getPublic().equals(keyPair2.getPublic()));
        assertFalse("Private keys should be different", 
                   keyPair1.getPrivate().equals(keyPair2.getPrivate()));
        
        // Verify moduli are different
        RSAPublicKey pubKey1 = (RSAPublicKey) keyPair1.getPublic();
        RSAPublicKey pubKey2 = (RSAPublicKey) keyPair2.getPublic();
        
        assertFalse("Moduli should be different", 
                   pubKey1.getModulus().equals(pubKey2.getModulus()));
    }
    
    @Test
    public void testKeyPairCryptographicProperties() {
        KeyPair keyPair = defaultGenerator.generateKeyPair();
        
        // Test encryption/decryption to verify key pair validity
        try {
            String testMessage = "Hello, RSA encryption test!";
            
            // Encrypt with public key
            String encryptedMessage = CryptoUtils.encrypt(testMessage, keyPair.getPublic());
            assertNotNull("Encrypted message should not be null", encryptedMessage);
            assertFalse("Encrypted message should be different from original", 
                       testMessage.equals(encryptedMessage));
            
            // Decrypt with private key
            String decryptedMessage = CryptoUtils.decrypt(encryptedMessage, keyPair.getPrivate());
            assertEquals("Decrypted message should match original", testMessage, decryptedMessage);
            
        } catch (Exception e) {
            fail("Key pair should be cryptographically valid: " + e.getMessage());
        }
    }
    
    @Test
    public void testKeyPairDigitalSignature() {
        KeyPair keyPair = defaultGenerator.generateKeyPair();
        
        // Test digital signature to verify key pair validity
        try {
            String testMessage = "Hello, digital signature test!";
            
            // Sign with private key
            String signature = CryptoUtils.sign(testMessage, keyPair.getPrivate());
            assertNotNull("Signature should not be null", signature);
            
            // Verify with public key
            boolean isValid = CryptoUtils.verify(testMessage, signature, keyPair.getPublic());
            assertTrue("Signature should be valid", isValid);
            
            // Test with tampered message
            boolean isInvalid = CryptoUtils.verify(testMessage + "tampered", signature, keyPair.getPublic());
            assertFalse("Signature should be invalid for tampered message", isInvalid);
            
        } catch (Exception e) {
            fail("Key pair should support digital signatures: " + e.getMessage());
        }
    }
    
    @Test
    public void testKeyGeneratorConstants() {
        assertEquals("KEY_SIZE_1024 should be 1024", 1024, KeyGenerator.KEY_SIZE_1024);
        assertEquals("KEY_SIZE_2048 should be 2048", 2048, KeyGenerator.KEY_SIZE_2048);
        assertEquals("KEY_SIZE_3072 should be 3072", 3072, KeyGenerator.KEY_SIZE_3072);
        assertEquals("KEY_SIZE_4096 should be 4096", 4096, KeyGenerator.KEY_SIZE_4096);
    }
    
    @Test
    public void testKeyGeneratorWithDifferentSecureRandom() {
        // Test with different SecureRandom implementations
        SecureRandom secureRandom1 = new SecureRandom();
        SecureRandom secureRandom2 = new SecureRandom();
        
        KeyGenerator generator1 = new KeyGenerator(KeyGenerator.KEY_SIZE_2048, secureRandom1);
        KeyGenerator generator2 = new KeyGenerator(KeyGenerator.KEY_SIZE_2048, secureRandom2);
        
        KeyPair keyPair1 = generator1.generateKeyPair();
        KeyPair keyPair2 = generator2.generateKeyPair();
        
        // Even with different SecureRandom instances, keys should still be different
        assertFalse("Keys generated with different SecureRandom should be different",
                   keyPair1.getPublic().equals(keyPair2.getPublic()));
    }
    
    @Test
    public void testPerformanceWithMultipleKeySizes() {
        // Simple performance test to ensure reasonable generation times
        int[] keySizes = {KeyGenerator.KEY_SIZE_1024, KeyGenerator.KEY_SIZE_2048};
        
        for (int keySize : keySizes) {
            KeyGenerator generator = new KeyGenerator(keySize);
            
            long startTime = System.currentTimeMillis();
            KeyPair keyPair = generator.generateKeyPair();
            long endTime = System.currentTimeMillis();
            
            long duration = endTime - startTime;
            
            // Key generation should complete within reasonable time (30 seconds)
            assertTrue("Key generation for " + keySize + " bits should complete within 30 seconds", 
                      duration < 30000);
            
            assertNotNull("Generated key pair should not be null", keyPair);
        }
    }
    
    @Test
    public void testKeyGeneratorToleratesRepeatedGeneration() {
        // Test that generator can be used multiple times without issues
        for (int i = 0; i < 10; i++) {
            KeyPair keyPair = defaultGenerator.generateKeyPair();
            assertNotNull("Key pair " + i + " should not be null", keyPair);
            
            // Verify each key pair is functional
            try {
                String testMsg = "Test message " + i;
                String encrypted = CryptoUtils.encrypt(testMsg, keyPair.getPublic());
                String decrypted = CryptoUtils.decrypt(encrypted, keyPair.getPrivate());
                assertEquals("Round-trip encryption should work for key pair " + i, testMsg, decrypted);
            } catch (Exception e) {
                fail("Key pair " + i + " should be functional: " + e.getMessage());
            }
        }
    }
}