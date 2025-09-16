package com.copan;

import com.copan.crypto.CryptoServer;
import com.copan.crypto.CryptoClient;
import com.copan.crypto.KeyManager;
import com.copan.crypto.KeyGenerator;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Scanner;

/**
 * Main application class for jSRPC - Multi-Threaded TCP Client/Server with RSA Encryption.
 * 
 * Provides both command-line interface and interactive menu for:
 * - TCP Server and Client operations
 * - Secure Crypto Server and Client operations  
 * - RSA Key generation
 * 
 * Usage:
 * - No arguments: Interactive menu
 * - client <ip> <port>: Start crypto client
 * - server <ip> <port>: Start crypto server with 10 threads
 * - server <ip> <port> <threads>: Start crypto server with custom thread count
 * - help: Show usage information
 */
public class App {
    
    private static final String VERSION = "1.0.0";
    private static final String APP_NAME = "jSRPC";
    
    public static void main(String[] args) {
        printHeader();
        
        if (args.length == 0) {
            // No arguments - show interactive menu
            showInteractiveMenu();
        } else {
            // Command line arguments provided
            handleCommandLineArgs(args);
        }
    }
    
    private static void printHeader() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  " + APP_NAME + " v" + VERSION + " - Multi-Threaded TCP with RSA Encryption  ║");
        System.out.println("║  Secure tunnel for XML/RPC communications               ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    private static void showInteractiveMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        
        while (!exit) {
            printMenu();
            System.out.print("Select option: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice.toLowerCase()) {
                case "1":
                case "generatekey":
                    handleGenerateKey(scanner);
                    break;
                case "2":
                case "client":
                    handleClientMenu(scanner);
                    break;
                case "3":
                case "server":
                    handleServerMenu(scanner);
                    break;
                case "4":
                case "help":
                    showHelp();
                    break;
                case "5":
                case "exit":
                case "quit":
                    exit = true;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
            
            if (!exit) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
        
        scanner.close();
    }
    
    private static void printMenu() {
        System.out.println("\n═══════════════ MAIN MENU ═══════════════");
        System.out.println("1. GenerateKey  - Generate RSA key pairs");
        System.out.println("2. Client       - Start crypto client");
        System.out.println("3. Server       - Start crypto server");
        System.out.println("4. Help         - Show detailed help");
        System.out.println("5. Exit         - Quit application");
        System.out.println("═════════════════════════════════════════");
    }
    
    private static void handleGenerateKey(Scanner scanner) {
        System.out.println("\n═══ RSA Key Generation ═══");
        
        System.out.print("Enter key name (default: 'default'): ");
        String keyName = scanner.nextLine().trim();
        if (keyName.isEmpty()) {
            keyName = "default";
        }
        
        System.out.print("Enter keys directory (default: 'keys'): ");
        String keysDir = scanner.nextLine().trim();
        if (keysDir.isEmpty()) {
            keysDir = "keys";
        }
        
        System.out.println("Select key size:");
        System.out.println("1. 1024 bit (fast, less secure)");
        System.out.println("2. 2048 bit (recommended)");
        System.out.println("3. 3072 bit (high security)");
        System.out.println("4. 4096 bit (maximum security, slow)");
        System.out.print("Choice (default: 2): ");
        
        String sizeChoice = scanner.nextLine().trim();
        int keySize = KeyGenerator.KEY_SIZE_2048; // default
        
        switch (sizeChoice) {
            case "1":
                keySize = KeyGenerator.KEY_SIZE_1024;
                break;
            case "3":
                keySize = KeyGenerator.KEY_SIZE_3072;
                break;
            case "4":
                keySize = KeyGenerator.KEY_SIZE_4096;
                break;
            default:
                keySize = KeyGenerator.KEY_SIZE_2048;
                break;
        }
        
        try {
            System.out.println("\nGenerating " + keySize + "-bit RSA key pair...");
            KeyGenerator generator = new KeyGenerator(keySize);
            KeyPair keyPair = generator.generateKeyPair();
            
            KeyManager keyManager = new KeyManager(keysDir);
            keyManager.saveKeyPair(keyName, keyPair);
            
            System.out.println("✓ RSA key pair generated successfully!");
            System.out.println("  Key name: " + keyName);
            System.out.println("  Key size: " + keySize + " bits");
            System.out.println("  Directory: " + keysDir);
            
        } catch (Exception e) {
            System.err.println("✗ Error generating keys: " + e.getMessage());
        }
    }
    
    private static void handleClientMenu(Scanner scanner) {
        System.out.println("\n═══ Crypto Client ═══");
        
        System.out.print("Enter server IP (default: localhost): ");
        String ip = scanner.nextLine().trim();
        if (ip.isEmpty()) {
            ip = "localhost";
        }
        
        System.out.print("Enter server port (default: 8080): ");
        String portStr = scanner.nextLine().trim();
        int port = 8080;
        if (!portStr.isEmpty()) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default: 8080");
                port = 8080;
            }
        }
        
        System.out.print("Enter client key name (default: 'client'): ");
        String clientKeyName = scanner.nextLine().trim();
        if (clientKeyName.isEmpty()) {
            clientKeyName = "client";
        }
        
        System.out.print("Enter keys directory (default: 'keys'): ");
        String keysDir = scanner.nextLine().trim();
        if (keysDir.isEmpty()) {
            keysDir = "keys";
        }
        
        startCryptoClient(ip, port, clientKeyName, keysDir, scanner);
    }
    
