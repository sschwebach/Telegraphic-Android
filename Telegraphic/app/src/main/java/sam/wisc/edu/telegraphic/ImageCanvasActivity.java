package sam.wisc.edu.telegraphic;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import sam.wisc.edu.telegraphic.R;

public class ImageCanvasActivity extends Activity {
    boolean existing;

    public static final int CREATENEW = 0;
    public static final int CONTINUE = 1;
    public static final int FINISH = 2;
    ImageCanvasActivity mActivity;
    String IID;
    String prev;
    int editTime;
    int hopsLeft;
    String recipient;
    String imageStr;
    ImageViewCanvas mCanvas;
    UserImage currImage;
    Button redButton;
    Button greenButton;
    Button blueButton;
    Button blackButton;
    Timer decTimer;
    TextView timeRemaining;
    int currTime;
    boolean isRunning = true;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pDialog = new ProgressDialog(this);
        pDialog.setTitle("Sending");
        pDialog.setMessage("Please Wait");
        mActivity = this;
        setContentView(R.layout.activity_image_canvas);
        mCanvas = new ImageViewCanvas(this);
        ((LinearLayout) findViewById(R.id.linear_canvas)).addView(mCanvas);
        //this.currImage = DataHolder.currImage;
        Intent intent = getIntent();
        existing = intent.getBooleanExtra("existing", false);
        recipient = intent.getStringExtra("recipient");
        editTime = intent.getIntExtra("editTime", 10);
        hopsLeft = intent.getIntExtra("hopsRemaining", 5);
        currTime = editTime + 1;
        if (existing){
            IID = intent.getStringExtra("IID");
            prev = intent.getStringExtra("prev");


            getImageStr();

        }
        doSetup();
    }

    public void doSetup(){
        timeRemaining = (TextView) findViewById(R.id.text_time_left);
        timeRemaining.setText("" + currTime);
        redButton = (Button) findViewById(R.id.button_red);
        greenButton = (Button) findViewById(R.id.button_green);
        blueButton = (Button) findViewById(R.id.button_blue);
        blackButton = (Button) findViewById(R.id.button_black);
        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCanvas.changeColor(Color.RED);
            }
        });
        greenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCanvas.changeColor(Color.GREEN);
            }
        });
        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCanvas.changeColor(Color.BLUE);
            }
        });
        blackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCanvas.changeColor(Color.BLACK);
            }
        });
    }

    public void getImageStr(){
        SubmitImageTask getImageTask = new SubmitImageTask();
        getImageTask.creation = true;
        getImageTask.addNVP(new BasicNameValuePair("accessToken", DataHolder.accessToken));
    }

    @Override
    public void onResume(){
        super.onResume();
        isRunning = true;
        this.decTimer = new Timer();
        this.decTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, 0, 1000);

    }

    @Override
    protected void onPause(){
        super.onPause();
        isRunning = false;
        this.decTimer.cancel();
    }

    @Override
    protected void onStop(){
        super.onStop();
        isRunning = false;
        this.decTimer.cancel();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        isRunning = false;
        this.decTimer.cancel();
    }

    private void TimerMethod(){
        this.runOnUiThread(new Runnable(){
            @Override
            public void run(){
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pm.isScreenOn() && isRunning){
                    if (currTime > 0) {
                        currTime -= 1;
                    }
                    timeRemaining.setText("" + currTime);
                }
                if (currTime <= 0){
                    //stop
                    if (existing && hopsLeft > 0){
                        SubmitImageTask imageTask = new SubmitImageTask();
                        imageTask.addNVP(new BasicNameValuePair("accessToken", DataHolder.accessToken));
                        imageTask.addNVP(new BasicNameValuePair("nextUser", recipient));
                        imageTask.addNVP(new BasicNameValuePair("image", Utilities.encodeTobase64(mCanvas.canvasToBitmap())));
                        imageTask.addNVP(new BasicNameValuePair("uuid", IID));
                        imageTask.execute(DataHolder.baseURL + DataHolder.imageUpdateURL);
                    }else if (!existing && hopsLeft > 0){
                        SubmitImageTask imageTask = new SubmitImageTask();
                        imageTask.addNVP(new BasicNameValuePair("accessToken", DataHolder.accessToken));
                        imageTask.addNVP(new BasicNameValuePair("nextUser", recipient));
                        imageTask.addNVP(new BasicNameValuePair("image", Utilities.encodeTobase64(mCanvas.canvasToBitmap())));
                        imageTask.addNVP(new BasicNameValuePair("editTime", "10"));
                        imageTask.addNVP(new BasicNameValuePair("hopsLeft", "5"));
                        imageTask.execute(DataHolder.baseURL + DataHolder.imageCreateURL);
                    }else {
                        SubmitImageTask imageTask = new SubmitImageTask();
                        imageTask.addNVP(new BasicNameValuePair("accessToken", DataHolder.accessToken));
                        imageTask.addNVP(new BasicNameValuePair("uuid", IID));
                        imageTask.execute(DataHolder.baseURL + DataHolder.imageCleanupURL);
                    }
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_canvas, menu);
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

    private class SubmitImageTask extends AsyncTask<String, Void, String>

    {
        JSONObject json = new JSONObject();
        boolean creation = false;
        public void addNVP(NameValuePair toAdd){
            try{
                json.put(toAdd.getName(), toAdd.getValue());
            }catch (Exception e){
                Log.e("JSON Exception", e.toString());
            }
        }

        @Override
        protected void onPreExecute(){
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            try{
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(params[0]);
                StringEntity se = new StringEntity(json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                HttpResponse response = client.execute(post);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                return reader.readLine();

            }catch (Exception e){
                //lol
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response){
            if (response != null){
                Log.e("RESPONSE", "" + response);
            }
            JSONObject finalObject = new JSONObject();
            try{
                finalObject = new JSONObject(response);
            }catch(JSONException e){
                //lol
            }
            try{
                if (creation){//get the image
                    JSONObject currJSON;
                    boolean success;
                    currJSON = finalObject;
                    JSONArray imageArray = currJSON.getJSONArray("items");
                    for (int i = 0; i < imageArray.length(); i++){
                        JSONObject current = imageArray.getJSONObject(i);
                        if (current.getString("imageUUID") == IID){
                            imageStr = current.getString("image");
                            mCanvas.oldImage = Utilities.decodeBase64(imageStr);
                        }
                    }
                }else {//send the image
                    JSONObject currJSON;
                    boolean success;
                    currJSON = finalObject;
                    success = Boolean.parseBoolean(currJSON.getString("success"));
                    if (success) {
                        pDialog.dismiss();
                        mActivity.finish();

                    }
                }
            }catch(Exception e){
                //lol
            }finally{
                pDialog.dismiss();
            }
        }
    }
}
