package hotstu.github.passkeeper;


import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;

/**
 * an activity to prevent viewing accounts without masterkey
 * the first time the app run will get the masterkey and a 
 * email address to sendto when backup.
 * @author foo
 *
 */
public class WatchdogActivity extends Activity {
	private Myapp app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.watchdog);
		Log.d("WatchdogActivity", "onCreate");
		app = (Myapp) getApplication();
		if (null == app.getKey() || "".equals(app.getKey())) {
			login();
		}
	}

	private void login() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View promptView = inflater.inflate(R.layout.pwdform, null);
		final EditText et1 = (EditText) promptView.findViewById(R.id.etPassword);
		final CheckBox ckbShowPwd = (CheckBox) promptView.findViewById(R.id.cbShowPwd);
		ckbShowPwd.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked){
					et1.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
					et1.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}else {
					et1.setInputType(InputType.TYPE_CLASS_TEXT);
					et1.setTransformationMethod(null);
				}
				
			}
		});
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setView(promptView);
		alertDialogBuilder
				.setOnCancelListener(new OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
						WatchdogActivity.this.finish();
					}
				})
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						//TODO the hash method of masterkey is not strong
						if (et1.getText().toString() != null
								&& !"".equals(et1.getText().toString())) {
							DBHelper dbh = new DBHelper(WatchdogActivity.this);
							String hash = dbh.checkHash();
							String input = ListActivity.md5(app.getSalt()
									+ et1.getText().toString());
							if (!"".equals(hash) && hash.equals(input)) {
								app.setKey(et1.getText().toString());
								dialog.dismiss();
								Intent i = new Intent(getApplicationContext(),
										ListActivity.class);
								startActivity(i);
								WatchdogActivity.this.finish();
							} else if ("".equals(hash)) {
								//the first time running this app
								String newhash = ListActivity.md5(app.getSalt()
										+ et1.getText().toString());
								dbh.addHash(newhash);
								app.setKey(et1.getText().toString());
								dialog.dismiss();
								Intent i = new Intent(getApplicationContext(),
										ListActivity.class);
								startActivity(i);
								WatchdogActivity.this.finish();
							} else {
								WatchdogActivity.this.finish();
							}

						} else
							WatchdogActivity.this.finish();
					}

				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								WatchdogActivity.this.finish();
							}
						});
		AlertDialog dialog = alertDialogBuilder.create();
		dialog.show();
	}

}
