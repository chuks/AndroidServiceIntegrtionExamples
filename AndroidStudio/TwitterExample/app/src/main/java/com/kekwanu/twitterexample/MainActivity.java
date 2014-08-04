package com.kekwanu.twitterexample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    Context mContext;
    Twitter mTwitter;
    ListView mListView;
    Button button;
    LinearLayout progressLayout;
    EditText hashtag;
    String twitterHashtag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button)findViewById(R.id.submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTweets();
            }
        });

        hashtag = (EditText) findViewById(R.id.hashtag);
        progressLayout = (LinearLayout) findViewById(R.id.pbHeaderProgress);
        mListView = (ListView) findViewById(R.id.listview);
        mContext = getApplicationContext();
        mTwitter = getTwitter();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private Twitter getTwitter() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(getString(R.string.consumer_key));
        cb.setOAuthConsumerSecret(getString(R.string.consumer_secret));
        cb.setOAuthAccessToken(getString(R.string.access_token));
        cb.setOAuthAccessTokenSecret(getString(R.string.access_token_secret));
        return new TwitterFactory(cb.build()).getInstance();
    }

    private void searchTweets(){

        int duration = Toast.LENGTH_SHORT;
        if (hashtag.getText().toString().equals("")){

            Toast.makeText(this, "hashtag value cannot be empty", duration).show();
        }
        else if (hashtag.getText().toString().equals("")){
            Toast.makeText(this, "hashtag value cannot be empty", duration).show();
        }
        else {
            twitterHashtag = hashtag.getText().toString();
            progressLayout.setVisibility(View.VISIBLE);

            new Thread(new Task()).start();
        }
    }

    private void showTweetsAbout(String queryString) {
        List<Status> statuses = new ArrayList<Status>();
        ArrayList<String> statusTexts = new ArrayList<String>();

        try {
            statuses = mTwitter.search(new Query(queryString)).getTweets();

            for (Status s : statuses) {
                statusTexts.add(s.getText() + "\n\n");
            }
        }
        catch (Exception e) {
            statusTexts.add("Twitter query failed: " + e.toString());
        }

        final ArrayList<String> twitterStatus = statusTexts;

        //this must be done on the UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressLayout.setVisibility(View.GONE);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,R.layout.list_item, android.R.id.text1, twitterStatus);
                mListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });

    }

    class Task implements Runnable {
        @Override
        public void run() {
            showTweetsAbout(twitterHashtag);
        }

    }
}
