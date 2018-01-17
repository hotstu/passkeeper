package hotstu.github.passkeeper.viewmodel;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import hotstu.github.passkeeper.Injection;
import hotstu.github.passkeeper.ListActivity;
import hotstu.github.passkeeper.db.HostEntity;
import hotstu.github.passkeeper.db.UserEntity;

/**
 * @author hglf
 * @since 2018/1/16
 */
public class ListViewModel extends AndroidViewModel {
    final List<ListActivity.Item> items;
    final LiveData<List<ListActivity.Item>> liveData;

    public ListViewModel(@NonNull Application application) {
        super(application);
        items = new ArrayList<>();

        liveData = Transformations.switchMap(Injection.getDataBase().HostModel().queryAllHosts(),
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
                    LiveData<List<UserEntity>> tempusers = Injection.getDataBase().UserModel().findUsersByHostId(hostEntity.id);
                    mediatorLiveData.addSource(tempusers, new Observer<List<UserEntity>>() {
                        @Override
                        public void onChanged(@Nullable List<UserEntity> userEntities) {
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

    public LiveData<List<ListActivity.Item>> getItems() {
        return liveData;
    }
}
