package sam.wisc.edu.telegraphic;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

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

/**
 * Created by Sam on 9/7/2014.
 */
public class CombinedImageActivity extends Activity {
    boolean isRunning = true;
    boolean first = true;
    boolean inList = true;
    Button newImageButton;
    int listCount = 5;
    ImagesFragment imageFragment;
    CanvasFragment canvasFragment;
    Bitmap currImage;
    Context mContext;
    Timer refreshTimer;
    ListView dialogList;
    ArrayList<UserImage> userImages = new ArrayList<UserImage>();
    ArrayList<String> userList = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_image_combined);

        this.mContext = (Context) this;
        updateImageList();


    }

    @Override
    protected void onPause(){
        super.onPause();
        isRunning = false;
        Log.e("ONPAUSE", "PAUSE");
        this.refreshTimer.cancel();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        isRunning = false;
        this.refreshTimer.cancel();
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
        }, 0, 1000);
    }

    private void TimerMethod(){
        this.runOnUiThread(new Runnable(){
            @Override
            public void run(){
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pm.isScreenOn() && isRunning){
                    if (inList){
                        listCount -= 1;
                        if (listCount <= 0){
                            updateImageList();
                            listCount = 5;
                        }
                    }else{
                        if (canvasFragment != null) {
                            canvasFragment.time -= 1;
                            canvasFragment.timer.setText("" + canvasFragment.time);
                            if (canvasFragment.time <= 0) {
                                if (canvasFragment.image != null) {
                                    if (canvasFragment.image.hopsRemaining > 0) {
                                        Log.e("CONTINUE", "CONTINUE");
                                        continueImage();
                                    } else {
                                        Log.e("FINISH", "FINISH");
                                        finishImage();
                                    }
                                } else {
                                    createImage();
                                }
                            }
                        }
                    }

                }
            }
        });
    }

    public void updateImageList() {
        GetUserRequest imageRequest = new GetUserRequest();
        imageRequest.addNVP(new BasicNameValuePair("accessToken", DataHolder.accessToken));
        imageRequest.isPost = true;
        imageRequest.type = imageRequest.GETIMAGES;
        imageRequest.execute(DataHolder.baseURL + DataHolder.imageQueryURL);
    }

    public void prepare(final UserImage image){
        //prepare to get the new fragment to replace the list fragment
        Log.e("prepare", "preparing");
        final Dialog dialog = new Dialog(this);
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
                dialog.dismiss();
                startCanvasFragment(image, userList.get(position));
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                imageFragment.setListShown(true);
            }
        });
        populateList(dialog);
    }

    public void populateList(final Dialog dialog){
        GetUserRequest request = new GetUserRequest();
        request.type = request.GETUSERS;
        request.isPost = true;
        request.addNVP(new BasicNameValuePair("accessToken", DataHolder.accessToken));
        request.execute(DataHolder.baseURL + DataHolder.userListURL);
        dialog.show();
    }

    public void startCanvasFragment(UserImage image, String recipient){
        newImageButton.setVisibility(View.GONE);
        if (image != null) {
            this.currImage = Utilities.decodeBase64(image.imageString);
        }
        canvasFragment = new CanvasFragment();
        Bundle bundle = new Bundle();
        canvasFragment.setArguments(bundle);
        canvasFragment.recipient = recipient;
        canvasFragment.image = image;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.layout_for_fragments, canvasFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        inList = false;
    }

    public void returnToList(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.layout_for_fragments, imageFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        inList = true;
        updateImageList();
        newImageButton.setVisibility(View.VISIBLE);
    }


    public void finishImage(){
        GetUserRequest finishRequest = new GetUserRequest();
        finishRequest.type = finishRequest.FINISHIMAGES;
        finishRequest.isPost = true;
        finishRequest.addNVP(new BasicNameValuePair("accessToken", DataHolder.accessToken));
        finishRequest.addNVP(new BasicNameValuePair("uuid", canvasFragment.image.IID));
        finishRequest.execute(DataHolder.baseURL + DataHolder.imageCleanupURL);
    }

    public void continueImage(){
        GetUserRequest continueRequest = new GetUserRequest();
        continueRequest.type = continueRequest.CONTINUEIMAGES;
        continueRequest.isPost = true;
        continueRequest.addNVP(new BasicNameValuePair("accessToken", DataHolder.accessToken));
        continueRequest.addNVP(new BasicNameValuePair("uuid", canvasFragment.image.IID));
        continueRequest.addNVP(new BasicNameValuePair("nextUser", canvasFragment.recipient));
        continueRequest.addNVP(new BasicNameValuePair("image", Utilities.encodeTobase64(canvasFragment.mCanvas.canvasToBitmap())));
        continueRequest.execute(DataHolder.baseURL + DataHolder.imageUpdateURL);
    }

    public void createImage(){
        GetUserRequest createRequest = new GetUserRequest();
        createRequest.type = createRequest.CREATEIMAGES;
        createRequest.isPost = true;
        createRequest.addNVP(new BasicNameValuePair("accessToken", DataHolder.accessToken));
        createRequest.addNVP(new BasicNameValuePair("editTime", "10"));
        createRequest.addNVP(new BasicNameValuePair("hopsLeft", "5"));
        createRequest.addNVP(new BasicNameValuePair("nextUser", canvasFragment.recipient));
        createRequest.addNVP(new BasicNameValuePair("image", Utilities.encodeTobase64(canvasFragment.mCanvas.canvasToBitmap())));
        createRequest.execute(DataHolder.baseURL + DataHolder.imageCreateURL);
    }

    private class GetUserRequest extends AsyncTask<String, Void, String> {
        public static final int GETIMAGES = 0;
        public static final int GETUSERS = 1;
        public static final int FINISHIMAGES = 2;
        public static final int CREATEIMAGES = 3;
        public static final int CONTINUEIMAGES = 4;
        public int type;

        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        int requestControl = -1; //0 = request, 1 = query, 2 = release
        boolean isPost = false;
        JSONObject json = new JSONObject();

        public void addNVP(NameValuePair toAdd) {
            try {
                json.put(toAdd.getName(), toAdd.getValue());
            } catch (Exception e) {
            }
        }

        @Override
        protected void onPreExecute() {
            try {
                if (imageFragment != null) {
                    imageFragment.setListShown(false);
                }
            }catch(IllegalStateException e){

            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                if (isPost) {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost(params[0]);
                    StringEntity se = new StringEntity(json.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    HttpResponse response = client.execute(post);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    return reader.readLine();
                } else {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(params[0]);
                    HttpResponse response = client.execute(get);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    return reader.readLine();
                }
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                Log.e("Response", response);
            }
            JSONObject finalObject = new JSONObject();
            try {
                finalObject = new JSONObject(response);
            } catch (JSONException e) {

            }
            try {
                JSONObject currJSON;
                boolean success;
                currJSON = finalObject;
                switch (type) {
                    case GETIMAGES:
                        JSONArray imageList = currJSON.getJSONArray("items");
                        if (imageFragment == null) {
                            imageFragment = new ImagesFragment();
                        }
                        imageFragment.images.clear();
                        imageFragment.imageList.clear();
                        for (int i = 0; i < imageList.length(); i++) {
                            JSONObject currObj = imageList.getJSONObject(i);
                            String IID = currObj.getString("imageUUID");
                            String prevUser = currObj.getString("previousUser");
                            int editTime = currObj.getInt("editTime");
                            int hops = currObj.getInt("hopsLeft");
                            String imageString = currObj.getString("image");
                            userImages.add(new UserImage(IID, prevUser, editTime, hops, imageString));
                            imageFragment.images.add(prevUser);
                        }

                        Log.e("Made it here", "BEFORE");
                        FragmentManager fm = getFragmentManager();
                        Log.e("Made it here", "AFTER");
                        Log.e("Loading", "MADE IT HERE");

                        imageFragment.imageList = userImages;
                        if (first) {
                            fm.beginTransaction().add(R.id.layout_for_fragments, imageFragment).commit();
                            first = false;
                            newImageButton = (Button) findViewById(R.id.button_new_image);
                            newImageButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    prepare(null);
                                    currImage = null;
                                }
                            });
                        }
                        imageFragment.setListShown(true);
                        //get the images and update the appropriate lists
                        break;
                    case GETUSERS:
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
                                mContext,
                                android.R.layout.simple_list_item_1,
                                userList);
                        dialogList.setAdapter(arrayAdapter);
                        //get the users and update the appropriate lists
                        break;
                    default:
                        //finish with the image being viewed (either create continue or destroy)
                        success = Boolean.parseBoolean(currJSON.getString("success"));
                        if (success){
                            returnToList();
                        }
                        break;
                }


            } catch (Exception e) {
                Log.e("EXCEPTION", e.toString(), e);
            }
        }
    }
}
