package rxbroadcast;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings({"checkstyle:MagicNumber"})
public final class ObjectSerializerTest {
    @Test
    public final void testEncodeDecodeDoesResultInSameObject() {
        final Serializer<TestValue> serializer = new ObjectSerializer<>();
        final byte[] bytes = serializer.encode(new TestValue(42));
        Assert.assertEquals(new TestValue(42), serializer.decode(bytes));
    }
}
