package com.copan.tcp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;

/**
 * Test per la classe TCPClient.
 * Verifica il funzionamento del client TCP.
 */
public class TCPClientTest {
    
    private TCPServer server;
    private TCPClient client;
    private static final int TEST_PORT = 8082;
    private static final String SERVER_HOST = "localhost";
    
    @Before
    public void setUp() throws IOException {
        // Avvia un server di test
        server = new TCPServer(TEST_PORT, 3);
        server.start();
        
        // Attende che il server sia completamente avviato
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        client = new TCPClient(SERVER_HOST, TEST_PORT);
    }
    
    @After
    public void tearDown() {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
        
        if (server != null && server.isRunning()) {
            server.stop();
        }
        
        // Attende la chiusura completa
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    public void testClientCreation() {
        assertEquals(SERVER_HOST, client.getServerHost());
        assertEquals(TEST_PORT, client.getServerPort());
        assertFalse(client.isConnected());
    }
    
    @Test
    public void testConnectAndDisconnect() throws IOException {
        // Test connessione
        client.connect();
        assertTrue(client.isConnected());
        
        // Test disconnessione
        client.disconnect();
        assertFalse(client.isConnected());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testConnectTwice() throws IOException {
        client.connect();
        client.connect(); // Dovrebbe lanciare IllegalStateException
    }
    
    @Test(expected = IOException.class)
    public void testConnectToInvalidServer() throws IOException {
        TCPClient invalidClient = new TCPClient("localhost", 9999); // Porta non in uso
        invalidClient.connect();
    }
    
    @Test
    public void testSendAndReceiveMessage() throws IOException {
        client.connect();
        
        // Test invio messaggio semplice
        String testMessage = "Hello Server";
        String response = client.sendAndReceive(testMessage);
        
        assertNotNull(response);
        assertTrue("La risposta dovrebbe contenere il messaggio originale", 
                  response.contains(testMessage));
    }
    
    @Test
    public void testPingCommand() throws IOException {
        client.connect();
        
        String response = client.ping();
        assertNotNull(response);
        assertEquals("pong", response);
    }
    
    @Test
    public void testTimeCommand() throws IOException {
        client.connect();
        
        String response = client.getTime();
        assertNotNull(response);
        assertTrue("The response should contain 'Current time'", 
                  response.contains("Current time"));
    }
    
    @Test
    public void testHelpCommand() throws IOException {
        client.connect();
        
        String response = client.getHelp();
        assertNotNull(response);
        assertTrue("The response should contain 'Available commands'", 
                  response.contains("Available commands"));
    }
    
    @Test
    public void testHelloCommand() throws IOException {
        client.connect();
        
        String response = client.sendAndReceive("hello");
        assertNotNull(response);
        assertTrue("La risposta dovrebbe contenere 'Hello!'", 
                  response.contains("Hello!"));
    }
    
    @Test
    public void testMultipleMessages() throws IOException {
        client.connect();
        
        // Invia multipli messaggi nella stessa sessione
        String[] testMessages = {"ping", "hello", "time", "help"};
        
        for (String message : testMessages) {
            String response = client.sendAndReceive(message);
            assertNotNull("Risposta per '" + message + "' non dovrebbe essere null", response);
            assertFalse("Risposta per '" + message + "' non dovrebbe essere vuota", 
                       response.trim().isEmpty());
        }
    }
    
    @Test(expected = IOException.class)
    public void testSendMessageWithoutConnection() throws IOException {
        // Tenta di inviare un messaggio senza essere connesso
        client.sendMessage("test");
    }
    
    @Test(expected = IOException.class)
    public void testReceiveMessageWithoutConnection() throws IOException {
        // Tenta di ricevere un messaggio senza essere connesso
        client.receiveMessage();
    }
    
    @Test
    public void testDisconnectWithoutConnection() {
        // Non dovrebbe lanciare eccezioni
        client.disconnect();
        assertFalse(client.isConnected());
    }
    
    @Test
    public void testReconnection() throws IOException {
        // Prima connessione
        client.connect();
        assertTrue(client.isConnected());
        
        String response1 = client.ping();
        assertEquals("pong", response1);
        
        // Disconnessione
        client.disconnect();
        assertFalse(client.isConnected());
        
        // Riconnessione
        client.connect();
        assertTrue(client.isConnected());
        
        String response2 = client.ping();
        assertEquals("pong", response2);
    }
    
    @Test
    public void testEmptyMessage() throws IOException {
        client.connect();
        
        String response = client.sendAndReceive("");
        assertNotNull(response);
        assertTrue("The response should indicate empty message", 
                  response.contains("Empty"));
    }
    
    @Test
    public void testCaseInsensitiveCommands() throws IOException {
        client.connect();
        
        // Test comandi con case diverse
        String response1 = client.sendAndReceive("PING");
        String response2 = client.sendAndReceive("ping");
        String response3 = client.sendAndReceive("Ping");
        
        assertEquals("pong", response1);
        assertEquals("pong", response2);
        assertEquals("pong", response3);
    }
}