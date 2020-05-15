package com.clevertap.pushtemplates;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class DBHelper extends SQLiteOpenHelper {
    private static int DATABASE_VERSION = 1;
    private static String DATABASE_NAME = "pushtemplates.db";
    private static String TABLE_PT = "pushtemplates";
    private static String TABLE_PT_COLUMN_ID = "id";
    private static String TABLE_PT_COLUMN_PTID = "pt_id";
    private static String TABLE_PT_COLUMN_PTEXTRAS = "pt_json";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "" + "CREATE TABLE " + TABLE_PT + " (" +
                TABLE_PT_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " +
                ", " + TABLE_PT_COLUMN_PTID + " TEXT " +
                ", " + TABLE_PT_COLUMN_PTEXTRAS + " TEXT " +
                ");";
        SQLiteStatement stmt = db.compileStatement(query);
        stmt.execute();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PT);

        onCreate(db);
    }

    public void deletePT(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PT + " WHERE " + TABLE_PT_COLUMN_ID
                + " = " + id + ";");
        db.close();
    }

    public void savePT(String ptID, String jsonExtras) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "INSERT INTO " + TABLE_PT + " ( " +TABLE_PT_COLUMN_PTID + "," + TABLE_PT_COLUMN_PTEXTRAS + " ) VALUES ( ?, ? )";

        db.beginTransactionNonExclusive();

        SQLiteStatement stmt = db.compileStatement(sql);

        stmt.bindString(1, ptID);
        stmt.bindString(2, jsonExtras);

        stmt.execute();
        stmt.clearBindings();

        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();
    }

    public boolean isNotificationPresentInDB(String ptID) {

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_PT
                + " WHERE " + TABLE_PT_COLUMN_PTID + " = '" + ptID + "';";

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (cursor.getLong(cursor.getColumnIndex(TABLE_PT_COLUMN_ID)) != 0) {
                if(cursor.getString(cursor.getColumnIndex(TABLE_PT_COLUMN_PTID)).equalsIgnoreCase(ptID)) {
                    cursor.close();
                    db.close();
                    return true;
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return false;
    }


}