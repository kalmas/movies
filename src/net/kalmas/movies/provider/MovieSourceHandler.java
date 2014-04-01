package net.kalmas.movies.provider;

import android.net.Uri;
import com.finchframework.finch.rest.RESTfulContentProvider;
import com.finchframework.finch.rest.ResponseHandler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Parses YouTube entity data and and inserts it into the finch video content
 * provider.
 */
public class MovieSourceHandler implements ResponseHandler {

//    private RESTfulContentProvider mFinchVideoProvider;
//
//    private String mQueryText;
//    private boolean isEntry;

    public MovieSourceHandler(RESTfulContentProvider restfulProvider, String queryText) {
//        mFinchVideoProvider = restfulProvider;
//        mQueryText = queryText;
    }

    /*
     * Handles the response from the YouTube gdata server, which is in the form
     * of an RSS feed containing references to YouTube videos.
     */
    @Override
    public void handleResponse(HttpResponse response, Uri uri) {
        try {
            int newCount = parse(response.getEntity());

            // only flush old state now that new state has arrived
            if (newCount > 0) {
                // deleteOld();
            }

        } catch (IOException e) {
            // use the exception to avoid clearing old state, if we can not
            // get new state.  This way we leave the application with some
            // data to work with in absence of network connectivity.

            // we could retry the request for data in the hope that the network
            // might return.
        }
    }


    private int parse(HttpEntity entity) throws IOException {
    	InputStream inputStream = entity.getContent();
        BufferedReader reader = new BufferedReader(
        		new InputStreamReader(inputStream, "UTF-8"), 8);
        
        StringBuilder sb = new StringBuilder();
        String line = null;
        while((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        
        String result = sb.toString();
        try {
			JSONObject jObject = new JSONObject(result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return 0;
    }
}
