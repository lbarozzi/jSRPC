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
 * Classe che gestisce la comunicazione con un singolo client TCP in un thread separato.
 * Ogni istanza di ClientHandler gestisce una connessione client specifica.
 */
public class ClientHandler implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    
    private final Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private boolean connected;
    
    /**
     * Costruttore per ClientHandler.
     * 
     * @param clientSocket Il socket della connessione client da gestire
     */
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.connected = false;
    }
    
    /**
     * Metodo principale del thread che gestisce la comunicazione con il client.
     */
    @Override
    public void run() {
        try {
            setupStreams();
            connected = true;
            
            String clientAddress = clientSocket.getInetAddress().toString();
            LOGGER.info("Iniziata gestione client: " + clientAddress);
            
            // Invia messaggio di benvenuto
            sendMessage("Benvenuto! Connessione stabilita con il server.");
            
            // Loop principale per gestire i messaggi del client
            String inputMessage;
            while (connected && (inputMessage = receiveMessage()) != null) {
                LOGGER.info("Messaggio ricevuto da " + clientAddress + ": " + inputMessage);
                
                // Processa il messaggio e invia risposta
                String response = processMessage(inputMessage);
                sendMessage(response);
                
                // Se il client invia "quit", termina la connessione
                if ("quit".equalsIgnoreCase(inputMessage.trim())) {
                    break;
                }
            }
            
        } catch (SocketException e) {
            LOGGER.info("Client disconnesso: " + clientSocket.getInetAddress());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore nella comunicazione con il client", e);
        } finally {
            disconnect();
        }
    }
    
    /**
     * Configura gli stream di input e output per la comunicazione.
     * 
     * @throws IOException Se si verifica un errore nella configurazione degli stream
     */
    protected void setupStreams() throws IOException {
        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        output = new PrintWriter(clientSocket.getOutputStream(), true);
    }
    
    /**
     * Riceve un messaggio dal client.
     * 
     * @return Il messaggio ricevuto, o null se la connessione è chiusa
     * @throws IOException Se si verifica un errore durante la lettura
     */
    protected String receiveMessage() throws IOException {
        return input.readLine();
    }
    
    /**
     * Invia un messaggio al client.
     * 
     * @param message Il messaggio da inviare
     */
    protected void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }
    
    /**
     * Processa un messaggio ricevuto dal client e genera una risposta.
     * Questa è una implementazione base che può essere estesa per funzionalità più complesse.
     * 
     * @param message Il messaggio ricevuto dal client
     * @return La risposta da inviare al client
     */
    protected String processMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Messaggio vuoto ricevuto.";
        }
        
        String trimmedMessage = message.trim().toLowerCase();
        
        switch (trimmedMessage) {
            case "hello":
                return "Hello! Come posso aiutarti?";
            case "time":
                return "Ora corrente: " + java.time.LocalDateTime.now().toString();
            case "ping":
                return "pong";
            case "quit":
                return "Arrivederci! Chiusura connessione.";
            case "help":
                return "Comandi disponibili: hello, time, ping, help, quit";
            default:
                return "Echo: " + message;
        }
    }
    
    /**
     * Disconnette il client e chiude tutte le risorse.
     */
    public void disconnect() {
        connected = false;
        
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Errore durante la chiusura dell'input stream", e);
        }
        
        if (output != null) {
            output.close();
        }
        
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Errore durante la chiusura del socket client", e);
        }
        
        LOGGER.info("Client disconnesso e risorse rilasciate");
    }
    
    /**
     * Verifica se il client è connesso.
     * 
     * @return true se il client è connesso, false altrimenti
     */
    public boolean isConnected() {
        return connected && clientSocket != null && !clientSocket.isClosed();
    }
    
    /**
     * Restituisce l'indirizzo IP del client.
     * 
     * @return L'indirizzo IP del client
     */
    public String getClientAddress() {
        return clientSocket != null ? clientSocket.getInetAddress().toString() : "Unknown";
    }
    
    /**
     * Restituisce il BufferedReader per la lettura dei messaggi.
     * Metodo protetto per l'estensione in sottoclassi.
     * 
     * @return BufferedReader per input
     */
    protected BufferedReader getBufferedReader() {
        return input;
    }
    
    /**
     * Restituisce il PrintWriter per l'invio dei messaggi.
     * Metodo protetto per l'estensione in sottoclassi.
     * 
     * @return PrintWriter per output
     */
    protected PrintWriter getPrintWriter() {
        return output;
    }
    
    /**
     * Restituisce il socket del client.
     * Metodo protetto per l'estensione in sottoclassi.
     * 
     * @return Socket del client
     */
    protected Socket getSocket() {
        return clientSocket;
    }
}