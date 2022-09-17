package com.example.minikers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Usage.class}, version = 13, exportSchema = false)

public abstract class UsageRoomDatabase extends RoomDatabase {

    public abstract AllDao allDao();

    private static volatile UsageRoomDatabase INSTANCE; //Makes this a Singleton to avoid having multiple instances of the database open at the same time
    //Using volatile keyword tells compiler/runtime processor to not reorder any instruction involving this variable

    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static UsageRoomDatabase getDatabase(final Context context) { //returns the Singleton
        if (INSTANCE == null) { //Create the database
            synchronized (UsageRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            UsageRoomDatabase.class, "word_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static boolean databaseExists(final Context context){
        if(INSTANCE == null)
            return false;
        return true;
    }
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

        }
    };
}