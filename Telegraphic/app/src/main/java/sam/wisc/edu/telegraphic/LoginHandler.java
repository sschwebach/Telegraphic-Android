package sam.wisc.edu.telegraphic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Sam on 9/6/2014.
 */
public class LoginHandler {
    private Context mContext;
    private LoginActivity mActivity;
    public boolean hasLogin = false;
    public String userName;
    public String pwordHash;
    public boolean remember = false;
    private String phoneNumber = "5555555555";
    private String uNameKey = "username";
    private String pWordKey = "passwordHash";
    private String pNumKey = "phoneNumber";
    private int type;
    private static final int USERLOGIN = 0;
    private static final int USERCREATE = 1;

    public LoginHandler(Context c, LoginActivity s){
        this.mContext = c;
        this.mActivity = s;
    }

    /**
     * Get any stored login information
     * @return True if login information is stored, false otherwise
     */
    public boolean getLogin(){
        //attempt to get login info from storage
        SharedPreferences settings = mActivity.getSharedPreferences(DataHolder.storageLoc, 0);
        String uName = settings.getString(uNameKey, null);
        String pWord = settings.getString(pWordKey, null);
        //if we have the login info, sest username and pwordHash to these values
        if (uName == null || pWord == null){
            return false;
        }
        this.userName = uName;
        this.pwordHash = pWord;
        DataHolder.username = uName;
        DataHolder.password = pWord;
        return true;
        //otherwise, return false
    }

    /**
     * Store the login information if it changed
     */
    public void storeLogin(){
        if (userName != null && pwordHash != null){
            SharedPreferences settings = mActivity.getSharedPreferences(DataHolder.storageLoc, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(uNameKey, this.userName);
            editor.putString(pWordKey, this.pwordHash);
            editor.commit();
        }
    }

    /**
     * Attempt to log into the server
     * @return True on successs, false on failure
     */
    public void login(){
        this.type = USERLOGIN;
        LoginQueryTask loginTask = new LoginQueryTask();
        loginTask.isPost = true;
        loginTask.addNVP(new BasicNameValuePair(this.uNameKey, this.userName));
        loginTask.addNVP(new BasicNameValuePair(this.pWordKey, hashPassword(this.pwordHash)));
        loginTask.execute(DataHolder.baseURL + DataHolder.loginURL);
    }

    public void register(){
        this.type = USERCREATE;
        LoginQueryTask registerTask = new LoginQueryTask();
        registerTask.isPost = true;
        registerTask.addNVP(new BasicNameValuePair(this.uNameKey, this.userName));
        registerTask.addNVP(new BasicNameValuePair(this.pNumKey, this.userName));
        registerTask.addNVP(new BasicNameValuePair(this.pWordKey, hashPassword(this.pwordHash)));
        registerTask.execute(DataHolder.baseURL + DataHolder.registerURL);
    }

    private class LoginQueryTask extends AsyncTask<String, Void, String> {
        JSONObject json = new JSONObject();
        boolean isPost = false;

        public void addNVP(NameValuePair toAdd){
            try{
                json.put(toAdd.getName(), toAdd.getValue());
            }catch (Exception e){
                Log.e("JSON Exception", e.toString());
            }
        }

        @Override
        protected void onPreExecute(){
            //might need something
        }

        @Override
        protected String doInBackground(String...arg0){
            if (isPost){
                try{
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost(arg0[0]);
                    StringEntity se = new StringEntity(json.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    HttpResponse response = client.execute(post);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    return reader.readLine();
                }catch (Exception e){
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
        protected void onPostExecute(String response){
            if (response != null){
                Log.e("RESPONSE", "" + response);
            }

            if (response == null){
                return;
            }
            JSONObject finalObject = new JSONObject();
            try{
                finalObject = new JSONObject(response);
            }catch (JSONException e){
                //lol
            }
            try{
                JSONObject currJSON;
                boolean success;
                String message;
                switch (type){
                    case USERLOGIN:
                        currJSON = finalObject;
                        success = currJSON.getBoolean("success");
                        message = currJSON.getString("message");
                        DataHolder.accessToken = currJSON.getString("accessToken");
                        //BEGIN TRANSITION TO NEW ACTIVITY
                        if (success){
                            storeLogin();
                            Intent intent = new Intent(mActivity, ImageListActivity.class);
                            mActivity.startActivity(intent);
                            //make an intent and all that shit
                        }
                        break;
                    case USERCREATE:
                        //do something
                        currJSON = finalObject;
                        success = currJSON.getBoolean("success");
                        message = currJSON.getString("message");
                        if (success) {
                            login();
                        }
                        break;
                    default:
                        //fuuuuuuck
                        break;
                }
            }catch (Exception e){
                //lol
            }
        }
    }

    public static String hashPassword(String in){
        String hashed = null;
        try{
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            //Add password bytes to digest
            md.update(in.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest();
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            hashed = sb.toString();
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }finally{
            return hashed;
        }
    }
}
