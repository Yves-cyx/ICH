package com.yingjian.feiyi.data;

import androidx.room.*;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;
import com.yingjian.feiyi.data.Models.Heritage;
import java.util.List;

@Database(entities = Heritage.class, version = 2, exportSchema = false)
public abstract class HeritageDatabase extends RoomDatabase {
    static final Migration ADD_COVER_URL = new Migration(1, 2) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE heritage ADD COLUMN coverUrl TEXT");
        }
    };

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

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void writeDetail(Heritage entry);
    }
}
