package rxbroadcast;

import org.jetbrains.annotations.NotNull;
import rx.Observable;
import rx.Scheduler;
import rx.Single;
import rx.schedulers.Schedulers;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Function;

public final class UdpBroadcast<T> implements Broadcast {
    private final DatagramSocket socket;

    private final Observable<Object> values;

    private final ConcurrentHashMap<Class<?>, Observable<?>> streams;

    private final Serializer<T> serializer;

    private final InetSocketAddress destination;

    private final Single<BroadcastOrder<T, Object>> broadcastOrder;

    @SuppressWarnings("WeakerAccess")
    public UdpBroadcast(
        final DatagramSocket socket,
        final InetSocketAddress destination,
        final Serializer<T> serializer,
        final Function<Sender, BroadcastOrder<T, Object>> createBroadcastOrder
    ) {
        this.socket = socket;
        final Scheduler singleThreadScheduler = Schedulers.from(
            Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory()));
        final Single<Sender> host = Single.fromCallable(new WhoAmI(
            socket.getLocalPort(), destination.getAddress() instanceof Inet6Address));
        this.broadcastOrder = host.subscribeOn(Schedulers.io()).map(createBroadcastOrder::apply).cache();
        this.broadcastOrder.subscribe();
        this.values = broadcastOrder.flatMapObservable((order1) ->
            Observable.unsafeCreate(new UdpBroadcastOnSubscribe<>(socket, serializer, order1))
                .subscribeOn(singleThreadScheduler))
            .share();
        this.serializer = serializer;
        this.streams = new ConcurrentHashMap<>();
        this.destination = destination;
    }

    public UdpBroadcast(
        final DatagramSocket socket,
        final InetSocketAddress destination,
        final Serializer<T> serializer,
        final BroadcastOrder<T, Object> order
    ) {
        this(socket, destination, serializer, (host) -> order);
    }

    @SuppressWarnings("unchecked")
    public UdpBroadcast(
        final DatagramSocket socket,
        final InetSocketAddress destination,
        final BroadcastOrder<T, Object> order
    ) {
        this(socket, destination, new KryoSerializer<>(), (host) -> order);
    }

    @SuppressWarnings("unchecked")
    public UdpBroadcast(
        final DatagramSocket socket,
        final InetSocketAddress destination,
        final Function<Sender, BroadcastOrder<T, Object>> createBroadcastOrder
    ) {
        this(socket, destination, new KryoSerializer<>(), createBroadcastOrder);
    }

    @Override
    public Observable<Void> send(@NotNull final Object value) {
        return broadcastOrder.flatMapObservable((order) -> {
            try {
                final byte[] data = serializer.encode(order.prepare(value));
                final DatagramPacket packet = new DatagramPacket(
                    data, data.length, destination.getAddress(), destination.getPort());
                socket.send(packet);
                return null;
            } catch (final Throwable e) {
                return Observable.error(e);
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> Observable<@NotNull V> valuesOfType(@NotNull final Class<V> clazz) {
        return (Observable<V>) streams.computeIfAbsent(clazz, k -> values.ofType(k).share());
    }
}
