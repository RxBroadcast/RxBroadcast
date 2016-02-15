package rx.broadcast;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public final class UdpBroadcast implements Broadcast {
    private final DatagramSocket socket;

    private final Observable<Object> values;

    private final ConcurrentHashMap<Class, Observable> streams;

    private final KryoSerializer serializer;

    private final InetAddress destinationAddress;

    private final int destinationPort;

    @SuppressWarnings("RedundantTypeArguments")
    public UdpBroadcast(final DatagramSocket socket, final InetAddress destinationAddress, final int destinationPort) {
        this.socket = socket;
        this.values = Observable.<Object>create(this::receive).subscribeOn(Schedulers.io()).share();
        this.serializer = new KryoSerializer();
        this.streams = new ConcurrentHashMap<>();
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
    }

    @Override
    public void await() throws InterruptedException {

    }

    @Override
    public void send(final Object value) {
        final byte[] data = serializer.serialize(value);
        final DatagramPacket packet = new DatagramPacket(data, data.length, destinationAddress, destinationPort);
        try {
            socket.send(packet);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Observable<T> valuesOfType(final Class<T> clazz) {
        streams.computeIfAbsent(clazz, k -> values.filter(k::isInstance).cast(k).share());
        return (Observable<T>) streams.get(clazz);
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     *
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     * <p/>
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     * <p/>
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     * <p/>
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p/>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p/>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     * <p/>
     * <p>Note that unlike the {@link java.io.Closeable#close close}
     * method of {@link java.io.Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p/>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {

    }

    private void receive(final Subscriber<Object> subscriber) {
        while (true) {
            if (subscriber.isUnsubscribed()) {
                break;
            }

            final byte[] buffer = new byte[65535];
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (final IOException e) {
                subscriber.onError(e);
                break;
            }

            final byte[] data = Arrays.copyOf(buffer, packet.getLength());
            subscriber.onNext(serializer.deserialize(data));
        }
    }
}
