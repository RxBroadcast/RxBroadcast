package rx.broadcast;

import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;

public class UdpBroadcastTest {
    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void valuesOfTypeDoesReceiveBroadcastValue() throws SocketException {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        final DatagramSocket sa = new DatagramSocket();
        final DatagramSocket sb = new DatagramSocket();
        final Broadcast broadcast1 = new UdpBroadcast(sa, InetAddress.getLoopbackAddress(), sb.getLocalPort());
        final Broadcast broadcast2 = new UdpBroadcast(sb, InetAddress.getLoopbackAddress(), sa.getLocalPort());

        broadcast2.valuesOfType(TestValue.class).first().subscribe(subscriber);
        broadcast1.send(new TestValue(42)).toBlocking().subscribe();

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        subscriber.assertReceivedOnNext(Collections.singletonList(new TestValue(42)));

        sa.close();
        sb.close();
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void eachSubscriptionDoesSendTheBroadcast() throws SocketException {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        final DatagramSocket sa = new DatagramSocket();
        final DatagramSocket sb = new DatagramSocket();
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

        sa.close();
        sb.close();
    }
}
