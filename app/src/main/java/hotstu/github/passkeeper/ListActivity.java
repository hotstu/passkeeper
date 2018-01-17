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
import android.os.Environment;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import hotstu.github.passkeeper.databinding.ActivityListBinding;
import hotstu.github.passkeeper.db.HostEntity;
import hotstu.github.passkeeper.db.UserEntity;
import hotstu.github.passkeeper.tree.Child;
import hotstu.github.passkeeper.tree.Node;
import hotstu.github.passkeeper.tree.Parent;
import hotstu.github.passkeeper.viewmodel.ListViewModel;
import hotstu.github.passkeeper.widget.AdapterCallback;
import hotstu.github.passkeeper.widget.TreeAdapter;
import io.reactivex.observers.DisposableObserver;

public class ListActivity extends AppCompatActivity {
    private static final String TAG = "ListActivity";

    TreeAdapter<VH, Item> mAdapter;
    private ActivityListBinding binding;
    private ListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_list);
        if (PassKeepApp.sInstance.isStale()) {
            Intent i=  new Intent(this, WatchdogActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }
        viewModel = ViewModelProviders.of(this).get(ListViewModel.class);
        binding.btnAddHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddParent();
            }
        });
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
                UserItem child = (UserItem) item;
                if (child.getData().id <= 0) {
                    showAddUser(child);
                } else {
                    generatePwd(child);
                }
            }
        }

        @Override
        public boolean onLongClick(Node item) {
            return onChildLongClick((Item) item);
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
                                String hostname = et.getText().toString();
                                if ("".equals(hostname)) {
                                    Toast.makeText(ListActivity.this, "error", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if ( (Injection.getDataBase().HostModel().addHost(new HostEntity(0, hostname))) > 0) {
                                    Toast.makeText(ListActivity.this, "succeed", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(ListActivity.this, "failed", Toast.LENGTH_LONG).show();
                                }

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


    void showAddUser(UserItem child) {
        final HostItem host = ((HostItem) child.getParent());
        LayoutInflater inflater = LayoutInflater.from(ListActivity.this);
        View promptView = inflater.inflate(R.layout.form_item, null);
        final EditText et1 = promptView.findViewById(R.id.form_user_et);
        final EditText et2 = promptView.findViewById(R.id.form_user_et2);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ListActivity.this);
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String username;
                        int pwdlenth;
                        try {
                            username = et1.getText().toString();
                            pwdlenth = Integer.parseInt(et2.getText().toString());
                        } catch (Exception e) {
                            Toast.makeText(ListActivity.this, "bad input", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if ("".equals(username) || pwdlenth < 6 || pwdlenth > 12) {
                            Toast.makeText(ListActivity.this, "bad input", Toast.LENGTH_LONG).show();
                            return;
                        }
                       long id = Injection.getDataBase().UserModel().addUser(new UserEntity(0, username, pwdlenth, host.getData().id));
                        if (id > 0) {
                            Toast.makeText(ListActivity.this, "succeed", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ListActivity.this, "failed", Toast.LENGTH_LONG).show();
                        }

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

    public boolean onChildLongClick(final Item item) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Warning")
                    .setMessage("do you want delete: " + item.getText())
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (item instanceof UserItem) {
                                        int count = Injection.getDataBase().UserModel().delUser((UserEntity) item.getData());
                                        Toast.makeText(ListActivity.this, count + " row(s) deleted", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        HostEntity data = (HostEntity) item.getData();
                                        int count =  Injection.getDataBase().HostModel().delHost(data);
                                        Toast.makeText(ListActivity.this, count + " row(s) deleted", Toast.LENGTH_LONG).show();
                                    }
                            }})
                    .setNegativeButton("No", null).show();
        return true;

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
			if (restore()){
				suscribeData();
			}
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
		String src = this.getDatabasePath("passkeeper.db").getAbsolutePath();
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
		startActivity(intent);
	}

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private boolean restore() {
        // FIXME 兼容老的数据库
		File dir = new File(Environment.getExternalStorageDirectory(), "passkeeperbackup");
		if (!dir.exists()) {
			Toast.makeText(this, "backup dir not exists!", Toast.LENGTH_LONG).show();
			return false;
		}
		File[] backups = dir.listFiles();
        if (backups == null) {
            Toast.makeText(this, "无法访问sd卡", Toast.LENGTH_LONG).show();
            return false;
        }
        long tem = 0;
		File lastbackup = null;
		for (File b : backups) {
			if (b.getName().startsWith("db")&&b.lastModified() > tem) {
				tem = b.lastModified();
				lastbackup = b;
			}
		}
		if (lastbackup == null) {
			Toast.makeText(this, "backup file not exists!", Toast.LENGTH_LONG).show();
			return false;
		}

		String dst = this.getDatabasePath("passkeeper.db").getAbsolutePath();
        FileInputStream finput = null;
        OutputStream foutput = null;
		try {
			finput = new FileInputStream(lastbackup);
            foutput = new FileOutputStream(dst);

			// Transfer bytes from the inputfile to the outputfile
			byte[] buffer = new byte[1024];
			int length;
			while ((length = finput.read(buffer)) > 0) {
				foutput.write(buffer, 0, length);
			}
            foutput.flush();
		} catch (Exception e) {
			Toast.makeText(this, "failed"+e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		} finally {
            // Close the streams
            if (foutput != null) {
                try {
                    foutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (finput != null) {
                try {
                    finput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        Toast.makeText(this, "restore succeed!", Toast.LENGTH_LONG).show();
		return true;
	}

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	private void export(){
        io.reactivex.Observable.just(viewModel.getItems().getValue())
                .map(new io.reactivex.functions.Function<List<Item>, Boolean>() {
                    @Override
                    public Boolean apply(List<Item> items) throws Exception {
                        File b = new File(Environment.getExternalStorageDirectory(), "passkeeperbackup");
                        File csv = new File(b, "/export"+System.currentTimeMillis()+".csv");
                        csv.getParentFile().mkdirs();
                        BufferedWriter bw = null;
                        try {
                            bw = new BufferedWriter(new FileWriter(csv, false));
                            for (Item tmp : items) {
                                HostItem host = ((HostItem) tmp);
                                String hostname = host.getText();
                                LinkedList<Node> tmpusers = host.getChildren();
                                for (Node tmpu : tmpusers) {
                                    UserItem user = ((UserItem) tmpu);
                                    UserEntity userEntity = user.getData();
                                    if (userEntity.id <= 0) continue;
                                    String username = userEntity.username;
                                    String key = PassKeepApp.sInstance.getKey();
                                    String hash = Util.md5(hostname + username + key);
                                    String pwd = hash.substring(0, userEntity.pwdLength);
                                    bw.newLine();
                                    bw.write(hostname + "," + username + "," + pwd);
                                }
                            }
                            return true;
                        }
                        finally {
                            if (bw != null) {
                                bw.close();
                            }
                        }
                    }
                })
                .subscribe(new DisposableObserver<Boolean>() {
            @Override
            public void onNext(Boolean items) {
                Toast.makeText(ListActivity.this,
                        "export succeed,check the dir:\"passkeeperbackup\" on sdcard! ",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Toast.makeText(ListActivity.this,
                        "出错：" + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete() {

            }
        });

    }

	private void generatePwd(UserItem item) {
        String hostname = ((HostItem) item.getParent()).getText();
        String username = item.getText();
        int len = item.getData().pwdLength;
		String key = PassKeepApp.sInstance.getKey();
		String hash = Util.md5(hostname + username + key);
		String pwd = hash.substring(0, len);

		LayoutInflater inflater = LayoutInflater.from(this);
		View promptView = inflater.inflate(R.layout.puretext_dialog, null);
		final TextView tv1 = (TextView) promptView.findViewById(R.id.pwd_tv);
		tv1.setText(pwd);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setView(promptView);
		AlertDialog dialog = alertDialogBuilder.create();
		dialog.show();
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
                    if (restore()){
                        suscribeData();
                    }
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
