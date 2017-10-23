package rxbroadcast.integration.pp;

import java.io.Serializable;
import java.util.Objects;

public final class Ping implements Serializable {
    private static final long serialVersionUID = 114L;

    @SuppressWarnings("WeakerAccess")
    public int value;

    Ping(final int value) {
        this.value = value;
    }

    @Deprecated
    @SuppressWarnings("unused")
    public Ping() {

    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Ping ping = (Ping) o;
        return value == ping.value;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public final String toString() {
        return String.format("Ping{%d}", value);
    }
}
