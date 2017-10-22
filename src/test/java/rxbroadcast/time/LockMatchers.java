package rxbroadcast.time;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.Lock;

final class LockMatchers {
    private LockMatchers() {
        // No instances pls
    }

    @NotNull
    static Matcher<Lock> isUnlocked() {
        return new UnlockedLockMatcher();
    }
}
