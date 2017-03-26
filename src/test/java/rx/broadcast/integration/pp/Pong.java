package rx.broadcast.integration.pp;

import java.util.Objects;

public final class Pong {
    @SuppressWarnings("WeakerAccess")
    public int value;

    public Pong(final int value) {
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
