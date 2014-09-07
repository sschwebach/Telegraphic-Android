package sam.wisc.edu.telegraphic;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by Sam on 9/6/2014.
 */
public class ImageListItemView implements AdapterView.OnClickListener{
    Context mContext;
    View mView;
    UserImage mImage;
    Intent intent;
    ListView dialogList;
    ArrayList<String> userList = new ArrayList<String>();

    public ImageListItemView(Context c, UserImage i){
        mContext = c;
        this.mImage = i;
    }

    @Override
    public void onClick(View v) {
        intent = new Intent(mContext, ImageCanvasActivity.class);
        intent.putExtra("existing", true);
        intent.putExtra("IID", mImage.IID);
        intent.putExtra("prev", mImage.prev);
        intent.putExtra("editTime", mImage.editTime);
        intent.putExtra("hopsRemaining", mImage.hopsRemaining);
        intent.putExtra("imageString", mImage.imageString);
        final Dialog dialog = new Dialog(mContext);
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
                ((Activity) mContext).startActivity(intent);
            }
        });
        populateList(dialog);
    }

    public View getView(){
        View newView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.list_image_view, null);
        ((TextView) newView.findViewById(R.id.text_last_user)).setText("Last edited by " + mImage.prev);
        ((TextView) newView.findViewById(R.id.text_remaining_time)).setText(mImage.hopsRemaining + " edits remaining");
        newView.setOnClickListener(this);
        return newView;

    }

    public void populateList(final Dialog dialog){
        GetUserRequest request = new GetUserRequest();
        request.execute(DataHolder.baseURL + DataHolder.userListURL);
        dialog.show();
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
                        mContext,
                        android.R.layout.simple_list_item_1,
                        userList);
                dialogList.setAdapter(arrayAdapter);

            }catch(Exception e){
                //lol
            }
        }
    }
}
