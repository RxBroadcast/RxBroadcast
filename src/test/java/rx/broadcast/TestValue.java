package rx.broadcast;

import java.util.Objects;

class TestValue {
    public int value;

    public TestValue(final int value) {
        this.value = value;
    }

    @SuppressWarnings("unused")
    public TestValue() {

    }

    @Override
    public boolean equals(final Object o) {
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
    public int hashCode() {
        return Objects.hash(value);
    }
}
