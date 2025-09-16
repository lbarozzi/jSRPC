package com.copan.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Class that handles communication with a single TCP client in a separate thread.
 * Each ClientHandler instance manages a specific client connection.
 */
public class ClientHandler implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    
    private final Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private boolean connected;
    
    /**
     * Constructor for ClientHandler.
     * 
     * @param clientSocket The client connection socket to manage
     */
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.connected = false;
    }
    
    /**
     * Main thread method that handles communication with the client.
     */
    @Override
    public void run() {
        try {
            setupStreams();
            connected = true;
            
            String clientAddress = clientSocket.getInetAddress().toString();
            LOGGER.info("Started handling client: " + clientAddress);
            
            // Send welcome message
            sendMessage("Welcome! Connection established with the server.");
            
            // Main loop to handle client messages
            String inputMessage;
            while (connected && (inputMessage = receiveMessage()) != null) {
                LOGGER.info("Message received from " + clientAddress + ": " + inputMessage);
                
                // Process the message and send response
                String response = processMessage(inputMessage);
                sendMessage(response);
                
                // If the client sends "quit", terminate the connection
                if ("quit".equalsIgnoreCase(inputMessage.trim())) {
                    break;
                }
            }
            
        } catch (SocketException e) {
            LOGGER.info("Client disconnected: " + clientSocket.getInetAddress());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error in communication with client", e);
        } finally {
            disconnect();
        }
    }
    
    /**
     * Configures input and output streams for communication.
     * 
     * @throws IOException If an error occurs during stream configuration
     */
    protected void setupStreams() throws IOException {
        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        output = new PrintWriter(clientSocket.getOutputStream(), true);
    }
    
    /**
     * Receives a message from the client.
     * 
     * @return The received message, or null if the connection is closed
     * @throws IOException If an error occurs during reading
     */
    protected String receiveMessage() throws IOException {
        return input.readLine();
    }
    
    /**
     * Sends a message to the client.
     * 
     * @param message The message to send
     */
    protected void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }
    
    /**
     * Processes a message received from the client and generates a response.
     * This is a base implementation that can be extended for more complex functionality.
     * 
     * @param message The message received from the client
     * @return The response to send to the client
     */
    protected String processMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Empty message received.";
        }
        
        String trimmedMessage = message.trim().toLowerCase();
        
        switch (trimmedMessage) {
            case "hello":
                return "Hello! How can I help you?";
            case "time":
                return "Current time: " + java.time.LocalDateTime.now().toString();
            case "ping":
                return "pong";
            case "quit":
                return "Goodbye! Closing connection.";
            case "help":
                return "Available commands: hello, time, ping, help, quit";
            default:
                return "Echo: " + message;
        }
    }
    
    /**
     * Disconnects the client and closes all resources.
     */
    public void disconnect() {
        connected = false;
        
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing input stream", e);
        }
        
        if (output != null) {
            output.close();
        }
        
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing client socket", e);
        }
        
        LOGGER.info("Client disconnected and resources released");
    }
    
    /**
     * Checks if the client is connected.
     * 
     * @return true if the client is connected, false otherwise
     */
    public boolean isConnected() {
        return connected && clientSocket != null && !clientSocket.isClosed();
    }
    
    /**
     * Returns the client's IP address.
     * 
     * @return The client's IP address
     */
    public String getClientAddress() {
        return clientSocket != null ? clientSocket.getInetAddress().toString() : "Unknown";
    }
    
    /**
     * Returns the BufferedReader for reading messages.
     * Protected method for extension in subclasses.
     * 
     * @return BufferedReader for input
     */
    protected BufferedReader getBufferedReader() {
        return input;
    }
    
    /**
     * Returns the PrintWriter for sending messages.
     * Protected method for extension in subclasses.
     * 
     * @return PrintWriter for output
     */
    protected PrintWriter getPrintWriter() {
        return output;
    }
    
    /**
     * Returns the client socket.
     * Protected method for extension in subclasses.
     * 
     * @return Client socket
     */
    protected Socket getSocket() {
        return clientSocket;
    }
}