package com.copan.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Multi-threaded TCP server that can handle multiple simultaneous client connections.
 * Uses a thread pool to efficiently manage client connections.
 */
public class TCPServer {
    private static final Logger LOGGER = Logger.getLogger(TCPServer.class.getName());
    
    private final int port;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean running;
    private final int maxThreads;
    
    /**
     * Constructor for the TCP server.
     * 
     * @param port The port on which the server will listen
     * @param maxThreads The maximum number of threads in the pool
     */
    public TCPServer(int port, int maxThreads) {
        this.port = port;
        this.maxThreads = maxThreads;
        this.running = false;
    }
    
    /**
     * Constructor with default number of threads.
     * 
     * @param port The port on which the server will listen
     */
    public TCPServer(int port) {
        this(port, 10); // Default: 10 thread massimi
    }
    
    /**
     * Starts the TCP server.
     * The server will start listening on the specified port and accept client connections.
     * 
     * @throws IOException If an error occurs while starting the server
     */
    public void start() throws IOException {
        if (running) {
            throw new IllegalStateException("The server is already running");
        }
        
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newFixedThreadPool(maxThreads);
        running = true;
        
        LOGGER.info(String.format("TCP server started on port %d with %d max threads", port, maxThreads));
        
        // Main thread to accept connections
        Thread acceptThread = new Thread(this::acceptConnections);
        acceptThread.setDaemon(false);
        acceptThread.start();
    }
    
    /**
     * Main loop to accept client connections.
     */
    protected void acceptConnections() {
        while (running && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                // Check if the threadPool is still available before submitting the task
                if (running && threadPool != null && !threadPool.isShutdown()) {
                    LOGGER.info("New client connection accepted from: " + clientSocket.getInetAddress());
                    
                    // Create a new ClientHandler to manage the connection
                    ClientHandler handler = new ClientHandler(clientSocket);
                    threadPool.submit(handler);
                } else {
                    // The server is about to close, close the connection
                    clientSocket.close();
                }
                
            } catch (IOException e) {
                if (running) {
                    LOGGER.log(Level.SEVERE, "Error accepting client connection", e);
                }
            }
        }
    }
    
    /**
     * Stops the TCP server.
     * Closes the server socket and terminates all threads in the pool.
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
            LOGGER.log(Level.WARNING, "Error closing server socket", e);
        }
        
        if (threadPool != null) {
            threadPool.shutdown();
        }
        
        LOGGER.info("TCP server stopped");
    }
    
    /**
     * Checks if the server is running.
     * 
     * @return true if the server is running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Returns the port on which the server is listening.
     * 
     * @return The server port
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Returns the maximum number of threads in the pool.
     * 
     * @return The maximum number of threads
     */
    public int getMaxThreads() {
        return maxThreads;
    }
    
    /**
     * Returns the server socket for derived classes.
     * 
     * @return The server socket
     */
    protected ServerSocket getServerSocket() {
        return serverSocket;
    }
    
    /**
     * Returns the executor service for derived classes.
     * 
     * @return The executor service
     */
    protected ExecutorService getExecutorService() {
        return threadPool;
    }
}