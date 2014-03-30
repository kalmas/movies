package net.kalmas.movies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class MovieListingDatabase {
	private static final String TAG = "MovieListingDatabase";
	
	public static final String KEY_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String KEY_DESCRIPTION = SearchManager.SUGGEST_COLUMN_TEXT_2;
	public static final String KEY_RELEASE_DATE = "release_date";
	
	
	private static final String DATABASE_NAME = "movieListing.db";
	private static final int DATABASE_VERSION = 1;
	private static final String FTS_VIRTUAL_TABLE = "FTSmovieListing";
	
	private final MovieListingOpenHelper movieListingOpenHelper;
	private static final HashMap<String,String> columnMap = buildColumnMap();


	public MovieListingDatabase(Context context) {
		movieListingOpenHelper = new MovieListingOpenHelper(context);
	}
	
    /**
     * Builds a map for all columns that may be requested, which will be given to the 
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include 
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(KEY_TITLE, KEY_TITLE);
        map.put(KEY_DESCRIPTION, KEY_DESCRIPTION);
        map.put(KEY_RELEASE_DATE, KEY_RELEASE_DATE);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }
    
    /**
     * Returns a Cursor positioned at the movie specified by rowId
     *
     * @param rowId id of movie to retrieve
     * @param columns The columns to include, if null then all are included
     * @return Cursor positioned to matching word, or null if not found.
     */
    public Cursor getMovie(String rowId, String[] columns) {
    	String selection = "rowid = ?";
    	String[] selectionArgs = new String[] { rowId };
    	
    	return query(selection, selectionArgs, columns);
    }
	
    /**
     * Returns a Cursor over all words that match the given query
     *
     * @param query The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all words that match, or null if none found.
     */
	public Cursor getMovieMatches(String query, String[] columns) {
		String selection = FTS_VIRTUAL_TABLE + " MATCH ?";
		String[] selectionArgs = new String[] { query + "*" };
		
		return query(selection, selectionArgs, columns);
	}
	
    /**
     * Performs a database query.
     * @param selection The selection clause
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param columns The columns to return
     * @return A Cursor over all rows matching the query
     */
    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
    	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
    	builder.setTables(FTS_VIRTUAL_TABLE);
    	builder.setProjectionMap(columnMap);
    	
    	Cursor cursor = builder.query(movieListingOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);
    	
        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }
	
	private static class MovieListingOpenHelper extends SQLiteOpenHelper {
		private final Context movieListingHelperContext;
		private SQLiteDatabase database;
		private static final String FTS_TABLE_CREATE = 
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                " USING fts3 (" +
                KEY_TITLE + ", " +
                KEY_DESCRIPTION + ", " +
                KEY_RELEASE_DATE + ");";
		
		public MovieListingOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			movieListingHelperContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			database = db;
			database.execSQL(FTS_TABLE_CREATE);
			loadMovieListing();
		}
		
        /**
         * Starts a thread to load the database table with movies
         */
        private void loadMovieListing() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        loadMovies();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
        
        private void loadMovies() throws IOException {
            Log.i(TAG, "Loading movies into db...");
            final Resources resources = movieListingHelperContext.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.movies);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] strings = TextUtils.split(line, "\\|");
                    if (strings.length < 3) continue;
                    long id = addMovie(strings[0].trim(), strings[1].trim(), strings[2].trim());
                    if (id < 0) {
                        Log.e(TAG, "unable to add movie: " + strings[0].trim());
                    }
                }
            } finally {
                reader.close();
            }
            Log.d(TAG, "DONE loading db.");
        }
        
        /**
         * Add a word to the dictionary.
         * @return rowId or -1 if failed
         */
        public long addMovie(String title, String description, String releaseDate) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_TITLE, title);
            initialValues.put(KEY_DESCRIPTION, description);
            initialValues.put(KEY_RELEASE_DATE, releaseDate);

            return database.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }
		
	}

}
