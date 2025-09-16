package com.copan.crypto;

import com.copan.tcp.TCPServer;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * Cryptographic TCP server that extends TCPServer to support secure communications.
 * Uses RSA keys for encryption and digital signatures for authentication and integrity.
 */
public class CriptoServer extends TCPServer {
    private static final Logger logger = Logger.getLogger(CriptoServer.class.getName());
    
    private final KeyManager keyManager;
    private final String serverKeyName;
    
    /**
     * Constructor for CriptoServer
     * 
     * @param port Port on which to start the server
     * @param maxThreads Maximum number of threads to handle clients
     * @param keyManager Cryptographic key manager
     * @param serverKeyName Identifying name for server keys
     */
    public CriptoServer(int port, int maxThreads, KeyManager keyManager, String serverKeyName) {
        super(port, maxThreads);
        this.keyManager = keyManager;
        this.serverKeyName = serverKeyName;
        
        // Generate or load server keys at startup
        keyManager.loadOrGenerateKeyPair(serverKeyName);
        logger.info("CriptoServer initialized on port " + port + 
                   " with keys: " + serverKeyName);
    }
    
    /**
     * Simplified constructor with default values
     */
    public CriptoServer(int port, KeyManager keyManager, String serverKeyName) {
        this(port, 10, keyManager, serverKeyName);
    }
    
    /**
     * Constructor with default key name
     */
    public CriptoServer(int port, int maxThreads, KeyManager keyManager) {
        this(port, maxThreads, keyManager, "server");
    }
    
    /**
     * Override of acceptConnections method to handle cryptographic clients
     */
    @Override
    protected void acceptConnections() {
        logger.info("Cryptographic server listening for new connections...");
        
        while (isRunning()) {
            try {
                Socket clientSocket = getServerSocket().accept();
                
                if (!isRunning()) {
                    clientSocket.close();
                    break;
                }
                
                logger.info("New cryptographic client connection accepted from: " + 
                           clientSocket.getRemoteSocketAddress());
                
                // Create a CriptoClientHandler instead of normal ClientHandler
                CriptoClientHandler clientHandler = new CriptoClientHandler(
                    clientSocket, keyManager, serverKeyName
                );
                
                // Submit the task to the thread pool
                ExecutorService executor = getExecutorService();
                if (executor != null && !executor.isShutdown()) {
                    try {
                        executor.submit(clientHandler);
                    } catch (java.util.concurrent.RejectedExecutionException e) {
                        // Thread pool was shutdown between the check and submit
                        logger.warning("Thread pool was shutdown, closing client connection");
                        try {
                            clientSocket.close();
                        } catch (IOException closeEx) {
                            logger.warning("Error closing client socket after rejected execution: " + closeEx.getMessage());
                        }
                    }
                } else {
                    logger.warning("ExecutorService not available, closing client connection");
                    clientSocket.close();
                }
                
            } catch (IOException e) {
                if (isRunning()) {
                    logger.severe("Error accepting client connection: " + e.getMessage());
                }
                break;
            } catch (Exception e) {
                logger.severe("Unexpected error in client handling: " + e.getMessage());
                break;
            }
        }
        
        logger.info("Cryptographic server stopped accepting connections");
    }
    
    @Override
    public void start() throws IOException {
        logger.info("Starting CriptoServer on port " + getPort() + 
                   " with RSA cryptographic protection");
        super.start();
    }
    
    @Override
    public void stop() {
        logger.info("Stopping CriptoServer...");
        super.stop();
        logger.info("CriptoServer stopped");
    }
    
    /**
     * Returns the key manager used by the server
     */
    public KeyManager getKeyManager() {
        return keyManager;
    }
    
    /**
     * Returns the server's key name
     */
    public String getServerKeyName() {
        return serverKeyName;
    }
    
    /**
     * Checks if the server has correctly configured keys
     */
    public boolean hasValidKeys() {
        return keyManager.keyPairExists(serverKeyName);
    }
}