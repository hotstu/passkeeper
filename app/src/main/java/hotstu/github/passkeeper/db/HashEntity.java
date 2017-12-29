package hotstu.github.passkeeper.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * @author hglf
 * @since 2017/12/28
 */
@Entity(tableName = "master")
public class HashEntity {


    public HashEntity(String hash) {
        this.hash = hash;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public int id;
    public String hash;

}
