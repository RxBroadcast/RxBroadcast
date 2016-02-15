package rx.broadcast;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.FastOutput;
import com.esotericsoftware.kryo.io.Output;

public class KryoSerializer {
    private final ThreadLocal<Kryo> threadLocalKryo = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return new Kryo();
        }
    };

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
}
