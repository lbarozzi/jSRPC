package com.copan.crypto;

import com.copan.tcp.TCPClient;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Cryptographic TCP client that extends TCPClient to support secure communications.
 * Uses RSA keys for encryption and digital signatures for authentication and integrity.
 */
public class CryptoClient extends TCPClient {
    private static final Logger logger = Logger.getLogger(CryptoClient.class.getName());
    
    private final KeyManager keyManager;
    private final String clientKeyName;
    private PublicKey serverPublicKey;
    private boolean cryptoHandshakeComplete;
    
    /**
     * Constructor for CriptoClient
     * 
     * @param serverHost Server address
     * @param serverPort Server port
     * @param keyManager Cryptographic key manager
     * @param clientKeyName Identifying name for client keys
     */
    public CryptoClient(String serverHost, int serverPort, KeyManager keyManager, String clientKeyName) {
        super(serverHost, serverPort);
        this.keyManager = keyManager;
        this.clientKeyName = clientKeyName;
        this.cryptoHandshakeComplete = false;
        
        // Generate or load client keys
        keyManager.loadOrGenerateKeyPair(clientKeyName);
        logger.info("CriptoClient initialized for " + serverHost + ":" + serverPort + 
                   " with keys: " + clientKeyName);
    }
    
    /**
     * Constructor with default key name
     */
    public CryptoClient(String serverHost, int serverPort, KeyManager keyManager) {
        this(serverHost, serverPort, keyManager, "client");
    }
    
    /**
     * Override of connection to include cryptographic handshake
     */
    @Override
    public void connect() throws IOException {
        // Connessione TCP base
        super.connect();
        
        try {
            // Esegui handshake crittografico
            performCryptoHandshake();
            logger.info("Handshake crittografico completato con successo");
            
        } catch (Exception e) {
            logger.severe("Errore durante l'handshake crittografico: " + e.getMessage());
            disconnect();
            throw new IOException("Handshake crittografico fallito", e);
        }
    }
    
    /**
     * Esegue l'handshake crittografico con il server
     */
    private void performCryptoHandshake() throws Exception {
        // Passo 1: Invia la chiave pubblica del client
        PublicKey clientPublicKey = keyManager.loadPublicKey(clientKeyName);
        String clientPublicKeyB64 = Base64.getEncoder().encodeToString(clientPublicKey.getEncoded());
        super.sendMessage("CRYPTO_HANDSHAKE_START:" + clientPublicKeyB64);
        
        // Passo 2: Ricevi la chiave pubblica del server
        String response = super.receiveMessage();
        if (response == null || !response.startsWith("SERVER_PUBLIC_KEY:")) {
            throw new IOException("Risposta handshake non valida: " + response);
        }
        
        String serverPublicKeyB64 = response.substring("SERVER_PUBLIC_KEY:".length());
        byte[] serverPublicKeyBytes = Base64.getDecoder().decode(serverPublicKeyB64);
        serverPublicKey = CryptoUtils.decodePublicKey(serverPublicKeyBytes);
        
        // Passo 3: Verifica che il server accetti la nostra chiave
        String confirmResponse = super.receiveMessage();
        if (!"HANDSHAKE_COMPLETE".equals(confirmResponse)) {
            throw new IOException("Handshake non completato dal server: " + confirmResponse);
        }
        
        cryptoHandshakeComplete = true;
        logger.info("Handshake crittografico completato, chiave server ricevuta");
    }
    
    /**
     * Invia un messaggio crittografato al server
     */
    @Override
    public void sendMessage(String message) throws IOException {
        if (!cryptoHandshakeComplete) {
            super.sendMessage(message);
            return;
        }
        
        try {
            // Crea messaggio sicuro
            SecureMessage secureMessage = CryptoUtils.createSecureMessage(
                message,
                keyManager.loadPrivateKey(clientKeyName),
                serverPublicKey
            );
            
            // Invia come JSON
            String jsonMessage = secureMessage.toJson();
            super.sendMessage("SECURE_MESSAGE:" + jsonMessage);
            
            logger.fine("Messaggio crittografato inviato: " + message);
            
        } catch (Exception e) {
            logger.severe("Errore nella cifratura del messaggio: " + e.getMessage());
            throw new IOException("Impossibile cifrare il messaggio", e);
        }
    }
    
    /**
     * Riceve un messaggio dal server (può essere crittografato o normale)
     */
    @Override
    public String receiveMessage() throws IOException {
        String rawMessage = super.receiveMessage();
        
        if (rawMessage == null || !cryptoHandshakeComplete) {
            return rawMessage;
        }
        
        // Se è un messaggio sicuro, decrittalo
        if (rawMessage.startsWith("SECURE_MESSAGE:")) {
            try {
                String jsonMessage = rawMessage.substring("SECURE_MESSAGE:".length());
                SecureMessage secureMessage = SecureMessage.fromJson(jsonMessage);
                
                // Verifica e decritta
                String decryptedMessage = CryptoUtils.verifyAndDecrypt(
                    secureMessage,
                    keyManager.loadPrivateKey(clientKeyName),
                    serverPublicKey
                );
                
                logger.fine("Messaggio crittografato ricevuto e decrittato: " + decryptedMessage);
                return decryptedMessage;
                
            } catch (Exception e) {
                logger.severe("Errore nella decrittazione del messaggio: " + e.getMessage());
                throw new IOException("Impossibile decrittare il messaggio", e);
            }
        }
        
        // Messaggio normale
        return rawMessage;
    }
    
    /**
     * Verifica se l'handshake crittografico è stato completato
     */
    public boolean isCryptoHandshakeComplete() {
        return cryptoHandshakeComplete;
    }
    
    /**
     * Restituisce la chiave pubblica del server (dopo l'handshake)
     */
    public PublicKey getServerPublicKey() {
        return serverPublicKey;
    }
    
    /**
     * Restituisce il gestore delle chiavi
     */
    public KeyManager getKeyManager() {
        return keyManager;
    }
    
    /**
     * Restituisce il nome delle chiavi del client
     */
    public String getClientKeyName() {
        return clientKeyName;
    }
    
    /**
     * Verifica se il client ha le chiavi configurate correttamente
     */
    public boolean hasValidKeys() {
        return keyManager.keyPairExists(clientKeyName);
    }
    
    @Override
    public void disconnect() {
        cryptoHandshakeComplete = false;
        serverPublicKey = null;
        logger.info("Disconnessione da connessione crittografica");
        super.disconnect();
    }
}