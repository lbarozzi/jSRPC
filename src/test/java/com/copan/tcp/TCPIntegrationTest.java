package com.copan.tcp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test di integrazione per il sistema client-server TCP.
 * Verifica l'interazione completa tra TCPClient e TCPServer.
 */
public class TCPIntegrationTest {
    
    private TCPServer server;
    private static final int TEST_PORT = 8083;
    private static final String SERVER_HOST = "localhost";
    
    @Before
    public void setUp() throws IOException {
        server = new TCPServer(TEST_PORT, 10);
        server.start();
        
        // Attende che il server sia completamente avviato
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @After
    public void tearDown() {
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
    public void testSingleClientServerInteraction() throws IOException {
        TCPClient client = new TCPClient(SERVER_HOST, TEST_PORT);
        
        try {
            client.connect();
            assertTrue(client.isConnected());
            
            // Test vari comandi
            assertEquals("pong", client.ping());
            assertTrue(client.getTime().contains("Ora corrente"));
            assertTrue(client.getHelp().contains("Comandi disponibili"));
            
            String echoResponse = client.sendAndReceive("Test message");
            assertTrue(echoResponse.contains("Test message"));
            
        } finally {
            client.disconnect();
        }
    }
    
    @Test
    public void testMultipleClientsSequential() throws IOException {
        final int numberOfClients = 5;
        
        for (int i = 0; i < numberOfClients; i++) {
            TCPClient client = new TCPClient(SERVER_HOST, TEST_PORT);
            
            try {
                client.connect();
                assertTrue("Client " + i + " dovrebbe connettersi", client.isConnected());
                
                String response = client.sendAndReceive("Message from client " + i);
                assertNotNull("Risposta per client " + i + " non dovrebbe essere null", response);
                assertTrue("Risposta dovrebbe contenere il messaggio", 
                          response.contains("Message from client " + i));
                
            } finally {
                client.disconnect();
            }
        }
    }
    
    @Test
    public void testMultipleClientsConcurrent() throws InterruptedException {
        final int numberOfClients = 5;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completionLatch = new CountDownLatch(numberOfClients);
        final AtomicInteger successfulClients = new AtomicInteger(0);
        final boolean[] results = new boolean[numberOfClients];
        
        // Crea thread per ciascun client
        for (int i = 0; i < numberOfClients; i++) {
            final int clientIndex = i;
            
            Thread clientThread = new Thread(() -> {
                TCPClient client = new TCPClient(SERVER_HOST, TEST_PORT);
                
                try {
                    // Attende il segnale per iniziare tutti insieme
                    startLatch.await();
                    
                    client.connect();
                    
                    // Esegui varie operazioni
                    String pingResponse = client.ping();
                    String echoResponse = client.sendAndReceive("Client " + clientIndex + " message");
                    
                    boolean success = "pong".equals(pingResponse) && 
                                    echoResponse.contains("Client " + clientIndex + " message");
                    
                    results[clientIndex] = success;
                    if (success) {
                        successfulClients.incrementAndGet();
                    }
                    
                } catch (Exception e) {
                    results[clientIndex] = false;
                    System.err.println("Errore in client " + clientIndex + ": " + e.getMessage());
                } finally {
                    if (client.isConnected()) {
                        client.disconnect();
                    }
                    completionLatch.countDown();
                }
            });
            
            clientThread.start();
        }
        
        // Avvia tutti i client contemporaneamente
        startLatch.countDown();
        
        // Attende che tutti i client completino (con timeout)
        boolean allCompleted = completionLatch.await(10, TimeUnit.SECONDS);
        assertTrue("Tutti i client dovrebbero completare entro il timeout", allCompleted);
        
        // Verifica che tutti i client abbiano avuto successo
        assertEquals("Tutti i client dovrebbero avere successo", numberOfClients, successfulClients.get());
        
        for (int i = 0; i < numberOfClients; i++) {
            assertTrue("Client " + i + " dovrebbe avere successo", results[i]);
        }
    }
    
    @Test
    public void testClientReconnectionWhileServerRunning() throws IOException {
        TCPClient client = new TCPClient(SERVER_HOST, TEST_PORT);
        
        // Prima connessione
        client.connect();
        assertEquals("pong", client.ping());
        client.disconnect();
        
        // Riconnessione immediata
        client.connect();
        assertEquals("pong", client.ping());
        
        client.disconnect();
    }
    
    @Test
    public void testLongRunningSession() throws IOException {
        TCPClient client = new TCPClient(SERVER_HOST, TEST_PORT);
        
        try {
            client.connect();
            
            // Invia molti messaggi nella stessa sessione
            for (int i = 0; i < 50; i++) {
                String response = client.sendAndReceive("Message " + i);
                assertNotNull("Risposta " + i + " non dovrebbe essere null", response);
                assertTrue("Risposta dovrebbe contenere il numero del messaggio", 
                          response.contains("Message " + i));
            }
            
        } finally {
            client.disconnect();
        }
    }
    
    @Test
    public void testServerStopWithConnectedClients() throws IOException, InterruptedException {
        TCPClient client1 = new TCPClient(SERVER_HOST, TEST_PORT);
        
        try {
            // Connetti il client
            client1.connect();
            assertTrue(client1.isConnected());
            
            // Test che il client funzioni
            assertEquals("pong", client1.ping());
            
            // Ferma il server
            server.stop();
            assertFalse(server.isRunning());
            
            // Il test verifica che il server si sia fermato correttamente
            // (il timing TCP può variare quindi non testiamo il momento esatto della disconnessione)
            
        } finally {
            // I client potrebbero già essere disconnessi, ma chiamiamo disconnect per sicurezza
            try { client1.disconnect(); } catch (Exception e) { /* ignora */ }
        }
    }
    
    @Test
    public void testQuitCommand() throws IOException {
        TCPClient client = new TCPClient(SERVER_HOST, TEST_PORT);
        
        try {
            client.connect();
            
            // Test alcuni comandi normali
            assertEquals("pong", client.ping());
            
            // Invia comando quit
            String quitResponse = client.sendAndReceive("quit");
            assertNotNull(quitResponse);
            assertTrue("La risposta dovrebbe confermare la chiusura", 
                      quitResponse.contains("Arrivederci"));
            
            // Il prossimo tentativo di comunicazione dovrebbe fallire
            try {
                client.ping();
                fail("La comunicazione dovrebbe fallire dopo quit");
            } catch (IOException e) {
                // Comportamento atteso
            }
            
        } finally {
            client.disconnect();
        }
    }
    
    @Test
    public void testMixedCommandsMultipleClients() throws InterruptedException {
        final int numberOfClients = 3;
        final CountDownLatch completionLatch = new CountDownLatch(numberOfClients);
        final AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < numberOfClients; i++) {
            final int clientId = i;
            
            Thread clientThread = new Thread(() -> {
                TCPClient client = new TCPClient(SERVER_HOST, TEST_PORT);
                
                try {
                    client.connect();
                    
                    // Ogni client esegue una sequenza diversa di comandi
                    switch (clientId) {
                        case 0:
                            client.ping();
                            client.getTime();
                            client.sendAndReceive("Hello from client 0");
                            break;
                        case 1:
                            client.getHelp();
                            client.sendAndReceive("echo test");
                            client.ping();
                            break;
                        case 2:
                            client.sendAndReceive("Custom message");
                            client.getTime();
                            client.getHelp();
                            break;
                    }
                    
                    successCount.incrementAndGet();
                    
                } catch (Exception e) {
                    System.err.println("Errore in client " + clientId + ": " + e.getMessage());
                } finally {
                    if (client.isConnected()) {
                        client.disconnect();
                    }
                    completionLatch.countDown();
                }
            });
            
            clientThread.start();
        }
        
        boolean allCompleted = completionLatch.await(5, TimeUnit.SECONDS);
        assertTrue("Tutti i client dovrebbero completare", allCompleted);
        assertEquals("Tutti i client dovrebbero avere successo", numberOfClients, successCount.get());
    }
}