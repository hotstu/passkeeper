package hotstu.github.passkeeper.widget;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

import hotstu.github.passkeeper.tree.Node;
import hotstu.github.passkeeper.tree.Parent;

public class TreeAdapter<VH extends RecyclerView.ViewHolder, T extends Node> extends RecyclerView.Adapter {
    /**
     * @param <VH> the type of the view holder
     * @param <T>  the type on Adapter's item model
     */
    public interface AdapterDelegate<VH extends RecyclerView.ViewHolder, T extends Node> {
        VH onCreateViewHolder(TreeAdapter<VH, T> adapter, ViewGroup parent, int viewType);

        void onBindViewHolder(TreeAdapter<VH, T> adapter, VH holder, int position);

        int getItemViewType(TreeAdapter<VH, T> adapter, int position);

        /**
         * 当设置hasStableIds为true时被调用， 可以优化性能，如果不知道是什么返回默认值即可
         **/
        long getItemId(TreeAdapter<VH, T> adapter, int position);

    }

    ParentWapper mNodeContainer;
    AdapterDelegate<VH, T> delegate;


    public TreeAdapter(AdapterDelegate<VH, T> delegate) {
        this.delegate = delegate;
        this.mNodeContainer = new ParentWapper();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return delegate.onCreateViewHolder(this, parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        delegate.onBindViewHolder(this, (VH) holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        return delegate.getItemViewType(this, position);
    }

    @Override
    public int getItemCount() {
        return mNodeContainer.getCount();
    }

    public final void setDataSet(List<? extends T> list) {
        mNodeContainer.clear();
        mNodeContainer.addChildren(list);
        notifyDataSetChanged();
    }

    public final void clearDataSet() {
        mNodeContainer.clear();
        notifyItemRangeRemoved(0, mNodeContainer.getCount());
    }

    /**
     *
     * @param parent null if add to root
     * @param item
     * @param relativePosition 在parent中的位置
     */
    public final void addItem(Parent parent, Node item, int relativePosition) {
        if (parent == null) {
            parent = mNodeContainer;
        }
        parent.addChild(item, relativePosition);
        int index = mNodeContainer.lookforItem(item);
        if (index >= 0) {
            notifyItemRangeInserted(index, 1);
        }
    }

    public final void removeItem(int position) {
        Node item = mNodeContainer.findItem(position);
        if (item != null &&  item.getParent() != null) {
            item.getParent().removeChild(item);
            notifyItemRangeRemoved(position, item.getCount());
        }
    }

    public final void removeItem(Node item) {
        int position = mNodeContainer.lookforItem(item);
        if (item != null &&  item.getParent() != null) {
            item.getParent().removeChild(item);
            if (position != RecyclerView.NO_POSITION) {
                notifyItemRangeRemoved(position, item.getCount());
            }
        }

    }

    @Override
    public long getItemId(int position) {
        return delegate.getItemId(this, position);
    }

    public final T getItem(int position) {
        return (T) mNodeContainer.findItem(position);
    }

    public void open(Parent item, int position) {
        if(item == mNodeContainer)
            return;
        item.open();
        notifyItemChanged(position);
        notifyItemRangeInserted(position + 1, item.getChildCount());
    }

    public void close(Parent item, int position) {
        if(item == mNodeContainer)
            return;
        item.close();
        notifyItemChanged(position);
        notifyItemRangeRemoved(position+ 1, item.getChildCount());
    }

    public void toggle(Parent item, int position) {
        if (item.isOpen()) {
            //close, et remove children
            close(item, position);
        } else {
            //open, et add children
            open(item, position);
        }
    }

    private static final class ParentWapper extends Parent {
        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public int getCount() {
            return getChildCount();
        }

        @Override
        public Node findItem(int index) {
            if(this.children != null){
                int swapIndex = index;
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
            if ( children != null) {
                int swap = 0;
                int i = 0;
                for (; i < children.size(); i++) {
                    int ret = children.get(i).lookforItem(item);
                    if (ret >= 0) {
                        return swap + ret;
                    }
                    swap += children.get(i).getCount();
                }
            }
            return RecyclerView.NO_POSITION;
        }

    }

}

