package rx.broadcast.time;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public final class LamportClock implements Clock {
    private final Lock lock;

    private long time = -1L;

    public LamportClock() {
        this(new ReentrantLock());
    }

    public LamportClock(final Lock lock) {
        this.lock = lock;
    }

    @Override
    public <T> T tick(final Function<Long, T> ticker) {
        lock.lock();
        try {
            time = time + 1;
            return ticker.apply(time);
        } finally {
            lock.unlock();
        }
    }
}
