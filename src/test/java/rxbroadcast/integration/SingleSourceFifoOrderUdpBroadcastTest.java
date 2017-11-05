package rxbroadcast.integration;

import rxbroadcast.Broadcast;
import rxbroadcast.SingleSourceFifoOrder;
import rxbroadcast.TestValue;
import rxbroadcast.UdpBroadcast;

import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:AvoidInlineConditionals"})
public final class SingleSourceFifoOrderUdpBroadcastTest {
    private static final int MESSAGE_COUNT = 100;

    private static final long TIMEOUT = 30;

    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    private static final Observable<TestValue> MESSAGES = Observable.range(1, MESSAGE_COUNT).map(TestValue::new);

    @Test
    public final void receive() throws SocketException, UnknownHostException {
        final int port = System.getProperty("port") != null
            ? Integer.parseInt(System.getProperty("port"))
            : 54321;
        final int destinationPort = System.getProperty("destinationPort") != null
            ? Integer.parseInt(System.getProperty("destinationPort"))
            : 12345;
        final InetAddress destination = System.getProperty("destination") != null
            ? InetAddress.getByName(System.getProperty("destination"))
            : InetAddress.getLoopbackAddress();
        final InetSocketAddress destinationSocket = new InetSocketAddress(destination, destinationPort);
        final TestSubscriber<TestValue> subscriber = new TestSubscriber<>();
        try (final DatagramSocket socket = new DatagramSocket(port)) {
            final Broadcast broadcast = new UdpBroadcast<>(
                socket, destinationSocket, new SingleSourceFifoOrder<>());

            broadcast.valuesOfType(TestValue.class).take(MESSAGE_COUNT).subscribe(subscriber);

            subscriber.awaitTerminalEventAndUnsubscribeOnTimeout(TIMEOUT, TIMEOUT_UNIT);
            subscriber.assertNoErrors();
            subscriber.assertValueCount(MESSAGE_COUNT);
            subscriber.assertReceivedOnNext(MESSAGES.toList().toBlocking().single());
        }
    }

    public static void main(final String[] args) throws SocketException, UnknownHostException {
        final int port = System.getProperty("port") != null
            ? Integer.parseInt(System.getProperty("port"))
            : 54321;
        final int destinationPort = System.getProperty("destinationPort") != null
            ? Integer.parseInt(System.getProperty("destinationPort"))
            : 12345;
        final InetAddress destination = System.getProperty("destination") != null
            ? InetAddress.getByName(System.getProperty("destination"))
            : InetAddress.getLoopbackAddress();
        final InetSocketAddress destinationSocket = new InetSocketAddress(destination, destinationPort);
        try (final DatagramSocket socket = new DatagramSocket(port)) {
            final Broadcast broadcast = new UdpBroadcast<>(
                socket, destinationSocket, new SingleSourceFifoOrder<>());

            MESSAGES.flatMap(broadcast::send).toBlocking().subscribe(null, System.err::println);
        }
    }
}
