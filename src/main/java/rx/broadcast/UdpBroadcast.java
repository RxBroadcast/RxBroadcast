package rx.broadcast;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class UdpBroadcast<A> implements Broadcast {
    private static final int MAX_UDP_PACKET_SIZE = 65535;

    private static final int BYTES_LONG = 8;

    private final DatagramSocket socket;

    private final Observable<Object> values;

    private final ConcurrentHashMap<Class, Observable> streams;

    private final KryoSerializer serializer;

    private final InetAddress destinationAddress;

    private final int destinationPort;

    private final BroadcastOrder<A, Object> order;

    @SuppressWarnings("RedundantTypeArguments")
    public UdpBroadcast(
        final DatagramSocket socket,
        final InetAddress destinationAddress,
        final int destinationPort,
        final BroadcastOrder<A, Object> order
    ) {
        this.socket = socket;
        this.order = order;
        this.values = Observable.<Object>create(this::receive).subscribeOn(Schedulers.io()).share();
        this.serializer = new KryoSerializer();
        this.streams = new ConcurrentHashMap<>();
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
    }

    @Override
    public Observable<Void> send(final Object value) {
        return Observable.defer(() -> {
            try {
                final byte[] data = serializer.serialize(order.prepare(value));
                final DatagramPacket packet = new DatagramPacket(
                    data, data.length, destinationAddress, destinationPort);
                socket.send(packet);
                return Observable.empty();
            } catch (final Throwable e) {
                return Observable.error(e);
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Observable<T> valuesOfType(final Class<T> clazz) {
        return (Observable<T>) streams.computeIfAbsent(clazz, k -> values.filter(k::isInstance).cast(k).share());
    }

    @SuppressWarnings("unchecked")
    private void receive(final Subscriber<Object> subscriber) {
        final Consumer<Object> consumer = subscriber::onNext;
        while (true) {
            if (subscriber.isUnsubscribed()) {
                break;
            }

            final byte[] buffer = new byte[MAX_UDP_PACKET_SIZE];
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (final IOException e) {
                subscriber.onError(e);
                break;
            }

            final InetAddress address = packet.getAddress();
            final int port = packet.getPort();
            final long sender = ByteBuffer.allocate(BYTES_LONG).put(address.getAddress()).putInt(port).getLong(0);
            final byte[] data = Arrays.copyOf(buffer, packet.getLength());
            order.receive(sender, consumer, (A) serializer.deserialize(data));
        }
    }
}
