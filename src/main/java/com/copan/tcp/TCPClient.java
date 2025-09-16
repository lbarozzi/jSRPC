package com.copan.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ConnectException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * TCP client that can connect to a server, send messages and receive responses.
 * Supports synchronous and asynchronous operations for communication.
 */
public class TCPClient {
    private static final Logger LOGGER = Logger.getLogger(TCPClient.class.getName());
    
    private final String serverHost;
    private final int serverPort;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private boolean connected;
    
    /**
     * Constructor for the TCP client.
     * 
     * @param serverHost The address of the server to connect to
     * @param serverPort The server port
     */
    public TCPClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.connected = false;
    }
    
    /**
     * Establishes connection with the server.
     * 
     * @throws IOException If the connection cannot be established
     */
    public void connect() throws IOException {
        if (connected) {
            throw new IllegalStateException("Client already connected to server");
        }
        
        try {
            socket = new Socket(serverHost, serverPort);
            setupStreams();
            connected = true;
            
            LOGGER.info(String.format("Connected to server %s:%d", serverHost, serverPort));
            
            // Read welcome message if present
            String welcomeMessage = receiveMessage();
            if (welcomeMessage != null) {
                LOGGER.info("Welcome message: " + welcomeMessage);
            }
            
        } catch (ConnectException e) {
            throw new IOException("Unable to connect to server " + serverHost + ":" + serverPort, e);
        }
    }
    
    /**
     * Configures input and output streams for communication.
     * 
     * @throws IOException If an error occurs in stream configuration
     */
    protected void setupStreams() throws IOException {
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
    }
    
    /**
     * Sends a message to the server.
     * 
     * @param message The message to send
     * @throws IOException If the client is not connected or an error occurs
     */
    public void sendMessage(String message) throws IOException {
        if (!connected) {
            throw new IOException("Client not connected to server");
        }
        
        if (output != null) {
            output.println(message);
            LOGGER.fine("Message sent: " + message);
        } else {
            throw new IOException("Output stream not available");
        }
    }
    
    /**
     * Receives a message from the server.
     * 
     * @return The message received from the server, or null if the connection is closed
     * @throws IOException If an error occurs during reading
     */
    public String receiveMessage() throws IOException {
        if (!connected) {
            throw new IOException("Client not connected to server");
        }
        
        if (input != null) {
            String message = input.readLine();
            if (message != null) {
                LOGGER.fine("Message received: " + message);
            }
            return message;
        } else {
            throw new IOException("Input stream not available");
        }
    }
    
    /**
     * Sends a message to the server and waits for a response.
     * 
     * @param message The message to send
     * @return The server response
     * @throws IOException If an error occurs during communication
     */
    public String sendAndReceive(String message) throws IOException {
        sendMessage(message);
        return receiveMessage();
    }
    
    /**
     * Disconnects the client from the server and releases all resources.
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        
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
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing socket", e);
        }
        
        LOGGER.info("Disconnected from server");
    }
    
    /**
     * Checks if the client is connected to the server.
     * 
     * @return true if the client is connected, false otherwise
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
    
    /**
     * Returns the server address.
     * 
     * @return The server address
     */
    public String getServerHost() {
        return serverHost;
    }
    
    /**
     * Returns the server port.
     * 
     * @return The server port
     */
    public int getServerPort() {
        return serverPort;
    }
    
    /**
     * Utility method to send a "ping" message to the server.
     * 
     * @return The server response to the ping
     * @throws IOException If an error occurs during communication
     */
    public String ping() throws IOException {
        return sendAndReceive("ping");
    }
    
    /**
     * Utility method to request the current time from the server.
     * 
     * @return The current time from the server
     * @throws IOException If an error occurs during communication
     */
    public String getTime() throws IOException {
        return sendAndReceive("time");
    }
    
    /**
     * Utility method to request the list of available commands.
     * 
     * @return The list of available commands
     * @throws IOException If an error occurs during communication
     */
    public String getHelp() throws IOException {
        return sendAndReceive("help");
    }
    
    /**
     * Returns the socket for derived classes.
     * 
     * @return The connection socket
     */
    protected Socket getSocket() {
        return socket;
    }
    
    /**
     * Returns the BufferedReader for derived classes.
     * 
     * @return The BufferedReader for reading
     */
    protected BufferedReader getInput() {
        return input;
    }
    
    /**
     * Returns the PrintWriter for derived classes.
     * 
     * @return The PrintWriter for writing
     */
    protected PrintWriter getOutput() {
        return output;
    }
    
    /**
     * Sets the connection state for derived classes.
     * 
     * @param connected The connection state
     */
    protected void setConnected(boolean connected) {
        this.connected = connected;
    }
}