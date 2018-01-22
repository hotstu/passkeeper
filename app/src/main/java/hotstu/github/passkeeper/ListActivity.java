package hotstu.github.passkeeper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import hotstu.github.passkeeper.databinding.ActivityListBinding;
import hotstu.github.passkeeper.databinding.FormItemBinding;
import hotstu.github.passkeeper.db.HostEntity;
import hotstu.github.passkeeper.db.UserEntity;
import hotstu.github.passkeeper.tree.Child;
import hotstu.github.passkeeper.tree.Node;
import hotstu.github.passkeeper.tree.Parent;
import hotstu.github.passkeeper.viewmodel.ListViewModel;
import hotstu.github.passkeeper.widget.AdapterCallback;
import hotstu.github.passkeeper.widget.TreeAdapter;

public class ListActivity extends AppCompatActivity {
    private static final String TAG = "ListActivity";

    TreeAdapter<VH, Item> mAdapter;
    private ActivityListBinding binding;
    private ListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_list);
        viewModel = ViewModelProviders.of(this, Injection.getViewModelFactory(this)).get(ListViewModel.class);
        binding.setVariable(BR.viewModel, viewModel);
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
                return new VH(binding.getRoot(), binding);
            }

            @Override
            public void onBindViewHolder(TreeAdapter<VH, Item> adapter, VH holder, int position) {
                holder.bindData(adapter.getItem(position));
            }

            @Override
            public int getItemViewType(TreeAdapter<VH, Item> adapter, int position) {
                return adapter.getItem(position).isLeaf()? 0: 1;
            }

            @Override
            public long getItemId(TreeAdapter<VH, Item> adapter, int position) {
                return 0;
            }
        });

        binding.list.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        Drawable drawable =  ContextCompat.getDrawable(this, R.drawable.divider);
        dividerItemDecoration.setDrawable(drawable);
        suscribeData();
    }

    void suscribeData() {

        viewModel.getItems().observe(this, new Observer<List<Item>>() {
            @Override
            public void onChanged(@Nullable List<Item> items) {
                mAdapter.clearDataSet();
                for (int i = 0; i < items.size(); i++) {
                    Item item = items.get(i);
                    if (item instanceof HostItem) {

                        HostItem host = (HostItem) item;
                        if(host.getChildCount() == 0 ){
                            host.addChild(new UserItem(new UserEntity(0, "+", 0, 0)), 0);
                        } else {
                            UserItem user = (UserItem) host.findItem(1);
                            if (user.getData().id != 0) {
                                host.addChild(new UserItem(new UserEntity(0, "+", 0, 0)), 0);
                            }

                        }
                    }
                }
                mAdapter.setDataSet(items);
            }
        });

        viewModel.addParentEvent.observe(this, new Observer<Item>() {
            @Override
            public void onChanged(@Nullable Item item) {
                showAddParent();
            }
        });

        viewModel.addChildEvent.observe(this, new Observer<Item>() {
            @Override
            public void onChanged(@Nullable Item item) {
                showAddUser((UserItem) item);
            }
        });

        viewModel.errToastEvent.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                Toast.makeText(ListActivity.this, s, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.nomalToastEvent.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                Toast.makeText(ListActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.deleteEvent.observe(this, new Observer<Item>() {
            @Override
            public void onChanged(@Nullable Item item) {
                showDeleteDialog(item);
            }
        });

        viewModel.showPwdEvent.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                generatePwd(s);
            }
        });

        viewModel.recreateEvent.observe(this, new Observer<Object>() {
            @Override
            public void onChanged(@Nullable Object o) {
                recreate();
            }
        });
    }


    public interface Item<T> extends Node {
        void setData(T data);
        T getData();
        String getText();
    }

    public static class HostItem extends Parent implements Item<HostEntity> {
        private HostEntity data;

        public HostItem() {
        }
        public HostItem(HostEntity data) {
            this.data = data;
        }
        @Override
        public void setData(HostEntity data) {
            this.data = data;
        }

        @Override
        public HostEntity getData() {
            return data;
        }

        @Override
        public String getText() {
            return data.hostname;
        }
    }

    public static class UserItem extends Child implements Item<UserEntity> {
        private UserEntity data;
        public UserItem() {
        }
        public UserItem(UserEntity data) {
            this.data = data;
        }
        @Override
        public void setData(UserEntity data) {
            this.data = data;
        }

        @Override
        public UserEntity getData() {
            return data;
        }

        @Override
        public String getText() {
            return data.username;
        }
    }

    AdapterCallback adapterCallback = new AdapterCallback() {

        @Override
        public void onClick(Node item) {
            if(!item.isLeaf()) {
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

    class VH extends RecyclerView.ViewHolder {

        final ViewDataBinding binding;
        public VH(View itemView, ViewDataBinding binding) {
            super(itemView);
            this.binding = binding;
        }

        public void bindData(Item item) {
            binding.setVariable(BR.item, item);
            binding.setVariable(BR.adapterCallback, adapterCallback);
            binding.executePendingBindings();
        }
    }

    public void showAddParent() {
        LayoutInflater inflater = LayoutInflater.from(ListActivity.this);
        View promptView = inflater.inflate(R.layout.form, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ListActivity.this);
        final EditText et = promptView.findViewById(R.id.form_et1);
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                viewModel.addHost(et.getText().toString());
                            }

                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
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
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final HostItem host = ((HostItem) child.getParent());
                        final String  username = dialogBinding.formUserEt.getText().toString();
                        final int   pwdlenth = viewModel.seekbarValue.get() + 6;
                        viewModel.addUser(username, pwdlenth, host.getData().id);
                    }

                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    public boolean showDeleteDialog(final Item item) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Warning")
                    .setMessage("do you want delete: " + item.getText())
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    viewModel.performDeleteAction(item);
                            }})
                    .setNegativeButton("No", null).show();
        return true;

    }

    private void generatePwd(String pwdStr) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View promptView = inflater.inflate(R.layout.puretext_dialog, null);
        final TextView tv1 = (TextView) promptView.findViewById(R.id.pwd_tv);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.backup == item.getItemId()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return true;
            }
            backup();
            return true;
		}
		if (R.id.restore == item.getItemId()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return true;
            }
			restore();
			return true;
		}
		if(R.id.export == item.getItemId()){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
                return true;
            }
			export();
			return true;
		}
        return super.onOptionsItemSelected(item);
	}


	@RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private void restore() {
        // FIXME 兼容老的数据库
        viewModel.restore();
	}

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	private void export(){
        viewModel.export();
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case 1:
                    backup();
                    break;
                case 2:
                    restore();
                    break;
                case 3:
                    export();
                    break;
                default:
                    break;
            }
        }

    }
}
