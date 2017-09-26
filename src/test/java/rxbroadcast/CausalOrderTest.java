package rxbroadcast;

import org.junit.Assert;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.util.Arrays;

public class CausalOrderTest {
    private static Sender[] arrayOf(final Sender... buffers) {
        return buffers;
    }

    private static Sender sender(final int... args) {
        final byte[] bytes = new byte[args.length];
        for (int i = 0; i < args.length; i++) {
            bytes[i] = (byte) args[i];
        }
        return new Sender(bytes);
    }

    /**
     * Returns a new {@link VectorTimestamped<TestValue>}.
     * @param value the test value
     * @param identifiers the IDs for the timestamp
     * @param clocks the times for the timestamp
     * @return a new {@link VectorTimestamped<TestValue>}
     */
    private static VectorTimestamped<TestValue> timestampedValue(
        final int value,
        final Sender[] identifiers,
        final long[] clocks
    ) {
        return new VectorTimestamped<>(new TestValue(value), new VectorTimestamp(identifiers, clocks));
    }

    @Test
    public final void receiveMessagesFromSingleSourceOutOfOrder() {
        final Sender sender0 = sender(0);
        final VectorTimestamped<TestValue> value0 = timestampedValue(42, arrayOf(sender0), new long[]{1});
        final VectorTimestamped<TestValue> value1 = timestampedValue(43, arrayOf(sender0), new long[]{2});
        final VectorTimestamped<TestValue> value2 = timestampedValue(44, arrayOf(sender0), new long[]{3});
        final CausalOrder<TestValue> causalOrder = new CausalOrder<>(sender(1));
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();

        causalOrder.receive(sender0, consumer::onNext, value2);
        causalOrder.receive(sender0, consumer::onNext, value1);
        causalOrder.receive(sender0, consumer::onNext, value0);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value1.value, value2.value));
        Assert.assertEquals(0, causalOrder.queueSize());
    }

    @Test
    public final void receiveMessagesFromSingleSourceInOrder() {
        final Sender sender0 = sender(0);
        final VectorTimestamped<TestValue> value0 = timestampedValue(42, arrayOf(sender0), new long[]{1});
        final VectorTimestamped<TestValue> value1 = timestampedValue(43, arrayOf(sender0), new long[]{2});
        final VectorTimestamped<TestValue> value2 = timestampedValue(44, arrayOf(sender0), new long[]{3});
        final CausalOrder<TestValue> causalOrder = new CausalOrder<>(sender(1));
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();

        causalOrder.receive(sender0, consumer::onNext, value0);
        causalOrder.receive(sender0, consumer::onNext, value1);
        causalOrder.receive(sender0, consumer::onNext, value2);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value1.value, value2.value));
        Assert.assertEquals(0, causalOrder.queueSize());
    }

    /**
     * Test messages are received in causal order. This process is the 0th process in the
     * following diagram:
     * <p>
     * <pre>
     * {@code
     * 0 -------x----x------
     *          |    |
     *          |    |
     *          |    |
     * 1 ---x---o----|------
     *      |________|
     *     /
     *    |
     * 2 -o-----------------
     * }
     * </pre>
     */
    @Test
    public final void receiveMessagesInCausalOrder() {
        final Sender sender0 = sender(0);
        final Sender sender1 = sender(1);
        final Sender sender2 = sender(2);
        final VectorTimestamped<TestValue> value0 = timestampedValue(42, arrayOf(sender2), new long[]{1});
        final VectorTimestamped<TestValue> value1 = timestampedValue(43, arrayOf(sender1, sender2), new long[]{1, 1});
        final CausalOrder<TestValue> causalOrder = new CausalOrder<>(sender0);
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();

        causalOrder.receive(sender1, consumer::onNext, value1);
        causalOrder.receive(sender2, consumer::onNext, value0);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value1.value));
        Assert.assertEquals(0, causalOrder.queueSize());
    }

    /**
     * See {@see CausalOrderTest#receiveMessagesInCausalOrder()} for drawing.
     */
    @Test
    public final void receiveDuplicateMessagesInCausalOrder() {
        final Sender sender0 = sender(0);
        final Sender sender1 = sender(1);
        final Sender sender2 = sender(2);
        final VectorTimestamped<TestValue> value0 = timestampedValue(42, arrayOf(sender2), new long[]{1});
        final VectorTimestamped<TestValue> value1 = timestampedValue(43, arrayOf(sender1, sender2), new long[]{1, 1});
        final CausalOrder<TestValue> causalOrder = new CausalOrder<>(sender0);
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();

        causalOrder.receive(sender1, consumer::onNext, value1);
        causalOrder.receive(sender1, consumer::onNext, value1);
        causalOrder.receive(sender2, consumer::onNext, value0);
        causalOrder.receive(sender2, consumer::onNext, value0);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value1.value));
    }

    @SuppressWarnings({"checkstyle:MagicNumber"})
    @Test
    public final void prepareDoesReturnVectorTimestampedValueWithIncreasingTimestamps() {
        final CausalOrder<TestValue> causalOrder = new CausalOrder<>(sender(0));
        final VectorTimestamped<TestValue> value1 = new VectorTimestamped<>(
            new TestValue(42), new VectorTimestamp(arrayOf(sender(0)), new long[]{1}));
        final VectorTimestamped<TestValue> value2 = new VectorTimestamped<>(
            new TestValue(42), new VectorTimestamp(arrayOf(sender(0)), new long[]{2}));
        final VectorTimestamped<TestValue> value3 = new VectorTimestamped<>(
            new TestValue(42), new VectorTimestamp(arrayOf(sender(0)), new long[]{3}));

        Assert.assertEquals(value1, causalOrder.prepare(new TestValue(42)));
        Assert.assertEquals(value2, causalOrder.prepare(new TestValue(42)));
        Assert.assertEquals(value3, causalOrder.prepare(new TestValue(42)));
    }

    @Test
    public final void sendReceiveUnrelatedMessagesWithTwoConsumers() {
        final Sender sender0 = sender(0);
        final Sender sender1 = sender(1);
        final VectorTimestamped<TestValue> value01 = timestampedValue(42, arrayOf(sender0), new long[]{1});
        final VectorTimestamped<TestValue> value02 = timestampedValue(43, arrayOf(sender0), new long[]{2});
        final VectorTimestamped<TestValue> value11 = timestampedValue(44, arrayOf(sender1), new long[]{1});
        final VectorTimestamped<TestValue> value12 = timestampedValue(44, arrayOf(sender1), new long[]{2});
        final CausalOrder<TestValue> causalOrder0 = new CausalOrder<>(sender0);
        final CausalOrder<TestValue> causalOrder1 = new CausalOrder<>(sender1);
        final TestSubscriber<TestValue> consumer0 = new TestSubscriber<>();
        final TestSubscriber<TestValue> consumer1 = new TestSubscriber<>();

        causalOrder1.receive(sender0, consumer1::onNext, value01);
        causalOrder0.receive(sender1, consumer0::onNext, value11);
        causalOrder1.receive(sender0, consumer1::onNext, value02);
        causalOrder0.receive(sender1, consumer0::onNext, value12);

        consumer0.assertReceivedOnNext(Arrays.asList(value11.value, value12.value));
        consumer1.assertReceivedOnNext(Arrays.asList(value01.value, value02.value));
        Assert.assertEquals(0, causalOrder0.queueSize());
        Assert.assertEquals(0, causalOrder1.queueSize());
    }

    @SuppressWarnings({"checkstyle:MagicNumber"})
    @Test
    public final void sendReceiveCausalMessagesWithTwoConsumers() {
        final Sender sender0 = sender(0);
        final Sender sender1 = sender(1);
        final CausalOrder<TestValue> causalOrder0 = new CausalOrder<>(sender0);
        final CausalOrder<TestValue> causalOrder1 = new CausalOrder<>(sender1);
        final TestSubscriber<TestValue> consumer0 = new TestSubscriber<>();
        final TestSubscriber<TestValue> consumer1 = new TestSubscriber<>();

        causalOrder1.receive(sender0, consumer1::onNext, causalOrder0.prepare(new TestValue(42)));
        causalOrder0.receive(sender1, consumer0::onNext, causalOrder1.prepare(new TestValue(42)));
        causalOrder1.receive(sender0, consumer1::onNext, causalOrder0.prepare(new TestValue(43)));
        causalOrder0.receive(sender1, consumer0::onNext, causalOrder1.prepare(new TestValue(43)));

        consumer0.assertReceivedOnNext(Arrays.asList(new TestValue(42), new TestValue(43)));
        consumer1.assertReceivedOnNext(Arrays.asList(new TestValue(42), new TestValue(43)));
        Assert.assertEquals(0, causalOrder0.queueSize());
        Assert.assertEquals(0, causalOrder1.queueSize());
    }
}
