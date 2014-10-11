package hotstu.github.passkeeper;

import hotstu.github.passkeeper.model.Host;
import hotstu.github.passkeeper.model.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;

public class ListActivity extends ActionBarActivity {
	private MyExpandableListAdapter adapter;
	private ExpandableListView listView;
	private TextView btnadd;
	private ArrayList<Host> hosts;
	private HashMap<Integer, ArrayList<User>> users;
	private Myapp app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		app = (Myapp) getApplication();

		btnadd = (TextView) findViewById(R.id.btn_add_host);
		btnadd.setText("+");
		btnadd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Toast.makeText(ListActivity.this, "add",
				// Toast.LENGTH_LONG).show();
				LayoutInflater inflater = LayoutInflater
						.from(ListActivity.this);
				View promptView = inflater.inflate(R.layout.form, null);
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						ListActivity.this);
				final EditText et = (EditText) promptView
						.findViewById(R.id.form_et1);
				alertDialogBuilder.setView(promptView);
				alertDialogBuilder
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										String hostname = et.getText()
												.toString();
										if ("".equals(hostname)) {
											Toast.makeText(ListActivity.this,
													"error", Toast.LENGTH_LONG)
													.show();
											return;
										}
										DBHelper dbh = new DBHelper(
												ListActivity.this);
										if (dbh.addHost(new Host(999, hostname))) {
											Toast.makeText(ListActivity.this,
													"succeed",
													Toast.LENGTH_LONG).show();
											getData();
											adapter.notifyDataSetChanged();
										} else {
											Toast.makeText(ListActivity.this,
													"failed", Toast.LENGTH_LONG)
													.show();
										}

									}

								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								});
				AlertDialog diaolog = alertDialogBuilder.create();
				diaolog.show();
			}
		});

		getData();

		listView = (ExpandableListView) findViewById(R.id.expand1);
		adapter = new MyExpandableListAdapter(this, this.hosts, this.users);
		listView.setAdapter(adapter);

		listView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					final int groupPosition, int childPosition, long id) {
				if (0 == childPosition) {
					LayoutInflater inflater = LayoutInflater
							.from(ListActivity.this);
					View promptView = inflater
							.inflate(R.layout.form_item, null);
					final EditText et1 = (EditText) promptView
							.findViewById(R.id.form_user_et);
					final EditText et2 = (EditText) promptView
							.findViewById(R.id.form_user_et2);
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							ListActivity.this);
					alertDialogBuilder.setView(promptView);
					alertDialogBuilder.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String username;
									int pwdlenth;
									try {
										username = et1.getText().toString();
										pwdlenth = Integer.parseInt(et2
												.getText().toString());
									} catch (Exception e) {
										Toast.makeText(ListActivity.this,
												"bad input", Toast.LENGTH_LONG)
												.show();
										return;
									}
									if ("".equals(username) || pwdlenth < 6
											|| pwdlenth > 12) {
										Toast.makeText(ListActivity.this,
												"bad input", Toast.LENGTH_LONG)
												.show();
										return;
									}
									DBHelper dbh = new DBHelper(
											ListActivity.this);
									if (dbh.addUser(new User(-1, username,
											pwdlenth, adapter.getGroup(
													groupPosition).get_id()))) {
										Toast.makeText(ListActivity.this,
												"succeed", Toast.LENGTH_LONG)
												.show();
										getData();
										adapter.notifyDataSetChanged();
									} else {
										Toast.makeText(ListActivity.this,
												"failed", Toast.LENGTH_LONG)
												.show();
									}

								}

							}).setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
					AlertDialog dialog = alertDialogBuilder.create();
					dialog.show();
				} else {
					// String text = "childposintion " + childPosition +
					// " count "
					// + adapter.getChildrenCount(groupPosition);
					// Toast.makeText(ListActivity.this, text,
					// Toast.LENGTH_LONG)
					// .show();
					generatePwd(groupPosition, childPosition);
				}
				return true;
			}
		});

		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				int itemType = ExpandableListView.getPackedPositionType(id);
				if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					final int childPosition = ExpandableListView
							.getPackedPositionChild(id);
					final int groupPosition = ExpandableListView
							.getPackedPositionGroup(id);
					if (adapter.getChild(groupPosition,childPosition).get_id() == -1) return true;

					new AlertDialog.Builder(ListActivity.this)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle("wornning")
							.setMessage(
									"do you want delete: "
											+ adapter.getChild(groupPosition,
													childPosition)
													.getUsername())
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

											DBHelper dbh = new DBHelper(
													ListActivity.this);
											int count = dbh.delUser(adapter
													.getChild(groupPosition,
															childPosition));
											Toast.makeText(ListActivity.this,
													count + " row(s) deleted",
													Toast.LENGTH_LONG).show();
											getData();
											adapter.notifyDataSetChanged();
										}

									}).setNegativeButton("No", null).show();
					// do your per-item callback here
					return true; // true if we consumed the click, false if not

				} else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
					final int groupPosition = ExpandableListView
							.getPackedPositionGroup(id);
					// do your per-group callback here
					new AlertDialog.Builder(ListActivity.this)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle("wornning")
							.setMessage(
									"do you want delete: "
											+ adapter.getGroup(groupPosition)
													.getHostname())
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

											DBHelper dbh = new DBHelper(
													ListActivity.this);
											int count = dbh.delHost(adapter
													.getGroup(groupPosition));
											Toast.makeText(ListActivity.this,
													count + " row(s) deleted",
													Toast.LENGTH_LONG).show();
											getData();
											adapter.notifyDataSetChanged();
										}

									}).setNegativeButton("No", null).show();
					return true; // true if we consumed the click, false if not

				} else {
					// null item; we don't consume the click
					return false;
				}
			}
		});
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
			backup();
			return true;
		} else if (R.id.restore == item.getItemId()) {
			if (restore()){
				getData();
				adapter.notifyDataSetChanged();
			}
			return true;
		} else if(R.id.export == item.getItemId()){
			export();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("ListActivity", "onKeyDown");
		return super.onKeyDown(keyCode, event);
	}

	private void backup() {
		String src = this.getDatabasePath("passkeeper.db").getAbsolutePath();
		String dst = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/passkeeperbackup/"
				+ "db"
				+ System.currentTimeMillis();
		File temfile = new File(Environment.getExternalStorageDirectory()
				+ "/passkeeperbackup");
		if (!temfile.exists()) {
			temfile.mkdir();
		}
		File fsrc = new File(src);
		try {
			FileInputStream fisrc = new FileInputStream(fsrc);
			OutputStream fodst = new FileOutputStream(dst);

			// Transfer bytes from the inputfile to the outputfile
			byte[] buffer = new byte[1024];
			int length;
			while ((length = fisrc.read(buffer)) > 0) {
				fodst.write(buffer, 0, length);
			}

			// Close the streams
			fodst.flush();
			fodst.close();
			fisrc.close();
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "FileNotFound", Toast.LENGTH_LONG).show();
			return;
		} catch (IOException e) {
			Toast.makeText(this, "IOException", Toast.LENGTH_LONG).show();
			return;
		}

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND)
				.setType("*/*")
				.putExtra(android.content.Intent.EXTRA_EMAIL,
						new String[] { app.getEmail() })
				.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"dbbackup" + System.currentTimeMillis())
				.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(dst)));
		startActivity(intent);
	}

	private boolean restore() {
		String backupdir = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/passkeeperbackup";
		File dir = new File(backupdir);
		if (!dir.isAbsolute()) {
			Toast.makeText(this, "backup dir not exists!", Toast.LENGTH_LONG)
					.show();
			return false;
		}
		File[] backups = dir.listFiles();
		long tem = 0;
		File lastbackup = null;
		for (File b : backups) {
			if (b.getName().startsWith("db")&&b.lastModified() > tem) {
				tem = b.lastModified();
				lastbackup = b;
			}
		}
		if (lastbackup == null) {
			Toast.makeText(this, "backup file not exists!", Toast.LENGTH_LONG)
					.show();
			return false;
		}

		String dst = this.getDatabasePath("passkeeper.db").getAbsolutePath();
		try {
			FileInputStream finput = new FileInputStream(lastbackup);
			OutputStream foutput = new FileOutputStream(dst);

			// Transfer bytes from the inputfile to the outputfile
			byte[] buffer = new byte[1024];
			int length;
			while ((length = finput.read(buffer)) > 0) {
				foutput.write(buffer, 0, length);
			}

			// Close the streams
			foutput.flush();
			foutput.close();
			finput.close();
		} catch (Exception e) {
			Toast.makeText(this, "failed"+e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		}
		Toast.makeText(this, "restore succeed!", Toast.LENGTH_LONG).show();
		return true;
	}

	private void export(){
		if (this.hosts == null||this.hosts.size() == 0||this.users == null||this.users.size() == 0) return;
		String backupdir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/passkeeperbackup";
		File b = new File(backupdir);
		if (!b.exists()) b.mkdir();
		String exportpath = backupdir+"/export"+System.currentTimeMillis()+".csv";
		try {
			File csv = new File(exportpath);
			BufferedWriter bw = new BufferedWriter(new FileWriter(csv, true));
			Iterator<Host> ih = this.hosts.iterator();
			while(ih.hasNext()){
				Host tmp = ih.next();
				String hostname = tmp.getHostname();
				ArrayList<User> users = this.users.get(tmp.get_id());
				Iterator<User> iu = users.iterator();
				while(iu.hasNext()){
					User tmpu = iu.next();
					if (tmpu.get_id() == -1) continue;
					String username = tmpu.getUsername();
					String key = app.getKey();
					String hash = md5(hostname + username + key);
					String pwd = hash.substring(0, tmpu.getPwdLength());
					
					bw.newLine();
					bw.write(hostname+","+username+","+pwd);
				}
			}
			bw.close();
		} catch (IOException e) {
			Toast.makeText(this, "export failed,"+e.getMessage(), Toast.LENGTH_LONG).show();
		}
		Toast.makeText(this, "export succeed,check the dir:\"passkeeperbackup\" on sdcard! ", Toast.LENGTH_LONG).show();
		return;
	}
	
	private void getData() {
		if (null == this.hosts)
			this.hosts = new ArrayList<Host>();
		if (null == this.users)
			this.users = new HashMap<Integer, ArrayList<User>>();
		DBHelper dbh = new DBHelper(this);
		ArrayList<Host> _hosts = dbh.queryAllHosts();
		ArrayList<User> _users = dbh.queryAllUsers();
		Iterator<Host> ih = _hosts.iterator();
		this.hosts.clear();
		this.users.clear();
		while (ih.hasNext()) {
			Host t = ih.next();
			this.hosts.add(t);
			ArrayList<User> tempUserList = new ArrayList<User>();
			tempUserList.add(new User(-1, "+", -1, -1));
			this.users.put(t.get_id(), tempUserList);
		}
		Iterator<User> iu = _users.iterator();
		while (iu.hasNext()) {
			User u = iu.next();
			if (null != this.users.get(u.getHostId())) {
				this.users.get(u.getHostId()).add(u);
			}
		}

	}

	private void generatePwd(int groupId, int childId) {
		String hostname = adapter.getGroup(groupId).getHostname();
		String username = adapter.getChild(groupId, childId).getUsername();
		int len = adapter.getChild(groupId, childId).getPwdLength();
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
}
