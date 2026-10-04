package com.example.favorit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_FAVORITES = "favorites";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DETAILS = "details";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_IS_FAVORITE = "is_favorite";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_DETAILS + " TEXT, " +
                COLUMN_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_TIMESTAMP + " INTEGER, " +
                COLUMN_IS_FAVORITE + " INTEGER DEFAULT 1)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }

    public long insertItem(FavoriteItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, item.getTitle());
        values.put(COLUMN_DETAILS, item.getDetails());
        values.put(COLUMN_CATEGORY, item.getCategory());
        values.put(COLUMN_TIMESTAMP, item.getTimestamp());
        values.put(COLUMN_IS_FAVORITE, item.isFavorite() ? 1 : 0);

        long id = db.insert(TABLE_FAVORITES, null, values);
        db.close();
        return id;
    }

    public int updateItem(FavoriteItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, item.getTitle());
        values.put(COLUMN_DETAILS, item.getDetails());
        values.put(COLUMN_CATEGORY, item.getCategory());
        values.put(COLUMN_IS_FAVORITE, item.isFavorite() ? 1 : 0);

        int rows = db.update(TABLE_FAVORITES, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
        db.close();
        return rows;
    }

    public void updateFavoriteStatus(long id, boolean isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_FAVORITE, isFavorite ? 1 : 0);
        db.update(TABLE_FAVORITES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<FavoriteItem> getFilteredItems(String categoryFilter, String searchQuery) {
        List<FavoriteItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_FAVORITES + " WHERE 1=1");
        List<String> argsList = new ArrayList<>();

        if (categoryFilter != null && !categoryFilter.isEmpty() && !categoryFilter.equals("الكل")) {
            query.append(" AND ").append(COLUMN_CATEGORY).append(" = ?");
            argsList.add(categoryFilter);
        }

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            query.append(" AND (").append(COLUMN_TITLE).append(" LIKE ? OR ")
                 .append(COLUMN_DETAILS).append(" LIKE ?)");
            String searchPattern = "%" + searchQuery.trim() + "%";
            argsList.add(searchPattern);
            argsList.add(searchPattern);
        }

        query.append(" ORDER BY ").append(COLUMN_TIMESTAMP).append(" DESC");

        String[] args = argsList.toArray(new String[0]);
        Cursor cursor = db.rawQuery(query.toString(), args);

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String details = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DETAILS));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                boolean isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE)) == 1;

                list.add(new FavoriteItem(id, title, details, category, timestamp, isFavorite));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return list;
    }
}