    private static void handleServerMenu(Scanner scanner) {
        System.out.println("\n═══ Crypto Server ═══");
        
        System.out.print("Enter server IP (default: localhost): ");
        String ip = scanner.nextLine().trim();
        if (ip.isEmpty()) {
            ip = "localhost";
        }
        
        System.out.print("Enter server port (default: 8080): ");
        String portStr = scanner.nextLine().trim();
        int port = 8080;
        if (!portStr.isEmpty()) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default: 8080");
                port = 8080;
            }
        }
        
        System.out.print("Enter max threads (default: 10): ");
        String threadsStr = scanner.nextLine().trim();
        int maxThreads = 10;
        if (!threadsStr.isEmpty()) {
            try {
                maxThreads = Integer.parseInt(threadsStr);
                if (maxThreads <= 0) {
                    System.err.println("Invalid thread count, using default: 10");
                    maxThreads = 10;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid thread count, using default: 10");
                maxThreads = 10;
            }
        }
        
        System.out.print("Enter server key name (default: 'server'): ");
        String serverKeyName = scanner.nextLine().trim();
        if (serverKeyName.isEmpty()) {
            serverKeyName = "server";
        }
        
        System.out.print("Enter keys directory (default: 'keys'): ");
        String keysDir = scanner.nextLine().trim();
        if (keysDir.isEmpty()) {
            keysDir = "keys";
        }
        
        startCryptoServer(ip, port, maxThreads, serverKeyName, keysDir);
    }
    
    private static void handleCommandLineArgs(String[] args) {
        String command = args[0].toLowerCase();
        
        switch (command) {
            case "client":
                if (args.length >= 3) {
                    String ip = args[1];
                    int port = parsePort(args[2]);
                    if (port > 0) {
                        startCryptoClient(ip, port, "client", "keys", null);
                    }
                } else {
                    System.err.println("Usage: client <ip> <port>");
                    showHelp();
                }
                break;
                
            case "server":
                if (args.length >= 3) {
                    String ip = args[1];
                    int port = parsePort(args[2]);
                    int threads = 10; // default
                    
                    if (args.length >= 4) {
                        threads = parseThreads(args[3]);
                    }
                    
                    if (port > 0 && threads > 0) {
                        startCryptoServer(ip, port, threads, "server", "keys");
                    }
                } else {
                    System.err.println("Usage: server <ip> <port> [threads]");
                    showHelp();
                }
                break;
                
            case "help":
            case "-h":
            case "--help":
                showHelp();
                break;
                
            default:
                System.err.println("Unknown command: " + command);
                showHelp();
        }
    }
    
    private static void startCryptoClient(String ip, int port, String clientKeyName, String keysDir, Scanner scanner) {
        try {
            KeyManager keyManager = new KeyManager(keysDir);
            CryptoClient client = new CryptoClient(ip, port, keyManager, clientKeyName);
            
            System.out.println("\nConnecting to crypto server " + ip + ":" + port + "...");
            client.connect();
            
            System.out.println("✓ Connected! Crypto handshake: " + 
                             (client.isCryptoHandshakeComplete() ? "SUCCESS" : "PENDING"));
            
            if (scanner != null) {
                // Interactive mode
                runInteractiveClient(client, scanner);
            } else {
                // Command line mode - run basic test
                runBasicClientTest(client);
            }
            
            client.disconnect();
            System.out.println("✓ Client disconnected");
            
        } catch (IOException e) {
            System.err.println("✗ Client error: " + e.getMessage());
        }
    }
    
    private static void startCryptoServer(String ip, int port, int maxThreads, String serverKeyName, String keysDir) {
        try {
            KeyManager keyManager = new KeyManager(keysDir);
            CryptoServer server = new CryptoServer(port, maxThreads, keyManager, serverKeyName);
            
            System.out.println("\nStarting crypto server on " + ip + ":" + port + 
                             " (max threads: " + maxThreads + ")...");
            server.start();
            
            System.out.println("✓ Crypto server started successfully!");
            System.out.println("  Server keys: " + (server.hasValidKeys() ? "OK" : "GENERATED"));
            System.out.println("  Press Enter to stop server...");
            
            // Wait for user input to stop server
            try (Scanner scanner = new Scanner(System.in)) {
                scanner.nextLine();
            }
            
            System.out.println("\nStopping server...");
            server.stop();
            System.out.println("✓ Server stopped");
            
        } catch (IOException e) {
            System.err.println("✗ Server error: " + e.getMessage());
        }
    }
    
    private static void runInteractiveClient(CryptoClient client, Scanner scanner) {
        System.out.println("\n═══ Interactive Client Session ═══");
        System.out.println("Enter messages to send (or 'quit' to exit):");
        
        String message;
        while (!(message = scanner.nextLine().trim()).equalsIgnoreCase("quit")) {
            if (!message.isEmpty()) {
                try {
                    String response = client.sendAndReceive(message);
                    System.out.println("Server response: " + response);
                } catch (IOException e) {
                    System.err.println("Error sending message: " + e.getMessage());
                    break;
                }
            }
        }
    }
    
    private static void runBasicClientTest(CryptoClient client) {
        try {
            System.out.println("\nRunning basic client test...");
            
            String response1 = client.sendAndReceive("ping");
            System.out.println("Ping response: " + response1);
            
            String response2 = client.sendAndReceive("time");
            System.out.println("Time response: " + response2);
            
            String response3 = client.sendAndReceive("Hello encrypted world!");
            System.out.println("Custom message response: " + response3);
            
        } catch (IOException e) {
            System.err.println("Test error: " + e.getMessage());
        }
    }
    
    private static int parsePort(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                System.err.println("Invalid port range: " + port + " (must be 1-65535)");
                return -1;
            }
            return port;
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + portStr);
            return -1;
        }
    }
    
    private static int parseThreads(String threadsStr) {
        try {
            int threads = Integer.parseInt(threadsStr);
            if (threads < 1 || threads > 1000) {
                System.err.println("Invalid thread count: " + threads + " (must be 1-1000)");
                return -1;
            }
            return threads;
        } catch (NumberFormatException e) {
            System.err.println("Invalid thread count: " + threadsStr);
            return -1;
        }
    }
    
    private static void showHelp() {
        System.out.println("\n" + APP_NAME + " v" + VERSION + " - Usage Information");
        System.out.println("═══════════════════════════════════════════");
        System.out.println();
        System.out.println("COMMAND LINE USAGE:");
        System.out.println("  java -jar jSRPC.jar                    - Interactive menu");
        System.out.println("  java -jar jSRPC.jar client <ip> <port> - Start crypto client");
        System.out.println("  java -jar jSRPC.jar server <ip> <port> - Start crypto server (10 threads)");
        System.out.println("  java -jar jSRPC.jar server <ip> <port> <threads> - Start with custom threads");
        System.out.println("  java -jar jSRPC.jar help               - Show this help");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  java -jar jSRPC.jar client localhost 8080");
        System.out.println("  java -jar jSRPC.jar server 0.0.0.0 8080 20");
        System.out.println();
        System.out.println("FEATURES:");
        System.out.println("  • Multi-threaded TCP server with configurable thread pool");
        System.out.println("  • RSA-2048 encryption with digital signatures");
        System.out.println("  • Automatic key generation and management");
        System.out.println("  • GPG-like secure communication protocol");
        System.out.println("  • Built-in commands: ping, time, help");
        System.out.println();
        System.out.println("KEY MANAGEMENT:");
        System.out.println("  Keys are automatically generated and stored in PEM format");
        System.out.println("  Default directory: ./keys/");
        System.out.println("  Default server key: 'server'");
        System.out.println("  Default client key: 'client'");
        System.out.println();
        System.out.println("SECURITY:");
        System.out.println("  • RSA-2048 encryption (PKCS1 padding for Java 8 compatibility)");
        System.out.println("  • SHA256withRSA digital signatures");
        System.out.println("  • Secure handshake with public key exchange");
        System.out.println("  • Message integrity verification");
    }
}
