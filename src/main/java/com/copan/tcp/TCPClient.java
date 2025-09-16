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
 * Client TCP che può connettersi a un server, inviare messaggi e ricevere risposte.
 * Supporta operazioni sincrone e asincrone per la comunicazione.
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
     * Costruttore per il client TCP.
     * 
     * @param serverHost L'indirizzo del server a cui connettersi
     * @param serverPort La porta del server
     */
    public TCPClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.connected = false;
    }
    
    /**
     * Stabilisce la connessione con il server.
     * 
     * @throws IOException Se la connessione non può essere stabilita
     */
    public void connect() throws IOException {
        if (connected) {
            throw new IllegalStateException("Client già connesso al server");
        }
        
        try {
            socket = new Socket(serverHost, serverPort);
            setupStreams();
            connected = true;
            
            LOGGER.info(String.format("Connesso al server %s:%d", serverHost, serverPort));
            
            // Leggi il messaggio di benvenuto se presente
            String welcomeMessage = receiveMessage();
            if (welcomeMessage != null) {
                LOGGER.info("Messaggio di benvenuto: " + welcomeMessage);
            }
            
        } catch (ConnectException e) {
            throw new IOException("Impossibile connettersi al server " + serverHost + ":" + serverPort, e);
        }
    }
    
    /**
     * Configura gli stream di input e output per la comunicazione.
     * 
     * @throws IOException Se si verifica un errore nella configurazione degli stream
     */
    protected void setupStreams() throws IOException {
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
    }
    
    /**
     * Invia un messaggio al server.
     * 
     * @param message Il messaggio da inviare
     * @throws IOException Se il client non è connesso o si verifica un errore
     */
    public void sendMessage(String message) throws IOException {
        if (!connected) {
            throw new IOException("Client non connesso al server");
        }
        
        if (output != null) {
            output.println(message);
            LOGGER.fine("Messaggio inviato: " + message);
        } else {
            throw new IOException("Output stream non disponibile");
        }
    }
    
    /**
     * Riceve un messaggio dal server.
     * 
     * @return Il messaggio ricevuto dal server, o null se la connessione è chiusa
     * @throws IOException Se si verifica un errore durante la lettura
     */
    public String receiveMessage() throws IOException {
        if (!connected) {
            throw new IOException("Client non connesso al server");
        }
        
        if (input != null) {
            String message = input.readLine();
            if (message != null) {
                LOGGER.fine("Messaggio ricevuto: " + message);
            }
            return message;
        } else {
            throw new IOException("Input stream non disponibile");
        }
    }
    
    /**
     * Invia un messaggio al server e attende una risposta.
     * 
     * @param message Il messaggio da inviare
     * @return La risposta del server
     * @throws IOException Se si verifica un errore durante la comunicazione
     */
    public String sendAndReceive(String message) throws IOException {
        sendMessage(message);
        return receiveMessage();
    }
    
    /**
     * Disconnette il client dal server e rilascia tutte le risorse.
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
            LOGGER.log(Level.WARNING, "Errore durante la chiusura dell'input stream", e);
        }
        
        if (output != null) {
            output.close();
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Errore durante la chiusura del socket", e);
        }
        
        LOGGER.info("Disconnesso dal server");
    }
    
    /**
     * Verifica se il client è connesso al server.
     * 
     * @return true se il client è connesso, false altrimenti
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
    
    /**
     * Restituisce l'indirizzo del server.
     * 
     * @return L'indirizzo del server
     */
    public String getServerHost() {
        return serverHost;
    }
    
    /**
     * Restituisce la porta del server.
     * 
     * @return La porta del server
     */
    public int getServerPort() {
        return serverPort;
    }
    
    /**
     * Metodo di utilità per inviare un messaggio "ping" al server.
     * 
     * @return La risposta del server al ping
     * @throws IOException Se si verifica un errore durante la comunicazione
     */
    public String ping() throws IOException {
        return sendAndReceive("ping");
    }
    
    /**
     * Metodo di utilità per richiedere l'ora corrente al server.
     * 
     * @return L'ora corrente dal server
     * @throws IOException Se si verifica un errore durante la comunicazione
     */
    public String getTime() throws IOException {
        return sendAndReceive("time");
    }
    
    /**
     * Metodo di utilità per richiedere la lista dei comandi disponibili.
     * 
     * @return La lista dei comandi disponibili
     * @throws IOException Se si verifica un errore durante la comunicazione
     */
    public String getHelp() throws IOException {
        return sendAndReceive("help");
    }
    
    /**
     * Restituisce il socket per le classi derivate.
     * 
     * @return Il socket della connessione
     */
    protected Socket getSocket() {
        return socket;
    }
    
    /**
     * Restituisce il BufferedReader per le classi derivate.
     * 
     * @return Il BufferedReader per la lettura
     */
    protected BufferedReader getInput() {
        return input;
    }
    
    /**
     * Restituisce il PrintWriter per le classi derivate.
     * 
     * @return Il PrintWriter per la scrittura
     */
    protected PrintWriter getOutput() {
        return output;
    }
    
    /**
     * Imposta lo stato di connessione per le classi derivate.
     * 
     * @param connected Lo stato di connessione
     */
    protected void setConnected(boolean connected) {
        this.connected = connected;
    }
}