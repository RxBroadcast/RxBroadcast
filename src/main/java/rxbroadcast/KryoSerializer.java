package rxbroadcast;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.FastOutput;
import com.esotericsoftware.kryo.io.Output;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("WeakerAccess")
public final class KryoSerializer<T> implements Serializer<T> {
    private final ThreadLocal<Kryo> threadLocalKryo = ThreadLocal.withInitial(Kryo::new);

    @SuppressWarnings({"checkstyle:MagicNumber"})
    public final byte[] serialize(final Object value) {
        final Kryo kryo = threadLocalKryo.get();
        try (final Output output = new FastOutput(16, 1024)) {
            kryo.writeClassAndObject(output, value);
            return output.toBytes();
        }
    }

    public final Object deserialize(final byte[] bytes) {
        final Kryo kryo = threadLocalKryo.get();
        return kryo.readClassAndObject(new FastInput(bytes));
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public final T decode(@NotNull final byte[] data) {
        return (T) deserialize(data);
    }

    @NotNull
    @Override
    public final byte[] encode(@NotNull final T data) {
        return serialize(data);
    }
}
