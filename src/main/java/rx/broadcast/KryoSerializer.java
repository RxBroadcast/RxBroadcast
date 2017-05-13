package rx.broadcast;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.FastOutput;
import com.esotericsoftware.kryo.io.Output;

@SuppressWarnings("WeakerAccess")
public final class KryoSerializer implements Serializer<Object> {
    private final ThreadLocal<Kryo> threadLocalKryo = ThreadLocal.withInitial(Kryo::new);

    public final byte[] serialize(final Object value) {
        final Kryo kryo = threadLocalKryo.get();
        final Output output = new FastOutput(16, 1024);
        kryo.writeClassAndObject(output, value);
        return output.toBytes();
    }

    public final Object deserialize(final byte[] bytes) {
        final Kryo kryo = threadLocalKryo.get();
        return kryo.readClassAndObject(new FastInput(bytes));
    }

    @Override
    public final Object decode(final byte[] data) {
        return deserialize(data);
    }

    @Override
    public final byte[] encode(final Object data) {
        return serialize(data);
    }
}
