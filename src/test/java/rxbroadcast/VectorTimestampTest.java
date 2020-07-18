package rxbroadcast;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings({"checkstyle:MagicNumber"})
public final class VectorTimestampTest {
    @Test
    public final void equalsContract() {
        EqualsVerifier.forClass(VectorTimestamp.class)
            .withCachedHashCode("hashCode", "computeHashCode", new VectorTimestamp(
                new Sender[]{new Sender(new byte[] {42}), new Sender(new byte[]{43})}, 1, 2))
            .verify();
    }

    @Test(expected = IllegalArgumentException.class)
    public final void timestampMustHaveEqualLengthFields() {
        Assert.assertThat(new VectorTimestamp(new Sender[0], 3, 4), CoreMatchers.anything());
    }
}
