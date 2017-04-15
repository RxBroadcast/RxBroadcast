package rx.broadcast;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

class DaemonThreadFactory implements ThreadFactory {
    private final AtomicLong id = new AtomicLong();

    @Override
    public final Thread newThread(final Runnable r) {
        final Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("RxBroadcast-" + id.incrementAndGet());
        return t;
    }
}
