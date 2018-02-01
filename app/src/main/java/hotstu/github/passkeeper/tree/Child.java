package hotstu.github.passkeeper.tree;


public class Child implements Node {
    private Parent parentNode = null;

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Node findItem(int index) {
        if (index == 0) {
            return this;
        }
        else {
            throw new IndexOutOfBoundsException("child not index shall never larger that 0");
        }
    }

    @Override
    public int lookforItem(Node item) {
        return item.equals(this) ? 0 : -1;
    }


    @Override
    public void setParent(Parent parent) {
        this.parentNode = parent;
    }

    @Override
    public Parent getParent() {
        return parentNode;
    }

    @Override
    public int getIndent() {
        if(this.parentNode == null)
            return 0;
        else
            return this.parentNode.getIndent() + 1;
    }
}
