package rx.broadcast;

import rx.Observable;
import rx.Subscriber;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.function.Consumer;

final class UdpBroadcastOnSubscribe<T> implements Observable.OnSubscribe<Object> {
    private static final int MAX_UDP_PACKET_SIZE = 65535;

    private final DatagramSocket readSocket;

    private final Serializer<T> serializer;

    private final BroadcastOrder<T, Object> broadcastOrder;

    UdpBroadcastOnSubscribe(
        final DatagramSocket readSocket,
        final Serializer<T> serializer,
        final BroadcastOrder<T, Object> broadcastOrder
    ) {
        this.readSocket = readSocket;
        this.serializer = serializer;
        this.broadcastOrder = broadcastOrder;
    }

    @Override
    public void call(final Subscriber<? super Object> subscriber) {
        final Consumer<Object> consumer = subscriber::onNext;
        while (true) {
            if (subscriber.isUnsubscribed()) {
                break;
            }

            final byte[] buffer = new byte[MAX_UDP_PACKET_SIZE];
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                readSocket.receive(packet);
            } catch (final IOException e) {
                subscriber.onError(e);
                break;
            }

            final Sender sender = new Sender(packet.getAddress(), packet.getPort());
            final byte[] data = Arrays.copyOf(buffer, packet.getLength());
            try {
                final T object = serializer.decode(data);
                broadcastOrder.receive(sender, consumer, object);
            } catch (final RuntimeException e) {
                /* This is bad and I feel bad about it. See issue #47 for plans to fix this. */
            }
        }
    }
}
