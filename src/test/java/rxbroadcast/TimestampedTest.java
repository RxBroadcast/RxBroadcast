package rxbroadcast;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public final class TimestampedTest {
    @Test
    public final void equalsContract() {
        EqualsVerifier.forClass(Timestamped.class).verify();
    }
}
