package More_Secure_Processor;

public interface UsedNonceStore {
    boolean markUsed(String nonce, long usedAtMillis);

    void removeEntriesOlderThan(long cutoffMillis);
}
