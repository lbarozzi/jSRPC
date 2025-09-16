package com.copan.crypto;

import com.copan.tcp.TCPServer;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * Server TCP crittografico che estende TCPServer per supportare comunicazioni sicure.
 * Utilizza chiavi RSA per cifratura e firme digitali per autenticazione e integrità.
 */
public class CriptoServer extends TCPServer {
    private static final Logger logger = Logger.getLogger(CriptoServer.class.getName());
    
    private final KeyManager keyManager;
    private final String serverKeyName;
    
    /**
     * Costruttore per CriptoServer
     * 
     * @param port Porta su cui avviare il server
     * @param maxThreads Numero massimo di thread per gestire i client
     * @param keyManager Gestore delle chiavi crittografiche
     * @param serverKeyName Nome identificativo per le chiavi del server
     */
    public CriptoServer(int port, int maxThreads, KeyManager keyManager, String serverKeyName) {
        super(port, maxThreads);
        this.keyManager = keyManager;
        this.serverKeyName = serverKeyName;
        
        // Genera o carica le chiavi del server all'avvio
        keyManager.loadOrGenerateKeyPair(serverKeyName);
        logger.info("CriptoServer inizializzato sulla porta " + port + 
                   " con chiavi: " + serverKeyName);
    }
    
    /**
     * Costruttore semplificato con valori di default
     */
    public CriptoServer(int port, KeyManager keyManager, String serverKeyName) {
        this(port, 10, keyManager, serverKeyName);
    }
    
    /**
     * Costruttore con nome chiave di default
     */
    public CriptoServer(int port, int maxThreads, KeyManager keyManager) {
        this(port, maxThreads, keyManager, "server");
    }
    
    /**
     * Override del metodo acceptConnections per gestire client crittografici
     */
    @Override
    protected void acceptConnections() {
        logger.info("Server crittografico in ascolto per nuove connessioni...");
        
        while (isRunning()) {
            try {
                Socket clientSocket = getServerSocket().accept();
                
                if (!isRunning()) {
                    clientSocket.close();
                    break;
                }
                
                logger.info("Nuova connessione client crittografica accettata da: " + 
                           clientSocket.getRemoteSocketAddress());
                
                // Crea un CriptoClientHandler invece di ClientHandler normale
                CriptoClientHandler clientHandler = new CriptoClientHandler(
                    clientSocket, keyManager, serverKeyName
                );
                
                // Sottometti il task al thread pool
                ExecutorService executor = getExecutorService();
                if (executor != null && !executor.isShutdown()) {
                    executor.submit(clientHandler);
                } else {
                    logger.warning("ExecutorService non disponibile, chiudo connessione client");
                    clientSocket.close();
                }
                
            } catch (IOException e) {
                if (isRunning()) {
                    logger.severe("Errore nell'accettazione connessione client: " + e.getMessage());
                }
                break;
            } catch (Exception e) {
                logger.severe("Errore imprevisto nella gestione client: " + e.getMessage());
                break;
            }
        }
        
        logger.info("Server crittografico ha smesso di accettare connessioni");
    }
    
    @Override
    public void start() throws IOException {
        logger.info("Avvio CriptoServer sulla porta " + getPort() + 
                   " con protezione crittografica RSA");
        super.start();
    }
    
    @Override
    public void stop() {
        logger.info("Arresto CriptoServer...");
        super.stop();
        logger.info("CriptoServer arrestato");
    }
    
    /**
     * Restituisce il gestore delle chiavi utilizzato dal server
     */
    public KeyManager getKeyManager() {
        return keyManager;
    }
    
    /**
     * Restituisce il nome delle chiavi del server
     */
    public String getServerKeyName() {
        return serverKeyName;
    }
    
    /**
     * Verifica se il server ha le chiavi configurate correttamente
     */
    public boolean hasValidKeys() {
        return keyManager.keyPairExists(serverKeyName);
    }
}