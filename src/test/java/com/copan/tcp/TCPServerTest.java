package com.copan.tcp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.Socket;

/**
 * Test per la classe TCPServer.
 * Verifica il funzionamento del server TCP multi-threaded.
 */
public class TCPServerTest {
    
    private TCPServer server;
    private static final int TEST_PORT = 8080;
    private static final int ALTERNATIVE_PORT = 8081;
    
    @Before
    public void setUp() {
        server = new TCPServer(TEST_PORT, 5);
    }
    
    @After
    public void tearDown() {
        if (server != null && server.isRunning()) {
            server.stop();
            // Attende un po' per la chiusura completa
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    @Test
    public void testServerCreation() {
        assertEquals(TEST_PORT, server.getPort());
        assertEquals(5, server.getMaxThreads());
        assertFalse(server.isRunning());
    }
    
    @Test
    public void testServerStartAndStop() throws IOException {
        // Test avvio server
        server.start();
        assertTrue(server.isRunning());
        
        // Test che il server stia ascoltando sulla porta corretta
        try {
            Socket testSocket = new Socket("localhost", TEST_PORT);
            assertTrue(testSocket.isConnected());
            testSocket.close();
        } catch (IOException e) {
            fail("Server non è raggiungibile sulla porta " + TEST_PORT);
        }
        
        // Test stop server
        server.stop();
        assertFalse(server.isRunning());
        
        // Attende che il server sia completamente chiuso
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verifica che il server non sia più raggiungibile
        try {
            Socket testSocket = new Socket("localhost", TEST_PORT);
            testSocket.close();
            fail("Il server dovrebbe essere chiuso ma è ancora raggiungibile");
        } catch (IOException e) {
            // Questo è il comportamento atteso
        }
    }
    
    @Test
    public void testMultipleConnections() throws IOException, InterruptedException {
        server.start();
        
        // Crea multiple connessioni simultanee
        Socket[] sockets = new Socket[3];
        
        try {
            for (int i = 0; i < sockets.length; i++) {
                sockets[i] = new Socket("localhost", TEST_PORT);
                assertTrue("Connessione " + i + " dovrebbe essere stabilita", 
                          sockets[i].isConnected());
            }
            
            // Attende un po' per permettere al server di processare tutte le connessioni
            Thread.sleep(100);
            
        } finally {
            // Chiudi tutte le connessioni
            for (Socket socket : sockets) {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        }
    }
    
    @Test(expected = IllegalStateException.class)
    public void testStartServerTwice() throws IOException {
        server.start();
        server.start(); // Dovrebbe lanciare IllegalStateException
    }
    
    @Test
    public void testStopServerNotStarted() {
        // Non dovrebbe lanciare eccezioni
        server.stop();
        assertFalse(server.isRunning());
    }
    
    @Test
    public void testServerWithDefaultThreadPool() throws IOException {
        TCPServer defaultServer = new TCPServer(ALTERNATIVE_PORT);
        
        assertEquals(ALTERNATIVE_PORT, defaultServer.getPort());
        assertEquals(10, defaultServer.getMaxThreads()); // Default value
        
        try {
            defaultServer.start();
            assertTrue(defaultServer.isRunning());
        } finally {
            defaultServer.stop();
        }
    }
    
    @Test
    public void testServerRestart() throws IOException, InterruptedException {
        // Avvia il server
        server.start();
        assertTrue(server.isRunning());
        
        // Test connessione
        Socket testSocket1 = new Socket("localhost", TEST_PORT);
        assertTrue(testSocket1.isConnected());
        testSocket1.close();
        
        // Ferma il server
        server.stop();
        assertFalse(server.isRunning());
        
        // Attende la chiusura completa
        Thread.sleep(200);
        
        // Crea un nuovo server sulla stessa porta
        server = new TCPServer(TEST_PORT, 5);
        server.start();
        assertTrue(server.isRunning());
        
        // Test che il server riavviato funzioni
        Socket testSocket2 = new Socket("localhost", TEST_PORT);
        assertTrue(testSocket2.isConnected());
        testSocket2.close();
    }
    
    @Test
    public void testConcurrentConnections() throws IOException, InterruptedException {
        server.start();
        
        final int numberOfClients = 5;
        Thread[] clientThreads = new Thread[numberOfClients];
        final boolean[] connectionResults = new boolean[numberOfClients];
        
        // Crea thread client che si connettono contemporaneamente
        for (int i = 0; i < numberOfClients; i++) {
            final int clientIndex = i;
            clientThreads[i] = new Thread(() -> {
                try (Socket socket = new Socket("localhost", TEST_PORT)) {
                    connectionResults[clientIndex] = socket.isConnected();
                    Thread.sleep(50); // Simula qualche attività
                } catch (Exception e) {
                    connectionResults[clientIndex] = false;
                }
            });
        }
        
        // Avvia tutti i thread contemporaneamente
        for (Thread thread : clientThreads) {
            thread.start();
        }
        
        // Attende che tutti i thread terminino
        for (Thread thread : clientThreads) {
            thread.join(1000); // Timeout di 1 secondo per thread
        }
        
        // Verifica che tutte le connessioni abbiano avuto successo
        for (int i = 0; i < numberOfClients; i++) {
            assertTrue("Client " + i + " dovrebbe essersi connesso con successo", 
                      connectionResults[i]);
        }
    }
}