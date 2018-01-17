/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hotstu.github.passkeeper.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.BuildConfig;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

@Database(entities = {UserEntity.class, HostEntity.class, HashEntity.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract HashDao hashModel();
    public abstract UserDao UserModel();
    public abstract HostDao HostModel();

    public static AppDatabase getInMemoryDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    if (BuildConfig.DEBUG) {
                        INSTANCE = Room.inMemoryDatabaseBuilder(context.getApplicationContext(), AppDatabase.class)
                                .allowMainThreadQueries()
                                .addMigrations(AppDatabase.MIGRATION_1_2)
                                .build();
                    } else {
                        INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                AppDatabase.class, "passkeeper.db")
                                .allowMainThreadQueries()
                                .addMigrations(AppDatabase.MIGRATION_1_2)
                                .build();
                    }
                }
            }

        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase _db) {
            //copy form generated sql by room
            _db.execSQL("CREATE TABLE IF NOT EXISTS `user` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT, `pwdLength` INTEGER NOT NULL, `hostId` INTEGER NOT NULL, FOREIGN KEY(`hostId`) REFERENCES `host`(`_id`) ON UPDATE CASCADE ON DELETE CASCADE )");
            _db.execSQL("CREATE  INDEX `index_user_hostId` ON `user` (`hostId`)");
            _db.execSQL("CREATE TABLE IF NOT EXISTS `host` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `hostname` TEXT)");
            _db.execSQL("CREATE TABLE IF NOT EXISTS `hash` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `hash` TEXT)");

            //
            _db.execSQL("INSERT  INTO `user` SELECT `_id` AS `_id`, `username` AS `username`, `pwdlenth` AS `pwdLength`, `hostid` AS `hostId` FROM `users` ");
            _db.execSQL("INSERT  INTO `host` SELECT `_id` AS `_id`, `hostname` AS `hostname` FROM `hosts` ");
            _db.execSQL("INSERT  INTO `hash` SELECT `_id` AS `_id`, `hash` AS `hash`  FROM `master` ");

            _db.execSQL("DROP TABLE IF EXISTS users");
            _db.execSQL("DROP TABLE IF EXISTS hosts");
            _db.execSQL("DROP TABLE IF EXISTS master");
        }
    };

    public static void destroyInstance() {
        INSTANCE = null;
    }
}