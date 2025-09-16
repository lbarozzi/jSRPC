# Comunicazione TCP Sicura - CriptoServer e CriptoClient

## Panoramica

Questo progetto estende le classi TCP di base (`TCPServer` e `TCPClient`) per fornire comunicazioni sicure utilizzando crittografia RSA e firme digitali. Le nuove classi `CriptoServer` e `CriptoClient` implementano un protocollo di comunicazione tipo GPG con:

- **Cifratura RSA-2048**: Tutti i messaggi sono cifrati con la chiave pubblica del destinatario
- **Firme digitali SHA256withRSA**: Ogni messaggio è firmato per garantire autenticità e integrità
- **Gestione automatica delle chiavi**: Generazione, salvataggio e caricamento automatico delle chiavi in formato PEM
- **Handshake crittografico**: Scambio sicuro delle chiavi pubbliche all'inizio della connessione

## Architettura

### Classi Principali

1. **CriptoServer**: Server TCP con supporto crittografico
   - Estende `TCPServer`
   - Utilizza `CriptoClientHandler` per gestire client sicuri
   - Supporta multiple connessioni crittografiche simultanee

2. **CriptoClient**: Client TCP con supporto crittografico
   - Estende `TCPClient`
   - Gestisce handshake crittografico automatico
   - Cifra/decifra automaticamente tutti i messaggi

3. **CriptoClientHandler**: Handler per singoli client crittografici
   - Estende `ClientHandler`
   - Gestisce l'handshake crittografico lato server
   - Processa messaggi sicuri

### Classi di Supporto

4. **KeyManager**: Gestione delle chiavi RSA
   - Generazione automatica di coppie di chiavi RSA-2048
   - Salvataggio in formato PEM standard
   - Caricamento da file esistenti

5. **CryptoUtils**: Utilities crittografiche
   - Metodi per cifratura/decifratura RSA
   - Firma e verifica digitale SHA256withRSA
   - Conversione chiavi e encoding Base64

6. **SecureMessage**: Rappresentazione dei messaggi sicuri
   - Contenitore per contenuto cifrato + firma
   - Serializzazione/deserializzazione JSON
   - Timestamp per anti-replay

## Protocollo di Comunicazione

### 1. Handshake Crittografico

```
Client → Server: "CRYPTO_HANDSHAKE_START:<client_public_key_base64>"
Server → Client: "SERVER_PUBLIC_KEY:<server_public_key_base64>"
Server → Client: "HANDSHAKE_COMPLETE"
```

### 2. Messaggi Sicuri

Ogni messaggio dopo l'handshake segue il formato:
```
"SECURE_MESSAGE:<json_secure_message>"
```

Dove `json_secure_message` contiene:
- `encryptedContent`: Messaggio cifrato con chiave pubblica destinatario
- `signature`: Firma digitale del messaggio originale
- `senderPublicKey`: Identificazione mittente
- `timestamp`: Timestamp di creazione

## Utilizzo

### Server Crittografico

```java
// Crea gestore chiavi
KeyManager keyManager = new KeyManager("keys");

// Avvia server crittografico
CriptoServer server = new CriptoServer(8080, 10, keyManager, "server");
server.start();

// ... server gestisce automaticamente connessioni sicure ...

server.stop();
```

### Client Crittografico

```java
// Crea gestore chiavi
KeyManager keyManager = new KeyManager("keys");

// Connetti al server sicuro
CriptoClient client = new CriptoClient("localhost", 8080, keyManager, "client");
client.connect(); // Include handshake automatico

// Comunicazione sicura trasparente
String response = client.sendAndReceive("ping");
System.out.println("Risposta sicura: " + response);

client.disconnect();
```

### Demo Completa

Esegui la demo per vedere il sistema in azione:
```bash
mvn exec:java -Dexec.mainClass="com.copan.crypto.CryptoDemo"
```

## Gestione delle Chiavi

### Struttura Directory
```
keys/
├── server_public.pem    # Chiave pubblica server
├── server_private.pem   # Chiave privata server
├── client_public.pem    # Chiave pubblica client
└── client_private.pem   # Chiave privata client
```

### Formato PEM Standard
Le chiavi vengono salvate nel formato PEM standard, compatibile con OpenSSL e altri strumenti crittografici:

```
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...
-----END PUBLIC KEY-----
```

### Generazione Automatica
- Le chiavi vengono generate automaticamente se non esistono
- RSA-2048 bit per sicurezza adeguata
- Salvataggio persistente per riutilizzo

## Sicurezza

### Caratteristiche di Sicurezza

1. **Confidenzialità**: Crittografia RSA-2048 con padding PKCS1
2. **Autenticità**: Firme digitali SHA256withRSA
3. **Integrità**: Verifica firme per ogni messaggio
4. **Anti-replay**: Timestamp nei messaggi sicuri

### Algoritmi Utilizzati

- **Cifratura**: RSA/ECB/PKCS1Padding (compatibile Java 8)
- **Firma**: SHA256withRSA
- **Hash**: SHA-256
- **Encoding**: Base64 per trasmissione

### Limitazioni Java 8

Il codice è ottimizzato per Java 8, utilizzando:
- PKCS1 padding invece di OAEP (non disponibile in Java 8)
- SHA256withRSA invece di PSS (non disponibile in Java 8)

## Test

### Suite Completa di Test

Esegui tutti i test:
```bash
mvn test
```

I test coprono:
- Generazione e gestione chiavi
- Handshake crittografico
- Comunicazione sicura end-to-end
- Serializzazione messaggi sicuri
- Operazioni crittografiche base

### Test Specifici per Crypto

```bash
mvn test -Dtest=CryptoTest
```

## Performance

### Considerazioni
- La cifratura RSA ha overhead computazionale
- Ogni messaggio richiede operazioni di cifratura/decifratura
- Le chiavi vengono caricate da file ad ogni operazione

### Ottimizzazioni Possibili
- Cache delle chiavi in memoria
- Utilizzo di cifratura simmetrica per messaggi grandi
- Pool di connessioni per riutilizzare handshake

## Compatibilità

- **Java**: 8+
- **Maven**: 3.x
- **JUnit**: 4.11+
- **Formato chiavi**: PEM standard (compatibile OpenSSL)

## Estensioni Future

1. **Cifratura ibrida**: RSA per scambio chiavi + AES per dati
2. **Certificati X.509**: Supporto PKI completo
3. **Perfect Forward Secrecy**: Chiavi di sessione effimere
4. **Compressione**: Riduzione overhead dati
5. **Logging sicuro**: Log senza esporre dati sensibili