package hotstu.github.passkeeper.viewmodel;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.databinding.ObservableField;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import hotstu.github.passkeeper.Injection;
import hotstu.github.passkeeper.ListActivity;
import hotstu.github.passkeeper.Util;
import hotstu.github.passkeeper.db.AppDatabase;
import hotstu.github.passkeeper.db.HostEntity;
import hotstu.github.passkeeper.db.UserEntity;
import hotstu.github.passkeeper.tree.Node;
import io.reactivex.observers.DisposableObserver;

/**
 * @author hglf
 * @since 2018/1/16
 */
public class ListViewModel extends AndroidViewModel {
    private static final String TAG = ListViewModel.class.getSimpleName();
    public final ObservableField<Integer> seekbarValue;

    final AppDatabase database;
    final String key;
    final List<ListActivity.Item> items;
    final LiveData<List<ListActivity.Item>> liveData;
    public final SingleLiveEvent<Void> addParentEvent;
    public final SingleLiveEvent<ListActivity.Item> addChildEvent;
    public final SingleLiveEvent<ListActivity.Item> deleteEvent;
    public final SingleLiveEvent<String> errToastEvent;
    public final SingleLiveEvent<String> nomalToastEvent;
    public final SingleLiveEvent<String> showPwdEvent;
    public final SingleLiveEvent<Void> recreateEvent;

    public void setSeekBarValue(int progress) {
        Log.d(TAG, "setSeekBarValue" + progress);
        seekbarValue.set(progress);
    }

    public ListViewModel(@NonNull Application application, AppDatabase db, String key) {
        super(application);
        this.database = db;
        this.key = key;
        items = new ArrayList<>();
        seekbarValue = new ObservableField<>();
        addParentEvent = new SingleLiveEvent<>();
        errToastEvent = new SingleLiveEvent<>();
        nomalToastEvent = new SingleLiveEvent<>();
        addChildEvent = new SingleLiveEvent<>();
        deleteEvent = new SingleLiveEvent<>();
        showPwdEvent = new SingleLiveEvent<>();
        recreateEvent = new SingleLiveEvent<>();
        seekbarValue.set(0);
        liveData = Transformations.switchMap(database.HostModel().queryAllHosts(),
                new Function<List<HostEntity>, LiveData<List<ListActivity.Item>>>() {
            @Override
            public LiveData<List<ListActivity.Item>> apply(List<HostEntity> input) {
                final MediatorLiveData<List<ListActivity.Item>> mediatorLiveData = new MediatorLiveData<>();
                items.clear();
                for (int i = 0; i < input.size(); i++) {
                    HostEntity hostEntity = input.get(i);
                    final ListActivity.HostItem itemhost = new ListActivity.HostItem();
                    itemhost.setData(hostEntity);
                    items.add(itemhost);
                    LiveData<List<UserEntity>> tempusers = database.UserModel().findUsersByHostId(hostEntity.id);
                    mediatorLiveData.addSource(tempusers, new Observer<List<UserEntity>>() {
                        @Override
                        public void onChanged(@Nullable List<UserEntity> userEntities) {
                            Log.d(TAG, "userEntities onChanged");
                            ArrayList<ListActivity.UserItem> useritems = new ArrayList<>();
                            for (int j = 0; j < userEntities.size(); j++) {
                                ListActivity.UserItem userItem = new ListActivity.UserItem();
                                userItem.setData(userEntities.get(j));
                                useritems.add(userItem);
                            }
                            itemhost.clear();
                            itemhost.addChildren(useritems);
                            mediatorLiveData.postValue(items);
                        }
                    });
                }
                mediatorLiveData.postValue(items);
                return mediatorLiveData;
            }
        });
    }

    public void openAddParent() {
        addParentEvent.call();
    }

    public void openAddChild(ListActivity.Item current) {
        addChildEvent.postValue(current);
    }

    public void showDeleteDialog(ListActivity.Item item) {
        deleteEvent.postValue(item);
    }

