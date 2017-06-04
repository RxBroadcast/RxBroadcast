package rx.broadcast;

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
    public final void receiveMessagesFromSingleSourceInOrder() {
        final VectorTimestamped<TestValue> value0 = timestampedValue(42, arrayOf(sender(0)), new long[]{0});
        final VectorTimestamped<TestValue> value1 = timestampedValue(43, arrayOf(sender(0)), new long[]{1});
        final VectorTimestamped<TestValue> value2 = timestampedValue(44, arrayOf(sender(0)), new long[]{2});
        final CausalOrder<TestValue> causalOrder = new CausalOrder<>();
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();

        causalOrder.receive(new Sender(new byte[] {0}), consumer::onNext, value0);
        causalOrder.receive(new Sender(new byte[] {0}), consumer::onNext, value1);
        causalOrder.receive(new Sender(new byte[] {0}), consumer::onNext, value2);

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
        final Sender sender1 = sender(1);
        final Sender sender2 = sender(2);
        final VectorTimestamped<TestValue> value0 = timestampedValue(42, arrayOf(sender2), new long[]{0});
        final VectorTimestamped<TestValue> value1 = timestampedValue(43, arrayOf(sender1, sender2), new long[]{0, 0});
        final CausalOrder<TestValue> causalOrder = new CausalOrder<>();
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
        final Sender sender1 = sender(1);
        final Sender sender2 = sender(2);
        final VectorTimestamped<TestValue> value0 = timestampedValue(42, arrayOf(sender2), new long[]{0});
        final VectorTimestamped<TestValue> value1 = timestampedValue(43, arrayOf(sender1, sender2), new long[]{0, 0});
        final CausalOrder<TestValue> causalOrder = new CausalOrder<>();
        final TestSubscriber<TestValue> consumer = new TestSubscriber<>();

        causalOrder.receive(sender1, consumer::onNext, value1);
        causalOrder.receive(sender1, consumer::onNext, value1);
        causalOrder.receive(sender2, consumer::onNext, value0);
        causalOrder.receive(sender2, consumer::onNext, value0);

        consumer.assertReceivedOnNext(Arrays.asList(value0.value, value1.value));
    }
}
