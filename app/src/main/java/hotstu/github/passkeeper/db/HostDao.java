package hotstu.github.passkeeper.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

/**
 * @author hglf
 * @since 2017/12/28
 */
@Dao
public interface HostDao {
    @Insert(onConflict = IGNORE)
    long addHost(HostEntity host);

    @Query("select * from host where _id = :id")
    HostEntity findHostById(long id);

    @Delete
    int delHost(HostEntity host);

    @Query("select * from host")
    List<HostEntity> queryAllHosts();
}
