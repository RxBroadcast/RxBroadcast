package rxbroadcast.time;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.locks.Lock;
import java.util.function.LongFunction;
import java.util.function.Supplier;

@SuppressWarnings({"checkstyle:MagicNumber"})
public final class LamportClockTest {
    private final LongFunction<Void> noOp = (val) -> null;

    private final Supplier<Void> nullSupplier = () -> null;

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

        clock.tick(noOp);
        Assert.assertEquals(6, clock.time());
    }

    @Test
    public final void tickClockDoesIncrementItsValue() {
        final LamportClock clock = new LamportClock();

        clock.tick(noOp);

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
        clock.tick(noOp);
        Assert.assertThat(lock, LockMatchers.isUnlocked());
        clock.tick(nullSupplier);
        Assert.assertThat(lock, LockMatchers.isUnlocked());
    }

    @Test
    public final void toStringDoesReleaseItsLock() {
        final Lock lock = new NonReentrantLock();
        final LamportClock clock = new LamportClock(lock);

        Assert.assertThat(lock, LockMatchers.isUnlocked());
        Assert.assertThat(clock.toString(), CoreMatchers.anything());
        Assert.assertThat(lock, LockMatchers.isUnlocked());
    }
}
