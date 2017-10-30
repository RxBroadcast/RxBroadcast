package rxbroadcast;

import org.jetbrains.annotations.NotNull;

public final class TestValueSerializer implements Serializer<TestValue> {
    @NotNull
    @Override
    public final TestValue decode(final @NotNull byte[] data) {
        return new TestValue(data[0]);
    }

    @Override
    public final @NotNull byte[] encode(final @NotNull TestValue data) {
        return new byte[]{(byte) data.value};
    }
}
