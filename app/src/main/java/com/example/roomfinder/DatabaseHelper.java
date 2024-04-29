package com.example.roomfinder;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "RoomFinder.db";
    private static final int DATABASE_VERSION = 1;

    // Table and columns
    private static final String TABLE_USERS = "Users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PHONE_NUMBER = "phone_number";
    private static final String COLUMN_USER_TYPE = "user_type";
    private static final String COLUMN_PASSWORD_HASH = "password_hash";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_USERS + "(" +
                        COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME + " TEXT NOT NULL," +
                        COLUMN_EMAIL + " TEXT UNIQUE NOT NULL," +
                        COLUMN_PHONE_NUMBER + " TEXT UNIQUE NOT NULL," +
                        COLUMN_USER_TYPE + " TEXT NOT NULL," +
                        COLUMN_PASSWORD_HASH + " TEXT NOT NULL" +
                        ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public void addUser(String name, String email, String phoneNumber, String userType, String passwordHash) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE_NUMBER, phoneNumber);
        values.put(COLUMN_USER_TYPE, userType);
        values.put(COLUMN_PASSWORD_HASH, passwordHash); // You should hash the password before storing it

        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    @SuppressLint("Range")
    public String getUserType(String email, String passwordHash) {
        SQLiteDatabase db = this.getReadableDatabase();
        String userType = null;
        Cursor cursor = db.query("Users", // table name
                new String[]{"user_type"}, // columns to return
                "email = ? AND password_hash = ?", // where clause
                new String[]{email, passwordHash}, // where params
                null, // groupBy
                null, // having
                null); // orderBy

        if (cursor.moveToFirst()) {
            userType = cursor.getString(cursor.getColumnIndex("user_type"));
        }
        cursor.close();

        return userType;
    }
}
