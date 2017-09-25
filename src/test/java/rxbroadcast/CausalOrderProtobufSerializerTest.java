package rxbroadcast;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings({"checkstyle:MagicNumber"})
public final class CausalOrderProtobufSerializerTest {
    /**
     * Tests that encoding a VT produces the same result as the schema.
     *
     * <p>The Protocol Buffer schema the resultant bytes should adhere to:
     * <pre>
     *     message VectorTimestamped {
     *         repeated bytes ids = 1;
     *         repeated uint64 timestamps = 2 [packed=true];
     *         required bytes value = 3;
     *     }
     * </pre>
     *
     * <p>Using the JavaScript generated from the schema:
     * <pre>
     *     > const t = new VectorTimestamped()
     *     undefined
     *     > t.addIds(new Uint8Array([1]))
     *     undefined
     *     > t.addIds(new Uint8Array([2]))
     *     undefined
     *     > t.addTimestamps(3)
     *     undefined
     *     > t.addTimestamps(4)
     *     undefined
     *     > t.setValue(new Uint8Array([42]))
     *     undefined
     *     > t.serializeBinary()
     *     Uint8Array [ 10, 1, 1, 10, 1, 2, 18, 2, 3, 4, 26, 1, 42 ]
     * </pre>
     */
    @Test
    public final void testEncode() {
        final CausalOrderProtobufSerializer<TestValue> serializer = new CausalOrderProtobufSerializer<>(
            new TestValueSerializer());
        final TestValue value = new TestValue(42);
        final Sender[] ids = new Sender[]{new Sender(new byte[]{1}), new Sender(new byte[]{2})};
        final long[] timestamps = new long[]{3, 4};
        final byte[] bytes = serializer.encode(new VectorTimestamped<>(value, new VectorTimestamp(ids, timestamps)));

        Assert.assertArrayEquals(new byte[]{10, 1, 1, 10, 1, 2, 18, 2, 3, 4, 26, 1, 42}, bytes);
    }

    @Test
    public final void testDecode() {
        final CausalOrderProtobufSerializer<TestValue> serializer = new CausalOrderProtobufSerializer<>(
            new TestValueSerializer());
        final Sender[] ids = new Sender[]{new Sender(new byte[]{1}), new Sender(new byte[]{2})};
        final long[] timestamps = new long[]{3, 4};
        final byte[] bytes = new byte[]{10, 1, 1, 10, 1, 2, 18, 2, 3, 4, 26, 1, 42};
        final VectorTimestamped<TestValue> timestamped = serializer.decode(bytes);

        Assert.assertEquals(new VectorTimestamp(ids, timestamps), timestamped.timestamp);
        Assert.assertEquals(new TestValue(42), timestamped.value);
    }
}
