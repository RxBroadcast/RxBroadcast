package rx.broadcast.time;

import java.util.function.Function;

public interface Clock {
    <T> T tick(final Function<Long, T> ticker);
}
