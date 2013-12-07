package net.kalmas.movies;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieListingProvider extends ContentProvider {
	public static String AUTHORITY = "net.kalmas.movies.MovieListingProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/movie_listing");

	
	public static final String TITLE_MIME_TYPE = 
			ContentResolver.CURSOR_DIR_BASE_TYPE + "/net.kalmas.movies";
	public static final String DESCRIPTION_MIME_TYPE = 
			ContentResolver.CURSOR_DIR_BASE_TYPE + "/net.kalmas.movies";
	
	private MovieListingDatabase movieListing;

	private static final int SEARCH_MOVIES = 0;
	private static final int GET_MOVIE = 1;
	private static final int SEARCH_SUGGEST = 2;
	private static final UriMatcher sURIMatcher = buildUriMatcher();
	
	
    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(AUTHORITY, "movie_listing", SEARCH_MOVIES);
        matcher.addURI(AUTHORITY, "movie_listing/#", GET_MOVIE);
        // to get suggestions...
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

        return matcher;
    }
    
	@Override
	public boolean onCreate() {
		movieListing = new MovieListingDatabase(getContext());
		return true;
	}
	
    /**
     * Handles all the movie listing searches and suggestion queries from the Search Manager.
     * When requesting a specific word, the uri alone is required.
     * When searching all of the dictionary for matches, the selectionArgs argument must carry
     * the search query as the first element.
     * All other arguments are ignored.
     */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder) {
		switch(sURIMatcher.match(uri)) {
			case SEARCH_SUGGEST:
			case SEARCH_MOVIES:
				if(selectionArgs == null) {
					throw new IllegalArgumentException("selectionArgs must be provided for the Uri: " + uri);
				}
				return getSuggestions(selectionArgs[0]);
			case GET_MOVIE:
				return getMovie(uri);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);		
		}
	}
	
	private Cursor getSuggestions(String query) {
		query = query.toLowerCase();
		String[] columns = new String[] {
			BaseColumns._ID,
			MovieListingDatabase.KEY_TITLE,
			MovieListingDatabase.KEY_DESCRIPTION,
			SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
		};
		
		return movieListing.getMovieMatches(query, columns);
	}
	
	private Cursor getMovie(Uri uri){
		String rowId = uri.getLastPathSegment();
		String[] columns = new String[] {
			MovieListingDatabase.KEY_TITLE,
			MovieListingDatabase.KEY_DESCRIPTION
		};
		
		return movieListing.getMovie(rowId, columns);
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

}