    public void performDeleteAction(ListActivity.Item item) {
        if (item instanceof ListActivity.UserItem) {
            int count = Injection.getDataBase().UserModel().delUser((UserEntity) item.getData());
            showToastSuccess( count + " row(s) deleted");
        }
        else {
            HostEntity data = (HostEntity) item.getData();
            int count =  Injection.getDataBase().HostModel().delHost(data);
            showToastSuccess(count + " row(s) deleted");
        }
    }

    public void addHost(String hostname) {
        if (TextUtils.isEmpty(hostname)) {
            showToastErr("host name 不能为空");
            return;
        }
        if ( (Injection.getDataBase().HostModel().addHost(new HostEntity(0, hostname))) > 0) {
            showToastSuccess("成功");
        } else {
           showToastErr("失败");
        }
    }

    public void addUser(String username, int pwdlenth, int hostId) {
        if ("".equals(username) || pwdlenth < 6 || pwdlenth > 12) {
            showToastErr("输入参数错误");
            return;
        }
        long id = Injection.getDataBase().UserModel().addUser(new UserEntity(0, username, pwdlenth, hostId));
        if (id > 0) {
            showToastSuccess("成功");
        } else {
            showToastErr("失败");
        }
    }


    public void generatePwd(ListActivity.UserItem child) {
        String hostname = ((ListActivity.HostItem) child.getParent()).getText();
        String username = child.getText();
        int len = child.getData().pwdLength;
        String hash = Util.md5(hostname + username + this.key);
        String pwd = hash.substring(0, len);
        showPwdEvent.postValue(pwd);
    }

    public void showToastErr(String msg) {
        errToastEvent.postValue(msg);
    }

    public void showToastSuccess(String msg) {
        nomalToastEvent.postValue(msg);
    }

    public LiveData<List<ListActivity.Item>> getItems() {
        return liveData;
    }

    public void export() {
        io.reactivex.Observable.just(getItems().getValue())
                .map(new io.reactivex.functions.Function<List<ListActivity.Item>, Boolean>() {
                    @Override
                    public Boolean apply(List<ListActivity.Item> items) throws Exception {
                        File b = new File(Environment.getExternalStorageDirectory(), "passkeeperbackup");
                        File csv = new File(b, "/export"+System.currentTimeMillis()+".csv");
                        csv.getParentFile().mkdirs();
                        BufferedWriter bw = null;
                        try {
                            bw = new BufferedWriter(new FileWriter(csv, false));
                            for (ListActivity.Item tmp : items) {
                                ListActivity.HostItem host = ((ListActivity.HostItem) tmp);
                                String hostname = host.getText();
                                LinkedList<Node> tmpusers = host.getChildren();
                                for (Node tmpu : tmpusers) {
                                    ListActivity.UserItem user = ((ListActivity.UserItem) tmpu);
                                    UserEntity userEntity = user.getData();
                                    if (userEntity.id <= 0) continue;
                                    String username = userEntity.username;
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
                        showToastSuccess("export succeed,check the dir:\"passkeeperbackup\" on sdcard! ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        showToastErr("出错：" + e.getMessage() );
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void restore() {
        File dir = new File(Environment.getExternalStorageDirectory(), "passkeeperbackup");
        if (!dir.exists()) {
            showToastErr("backup dir not exists!");
            return;
        }
        File[] backups = dir.listFiles();
        if (backups == null) {
            showToastErr( "无法访问sd卡");
            return;
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
            showToastErr("backup file not exists!");
            return;
        }

        String dst = Injection.getApplicaitonContext().getDatabasePath("passkeeper.db").getAbsolutePath();
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
            recreate();//重启activity
        } catch (Exception e) {
            showToastErr("failed"+e.getMessage());
            return;
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
        showToastSuccess( "restore succeed!");
    }

    private void recreate() {
        recreateEvent.call();
    }
}
