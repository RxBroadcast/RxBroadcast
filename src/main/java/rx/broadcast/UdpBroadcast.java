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
    private static final int MAX_UDP_PACKET_SIZE = 65535;

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
    public Observable<Void> send(final Object value) {
        return Observable.defer(() -> {
            try {
                final byte[] data = serializer.serialize(value);
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
        streams.computeIfAbsent(clazz, k -> values.filter(k::isInstance).cast(k).share());
        return (Observable<T>) streams.get(clazz);
    }

    private void receive(final Subscriber<Object> subscriber) {
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

            final byte[] data = Arrays.copyOf(buffer, packet.getLength());
            subscriber.onNext(serializer.deserialize(data));
        }
    }
}
