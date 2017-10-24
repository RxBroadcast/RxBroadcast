package rxbroadcast.integration.pp;

import java.io.Serializable;
import java.util.Objects;

final class Pong implements Serializable {
    private static final long serialVersionUID = 114L;

    @SuppressWarnings("WeakerAccess")
    public int value;

    Pong(final int value) {
        this.value = value;
    }

    @SuppressWarnings("unused")
    public Pong() {

    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Pong pong = (Pong) o;
        return value == pong.value;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public final String toString() {
        return String.format("Pong{%d}", value);
    }
}
