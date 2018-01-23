package hotstu.github.passkeeper.widget;

import org.junit.Test;

/**
 * @author hglf
 * @since 2018/1/23
 */
public class CompositeObserverTest {
    @Test
    public void test1() {
        MyTestClass o = new MyTestClass();
        System.out.println(o.getTag());
        assert  o.getClass().getCanonicalName().equals(o.getTag());
    }
}