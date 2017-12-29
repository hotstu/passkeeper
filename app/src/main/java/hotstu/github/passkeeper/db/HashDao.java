package hotstu.github.passkeeper.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

/**
 * @author hglf
 * @since 2017/12/28
 */
@Dao
public interface HashDao {
    @Query("select hash from master limit 1")
    String checkHash();

    @Insert(onConflict = IGNORE)
     void addHash(HashEntity hash);
}
