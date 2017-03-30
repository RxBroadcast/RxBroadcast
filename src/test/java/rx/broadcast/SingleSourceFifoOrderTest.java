package rx.broadcast;

import org.junit.Assert;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.util.Arrays;

@SuppressWarnings({"Duplicates", "checkstyle:MagicNumber"})
public class SingleSourceFifoOrderTest {
    @Test
    public final void receiveMessagesInOrder() {
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(0, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(1, new TestValue(42));
        final Timestamped<TestValue> value2 = new Timestamped<>(2, new TestValue(43));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(0, consumer::onNext, value0);
        ssf.receive(0, consumer::onNext, value1);
        ssf.receive(0, consumer::onNext, value2);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value1.value, value2.value));
    }

    @Test
    public final void receiveMessagesOutOfOrder() {
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(0, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(1, new TestValue(42));
        final Timestamped<TestValue> value2 = new Timestamped<>(2, new TestValue(43));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(0, consumer::onNext, value1);
        ssf.receive(0, consumer::onNext, value0);
        ssf.receive(0, consumer::onNext, value2);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value1.value, value2.value));
    }

    @Test
    public final void receiveMessagesInReverseOrder() {
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(0, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(1, new TestValue(42));
        final Timestamped<TestValue> value2 = new Timestamped<>(2, new TestValue(43));
        final Timestamped<TestValue> value3 = new Timestamped<>(3, new TestValue(44));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(0, consumer::onNext, value3);
        ssf.receive(0, consumer::onNext, value2);
        ssf.receive(0, consumer::onNext, value1);
        ssf.receive(0, consumer::onNext, value0);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value1.value, value2.value, value3.value));
    }

    @Test
    public final void receiveMessagesWithDropLateFlagDoesDropLateMessages() {
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(0, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(1, new TestValue(42));
        final Timestamped<TestValue> value2 = new Timestamped<>(2, new TestValue(43));
        final Timestamped<TestValue> value3 = new Timestamped<>(3, new TestValue(44));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>(SingleSourceFifoOrder.DROP_LATE);

        ssf.receive(0, consumer::onNext, value2);
        ssf.receive(0, consumer::onNext, value0);
        ssf.receive(0, consumer::onNext, value1);
        ssf.receive(0, consumer::onNext, value3);

        consumer.assertReceivedOnNext(Arrays.asList(value2.value, value3.value));
    }

    @Test
    public final void receiveDuplicateMessagesInOrder() {
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(0, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(0, new TestValue(41));
        final Timestamped<TestValue> value2 = new Timestamped<>(1, new TestValue(42));
        final Timestamped<TestValue> value3 = new Timestamped<>(1, new TestValue(42));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(0, consumer::onNext, value0);
        ssf.receive(0, consumer::onNext, value1);
        ssf.receive(0, consumer::onNext, value2);
        ssf.receive(0, consumer::onNext, value3);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value2.value));
    }

    @Test
    public final void receiveDuplicateMessagesInReverseOrder() {
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(0, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(0, new TestValue(41));
        final Timestamped<TestValue> value2 = new Timestamped<>(1, new TestValue(42));
        final Timestamped<TestValue> value3 = new Timestamped<>(1, new TestValue(42));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(0, consumer::onNext, value3);
        ssf.receive(0, consumer::onNext, value2);
        ssf.receive(0, consumer::onNext, value1);
        ssf.receive(0, consumer::onNext, value0);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value2.value));
    }

    @Test
    public final void receiveUnrelatedMessagesFromTwoSenders() {
        final long sender1 = -6048052811696954696L;
        final long sender2 = -6048052815991921992L;

        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(0, new TestValue(40));
        final Timestamped<TestValue> value1 = new Timestamped<>(0, new TestValue(41));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(sender1, consumer::onNext, value0);
        ssf.receive(sender2, consumer::onNext, value1);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value1.value));
    }

    @SuppressWarnings({"checkstyle:LineLength"})
    @Test
    public final void receiveMessagesFromTwoSendersBothOutOfOrder() {
        final long sender1 = -6048052811696954696L;
        final long sender2 = -6048052815991921992L;

        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(0, new TestValue(40));
        final Timestamped<TestValue> value1 = new Timestamped<>(1, new TestValue(41));
        final Timestamped<TestValue> value2 = new Timestamped<>(2, new TestValue(42));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(sender1, consumer::onNext, value2);
        ssf.receive(sender2, consumer::onNext, value1);
        ssf.receive(sender1, consumer::onNext, value1);
        ssf.receive(sender2, consumer::onNext, value2);
        ssf.receive(sender1, consumer::onNext, value0);
        ssf.receive(sender2, consumer::onNext, value0);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value1.value, value2.value, value0.value, value1.value, value2.value));
    }

    @Test
    public final void receiveMessagesFromTwoSendersWithDropLateFlagDoesDropLateMessages() {
        final long sender1 = -6048052811696954696L;
        final long sender2 = -6048052815991921992L;

        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(0, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(1, new TestValue(42));
        final Timestamped<TestValue> value2 = new Timestamped<>(2, new TestValue(43));
        final Timestamped<TestValue> value3 = new Timestamped<>(3, new TestValue(44));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>(SingleSourceFifoOrder.DROP_LATE);

        ssf.receive(sender1, consumer::onNext, value2);
        ssf.receive(sender1, consumer::onNext, value0);
        ssf.receive(sender1, consumer::onNext, value1);
        ssf.receive(sender1, consumer::onNext, value3);

        ssf.receive(sender2, consumer::onNext, value2);
        ssf.receive(sender2, consumer::onNext, value0);
        ssf.receive(sender2, consumer::onNext, value1);
        ssf.receive(sender2, consumer::onNext, value3);

        consumer.assertReceivedOnNext(Arrays.asList(value2.value, value3.value, value2.value, value3.value));
    }

    @Test
    public final void receiveDuplicateMessagesFromTwoSendersInOrder() {
        final long sender1 = -6048052811696954696L;
        final long sender2 = -6048052815991921992L;

        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(0, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(0, new TestValue(41));
        final Timestamped<TestValue> value2 = new Timestamped<>(1, new TestValue(42));
        final Timestamped<TestValue> value3 = new Timestamped<>(1, new TestValue(42));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(sender1, consumer::onNext, value0);
        ssf.receive(sender1, consumer::onNext, value1);
        ssf.receive(sender1, consumer::onNext, value2);
        ssf.receive(sender1, consumer::onNext, value3);

        ssf.receive(sender2, consumer::onNext, value0);
        ssf.receive(sender2, consumer::onNext, value1);
        ssf.receive(sender2, consumer::onNext, value2);
        ssf.receive(sender2, consumer::onNext, value3);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value2.value, value0.value, value2.value));
    }

    @Test
    public final void receiveDuplicateMessagesFromTwoSendersInReverseOrder() {
        final long sender1 = -6048052811696954696L;
        final long sender2 = -6048052815991921992L;

        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(0, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(0, new TestValue(41));
        final Timestamped<TestValue> value2 = new Timestamped<>(1, new TestValue(42));
        final Timestamped<TestValue> value3 = new Timestamped<>(1, new TestValue(42));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(sender1, consumer::onNext, value3);
        ssf.receive(sender1, consumer::onNext, value2);
        ssf.receive(sender1, consumer::onNext, value1);
        ssf.receive(sender1, consumer::onNext, value0);

        ssf.receive(sender2, consumer::onNext, value3);
        ssf.receive(sender2, consumer::onNext, value2);
        ssf.receive(sender2, consumer::onNext, value1);
        ssf.receive(sender2, consumer::onNext, value0);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value2.value, value0.value, value2.value));
    }

    @Test
    public final void prepareValueShouldReturnTimestampedValueWithIncreasingTimestamps() {
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        Assert.assertEquals(new Timestamped<>(0, new TestValue(42)), ssf.prepare(new TestValue(42)));
        Assert.assertEquals(new Timestamped<>(1, new TestValue(42)), ssf.prepare(new TestValue(42)));
        Assert.assertEquals(new Timestamped<>(2, new TestValue(42)), ssf.prepare(new TestValue(42)));
    }
}
