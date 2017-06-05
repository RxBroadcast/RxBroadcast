package rx.broadcast;

import org.junit.After;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
        final DatagramSocket s1 = datagramSocketSupplier.get();
        final DatagramSocket s2 = datagramSocketSupplier.get();
        final Broadcast broadcast1 = new UdpBroadcast<>(
            s1, InetAddress.getLoopbackAddress(), s2.getLocalPort(), new BasicOrder<>());
        final Broadcast broadcast2 = new UdpBroadcast<>(
            s2, InetAddress.getLoopbackAddress(), s1.getLocalPort(), new BasicOrder<>());

        broadcast2.valuesOfType(TestValue.class).first().subscribe(subscriber);
        broadcast1.send(new TestValue(42)).toBlocking().subscribe();

        subscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        subscriber.assertReceivedOnNext(Collections.singletonList(new TestValue(42)));
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void eachSubscriptionDoesSendTheBroadcast() {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        final DatagramSocket s1 = datagramSocketSupplier.get();
        final DatagramSocket s2 = datagramSocketSupplier.get();
        final Broadcast broadcast1 = new UdpBroadcast<>(
            s1, InetAddress.getLoopbackAddress(), s2.getLocalPort(), new BasicOrder<>());
        final Broadcast broadcast2 = new UdpBroadcast<>(
            s2, InetAddress.getLoopbackAddress(), s1.getLocalPort(), new BasicOrder<>());

        broadcast2.valuesOfType(TestValue.class).take(4).subscribe(subscriber);

        final Observable<Void> testValue = broadcast1.send(new TestValue(42));
        testValue.toBlocking().subscribe();
        testValue.toBlocking().subscribe();
        testValue.toBlocking().subscribe();
        testValue.toBlocking().subscribe();

        subscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(4);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new TestValue(42), new TestValue(42), new TestValue(42), new TestValue(42)));
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void errorSendingBroadcastIsReceivedInOnError() {
        final TestSubscriber<Void> subscriber = new TestSubscriber<>();
        final DatagramSocket s1 = datagramSocketSupplier.get();
        final DatagramSocket s2 = datagramSocketSupplier.get();
        final Broadcast broadcast1 = new UdpBroadcast<>(
            s1, InetAddress.getLoopbackAddress(), s2.getLocalPort(), new BasicOrder<>());

        s1.close();
        broadcast1.send(new TestValue(42)).toBlocking().subscribe(subscriber);

        subscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        subscriber.assertError(SocketException.class);
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void deserializationErrorDoesNotTerminateStream() throws IOException {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        final DatagramSocket s1 = datagramSocketSupplier.get();
        final DatagramSocket s2 = datagramSocketSupplier.get();
        final Broadcast broadcast1 = new UdpBroadcast<>(
            s1, InetAddress.getLoopbackAddress(), s2.getLocalPort(), new BasicOrder<>());
        final Broadcast broadcast2 = new UdpBroadcast<>(
            s2, InetAddress.getLoopbackAddress(), s1.getLocalPort(), new BasicOrder<>());

        broadcast2.valuesOfType(TestValue.class).take(4).subscribe(subscriber);

        final Observable<Void> testValue = broadcast1.send(new TestValue(42));
        s1.send(new DatagramPacket(new byte[]{42, 43, 44, 45}, 4, InetAddress.getLoopbackAddress(), s2.getLocalPort()));
        testValue.toBlocking().subscribe();
        testValue.toBlocking().subscribe();
        testValue.toBlocking().subscribe();
        testValue.toBlocking().subscribe();

        subscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        subscriber.assertNoErrors();
        subscriber.assertValueCount(4);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new TestValue(42), new TestValue(42), new TestValue(42), new TestValue(42)));
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void twoWayBasicOrderMessages() {
        final TestSubscriber<Object> subscriber1 = new TestSubscriber<>();
        final TestSubscriber<Object> subscriber2 = new TestSubscriber<>();
        final DatagramSocket s1 = datagramSocketSupplier.get();
        final DatagramSocket s2 = datagramSocketSupplier.get();
        final Broadcast broadcast1 = new UdpBroadcast<>(
            s1, InetAddress.getLoopbackAddress(), s2.getLocalPort(), new BasicOrder<>());
        final Broadcast broadcast2 = new UdpBroadcast<>(
            s2, InetAddress.getLoopbackAddress(), s1.getLocalPort(), new BasicOrder<>());

        broadcast1.valuesOfType(TestValue.class).first().subscribe(subscriber1);
        broadcast2.valuesOfType(TestValue.class).first().subscribe(subscriber2);

        broadcast1.send(new TestValue(42)).toBlocking().subscribe();
        broadcast2.send(new TestValue(41)).toBlocking().subscribe();

        subscriber1.awaitTerminalEvent(10, TimeUnit.SECONDS);
        subscriber2.awaitTerminalEvent(10, TimeUnit.SECONDS);

        subscriber1.assertNoErrors();
        subscriber1.assertValueCount(1);
        subscriber1.assertReceivedOnNext(Collections.singletonList(new TestValue(41)));

        subscriber2.assertNoErrors();
        subscriber2.assertValueCount(1);
        subscriber2.assertReceivedOnNext(Collections.singletonList(new TestValue(42)));
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void twoWaySingleSourceFifoOrderMessages() {
        final TestSubscriber<Object> subscriber1 = new TestSubscriber<>();
        final TestSubscriber<Object> subscriber2 = new TestSubscriber<>();
        final DatagramSocket s1 = datagramSocketSupplier.get();
        final DatagramSocket s2 = datagramSocketSupplier.get();
        final Broadcast broadcast1 = new UdpBroadcast<>(
            s1, InetAddress.getLoopbackAddress(), s2.getLocalPort(), new SingleSourceFifoOrder<>());
        final Broadcast broadcast2 = new UdpBroadcast<>(
            s2, InetAddress.getLoopbackAddress(), s1.getLocalPort(), new SingleSourceFifoOrder<>());

        broadcast1.valuesOfType(TestValue.class).first().subscribe(subscriber1);
        broadcast2.valuesOfType(TestValue.class).first().subscribe(subscriber2);

        broadcast1.send(new TestValue(42)).toBlocking().subscribe();
        broadcast2.send(new TestValue(41)).toBlocking().subscribe();

        subscriber1.awaitTerminalEvent(10, TimeUnit.SECONDS);
        subscriber2.awaitTerminalEvent(10, TimeUnit.SECONDS);

        subscriber1.assertNoErrors();
        subscriber1.assertValueCount(1);
        subscriber1.assertReceivedOnNext(Collections.singletonList(new TestValue(41)));

        subscriber2.assertNoErrors();
        subscriber2.assertValueCount(1);
        subscriber2.assertReceivedOnNext(Collections.singletonList(new TestValue(42)));
    }
}
