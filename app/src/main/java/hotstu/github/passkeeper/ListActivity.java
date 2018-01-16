package hotstu.github.passkeeper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import hotstu.github.passkeeper.db.AppDatabase;
import hotstu.github.passkeeper.db.HostEntity;
import hotstu.github.passkeeper.db.UserEntity;
import hotstu.github.passkeeper.tree.Child;
import hotstu.github.passkeeper.tree.Node;
import hotstu.github.passkeeper.tree.Parent;
import hotstu.github.passkeeper.widget.TreeAdapter;

public class ListActivity extends AppCompatActivity {
    private static final String TAG = "ListActivity";
    private RecyclerView mRecylcerView;
    private View btnAdd;

    private Myapp app;
    TreeAdapter<VH, Item> mAdapter;
    private int widthPixel;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        app = (Myapp) getApplication();
        db = AppDatabase.getInMemoryDatabase(getApplicationContext());
        btnAdd = findViewById(R.id.btn_add_host);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddParent();
            }
        });
        mRecylcerView = (RecyclerView) findViewById(R.id.list);
        mRecylcerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });

        mAdapter = new TreeAdapter<>(new TreeAdapter.AdapterDelegate<VH, Item>() {
            @Override
            public VH onCreateViewHolder(TreeAdapter<VH, Item> adapter, ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                int resId = viewType == 0 ? R.layout.list_item : R.layout.list_group;
                return new VH(inflater.inflate(resId, parent, false));
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

        mRecylcerView.setAdapter(mAdapter);
        widthPixel = (int) (getResources().getDimension(R.dimen.activity_horizontal_margin) *2);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        Drawable drawable =  ContextCompat.getDrawable(this, R.drawable.divider);
        dividerItemDecoration.setDrawable(drawable);
        mRecylcerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                VH vh = (VH) parent.getChildViewHolder(view);
                Node node = vh.getData();
                outRect.left = widthPixel * (node.getIndent() - 1);
            }
        });

        inflateData();
    }

    void inflateData() {
        List<HostEntity> retHosts = db.HostModel().queryAllHosts();
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < retHosts.size(); i++) {
            HostEntity host = retHosts.get(i);
            HostItem itemhost = new HostItem();
            itemhost.setData(host);
            UserItem addUser = new UserItem();
            addUser.setData(new UserEntity(-1, "+", -1, -1));
            itemhost.addChild(addUser, 0);
            List<UserEntity> tempusers = db.UserModel().findUsersByHostId(host.id);
            if (tempusers != null) {
                ArrayList<UserItem> useritems = new ArrayList<>();
                for (int j = 0; j < tempusers.size(); j++) {
                    UserItem userItem = new UserItem();
                    userItem.setData(tempusers.get(j));
                    useritems.add(userItem);
                }
                itemhost.addChildren(useritems);
            }
            items.add(itemhost);
        }
        mAdapter.clearDataSet();
        mAdapter.setDataSet(items);

    }


    interface Item<T> extends Node {
        void setData(T data);
        T getData();
        String getText();
    }

    static class HostItem extends Parent implements Item<HostEntity> {
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

    static class UserItem extends Child implements Item<UserEntity> {
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


    class VH extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView tv;
        Item item;
        public VH(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(android.R.id.text1);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void bindData(Item item) {
            this.item = item;
            this.tv.setText(item.getText());
        }

        public Item getData(){
            return item;
        }

        @Override
        public void onClick(View v) {
            int position = this.getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            Node item = getData();
            if(!item.isLeaf()) {
                Parent parent = (Parent) item;
                mAdapter.toggle(parent, position);
            } else {
                onChildClick((UserItem) item);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return onChildLongClick(getData());
        }
    }



    public void showAddParent() {
        LayoutInflater inflater = LayoutInflater.from(ListActivity.this);
        View promptView = inflater.inflate(R.layout.form, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ListActivity.this);
        final EditText et = (EditText) promptView.findViewById(R.id.form_et1);
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
                                long id;
                                if ( (id = db.HostModel().addHost(new HostEntity(0, hostname))) > 0) {
                                    Toast.makeText(ListActivity.this, "succeed", Toast.LENGTH_LONG).show();
                                    HostEntity added = db.HostModel().findHostById(id);
                                    if (added != null) {
                                        HostItem hostItem = new HostItem(added);
                                        hostItem.addChild(new UserItem(new UserEntity(-1, "+", -1, -1)), 0);
                                        mAdapter.addItem(null, hostItem, -1);
                                    }
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

    public boolean onChildClick(UserItem child) {
        if (child.getData().id <= 0) {
            showAddUser(child);
        } else {
            generatePwd(child);
        }
        return true;
    }

    void showAddUser(UserItem child) {
        final HostItem host = ((HostItem) child.getParent());
        LayoutInflater inflater = LayoutInflater.from(ListActivity.this);
        View promptView = inflater.inflate(R.layout.form_item, null);
        final EditText et1 = (EditText) promptView.findViewById(R.id.form_user_et);
        final EditText et2 = (EditText) promptView.findViewById(R.id.form_user_et2);
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
                       long id = db.UserModel().addUser(new UserEntity(0, username, pwdlenth, host.getData().id));
                        if (id > 0) {
                            Toast.makeText(ListActivity.this, "succeed", Toast.LENGTH_LONG).show();
                            // refresh data;
                            UserEntity added = db.UserModel().findUserById(id);
                            if (added != null) {
                                UserItem userItem = new UserItem(added);
                                mAdapter.addItem(host, userItem, -1);
                            }
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
                                        int count = db.UserModel().delUser((UserEntity) item.getData());
                                        Toast.makeText(ListActivity.this, count + " row(s) deleted", Toast.LENGTH_LONG).show();
                                        mAdapter.removeItem(item);
                                    }
                                    else {
                                        int count =  db.HostModel().delHost((HostEntity) item.getData());
                                        Toast.makeText(ListActivity.this, count + " row(s) deleted", Toast.LENGTH_LONG).show();
                                        mAdapter.removeItem(item);
                                    }
                            }})
                    .setNegativeButton("No", null).show();
        return true;

    }


    @Override
    public void onBackPressed() {
        Log.d("ListActivity", "onBackPressed");
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
				inflateData();
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
				.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{app.getEmail()})
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

	@RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
	private void export(){
        ArrayList<HostEntity> hosts;
        SparseArray<ArrayList<UserEntity>> hostId2Users;
        hosts = new ArrayList<>();
        hostId2Users = new SparseArray<>();

        List<HostEntity> retHosts = db.HostModel().queryAllHosts();
        List<UserEntity> retUsers = db.UserModel().queryAllUsers();
        hosts.addAll(retHosts);
        SparseArray<HostEntity> tempHostMapping = new SparseArray<>();
        for (int i = 0; i < retHosts.size(); i++) {
            HostEntity host = retHosts.get(i);
            hostId2Users.put(host.id, new ArrayList<UserEntity>());
            tempHostMapping.put(host.id, host);
            //this.hostId2Users.get(host.get_id()).add(new User(-1, "+", -1, -1));
        }
        for (int i = 0; i < retUsers.size(); i++) {
            UserEntity user = retUsers.get(i);
            List<UserEntity> tempusers = hostId2Users.get(user.hostId);
            if (tempusers != null) {
                tempusers.add(user);
            }
        }
        File b = new File(Environment.getExternalStorageDirectory(), "passkeeperbackup");
		if (!b.exists() && !b.mkdirs()) {
            Toast.makeText(this, "无法访问sd卡", Toast.LENGTH_LONG).show();
            return;
        }
        File csv = new File(b, "/export"+System.currentTimeMillis()+".csv");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(csv, true));
            for (HostEntity tmp : hosts) {
                String hostname = tmp.hostname;
                ArrayList<UserEntity> tmpusers = hostId2Users.get(tmp.id);
                for (UserEntity tmpu : tmpusers) {
                    if (tmpu.id <= 0) continue;
                    String username = tmpu.username;
                    String key = app.getKey();
                    String hash = md5(hostname + username + key);
                    String pwd = hash.substring(0, tmpu.pwdLength);
                    bw.newLine();
                    bw.write(hostname + "," + username + "," + pwd);
                }
            }
			bw.close();
		} catch (IOException e) {
            Log.e(TAG, "export: ", e);
            Toast.makeText(this, "export failed,"+e.getMessage(), Toast.LENGTH_LONG).show();
		    return;
        }
		Toast.makeText(this, "export succeed,check the dir:\"passkeeperbackup\" on sdcard! ", Toast.LENGTH_LONG).show();
	}

	private void generatePwd(UserItem item) {
        String hostname = ((HostItem) item.getParent()).getText();
        String username = item.getText();
        int len = item.getData().pwdLength;
		String key = app.getKey();
		String hash = md5(hostname + username + key);
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

	public static String md5(String string) {
		byte[] hash;
		try {
			hash = MessageDigest.getInstance("MD5").digest(
					string.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Huh, MD5 should be supported?", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Huh, UTF-8 should be supported?", e);
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10)
				hex.append("0");
			hex.append(Integer.toHexString(b & 0xFF));
		}
		return hex.toString();
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
                        inflateData();
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
