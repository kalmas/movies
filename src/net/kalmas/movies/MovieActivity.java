package net.kalmas.movies;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

public class MovieActivity extends Activity {
	
	private Date release;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.movie);
		
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        Uri uri = getIntent().getData();
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        
		if(cursor == null) {
			finish();
		} else {
			cursor.moveToFirst();
			
			final TextView title_text = (TextView) findViewById(R.id.title);
			final TextView description_text = (TextView) findViewById(R.id.description);
			final TextView releaseDate_text = (TextView) findViewById(R.id.release_date);
			
//			int tIndex = cursor.getColumnIndexOrThrow(MovieListingDatabase.KEY_TITLE);
//			int dIndex = cursor.getColumnIndexOrThrow(MovieListingDatabase.KEY_DESCRIPTION);
//			int rdIndex = cursor.getColumnIndexOrThrow(MovieListingDatabase.KEY_RELEASE_DATE);
			
			title_text.setText("Title");
			description_text.setText("Description");
			
			SimpleDateFormat format = new SimpleDateFormat("y-M-d");
			try {
				release = format.parse("Release Date");
				releaseDate_text.setText(release.toString());
			} catch(Exception e) {
				Log.e("MovieActivity", e.getMessage());
			}
		}
	}
	
//	 public void addToMyMovies(View view) {
//		 ContentValues movie = new ContentValues();
//         movie.put(MyMoviesDatabase.KEY_MOVIE_LISTING_ID, 123);
//		 getContentResolver().insert(MyMoviesProvider.INSERT_URI, movie);
//	 }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }
}
