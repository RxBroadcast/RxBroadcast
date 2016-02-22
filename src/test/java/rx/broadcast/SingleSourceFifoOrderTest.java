package rx.broadcast;

import org.junit.Test;
import rx.observers.TestSubscriber;

import java.util.Arrays;

public class SingleSourceFifoOrderTest {
    @SuppressWarnings({"checkstyle:magicnumber"})
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
}
