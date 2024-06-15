package com.application.reethau.com;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

	Context context;
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAMA = "reethau";

	public static final String TABLE_READ = "unread";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAMA, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public void createTable() {
		SQLiteDatabase db = this.getWritableDatabase();

		//db.execSQL("DROP TABLE IF EXISTS " + TABLE_READ);
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_READ + "(kode TEXT)");
		
		db.close();
	}

	public void insertData(String kode) {

		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put("kode", kode);
		db.insert(TABLE_READ, null, values);
		
		db.close();
	}

	public void deleteData() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_READ, null, null);
		db.close();
	}

	public boolean checkData(String kode) {
		
		boolean result = false;

		try {
			String sql = "SELECT kode FROM " + TABLE_READ + " WHERE kode='"+kode+"'";
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, null);
			result = cursor.getCount() > 0;
			cursor.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

}
