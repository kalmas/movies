package net.kalmas.movies;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyMoviesDatabase {
	
	public static final String KEY_MOVIE_LISTING_ID = "MovieListing_Id";
	
	private static final String DATABASE_NAME = "myMovies";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE = "myMovies";
	
	private final MyMoviesOpenHelper myMoviesOpenHelper;


	public MyMoviesDatabase(Context context) {
		myMoviesOpenHelper = new MyMoviesOpenHelper(context);
	}
	
	public long addMovie(ContentValues movie){
		SQLiteDatabase db = myMoviesOpenHelper.getWritableDatabase();
		return db.insert(TABLE, null, movie);
	}
	
	
	private static class MyMoviesOpenHelper extends SQLiteOpenHelper {
		private SQLiteDatabase database;
		private static final String TABLE_CREATE = 
                "CREATE TABLE " + TABLE +
                " (" + KEY_MOVIE_LISTING_ID + ");";
		
		public MyMoviesOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			database = db;
			database.execSQL(TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
		}

		
	}

}
