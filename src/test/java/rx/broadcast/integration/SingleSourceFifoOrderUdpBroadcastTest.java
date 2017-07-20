package rx.broadcast.integration;

import org.junit.Test;
import rx.Observable;
import rx.broadcast.Broadcast;
import rx.broadcast.SingleSourceFifoOrder;
import rx.broadcast.TestValue;
import rx.broadcast.UdpBroadcast;
import rx.observers.TestSubscriber;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public final class SingleSourceFifoOrderUdpBroadcastTest {
    private static final int MESSAGE_COUNT = 100;

    private static final long TIMEOUT = 30;

    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    private static final Observable<TestValue> MESSAGES = Observable.range(1, MESSAGE_COUNT).map(TestValue::new);

    @Test
    public final void receive() throws SocketException, UnknownHostException {
        final int port = Integer.valueOf(System.getProperty("port"));
        try (final DatagramSocket socket = new DatagramSocket(port)) {
            final TestSubscriber<TestValue> subscriber = new TestSubscriber<>();
            final InetAddress destination = InetAddress.getByName(System.getProperty("destination"));
            final Broadcast broadcast = new UdpBroadcast<>(socket, destination, port, new SingleSourceFifoOrder<>());

            broadcast.valuesOfType(TestValue.class).take(MESSAGE_COUNT).subscribe(subscriber);

            subscriber.awaitTerminalEventAndUnsubscribeOnTimeout(TIMEOUT, TIMEOUT_UNIT);
            subscriber.assertNoErrors();
            subscriber.assertValueCount(MESSAGE_COUNT);
            subscriber.assertReceivedOnNext(MESSAGES.toList().toBlocking().single());
        }
    }

    public static void main(final String[] args) throws SocketException, UnknownHostException {
        final int port = Integer.valueOf(System.getProperty("port"));
        try (final DatagramSocket socket = new DatagramSocket(port)) {
            final InetAddress destination = InetAddress.getByName(System.getProperty("destination"));
            final Broadcast broadcast = new UdpBroadcast<>(socket, destination, port, new SingleSourceFifoOrder<>());

            MESSAGES.flatMap(broadcast::send).toBlocking().subscribe(null, System.err::println);
        }
    }
}
