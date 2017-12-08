package hotstu.github.passkeeper.tree;

public interface Node {
    /**
     * @return true if is solid endpoint node
     */
    boolean isLeaf();

    /**
     *
     * @return the number of self  children,ie children.lenth + 1,
     */
    int getCount();

    /**
     *
     * @param index [0, getCount())
     * @return self or descendants
     */
    Node findItem(int index);

    int lookforItem(Node item);


    void setParent(Parent parent);

    Parent getParent();
    int getIndent();
}
