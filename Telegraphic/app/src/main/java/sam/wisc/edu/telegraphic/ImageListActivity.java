package sam.wisc.edu.telegraphic;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import sam.wisc.edu.telegraphic.R;

public class ImageListActivity extends Activity{
    ArrayList<UserImage> images;
    ArrayList<String> friends;
    ArrayList<View> viewList;
    ImageListActivity mActivity;
    LinearLayout imageList;
    String IID;
    String prevU;
    int editTime;
    int hopsLeft;
    Button newImage;
    boolean isRunning = true;
    String imageString;
    ListView dialogList;
    Timer refreshTimer;
    UserImage toAdd;
    Intent intent;
    ProgressDialog pDialog;
    ArrayList<String> userList = new ArrayList<String>();
    ArrayList<ImageListItemView> itemList = new ArrayList<ImageListItemView>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pDialog = new ProgressDialog(this);
        pDialog.setTitle("Refreshing");
        pDialog.setMessage("Please Wait");
        isRunning = true;
        setContentView(R.layout.activity_image_list);
        this.mActivity = this;
        images = new ArrayList<UserImage>();
        friends = new ArrayList<String>();


        newImage = (Button) findViewById(R.id.button_new_image);
        newImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(mActivity, ImageCanvasActivity.class);
                intent.putExtra("existing", false);
                final Dialog dialog = new Dialog(mActivity);
                dialog.setContentView(R.layout.user_dialog);
                dialog.setTitle("Send To:");
                dialogList = (ListView) dialog.findViewById(R.id.list_view_users);
                Button cancel = (Button) dialog.findViewById(R.id.button_dialog_cancel);

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        intent.putExtra("recipient", userList.get(position));
                        dialog.dismiss();
                        try {
                            (mActivity).startActivity(intent);
                        }catch (Exception e){
                            Log.e("EXCEPTION", e.toString());
                        }
                    }
                });

                populateList(dialog);
            }
        });
        //doSetup();
    }

    public void populateList(final Dialog dialog){
        GetUserRequest request = new GetUserRequest();
        request.execute(DataHolder.baseURL + DataHolder.userListURL);
        dialog.show();
    }

    @Override
    protected void onResume(){
        super.onResume();
        isRunning = true;
        this.refreshTimer = new Timer();
        this.refreshTimer.schedule(new TimerTask(){
            @Override
            public void run(){
                TimerMethod();
            }
        }, 0, 8000);
        if (imageList != null) {
            imageList.removeAllViews();
        }
        doSetup();
    }

    @Override
    protected void onPause(){
        super.onPause();
        isRunning = false;
        this.refreshTimer.cancel();
    }

    @Override
    protected void onStop(){
        super.onStop();
        isRunning = false;
        this.refreshTimer.cancel();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        isRunning = false;
        this.refreshTimer.cancel();
    }

    public void doSetup(){
        imageList = (LinearLayout) findViewById(R.id.linear_image_list);
        ImageQueryTask getImage = new ImageQueryTask();
        getImage.isPost = true;
        getImage.addNVP(new BasicNameValuePair("accessToken", DataHolder.accessToken));
        getImage.execute(DataHolder.baseURL + DataHolder.imageQueryURL);
    }

    private void TimerMethod(){
        this.runOnUiThread(new Runnable(){
            @Override
            public void run(){
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pm.isScreenOn() && isRunning){
                    imageList.removeAllViewsInLayout();
                    mActivity.doSetup();
                }
            }
        });
    }


    private class ImageQueryTask extends AsyncTask<String, Void, String> {
        JSONObject json = new JSONObject();
        boolean isPost = false;
        int type;
        public static final int GETIMAGES = 0;
        public static final int GETFRIENDS = 1;
        public static final int CREATEIMAGE = 2;

        public void addNVP(NameValuePair toAdd){
            try{
                json.put(toAdd.getName(), toAdd.getValue());
            }catch (Exception e){
            }
        }

        @Override
        protected void onPreExecute(){
            pDialog.show();
        }
        @Override
        protected String doInBackground(String...arg0){
            if (isPost) {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost(arg0[0]);
                    StringEntity se = new StringEntity(json.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    HttpResponse response = client.execute(post);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    return reader.readLine();
                } catch (Exception e) {
                }
            }else{
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(arg0[0]);
                    HttpResponse response = client.execute(get);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    return reader.readLine();
                } catch (Exception e) {
                }
            }
            return null;
        }

        @Override
        protected  void onPostExecute(String response){
            if (response != null) {
            }else{
                return;
            }
            JSONObject finalObject = new JSONObject();
            try{
                finalObject = new JSONObject(response);
            }catch(JSONException e){
                //lol
            }try{
                JSONObject currJSON;
                boolean success;
                String message;
                switch (type){
                    case GETIMAGES:
                        currJSON = finalObject;
                        success = currJSON.getBoolean("success");
                        JSONArray imageArray = currJSON.getJSONArray("items");
                        itemList.clear();
                        if (imageArray.length() > 0) {
                            for (int i = 0; i < imageArray.length(); i++) {
                                currJSON = imageArray.getJSONObject(i);
                                IID = currJSON.getString("imageUUID");
                                prevU = currJSON.getString("previousUser");
                                editTime = currJSON.getInt("editTime");
                                hopsLeft = currJSON.getInt("hopsLeft");
                                imageString = currJSON.getString("image");
                                toAdd = new UserImage(IID, prevU, editTime, hopsLeft, imageString);
                                //create each view and add it to the linear layout

                                ImageListItemView viewToAdd = new ImageListItemView(mActivity, toAdd);
                                itemList.add(viewToAdd);

                            }
                        }

                        mActivity.addViews(itemList);
                        if (images.size() > 0) {
                        }

                        break;
                    case GETFRIENDS:
                        currJSON = finalObject;
                        success = currJSON.getBoolean("success");
                        JSONArray friendArray = currJSON.getJSONArray("items");
                        for (int i = 0; i < friendArray.length(); i++){
                            friends.add(friendArray.getJSONObject(i).getString("username"));
                        }
                        break;
                    case CREATEIMAGE:
                        currJSON = finalObject;
                        success = currJSON.getBoolean("success");
                        break;

                }
            }catch (Exception e){
            }finally{
                pDialog.dismiss();
            }
        }
    }

    public void addViews(ArrayList<ImageListItemView> toAdd){
        imageList.removeAllViews();
        for (int i = 0; i < toAdd.size();  i++){
            imageList.addView(toAdd.get(i).getView());
        }
    }

    private class GetUserRequest extends AsyncTask<String, Void, String> {


        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        int requestControl = -1; //0 = request, 1 = query, 2 = release
        boolean isPost = false;
        JSONObject json = new JSONObject();

        public void addNVP(NameValuePair toAdd){
            try{
                json.put(toAdd.getName(), toAdd.getValue());
            }catch (Exception e){
            }
        }
        @Override
        protected String doInBackground(String... params) {
            try{
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(params[0]);
                HttpResponse response = client.execute(get);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                return reader.readLine();
            }catch (Exception e){
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response){
            if (response != null){
            }
            JSONObject finalObject = new JSONObject();
            try{
                finalObject = new JSONObject(response);
            }catch (JSONException e){

            }
            try{
                JSONObject currJSON;
                boolean success;
                currJSON = finalObject;
                success = Boolean.parseBoolean(currJSON.getString("success"));
                JSONArray userArray = currJSON.getJSONArray("items");
                userList.clear();
                for (int i = 0; i < userArray.length(); i++){
                    currJSON = userArray.getJSONObject(i);
                    String userName = currJSON.getString("username");
                    if (userName != DataHolder.username) {
                        userList.add(userName);
                    }
                }
                //now fill in the listview and all that sheeeeeiiiiit
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        mActivity,
                        android.R.layout.simple_list_item_1,
                        userList);
                dialogList.setAdapter(arrayAdapter);

            }catch(Exception e){
                //lol
            }
        }
    }
}

