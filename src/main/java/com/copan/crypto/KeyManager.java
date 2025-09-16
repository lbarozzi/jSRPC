package com.copan.crypto;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Gestore per la creazione, salvataggio e caricamento di coppie di chiavi RSA.
 * Le chiavi vengono salvate in formato PEM per compatibilità con standard crittografici.
 */
public class KeyManager {
    private static final Logger logger = Logger.getLogger(KeyManager.class.getName());
    private static final int KEY_SIZE = 2048;
    private static final String ALGORITHM = "RSA";
    private static final String PUBLIC_KEY_HEADER = "-----BEGIN PUBLIC KEY-----";
    private static final String PUBLIC_KEY_FOOTER = "-----END PUBLIC KEY-----";
    private static final String PRIVATE_KEY_HEADER = "-----BEGIN PRIVATE KEY-----";
    private static final String PRIVATE_KEY_FOOTER = "-----END PRIVATE KEY-----";
    
    private final String baseDir;
    
    public KeyManager(String baseDir) {
        this.baseDir = baseDir;
        createDirectoryIfNotExists();
    }
    
    public KeyManager() {
        this("keys");
    }
    
    private void createDirectoryIfNotExists() {
        try {
            Path path = Paths.get(baseDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Creata directory per le chiavi: " + baseDir);
            }
        } catch (IOException e) {
            logger.severe("Errore nella creazione della directory chiavi: " + e.getMessage());
            throw new RuntimeException("Impossibile creare directory chiavi", e);
        }
    }
    
    /**
     * Genera una nuova coppia di chiavi RSA
     */
    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            logger.info("Generata nuova coppia di chiavi RSA " + KEY_SIZE + " bit");
            return keyPair;
        } catch (NoSuchAlgorithmException e) {
            logger.severe("Algoritmo RSA non disponibile: " + e.getMessage());
            throw new RuntimeException("Errore nella generazione delle chiavi", e);
        }
    }
    
    /**
     * Salva una coppia di chiavi su file in formato PEM
     */
    public void saveKeyPair(String keyName, KeyPair keyPair) {
        savePublicKey(keyName, keyPair.getPublic());
        savePrivateKey(keyName, keyPair.getPrivate());
    }
    
    /**
     * Salva la chiave pubblica in formato PEM
     */
    public void savePublicKey(String keyName, PublicKey publicKey) {
        String filename = baseDir + File.separator + keyName + "_public.pem";
        try {
            String encodedKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String pemKey = PUBLIC_KEY_HEADER + "\n" + 
                           formatBase64(encodedKey) + "\n" + 
                           PUBLIC_KEY_FOOTER;
            
            Files.write(Paths.get(filename), pemKey.getBytes());
            logger.info("Chiave pubblica salvata: " + filename);
        } catch (IOException e) {
            logger.severe("Errore nel salvataggio della chiave pubblica: " + e.getMessage());
            throw new RuntimeException("Impossibile salvare chiave pubblica", e);
        }
    }
    
    /**
     * Salva la chiave privata in formato PEM
     */
    public void savePrivateKey(String keyName, PrivateKey privateKey) {
        String filename = baseDir + File.separator + keyName + "_private.pem";
        try {
            String encodedKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            String pemKey = PRIVATE_KEY_HEADER + "\n" + 
                           formatBase64(encodedKey) + "\n" + 
                           PRIVATE_KEY_FOOTER;
            
            Files.write(Paths.get(filename), pemKey.getBytes());
            logger.info("Chiave privata salvata: " + filename);
        } catch (IOException e) {
            logger.severe("Errore nel salvataggio della chiave privata: " + e.getMessage());
            throw new RuntimeException("Impossibile salvare chiave privata", e);
        }
    }
    
    /**
     * Carica una chiave pubblica dal file PEM
     */
    public PublicKey loadPublicKey(String keyName) {
        String filename = baseDir + File.separator + keyName + "_public.pem";
        try {
            String pemContent = Files.readString(Paths.get(filename));
            String encodedKey = pemContent
                .replace(PUBLIC_KEY_HEADER, "")
                .replace(PUBLIC_KEY_FOOTER, "")
                .replaceAll("\\s", "");
            
            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            logger.info("Chiave pubblica caricata: " + filename);
            return publicKey;
        } catch (Exception e) {
            logger.severe("Errore nel caricamento della chiave pubblica: " + e.getMessage());
            throw new RuntimeException("Impossibile caricare chiave pubblica", e);
        }
    }
    
    /**
     * Carica una chiave privata dal file PEM
     */
    public PrivateKey loadPrivateKey(String keyName) {
        String filename = baseDir + File.separator + keyName + "_private.pem";
        try {
            String pemContent = Files.readString(Paths.get(filename));
            String encodedKey = pemContent
                .replace(PRIVATE_KEY_HEADER, "")
                .replace(PRIVATE_KEY_FOOTER, "")
                .replaceAll("\\s", "");
            
            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            logger.info("Chiave privata caricata: " + filename);
            return privateKey;
        } catch (Exception e) {
            logger.severe("Errore nel caricamento della chiave privata: " + e.getMessage());
            throw new RuntimeException("Impossibile caricare chiave privata", e);
        }
    }
    
    /**
     * Carica una coppia di chiavi esistente o ne genera una nuova se non esiste
     */
    public KeyPair loadOrGenerateKeyPair(String keyName) {
        if (keyPairExists(keyName)) {
            return new KeyPair(loadPublicKey(keyName), loadPrivateKey(keyName));
        } else {
            logger.info("Coppia di chiavi non trovata, genero nuova coppia: " + keyName);
            KeyPair keyPair = generateKeyPair();
            saveKeyPair(keyName, keyPair);
            return keyPair;
        }
    }
    
    /**
     * Verifica se esiste una coppia di chiavi con il nome specificato
     */
    public boolean keyPairExists(String keyName) {
        String publicKeyPath = baseDir + File.separator + keyName + "_public.pem";
        String privateKeyPath = baseDir + File.separator + keyName + "_private.pem";
        return Files.exists(Paths.get(publicKeyPath)) && Files.exists(Paths.get(privateKeyPath));
    }
    
    /**
     * Verifica se esiste la chiave pubblica con il nome specificato
     */
    public boolean publicKeyExists(String keyName) {
        String publicKeyPath = baseDir + File.separator + keyName + "_public.pem";
        return Files.exists(Paths.get(publicKeyPath));
    }
    
    /**
     * Elimina una coppia di chiavi
     */
    public void deleteKeyPair(String keyName) {
        try {
            String publicKeyPath = baseDir + File.separator + keyName + "_public.pem";
            String privateKeyPath = baseDir + File.separator + keyName + "_private.pem";
            
            Files.deleteIfExists(Paths.get(publicKeyPath));
            Files.deleteIfExists(Paths.get(privateKeyPath));
            logger.info("Coppia di chiavi eliminata: " + keyName);
        } catch (IOException e) {
            logger.severe("Errore nell'eliminazione delle chiavi: " + e.getMessage());
            throw new RuntimeException("Impossibile eliminare chiavi", e);
        }
    }
    
    /**
     * Formatta una stringa Base64 in linee da 64 caratteri (standard PEM)
     */
    private String formatBase64(String base64String) {
        StringBuilder formatted = new StringBuilder();
        int lineLength = 64;
        for (int i = 0; i < base64String.length(); i += lineLength) {
            int end = Math.min(i + lineLength, base64String.length());
            formatted.append(base64String.substring(i, end));
            if (end < base64String.length()) {
                formatted.append("\n");
            }
        }
        return formatted.toString();
    }
}