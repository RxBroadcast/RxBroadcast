package rxbroadcast;

import java.io.Serializable;
import java.util.Objects;

public final class TestValue implements Serializable {
    private static final long serialVersionUID = 114L;

    @SuppressWarnings("WeakerAccess")
    public int value;

    public TestValue(final int value) {
        this.value = value;
    }

    @Deprecated
    @SuppressWarnings("unused")
    public TestValue() {

    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TestValue testValue = (TestValue) o;
        return value == testValue.value;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public final String toString() {
        return String.format("TestValue{%d}", value);
    }
}
