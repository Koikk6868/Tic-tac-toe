package More_Secure_Processor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InMemoryUsedNonceStore implements UsedNonceStore {
    private final Map<String, Long> usedNonces = new HashMap<>();

    @Override
    public boolean markUsed(String nonce, long usedAtMillis) {
        if (usedNonces.containsKey(nonce)) {
            return false;
        }
        usedNonces.put(nonce, usedAtMillis);
        return true;
    }

    @Override
    public void removeEntriesOlderThan(long cutoffMillis) {
        Iterator<Map.Entry<String, Long>> iterator = usedNonces.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() < cutoffMillis) {
                iterator.remove();
            }
        }
    }
}
