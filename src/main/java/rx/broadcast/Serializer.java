package rx.broadcast;

import org.jetbrains.annotations.NotNull;

public interface Serializer<T> {
    @NotNull
    T decode(@NotNull byte[] data);

    @NotNull
    byte[] encode(@NotNull T data);
}
