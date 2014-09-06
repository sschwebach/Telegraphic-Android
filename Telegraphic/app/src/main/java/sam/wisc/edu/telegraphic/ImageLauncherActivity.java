package sam.wisc.edu.telegraphic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.jar.Attributes;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

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

import sam.wisc.edu.telegraphic.R;

public class ImageLauncherActivity extends Activity implements ActionBar.TabListener {
    ArrayList<UserImage> images = new ArrayList<UserImage>();
    ArrayList<String> friends = new ArrayList<String>();
    static ListView imageList;
    static ListView friendList;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_launcher);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_launcher, menu);
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            Fragment toReturn = PlaceholderFragment.newInstance(position + 1);
            if (position == 1) {
                ImageQueryTask getImage = new ImageQueryTask();
                getImage.isPost = true;
                getImage.addNVP(new BasicNameValuePair("accessToken", DataHolder.accessToken));
                getImage.execute(DataHolder.baseURL + DataHolder.imageQueryURL);
            }

            return toReturn;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "Friends";
                case 1:
                    return "Pending";
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements AdapterView.OnItemClickListener{
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_image_list, container, false);
            if (getArguments().getInt(ARG_SECTION_NUMBER, 0) == 2) {
                //imageList = (ListView) rootView.findViewById(R.id.list_view_images);

            }


            return rootView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.e("Click", "click");
        }
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
                            String IID = currJSON.getString("imageUUID");
                            Log.e("IID", IID);
                            String prevU = currJSON.getString("previousUser");
                            Log.e("prevU", prevU);
                            int editTime = currJSON.getInt("editTime");
                            Log.e("editTime", "" + editTime);
                            int hopsLeft = currJSON.getInt("hopsLeft");
                            Log.e("hopsLeft", "" + hopsLeft);
                            String imageString = currJSON.getString("image");
                            Log.e("image", imageString);
                            images.add(new UserImage(IID, prevU, editTime, hopsLeft, imageString));

                        }
                        Log.e("Size", "" + images.size());
                        Log.e("Image adapter images", "" + images.get(0).IID);
                        ImageListAdapter adapter = new ImageListAdapter(images, imageList.getContext());
                        //imageList.setAdapter(adapter);
                        imageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Log.e("efalkjasdf", "oiahfoiaef;jsjfa");
                            }
                        });
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
                //lol
            }
        }
    }

}
