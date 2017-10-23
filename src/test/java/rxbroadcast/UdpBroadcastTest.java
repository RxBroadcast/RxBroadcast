package rxbroadcast;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public final class UdpBroadcastTest {
    @Rule
    public final Timeout timeout = Timeout.seconds(60);

    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void valuesOfTypeDoesReceiveBroadcastValue() throws SocketException {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        try (
            final DatagramSocket s1 = new DatagramSocket();
            final DatagramSocket s2 = new DatagramSocket();
        ) {
            final Broadcast broadcast1 = new UdpBroadcast<>(
                s1, new InetSocketAddress(InetAddress.getLoopbackAddress(), s2.getLocalPort()), new NoOrder<>());
            final Broadcast broadcast2 = new UdpBroadcast<>(
                s2, new InetSocketAddress(InetAddress.getLoopbackAddress(), s1.getLocalPort()), new NoOrder<>());

            broadcast2.valuesOfType(TestValue.class).first().subscribe(subscriber);
            broadcast1.send(new TestValue(42)).toBlocking().subscribe();

            subscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);

            subscriber.assertNoErrors();
            subscriber.assertValueCount(1);
            subscriber.assertReceivedOnNext(Collections.singletonList(new TestValue(42)));
        }
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void eachSubscriptionDoesSendTheBroadcast() throws SocketException {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        try (
            final DatagramSocket s1 = new DatagramSocket();
            final DatagramSocket s2 = new DatagramSocket();
        ) {
            final Broadcast broadcast1 = new UdpBroadcast<>(
                s1, new InetSocketAddress(InetAddress.getLoopbackAddress(), s2.getLocalPort()), new NoOrder<>());
            final Broadcast broadcast2 = new UdpBroadcast<>(
                s2, new InetSocketAddress(InetAddress.getLoopbackAddress(), s1.getLocalPort()), new NoOrder<>());

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
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void errorSendingBroadcastIsReceivedInOnError() throws SocketException {
        final TestSubscriber<Void> subscriber = new TestSubscriber<>();
        final DatagramSocket s1 = new DatagramSocket();
        try (final DatagramSocket s2 = new DatagramSocket()) {
            final Broadcast broadcast1 = new UdpBroadcast<>(
                s1, new InetSocketAddress(InetAddress.getLoopbackAddress(), s2.getLocalPort()), new NoOrder<>());

            s1.close();
            broadcast1.send(new TestValue(42)).toBlocking().subscribe(subscriber);

            subscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
            subscriber.assertError(SocketException.class);
        }
    }

    @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:LineLength"})
    @Test
    public final void deserializationErrorDoesTerminateStream() throws IOException {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        try (
            final DatagramSocket s1 = new DatagramSocket();
            final DatagramSocket s2 = new DatagramSocket();
        ) {
            final Broadcast broadcast1 = new UdpBroadcast<>(
                s1, new InetSocketAddress(InetAddress.getLoopbackAddress(), s2.getLocalPort()), new NoOrder<>());
            final Broadcast broadcast2 = new UdpBroadcast<>(
                s2, new InetSocketAddress(InetAddress.getLoopbackAddress(), s1.getLocalPort()), new NoOrder<>());

            broadcast2.valuesOfType(TestValue.class).take(4).subscribe(subscriber);

            final Observable<Void> testValue = broadcast1.send(new TestValue(42));
            s1.send(new DatagramPacket(new byte[]{42, 43, 44, 45}, 4, InetAddress.getLoopbackAddress(), s2.getLocalPort()));
            testValue.toBlocking().subscribe();
            testValue.toBlocking().subscribe();
            testValue.toBlocking().subscribe();
            testValue.toBlocking().subscribe();

            subscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
            subscriber.assertValueCount(0);
            subscriber.assertError(RuntimeException.class);
        }
    }

    @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:LineLength"})
    @Test
    public final void deserializationErrorStreamCanBeRestarted() throws IOException {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        try (
            final DatagramSocket s1 = new DatagramSocket();
            final DatagramSocket s2 = new DatagramSocket();
        ) {
            final Broadcast broadcast1 = new UdpBroadcast<>(
                s1, new InetSocketAddress(InetAddress.getLoopbackAddress(), s2.getLocalPort()), new NoOrder<>());
            final Broadcast broadcast2 = new UdpBroadcast<>(
                s2, new InetSocketAddress(InetAddress.getLoopbackAddress(), s1.getLocalPort()), new NoOrder<>());

            broadcast2.valuesOfType(TestValue.class)
                .take(4)
                .retry()
                .subscribe(subscriber);

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
    }

    @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:LineLength"})
    @Test
    public final void broadcastOrderDoesGetNonNullHostMachineAddress() throws SocketException {
        try (
            final DatagramSocket s1 = new DatagramSocket();
            final DatagramSocket s2 = new DatagramSocket();
        ) {
            final InetSocketAddress destination = new InetSocketAddress(
                InetAddress.getLoopbackAddress(), s2.getLocalPort());
            final Broadcast broadcast1 = new UdpBroadcast<>(s1, destination, (host) -> {
                Assert.assertThat(host, CoreMatchers.notNullValue());
                return new NoOrder<>();
            });
            final TestSubscriber<TestValue> subscriber = new TestSubscriber<>();

            broadcast1.valuesOfType(TestValue.class).subscribe(subscriber);

            subscriber.awaitTerminalEvent(100, TimeUnit.MILLISECONDS);
            subscriber.assertNoValues();
            subscriber.assertNoErrors();
            subscriber.assertNotCompleted();
        }
    }

    @SuppressWarnings({"checkstyle:MagicNumber"})
    @Test
    public final void sendDoesCompleteSuccessfully() throws SocketException {
        final TestSubscriber<Void> subscriber = new TestSubscriber<>();
        try (
            final DatagramSocket s1 = new DatagramSocket();
            final DatagramSocket s2 = new DatagramSocket();
        ) {
            final Broadcast broadcast1 = new UdpBroadcast<>(
                s1, new InetSocketAddress(InetAddress.getLoopbackAddress(), s2.getLocalPort()), new NoOrder<>());

            broadcast1.send(new TestValue(42)).subscribe(subscriber);

            subscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
            subscriber.assertNoErrors();
            subscriber.assertNoValues();
            subscriber.assertCompleted();
        }
    }
}
