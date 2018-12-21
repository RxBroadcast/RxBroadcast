package rxbroadcast.time;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongFunction;
import java.util.function.Supplier;

/**
 * A clock used to generate <a href="https://en.wikipedia.org/wiki/Lamport_timestamps">Lamport timestamps</a>.
 */
public final class LamportClock {
    private final Lock lock;

    private long currentTime = 0L;

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
            return currentTime;
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
            currentTime = incoming;
        } finally {
            lock.unlock();
        }
    }

    public <T> T tick(@NotNull final LongFunction<T> ticker) {
        lock.lock();
        try {
            currentTime = currentTime + 1;
            return ticker.apply(currentTime);
        } finally {
            lock.unlock();
        }
    }

    public <T> T tick(@NotNull final Supplier<T> ticker) {
        lock.lock();
        try {
            currentTime = currentTime + 1;
            return ticker.get();
        } finally {
            lock.unlock();
        }
    }

    @NotNull
    @Override
    public final String toString() {
        lock.lock();
        try {
            return String.format("LamportClock{currentTime=%d}", currentTime);
        } finally {
            lock.unlock();
        }
    }
}
