package rxbroadcast;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public final class SenderTest {
    @Test
    public final void equalsContract() {
        EqualsVerifier.forClass(Sender.class).verify();
    }
}
