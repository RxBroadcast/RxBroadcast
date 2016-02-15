package rx.broadcast;

import org.junit.Test;
import rx.observers.TestSubscriber;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collections;

public class UdpBroadcastTest {
    @Test
    public final void valuesOfTypeDoesReceiveBroadcastValue() throws Exception {
        final TestSubscriber subscriber = new TestSubscriber();
        final DatagramSocket sa = new DatagramSocket();
        final DatagramSocket sb = new DatagramSocket();
        final Broadcast broadcast1 = new UdpBroadcast(sa, InetAddress.getLoopbackAddress(), sb.getLocalPort());
        final Broadcast broadcast2 = new UdpBroadcast(sb, InetAddress.getLoopbackAddress(), sa.getLocalPort());

        broadcast2.valuesOfType(TestValue.class).first().subscribe(subscriber);
        broadcast1.send(new TestValue(42));

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        subscriber.assertReceivedOnNext(Collections.singletonList(new TestValue(42)));

        sa.close();
        sb.close();
    }
}
