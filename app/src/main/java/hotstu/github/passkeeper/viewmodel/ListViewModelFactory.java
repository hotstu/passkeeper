/*
 *  Copyright 2017 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package hotstu.github.passkeeper.viewmodel;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import hotstu.github.passkeeper.ListActivity;
import hotstu.github.passkeeper.db.AppDatabase;

/**
 * A creator is used to inject the application, repository, key into the ViewModel
 * <p>
 * This creator is to showcase how to inject dependencies into ViewModels. It's not
 * actually necessary in this case, as the product ID can be passed in a public method.
 */
public class ListViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final Application mApplication;

    private final AppDatabase mTasksRepository;

    private final String key;

    public ListViewModelFactory(Application application, AppDatabase repository, ListActivity activity) {
        mApplication = application;
        mTasksRepository = repository;
        key = activity.getIntent().getStringExtra("key");
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ListViewModel.class)) {
            //noinspection unchecked
            return (T) new ListViewModel(mApplication, mTasksRepository, key);
        } else
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
