package rxbroadcast;

import org.junit.Assert;
import org.junit.Test;
import rx.observers.TestSubscriber;

public final class CausalOrderTest {
    @Test
    public final void prepareDoesIncrementTheLocalClock() {
        final Sender s = new Sender(new byte[]{1});
        final CausalOrder<TestValue> causalOrder = new CausalOrder<>(s);

        final VectorTimestamped<TestValue> value = causalOrder.prepare(new TestValue('a'));

        Assert.assertEquals(
            new VectorTimestamped<>(new TestValue('a'), new VectorTimestamp(new Sender[]{s}, 1)),
            value);
    }

    @Test
    public final void prepareDoesAttachTheFullClockToTheMessage() {
        final Sender s1 = new Sender(new byte[]{1});
        final Sender s2 = new Sender(new byte[]{2});
        final CausalOrder<TestValue> causalOrder1 = new CausalOrder<>(s1);
        final CausalOrder<TestValue> causalOrder2 = new CausalOrder<>(s2);
        final TestSubscriber<TestValue> subscriber = new TestSubscriber<>();

        causalOrder2.prepare(new TestValue('b'));
        causalOrder2.receive(s1, subscriber::onNext, causalOrder1.prepare(new TestValue('a')));
        final VectorTimestamped<TestValue> value = causalOrder2.prepare(new TestValue('c'));

        Assert.assertEquals(
            new VectorTimestamped<>(new TestValue('c'), new VectorTimestamp(new Sender[]{s1, s2}, 1, 2)),
            value);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public final void firstMessagePreparedCanBeReceived() {
        final Sender s = new Sender(new byte[]{1});
        final CausalOrder<TestValue> causalOrder = new CausalOrder<>(s);
        final TestSubscriber<TestValue> subscriber = new TestSubscriber<>();

        causalOrder.receive(s, subscriber::onNext, causalOrder.prepare(new TestValue(42)));

        subscriber.assertNotCompleted();
        subscriber.assertNoErrors();
        subscriber.assertValues(new TestValue(42));
        Assert.assertEquals(0, causalOrder.delayQueueSize());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public final void duplicateMessageFromHostIsDelayedIndefinitely() {
        final Sender s = new Sender(new byte[]{1});
        final CausalOrder<TestValue> causalOrder = new CausalOrder<>(s);
        final TestSubscriber<TestValue> subscriber = new TestSubscriber<>();

        final VectorTimestamped<TestValue> value = causalOrder.prepare(new TestValue(42));
        causalOrder.receive(s, subscriber::onNext, value);
        causalOrder.receive(s, subscriber::onNext, value);

        subscriber.assertNotCompleted();
        subscriber.assertNoErrors();
        subscriber.assertValues(new TestValue(42));
        Assert.assertEquals(1, causalOrder.delayQueueSize());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public final void receiveTwoOutOfOrderMessagesFromHost() {
        final Sender s = new Sender(new byte[]{1});
        final CausalOrder<TestValue> causalOrder = new CausalOrder<>(s);
        final TestSubscriber<TestValue> subscriber = new TestSubscriber<>();

        final VectorTimestamped<TestValue> value1 = causalOrder.prepare(new TestValue(42));
        final VectorTimestamped<TestValue> value2 = causalOrder.prepare(new TestValue(43));
        causalOrder.receive(s, subscriber::onNext, value2);
        causalOrder.receive(s, subscriber::onNext, value1);

        subscriber.assertNotCompleted();
        subscriber.assertNoErrors();
        subscriber.assertValues(new TestValue(42), new TestValue(43));
        Assert.assertEquals(0, causalOrder.delayQueueSize());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public final void receiveMessagesInReverseOrderFromHost() {
        final Sender s = new Sender(new byte[]{1});
        final CausalOrder<TestValue> causalOrder = new CausalOrder<>(s);
        final TestSubscriber<TestValue> subscriber = new TestSubscriber<>();

        final VectorTimestamped<TestValue> value1 = causalOrder.prepare(new TestValue(42));
        final VectorTimestamped<TestValue> value2 = causalOrder.prepare(new TestValue(43));
        final VectorTimestamped<TestValue> value3 = causalOrder.prepare(new TestValue(44));
        causalOrder.receive(s, subscriber::onNext, value3);
        causalOrder.receive(s, subscriber::onNext, value2);
        causalOrder.receive(s, subscriber::onNext, value1);

        subscriber.assertNotCompleted();
        subscriber.assertNoErrors();
        subscriber.assertValues(new TestValue(42), new TestValue(43), new TestValue(44));
        Assert.assertEquals(0, causalOrder.delayQueueSize());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public final void duplicateMessagesReceivedInReverseOrderFromHostAreDelayedIndefinitely() {
        final Sender s = new Sender(new byte[]{1});
        final CausalOrder<TestValue> causalOrder = new CausalOrder<>(s);
        final TestSubscriber<TestValue> subscriber = new TestSubscriber<>();

        final VectorTimestamped<TestValue> value1 = causalOrder.prepare(new TestValue(42));
        final VectorTimestamped<TestValue> value2 = causalOrder.prepare(new TestValue(43));
        final VectorTimestamped<TestValue> value3 = causalOrder.prepare(new TestValue(44));
        causalOrder.receive(s, subscriber::onNext, value3);
        causalOrder.receive(s, subscriber::onNext, value3);
        causalOrder.receive(s, subscriber::onNext, value2);
        causalOrder.receive(s, subscriber::onNext, value2);
        causalOrder.receive(s, subscriber::onNext, value1);
        causalOrder.receive(s, subscriber::onNext, value1);

        subscriber.assertNotCompleted();
        subscriber.assertNoErrors();
        subscriber.assertValues(new TestValue(42), new TestValue(43), new TestValue(44));
        Assert.assertEquals(3, causalOrder.delayQueueSize());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public final void receiveMessagesInSingleSourceFifoOrder() {
        final Sender s1 = new Sender(new byte[]{1});
        final Sender s2 = new Sender(new byte[]{2});
        final CausalOrder<TestValue> causalOrder1 = new CausalOrder<>(s1);
        final CausalOrder<TestValue> causalOrder2 = new CausalOrder<>(s2);
        final TestSubscriber<TestValue> subscriber = new TestSubscriber<>();

        final VectorTimestamped<TestValue> v1 = causalOrder2.prepare(new TestValue(42));
        final VectorTimestamped<TestValue> v2 = causalOrder2.prepare(new TestValue(43));
        causalOrder1.receive(s2, subscriber::onNext, v1);
        causalOrder1.receive(s2, subscriber::onNext, v2);

        subscriber.assertNotCompleted();
        subscriber.assertNoErrors();
        subscriber.assertValues(new TestValue(42), new TestValue(43));
        Assert.assertEquals(0, causalOrder1.delayQueueSize());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public final void receiveMessagesInReverseOrderFromSingleSource() {
        final Sender s1 = new Sender(new byte[]{1});
        final Sender s2 = new Sender(new byte[]{2});
        final CausalOrder<TestValue> causalOrder1 = new CausalOrder<>(s1);
        final CausalOrder<TestValue> causalOrder2 = new CausalOrder<>(s2);
        final TestSubscriber<TestValue> subscriber = new TestSubscriber<>();

        final VectorTimestamped<TestValue> v1 = causalOrder2.prepare(new TestValue(42));
        final VectorTimestamped<TestValue> v2 = causalOrder2.prepare(new TestValue(43));
        final VectorTimestamped<TestValue> v3 = causalOrder2.prepare(new TestValue(44));
        causalOrder1.receive(s2, subscriber::onNext, v3);
        causalOrder1.receive(s2, subscriber::onNext, v2);
        causalOrder1.receive(s2, subscriber::onNext, v1);

        subscriber.assertNotCompleted();
        subscriber.assertNoErrors();
        subscriber.assertValues(new TestValue(42), new TestValue(43), new TestValue(44));
        Assert.assertEquals(0, causalOrder1.delayQueueSize());
    }

    @Test
    public final void receiveMessagesInCausalOrder1() {
        final Sender s1 = new Sender(new byte[]{1});
        final Sender s2 = new Sender(new byte[]{2});
        final Sender s3 = new Sender(new byte[]{3});
        final CausalOrder<TestValue> causalOrder1 = new CausalOrder<>(s1);
        final CausalOrder<TestValue> causalOrder2 = new CausalOrder<>(s2);
        final CausalOrder<TestValue> causalOrder3 = new CausalOrder<>(s3);
        final TestSubscriber<TestValue> subscriber2 = new TestSubscriber<>();
        final TestSubscriber<TestValue> subscriber3 = new TestSubscriber<>();

        final VectorTimestamped<TestValue> valueA = causalOrder1.prepare(new TestValue('a'));
        causalOrder2.receive(s1, subscriber2::onNext, valueA);
        final VectorTimestamped<TestValue> valueB = causalOrder2.prepare(new TestValue('b'));
        causalOrder3.receive(s2, subscriber3::onNext, valueB);
        causalOrder3.receive(s1, subscriber3::onNext, valueA);

        subscriber2.assertNotCompleted();
        subscriber2.assertNoErrors();
        subscriber2.assertValue(new TestValue('a'));
        subscriber3.assertNotCompleted();
        subscriber3.assertNoErrors();
        subscriber3.assertValues(new TestValue('a'), new TestValue('b'));
        Assert.assertEquals(0, causalOrder2.delayQueueSize());
        Assert.assertEquals(0, causalOrder3.delayQueueSize());
    }

    @Test
    public final void receiveMessagesInCausalOrder2() {
        final Sender sender1 = new Sender(new byte[]{1});
        final Sender sender2 = new Sender(new byte[]{2});
        final Sender sender3 = new Sender(new byte[]{3});
        final CausalOrder<TestValue> causalOrder1 = new CausalOrder<>(sender1);
        final CausalOrder<TestValue> causalOrder2 = new CausalOrder<>(sender2);
        final CausalOrder<TestValue> causalOrder3 = new CausalOrder<>(sender3);
        final TestSubscriber<TestValue> subscriber1 = new TestSubscriber<>();
        final TestSubscriber<TestValue> subscriber2 = new TestSubscriber<>();
        final TestSubscriber<TestValue> subscriber3 = new TestSubscriber<>();

        final VectorTimestamped<TestValue> valueA = causalOrder1.prepare(new TestValue('a'));
        causalOrder1.receive(sender1, subscriber1::onNext, valueA);

        final VectorTimestamped<TestValue> valueL = causalOrder2.prepare(new TestValue('l'));
        causalOrder2.receive(sender2, subscriber2::onNext, valueL);
        causalOrder2.receive(sender1, subscriber2::onNext, valueA);

        final VectorTimestamped<TestValue> valueV = causalOrder3.prepare(new TestValue('v'));
        causalOrder3.receive(sender3, subscriber3::onNext, valueV);
        causalOrder3.receive(sender2, subscriber3::onNext, valueL);
        causalOrder3.receive(sender1, subscriber3::onNext, valueA);
        causalOrder2.receive(sender3, subscriber2::onNext, valueV);
        causalOrder1.receive(sender3, subscriber1::onNext, valueV);
        causalOrder1.receive(sender2, subscriber1::onNext, valueL);

        final VectorTimestamped<TestValue> valueM = causalOrder2.prepare(new TestValue('m'));
        causalOrder2.receive(sender2, subscriber2::onNext, valueM);
        causalOrder1.receive(sender2, subscriber1::onNext, valueM);
        causalOrder3.receive(sender2, subscriber3::onNext, valueM);

        subscriber1.assertNotCompleted();
        subscriber1.assertNoErrors();
        subscriber1.assertValues(new TestValue('a'), new TestValue('v'), new TestValue('l'), new TestValue('m'));
        subscriber2.assertNotCompleted();
        subscriber2.assertNoErrors();
        subscriber2.assertValues(new TestValue('l'), new TestValue('a'), new TestValue('v'), new TestValue('m'));
        subscriber3.assertNotCompleted();
        subscriber3.assertNoErrors();
        subscriber3.assertValues(new TestValue('v'), new TestValue('l'), new TestValue('a'), new TestValue('m'));

        final VectorTimestamped<TestValue> valueW = causalOrder3.prepare(new TestValue('w'));
        Assert.assertEquals(
            new VectorTimestamped<>(new TestValue('w'), new VectorTimestamp(
                new Sender[]{sender1, sender2, sender3}, 1, 2, 2)),
            valueW);
    }

    @Test
    public final void duplicateMessagesReceivedInCausalOrderAreDelayedIndefinitely1() {
        final Sender s1 = new Sender(new byte[]{1});
        final Sender s2 = new Sender(new byte[]{2});
        final Sender s3 = new Sender(new byte[]{3});
        final CausalOrder<TestValue> causalOrder1 = new CausalOrder<>(s1);
        final CausalOrder<TestValue> causalOrder2 = new CausalOrder<>(s2);
        final CausalOrder<TestValue> causalOrder3 = new CausalOrder<>(s3);
        final TestSubscriber<TestValue> subscriber2 = new TestSubscriber<>();
        final TestSubscriber<TestValue> subscriber3 = new TestSubscriber<>();

        final VectorTimestamped<TestValue> valueA = causalOrder1.prepare(new TestValue('a'));
        causalOrder2.receive(s1, subscriber2::onNext, valueA);
        final VectorTimestamped<TestValue> valueB = causalOrder2.prepare(new TestValue('b'));
        causalOrder3.receive(s2, subscriber3::onNext, valueB);
        causalOrder3.receive(s1, subscriber3::onNext, valueA);
        causalOrder3.receive(s1, subscriber3::onNext, valueA);

        subscriber2.assertNotCompleted();
        subscriber2.assertNoErrors();
        subscriber2.assertValue(new TestValue('a'));
        subscriber3.assertNotCompleted();
        subscriber3.assertNoErrors();
        subscriber3.assertValues(new TestValue('a'), new TestValue('b'));
        Assert.assertEquals(0, causalOrder2.delayQueueSize());
        Assert.assertEquals(1, causalOrder3.delayQueueSize());
    }
}
