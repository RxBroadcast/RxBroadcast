package rxbroadcast;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public final class VectorTimestampedTest {
    @Test
    public final void equalsContract() {
        EqualsVerifier.forClass(VectorTimestamped.class).verify();
    }
}
