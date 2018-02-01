package hotstu.github.passkeeper.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Transformations;
import android.databinding.ObservableField;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;

import hotstu.github.passkeeper.Injection;
import hotstu.github.passkeeper.Util;
import hotstu.github.passkeeper.db.AppDatabase;
import hotstu.github.passkeeper.db.HostEntity;
import hotstu.github.passkeeper.db.UserEntity;
import hotstu.github.passkeeper.vo.HostItem;
import hotstu.github.passkeeper.vo.Item;
import hotstu.github.passkeeper.vo.UserItem;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * @author hglf
 * @since 2018/1/16
 */
public class ListViewModel extends AndroidViewModel {
    private static final String TAG = ListViewModel.class.getSimpleName();
    public final ObservableField<Integer> seekbarValue;

    final AppDatabase database;
    final String key;
    final LiveData<SparseArray<Pair<HostEntity, SparseArray<UserEntity>>>> liveData;
    public final SingleLiveEvent<Void> addParentEvent;
    public final SingleLiveEvent<Item> addChildEvent;
    public final SingleLiveEvent<Item> deleteEvent;
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
        seekbarValue = new ObservableField<>();
        addParentEvent = new SingleLiveEvent<>();
        errToastEvent = new SingleLiveEvent<>();
        nomalToastEvent = new SingleLiveEvent<>();
        addChildEvent = new SingleLiveEvent<>();
        deleteEvent = new SingleLiveEvent<>();
        showPwdEvent = new SingleLiveEvent<>();
        recreateEvent = new SingleLiveEvent<>();
        seekbarValue.set(0);
        final MediatorLiveData<SparseArray<Pair<HostEntity, SparseArray<UserEntity>>>> mediatorLiveData = new MediatorLiveData<>();
        final SparseArray<Pair<HostEntity, SparseArray<UserEntity>>> items = new SparseArray<>();
        final SparseArray<LiveData<List<UserEntity>>> activeSources = new SparseArray<>();
        liveData = Transformations.switchMap(database.HostModel().queryAllHosts(),
                input -> {
                    HashSet<Integer> activeHostIdSet = new HashSet<>();
                    for (int i = 0; i < input.size(); i++) {
                        HostEntity host = input.get(i);
                        Pair<HostEntity, SparseArray<UserEntity>> old = items.get(host.id);
                        items.put(host.id, old == null ? new Pair<>(host, new SparseArray<>()) : old);

                        final int hostId = host.id;
                        activeHostIdSet.add(hostId);
                        if (activeSources.get(hostId) == null) {
                            activeSources.put(hostId, database.UserModel().findUsersByHostId(hostId));
                            mediatorLiveData.addSource(activeSources.get(hostId),
                                    userEntities -> {
                                        HashSet<Integer> activeUserIds = new HashSet<>();
                                        Pair<HostEntity, SparseArray<UserEntity>> hostItemSparseArrayPair = items.get(hostId);
                                        if (hostItemSparseArrayPair == null) {
                                            //removed stop watch
                                            Log.w(TAG, "user with illegal hostId, ignore");
                                            return;
                                        }
                                        for (int j = 0; j < userEntities.size(); j++) {
                                            UserEntity userEntity = userEntities.get(j);
                                            activeUserIds.add(userEntity.id);
                                            hostItemSparseArrayPair.second.put(userEntity.id, userEntity);
                                        }
                                        HashSet<Integer> toRemove = new HashSet<>();
                                        for (int k = 0; k < hostItemSparseArrayPair.second.size(); k++) {
                                            UserEntity user = hostItemSparseArrayPair.second.get(hostItemSparseArrayPair.second.keyAt(k));
                                            if (!activeUserIds.contains(user.id)) {
                                                toRemove.add(hostItemSparseArrayPair.second.keyAt(k));
                                            }
                                        }
                                        for (Integer integer : toRemove) {
                                            hostItemSparseArrayPair.second.remove(integer);
                                        }
                                        mediatorLiveData.postValue(items);
                                    });
                        }

                    }
                    // remove inactive hostId, et.  deleted
                    HashSet<Integer> deleteHostIdset = new HashSet<>();
                    for (int i = 0; i < items.size(); i++) {
                        Pair<HostEntity, SparseArray<UserEntity>> hostEntitySparseArrayPair = items.get(items.keyAt(i));
                        if (!activeHostIdSet.contains(hostEntitySparseArrayPair.first.id)) {
                            deleteHostIdset.add(items.keyAt(i));
                        }
                    }
                    for (Integer integer : deleteHostIdset) {
                        int id = items.get(integer).first.id;
                        items.remove(integer);
                        if (activeSources.get(id) != null) {
                            mediatorLiveData.removeSource(activeSources.get(id));
                            activeSources.remove(id);
                        }
                    }
                    mediatorLiveData.postValue(items);
                    return mediatorLiveData;
                });
    }

    public void openAddParent() {
        addParentEvent.call();
    }

    public void openAddChild(Item current) {
        addChildEvent.postValue(current);
    }

    public void showDeleteDialog(Item item) {
        deleteEvent.postValue(item);
    }

    public void performDeleteAction(Item item) {
        if (item instanceof UserItem) {
            int count = Injection.getDataBase().UserModel().delUser((UserEntity) item.getData());
            showToastSuccess(count + " row(s) deleted");
        } else {
            HostEntity data = (HostEntity) item.getData();
            int count = Injection.getDataBase().HostModel().delHost(data);
            showToastSuccess(count + " row(s) deleted");
        }
    }

    public void addHost(String hostname) {
        if (TextUtils.isEmpty(hostname)) {
            showToastErr("host name 不能为空");
            return;
        }
        if ((Injection.getDataBase().HostModel().addHost(new HostEntity(0, hostname))) > 0) {
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


    public void generatePwd(UserItem child) {
        String hostname = ((HostItem) child.getParent()).getText();
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

    public LiveData<SparseArray<Pair<HostEntity, SparseArray<UserEntity>>>> getItems() {
        return liveData;
    }

    public void export() {
        //cold observable
        Observable.just(getItems().getValue())
                .map(items -> {
                    File b = new File(Environment.getExternalStorageDirectory(), "passkeeperbackup");
                    File csv = new File(b, "/export" + System.currentTimeMillis() + ".csv");
                    csv.getParentFile().mkdirs();
                    BufferedWriter bw = null;
                    try {
                        bw = new BufferedWriter(new FileWriter(csv, false));
                        for (int i = 0; i < items.size(); i++) {
                            int key = items.keyAt(i);
                            Pair<HostEntity, SparseArray<UserEntity>> hostEntitySparseArrayPair = items.get(key);
                            HostEntity host = hostEntitySparseArrayPair.first;
                            String hostname = host.hostname;
                            SparseArray<UserEntity> tmpusers = hostEntitySparseArrayPair.second;
                            for (int j = 0; j < tmpusers.size(); j++) {
                                int key2 = tmpusers.keyAt(j);
                                UserEntity user = tmpusers.get(key2);
                                if (user.id <= 0) continue;
                                String username = user.username;
                                String hash = Util.md5(hostname + username + key);
                                String pwd = hash.substring(0, user.pwdLength);
                                bw.newLine();
                                bw.write(hostname + "," + username + "," + pwd);
                            }
                        }
                        return true;
                    } finally {
                        if (bw != null) {
                            bw.close();
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
                        showToastErr("出错：" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void restore() {
        Observable.just(new File(Environment.getExternalStorageDirectory(), "passkeeperbackup"))
                .map(file -> {
                    return file.listFiles();
                })
                .onErrorResumeNext((Throwable throwable) -> {
                    throwable.printStackTrace();
                    Log.e(TAG, Thread.currentThread().getName());
                    showToastErr("TEST");
                    return Observable.empty();
                })
                .map(files -> {
                    long tem = 0;
                    File lastbackup = null;
                    for (File b : files) {
                        if (b.getName().startsWith("db") && b.lastModified() > tem) {
                            tem = b.lastModified();
                            lastbackup = b;
                        }
                    }
                    return lastbackup;
                })
                .onErrorResumeNext(throwable -> {
                    showToastErr("无法找到备份文件");
                })
                .map(file -> {
                            String dst = Injection.getApplicaitonContext().getDatabasePath("passkeeper.db").getAbsolutePath();
                            FileInputStream finput = null;
                            OutputStream foutput = null;
                            try {
                                finput = new FileInputStream(file);
                                foutput = new FileOutputStream(dst);

                                // Transfer bytes from the inputfile to the outputfile
                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = finput.read(buffer)) > 0) {
                                    foutput.write(buffer, 0, length);
                                }
                                foutput.flush();
                            } finally {
                                if (finput != null) {
                                    finput.close();
                                }
                                if (foutput != null) {
                                    foutput.close();
                                }
                            }
                            return true;
                        }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(r -> {
                            recreate();
                            showToastSuccess("restore succeed!");
                        },
                        throwable -> {
                            showToastErr("出错：" + throwable.getMessage());
                        }
                );

    }


    public final ItemProvider itemProvider = new ItemProvider() {
        SparseArray<HostItem> hostSet = new SparseArray<>();
        SparseArray<SparseArray<UserItem>> userSet = new SparseArray<>();

        @Override
        public HostItem obtainHost(int id) {
            if (hostSet.get(id) == null) {
                hostSet.put(id, new HostItem());
            }
            return hostSet.get(id);
        }

        @Override
        public UserItem obtainUser(int id, int hostId) {
            if (userSet.get(hostId) == null) {
                userSet.put(hostId, new SparseArray<>());
            }
            if (userSet.get(hostId).get(id) == null) {
                userSet.get(hostId).put(id, new UserItem());
            }
            return userSet.get(hostId).get(id);
        }
    };

    private void recreate() {
        recreateEvent.call();
    }

    public interface ItemProvider {
        HostItem obtainHost(int id);

        UserItem obtainUser(int id, int hostId);
    }
}
