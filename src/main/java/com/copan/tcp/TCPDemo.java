package com.copan.tcp;

import java.io.IOException;
import java.util.Scanner;

/**
 * Classe di esempio che dimostra l'utilizzo del sistema client-server TCP.
 * Contiene metodi per avviare server e client interattivi.
 */
public class TCPDemo {
    
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_HOST = "localhost";
    
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        String mode = args[0].toLowerCase();
        
        switch (mode) {
            case "server":
                runServer(args);
                break;
            case "client":
                runClient(args);
                break;
            default:
                System.err.println("Modalità non valida: " + mode);
                printUsage();
        }
    }
    
    /**
     * Avvia il server TCP in modalità interattiva.
     */
    private static void runServer(String[] args) {
        int port = DEFAULT_PORT;
        int maxThreads = 10;
        
        // Parse argomenti server
        for (int i = 1; i < args.length; i += 2) {
            if (i + 1 < args.length) {
                switch (args[i]) {
                    case "-p":
                    case "--port":
                        try {
                            port = Integer.parseInt(args[i + 1]);
                        } catch (NumberFormatException e) {
                            System.err.println("Porta non valida: " + args[i + 1]);
                            return;
                        }
                        break;
                    case "-t":
                    case "--threads":
                        try {
                            maxThreads = Integer.parseInt(args[i + 1]);
                        } catch (NumberFormatException e) {
                            System.err.println("Numero di thread non valido: " + args[i + 1]);
                            return;
                        }
                        break;
                }
            }
        }
        
        TCPServer server = new TCPServer(port, maxThreads);
        
        try {
            System.out.println("Avvio server TCP sulla porta " + port + " con " + maxThreads + " thread massimi...");
            server.start();
            System.out.println("Server avviato con successo!");
            System.out.println("Premi ENTER per fermare il server...");
            
            // Attende input per fermare il server
            try (Scanner scanner = new Scanner(System.in)) {
                scanner.nextLine();
            }
            
            System.out.println("Fermando il server...");
            server.stop();
            System.out.println("Server fermato.");
            
        } catch (IOException e) {
            System.err.println("Errore durante l'avvio del server: " + e.getMessage());
        }
    }
    
    /**
     * Avvia il client TCP in modalità interattiva.
     */
    private static void runClient(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        
        // Parse argomenti client
        for (int i = 1; i < args.length; i += 2) {
            if (i + 1 < args.length) {
                switch (args[i]) {
                    case "-h":
                    case "--host":
                        host = args[i + 1];
                        break;
                    case "-p":
                    case "--port":
                        try {
                            port = Integer.parseInt(args[i + 1]);
                        } catch (NumberFormatException e) {
                            System.err.println("Porta non valida: " + args[i + 1]);
                            return;
                        }
                        break;
                }
            }
        }
        
        TCPClient client = new TCPClient(host, port);
        
        try {
            System.out.println("Connessione al server " + host + ":" + port + "...");
            client.connect();
            System.out.println("Connesso con successo!");
            System.out.println("Comandi disponibili: hello, time, ping, help, quit");
            System.out.println("Oppure digita qualsiasi messaggio per ricevere un echo.");
            System.out.println("Digita 'exit' per disconnetterti.");
            
            try (Scanner scanner = new Scanner(System.in)) {
                String input;
                while (true) {
                    System.out.print("> ");
                    input = scanner.nextLine().trim();
                    
                    if (input.isEmpty()) {
                        continue;
                    }
                    
                    if ("exit".equalsIgnoreCase(input)) {
                        break;
                    }
                    
                    try {
                        String response = client.sendAndReceive(input);
                        System.out.println("< " + response);
                        
                        // Se il server risponde a quit, probabilmente ha chiuso la connessione
                        if ("quit".equalsIgnoreCase(input)) {
                            break;
                        }
                        
                    } catch (IOException e) {
                        System.err.println("Errore nella comunicazione: " + e.getMessage());
                        break;
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Errore di connessione: " + e.getMessage());
        } finally {
            client.disconnect();
            System.out.println("Disconnesso dal server.");
        }
    }
    
    /**
     * Stampa le istruzioni di utilizzo.
     */
    private static void printUsage() {
        System.out.println("Utilizzo: java TCPDemo <modalità> [opzioni]");
        System.out.println();
        System.out.println("Modalità:");
        System.out.println("  server    Avvia il server TCP");
        System.out.println("  client    Avvia il client TCP");
        System.out.println();
        System.out.println("Opzioni server:");
        System.out.println("  -p, --port <porta>     Porta del server (default: " + DEFAULT_PORT + ")");
        System.out.println("  -t, --threads <num>    Numero massimo di thread (default: 10)");
        System.out.println();
        System.out.println("Opzioni client:");
        System.out.println("  -h, --host <host>      Host del server (default: " + DEFAULT_HOST + ")");
        System.out.println("  -p, --port <porta>     Porta del server (default: " + DEFAULT_PORT + ")");
        System.out.println();
        System.out.println("Esempi:");
        System.out.println("  java TCPDemo server");
        System.out.println("  java TCPDemo server -p 9090 -t 20");
        System.out.println("  java TCPDemo client");
        System.out.println("  java TCPDemo client -h 192.168.1.100 -p 9090");
    }
}