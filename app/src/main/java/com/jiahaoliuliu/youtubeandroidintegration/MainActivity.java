package com.jiahaoliuliu.youtubeandroidintegration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {

    private static final String TAG = "MainActivity";

    private static final String VIDEO_ID = "PVjiKRfKpPI";

    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private static final String DEVELOPER_KEY = "";
    private static final String YOUTUBE_DATA_KEY = "";

    private Context mContext;
    private YouTubePlayerSupportFragment mYoutubePlayerFragment;
    private YouTubePlayer mYouTubePlayer;

    // Check if it was full screen or not
    private boolean mIsFullScreen;

    // Views
    private TextView mTitleTextView;
    private TextView mDescriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        // Link the views
        mTitleTextView = (TextView) findViewById(R.id.titleTextView);
        mDescriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        mYoutubePlayerFragment = (YouTubePlayerSupportFragment)getSupportFragmentManager().findFragmentById(R.id.youtube_fragment);
        mYoutubePlayerFragment.initialize(DEVELOPER_KEY, this);

        new Thread(new FillYoutubeInfoThread()).start();
    }

    public class FillYoutubeInfoThread implements Runnable {

        @Override
        public void run() {
            fillYoutubeInfo();
        }
    }


    private void fillYoutubeInfo() {
        YouTube youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {}
            }
        ).setApplicationName(mContext.getString(R.string.app_name)).build();

        try{
            YouTube.Search.List query = youtube.search().list("id,snippet");
            query.setKey(YOUTUBE_DATA_KEY);
            query.setType("video");
            query.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)");
            query.setQ("drone");
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();
            Log.v(TAG, "List of search results retrieved. " + results.size());
            for (SearchResult searchResult : results) {
                Log.v(TAG, searchResult.toPrettyString());
            }
        }catch(IOException e){
            Log.e(TAG, "Could not initialize: ", e);
        }


    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {
            mYouTubePlayer = youTubePlayer;
            mYouTubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                @Override
                public void onFullscreen(boolean isFullScreen) {
                    mTitleTextView.setVisibility(isFullScreen? View.GONE : View.VISIBLE);
                    mDescriptionTextView.setVisibility(isFullScreen? View.GONE : View.VISIBLE);
                    mIsFullScreen = isFullScreen;
                }
            });

            mYouTubePlayer.cueVideo(VIDEO_ID);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(getString(R.string.error_player), youTubeInitializationResult.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            mYoutubePlayerFragment.initialize(DEVELOPER_KEY, this);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (mIsFullScreen) {
            mYouTubePlayer.setFullscreen(false);
            return;
        }
        super.onBackPressed();
    }
}
