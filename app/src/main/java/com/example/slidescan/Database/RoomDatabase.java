package com.example.slidescan.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;

import com.example.slidescan.Models.Notes;

@Database(entities = Notes.class, version = 1, exportSchema = false)
public abstract class RoomDatabase extends androidx.room.RoomDatabase
{
    private static RoomDatabase database;
    private static String DATABASE_NAME = "Notes App";

    public synchronized static RoomDatabase getInstance(Context context)
    {
        if(database == null)
        {
            database = Room.databaseBuilder(context.getApplicationContext(), RoomDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries().fallbackToDestructiveMigration().build();
        }
        return database;
    }

    public abstract MainDataAccessObject mainDataAccessObject();
}
