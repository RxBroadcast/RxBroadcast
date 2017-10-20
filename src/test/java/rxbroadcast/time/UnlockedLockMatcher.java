package rxbroadcast.time;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.concurrent.locks.Lock;

final class UnlockedLockMatcher extends BaseMatcher<Lock> {
    @Override
    public final boolean matches(final Object o) {
        final Lock lock = (Lock) o;
        if (lock.tryLock()) {
            lock.unlock();
            return true;
        }

        return false;
    }

    @Override
    public final void describeTo(final Description description) {
        description.appendText("Lock#tryLock to be true");
    }

    @Override
    public final void describeMismatch(final Object item, final Description description) {
        description.appendText("Lock#tryLock returned false");
    }
}
