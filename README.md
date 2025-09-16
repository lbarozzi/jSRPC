# jSRPC - Sistema Client-Server TCP Multi-threaded

Un'implementazione Java di un sistema client-server TCP multi-threaded progettato per gestire comunicazioni efficienti tra client e server.

## Caratteristiche

- **Server TCP Multi-threaded**: Gestisce multiple connessioni client simultanee utilizzando un pool di thread
- **Client TCP**: Client semplice e robusto per connettersi e comunicare con il server
- **Gestione delle Connessioni**: Gestione automatica delle connessioni, disconnessioni e errori
- **Protocollo di Comunicazione**: Supporta vari comandi predefiniti (ping, time, help, quit) e messaggi personalizzati
- **Test Completi**: Suite completa di test unitari e di integrazione
- **Demo Interattiva**: Applicazione di esempio per testare il sistema

## Struttura del Progetto

```
src/
├── main/java/com/copan/tcp/
│   ├── TCPServer.java      # Server TCP multi-threaded
│   ├── ClientHandler.java  # Gestore per singole connessioni client
│   ├── TCPClient.java      # Client TCP
│   └── TCPDemo.java        # Applicazione demo interattiva
└── test/java/com/copan/tcp/
    ├── TCPServerTest.java      # Test per TCPServer
    ├── TCPClientTest.java      # Test per TCPClient
    └── TCPIntegrationTest.java # Test di integrazione
```

## Utilizzo

### Compilazione

```bash
mvn clean compile
```

### Esecuzione dei Test

```bash
mvn test
```

### Demo Interattiva

#### Avvio del Server

```bash
# Server con impostazioni predefinite (porta 8080, 10 thread max)
mvn exec:java -Dexec.mainClass="com.copan.tcp.TCPDemo" -Dexec.args="server"

# Server con configurazione personalizzata
mvn exec:java -Dexec.mainClass="com.copan.tcp.TCPDemo" -Dexec.args="server -p 9090 -t 20"
```

#### Avvio del Client

```bash
# Client con impostazioni predefinite (localhost:8080)
mvn exec:java -Dexec.mainClass="com.copan.tcp.TCPDemo" -Dexec.args="client"

# Client con configurazione personalizzata
mvn exec:java -Dexec.mainClass="com.copan.tcp.TCPDemo" -Dexec.args="client -h 192.168.1.100 -p 9090"
```

### Utilizzo Programmatico

#### Server

```java
TCPServer server = new TCPServer(8080, 10);
server.start();

// Il server gestirà automaticamente le connessioni client
// Premere ENTER per fermare...

server.stop();
```

#### Client

```java
TCPClient client = new TCPClient("localhost", 8080);

try {
    client.connect();
    
    // Invia comandi
    String response = client.ping();
    System.out.println(response); // "pong"
    
    String time = client.getTime();
    System.out.println(time); // "Ora corrente: ..."
    
    // Messaggio personalizzato
    String echo = client.sendAndReceive("Hello Server!");
    System.out.println(echo); // "Echo: Hello Server!"
    
} finally {
    client.disconnect();
}
```

## Comandi Supportati

Il server supporta i seguenti comandi predefiniti:

- `ping` - Risponde con "pong"
- `time` - Restituisce l'ora corrente del server
- `hello` - Risponde con un saluto
- `help` - Mostra la lista dei comandi disponibili
- `quit` - Chiude la connessione client
- Qualsiasi altro messaggio viene ripetuto come echo

## Architettura

### TCPServer
- Utilizza un `ServerSocket` per accettare connessioni
- Gestisce ogni client in un thread separato utilizzando un `ExecutorService`
- Thread-safe e può gestire multiple connessioni simultanee
- Supporta start/stop graceful

### ClientHandler
- Gestisce la comunicazione con un singolo client
- Implementa `Runnable` per esecuzione in thread separato
- Gestisce automaticamente la disconnessione e pulizia delle risorse

### TCPClient
- Fornisce un'interfaccia semplice per la comunicazione client-server
- Supporta operazioni sincrone di send/receive
- Gestione automatica degli errori e delle disconnessioni

## Test

Il progetto include tre suite di test:

1. **TCPServerTest**: Test unitari per il server
2. **TCPClientTest**: Test unitari per il client
3. **TCPIntegrationTest**: Test di integrazione completi

I test coprono:
- Connessioni singole e multiple
- Gestione degli errori
- Scenari concorrenti
- Riconnessioni
- Comandi del protocollo

## Requisiti

- Java 8 o superiore
- Maven 3.6 o superiore
- JUnit 4.11 (per i test)

## Licenza

Questo progetto è rilasciato sotto licenza MIT.
Secure tunnel for XML/RPC 
