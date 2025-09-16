package com.copan.crypto;

import com.copan.tcp.ClientHandler;
import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Handler per client crittografici che estende ClientHandler per supportare comunicazioni sicure.
 * Gestisce l'handshake crittografico e i messaggi cifrati con RSA.
 */
public class CryptoClientHandler extends ClientHandler {
    private static final Logger logger = Logger.getLogger(CryptoClientHandler.class.getName());
    
    private final KeyManager keyManager;
    private final String serverKeyName;
    private PublicKey clientPublicKey;
    private boolean cryptoHandshakeComplete;
    
    /**
     * Costruttore per CriptoClientHandler
     * 
     * @param clientSocket Socket della connessione client
     * @param keyManager Gestore delle chiavi crittografiche
     * @param serverKeyName Nome identificativo per le chiavi del server
     */
    public CryptoClientHandler(Socket clientSocket, KeyManager keyManager, String serverKeyName) {
        super(clientSocket);
        this.keyManager = keyManager;
        this.serverKeyName = serverKeyName;
        this.cryptoHandshakeComplete = false;
        
        logger.info("CriptoClientHandler creato per client: " + clientSocket.getRemoteSocketAddress());
    }
    
    /**
     * Override del metodo run per includere l'handshake crittografico
     */
    @Override
    public void run() {
        try {
            setupStreams();
            sendMessage("Benvenuto nel server crittografico! Iniziando handshake...");
            
            // Esegui handshake crittografico
            if (performCryptoHandshake()) {
                sendMessage("Handshake completato. Comunicazione sicura attiva.");
                
                // Gestisci messaggi sicuri
                handleSecureMessages();
            } else {
                sendMessage("Handshake fallito. Connessione terminata.");
            }
            
        } catch (IOException e) {
            logger.warning("Errore di I/O nel client handler crittografico: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Errore imprevisto nel client handler: " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    /**
     * Esegue l'handshake crittografico per stabilire comunicazione sicura
     */
    private boolean performCryptoHandshake() throws Exception {
        logger.info("Iniziando handshake crittografico con client");
        
        // Passo 1: Aspetta l'avvio dell'handshake dal client
        String handshakeStart = receiveMessage();
        if (handshakeStart == null || !handshakeStart.startsWith("CRYPTO_HANDSHAKE_START:")) {
            logger.warning("Handshake non inizializzato correttamente: " + handshakeStart);
            return false;
        }
        
        // Passo 2: Estrai e salva la chiave pubblica del client
        String clientPublicKeyB64 = handshakeStart.substring("CRYPTO_HANDSHAKE_START:".length());
        byte[] clientPublicKeyBytes = Base64.getDecoder().decode(clientPublicKeyB64);
        clientPublicKey = CryptoUtils.decodePublicKey(clientPublicKeyBytes);
        
        // Passo 3: Invia la nostra chiave pubblica al client
        PublicKey serverPublicKey = keyManager.loadPublicKey(serverKeyName);
        String serverPublicKeyB64 = Base64.getEncoder().encodeToString(serverPublicKey.getEncoded());
        sendMessage("SERVER_PUBLIC_KEY:" + serverPublicKeyB64);
        
        // Passo 4: Conferma completamento handshake
        sendMessage("HANDSHAKE_COMPLETE");
        
        cryptoHandshakeComplete = true;
        logger.info("Handshake crittografico completato con client: " + getSocket().getRemoteSocketAddress());
        return true;
    }
    
    /**
     * Gestisce i messaggi sicuri dopo l'handshake
     */
    private void handleSecureMessages() throws IOException {
        String message;
        while ((message = receiveMessage()) != null && !message.equals("quit")) {
            try {
                if (message.startsWith("SECURE_MESSAGE:")) {
                    // Processa messaggio crittografato
                    String decryptedMessage = processSecureMessage(message);
                    if (decryptedMessage != null) {
                        String response = processMessage(decryptedMessage);
                        sendSecureResponse(response);
                    }
                } else {
                    // Messaggio non sicuro dopo handshake - rifiuta
                    sendMessage("ERRORE: Tutti i messaggi devono essere cifrati dopo l'handshake");
                }
            } catch (Exception e) {
                logger.warning("Errore nel processamento messaggio sicuro: " + e.getMessage());
                sendMessage("ERRORE: Impossibile processare il messaggio sicuro");
            }
        }
    }
    
    /**
     * Processa un messaggio sicuro ricevuto dal client
     */
    private String processSecureMessage(String secureMessageStr) throws Exception {
        String jsonMessage = secureMessageStr.substring("SECURE_MESSAGE:".length());
        SecureMessage secureMessage = SecureMessage.fromJson(jsonMessage);
        
        // Verifica e decritta il messaggio
        String decryptedMessage = CryptoUtils.verifyAndDecrypt(
            secureMessage,
            keyManager.loadPrivateKey(serverKeyName),
            clientPublicKey
        );
        
        logger.fine("Messaggio sicuro ricevuto e decrittato: " + decryptedMessage);
        return decryptedMessage;
    }
    
    /**
     * Invia una risposta crittografata al client
     */
    private void sendSecureResponse(String response) throws Exception {
        // Crea messaggio sicuro
        SecureMessage secureMessage = CryptoUtils.createSecureMessage(
            response,
            keyManager.loadPrivateKey(serverKeyName),
            clientPublicKey
        );
        
        // Invia come JSON
        String jsonMessage = secureMessage.toJson();
        sendMessage("SECURE_MESSAGE:" + jsonMessage);
        
        logger.fine("Risposta sicura inviata: " + response);
    }
    
    /**
     * Verifica se l'handshake è stato completato
     */
    public boolean isCryptoHandshakeComplete() {
        return cryptoHandshakeComplete;
    }
    
    /**
     * Restituisce la chiave pubblica del client
     */
    public PublicKey getClientPublicKey() {
        return clientPublicKey;
    }
    
    /**
     * Restituisce il gestore delle chiavi
     */
    public KeyManager getKeyManager() {
        return keyManager;
    }
}