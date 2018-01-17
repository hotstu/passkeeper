package hotstu.github.passkeeper.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "user", foreignKeys = {
        @ForeignKey(entity = HostEntity.class,
                parentColumns = "_id",
                childColumns = "hostId",
                onUpdate = CASCADE,
                onDelete = CASCADE
        )}, indices = {@Index({"hostId"})})
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public int id;
    public String username;
    public int pwdLength;
    public int hostId;

    public UserEntity(int id, String username, int pwdLength, int hostId) {
        this.id = id;
        this.username = username;
        this.pwdLength = pwdLength;
        this.hostId = hostId;
    }
}
