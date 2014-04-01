package net.kalmas.movies.provider;

import java.io.File;

import com.finchframework.finch.rest.FileHandlerFactory;
import com.finchframework.finch.rest.RESTfulContentProvider;
import com.finchframework.finch.rest.ResponseHandler;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class MovieListingContentProvider extends RESTfulContentProvider {
	/*
	 * DB Constants 
	 */
	private static final String DATABASE_NAME = "movie.db";
	private static final int DATABASE_VERSION = 1;
	
	public static final String TABLE_SUGGESTION_NAME = "movieSuggestion";
	public static final String KEY_SUGGESTION_QUERY = "query";
	// I'll probably want my select query to be title AS SearchManager.SUGGEST_COLUMN_TEXT_1
	// public static final String KEY_SUGGESTION_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String KEY_SUGGESTION_TITLE = "title";
	public static final String KEY_SUGGESTION_MOVIE_ID = "movie_id";
	
	/*
	 * Web Service Constants
	 */
	private static final String MOVIE_FILE_CACHE = "movie_file_cache";
	private static final String WEB_URI = "http://kalmas.net/movies/complete?q=";
	
	
	
	// old consts
	public static String AUTHORITY = "net.kalmas.movies.provider.MovieListingContentProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/movie_listing");
	
	public static final String TITLE_MIME_TYPE = 
			ContentResolver.CURSOR_DIR_BASE_TYPE + "/net.kalmas.movies";
	public static final String DESCRIPTION_MIME_TYPE = 
			ContentResolver.CURSOR_DIR_BASE_TYPE + "/net.kalmas.movies";
	
//	private MovieListingDatabase movieListing;

	private static final int SEARCH_MOVIES = 0;
	private static final int GET_MOVIE = 1;
	private static final int SEARCH_SUGGEST = 2;
	private static UriMatcher sURIMatcher;
	
    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    static {
    	sURIMatcher =  new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
    	sURIMatcher.addURI(AUTHORITY, "movie_listing", SEARCH_MOVIES);
    	sURIMatcher.addURI(AUTHORITY, "movie_listing/#", GET_MOVIE);
        // to get suggestions...
    	sURIMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
    	sURIMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
    }
    
    private DatabaseHelper mOpenHelper;
    private SQLiteDatabase mDb;

    private static class DatabaseHelper extends SQLiteOpenHelper {

		private DatabaseHelper(Context context, 
        		String name, 
        		SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            createTable(sqLiteDatabase);
        }

        private void createTable(SQLiteDatabase sqLiteDatabase) {
            String createSuggestionTable =
            		"CREATE TABLE " + TABLE_SUGGESTION_NAME + " (" +
                            BaseColumns._ID +
                            " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            KEY_SUGGESTION_QUERY + " TEXT, " +
                            KEY_SUGGESTION_TITLE + " TEXT, " +
                            KEY_SUGGESTION_MOVIE_ID + " INTEGER" +
                            ");";
            sqLiteDatabase.execSQL(createSuggestionTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldv, int newv) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " +
                    TABLE_SUGGESTION_NAME + ";");
            createTable(sqLiteDatabase);
        }
    }
    
	@Override
	public boolean onCreate() {
        FileHandlerFactory fileHandlerFactory =
                new FileHandlerFactory(new File(getContext().getFilesDir(),
                		MOVIE_FILE_CACHE));
        setFileHandlerFactory(fileHandlerFactory);
        
        mOpenHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null);
        mDb = mOpenHelper.getWritableDatabase();

        return true;
	}
	
	@Override
	public SQLiteDatabase getDatabase() {
		return mDb;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder) {
		Cursor queryCursor;
		
		switch(sURIMatcher.match(uri)) {
			case SEARCH_SUGGEST:
			case SEARCH_MOVIES:
                String queryText = selectionArgs[0];
                
                if (queryText == null) {
                    return null;
                }
                
                String select = KEY_SUGGESTION_QUERY + " = '" +  queryText + "'";
                queryCursor = mDb.query(TABLE_SUGGESTION_NAME, 
                				null,
                                select,
                                null,
                                null,
                                null, 
                                null);
				
                // make the cursor observe the requested query
                queryCursor.setNotificationUri(getContext().getContentResolver(), uri);
                
                if (!"".equals(queryText)) {
                    asyncQueryRequest(queryText, WEB_URI + encode(queryText));
                }
                break;
			case GET_MOVIE:
				// return getMovie(uri);
				return null;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);		
		}
		
		return queryCursor;
	}

	@Override
	public String getType(Uri uri) {
		switch(sURIMatcher.match(uri)) {
			case SEARCH_MOVIES:
				return TITLE_MIME_TYPE;
			case GET_MOVIE:
				return DESCRIPTION_MIME_TYPE;
			case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
			default:
                throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}
	
    /**
     * Provides a handler that can parse YouTube gData RSS content.
     *
     * @param requestTag unique tag identifying this request.
     * @return a MovieSourceHandler object.
     */
    @Override
    protected ResponseHandler newResponseHandler(String requestTag) {
        return new MovieSourceHandler(this, requestTag);
    }
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Uri insert(Uri uri, ContentValues cv, SQLiteDatabase db) {
		throw new UnsupportedOperationException();
	}


}
