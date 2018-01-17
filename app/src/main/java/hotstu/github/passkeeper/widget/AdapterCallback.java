package hotstu.github.passkeeper.widget;

import hotstu.github.passkeeper.tree.Node;

/**
 * @author hglf
 * @since 2018/1/17
 */
public interface AdapterCallback {
    void onClick(Node item);
    boolean onLongClick(Node item);
}
