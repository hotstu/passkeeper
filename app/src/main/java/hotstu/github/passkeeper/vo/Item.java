package hotstu.github.passkeeper.vo;

import hotstu.github.passkeeper.tree.Node;

public interface Item<T> extends Node {
        void setData(T data);
        T getData();
        String getText();
        int getId();
    }