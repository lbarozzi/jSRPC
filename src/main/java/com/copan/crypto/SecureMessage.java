package com.copan.crypto;

/**
 * Classe per rappresentare un messaggio sicuro con cifratura e firma
 */
public class SecureMessage {
    private final String encryptedContent;
    private final String signature;
    private final String senderPublicKey;
    private final long timestamp;
    
    public SecureMessage(String encryptedContent, String signature, String senderPublicKey) {
        this.encryptedContent = encryptedContent;
        this.signature = signature;
        this.senderPublicKey = senderPublicKey;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getEncryptedContent() {
        return encryptedContent;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public String getSenderPublicKey() {
        return senderPublicKey;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Serializza il messaggio sicuro in formato JSON semplice
     */
    public String toJson() {
        return String.format("{\"encryptedContent\":\"%s\",\"signature\":\"%s\",\"senderPublicKey\":\"%s\",\"timestamp\":%d}",
                encryptedContent, signature, senderPublicKey, timestamp);
    }
    
    /**
     * Deserializza un messaggio sicuro da formato JSON semplice
     */
    public static SecureMessage fromJson(String json) {
        try {
            // Parser JSON semplice (in produzione usare libreria JSON)
            String encryptedContent = extractJsonValue(json, "encryptedContent");
            String signature = extractJsonValue(json, "signature");
            String senderPublicKey = extractJsonValue(json, "senderPublicKey");
            
            return new SecureMessage(encryptedContent, signature, senderPublicKey);
        } catch (Exception e) {
            throw new RuntimeException("Errore nel parsing del messaggio sicuro", e);
        }
    }
    
    private static String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int startIndex = json.indexOf(pattern) + pattern.length();
        int endIndex = json.indexOf("\"", startIndex);
        return json.substring(startIndex, endIndex);
    }
}