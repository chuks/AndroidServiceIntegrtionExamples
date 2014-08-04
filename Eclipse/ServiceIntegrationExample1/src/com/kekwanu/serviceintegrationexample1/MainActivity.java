package com.kekwanu.serviceintegrationexample1;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends Activity {
    private final String TAG = MainActivity.class.getCanonicalName();
    private TextView numFollowersTextView;
    private TextView numFollowingTextView;
    private TextView numLikesTextView;
    private EditText lowEditText;
    private EditText highEditText;
    private Button submitButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numFollowersTextView = (TextView)findViewById(R.id.num_followers);
        numFollowingTextView = (TextView)findViewById(R.id.num_following);
        numLikesTextView = (TextView) findViewById(R.id.num_likes);
        lowEditText = (EditText)findViewById(R.id.low);
        highEditText = (EditText)findViewById(R.id.high);
        submitButton = (Button)findViewById(R.id.submit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestData();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestData(){
        Log.i(TAG, "requestData");

        int duration = Toast.LENGTH_SHORT;

        if (lowEditText.getText().toString().equals("")){

            Toast.makeText(this, "low value cannot be empty", duration).show();
        }
        else if (highEditText.getText().toString().equals("")){
            Toast.makeText(this, "high value cannot be empty", duration).show();
        }
        else{
            String stringUrl = "http://hello.fanhour.com/api";
            int low = Integer.parseInt(lowEditText.getText().toString());
            int high = Integer.parseInt(highEditText.getText().toString());

            JSONObject objRequest = new JSONObject();
            try{
                objRequest.put("request", "test_api");
                objRequest.put("low", low);
                objRequest.put("high", high);

                new NetworkTask().execute(stringUrl, objRequest.toString());
            }
            catch(JSONException e){
                e.printStackTrace();
            }

        }

    }

    public HttpResponse makeHttpRequest(String uri, String json) throws ClientProtocolException,
            IOException, IllegalStateException, JSONException{

        HttpPost httpPost = new HttpPost(uri);

        if (!json.equals("")) {
            httpPost.setEntity(new StringEntity(json));
        }
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        return new DefaultHttpClient().execute(httpPost);

    }


    private void processResults(JSONObject asyncResult){
        Log.i(TAG, "processResults");

        if (asyncResult != null) {
            Log.i(TAG, "processResults - asyncResult is not null...");

            try {
                long num_followers = asyncResult.getLong("num_followers");
                long num_following = asyncResult.getLong("num_following");
                long num_likes = asyncResult.getLong("num_likes");

                Log.i(TAG, "processResults - num_followers is: "+num_followers);
                Log.i(TAG, "processResults - num_following is: "+num_following);

                numFollowersTextView.setText("Followers: "+Long.toString(num_followers));
                numFollowingTextView.setText("Following: "+Long.toString(num_following));
                numLikesTextView.setText("Likes: "+Long.toString(num_likes));
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            Log.i(TAG, "processResults - asyncResult is null...");

        }
    }


    //network stuff <params,progress,result>
    private class NetworkTask extends AsyncTask<String, Void, JSONObject> {
        private final String TAG = NetworkTask.class.getCanonicalName();

        @Override
        protected JSONObject doInBackground(String... params) {
            Log.i(TAG, "doInBackground");

            String result = null;
            JSONObject returnObj = null;

            // params comes from the execute() call: params[0] is the url.
            try {

                String url      = params[0];
                String paramStr = params[1];

                Log.i(TAG, "doInBackground - url is: "+url+" params is: "+paramStr);

                HttpResponse response = null;
                response = makeHttpRequest(url, paramStr);

                if (response != null){
                    Log.i(TAG, "doInBackground - got follows data");

                    result = EntityUtils.toString(response.getEntity());

                    JSONObject jObject = null;

                    try {
                        jObject         = new JSONObject(result);
                        String msg      = jObject.getString("msg");
                        String request  = jObject.getString("request");

                        if (msg.equals("success")){
                            Log.i(TAG, "doInBackground - call returned success, request is: "+request);

                            returnObj = jObject.getJSONObject("data");

                        }
                        else {
                            Log.i(TAG, "doInBackground - error: "+msg);

                        }

                    }
                    catch (JSONException e) {
                        Log.i(TAG, "doInBackground - catch block, err: "+e.getMessage());

                        e.printStackTrace();
                    }
                }
                else{
                    Log.i(TAG, "doInBackground - follows data is null");
                }
            }
            catch (IOException e) {
                Log.i(TAG, "doInBackground - exception: "+e.getMessage());
            }
            catch (JSONException e){
                Log.i(TAG, "doInBackground - exception: "+e.getMessage());
            }

            return returnObj;
        }

        @Override
        public void onPreExecute() {
            Log.i(TAG, "onPreExecute");

            progressBar.setVisibility(View.VISIBLE);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(JSONObject asyncResult) {
            Log.i(TAG, "onPostExecute - asyncResult is : "+asyncResult);

            progressBar.setVisibility(View.GONE);

            if (asyncResult != null) {
                processResults(asyncResult);
            }
            else{
                Log.i(TAG, "onPostExecute - asyncResult is : "+asyncResult);
            }

        }
    }

}
