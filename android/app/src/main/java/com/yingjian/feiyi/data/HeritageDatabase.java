package com.yingjian.feiyi.data;

import androidx.room.*;
import com.yingjian.feiyi.data.Models.Heritage;
import java.util.List;

@Database(entities = Heritage.class, version = 1, exportSchema = false)
public abstract class HeritageDatabase extends RoomDatabase {
    public abstract Catalog catalog();

    @Dao
    public interface Catalog {
        @Query("SELECT * FROM heritage ORDER BY id")
        List<Heritage> read();

        @Query("SELECT * FROM heritage WHERE id = :id")
        Heritage detail(long id);

        @Query("DELETE FROM heritage")
        void clear();

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void write(List<Heritage> entries);
    }
}
