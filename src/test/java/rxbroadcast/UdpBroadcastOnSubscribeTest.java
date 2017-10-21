package rxbroadcast;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import rx.observers.TestSubscriber;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings({"checkstyle:MagicNumber"})
public final class UdpBroadcastOnSubscribeTest {
    private final List<Closeable> resources = new LinkedList<>();

    private final Supplier<DatagramSocket> datagramSocketSupplier = () -> {
        try {
            final DatagramSocket socket = new DatagramSocket();
            resources.add(socket);
            return socket;
        } catch (final SocketException e) {
            throw new RuntimeException(e);
        }
    };

    @Rule
    public final Timeout timeout = Timeout.seconds(1);

    @After
    public final void tearDown() {
        resources.forEach(closeable -> {
            try {
                closeable.close();
            } catch (final IOException e) {
                // ???
            }
        });
    }

    @Test
    public final void unsubscribeDoesBreakCall() throws SocketException {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        final UdpBroadcastOnSubscribe<Object> onSubscribe = new UdpBroadcastOnSubscribe<>(
            datagramSocketSupplier.get(), new ObjectSerializer<>(), new NoOrder<>());

        subscriber.unsubscribe();
        onSubscribe.call(subscriber);

        subscriber.assertNoErrors();
        subscriber.assertNoValues();
        subscriber.assertNotCompleted();
    }

    @Test
    public final void closedReadSocketErrorDoesPropagateToSubscriber() throws SocketException {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        final DatagramSocket socket = new DatagramSocket();
        final UdpBroadcastOnSubscribe<Object> onSubscribe = new UdpBroadcastOnSubscribe<>(
            socket, new ObjectSerializer<>(), new NoOrder<>());

        socket.close();
        onSubscribe.call(subscriber);

        subscriber.assertError(IOException.class);
    }
}
