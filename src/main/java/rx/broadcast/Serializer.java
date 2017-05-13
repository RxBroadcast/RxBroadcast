package rx.broadcast;

public interface Serializer<T> {
    T decode(byte[] data);

    byte[] encode(T data);
}
