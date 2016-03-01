package rx.broadcast.time;

import java.util.function.LongFunction;

public interface Clock {
    <T> T tick(LongFunction<T> ticker);
}
