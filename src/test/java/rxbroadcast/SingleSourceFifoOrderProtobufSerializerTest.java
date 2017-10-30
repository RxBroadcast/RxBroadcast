package rxbroadcast;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings({"checkstyle:MagicNumber"})
public final class SingleSourceFifoOrderProtobufSerializerTest {
    /**
     * Tests that encoding a timestamp produces the same result as the schema.
     *
     * <p>The Protocol Buffer schema the resultant bytes should adhere to:
     * <pre>
     *     message Timestamped {
     *         required uint64 timestamp = 1;
     *         required bytes value = 2;
     *     }
     * </pre>
     *
     * <p>Using the JavaScript generated from the schema:
     * <pre>
     *     > const timestamped = new Timestamped();
     *     undefined
     *     > timestamped.setTimestamp(33);
     *     undefined
     *     > timestamped.setValue(new Uint8Array([42]));
     *     undefined
     *     > timestamped.serializeBinary()
     *     Uint8Array [ 8, 33, 18, 1, 42 ]
     * </pre>
     */
    @Test
    public final void testEncode() {
        final SingleSourceFifoOrderProtobufSerializer<TestValue> s = new SingleSourceFifoOrderProtobufSerializer<>(
            new TestValueSerializer());
        final byte[] bytes = s.encode(new Timestamped<>(33, new TestValue(42)));

        Assert.assertArrayEquals(new byte[]{8, 33, 18, 1, 42}, bytes);
    }

    @Test
    public final void testDecode() {
        final SingleSourceFifoOrderProtobufSerializer<TestValue> s = new SingleSourceFifoOrderProtobufSerializer<>(
            new TestValueSerializer());
        final byte[] bytes = new byte[]{8, 33, 18, 1, 42};
        final Timestamped<TestValue> timestamped = s.decode(bytes);

        Assert.assertEquals(33, timestamped.timestamp);
        Assert.assertEquals(new TestValue(42), timestamped.value);
    }
}
