import More_Secure_Processor.InMemoryUsedNonceStore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryUsedNonceStoreTest {
    @Test
    void nonceCanBeUsedOnlyOnceUntilCleaned() {
        InMemoryUsedNonceStore store = new InMemoryUsedNonceStore();

        assertTrue(store.markUsed("nonce", 1000L));
        assertFalse(store.markUsed("nonce", 1001L));

        store.removeEntriesOlderThan(1001L);

        assertTrue(store.markUsed("nonce", 1002L));
    }
}
