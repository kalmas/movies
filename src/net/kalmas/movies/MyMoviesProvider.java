package net.kalmas.movies;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class MyMoviesProvider extends ContentProvider {
	public static String AUTHORITY = "net.kalmas.movies.MyMoviesProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/list");
	public static final Uri INSERT_URI = Uri.parse("content://" + AUTHORITY + "/insert");
		
	private MyMoviesDatabase myMovies;

	private static final int LIST_MOVIES = 0;
	private static final int ADD_MOVIE = 1;
	
	private static final UriMatcher sURIMatcher = buildUriMatcher();
	
    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "list", LIST_MOVIES);
        matcher.addURI(AUTHORITY, "insert", ADD_MOVIE);

        return matcher;
    }
    
	@Override
	public boolean onCreate() {
		myMovies = new MyMoviesDatabase(getContext());
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder) {
		switch(sURIMatcher.match(uri)) {
			case LIST_MOVIES:
				//return listMyMovies(uri);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);		
		}
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues movie) {
		switch(sURIMatcher.match(uri)) {
			case ADD_MOVIE:
				long id = myMovies.addMovie(movie);
				return uri;
			default:
				throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}


	@Override
	public String getType(Uri uri) {
		throw new IllegalArgumentException("Unknown URL " + uri);
	}
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		throw new UnsupportedOperationException();
	}

}
