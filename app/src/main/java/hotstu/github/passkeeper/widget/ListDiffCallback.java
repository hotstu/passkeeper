package hotstu.github.passkeeper.widget;

import android.support.v7.util.DiffUtil;

import java.util.LinkedList;
import java.util.List;

import hotstu.github.passkeeper.tree.Node;
import hotstu.github.passkeeper.tree.Parent;
import hotstu.github.passkeeper.vo.Item;

/**
 * @author hglf
 * @since 2018/1/24
 */
public class ListDiffCallback extends DiffUtil.Callback {
    final ParentWrapper origin;
    final ParentWrapper newData;

    public ListDiffCallback(List<Item> origin, List<Item> newData) {
        this.origin = new ParentWrapper(origin);
        this.newData = new ParentWrapper(newData);
    }

    @Override
    public int getOldListSize() {
        return origin.getCount();
    }

    @Override
    public int getNewListSize() {
        return newData.getCount();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return areItemsTheSame(((Item) origin.findItem(oldItemPosition)),
                ((Item) newData.findItem(newItemPosition)));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return areContentsTheSame(((Item) origin.findItem(oldItemPosition)),
                ((Item) newData.findItem(newItemPosition)));
    }

     boolean areItemsTheSame(Item oldItem, Item newItem) {
        return oldItem.equals(newItem);
    }

     boolean areContentsTheSame(Item oldItem, Item newItem) {
        if (!oldItem.isLeaf() && !newItem.isLeaf()
                && ((Parent) oldItem).isOpen() != ((Parent) newItem).isOpen()) {
            return false;
        }
        if (oldItem.getText() == null ) {
            return newItem.getText() == null;
        }
        return oldItem.getText().equals(newItem.getText());
    }

    private static final class ParentWrapper {
        private LinkedList<Node> children;

        public ParentWrapper(List<? extends Node> items) {
            addChildren(items);
        }

        public int getCount() {
                if(children == null)
                    return 0;
                int count = 0;
                for (int i = 0; i < children.size(); i++) {
                    count += children.get(i).getCount();
                }
                return count;
        }

        public void addChildren(List<? extends Node> items) {
            if (children == null) {
                children = new LinkedList<>();
            }
            if (items != null) {
                children.addAll(items);
            }
        }

        public Node findItem(int index) {
            if(children != null){
                int swapIndex = index;
                for (int i = 0; i < children.size(); i++) {
                    Node child = children.get(i);
                    final int childCount = child.getCount();
                    if (swapIndex < childCount) {
                        return child.findItem(swapIndex);
                    }
                    swapIndex -= childCount;
                }
            }
            final int currentCount = getCount();
            throw new IndexOutOfBoundsException("Count=" + currentCount + ",index=" + index);
        }


    }
}
