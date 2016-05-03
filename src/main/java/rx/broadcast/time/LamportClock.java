package rx.broadcast.time;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongFunction;

public final class LamportClock implements Clock {
    private final Lock lock;

    private long time = -1L;

    public LamportClock() {
        this.lock = new ReentrantLock();
    }

    public long time() {
        lock.lock();
        try {
            return time;
        } finally {
            lock.unlock();
        }
    }

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

    public void tick() {
        lock.lock();
        try {
            time = time  + 1;
        } finally {
            lock.unlock();
        }
    }

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
}
