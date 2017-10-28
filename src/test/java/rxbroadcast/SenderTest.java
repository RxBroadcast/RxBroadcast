package rxbroadcast;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Test;

public final class SenderTest {
    @Test
    public final void equalsContract() {
        EqualsVerifier.forClass(Sender.class).verify();
    }

    @Test
    public final void testCompareToSimple1() {
        final Sender s1 = new Sender(new byte[]{1});
        final Sender s2 = new Sender(new byte[]{2});
        Assert.assertTrue("s1 < s2", s1.compareTo(s2) < 0);
    }

    @Test
    public final void testCompareToSimple2() {
        final Sender s1 = new Sender(new byte[]{2});
        final Sender s2 = new Sender(new byte[]{1});
        Assert.assertTrue("s1 > s2", s1.compareTo(s2) > 0);
    }

    @Test
    public final void testCompareToSimple3() {
        final Sender s1 = new Sender(new byte[]{2});
        final Sender s2 = new Sender(new byte[]{2});
        Assert.assertEquals(0, s1.compareTo(s2));
    }
}
