package rx.broadcast;

import org.jetbrains.annotations.NotNull;
import rx.Observable;
import rx.Scheduler;
import rx.Single;
import rx.schedulers.Schedulers;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Function;

public final class UdpBroadcast<A> implements Broadcast {
    private final DatagramSocket socket;

    private final Observable<Object> values;

    private final ConcurrentHashMap<Class<?>, Observable<?>> streams;

    private final Serializer<A> serializer;

    private final InetAddress destinationAddress;

    private final int destinationPort;

    private final Single<BroadcastOrder<A, Object>> broadcastOrder;

    @SuppressWarnings("WeakerAccess")
    public UdpBroadcast(
        final DatagramSocket socket,
        final InetAddress destinationAddress,
        final int destinationPort,
        final Serializer<A> serializer,
        final Function<Sender, BroadcastOrder<A, Object>> createBroadcastOrder
    ) {
        this.socket = socket;
        final Scheduler singleThreadScheduler = Schedulers.from(
            Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory()));
        final Single<Sender> host = Single.fromCallable(new WhoAmI(destinationPort));
        this.broadcastOrder = host.subscribeOn(Schedulers.io()).map(createBroadcastOrder::apply).cache();
        this.broadcastOrder.subscribe();
        this.values = broadcastOrder.flatMapObservable((order1) ->
            Observable.unsafeCreate(new UdpBroadcastOnSubscribe<>(socket, serializer, order1))
                .subscribeOn(singleThreadScheduler))
            .share();
        this.serializer = serializer;
        this.streams = new ConcurrentHashMap<>();
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
    }

    public UdpBroadcast(
        final DatagramSocket socket,
        final InetAddress destinationAddress,
        final int destinationPort,
        final Serializer<A> serializer,
        final BroadcastOrder<A, Object> order
    ) {
        this(socket, destinationAddress, destinationPort, serializer, (host) -> order);
    }

    @SuppressWarnings("unchecked")
    public UdpBroadcast(
        final DatagramSocket socket,
        final InetAddress destinationAddress,
        final int destinationPort,
        final BroadcastOrder<A, Object> order
    ) {
        this(socket, destinationAddress, destinationPort, new KryoSerializer<>(), (host) -> order);
    }

    @SuppressWarnings("unchecked")
    public UdpBroadcast(
        final DatagramSocket socket,
        final InetAddress destinationAddress,
        final int destinationPort,
        final Function<Sender, BroadcastOrder<A, Object>> createBroadcastOrder
    ) {
        this(socket, destinationAddress, destinationPort, new KryoSerializer<>(), createBroadcastOrder);
    }

    @Override
    public Observable<Void> send(@NotNull final Object value) {
        return broadcastOrder.flatMapObservable((order) -> {
            try {
                final byte[] data = serializer.encode(order.prepare(value));
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
    public <T> Observable<@NotNull T> valuesOfType(@NotNull final Class<T> clazz) {
        return (Observable<T>) streams.computeIfAbsent(clazz, k -> values.ofType(k).share());
    }
}
