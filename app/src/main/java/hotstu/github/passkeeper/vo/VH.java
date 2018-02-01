package hotstu.github.passkeeper.vo;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import hotstu.github.passkeeper.BR;
import hotstu.github.passkeeper.widget.AdapterCallback;

public class VH extends RecyclerView.ViewHolder {

        final ViewDataBinding binding;
    final AdapterCallback adapterCallback;
        public VH(View itemView, ViewDataBinding binding, AdapterCallback adapterCallback) {
            super(itemView);
            this.binding = binding;
            this.adapterCallback = adapterCallback;
        }

        public void bindData(Item item) {
            //FIXME 将item传入会有问题
            binding.setVariable(BR.item, item);
            binding.setVariable(BR.adapterCallback, adapterCallback);
            binding.executePendingBindings();
        }
    }