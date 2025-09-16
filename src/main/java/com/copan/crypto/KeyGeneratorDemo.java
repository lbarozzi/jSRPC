package com.copan.crypto;

import java.security.KeyPair;
import java.security.SecureRandom;

/**
 * Demonstration of the KeyGenerator class usage.
 * Shows various ways to create secure RSA key pairs.
 */
public class KeyGeneratorDemo {
    
    public static void main(String[] args) {
        System.out.println("=== KeyGenerator Demo ===\n");
        
        // 1. Default KeyGenerator (2048-bit keys)
        demonstrateDefaultKeyGenerator();
        
        // 2. KeyGenerator with custom key size
        demonstrateCustomKeySizeGenerator();
        
        // 3. KeyGenerator with custom SecureRandom
        demonstrateCustomSecureRandomGenerator();
        
        // 4. Multiple key generation
        demonstrateMultipleKeyGeneration();
        
        // 5. Integration with existing crypto utilities
        demonstrateCryptoIntegration();
        
        System.out.println("\n=== Demo completed successfully ===");
    }
    
    private static void demonstrateDefaultKeyGenerator() {
        System.out.println("1. Default KeyGenerator (2048-bit RSA):");
        
        KeyGenerator generator = new KeyGenerator();
        System.out.println("   - Key size: " + generator.getKeySize() + " bits");
        System.out.println("   - Algorithm: " + generator.getAlgorithm());
        System.out.println("   - Public exponent: " + generator.getPublicExponent());
        
        KeyPair keyPair = generator.generateKeyPair();
        System.out.println("   - Generated key pair: " + (keyPair != null ? "SUCCESS" : "FAILED"));
        System.out.println();
    }
    
    private static void demonstrateCustomKeySizeGenerator() {
        System.out.println("2. KeyGenerator with custom key sizes:");
        
        int[] keySizes = {KeyGenerator.KEY_SIZE_1024, KeyGenerator.KEY_SIZE_2048, 
                         KeyGenerator.KEY_SIZE_3072, KeyGenerator.KEY_SIZE_4096};
        
        for (int keySize : keySizes) {
            KeyGenerator generator = new KeyGenerator(keySize);
            long startTime = System.currentTimeMillis();
            KeyPair keyPair = generator.generateKeyPair();
            long endTime = System.currentTimeMillis();
            
            System.out.println("   - " + keySize + " bits: " + 
                              (keyPair != null ? "SUCCESS" : "FAILED") + 
                              " (" + (endTime - startTime) + "ms)");
        }
        System.out.println();
    }
    
    private static void demonstrateCustomSecureRandomGenerator() {
        System.out.println("3. KeyGenerator with custom SecureRandom:");
        
        SecureRandom customRandom = new SecureRandom();
        KeyGenerator generator = new KeyGenerator(KeyGenerator.KEY_SIZE_2048, customRandom);
        
        KeyPair keyPair = generator.generateKeyPair();
        System.out.println("   - Custom SecureRandom: " + (keyPair != null ? "SUCCESS" : "FAILED"));
        System.out.println();
    }
    
    private static void demonstrateMultipleKeyGeneration() {
        System.out.println("4. Multiple key generation:");
        
        KeyGenerator generator = new KeyGenerator();
        int count = 5;
        
        long startTime = System.currentTimeMillis();
        KeyPair[] keyPairs = generator.generateKeyPairs(count);
        long endTime = System.currentTimeMillis();
        
        System.out.println("   - Generated " + count + " key pairs: " + 
                          (keyPairs.length == count ? "SUCCESS" : "FAILED") + 
                          " (" + (endTime - startTime) + "ms total)");
        
        // Verify uniqueness
        boolean allUnique = true;
        for (int i = 0; i < keyPairs.length && allUnique; i++) {
            for (int j = i + 1; j < keyPairs.length && allUnique; j++) {
                if (keyPairs[i].getPublic().equals(keyPairs[j].getPublic())) {
                    allUnique = false;
                }
            }
        }
        System.out.println("   - All keys unique: " + (allUnique ? "SUCCESS" : "FAILED"));
        System.out.println();
    }
    
    private static void demonstrateCryptoIntegration() {
        System.out.println("5. Integration with existing crypto utilities:");
        
        KeyGenerator generator = new KeyGenerator();
        KeyPair keyPair = generator.generateKeyPair();
        
        String originalMessage = "Hello, secure world!";
        
        try {
            // Test encryption/decryption
            String encrypted = CryptoUtils.encrypt(originalMessage, keyPair.getPublic());
            String decrypted = CryptoUtils.decrypt(encrypted, keyPair.getPrivate());
            boolean encryptionWorks = originalMessage.equals(decrypted);
            
            System.out.println("   - Encryption/Decryption: " + (encryptionWorks ? "SUCCESS" : "FAILED"));
            
            // Test digital signature
            String signature = CryptoUtils.sign(originalMessage, keyPair.getPrivate());
            boolean signatureValid = CryptoUtils.verify(originalMessage, signature, keyPair.getPublic());
            
            System.out.println("   - Digital Signature: " + (signatureValid ? "SUCCESS" : "FAILED"));
            
            // Test with KeyManager
            KeyManager keyManager = new KeyManager("demo_keys");
            keyManager.saveKeyPair("demo", keyPair);
            boolean keysSaved = keyManager.keyPairExists("demo");
            
            System.out.println("   - KeyManager Integration: " + (keysSaved ? "SUCCESS" : "FAILED"));
            
            // Cleanup
            keyManager.deleteKeyPair("demo");
            
        } catch (Exception e) {
            System.out.println("   - Integration test failed: " + e.getMessage());
        }
        
        System.out.println();
    }
}