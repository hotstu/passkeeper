package hotstu.github.passkeeper.tree;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class TreeAdapter<VH extends RecyclerView.ViewHolder, T extends Node> extends RecyclerView.Adapter {

    private static final String TAG = TreeAdapter.class.getSimpleName();

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

    ParentWrapper mNodeContainer;
    AdapterDelegate<VH, T> delegate;

    public TreeAdapter(AdapterDelegate<VH, T> delegate) {
        super();
        this.delegate = delegate;
        this.mNodeContainer = new ParentWrapper();
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

    public final void setDataSetQuietly(List<? extends T> list) {
        mNodeContainer.clear();
        mNodeContainer.addChildren(list);
    }

    public List<T> getDataSet() {
        return (List<T>) mNodeContainer.children;
    }

    public final void clearDataSet() {
        mNodeContainer.clear();
        notifyItemRangeRemoved(0, mNodeContainer.getCount());
    }


    @Override
    public long getItemId(int position) {
        return delegate.getItemId(this, position);
    }

    public final T getItem(int position) {
        return (T) mNodeContainer.findItem(position);
    }

    public void open(Parent item, int position) {
        Log.e(TAG, "open:" + position);
        item.open();
        notifyItemChanged(position);
        notifyItemRangeInserted(position + 1, item.getChildCount());
    }

    public void close(Parent item, int position) {
        Log.e(TAG, "close:" + position);
        item.close();
        notifyItemChanged(position);
        notifyItemRangeRemoved(position + 1, item.getChildCount());
    }

    public void toggle(Parent item) {
        int position = mNodeContainer.lookforItem(item);
        if (item.isOpen()) {
            //close, et remove children
            close(item, position);
        } else {
            //open, et add children
            open(item, position);
        }
    }

    private static final class ParentWrapper {
        private final ArrayList<Node> children;

        ParentWrapper() {
            children = new ArrayList<>();
        }

        int getCount() {
            int count = 0;
            for (int i = 0; i < children.size(); i++) {
                count += children.get(i).getCount();
            }
            return count;
        }

        void addChildren(List<? extends Node> items) {
            if (items != null) {
                children.addAll(items);
            }
        }

        int lookforItem(Node item) {
            int swap = 0;
            int i = 0;
            for (; i < children.size(); i++) {
                int ret = children.get(i).lookforItem(item);
                if (ret >= 0) {
                    return swap + ret;
                }
                swap += children.get(i).getCount();
            }
            return RecyclerView.NO_POSITION;
        }

        Node findItem(int index) {
            int swapIndex = index;
            for (int i = 0; i < children.size(); i++) {
                Node child = children.get(i);
                final int childCount = child.getCount();
                if (swapIndex < childCount) {
                    return child.findItem(swapIndex);
                }
                swapIndex -= childCount;
            }
            final int currentCount = getCount();
            throw new IndexOutOfBoundsException("Count=" + currentCount + ",index=" + index);
        }


        public void clear() {
            children.clear();
        }
    }
}

