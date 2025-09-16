package com.copan.crypto;

import java.io.IOException;

/**
 * Demo per le classi crittografiche CriptoServer e CriptoClient.
 * Mostra come utilizzare comunicazioni TCP sicure con cifratura RSA e firme digitali.
 */
public class CryptoDemo {
    
    public static void main(String[] args) {
        System.out.println("Demo Comunicazione TCP Crittografica");
        System.out.println("=====================================");
        
        final int PORT = 8080;
        final KeyManager keyManager = new KeyManager("demo_keys");
        
        // Thread per il server
        Thread serverThread = new Thread(() -> {
            try {
                System.out.println("Avvio CriptoServer sulla porta " + PORT + "...");
                
                CriptoServer server = new CriptoServer(PORT, 5, keyManager, "demo_server");
                server.start();
                
                // Il server rimane attivo per il test
                Thread.sleep(10000); // 10 secondi
                
                System.out.println("Arresto CriptoServer...");
                server.stop();
                
            } catch (IOException e) {
                System.err.println("Errore nel server: " + e.getMessage());
            } catch (InterruptedException e) {
                System.out.println("Server interrotto");
            }
        });
        
        // Thread per il client
        Thread clientThread = new Thread(() -> {
            try {
                // Aspetta che il server si avvii
                Thread.sleep(2000);
                
                System.out.println("Connessione del CriptoClient al server...");
                
                CriptoClient client = new CriptoClient("localhost", PORT, keyManager, "demo_client");
                client.connect();
                
                System.out.println("Connessione stabilita, handshake crittografico completato: " + 
                                 client.isCryptoHandshakeComplete());
                
                // Test comunicazione sicura
                System.out.println("Test messaggi crittografati:");
                
                String response1 = client.sendAndReceive("ping");
                System.out.println("Risposta a 'ping': " + response1);
                
                String response2 = client.sendAndReceive("time");
                System.out.println("Risposta a 'time': " + response2);
                
                String response3 = client.sendAndReceive("Messaggio segreto!");
                System.out.println("Risposta a messaggio personalizzato: " + response3);
                
                System.out.println("Disconnessione client...");
                client.disconnect();
                
            } catch (IOException e) {
                System.err.println("Errore nel client: " + e.getMessage());
            } catch (InterruptedException e) {
                System.out.println("Client interrotto");
            }
        });
        
        // Avvia server e client
        serverThread.start();
        clientThread.start();
        
        try {
            // Aspetta che finiscano
            serverThread.join();
            clientThread.join();
        } catch (InterruptedException e) {
            System.out.println("Demo interrotta");
        }
        
        System.out.println("Demo completata!");
        System.out.println("Le chiavi RSA sono state salvate nella directory 'demo_keys'");
    }
}