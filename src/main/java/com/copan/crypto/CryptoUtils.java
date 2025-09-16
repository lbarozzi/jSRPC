package com.copan.crypto;

import javax.crypto.Cipher;
import java.security.*;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Utility per operazioni crittografiche: cifratura, decifratura, firma digitale e verifica.
 * Utilizza RSA con padding PKCS1 per compatibilità con Java 8 e SHA256withRSA per le firme.
 */
public class CryptoUtils {
    private static final Logger logger = Logger.getLogger(CryptoUtils.class.getName());
    
    // Algoritmi e parametri crittografici
    private static final String RSA_ALGORITHM = "RSA";
    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING" ; 
    //"RSA/ECB/PKCS1Padding";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String HASH_ALGORITHM = "SHA-256";
    
    /**
     * Cifra un messaggio usando la chiave pubblica del destinatario
     */
    public static String encrypt(String plainText, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
            String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
            
            logger.fine("Messaggio cifrato con successo");
            return encryptedText;
            
        } catch (Exception e) {
            logger.severe("Errore nella cifratura del messaggio: " + e.getMessage());
            throw new RuntimeException("Errore nella cifratura", e);
        }
    }
    
    /**
     * Decifra un messaggio usando la propria chiave privata
     */
    public static String decrypt(String encryptedText, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            String plainText = new String(decryptedBytes, "UTF-8");
            logger.fine("Messaggio decifrato con successo");
            return plainText;
            
        } catch (Exception e) {
            logger.severe("Errore nella decifratura del messaggio: " + e.getMessage());
            throw new RuntimeException("Errore nella decifratura", e);
        }
    }
    
    /**
     * Firma un messaggio usando la propria chiave privata
     */
    public static String sign(String message, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(message.getBytes("UTF-8"));
            
            byte[] signatureBytes = signature.sign();
            String signatureText = Base64.getEncoder().encodeToString(signatureBytes);
            
            logger.fine("Messaggio firmato con successo");
            return signatureText;
            
        } catch (Exception e) {
            logger.severe("Errore nella firma del messaggio: " + e.getMessage());
            throw new RuntimeException("Errore nella firma", e);
        }
    }
    
    /**
     * Verifica la firma di un messaggio usando la chiave pubblica del mittente
     */
    public static boolean verify(String message, String signatureText, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(message.getBytes("UTF-8"));
            
            byte[] signatureBytes = Base64.getDecoder().decode(signatureText);
            boolean isValid = signature.verify(signatureBytes);
            
            logger.fine("Verifica firma: " + (isValid ? "valida" : "non valida"));
            return isValid;
            
        } catch (Exception e) {
            logger.severe("Errore nella verifica della firma: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Calcola l'hash SHA-256 di un messaggio
     */
    public static String hash(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(message.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            logger.severe("Errore nel calcolo hash: " + e.getMessage());
            throw new RuntimeException("Errore nel calcolo hash", e);
        }
    }
    
    /**
     * Verifica l'integrità di un messaggio confrontando gli hash
     */
    public static boolean verifyIntegrity(String message, String expectedHash) {
        String actualHash = hash(message);
        return actualHash.equals(expectedHash);
    }
    
    /**
     * Converte una chiave pubblica in formato stringa Base64 per trasmissione
     */
    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    
    /**
     * Ricostruisce una chiave pubblica da stringa Base64
     */
    public static PublicKey stringToPublicKey(String keyString) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyString);
            java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            logger.severe("Errore nella conversione chiave pubblica: " + e.getMessage());
            throw new RuntimeException("Errore nella conversione chiave", e);
        }
    }
    
    /**
     * Ricostruisce una chiave pubblica da byte array (alias per compatibilità)
     */
    public static PublicKey decodePublicKey(byte[] keyBytes) {
        try {
            java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            logger.severe("Errore nella decodifica chiave pubblica: " + e.getMessage());
            throw new RuntimeException("Errore nella decodifica chiave", e);
        }
    }
    
    /**
     * Crea un messaggio sicuro: cifrato con la chiave pubblica del destinatario 
     * e firmato con la chiave privata del mittente
     */
    public static SecureMessage createSecureMessage(String message, PrivateKey senderPrivateKey, 
                                                   PublicKey recipientPublicKey) {
        try {
            // Cifra il messaggio con la chiave pubblica del destinatario
            String encryptedContent = encrypt(message, recipientPublicKey);
            
            // Firma il messaggio originale con la chiave privata del mittente
            String signature = sign(message, senderPrivateKey);
            
            // Ottieni la chiave pubblica del mittente in formato stringa per identificazione
            // Qui dovremmo passarla come parametro, ma per semplicità usiamo una stringa vuota
            String senderPublicKeyString = ""; // Sarà gestita dal client/server
            
            return new SecureMessage(encryptedContent, signature, senderPublicKeyString);
        } catch (Exception e) {
            logger.severe("Errore nella creazione del messaggio sicuro: " + e.getMessage());
            throw new RuntimeException("Errore nella creazione messaggio sicuro", e);
        }
    }
    
    /**
     * Verifica e decritta un messaggio sicuro
     */
    public static String verifyAndDecrypt(SecureMessage secureMessage, PrivateKey recipientPrivateKey,
                                        PublicKey senderPublicKey) {
        try {
            // Decritta il contenuto con la nostra chiave privata
            String decryptedMessage = decrypt(secureMessage.getEncryptedContent(), recipientPrivateKey);
            
            // Verifica la firma con la chiave pubblica del mittente
            boolean isSignatureValid = verify(decryptedMessage, secureMessage.getSignature(), senderPublicKey);
            
            if (!isSignatureValid) {
                throw new SecurityException("Firma del messaggio non valida");
            }
            
            logger.fine("Messaggio sicuro verificato e decriptato con successo");
            return decryptedMessage;
        } catch (Exception e) {
            logger.severe("Errore nella verifica/decrittazione del messaggio: " + e.getMessage());
            throw new RuntimeException("Errore nella verifica del messaggio", e);
        }
    }
}