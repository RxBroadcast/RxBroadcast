package rx.broadcast.integration;

import org.junit.Assert;
import rx.Observable;
import rx.broadcast.BasicOrder;
import rx.broadcast.Broadcast;
import rx.broadcast.TestValue;
import rx.broadcast.UdpBroadcast;
import rx.observers.TestSubscriber;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class TwoWayBasicOrderUdpBroadcastTest {
    private static final int MESSAGE_COUNT = 100;

    private static final double MESSAGE_DELTA = (double) 30;

    private static final Observable<Long> MESSAGE_INTERVAL = Observable.interval(50, TimeUnit.MILLISECONDS);

    private static final long TIMEOUT_DURATION = 5;

    private static final TimeUnit TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;

    private TwoWayBasicOrderUdpBroadcastTest() { /* empty */ }

    public static void main(final String[] args) throws InterruptedException, SocketException, UnknownHostException {
        final int port = Integer.parseInt(System.getProperty("port"));
        final int identifier = Integer.parseInt(System.getProperty("identifier"));
        final InetAddress destination = InetAddress.getByName(System.getProperty("destination"));
        final Broadcast broadcast = new UdpBroadcast<>(
            new DatagramSocket(port), destination, port, new BasicOrder<>());
        final TestSubscriber<TestValue> subscriberA = new TestSubscriber<>();
        final TestSubscriber<TestValue> subscriberB = new TestSubscriber<>();
        final CountDownLatch latch = new CountDownLatch(1);

        System.out.println("Starting");
        MESSAGE_INTERVAL.map(x -> new TestValue(identifier)).flatMap(broadcast::send).subscribe(
            x -> System.out.printf("Interval#onNext %s%n", x));

        final Observable<TestValue> theirs = broadcast.valuesOfType(TestValue.class)
            .filter(x -> x.value != identifier)
            .doOnNext(x -> System.out.printf("Received#onNext %s%n", x));
        final Observable<TestValue> ours = broadcast.valuesOfType(TestValue.class)
            .filter(x -> x.value == identifier)
            .take(MESSAGE_COUNT)
            .doOnNext(x -> System.out.printf("Received#onNext %s%n", x));

        ours.subscribe(subscriberA);
        theirs.subscribe(subscriberB);

        latch.await(TIMEOUT_DURATION, TIMEOUT_TIME_UNIT);
        subscriberA.assertCompleted();
        subscriberA.assertNoErrors();
        Assert.assertEquals(MESSAGE_COUNT, subscriberA.getOnNextEvents().size(), MESSAGE_DELTA);
        Assert.assertEquals(MESSAGE_COUNT, subscriberB.getOnNextEvents().size(), MESSAGE_DELTA);
    }
}
