package hotstu.github.passkeeper.widget;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hotstu.github.passkeeper.R;
import hotstu.github.passkeeper.tree.Child;
import hotstu.github.passkeeper.tree.Node;
import hotstu.github.passkeeper.tree.NodeVH;
import hotstu.github.passkeeper.tree.Parent;

public class TestActivity extends AppCompatActivity {
    RecyclerView list;
    TreeAdapter.AdapterDelegate<VH, Node> delegate;
    TreeAdapter<VH, Node> mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);
        list = (RecyclerView) findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        };
        list.setLayoutManager(layoutManager);
        delegate = new TreeAdapter.AdapterDelegate<VH, Node>() {
            @Override
            public VH onCreateViewHolder(TreeAdapter<VH, Node> adapter, ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());

                if(viewType == 1) {
                    return new VH(inflater.inflate(R.layout.list_item_node_parent, parent, false));
                } else {
                    return new VH(inflater.inflate(android.R.layout.simple_list_item_1, parent, false));
                }
            }

            @Override
            public void onBindViewHolder(TreeAdapter<VH, Node> adapter, VH holder, int position) {
                holder.bindData(adapter.getItem(position));
            }

            @Override
            public int getItemViewType(TreeAdapter<VH, Node> adapter, int position) {
                Node node = adapter.getItem(position);
                return node.isLeaf() ? 0 : 1;
            }

            @Override
            public long getItemId(TreeAdapter<VH, Node> adapter, int position) {
                return 0;
            }
        };
        mAdapter = new TreeAdapter<>(delegate);
        List<Node> datas = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Parent p = new Parent();
            List<Node> children = new ArrayList<>();
            for (int i1 = 0; i1 < 4; i1++) {
                children.add(new Child());
            }
            p.addChildren(children);
            datas.add(p);
        }
        mAdapter.setDataSet(datas);
        list.setAdapter(mAdapter);
        //list.setItemAnimator(new LandingAnimator());
        float dimension = getResources().getDimension(R.dimen.activity_horizontal_margin);
        list.addItemDecoration(new IndentDecoration<VH>((int) dimension*2) );
    }

    public class VH extends RecyclerView.ViewHolder implements View.OnClickListener, NodeVH {

        TextView tv;
        Node item;
        public VH(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tv = (TextView) itemView.findViewById(android.R.id.text1);
        }

        public void bindData(Node item) {
            this.item = item;
            tv.setText((item.isLeaf()? "child": "parent"));
        }

        @Override
        public void onClick(View v) {
            int position = list.getChildAdapterPosition(v);
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            Node item = mAdapter.getItem(position);
            if(!item.isLeaf()) {
                Parent parent = (Parent) item;
                mAdapter.toggle(parent, position);
            }
        }

        @Override
        public Node getNode() {
            return this.item;
        }
    }
}
