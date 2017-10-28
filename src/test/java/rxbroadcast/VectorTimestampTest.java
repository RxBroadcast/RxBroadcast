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
                new Sender[]{new Sender(new byte[] {42}), new Sender(new byte[]{43})}, new long[]{1, 2}
            ))
            .verify();
    }

    @Test(expected = IllegalArgumentException.class)
    public final void timestampMustHaveEqualLengthFields() {
        final VectorTimestamp t = new VectorTimestamp(new Sender[0], new long[]{3, 4});
        Assert.assertThat(t, CoreMatchers.notNullValue());
    }
}
