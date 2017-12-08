package hotstu.github.passkeeper.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import hotstu.github.passkeeper.tree.Node;
import hotstu.github.passkeeper.tree.NodeVH;

/**
 * Created by hotstuNg on 2016/8/17.
 */
public class IndentDecoration<VH extends NodeVH> extends RecyclerView.ItemDecoration {
    private int widthPixel  = 0;

    public IndentDecoration(int widthPixel) {
        this.widthPixel = widthPixel;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        VH vh = (VH) parent.getChildViewHolder(view);
        Node node = vh.getNode();
        outRect.left = widthPixel * (node.getIndent() - 1);

        //如果view的高度达不到要的宽度，需要加上一些padding
        //TODO 无法获取view的高宽，因为没有测量
    }
}
