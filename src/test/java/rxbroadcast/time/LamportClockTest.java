package rxbroadcast.time;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.locks.Lock;

@SuppressWarnings({"checkstyle:MagicNumber"})
public final class LamportClockTest {
    @Test
    public final void clockDoesStartAtZero() {
        final LamportClock clock = new LamportClock();
        Assert.assertEquals(0, clock.time());
    }

    @Test
    public final void setTimestampDoesAdvanceTimestampCorrectly() {
        final LamportClock clock = new LamportClock();

        clock.set(5);
        Assert.assertEquals(5, clock.time());

        clock.tick();
        Assert.assertEquals(6, clock.time());
    }

    @Test
    public final void setTimestampDoesNotUpdateTimestampIfNewValueIsLess() {
        final LamportClock clock = new LamportClock();

        clock.tick();
        clock.tick();
        clock.tick();
        clock.tick();
        clock.tick();
        Assert.assertEquals(5, clock.time());

        clock.set(3);
        Assert.assertEquals(5, clock.time());

        clock.tick();
        Assert.assertEquals(6, clock.time());
    }

    @Test
    public final void tickClockDoesIncrementItsValue() {
        final LamportClock clock = new LamportClock();

        clock.tick();

        Assert.assertEquals(1, clock.time());
    }

    @Test
    public final void tickClockDoesPassIncrementedValueToTickerFunction() {
        final LamportClock clock = new LamportClock();

        clock.tick((time) -> {
            Assert.assertEquals(1, time);
            return null;
        });
    }

    @Test
    public final void toStringDoesNotReturnNull() {
        final LamportClock clock = new LamportClock();
        Assert.assertThat(clock.toString(), CoreMatchers.notNullValue());
    }

    @Test
    public final void equalsContract() {
        EqualsVerifier.forClass(LamportClock.class)
            .withIgnoredFields("lock")
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    @Test
    public final void timeDoesReleaseItsLock() {
        final Lock lock = new NonReentrantLock();
        final LamportClock clock = new LamportClock(lock);

        Assert.assertThat(lock, LockMatchers.isUnlocked());
        clock.time();
        Assert.assertThat(lock, LockMatchers.isUnlocked());
    }

    @Test
    public final void setDoesReleaseItsLock() {
        final Lock lock = new NonReentrantLock();
        final LamportClock clock = new LamportClock(lock);

        Assert.assertThat(lock, LockMatchers.isUnlocked());
        clock.set(1);
        Assert.assertThat(lock, LockMatchers.isUnlocked());
    }

    @Test
    public final void tickDoesReleaseItsLock() {
        final Lock lock = new NonReentrantLock();
        final LamportClock clock = new LamportClock(lock);

        Assert.assertThat(lock, LockMatchers.isUnlocked());
        clock.tick();
        Assert.assertThat(lock, LockMatchers.isUnlocked());
        clock.tick((time) -> null);
        Assert.assertThat(lock, LockMatchers.isUnlocked());
    }
}
