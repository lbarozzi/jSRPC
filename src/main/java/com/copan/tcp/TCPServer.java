package com.copan.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Server TCP multi-threaded che può gestire multiple connessioni client simultanee.
 * Utilizza un thread pool per gestire le connessioni client in modo efficiente.
 */
public class TCPServer {
    private static final Logger LOGGER = Logger.getLogger(TCPServer.class.getName());
    
    private final int port;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean running;
    private final int maxThreads;
    
    /**
     * Costruttore per il server TCP.
     * 
     * @param port La porta su cui il server ascolterà
     * @param maxThreads Il numero massimo di thread nel pool
     */
    public TCPServer(int port, int maxThreads) {
        this.port = port;
        this.maxThreads = maxThreads;
        this.running = false;
    }
    
    /**
     * Costruttore con numero di thread predefinito.
     * 
     * @param port La porta su cui il server ascolterà
     */
    public TCPServer(int port) {
        this(port, 10); // Default: 10 thread massimi
    }
    
    /**
     * Avvia il server TCP.
     * Il server inizierà ad ascoltare sulla porta specificata e accetterà connessioni client.
     * 
     * @throws IOException Se si verifica un errore durante l'avvio del server
     */
    public void start() throws IOException {
        if (running) {
            throw new IllegalStateException("Il server è già in esecuzione");
        }
        
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newFixedThreadPool(maxThreads);
        running = true;
        
        LOGGER.info(String.format("Server TCP avviato sulla porta %d con %d thread massimi", port, maxThreads));
        
        // Thread principale per accettare connessioni
        Thread acceptThread = new Thread(this::acceptConnections);
        acceptThread.setDaemon(false);
        acceptThread.start();
    }
    
    /**
     * Loop principale per accettare le connessioni client.
     */
    private void acceptConnections() {
        while (running && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                // Controlla se il threadPool è ancora disponibile prima di sottomettere il task
                if (running && threadPool != null && !threadPool.isShutdown()) {
                    LOGGER.info("Nuova connessione client accettata da: " + clientSocket.getInetAddress());
                    
                    // Crea un nuovo ClientHandler per gestire la connessione
                    ClientHandler handler = new ClientHandler(clientSocket);
                    threadPool.submit(handler);
                } else {
                    // Il server sta per chiudere, chiudi la connessione
                    clientSocket.close();
                }
                
            } catch (IOException e) {
                if (running) {
                    LOGGER.log(Level.SEVERE, "Errore durante l'accettazione di una connessione client", e);
                }
            }
        }
    }
    
    /**
     * Ferma il server TCP.
     * Chiude il server socket e termina tutti i thread del pool.
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Errore durante la chiusura del server socket", e);
        }
        
        if (threadPool != null) {
            threadPool.shutdown();
        }
        
        LOGGER.info("Server TCP fermato");
    }
    
    /**
     * Verifica se il server è in esecuzione.
     * 
     * @return true se il server è in esecuzione, false altrimenti
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Restituisce la porta su cui il server sta ascoltando.
     * 
     * @return La porta del server
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Restituisce il numero massimo di thread nel pool.
     * 
     * @return Il numero massimo di thread
     */
    public int getMaxThreads() {
        return maxThreads;
    }
}