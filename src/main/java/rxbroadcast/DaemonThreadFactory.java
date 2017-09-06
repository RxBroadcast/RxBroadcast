package rxbroadcast;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

final class DaemonThreadFactory implements ThreadFactory {
    private final AtomicLong id = new AtomicLong();

    @NotNull
    @Override
    public final Thread newThread(@NotNull final Runnable r) {
        final Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("RxBroadcast-" + id.incrementAndGet());
        return t;
    }
}
