package hotstu.github.passkeeper.widget;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

import hotstu.github.passkeeper.tree.Node;


public class CompositeDelegate implements TreeAdapter.AdapterDelegate {
    public static abstract class TypeBaseDelegate<VH extends RecyclerView.ViewHolder, T extends Node> implements TreeAdapter.AdapterDelegate<VH, T> {
        public abstract boolean isForType(Class<?> clazz);
    }

    private List<TypeBaseDelegate> delegates;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(TreeAdapter adapter, ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(TreeAdapter adapter, RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemViewType(TreeAdapter adapter, int position) {
        return 0;
    }

    @Override
    public long getItemId(TreeAdapter adapter, int position) {
        return 0;
    }
}
