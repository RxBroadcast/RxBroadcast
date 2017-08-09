package rx.broadcast;

import org.junit.Assert;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.nio.ByteBuffer;
import java.util.Arrays;

@SuppressWarnings({"Duplicates", "checkstyle:MagicNumber"})
public class SingleSourceFifoOrderTest {
    @Test
    public final void receiveMessagesInOrder() {
        final Sender sender1 = new Sender(new byte[]{0});
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(1, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(2, new TestValue(42));
        final Timestamped<TestValue> value2 = new Timestamped<>(3, new TestValue(43));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(sender1, consumer::onNext, value0);
        ssf.receive(sender1, consumer::onNext, value1);
        ssf.receive(sender1, consumer::onNext, value2);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value1.value, value2.value));
    }

    @Test
    public final void receiveMessagesOutOfOrder() {
        final Sender sender1 = new Sender(new byte[]{0});
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(1, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(2, new TestValue(42));
        final Timestamped<TestValue> value2 = new Timestamped<>(3, new TestValue(43));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(sender1, consumer::onNext, value1);
        ssf.receive(sender1, consumer::onNext, value0);
        ssf.receive(sender1, consumer::onNext, value2);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value1.value, value2.value));
    }

    @Test
    public final void receiveMessagesInReverseOrder() {
        final Sender sender1 = new Sender(new byte[]{0});
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(1, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(2, new TestValue(42));
        final Timestamped<TestValue> value2 = new Timestamped<>(3, new TestValue(43));
        final Timestamped<TestValue> value3 = new Timestamped<>(4, new TestValue(44));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(sender1, consumer::onNext, value3);
        ssf.receive(sender1, consumer::onNext, value2);
        ssf.receive(sender1, consumer::onNext, value1);
        ssf.receive(sender1, consumer::onNext, value0);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value1.value, value2.value, value3.value));
    }

    @Test
    public final void receiveMessagesWithDropLateFlagDoesDropLateMessages() {
        final Sender sender1 = new Sender(new byte[]{0});
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(1, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(2, new TestValue(42));
        final Timestamped<TestValue> value2 = new Timestamped<>(3, new TestValue(43));
        final Timestamped<TestValue> value3 = new Timestamped<>(4, new TestValue(44));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>(SingleSourceFifoOrder.DROP_LATE);

        ssf.receive(sender1, consumer::onNext, value2);
        ssf.receive(sender1, consumer::onNext, value0);
        ssf.receive(sender1, consumer::onNext, value1);
        ssf.receive(sender1, consumer::onNext, value3);

        consumer.assertReceivedOnNext(Arrays.asList(value2.value, value3.value));
    }

    @Test
    public final void receiveDuplicateMessagesInOrder() {
        final Sender sender1 = new Sender(new byte[]{0});
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(1, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(1, new TestValue(41));
        final Timestamped<TestValue> value2 = new Timestamped<>(2, new TestValue(42));
        final Timestamped<TestValue> value3 = new Timestamped<>(2, new TestValue(42));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(sender1, consumer::onNext, value0);
        ssf.receive(sender1, consumer::onNext, value1);
        ssf.receive(sender1, consumer::onNext, value2);
        ssf.receive(sender1, consumer::onNext, value3);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value2.value));
    }

    @Test
    public final void receiveDuplicateMessagesInReverseOrder() {
        final Sender sender1 = new Sender(new byte[]{0});
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(1, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(1, new TestValue(41));
        final Timestamped<TestValue> value2 = new Timestamped<>(2, new TestValue(42));
        final Timestamped<TestValue> value3 = new Timestamped<>(2, new TestValue(42));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(sender1, consumer::onNext, value3);
        ssf.receive(sender1, consumer::onNext, value2);
        ssf.receive(sender1, consumer::onNext, value1);
        ssf.receive(sender1, consumer::onNext, value0);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value2.value));
    }

    @Test
    public final void receiveUnrelatedMessagesFromTwoSenders() {
        final Sender sender1 = new Sender(ByteBuffer.allocate(Long.BYTES).putLong(-6048052811696954696L).array());
        final Sender sender2 = new Sender(ByteBuffer.allocate(Long.BYTES).putLong(-6048052815991921992L).array());

        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(1, new TestValue(40));
        final Timestamped<TestValue> value1 = new Timestamped<>(1, new TestValue(41));
        final SingleSourceFifoOrder<TestValue> ssf = new SingleSourceFifoOrder<>();

        ssf.receive(sender1, consumer::onNext, value0);
        ssf.receive(sender2, consumer::onNext, value1);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value1.value));
    }

    @SuppressWarnings({"checkstyle:LineLength"})
    @Test
    public final void receiveMessagesFromTwoSendersBothOutOfOrder() {
        final Sender sender1 = new Sender(ByteBuffer.allocate(Long.BYTES).putLong(-6048052811696954696L).array());
        final Sender sender2 = new Sender(ByteBuffer.allocate(Long.BYTES).putLong(-6048052815991921992L).array());

        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(1, new TestValue(40));
        final Timestamped<TestValue> value1 = new Timestamped<>(2, new TestValue(41));
        final Timestamped<TestValue> value2 = new Timestamped<>(3, new TestValue(42));
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
        final Sender sender1 = new Sender(ByteBuffer.allocate(Long.BYTES).putLong(-6048052811696954696L).array());
        final Sender sender2 = new Sender(ByteBuffer.allocate(Long.BYTES).putLong(-6048052815991921992L).array());

        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(1, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(2, new TestValue(42));
        final Timestamped<TestValue> value2 = new Timestamped<>(3, new TestValue(43));
        final Timestamped<TestValue> value3 = new Timestamped<>(4, new TestValue(44));
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
        final Sender sender1 = new Sender(ByteBuffer.allocate(Long.BYTES).putLong(-6048052811696954696L).array());
        final Sender sender2 = new Sender(ByteBuffer.allocate(Long.BYTES).putLong(-6048052815991921992L).array());

        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(1, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(1, new TestValue(41));
        final Timestamped<TestValue> value2 = new Timestamped<>(2, new TestValue(42));
        final Timestamped<TestValue> value3 = new Timestamped<>(2, new TestValue(42));
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
        final Sender sender1 = new Sender(ByteBuffer.allocate(Long.BYTES).putLong(-6048052811696954696L).array());
        final Sender sender2 = new Sender(ByteBuffer.allocate(Long.BYTES).putLong(-6048052815991921992L).array());

        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();
        final Timestamped<TestValue> value0 = new Timestamped<>(1, new TestValue(41));
        final Timestamped<TestValue> value1 = new Timestamped<>(1, new TestValue(41));
        final Timestamped<TestValue> value2 = new Timestamped<>(2, new TestValue(42));
        final Timestamped<TestValue> value3 = new Timestamped<>(2, new TestValue(42));
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

        Assert.assertEquals(new Timestamped<>(1, new TestValue(42)), ssf.prepare(new TestValue(42)));
        Assert.assertEquals(new Timestamped<>(2, new TestValue(42)), ssf.prepare(new TestValue(42)));
        Assert.assertEquals(new Timestamped<>(3, new TestValue(42)), ssf.prepare(new TestValue(42)));
    }
}
