package hotstu.github.passkeeper.widget;

import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

/**
 * @author hglf
 * @since 2018/1/23
 */
public class CompositeObserver implements Observer<Object> {
    public interface Tagable {
        default String getTag() {
            return getClass().getCanonicalName();
        }
    }
    @Override
    public void onChanged(@Nullable Object o) {

    }
}
