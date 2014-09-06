package sam.wisc.edu.telegraphic;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
    String imageString;
    UserImage toAdd;
    ArrayList<ImageListItemView> itemList = new ArrayList<ImageListItemView>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);
        this.mActivity = this;
        images = new ArrayList<UserImage>();
        friends = new ArrayList<String>();
        doSetup();
    }

    public void doSetup(){
        imageList = (LinearLayout) findViewById(R.id.linear_image_list);
        ImageQueryTask getImage = new ImageQueryTask();
        getImage.isPost = true;
        getImage.addNVP(new BasicNameValuePair("accessToken", DataHolder.accessToken));
        getImage.execute(DataHolder.baseURL + DataHolder.imageQueryURL);
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
                Log.e("JSON Exception", e.toString());
            }
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
                    Log.e("Exception", e.toString());
                }
            }else{
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(arg0[0]);
                    HttpResponse response = client.execute(get);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    return reader.readLine();
                } catch (Exception e) {
                    Log.e("BackgroundTaskException", e.toString());
                }
            }
            return null;
        }

        @Override
        protected  void onPostExecute(String response){
            if (response != null) {
                Log.e("RESPONSE", "" + response);
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
                        for (int i = 0; i < imageArray.length(); i++){
                            currJSON = imageArray.getJSONObject(i);
                            IID = currJSON.getString("imageUUID");
                            Log.e("IID", IID);
                            prevU = currJSON.getString("previousUser");
                            Log.e("prevU", prevU);
                            editTime = currJSON.getInt("editTime");
                            Log.e("editTime", "" + editTime);
                            hopsLeft = currJSON.getInt("hopsLeft");
                            Log.e("hopsLeft", "" + hopsLeft);
                            imageString = currJSON.getString("image");
                            Log.e("image", imageString);
                            toAdd = new UserImage(IID, prevU, editTime, hopsLeft, imageString);
                            //create each view and add it to the linear layout

                            ImageListItemView viewToAdd = new ImageListItemView(mActivity, toAdd);
                            itemList.add(viewToAdd);

                        }

                        mActivity.addViews(itemList);
                        Log.e("Size", "" + images.size());
                        Log.e("Image adapter images", "" + images.get(0).IID);

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
                Log.e("Exception", e.toString() + e.getStackTrace().toString());
            }
        }
    }

    public void addViews(ArrayList<ImageListItemView> toAdd){
        for (int i = 0; i < toAdd.size();  i++){
            imageList.addView(toAdd.get(i).getView());
        }
    }

}

