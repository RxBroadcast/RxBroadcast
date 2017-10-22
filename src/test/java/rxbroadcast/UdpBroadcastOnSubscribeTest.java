package rxbroadcast;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import rx.observers.TestSubscriber;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

@SuppressWarnings({"checkstyle:MagicNumber"})
public final class UdpBroadcastOnSubscribeTest {
    @Rule
    public final Timeout timeout = Timeout.seconds(1);

    @Test
    public final void unsubscribeDoesBreakCall() throws SocketException {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        try (final DatagramSocket readSocket = new DatagramSocket()) {
            final UdpBroadcastOnSubscribe<Object> onSubscribe = new UdpBroadcastOnSubscribe<>(
                readSocket, new ObjectSerializer<>(), new NoOrder<>());

            subscriber.unsubscribe();
            onSubscribe.call(subscriber);

            subscriber.assertNoErrors();
            subscriber.assertNoValues();
            subscriber.assertNotCompleted();
        }
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
