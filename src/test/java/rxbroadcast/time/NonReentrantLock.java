package rxbroadcast.time;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

final class NonReentrantLock implements Lock {
    private final Semaphore semaphore = new Semaphore(1);

    @Override
    public final void lock() {
        semaphore.acquireUninterruptibly();
    }

    @Override
    public final void lockInterruptibly() throws InterruptedException {
        semaphore.acquire();
    }

    @Override
    public final boolean tryLock() {
        return semaphore.tryAcquire();
    }

    @Override
    public final boolean tryLock(final long time, @NotNull final TimeUnit unit) throws InterruptedException {
        return semaphore.tryAcquire(time, unit);
    }

    @Override
    public final void unlock() {
        semaphore.release();
    }

    @NotNull
    @Override
    public final Condition newCondition() {
        throw new UnsupportedOperationException("A NonReentrantLock cannot bind a Condition");
    }
}
