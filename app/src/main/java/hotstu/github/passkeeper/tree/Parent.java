package hotstu.github.passkeeper.tree;

import android.support.v7.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

public class Parent implements Node {
    final private LinkedList<Node> children;
    private boolean open = false;
    private Parent parentNode = null;

    public Parent() {
        children = new LinkedList<>();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public int getCount() {
        return (open? getChildCount()  : 0) + 1;
    }

    public int getChildCount() {
        int count = 0;
        for (int i = 0; i < children.size(); i++) {
            count += children.get(i).getCount();
        }
        return count;
    }

    public void addChildren(List<? extends Node> items) {
        if (items != null && items.size() > 0) {
            for (int i = 0; i < items.size(); i++) {
                items.get(i).setParent(this);
            }
            children.addAll(items);
        }
    }

    public void addChild(Node item, int position) {
        if(position > children.size()){
            throw new IndexOutOfBoundsException("position = " + position + "current len=" + children.size());
        }
        if (position < 0) {
            children.addLast(item);
        } else {
            children.add(position, item);
        }
        item.setParent(this);
    }

    public void clear() {
        this.children.clear();
    }

    @Override
    public Node findItem(final int index) {
        if(index == 0)
            return this;
        else {
            int swapIndex = index - 1;
            for (int i = 0; i < this.children.size(); i++) {
                Node child = this.children.get(i);
                final int childCount = child.getCount();
                if (swapIndex < childCount) {
                    return child.findItem(swapIndex);
                }
                swapIndex -= childCount;
            }
        }
        final int currentCount = this.getCount();
        throw new IndexOutOfBoundsException("Count=" + currentCount + ",index=" + index);
    }

    @Override
    public int lookforItem(Node item) {
        if(item.equals(this))
            return 0;
        else {
            if (isOpen() ) {
                int swap = 0;
                int i = 0;
                for (; i < children.size(); i++) {
                    int ret = children.get(i).lookforItem(item);
                    if (ret >= 0) {
                        return swap + 1 + ret;
                    }
                    swap += children.get(i).getCount();
                }
            }
        }
        return RecyclerView.NO_POSITION;
    }

    public List<Node> getChildren() {
        return children;
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

    public void open() {
        open = true;
    }

    public void close() {
        open = false;
    }

}
