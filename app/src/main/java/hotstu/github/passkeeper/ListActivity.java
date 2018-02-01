package hotstu.github.passkeeper;

import android.Manifest;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hotstu.github.passkeeper.databinding.ActivityListBinding;
import hotstu.github.passkeeper.databinding.FormItemBinding;
import hotstu.github.passkeeper.db.HostEntity;
import hotstu.github.passkeeper.db.UserEntity;
import hotstu.github.passkeeper.tree.Node;
import hotstu.github.passkeeper.tree.Parent;
import hotstu.github.passkeeper.tree.TreeAdapter;
import hotstu.github.passkeeper.viewmodel.ListViewModel;
import hotstu.github.passkeeper.vo.HostItem;
import hotstu.github.passkeeper.vo.Item;
import hotstu.github.passkeeper.vo.UserItem;
import hotstu.github.passkeeper.vo.VH;
import hotstu.github.passkeeper.widget.AdapterCallback;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ListActivity extends AppCompatActivity {
    private static final String TAG = "ListActivity";

    TreeAdapter<VH, Item> mAdapter;
    private ActivityListBinding binding;
    private ListViewModel viewModel;
    private RxPermissions rxPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxPermissions = new RxPermissions(this);
        rxPermissions.setLogging(true);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_list);
        viewModel = ViewModelProviders.of(this, Injection.getViewModelFactory(this)).get(ListViewModel.class);
        binding.setViewModel(viewModel);
        binding.list.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });

        mAdapter = new TreeAdapter<>(new TreeAdapter.AdapterDelegate<VH, Item>() {
            @Override
            public VH onCreateViewHolder(TreeAdapter<VH, Item> adapter, ViewGroup parent, int viewType) {
                int resId = viewType == 0 ? R.layout.list_item : R.layout.list_group;
                ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), resId, parent, false);
                return new VH(binding.getRoot(), binding, adapterCallback);
            }

            @Override
            public void onBindViewHolder(TreeAdapter<VH, Item> adapter, VH holder, int position) {
                holder.bindData(adapter.getItem(position));
            }

            @Override
            public int getItemViewType(TreeAdapter<VH, Item> adapter, int position) {
                return adapter.getItem(position).isLeaf() ? 0 : 1;
            }

            @Override
            public long getItemId(TreeAdapter<VH, Item> adapter, int position) {
                return 0;
            }

        });

        binding.list.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.divider);
        dividerItemDecoration.setDrawable(drawable);
        suscribeData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.btnAddHost.setScaleX(0);
        binding.btnAddHost.setScaleY(0);
        binding.btnAddHost.animate()
                .setStartDelay(1000)
                .setDuration(500)
                .setInterpolator(new BounceInterpolator())
                .scaleX(1)
                .scaleY(1)
                .start();
    }

    void suscribeData() {
        Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(this, viewModel.getItems()))
                .debounce(500, TimeUnit.MILLISECONDS)
                .flatMap(items -> {
                    List<Item> list = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        Pair<HostEntity, SparseArray<UserEntity>> p = items.get(items.keyAt(i));
                        //使用套娃结构保持item的用户操作状态
                        HostItem host = viewModel.itemProvider.obtainHost(p.first.id);
                        host.setData(p.first);
                        SparseArray<UserEntity> second = p.second;
                        host.clear();
                        UserItem userItem = viewModel.itemProvider.obtainUser(0, host.getId());
                        userItem.setData(new UserEntity(0, "+", 0,  host.getId()));
                        host.addChild(userItem,-1);
                        for (int j = 0; j < second.size(); j++) {
                            UserEntity userEntity = second.get(second.keyAt(j));
                            UserItem user = viewModel.itemProvider.obtainUser(userEntity.id, userEntity.hostId);
                            user.setData(userEntity);
                            host.addChild(user, -1);
                        }
                        list.add(host);
                    }
                    //DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ListDiffCallback(mAdapter.getDataSet(), list));
                    return Observable.just(new Pair<>(list, null));
                })
                .observeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    Log.d(TAG, "list = " + pair.first.size());

                    mAdapter.setDataSet(pair.first);

                    //pair.second.dispatchUpdatesTo(mAdapter);
                });

        viewModel.addParentEvent.observe(this, aVoid -> showAddParent());

        viewModel.addChildEvent.observe(this, item -> showAddUser((UserItem) item));

        viewModel.errToastEvent.observe(this, s -> Toast.makeText(ListActivity.this, s, Toast.LENGTH_LONG).show());

        viewModel.nomalToastEvent.observe(this, s -> Toast.makeText(ListActivity.this, s, Toast.LENGTH_SHORT).show());

        viewModel.deleteEvent.observe(this, this::showDeleteDialog);

        viewModel.showPwdEvent.observe(this, this::generatePwd);

        viewModel.recreateEvent.observe(this, aVoid -> recreate());
    }


    AdapterCallback adapterCallback = new AdapterCallback() {

        @Override
        public void onClick(Node item) {
            if (!item.isLeaf()) {
                Parent parent = (Parent) item;
                mAdapter.toggle(parent);
            } else {
                final UserItem child = (UserItem) item;
                if (child.getData().id <= 0) {
                    viewModel.openAddChild(child);
                } else {
                    viewModel.generatePwd(child);
                }
            }
        }

        @Override
        public boolean onLongClick(Node item) {
            viewModel.showDeleteDialog((Item) item);
            return true;
        }
    };

    public void showAddParent() {
        LayoutInflater inflater = LayoutInflater.from(ListActivity.this);
        View promptView = inflater.inflate(R.layout.form, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ListActivity.this);
        final EditText et = promptView.findViewById(R.id.form_et1);
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        (dialog, which) -> viewModel.addHost(et.getText().toString()))
                .setNegativeButton("Cancel",
                        (dialog, which) -> dialog.cancel());
        AlertDialog diaolog = alertDialogBuilder.create();
        diaolog.show();
    }


    void showAddUser(final UserItem child) {
        LayoutInflater inflater = LayoutInflater.from(ListActivity.this);
        final FormItemBinding dialogBinding = FormItemBinding.inflate(inflater);
        dialogBinding.setViewModel(viewModel);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ListActivity.this);
        alertDialogBuilder.setView(dialogBinding.getRoot());
        alertDialogBuilder.setPositiveButton("OK",
                (dialog, which) -> {
                    final HostItem host = ((HostItem) child.getParent());
                    final String username = dialogBinding.formUserEt.getText().toString();
                    final int pwdlenth = viewModel.seekbarValue.get() + 6;
                    viewModel.addUser(username, pwdlenth, host.getData().id);
                }).setNegativeButton("Cancel",
                (dialog, which) -> dialog.cancel());
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    public boolean showDeleteDialog(final Item item) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Warning")
                .setMessage("do you want delete: " + item.getText())
                .setPositiveButton("Yes",
                        (dialog, which) -> viewModel.performDeleteAction(item))
                .setNegativeButton("No", null).show();
        return true;

    }

    private void generatePwd(String pwdStr) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View promptView = inflater.inflate(R.layout.puretext_dialog, null);
        final TextView tv1 = promptView.findViewById(R.id.pwd_tv);
        tv1.setText(pwdStr);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        System.exit(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .filter(aBoolean -> aBoolean)
                .flatMap(aBoolean -> Observable.just(item.getItemId()))
                .subscribe(id -> {
                    if (R.id.backup == id) {
                        backup();
                    }
                    if (R.id.restore == id) {
                        viewModel.restore();
                    }
                    if (R.id.export == id) {
                        viewModel.export();
                    }
                }, Throwable::printStackTrace);

        return true;
    }


    private void backup() {
        String src = getDatabasePath("passkeeper.db").getAbsolutePath();
        Intent intent = new Intent();

        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", new File(src));
        Log.d(TAG, uri.toString());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_SEND)
                .setType("*/*")
                .putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{PassKeepApp.sInstance.getEmail()})
                .putExtra(android.content.Intent.EXTRA_SUBJECT, "dbbackup" + System.currentTimeMillis())
                .putExtra(Intent.EXTRA_STREAM, uri);

        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "出错: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


}
