package rx.broadcast;

import org.junit.After;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class UdpBroadcastTest {
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

    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void valuesOfTypeDoesReceiveBroadcastValue() {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        final DatagramSocket sa = datagramSocketSupplier.get();
        final DatagramSocket sb = datagramSocketSupplier.get();
        final Broadcast broadcast1 = new UdpBroadcast(sa, InetAddress.getLoopbackAddress(), sb.getLocalPort());
        final Broadcast broadcast2 = new UdpBroadcast(sb, InetAddress.getLoopbackAddress(), sa.getLocalPort());

        broadcast2.valuesOfType(TestValue.class).first().subscribe(subscriber);
        broadcast1.send(new TestValue(42)).toBlocking().subscribe();

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        subscriber.assertReceivedOnNext(Collections.singletonList(new TestValue(42)));
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void eachSubscriptionDoesSendTheBroadcast() {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        final DatagramSocket sa = datagramSocketSupplier.get();
        final DatagramSocket sb = datagramSocketSupplier.get();
        final Broadcast broadcast1 = new UdpBroadcast(sa, InetAddress.getLoopbackAddress(), sb.getLocalPort());
        final Broadcast broadcast2 = new UdpBroadcast(sb, InetAddress.getLoopbackAddress(), sa.getLocalPort());

        broadcast2.valuesOfType(TestValue.class).take(4).subscribe(subscriber);

        final Observable<Void> testValue = broadcast1.send(new TestValue(42));
        testValue.toBlocking().subscribe();
        testValue.toBlocking().subscribe();
        testValue.toBlocking().subscribe();
        testValue.toBlocking().subscribe();

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(4);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new TestValue(42), new TestValue(42), new TestValue(42), new TestValue(42)));
    }
}
