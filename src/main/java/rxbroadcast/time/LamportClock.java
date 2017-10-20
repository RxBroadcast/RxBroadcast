package rxbroadcast.time;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongFunction;

/**
 * A clock used to generate <a href="https://en.wikipedia.org/wiki/Lamport_timestamps">Lamport timestamps</a>.
 */
public final class LamportClock implements Clock {
    private final Lock lock;

    private long time = 0L;

    LamportClock(final Lock lock) {
        this.lock = lock;
    }

    /**
     * Creates an instance of {@code LamportClock}.
     */
    public LamportClock() {
        this(new ReentrantLock());
    }

    /**
     * Returns the current clock timestamp.
     * @return the current clock timestamp.
     */
    public long time() {
        lock.lock();
        try {
            return time;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Updates this clock to account for the given incoming timestamp.
     *
     * If the given timestamp is larger than the current clock's time,
     * the clock will set its timestamp to the given value, and the
     * next timestamp generated by the instance will be {@code incoming + 1}.
     * @param incoming the incoming timestamp
     */
    public void set(final long incoming) {
        lock.lock();
        try {
            if (Long.compareUnsigned(time, incoming) < 0) {
                time = incoming;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Executes a single tick of the clock, generating a timestamp.
     */
    public void tick() {
        lock.lock();
        try {
            time = time  + 1;
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T tick(final LongFunction<T> ticker) {
        lock.lock();
        try {
            time = time + 1;
            return ticker.apply(time);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public final String toString() {
        return String.format("LamportClock{time=%d}", time);
    }

    @Override
    public final boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final LamportClock that = (LamportClock) o;
        return time == that.time;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(time);
    }
}
