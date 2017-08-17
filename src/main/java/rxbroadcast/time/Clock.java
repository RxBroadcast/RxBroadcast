package rxbroadcast.time;

import java.util.function.LongFunction;

/**
 * A clock used to generate timestamps for a message.
 */
public interface Clock {
    /**
     * Executes a single tick of the clock, generating a timestamp and calling
     * the given ticker function with the timestamp and returning the result.
     * @param ticker a function of a timestamp
     * @param <T> the type of the result of the ticker
     * @return the result of the ticker function
     */
    <T> T tick(LongFunction<T> ticker);
}
